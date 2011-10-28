//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.common.ejb3;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Facade bean for all locale *client* functionality (i.e. consumption of locale
 * data as opposed to editing locale data).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional
public class LocaleFacadeBean extends NuclosFacadeBean implements LocaleFacadeLocal, LocaleFacadeRemote {

	private static final Logger LOG = Logger.getLogger(LocaleFacadeBean.class);

	private static final String R_PARENT = "parent";

	private static final String F_RESOURCEID = "resourceID";
	private static final String F_TEXT = "text";
	private static final String F_LOCALE = "locale";

	private static final TransactionSynchronization ts = new TransactionSynchronizationAdapter() {
		@Override
		public void afterCommit() {
			NuclosJMSUtils.sendMessage("flush", JMSConstants.TOPICNAME_LOCALE, JMSConstants.BROADCAST_MESSAGE);
		}
	};

	@Override
	public void flushInternalCaches() {
		internalFlush();
	}

	@Transactional
	private void internalFlush() {
		try {
			List<TransactionSynchronization> list = TransactionSynchronizationManager.getSynchronizations();
			if (!list.contains(ts)) {
				TransactionSynchronizationManager.registerSynchronization(ts);
			}
		}
		catch (IllegalStateException ex) {
			LOG.warn("Error on transaction synchronization registration.", ex);
		}
	}

	@Override
	public LocaleInfo getDefaultLocale() throws CommonFatalException {
		return getBestLocale(LocaleInfo.parseTag(getDefaultTag()));
	}

	private String getDefaultTag() throws CommonFatalException {
		String tag = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_DEFAULT_LOCALE);
		if (tag == null)
			throw new CommonFatalException("No default locale");
		return tag;
	}

	/**
	 * Fetch the user locale (server-internal)
	 *
	 * @return the user locale (or the default locale if not set)
	 */
	@Override
	public LocaleInfo getUserLocale() {
		return getBestLocale(LocaleInfo.parseTag(LocaleContextHolder.getLocale().toString()));
	}

	/**
	 * Return the complete resource bundle for a given locale info
	 * @param localeInfo  the locale info
	 * @return the resulting resource bundle
	 * @throws CommonFatalException
	 */
	@Override
	public HashResourceBundle getResourceBundle(LocaleInfo localeInfo) throws CommonFatalException {
		long start = System.currentTimeMillis();

		HashResourceBundle res = new HashResourceBundle();
		for (MasterDataVO mdvo : getResourcesAsVO(localeInfo)) {
			res.putProperty((String) mdvo.getField(F_RESOURCEID), StringUtils.unicodeDecodeWithNewlines((String) mdvo.getField(F_TEXT)));
		}
		LOG.info("Created resource cache for locale " + localeInfo.getTag() + " in " + (System.currentTimeMillis() - start) + " ms");
		return res;
	}

	@Override
	public DateFormat getDateFormat() {
		LocaleInfo userLocale = getUserLocale();
		try {
			return DateFormat.getDateInstance(DateFormat.DEFAULT, userLocale.toLocale());
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	private Map<LocaleInfo, String> getLocaleInfosWithParent() {
		Map<LocaleInfo, String> map = new HashMap<LocaleInfo, String>();
		for (MasterDataVO mdvo : getMasterDataFacade().getMasterData(NuclosEntity.LOCALE.getEntityName(), null, true)) {
			LocaleInfo localeInfo = new LocaleInfo(mdvo.getField("name", String.class),
				mdvo.getField("description", String.class), mdvo.getIntId(),
				mdvo.getField("language", String.class), mdvo.getField("country", String.class));
			String parentTag = mdvo.getField("parent", String.class);
			map.put(localeInfo, parentTag);
		}
		return map;
		/*
   	return NuclosSQLUtils.runSelect(NuclosDataSources.getDefaultDS(), "select * from t_md_locale",
   		new NuclosSQLUtils.ResultSetRunner<Map<LocaleInfo, String>>() {
				@Override
				public Map<LocaleInfo, String> perform(ResultSet rs) throws SQLException {
					while (rs.next()) {
						LocaleInfo localeInfo = new LocaleInfo(rs.getString("strdescription"), rs.getInt("intid"),
							rs.getString("strlanguagecode"), rs.getString("strcountrycode"));
						String parentTag = rs.getString("strparent");
						map.put(localeInfo, parentTag);
					}
					return map;
				}
   		});
		 */
	}

	private LocaleInfo getLocaleInfoForId(final Integer iLocale) {
		return CollectionUtils.findFirst(getLocaleInfosWithParent().keySet(), new Predicate<LocaleInfo>() {
			@Override
			public boolean evaluate(LocaleInfo li) {
				return li.localeId.equals(iLocale);
			}
		});
	}

	/**
	 * Return an overview of all defined locales
	 * @param includeNull  true, to include the null-locale, false to filter
	 * @return the locales
	 * @throws CommonFatalException
	 */
	@Override
	public Collection<LocaleInfo> getAllLocales(boolean includeNull) {
		Collection<LocaleInfo> locales = CollectionUtils.sorted(getLocaleInfosWithParent().keySet(), LocaleInfo.DESCRIPTION_COMPARATOR);
		if (!includeNull)
			locales.remove(LocaleInfo.I_DEFAULT);
		return locales;
	}


	@Override
	public Collection<MasterDataVO> getLocaleResourcesForParent(LocaleInfo localeInfo) {
		List<LocaleInfo> parentChain = getParentChain(localeInfo);
		return parentChain.size() >= 2 ? getResourcesAsVO(parentChain.get(parentChain.size() - 2)) : Collections.<MasterDataVO>emptyList();
	}

	public Collection<MasterDataVO> getResourcesAsVO(LocaleInfo localeInfo) {
		Map<String, MasterDataVO> res = new HashMap<String, MasterDataVO>();
		List<LocaleInfo> parentChain = getParentChain(localeInfo);
		Collections.reverse(parentChain);
		for(LocaleInfo li : parentChain) {
			CollectableSearchCondition cond = getResourcesSearchCondition(li);
			TruncatableCollection<MasterDataVO> lst = getMasterDataFacade().getMasterData(NuclosEntity.LOCALERESOURCE.getEntityName(), cond, true);
			for (MasterDataVO mdvo : lst) {
				res.put((String) mdvo.getField(F_RESOURCEID), mdvo);
			}
		}
		return res.values();
	}

	@Override
	public String getResourceById(LocaleInfo localeInfo, String sresourceId) {
		MasterDataVO mdvo = XMLEntities.getData(NuclosEntity.LOCALERESOURCE).findVO("resourceID", sresourceId, "locale", localeInfo.language);
		if (mdvo != null) {
			return mdvo.getField("text", String.class);
		}

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRTEXT", String.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("STRRESOURCEID", String.class), sresourceId),
			builder.equal(t.baseColumn("STRLOCALE", String.class), localeInfo.getTag())));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}

	@Override
	public Map<String, String> getAllResourcesById(String resourceId) {
		Map<String, String> map = new HashMap<String, String>();
		for (MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.LOCALERESOURCE).findAllVO("resourceID", resourceId)) {
			map.put(mdvo.getField("locale", String.class), mdvo.getField("text", String.class));
		}
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("STRLOCALE", String.class),
			t.baseColumn("STRTEXT", String.class));
		query.where(builder.equal(t.baseColumn("STRRESOURCEID", String.class), resourceId));
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			map.put(tuple.get(0, String.class), tuple.get(1, String.class));
		}
		return map;
	}

	@Override
	public Object modify(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {
		Object oId = this.getMasterDataFacade().modify(NuclosEntity.LOCALE.getEntityName(), mdvo, new DependantMasterDataMap());
		this.modifyDependants(mpDependants.getData(NuclosEntity.LOCALERESOURCE.getEntityName()), getLocaleInfoForId((Integer)mdvo.getField(R_PARENT + "Id")));

		return oId;
	}

	private void modifyDependants(Collection<EntityObjectVO> collmdvo, LocaleInfo parentLocale) {
		// TODO_AUTOSYNC
		throw new UnsupportedOperationException("TODO");
		/*
   	// TODO_AUTOSYNC: localetext "weggefallen"
      MasterDataMetaVO localeResMeta = getMasterDataFacade().getMetaData(E_LOCALERESOURCE);
      try {
         for (MasterDataVO mdvo : collmdvo) {

            Integer localeId = (Integer)mdvo.getField(R_LOCALE_ID);
            LocaleInfo localeInfo = getLocaleInfoForId(localeId);
            String resourceId = (String)mdvo.getField(F_RESOURCEID);
            String localeText = (String)mdvo.getField("localetext");

            if (mdvo.getId() != null) {
               if (getResourceById(parentLocale, resourceId) != null) {

                  MasterDataVO mdvo_db; // = this.getMasterDataFacade().get(E_LOCALERESOURCE, mdvo.getId());

                  CollectableSearchCondition cnd = SearchConditionUtils.and(
                  	getSearchCondition(localeInfo),
                  	SearchConditionUtils.newMDComparison(localeResMeta, F_RESOURCEID, ComparisonOperator.EQUAL, resourceId));

                  Collection<MasterDataVO> c = getMasterDataFacade().getMasterData(E_LOCALERESOURCE, cnd, true);
                  if(c.isEmpty())
                     mdvo_db = null;
                  else
                     mdvo_db = c.iterator().next();

                  Integer localeId_db = (Integer)mdvo_db.getField(R_LOCALE_ID);
                  String resourceId_db = (String)mdvo_db.getField(F_RESOURCEID);

                  if ((localeId_db).equals(parentLocale.localeId) && resourceId_db.equals(resourceId)) {
                     if (!mdvo.getField(F_LOCALETEXT).equals(mdvo_db.getField(F_TEXT))) {
                        this.update(resourceId, localeInfo, localeText);
                     }
                  }
                  else if (!mdvo.getField(F_LOCALETEXT).equals(mdvo_db.getField(F_TEXT))) {
                     this.update(resourceId, localeInfo, localeText);
                  }
               }
               else {
                  this.setResourceForLocale(resourceId, localeInfo, localeText);
               }
            }
            else {
               this.setResourceForLocale(null, localeInfo, localeText);
            }
         }
      }
      catch (Exception ex) {
         throw new CommonFatalException(ex);
      }
      internalFlush();
		 */
	}

	@Override
	public void update(String resourceId, LocaleInfo localeInfo, String text) {
		if (text != null) {
			DataBaseHelper.execute(DbStatementUtils.updateValues("T_MD_LOCALERESOURCE",
				"STRTEXT", text).where("STRRESOURCEID", resourceId, "STRLOCALE", localeInfo.getTag()));
			internalFlush();
		}
	}

	@Override
	public void deleteResource(String resourceId) {
		if (resourceId != null) {
			DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_MD_LOCALERESOURCE",
				"STRRESOURCEID", resourceId));
			internalFlush();
		}
	}

	@Override
	public void deleteResourceFromLocale(String resourceId, LocaleInfo localeInfo) {
		if (resourceId != null) {
			DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_MD_LOCALERESOURCE",
				"STRRESOURCEID", resourceId,
				"STRLOCALE", localeInfo.getTag()));
			internalFlush();
		}
	}


	/**
	 * get resource by the given id
	 */
	@Override
	public String getResource(String resourceId) {
		String text = getResourceById(getDefaultLocale(), resourceId);
		if (text == null) {
			text = getResourceById(LocaleInfo.I_DEFAULT, resourceId);
		}
		return text;
	}

	/**
	 * get resources by the given id
	 */
	@Override
	public Collection<MasterDataVO> getResourcesAsVO(Collection<String> coll, final LocaleInfo localeInfo) {
		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LOCALERESOURCE);

		return CollectionUtils.transform(coll, new Transformer<String, MasterDataVO>() {
			@Override
			public MasterDataVO transform(String resId) {
				try {
					Integer iId = getResourceIntId(resId, localeInfo);
					return MasterDataFacadeHelper.getMasterDataCVOById(mdmetavo, iId);
				}
				catch (CommonFinderException ex) {
					// This may never occur inside of a "repeatable read" transaction:
					throw new CommonFatalException(ex);
				}
			}});
	}

	@Override
	public String setResourceForLocale(String sResourceId, LocaleInfo localeInfo, String sText) {
		internalFlush();
		if (sText != null) {

			if(getResourceById(localeInfo, sResourceId) != null) {
				update(sResourceId, localeInfo, sText);
				return sResourceId;
			}
			else {
				return insert(sResourceId, localeInfo, sText, false);
			}
		}
		return null;
	}

	@Override
	public String insert(String sResourceId, LocaleInfo localeInfo, String sText) {
		return insert(sResourceId, localeInfo, sText, false);
	}

	public String insert(String sResourceId, LocaleInfo localeInfo, String sText, boolean internal) {
		final Integer nextId = DataBaseHelper.getNextIdAsInteger(internal ? "resids" : "idfactory");

		if (sResourceId == null)
			sResourceId = generateResourceId(nextId);

		DataBaseHelper.execute(DbStatementUtils.insertInto("T_MD_LOCALERESOURCE",
			"INTID", new DbId(),
			"STRRESOURCEID", sResourceId,
			"STRLOCALE", localeInfo.getTag(),
			"STRTEXT", sText,
			"DATCREATED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCREATED", getCurrentUserName(),
			"DATCHANGED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCHANGED", getCurrentUserName(),
			"INTVERSION", 1));

		return sResourceId;
	}

	@Override
	public String setDefaultResource(String sResourceId, String stext) {
		return setResourceForLocale(sResourceId, getDefaultLocale(), stext);
	}

	@Override
	public String createResource(String sText) {
		boolean inInternalTranslation = false;  // Novabit only

		internalFlush();
		if (!StringUtils.isNullOrEmpty(sText)) {
			if(inInternalTranslation) {
				return insert(null, LocaleInfo.I_DEFAULT, sText, true);
			}
			else {
				return insert(null, this.getDefaultLocale(), sText, false);
			}
		}
		return null;

	}

	@Override
	public void updateResource(String resourceId, String text) {
		if (text != null) {
			update(resourceId, getDefaultLocale(), text);
		}
		internalFlush();
	}

	private String generateResourceId(Integer iId) {
		return "R"+iId;
	}

	/**
	 * Return a specific locale
	 * @return the locale
	 * @throws CommonFatalException
	 */
	@Override
	public MasterDataVO getLocaleVO(LocaleInfo localeInfo) {
		LocaleInfo existing = getBestLocale(localeInfo);
		if (!existing.equals(localeInfo))
			return null;
		try {
			return getMasterDataFacade().get(NuclosEntity.LOCALE.getEntityName(), existing.localeId);
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public LocaleInfo getBestLocale(LocaleInfo localeInfo) {
		List<LocaleInfo> parentChain = getParentChain(localeInfo);
		return parentChain.get(0);
	}

	private Integer getResourceIntId(String resId, LocaleInfo localeInfo) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("STRRESOURCEID", String.class), resId),
			builder.equal(t.baseColumn("STRLOCALE", String.class), localeInfo.getTag())));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}

	private static CollectableSearchCondition getResourcesSearchCondition(LocaleInfo localeInfo) {
		// return SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NNuclosEntity.LOCALERESOURCE), "locale", localeInfo.localeId);
		return SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LOCALERESOURCE), F_LOCALE, ComparisonOperator.EQUAL, localeInfo.getTag());
	}

	@Override
	@RolesAllowed("Login")
	public List<LocaleInfo> getParentChain(final LocaleInfo localeInfo) {
		Map<LocaleInfo, String> localesWithParent = getLocaleInfosWithParent();
		Map<String, LocaleInfo> localesByTag = new HashMap<String ,LocaleInfo>();
		for (LocaleInfo li : localesWithParent.keySet())
			localesByTag.put(li.getTag(), li);

		Set<String> candidates = new HashSet<String>();
		List<LocaleInfo> chain = new ArrayList<LocaleInfo>();

		// given locale and all parents
		String tag = localeInfo.getTag();
		while (tag != null && candidates.add(tag)) {
			LocaleInfo li = localesByTag.get(tag);
			if (li != null)
				chain.add(li);
			tag = getParent(tag, localesWithParent.get(li));
		}
		// default locale and all parents
		tag = getDefaultTag();
		while (tag != null && candidates.add(tag)) {
			LocaleInfo li = localesByTag.get(tag);
			if (li != null)
				chain.add(li);
			tag = getParent(tag, localesWithParent.get(li));
		}
		// null locale
		if (candidates.add(LocaleInfo.I_DEFAULT_TAG))
			chain.add(localesByTag.get(LocaleInfo.I_DEFAULT_TAG));

		return chain;
	}

	private static String getParent(String tag, String parent) {
		return (parent != null) ? parent : LocaleInfo.getStandardParentTag(tag);
	}

	@Override
	public Date getLastChange() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Date> query = builder.createQuery(Date.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(builder.max(t.baseColumn("DATCHANGED", Date.class)));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	@Override
	public boolean isResourceId(String s) {
		if (s == null) {
			return false;
		}

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Long.class)).where(builder.equal(builder.upper(t.baseColumn("STRRESOURCEID", String.class)), s.toUpperCase()));
		return DataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}
}

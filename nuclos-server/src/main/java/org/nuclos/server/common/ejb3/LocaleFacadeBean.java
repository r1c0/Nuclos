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
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.TranslationVO;
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
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.autosync.SystemMasterDataVO;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.dal.provider.SystemEntityFieldMetaDataVO;
import org.nuclos.server.dal.provider.SystemEntityMetaDataVO;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Transactional(noRollbackFor= {Exception.class})
public class LocaleFacadeBean implements LocaleFacadeRemote {

	private static final Logger LOG = Logger.getLogger(LocaleFacadeBean.class);

	private static final String R_PARENT = "parent";

	private static final String F_RESOURCEID = "resourceID";
	private static final String F_TEXT = "text";
	private static final String F_LOCALE = "locale";
	
	private static final long UPDATE_TIME = 60 * 20 * 1000L;
	

	private static final LocaleInfo NULL_LOCALE_INFO = LocaleInfo.I_DEFAULT;
	private static final String NULL_LOCALE_STRING = LocaleInfo.I_DEFAULT_TAG;

	/**
	 * for simple caching implementation.
	 * TODO replace with real caching solution, i.e. with caching in Spring 3.1
	 */
	private static final Map<LocaleInfo, HashResourceBundle> CACHE = new ConcurrentHashMap<LocaleInfo, HashResourceBundle>();

	private static final TransactionSynchronization TX_SYNC = new TransactionSynchronizationAdapter() {
		@Override
		public void afterCommit() {
			LOG.info("afterCommit: " + this + " clear cache, JMS send flush message...");
			CACHE.clear();
			NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_LOCALE, null);
		}
	};
	
	//
	
	private final Map<LocaleInfo, String> localeInfos = new ConcurrentHashMap<LocaleInfo, String>();
	
	private volatile long lastUpdate = -1L;
	
	private MasterDataFacadeHelper masterDataFacadeHelper;
	
	private ServerParameterProvider serverParameterProvider;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public LocaleFacadeBean() {
	}
	
	@Autowired
	final void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.masterDataFacadeHelper = masterDataFacadeHelper;
	}
	
	@Autowired
	final void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	@Autowired
	final void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	public void flushInternalCaches() {
		internalFlush();
	}

	@Transactional(noRollbackFor= {Exception.class})
	private void internalFlush() {
		try {
			List<TransactionSynchronization> list = TransactionSynchronizationManager.getSynchronizations();
			if (!list.contains(TX_SYNC)) {
				TransactionSynchronizationManager.registerSynchronization(TX_SYNC);
			}
		}
		catch (IllegalStateException ex) {
			LOG.warn("Error on transaction synchronization registration.", ex);
		}
	}

	public LocaleInfo getDefaultLocale() throws CommonFatalException {
		return getBestLocale(LocaleInfo.parseTag(getDefaultTag()));
	}

	private String getDefaultTag() throws CommonFatalException {
		String tag = serverParameterProvider.getValue(ParameterProvider.KEY_DEFAULT_LOCALE);
		if (tag == null)
			throw new CommonFatalException("No default locale");
		return tag;
	}

	/**
	 * Fetch the user locale (server-internal)
	 *
	 * @return the user locale (or the default locale if not set)
	 */
	public LocaleInfo getUserLocale() {
		return getBestLocale(LocaleInfo.parseTag(LocaleContextHolder.getLocale().toString()));
	}

	/**
	 * To avoid StackOverflow. If current thread is already loading resources, return an empty resourcebundle.
	 */
	private ThreadLocal<Boolean> isLoadingResources = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * Return the complete resource bundle for a given locale info
	 * @param localeInfo  the locale info
	 * @return the resulting resource bundle
	 * @throws CommonFatalException
	 */
	public HashResourceBundle getResourceBundle(LocaleInfo localeInfo) throws CommonFatalException {
		HashResourceBundle result = localeInfo == null ? CACHE.get(localeInfo) : null;
		if (result == null) {
			// Avoid entering this block over and over again in different threads
			// when the CACHE hasn't been filled yes. In this case, waiting on sync lock
			// it the right thing to do.
			// 
			// This is an issue when assigning nuclet components to nuclets. (tp)
			synchronized (CACHE) {
				// Test if the resource bundle has already been loaded be an concurrent thread.
				result = CACHE.get(localeInfo);
				if (result == null) {
					// Check if current thread is already loading resources.
					// If yes, return an empty result to avoid infinite recursion through calls to getResourcesAsVO().
					if (!isLoadingResources.get()) {
						isLoadingResources.set(true);
						try {
							long start = System.currentTimeMillis();
		
							result = new HashResourceBundle();
							for (MasterDataVO mdvo : getResourcesAsVO(localeInfo)) {
								result.putProperty((String) mdvo.getField(F_RESOURCEID), StringUtils.unicodeDecodeWithNewlines((String) mdvo.getField(F_TEXT)));
							}
							LOG.info("Created resource cache for locale " + localeInfo.getTag() + " in " + (System.currentTimeMillis() - start) + " ms");
							CACHE.put(localeInfo, result);
						}
						finally {
							isLoadingResources.set(false);
						}
					}
				}
			}
		}
		if (result == null) {
			result = new HashResourceBundle();
		}
		return result;
	}

	public DateFormat getDateFormat() {
		LocaleInfo userLocale = getUserLocale();
		try {
			return DateFormat.getDateInstance(DateFormat.DEFAULT, userLocale.toLocale());
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	private Map<LocaleInfo, String> _getLocaleInfosWithParent() {
		Map<LocaleInfo, String> map = new HashMap<LocaleInfo, String>();
		for (MasterDataVO mdvo : masterDataFacade.getMasterData(NuclosEntity.LOCALE.getEntityName(), null, true)) {
			LocaleInfo localeInfo = new LocaleInfo(mdvo.getField("name", String.class),
				mdvo.getField("description", String.class), mdvo.getIntId(),
				mdvo.getField("language", String.class), mdvo.getField("country", String.class));
			String parentTag = mdvo.getField("parent", String.class);
			if (parentTag == null) {
				parentTag = NULL_LOCALE_STRING;
			}
			map.put(localeInfo, parentTag);
		}
		return map;
	}

	private Map<LocaleInfo, String> getLocaleInfosWithParent() {
		long current = System.currentTimeMillis();
		if (localeInfos.isEmpty() || current - lastUpdate > UPDATE_TIME) {
			synchronized (localeInfos) {
				localeInfos.clear();
				localeInfos.putAll(_getLocaleInfosWithParent());
				lastUpdate = current;
			}
		}
		return localeInfos;
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
	public Collection<LocaleInfo> getAllLocales(boolean includeNull) {
		Collection<LocaleInfo> locales = CollectionUtils.sorted(getLocaleInfosWithParent().keySet(), LocaleInfo.DESCRIPTION_COMPARATOR);
		if (!includeNull)
			locales.remove(LocaleInfo.I_DEFAULT);
		return locales;
	}


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
			TruncatableCollection<MasterDataVO> lst = masterDataFacade.getMasterData(NuclosEntity.LOCALERESOURCE.getEntityName(), cond, true);
			for (MasterDataVO mdvo : lst) {
				res.put((String) mdvo.getField(F_RESOURCEID), mdvo);
			}
		}
		return res.values();
	}

	@Cacheable(value="localeResource", key="#p0.cacheKey(#p1)")
	public String getResourceById(LocaleInfo localeInfo, String sresourceId) {
		MasterDataVO mdvo = XMLEntities.getData(NuclosEntity.LOCALERESOURCE).findVO("resourceID", sresourceId, "locale", localeInfo.language);
		if (mdvo != null) {
			return mdvo.getField("text", String.class);
		}

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRTEXT", String.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("STRRESOURCEID", String.class), sresourceId),
			builder.equal(t.baseColumn("STRLOCALE", String.class), localeInfo.getTag())));
		return CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
	}

	@Cacheable(value="localeAllResource", key="#p0", condition="#p0 != null")
	public Map<String, String> getAllResourcesById(String resourceId) {
		Map<String, String> map = new HashMap<String, String>();
		for (MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.LOCALERESOURCE).findAllVO("resourceID", resourceId)) {
			map.put(mdvo.getField("locale", String.class), mdvo.getField("text", String.class));
		}
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("STRLOCALE", String.class),
			t.baseColumn("STRTEXT", String.class));
		query.where(builder.equal(t.baseColumn("STRRESOURCEID", String.class), resourceId));
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			map.put(tuple.get(0, String.class), tuple.get(1, String.class));
		}
		return map;
	}

	public Object modify(MasterDataVO mdvo, DependantMasterDataMap mpDependants) 
			throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, 
			CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {
		
		Object oId = masterDataFacade.modify(NuclosEntity.LOCALE.getEntityName(), mdvo, new DependantMasterDataMap());
		this.modifyDependants(mpDependants.getData(NuclosEntity.LOCALERESOURCE.getEntityName()), getLocaleInfoForId((Integer)mdvo.getField(R_PARENT + "Id")));

		return oId;
	}

	private void modifyDependants(Collection<EntityObjectVO> collmdvo, LocaleInfo parentLocale) {
		// TODO_AUTOSYNC
		throw new UnsupportedOperationException("TODO");
	}

	@Caching(evict= {
			@CacheEvict(value="localeResource", key="#p1.cacheKey(#p0)"), 
			@CacheEvict(value="localeAllResource", key="#p0", condition="#p0 != null")
			})
	public void update(String resourceId, LocaleInfo localeInfo, String text) {
		if (text != null) {
			dataBaseHelper.execute(DbStatementUtils.updateValues("T_MD_LOCALERESOURCE",
				"STRTEXT", text).where("STRRESOURCEID", resourceId, "STRLOCALE", localeInfo.getTag()));
			internalFlush();
		}
	}

	@Caching(evict= {
			@CacheEvict(value="localeResource", allEntries=true), 
			@CacheEvict(value="localeAllResource", key="#p0", condition="#p0 != null")
			})
	public void deleteResource(String resourceId) {
		if (resourceId != null) {
			dataBaseHelper.execute(DbStatementUtils.deleteFrom("T_MD_LOCALERESOURCE",
				"STRRESOURCEID", resourceId));
			internalFlush();
		}
	}

	public void deleteResourceFromLocale(String resourceId, LocaleInfo localeInfo) {
		if (resourceId != null) {
			dataBaseHelper.execute(DbStatementUtils.deleteFrom("T_MD_LOCALERESOURCE",
				"STRRESOURCEID", resourceId,
				"STRLOCALE", localeInfo.getTag()));
			internalFlush();
		}
	}

	/**
	 * get resource by the given id
	 */
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
	public Collection<MasterDataVO> getResourcesAsVO(Collection<String> coll, final LocaleInfo localeInfo) {
		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LOCALERESOURCE);

		return CollectionUtils.transform(coll, new Transformer<String, MasterDataVO>() {
			@Override
			public MasterDataVO transform(String resId) {
				try {
					Integer iId = getResourceIntId(resId, localeInfo);
					return masterDataFacadeHelper.getMasterDataCVOById(mdmetavo, iId);
				}
				catch (CommonFinderException ex) {
					// This may never occur inside of a "repeatable read" transaction:
					throw new CommonFatalException(ex);
				}
			}});
	}

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

	public String insert(String sResourceId, LocaleInfo localeInfo, String sText) {
		return insert(sResourceId, localeInfo, sText, false);
	}

	@Caching(evict= {
			@CacheEvict(value="localeResource", key="#p1.cacheKey(#p0)"), 
			@CacheEvict(value="localeAllResource", key="#p0", condition="#p0 != null")
			})
	public String insert(String sResourceId, LocaleInfo localeInfo, String sText, boolean internal) {
		final Integer nextId = dataBaseHelper.getNextIdAsInteger(internal ? "resids" : "idfactory");

		if (sResourceId == null)
			sResourceId = generateResourceId(nextId);

		dataBaseHelper.execute(DbStatementUtils.insertInto("T_MD_LOCALERESOURCE",
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

	public String setDefaultResource(String sResourceId, String stext) {
		return setResourceForLocale(sResourceId, getDefaultLocale(), stext);
	}

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
	public MasterDataVO getLocaleVO(LocaleInfo localeInfo) {
		LocaleInfo existing = getBestLocale(localeInfo);
		if (!existing.equals(localeInfo))
			return null;
		try {
			return masterDataFacade.get(NuclosEntity.LOCALE.getEntityName(), existing.localeId);
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	public LocaleInfo getBestLocale(LocaleInfo localeInfo) {
		List<LocaleInfo> parentChain = getParentChain(localeInfo);
		return parentChain.get(0);
	}

	private Integer getResourceIntId(String resId, LocaleInfo localeInfo) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("STRRESOURCEID", String.class), resId),
			builder.equal(t.baseColumn("STRLOCALE", String.class), localeInfo.getTag())));
		return CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
	}

	private static CollectableSearchCondition getResourcesSearchCondition(LocaleInfo localeInfo) {
		// return SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NNuclosEntity.LOCALERESOURCE), "locale", localeInfo.localeId);
		return SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LOCALERESOURCE), F_LOCALE, ComparisonOperator.EQUAL, localeInfo.getTag());
	}

	@RolesAllowed("Login")
	public List<LocaleInfo> getParentChain(final LocaleInfo localeInfo) {
		Map<LocaleInfo, String> localesWithParent = getLocaleInfosWithParent();
		Map<String, LocaleInfo> localesByTag = new HashMap<String ,LocaleInfo>();
		
		for (LocaleInfo li : localesWithParent.keySet()) {
			localesByTag.put(li.getTag(), li);
		}

		Set<String> candidates = new HashSet<String>();
		List<LocaleInfo> chain = new ArrayList<LocaleInfo>();

		// given locale and all parents
		String tag = localeInfo.getTag();
		while (tag != null && candidates.add(tag)) {
			LocaleInfo li = localesByTag.get(tag);
			if (li != null) {
				chain.add(li);
				tag = getParent(tag, localesWithParent.get(li));
			}
			else {
				tag = getParent(tag, null);
			}
		}
		// default locale and all parents
		tag = getDefaultTag();
		while (tag != null && candidates.add(tag)) {
			LocaleInfo li = localesByTag.get(tag);
			if (li != null) {
				chain.add(li);
				tag = getParent(tag, localesWithParent.get(li));
			}
			else {
				tag = getParent(tag, null);
			}
		}
		// null locale
		if (candidates.add(LocaleInfo.I_DEFAULT_TAG))
			chain.add(localesByTag.get(LocaleInfo.I_DEFAULT_TAG));

		return chain;
	}

	private static String getParent(String tag, String parent) {
		return (parent != null && parent != NULL_LOCALE_STRING) ? parent : LocaleInfo.getStandardParentTag(tag);
	}

	public Date getLastChange() {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Date> query = builder.createQuery(Date.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(builder.max(t.baseColumn("DATCHANGED", Date.class)));
		return dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	public boolean isResourceId(String s) {
		if (s == null) {
			return false;
		}

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_MD_LOCALERESOURCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Long.class)).where(builder.equal(builder.upper(t.baseColumn("STRRESOURCEID", String.class)), s.toUpperCase()));
		return dataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}
	
	final String getCurrentUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}
	
	public void setResources(String entity, MasterDataVO md) {
		SystemEntityMetaDataVO meta = SystemMetaDataProvider.getInstance().getEntity(entity);
		Map<String, SystemEntityFieldMetaDataVO> fields = SystemMetaDataProvider.getInstance().getAllEntityFieldsByEntity(entity);
		
		Map<Integer, LocaleInfo> lis = CollectionUtils.transformIntoMap(getAllLocales(false), new Transformer<LocaleInfo, Integer>() {
			@Override
			public Integer transform(LocaleInfo i) {
				return i.localeId;
			}
		}, new Transformer<LocaleInfo, LocaleInfo>() {
			@Override
			public LocaleInfo transform(LocaleInfo i) {
				return i;
			}
		});
		
		for (SystemEntityFieldMetaDataVO field : fields.values()) {
			if (field.isResourceField()) {
				String resourceId = md.getField(field.getField(), String.class);
				for(TranslationVO vo : md.getResources()) {
					LocaleInfo li = lis.get(vo.getLocaleId());

					resourceId = setResourceForLocale(resourceId, li, vo.getLabels().get(field.getField()));
					LocaleUtils.setResourceIdForDbField(meta.getDbEntity(), md.getIntId(), field.getDbColumn(), resourceId);
				}
			}
		}
	}
	
	public List<TranslationVO> getResources(String entity, Integer id) throws CommonBusinessException {
		ArrayList<TranslationVO> result = new ArrayList<TranslationVO>();
		Map<String, SystemEntityFieldMetaDataVO> fields = SystemMetaDataProvider.getInstance().getAllEntityFieldsByEntity(entity);
		
		MasterDataFacadeLocal service = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		MasterDataVO md = service.get(entity, id);

		for (LocaleInfo li : getAllLocales(false)) {
			Map<String, String> labels = new HashMap<String, String>();
			
			for (SystemEntityFieldMetaDataVO field : fields.values()) {
				if (field.isResourceField()) {
					String resourceId = md.getField(field.getField(), String.class);
					if (resourceId != null) {
						labels.put(field.getField(), getResourceById(li, resourceId));
					}
				}
			}
			TranslationVO vo = new TranslationVO(li.localeId, li.title, li.language, labels);
			result.add(vo);
		}
		return result;
	}
	
	@PreDestroy
	public synchronized void destroy() {
		isLoadingResources.remove();
		isLoadingResources = null;
	}
	
}

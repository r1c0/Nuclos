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
package org.nuclos.server.genericobject.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.BadAttributeValueException;
import org.nuclos.server.attribute.BadGenericObjectException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.BusinessIDFactory;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Helper class for the GenericObjectFacade
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericObjectFacadeHelper {

	private static final Logger log = Logger.getLogger(GenericObjectFacadeHelper.class);

	/** @todo tune the DEFAULT_PAGESIZE (300 seems to be much better than 1000) */
	public static final int DEFAULT_PAGESIZE = 300;

	private MasterDataFacadeLocal mdFacade;

	private GenericObjectFacadeLocal goFacade;

	private LayoutFacadeLocal layoutFacade;

	public GenericObjectFacadeHelper() {
	}

	private GenericObjectFacadeLocal getGenericObjectFacade() {
		if (goFacade == null)
			goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

		return goFacade;
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		if (mdFacade == null)
			mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdFacade;
	}

	private LayoutFacadeLocal getLayoutFacade() {
		if (layoutFacade == null)
			layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
		return layoutFacade;
	}

	/**
	 * @param sSql
	 * @return empty leased objects (without attributes)
	 * @postcondition result != null
	 */
	 
//	static List<GenericObjectWithDependantsVO> getEmptyGenericObjectsBySQL(Integer iModuleId, String sUserName, String sSql, final Set<Integer> stRequiredAttributeIds) {
//		final List<GenericObjectWithDependantsVO> result = NuclosSQLUtils.runSelect(NuclosDataSources.getDefaultDS(), sSql,
//				new NuclosSQLUtils.AbstractTransformResultSetIntoList<GenericObjectWithDependantsVO>() {
//
//			@Override
//			public GenericObjectWithDependantsVO transform(ResultSet rs) throws SQLException {
//				final NuclosValueObject nvo = new NuclosValueObject(SQLUtils.getInteger(rs, "intid"),
//						rs.getTimestamp("datcreated"), rs.getString("strcreated"), rs.getTimestamp("datchanged"), rs.getString("strchanged"),
//						SQLUtils.getInteger(rs, "intversion"));
//
//				/** @todo optimize don't create empty DependantMasterDataMaps here */
//				return new GenericObjectWithDependantsVO(nvo, SQLUtils.getInteger(rs, "intid_t_md_module"),
//						SQLUtils.getInteger(rs, "intid_t_ud_go_parent"), SQLUtils.getInteger(rs, "intid_t_md_instance"), stRequiredAttributeIds,
//						rs.getBoolean("blndeleted"), new DependantMasterDataMap());
//			}
//		});
//
//		// detect all genericobjcts on which the user has read-permission ...
//		final Set<Integer> stIds = SecurityCache.getInstance().getReadableGenericObjectIdsByModule(sUserName, iModuleId);
//
//		// ... and remove all others from the result list
//		if (stIds != null) {
//			CollectionUtils.retainAll(result, new Predicate<GenericObjectWithDependantsVO>() {
//				public boolean evaluate(GenericObjectWithDependantsVO gowdvo) {
//					return (gowdvo == null) ? false : stIds.contains(gowdvo.getId());
//				}
//			});
//		}
//
//		assert result != null;
//		return result;
//	}
	

	/**
	 * @param sSql
	 * @return List<Integer> List of leased object ids found by the given SQL query.
	 */
	 /*
	public static List<Integer> getGenericObjectIdsBySQL(Integer iModuleId, String sUserName, String sSql) {
		return CollectionUtils.transform(getEmptyGenericObjectsBySQL(iModuleId, sUserName, sSql, Collections.<Integer>emptySet()), new NuclosValueObject.GetId());
	}
	*/

	/**
	 * efficiently creates an ArrayList by adding each element of the given Iterator to it.
	 * @param ksiter
	 * @return ArrayList<E>
	 * @postcondition result != null
	 */
	public static <E> ArrayList<E> newArrayList(KnownSizeIterator<? extends E> ksiter) {
		final ArrayList<E> result = new ArrayList<E>(ksiter.size());
		CollectionUtils.addAll(result, ksiter);
		return result;
	}

	public static Map<Integer, DynamicAttributeVO> getHistoricalAttributes(GenericObjectVO govo,
			Date dateHistorical, final DependantMasterDataMap mpDependantsResult) {

		final Map<Integer, DynamicAttributeVO> result = CollectionUtils.newHashMap();
		for (DynamicAttributeVO attrvo : govo.getAttributes()) {
			result.put(attrvo.getAttributeId(), attrvo);
		}

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_UD_LOGBOOK").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), // 0
			t.baseColumn("INTID_T_DP_VALUE_OLD", Integer.class), // 1
			t.baseColumn("INTID_EXTERNAL_OLD", Integer.class),   // 2
			t.baseColumn("STRVALUE_OLD", String.class),          // 3
			t.baseColumn("INTID_MD_EXTERNAL", Integer.class),     // 4
			t.baseColumn("STRMD_ACTION", String.class),          // 5
			t.baseColumn("INTID_T_AD_MASTERDATA", Integer.class), // 6
			t.baseColumn("INTID_T_AD_MD_FIELD", Integer.class));  // 7
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_UD_GENERICOBJECT", Integer.class), govo.getId()),
			builder.greaterThanOrEqualTo(t.baseColumn("DATCREATED", InternalTimestamp.class), builder.literal(InternalTimestamp.toInternalTimestamp(dateHistorical)))));
		query.orderBy(builder.desc(t.baseColumn("DATCREATED", InternalTimestamp.class)));
		
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				final Integer iAttributeId = tuple.get(0, Integer.class);
				// If the value of an existing attribute (not generated by a migration) is given...				
				if (iAttributeId != null) {
					try {
						AttributeCache.getInstance().getAttribute(iAttributeId);
					}
					catch(NuclosAttributeNotFoundException e) {
						// Attribute was deleted
						continue;
					}
					final Integer iValueId = (tuple.get(1) != null) ? tuple.get(1, Integer.class) : tuple.get(2, Integer.class);
					final String sValue = tuple.get(3, String.class);
					DynamicAttributeVO loavo = result.get(iAttributeId);
					if (loavo != null) {
						loavo.setValueId(iValueId);
						loavo.setCanonicalValue(sValue, AttributeCache.getInstance());
					}
					else {
						loavo = DynamicAttributeVO.createGenericObjectAttributeVOCanonical(iAttributeId, iValueId, sValue, AttributeCache.getInstance());
					}
					result.put(iAttributeId, loavo);
				}
				else {
					// If a change in a subform entry is given...
					final Integer iMasterDataRecordId = tuple.get(4, Integer.class);
					if (iMasterDataRecordId != null) {
						final String sAction = tuple.get(5, String.class);
						final Integer iMasterDataMetaId = tuple.get(6, Integer.class);
						final Integer iMasterDataMetaFieldId = tuple.get(7, Integer.class);
						//final Integer iRecordId = Helper.getInteger(rs, "intid_md_external");

						final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaDataById(iMasterDataMetaId);
						final String sEntity = mdmetavo.getEntityName();
						final MasterDataMetaFieldVO mdmetafieldvo = mdmetavo.getFieldById(iMasterDataMetaFieldId);
						final String sField = mdmetafieldvo.getFieldName();
						final String sValue = tuple.get(3, String.class);

						final Collection<EntityObjectVO> collmdvo = mpDependantsResult.getData(sEntity);

						// Find the masterdata cvo in the dependencies if possible
						EntityObjectVO mdvo = null;
						for (EntityObjectVO mdvo1 : collmdvo) {
							if (iMasterDataRecordId.equals(LangUtils.convertId(mdvo1.getId()))) {
								mdvo = mdvo1;
								break;
							}
						}

						if ("D".equals(sAction)) {
							// Entry has been deleted and must be recreated before value can be set
							if (mdvo == null) {
								mdvo = new EntityObjectVO();
								mdvo.setId(LangUtils.convertId(iMasterDataRecordId));
								mpDependantsResult.addData(sEntity, mdvo);
							}
							mdvo.getFields().put(sField, sValue);
						}
						else if ("C".equals(sAction)) {
							// Entry has been newly created and has simply to be deleted
							if (mdvo != null) {
								mpDependantsResult.removeKey(sEntity);
								collmdvo.remove(mdvo);
								mpDependantsResult.addAllData(sEntity, collmdvo);
							}
						}
						else if ("M".equals(sAction)) {
							// Existing entry has just been modified
							if (mdvo != null) {	//ought to be always so with this action
								mdvo.getFields().put(sField, sValue);
							}
						}
					}
					// else it must be a migrated value and can be ignored here
				}
			} catch (CommonValidationException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		
		return result;
	}

	public void createDependants(String entityname, int iGenericObjectId, DependantMasterDataMap mpDependants) throws CommonCreateException {
		if (!mpDependants.areAllDependantsNew()) {
			throw new IllegalArgumentException("Dependants must be new (must have empty ids).");
		}
		mpDependants.setParent(entityname, iGenericObjectId);

		try {
			// @todo: genericObjectId makes no sense as an entityName
			this.getMasterDataFacade().modifyDependants(entityname,iGenericObjectId,false,mpDependants);
		}
		catch (CommonFinderException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
		catch (CommonPermissionException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
		catch (CommonStaleVersionException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
		catch (CommonRemoveException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * fills the dependants of <code>lowdcvo</code> with the data from the required sub entities.
	 * @param lowdcvo
	 * @param stRequiredSubEntityNames
	 * @param subEntities
	 * @precondition stRequiredSubEntityNames != null
	 * @deprecated This method doesn't respect the foreign key field name. Replace with fillDependants().
	 */
	@Deprecated
	public void _fillDependants(GenericObjectWithDependantsVO lowdcvo,
			Set<String> stRequiredSubEntityNames, Map<EntityAndFieldName, String> subEntities, String username) throws CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new NullArgumentException("stRequiredSubEntityNames");
		}

		// load the system attribute nuclosProcess for this genericobject (if not already loaded);
		// this is necessary for getting the dependant subforms in the next step
		Integer iAttributeId = NuclosEOField.PROCESS.getMetaData().getId().intValue();
		if(!lowdcvo.wasAttributeIdLoaded(iAttributeId)) {
			lowdcvo.addAttribute(iAttributeId);

			Set<Integer> stAttributeId = new HashSet<Integer>();
			stAttributeId.add(iAttributeId);

			CollectableSearchExpression clctSearchExpression = new CollectableSearchExpression(new CollectableIdCondition(lowdcvo.getId()));
			List<GenericObjectWithDependantsVO> lsgowdvo = getGenericObjects(lowdcvo.getModuleId(), clctSearchExpression, Collections.<String>emptySet(), username);
			if (lsgowdvo.size() == 1 && lsgowdvo.get(0).getAttribute(iAttributeId) != null) {
				lowdcvo.setAttribute(lsgowdvo.get(0).getAttribute(iAttributeId));
			}
		}

		final Map<EntityAndFieldName, String> collSubEntities = (subEntities != null) ? subEntities :
			getLayoutFacade().getSubFormEntityAndParentSubFormEntityNames(Modules.getInstance().getEntityNameByModuleId(lowdcvo.getModuleId()),lowdcvo.getId(),false);


		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
			String sSubEntityName = eafn.getEntityName();
			// care only about subforms which are on the highest level and in the given set of entity names
			if (collSubEntities.get(eafn) == null && stRequiredSubEntityNames.contains(sSubEntityName)) {
				String sForeignKeyField = eafn.getFieldName();
				if (sForeignKeyField == null) {
					sForeignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
				}
				final Collection<EntityObjectVO> collmdvo = MasterDataFacadeHelper.getDependantMasterData(sSubEntityName, sForeignKeyField, lowdcvo.getId(), username);
				if (CollectionUtils.isNonEmpty(collmdvo)) {
					lowdcvo.getDependants().addAllData(sSubEntityName, collmdvo);

//					this is not necessary here, because the callers of this method don't work on child subform data
//					// now read all dependant data of the child subforms
//					for (MasterDataVO mdVO : collmdvo) {
//						mdfacade.readAllDependants(sSubEntityName, mdVO.getIntId(), mdVO.getDependants(), mdVO.isRemoved(), sSubEntityName, collSubEntities);
//					}
				}
			}
		}
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects
	 * @return list of generic object value objects with specified dependants and without parent objects!
	 * @postcondition result != null
	 */
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr, 
		Set<String> stRequiredSubEntityNames, String username) {

		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId));
		final List<GenericObjectWithDependantsVO> result = new ArrayList<GenericObjectWithDependantsVO>();
		
		for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpression(appendRecordGrants(clctexpr, eMeta.getEntity()), clctexpr.getSortingOrder()!=null)) {
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				_fillDependants(go, stRequiredSubEntityNames, null, username);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			result.add(go);
		}
		
//		final String sSql = getUnparser(username).unparseExpression(iModuleId, clctexpr, 0);
//
//		final KnownSizeIterator<GenericObjectWithDependantsVO> iter = getGenericObjectsIteratorBySQL(iModuleId, username, sSql,
//				DEFAULT_PAGESIZE, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects);
//
//		final List<GenericObjectWithDependantsVO> result = GenericObjectFacadeHelper.newArrayList(iter);

		assert result != null;
		return result;
	}

	/**
	 * @param govo
	 * @return the calculated attributes
	 * @postcondition result != null
	 */
//	private static Collection<AttributeCVO> getCalculatedAttributes(GenericObjectVO govo) {
//		final UsageCriteria usagecriteria = govo.getUsageCriteria(AttributeCache.getInstance());
//		final Collection<AttributeCVO> result;
//		try {
//			final GenericObjectMetaDataProvider lometaprovider = GenericObjectMetaDataCache.getInstance();
//			result = CollectionUtils.select(lometaprovider.getAttributeCVOsByLayoutId(lometaprovider.getBestMatchingLayoutId(usagecriteria, false)), new IsCalculated());
//		}
//		catch (CommonFinderException ex) {
//			throw new NuclosFatalException(ex);
//		}
//		assert result != null;
//		return result;
//	}

	/**
	 * @return the calculated attribute values
	 * @postcondition result != null
	 */
//	public Collection<DynamicAttributeVO> getCalculatedAttributeValues(final GenericObjectVO govo, String username) {
//		// get calculated attributes, if any
//		final Collection<AttributeCVO> collattrcvoCalculated = getCalculatedAttributes(govo);
//		final List<DynamicAttributeVO> dcollattrcvo = new ArrayList<DynamicAttributeVO>(collattrcvoCalculated.size());
//		if (!collattrcvoCalculated.isEmpty()) {
//			final String sSql = "select " + getCommaSeparatedEscapedAttributeNames(collattrcvoCalculated) + " from v_ud_go_general where intid = " + govo.getId();
//			Date startDate = new Date();
//			NuclosSQLUtils.runSelect(NuclosDataSources.getDefaultDS(), sSql, new ResultSetRunner<Void>() {
//				public Void perform(ResultSet rs) throws SQLException {
//					if (!rs.next()) {
//						throw new NuclosFatalException();
//					}
//					for (AttributeCVO attrcvo : collattrcvoCalculated) {
//						dcollattrcvo.add(new DynamicAttributeVO(attrcvo.getId(), null, AttributeCVO.getAttributeValueFromGeneralGenericObjectView(attrcvo, rs)));
//					}
//					return null;
//				}
//			});
//			Date endate = new Date();
//			NuclosPerformanceLogger.performanceLog(
//					startDate.getTime(),
//					endate.getTime(),
//					username,
//					govo.getId(),
//					govo.getModuleId(),
//					"Reading the calculated attributes ("+getCommaSeparatedEscapedAttributeNames(collattrcvoCalculated)+") for an objekt of type "+govo.getModuleId(),
//					"",
//					"");
//		}
//		return dcollattrcvo;
//	}

	// copied from the formerly ejbPostCreate from GenericObjectBean
	public void postCreate(GenericObjectVO govo) throws CommonCreateException {

		final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();

		final Set<Integer> stExcludedAttributeIds = getAttributeIdsExcludedInCreate(lometacache);

		// get set of attributes in the layout:
		final Set<String> stAttributesInLayout;
		try {
			stAttributesInLayout = lometacache.getBestMatchingLayoutAttributeNames(govo.getUsageCriteria(lometacache));
		}
		catch (CommonFinderException ex) {
			throw new CommonFatalException(ex);
		}

		// @todo try to refactor - the following is nearly duplicated in setValueObject
		final Collection<BadAttributeValueException> collex = new ArrayList<BadAttributeValueException>();
		// create leased object attributes not in exclude list:
		for (DynamicAttributeVO attrvo : govo.getAttributes()) {
			final Integer iAttributeId = attrvo.getAttributeId();
			if (!lometacache.getAttribute(iAttributeId).isCalculated() && !stExcludedAttributeIds.contains(iAttributeId)) {
				final String sAttributeName = lometacache.getAttribute(iAttributeId).getName();
				if (stAttributesInLayout.contains(sAttributeName)) {
					final String sCanonicalValue = attrvo.getCanonicalValue(lometacache);

					if (!StringUtils.isNullOrEmpty(sCanonicalValue)) {
						/** @todo consider attribute group permissions here ! */
						/*attrcvo = AttributeCache.getInstance().getAttribute(attrvo.getAttributeId());
											if (!SecurityCache.getInstance().getAttributegroupsRW(entityContext.getCallerPrincipal().getName()).contains(attrcvo.getAttributegroupId())) {*/
						/** todo activate following line when client does not deliver readonly attributes anymore to server */
						//throw new CreateException(attrcvo.getName()); //is a permission exception
						/*}*/

						try {
							getGenericObjectFacade().createGenericObjectAttribute(govo.getId(), iAttributeId, attrvo.getValueId(), sCanonicalValue, false);
						}
						catch (CreateException ex) {
							throw new CommonFatalException(ex);
						}
					}
				}
				else {
					// attribute not in layout - issue message:
					log.info("Es wird versucht, das Attribut " + sAttributeName +
							" in das Objekt mit der ID " + govo.getId() + " zu schreiben; es ist im Layout aber nicht verf\u00fcgbar.");
				}
			}
		}

		if (!collex.isEmpty()) {
			final BadGenericObjectException exCause = new BadGenericObjectException(govo.getId(), collex, govo.getAttributes().size());
			final CreateException ex = new CreateException(exCause.getMessage());
			ex.initCause(exCause);
			throw new CommonFatalException(ex);
		}

		// generate system identifier:
		final String sCanonicalValueSystemIdentifier = BusinessIDFactory.generateSystemIdentifier(govo.getModuleId());
		assert !StringUtils.isNullOrEmpty(sCanonicalValueSystemIdentifier);

		try {
			getGenericObjectFacade().createGenericObjectAttribute(govo.getId(), NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue(), null, sCanonicalValueSystemIdentifier, false);
		}
		catch (CreateException ex) {
			throw new CommonFatalException(ex);
		}

		// store system attributes:
		if (Modules.getInstance().isMainModule(govo.getModuleId())) {
			// system attributes are not generated for submodule objects:
			this.storeSystemAttributes(govo, lometacache, false);
		}
	}

	/**
	 * @param lometacache
	 * @return the list of attributes to exclude in create
	 */
	private static Set<Integer> getAttributeIdsExcludedInCreate(GenericObjectMetaDataCache lometacache) {
		final Set<Integer> result = lometacache.getReadOnlyAttributeIds();

		// @todo P2 this is for backwards compatibility only - check if this is correct!
		result.remove(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());

		return result;
	}

	private void storeSystemAttributes(GenericObjectVO govo, AttributeProvider attrprovider, boolean bLogbookTracking) {
		try {
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CREATEDAT.getMetaData().getId().intValue()), govo.getCreatedAt(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CREATEDBY.getMetaData().getId().intValue()), govo.getCreatedBy(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue()), govo.getChangedAt(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue()), govo.getChangedBy(), bLogbookTracking);
		}
		catch (CreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonValidationException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (NuclosBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * @param attrcvo
	 * @param oValue
	 * @param bLogbookTracking
	 * @throws CreateException
	 * @throws CommonValidationException
	 * @precondition attrcvo != null
	 * @precondition oValue != null
	 */
	private void storeAttributeValue(GenericObjectVO govo, AttributeCVO attrcvo, Object oValue, boolean bLogbookTracking) throws CreateException, CommonValidationException, NuclosBusinessException {
		if (oValue == null) {
			throw new NullArgumentException("oValue");
		}
		final DynamicAttributeVO attrvo = new DynamicAttributeVO(attrcvo.getId(), null, oValue);

//		try {
//			GenericObjectAttribute goa = getGenericObjectFacade().findAttributeByGoAndAttributeId(govo.getId(), attrcvo.getId());

			// entry for goa exists - update it:
//			getGenericObjectFacade().updateGenericObjectAttribute(goa, attrvo, govo.getId(), bLogbookTracking);
			getGenericObjectFacade().updateGenericObjectAttribute(attrvo, govo.getId(), bLogbookTracking);
//		}
//		catch (CommonFinderException ex) {
//			// entry for goa does not exist - create it:
//			getGenericObjectFacade().createGenericObjectAttribute(govo.getId(), attrvo.getAttributeId(), attrvo.getValueId(), attrvo.getCanonicalValue(AttributeCache.getInstance()), true);
//		}
	}

	// replaces the getValueObject() from GenericObjectBean
//	public GenericObjectVO getValueObject(GenericObjectVO govo) {
//		// 1. get the persistent attributes from the database:
//		final Collection<GenericObjectAttribute> attributes;
//		try {
//			attributes = getGenericObjectFacade().findAttributesByGenericObjectId(govo.getId());
//		}
//		catch (CommonFinderException ex) {
//			throw new NuclosFatalException(ex);
//		}
//
//		final GenericObjectVO result = getValueObject(govo,attributes);
//
//		// 2. add the calculated attributes, if any, to the result:
//		final Collection<AttributeCVO> collattrcvoCalculated = getCalculatedAttributes(result);
//		if (!collattrcvoCalculated.isEmpty()) {
//			this.addCalculatedValues(collattrcvoCalculated, result);
//		}
		
//		assert result != null;
//		return result;
//	}

	// replaces the getValueObject(Collection<DynamicAttributeVO> collattrvo) from GenericObjectBean
	
//	private GenericObjectVO getValueObject(GenericObjectVO govo, Collection<GenericObjectAttribute> attributes) {
//		if (attributes == null) {
//			throw new NullArgumentException("attributes");
//		}
//
//		final GenericObjectVO result = govo;
//
//		final boolean bUseOptimization = true;
//		if (bUseOptimization) {
//			assert result.getAttributes().isEmpty();
//			Collection<DynamicAttributeVO> attrVOList = new ArrayList<DynamicAttributeVO>();
//
//			for (GenericObjectAttribute attr : attributes) {
//				if(attr.getExternalValue() == null) {				
//					attrVOList.add(attr.getValueObject());
//				}
//				else {
//					DynamicAttributeVO vo = attr.getValueObject();
//					vo.setValue(attr.getExternalValue());
//					attrVOList.add(vo);
//				}
//			}
//			result.setAttributes(attrVOList);
//		}
//		else {
//			//add leased object attributes to vo for which user has appropriate attribute group permissions
//			for (GenericObjectAttribute attr : attributes) {
//				//			AttributeCVO attribute = AttributeCache.getInstance().getAttribute(attrvo.getAttributeId());
//				/** @todo consider attribute group permissions here ! */
//				/*if ((SecurityCache.getInstance().getAttributegroupsRO(entityContext.getCallerPrincipal().getName()).contains(attribute.getAttributegroupId())) || (attribute.isSystemAttribute())) {*/
////		this.mapAttributes.put(voGenericObjectAttribute.getAttributeId(), voGenericObjectAttribute);
//				result.setAttribute(attr.getValueObject());
//				/*}*/
//			}
//		}
//
//		assert result != null;
//		return result;
//	}
	
	
	/**
	 * 
	 */
	public void createLogBookEntryIfNecessary(final EntityObjectVO eoOld, final EntityObjectVO eoNew, final DynamicAttributeVO vo) {
		
		final String field = DalSupportForGO.getEntityFieldFromAttribute(vo.getAttributeId());
		final String newCanonicalValue = vo.getValue() != null ? DalSupportForGO.convertToCanonicalAttributeValue(vo.getAttributeId(), vo.getValue()) : "";
		
		if (eoOld != null) {
			final Integer oldInternalValueId = LangUtils.convertId(DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?null:eoOld.getFieldIds().get(field));
			final Integer oldExternalValueId = LangUtils.convertId(DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?eoOld.getFieldIds().get(field):null);
			final String oldCanonicalValue;
			if (NuclosEOField.CHANGEDBY.getMetaData().getField().equals(field)) {
				oldCanonicalValue = eoOld.getChangedBy();
			} else if (NuclosEOField.CHANGEDAT.getMetaData().getField().equals(field)) {
				oldCanonicalValue = DalSupportForGO.convertToCanonicalAttributeValue(Date.class, eoOld.getChangedAt());
			} else if (NuclosEOField.CREATEDBY.getMetaData().getField().equals(field)) {
				oldCanonicalValue = eoOld.getCreatedBy();
			} else if (NuclosEOField.CREATEDAT.getMetaData().getField().equals(field)) {
				oldCanonicalValue = DalSupportForGO.convertToCanonicalAttributeValue(Date.class, eoOld.getCreatedAt());
			} else {
				oldCanonicalValue = eoOld.getFields().get(field) != null ? DalSupportForGO.convertToCanonicalAttributeValue(vo.getAttributeId(), eoOld.getFields().get(field)) : "";
			}
			
			if (!oldCanonicalValue.equals(newCanonicalValue)) {
				getGenericObjectFacade().createLogbookEntry(LangUtils.convertId(eoOld.getId()), vo.getAttributeId(), null, null, null, null, 
					oldInternalValueId,
					oldExternalValueId, 
					oldCanonicalValue, 
					DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?null:vo.getValueId(), 
					DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?vo.getValueId():null, 
					newCanonicalValue);
			}
		} else if (eoNew != null) {
			getGenericObjectFacade().createLogbookEntry(LangUtils.convertId(eoNew.getId()), vo.getAttributeId(), null, null, null, null, 
				null,
				null, 
				null, 
				DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?null:vo.getValueId(), 
				DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?vo.getValueId():null, 
				newCanonicalValue);
		}

	}

	// replaces the setValueObject(GenericObjectVO govo) from GenericObjectBean
	//public void setValueObject(GenericObjectVO govo) throws CommonPermissionException, CommonValidationException, NuclosBusinessException {
	public void trackChangesToLogbook(EntityObjectVO eoOld, EntityObjectVO eoNew) throws CommonPermissionException, CommonValidationException, NuclosBusinessException {
		//this.increaseVersion();

//		try {
			//this.setDeleted(govo.isDeleted());

			// get set of attributes to exclude in update process
			final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();

			final Set<Integer> stExcludedAttributeIds = lometacache.getReadOnlyAttributeIds();

			// get set of attributes in the layout:
//			final Set<String> stAttributesInLayout = lometacache.getBestMatchingLayoutAttributeNames(govo.getUsageCriteria(lometacache));

			final boolean bLogbookTracking = Modules.getInstance().isLogbookTracking(LangUtils.convertId(MetaDataServerProvider.getInstance().getEntity(eoNew.getEntity()).getId()));

			// @todo try to refactor - the following is nearly duplicated in ejbPostCreate
			final Collection<BadAttributeValueException> collex = new ArrayList<BadAttributeValueException>();
			// update leased object attributes not in exclude list and not in layout:
//			for (DynamicAttributeVO attrvo : govo.getAttributes()) {
//				final Integer iAttributeId = attrvo.getAttributeId();
//				if (!lometacache.getAttribute(iAttributeId).isCalculated() && !stExcludedAttributeIds.contains(iAttributeId)) {
//					final String sAttributeName = lometacache.getAttribute(iAttributeId).getName();
//					if (stAttributesInLayout.contains(sAttributeName)) {
//						//NUCLEUSINT-1142
//						String sCanonicalValue = attrvo.getCanonicalValue(lometacache);
//						try {
//							/** @todo consider attribute group permissions here ! */
//							/*attribute = AttributeCache.getInstance().getAttribute(attrvo.getAttributeId());
//							if (!SecurityCache.getInstance().getAttributegroupsRW(entityContext.getCallerPrincipal().getName()).contains(attribute.getAttributegroupId())) {*/
//							/** @todo enable following lines as soon as CR has adjusted client (deliver only permitted attributes in cvo - performance reasons also!) */
//							/** @todo Yeah - blame it on me! ;) CR */
//							//entityContext.setRollbackOnly();
//							//throw new CommonPermissionException(attribute.getName());
//							/*}*/
//
//							if (attrvo.getId() == null) {
//								if (!StringUtils.isNullOrEmpty(sCanonicalValue)) {
//									if(attrvo.getValue() instanceof NuclosAttributeExternalValue) {
//										collex.addAll(getGenericObjectFacade().createGenericObjectAttributeExternal((NuclosAttributeExternalValue)attrvo.getValue(), govo.getId(), iAttributeId, attrvo.getValueId(), sCanonicalValue, false));
//									}
//									else {
//										if (lometacache.getAttribute(iAttributeId).getJavaClass().isAssignableFrom(NuclosPassword.class)) {
//											//NUCLEUSINT-1142
//											String storePasswordEncrypted = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_STORE_PASSWORD_ENCRYPTED);
//											sCanonicalValue = NuclosPassword.encryptPassword(attrvo.getValue(), storePasswordEncrypted);
//										}
//										/** @todo error occurs here if attribute is added to lo, but exists in db already (i.e. delete in screen, add in rule -> auftrags_nr_e_plus) solution: modify setAttribute in LOCVO */
//										collex.addAll(getGenericObjectFacade().createGenericObjectAttribute(govo.getId(), iAttributeId, attrvo.getValueId(), sCanonicalValue, false));
//									}
//								}
//							}
//							else {
//								NucleusDbo dbo = getDboFacade().findById(new GenericObjectAttribute(),attrvo.getId());
//								if (attrvo.isRemoved() || StringUtils.isNullOrEmpty(sCanonicalValue)) {
//									getDboFacade().delete(dbo);
//								}
//								else {
//									if(attrvo.getValue() instanceof NuclosAttributeExternalValue) {
//										getGenericObjectFacade().updateGenericObjectAttributeExternal((NuclosAttributeExternalValue)attrvo.getValue(), (GenericObjectAttribute)dbo,attrvo,govo.getId(),bLogbookTracking);
//									}
//									else {
//										if (lometacache.getAttribute(iAttributeId).getJavaClass().isAssignableFrom(NuclosPassword.class)) {
//											//NUCLEUSINT-1142
//											String storePasswordEncrypted = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_STORE_PASSWORD_ENCRYPTED);
//											Object maybeOldValue = ((GenericObjectAttribute)dbo).getValueObject().getValue();
//											if (maybeOldValue != null) {
//												if (!maybeOldValue.equals(attrvo.getValue())) {
//													attrvo.setValue(NuclosPassword.encryptPassword(attrvo.getValue(), storePasswordEncrypted));
//												}
//											} else
//												attrvo.setValue(NuclosPassword.encryptPassword(attrvo.getValue(), storePasswordEncrypted));												
//										}
//										
//										getGenericObjectFacade().updateGenericObjectAttribute((GenericObjectAttribute)dbo,attrvo,govo.getId(),bLogbookTracking);
//									}
//								}
//							}
//						}
//						catch (CreateException ex) {
//							throw new NuclosFatalException(ex);
//						}
//						catch (SQLException ex) {
//							throw new NuclosFatalException(ex);
//						}
//						catch (CommonFinderException ex) {
//							throw new NuclosFatalException(ex);
//						}
//						catch (CommonStaleVersionException ex) {
//							throw new NuclosFatalException(ex);
//						}
//						catch (RuntimeException ex) {
//							final String sMessage = "Error writing attribute " + sAttributeName + " (id " + iAttributeId +") for the generic object with id " + govo.getId() + " - value Id: " + attrvo.getValueId() + " - value: " + sCanonicalValue + ".";   
//								//"Fehler beim Schreiben des Attributs " + sAttributeName + " (Id " + iAttributeId + ") im GenericObject mit der Id " + govo.getId() + " - Value Id: " + attrvo.getValueId() + " - Value: " + sCanonicalValue + ".";
//							throw new CommonFatalException(sMessage, ex);
//						}
//					}
//					else {
//						// attribute not in layout - issue message:
//						log.info("Es wird versucht, das Attribut " + sAttributeName +
//								" in das Objekt mit der ID " + govo.getId() + " zu schreiben; es ist im Layout aber nicht verf\u00fcgbar.");
//					}
//				}
//			}
//			if (!collex.isEmpty()) {
//				throw new BadGenericObjectException(govo.getId(), collex, govo.getAttributes().size());
//			}
//
//			if (Modules.getInstance().isMainModule(govo.getModuleId())) {
//				storeSystemAttributes(govo, lometacache, bLogbookTracking);
//			}
//		}
//		catch (CommonFinderException ex) {
//			throw new NuclosFatalException(ex);
//		}
		
		if (bLogbookTracking) {
				for (String field : eoNew.getFields().keySet()) {
					if (!stExcludedAttributeIds.contains(MetaDataServerProvider.getInstance().getEntityField(eoNew.getEntity(), field).getId())) {
						if (MetaDataServerProvider.getInstance().getEntityField(eoNew.getEntity(), field).isLogBookTracking()){
							DynamicAttributeVO attr = DalSupportForGO.getDynamicAttributeVO(eoNew, field);
							this.createLogBookEntryIfNecessary(eoOld, eoNew, attr);
						}
					}
				}
			}
	}

	public void removeLogBookEntries(Integer govoId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_LOGBOOK",
			"INTID_T_UD_GENERICOBJECT", govoId);
		DataBaseHelper.getDbAccess().execute(stmt);
	}
	
	public void removeDependantTaskObjects(Integer govoId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_TODO_OBJECT",
			"INTID_T_UD_GENERICOBJECT", govoId);
		DataBaseHelper.getDbAccess().execute(stmt);
	}

	public void removeGroupBelonging(Integer govoId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_GO_GROUP",
			"INTID_T_UD_GENERICOBJECT", govoId);
		DataBaseHelper.getDbAccess().execute(stmt);
	}
	
	/**
	 * Adapter for KnownSizeIterator.
	 */
	static class KnownSizeIteratorAdapter<E> implements KnownSizeIterator<E> {
		private final Iterator<E> iter;
		private final int iSize;

		/**
		 * creates an Iterator over the given Collection that knows its size in advance.
		 * @precondition coll != null
		 * @postcondition this.size() == coll.size();
		 */
		KnownSizeIteratorAdapter(Collection<E> coll) {
			this.iter = coll.iterator();
			this.iSize = coll.size();

			assert this.size() == coll.size();
		}

		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public E next() {
			return this.iter.next();
		}

		@Override
		public void remove() {
			this.iter.remove();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return this.iSize;
		}

	}	// inner class KnownSizeIteratorAdapter
	
	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 */
	protected static CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, String entity) {
		return RecordGrantUtils.append(expr, entity);
	}

}	// class GenericObjectFacadeHelper

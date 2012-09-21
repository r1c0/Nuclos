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

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.BadAttributeValueException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for the GenericObjectFacade
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
@Component
public class GenericObjectFacadeHelper {

	private static final Logger LOG = Logger.getLogger(GenericObjectFacadeHelper.class);

	/** @todo tune the DEFAULT_PAGESIZE (300 seems to be much better than 1000) */
	public static final int DEFAULT_PAGESIZE = 300;
	
	//

	private MasterDataFacadeLocal mdFacade;

	private GenericObjectFacadeLocal goFacade;

	private LayoutFacadeLocal layoutFacade;
	
	private RecordGrantUtils grantUtils;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	/**
	 * @deprecated
	 */
	private MasterDataFacadeHelper masterDataFacadeHelper;

	public GenericObjectFacadeHelper() {
	}
	
	@Autowired
	void setRecordGrantUtils(RecordGrantUtils grantUtils) {
		this.grantUtils = grantUtils;
	}
	
	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.masterDataFacadeHelper = masterDataFacadeHelper;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}

	private GenericObjectFacadeLocal getGenericObjectFacade() {
		if (goFacade == null)
			goFacade = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

		return goFacade;
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		if (mdFacade == null)
			mdFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdFacade;
	}

	private LayoutFacadeLocal getLayoutFacade() {
		if (layoutFacade == null)
			layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
		return layoutFacade;
	}

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

		DbQueryBuilder builder = SpringDataBaseHelper.getInstance().getDbAccess().getQueryBuilder();
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
		
		for (DbTuple tuple : SpringDataBaseHelper.getInstance().getDbAccess().executeQuery(query)) {
			try {
				final Integer iAttributeId = tuple.get(0, Integer.class);
				// If the value of an existing attribute (not generated by a migration) is given...				
				if (iAttributeId != null) {
					try {
						AttributeCache.getInstance().getAttribute(iAttributeId);
					}
					catch(NuclosAttributeNotFoundException e) {
						// Attribute was deleted
						LOG.debug("getHistoricalAttributes: " + e);
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
							if (iMasterDataRecordId.equals(IdUtils.toLongId(mdvo1.getId()))) {
								mdvo = mdvo1;
								break;
							}
						}

						if ("D".equals(sAction)) {
							// Entry has been deleted and must be recreated before value can be set
							if (mdvo == null) {
								mdvo = new EntityObjectVO();
								mdvo.setId(IdUtils.toLongId(iMasterDataRecordId));
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

	public void createDependants(String entityname, int iGenericObjectId, DependantMasterDataMap mpDependants, Map<EntityAndFieldName, String> collSubEntities, String customUsage) throws CommonCreateException {
		if (!mpDependants.areAllDependantsNew()) {
			throw new IllegalArgumentException("Dependants must be new (must have empty ids).");
		}
		mpDependants.setParent(entityname, iGenericObjectId, collSubEntities);

		try {
			// @todo: genericObjectId makes no sense as an entityName
			this.getMasterDataFacade().createDependants(entityname,iGenericObjectId,false,mpDependants, customUsage);
		}
		catch (CommonPermissionException ex) {
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
	public void _fillDependants(GenericObjectWithDependantsVO lowdcvo, UsageCriteria usage,
			Set<String> stRequiredSubEntityNames, Map<EntityAndFieldName, String> subEntities, String username, String customUsage) throws CommonFinderException {

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
			List<GenericObjectWithDependantsVO> lsgowdvo = getGenericObjects(lowdcvo.getModuleId(), clctSearchExpression, Collections.<String>emptySet(), username, customUsage);
			if (lsgowdvo.size() == 1 && lsgowdvo.get(0).getAttribute(iAttributeId) != null) {
				lowdcvo.setAttribute(lsgowdvo.get(0).getAttribute(iAttributeId));
			}
		}

		final Map<EntityAndFieldName, String> collSubEntities = (subEntities != null) ? subEntities :
			// getLayoutFacade().getSubFormEntityAndParentSubFormEntityNames(Modules.getInstance().getEntityNameByModuleId(lowdcvo.getModuleId()),lowdcvo.getId(),false);
			getLayoutFacade().getSubFormEntityAndParentSubFormEntityNamesByGO(usage);


		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
			String sSubEntityName = eafn.getEntityName();
			// care only about subforms which are on the highest level and in the given set of entity names
			if (collSubEntities.get(eafn) == null && stRequiredSubEntityNames != null &&  (stRequiredSubEntityNames.isEmpty() || stRequiredSubEntityNames.contains(sSubEntityName))) {
				String sForeignKeyField = eafn.getFieldName();
				if (sForeignKeyField == null) {
					sForeignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
				}
				final Collection<EntityObjectVO> collmdvo = masterDataFacadeHelper.getDependantMasterData(
						sSubEntityName, sForeignKeyField, lowdcvo.getId(), username);
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
		Set<String> stRequiredSubEntityNames, String username, String customUsage) {

		final AttributeCache attributeCache = AttributeCache.getInstance();
		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId));
		final List<GenericObjectWithDependantsVO> result = new ArrayList<GenericObjectWithDependantsVO>();
		
		for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpression(
				appendRecordGrants(clctexpr, eMeta.getEntity()), clctexpr.getSortingOrder()!=null)) {
			
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(
					DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				_fillDependants(go, go.getUsageCriteria(attributeCache, customUsage), stRequiredSubEntityNames, null, username, customUsage);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			result.add(go);
		}
		
		assert result != null;
		return result;
	}

	/**
	 * @param lometacache
	 * @return the list of attributes to exclude in create
	 */
	private static Set<Integer> getAttributeIdsExcludedInCreate(GenericObjectMetaDataCache lometacache) {
		final Set<Integer> result = lometacache.getReadOnlyAttributeIds();

		// @todo P2 this is for backwards compatibility only - check if this is correct!
		result.remove(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		result.remove(NuclosEOField.STATEICON.getMetaData().getId().intValue());

		return result;
	}

	private void storeSystemAttributes(GenericObjectVO govo, AttributeProvider attrprovider, boolean bLogbookTracking) {
		try {
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CREATEDAT.getMetaData().getId().intValue()), govo.getCreatedAt(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CREATEDBY.getMetaData().getId().intValue()), govo.getCreatedBy(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue()), govo.getChangedAt(), bLogbookTracking);
			storeAttributeValue(govo, attrprovider.getAttribute(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue()), govo.getChangedBy(), bLogbookTracking);
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
	private void storeAttributeValue(GenericObjectVO govo, AttributeCVO attrcvo, Object oValue, boolean bLogbookTracking) throws CommonValidationException, NuclosBusinessException {
		if (oValue == null) {
			throw new NullArgumentException("oValue");
		}
		final DynamicAttributeVO attrvo = new DynamicAttributeVO(attrcvo.getId(), null, oValue);
	}
	
	public void createLogBookEntryIfNecessary(final EntityObjectVO eoOld, final EntityObjectVO eoNew, final DynamicAttributeVO vo) {
		
		final String field = DalSupportForGO.getEntityFieldFromAttribute(vo.getAttributeId());
		final String newCanonicalValue = vo.getValue() != null ? DalSupportForGO.convertToCanonicalAttributeValue(vo.getAttributeId(), vo.getValue()) : "";
		
		if (eoOld != null) {
			final Integer oldInternalValueId = IdUtils.unsafeToId(DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?null:eoOld.getFieldIds().get(field));
			final Integer oldExternalValueId = IdUtils.unsafeToId(DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?eoOld.getFieldIds().get(field):null);
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
				getGenericObjectFacade().createLogbookEntry(IdUtils.unsafeToId(eoOld.getId()), vo.getAttributeId(), null, null, null, null, 
					oldInternalValueId,
					oldExternalValueId, 
					oldCanonicalValue, 
					DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?null:vo.getValueId(), 
					DalSupportForGO.isEntityFieldForeign(vo.getAttributeId())?vo.getValueId():null, 
					newCanonicalValue);
			}
		} else if (eoNew != null) {
			getGenericObjectFacade().createLogbookEntry(IdUtils.unsafeToId(eoNew.getId()), vo.getAttributeId(), null, null, null, null, 
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

			final boolean bLogbookTracking = Modules.getInstance().isLogbookTracking(
					IdUtils.unsafeToId(MetaDataServerProvider.getInstance().getEntity(eoNew.getEntity()).getId()));

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
		dataBaseHelper.getDbAccess().execute(stmt);
	}
	
	public void removeDependantTaskObjects(Integer govoId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_TODO_OBJECT",
			"INTID_T_UD_GENERICOBJECT", govoId);
		dataBaseHelper.getDbAccess().execute(stmt);
	}

	public void removeGroupBelonging(Integer govoId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_GO_GROUP",
			"INTID_T_UD_GENERICOBJECT", govoId);
		dataBaseHelper.getDbAccess().execute(stmt);
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
	 * 
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, String entity) {
		return grantUtils.append(expr, entity);
	}

}	// class GenericObjectFacadeHelper

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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.exception.DalBusinessException;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.TruncatableCollectionDecorator;
import org.nuclos.common2.exception.CommonBusinessException;
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
import org.nuclos.server.common.BusinessIDFactory;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.GenericObjectProxyList;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectRelationVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all generic object management functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(GenericObjectFacadeLocal.class)
@Remote(GenericObjectFacadeRemote.class)
@Transactional
public class GenericObjectFacadeBean extends NuclosFacadeBean implements GenericObjectFacadeLocal, GenericObjectFacadeRemote {

	private final GenericObjectFacadeHelper helper = new GenericObjectFacadeHelper();

	@Override
	@RolesAllowed("Login")
	public GenericObjectMetaDataVO getMetaData() {
		return GenericObjectMetaDataCache.getInstance().getMetaDataCVO();
	}

	@Override
	@RolesAllowed("Login")
	public Map<Integer, String> getResourceMap() {
		return GenericObjectMetaDataCache.getInstance().getResourceMap();
	}

	/**
	 * @param iGenericObjectId
	 * @return the generic object with the given id
	 * @postcondition result != null
	 * @throws CommonFinderException if there is no object with the given id.
	 * @throws CommonPermissionException if the user doesn't have the permission to view the generic object with the given id.
	 * @nucleus.permission mayRead(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectVO get(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		return get(iGenericObjectId, true);
	}


	/**
	 * @param iGenericObjectId
	 * @param bCheckPermission
	 * @return the generic object with the given id
	 * @postcondition result != null
	 * @throws CommonFinderException if there is no object with the given id.
	 * @throws CommonPermissionException if the user doesn't have the permission to view the generic object with the given id.
	 * @nucleus.permission mayRead(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectVO get(Integer iGenericObjectId, boolean bCheckPermission) throws CommonFinderException, CommonPermissionException {
//		GenericObjectVO govo = MasterDataWrapper.getGenericObjectVO(getMasterDataFacade().get(ENTITY_NAME_GENERICOBJECT, iGenericObjectId));
//		govo = helper.getValueObject(govo);
		final GenericObjectVO govo = DalSupportForGO.getGenericObject(iGenericObjectId);

		if (bCheckPermission){
			checkReadAllowedForModule(govo.getModuleId(), iGenericObjectId);
			RecordGrantUtils.checkInternal(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(govo.getModuleId())).getEntity(), LangUtils.convertId(iGenericObjectId));
		}
		assert govo != null;
		return govo;
	}

	private void checkForStaleVersion(GenericObjectVO oldGO, GenericObjectVO newGO) throws CommonStaleVersionException {
		if (oldGO.getVersion() != newGO.getVersion()) {
			throw new CommonStaleVersionException();
		}
	}

	/**
	 * @param iGenericObjectId
	 * @param stRequiredSubEntityNames the dependant objects with the subentites with the given names will be fetched. Null means all.
	 * @return the complete generic object with the given id, along with its required dependants.
	 * @throws CommonFinderException if no such object was found.
	 * @throws CommonPermissionException if the user doesn't have the permission to view the generic object.
	 * @throws IllegalArgumentException if the given module id doesn't match the generic object's module id.
	 * @postcondition result != null
	 * @postcondition result.isComplete()
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO getWithDependants(Integer iGenericObjectId, Set<String> stRequiredSubEntityNames)
			throws CommonPermissionException, CommonFinderException {

		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(this.get(iGenericObjectId), new DependantMasterDataMap());

		final Set<String> stSubEntityNames;
		if (stRequiredSubEntityNames == null) {
			final GenericObjectMetaDataCache lometadataprovider = GenericObjectMetaDataCache.getInstance();
			stSubEntityNames = lometadataprovider.getSubFormEntityNamesByLayoutId(lometadataprovider.getBestMatchingLayoutId(result.getUsageCriteria(lometadataprovider), false));
		}
		else {
			stSubEntityNames = stRequiredSubEntityNames;
		}
		_fillDependants(result, stSubEntityNames);

		assert result != null;
		assert result.isComplete();
		return result;
	}

	/**
	 * @param iGenericObjectId
	 * @return the version of the given genericobject id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@Override
	@RolesAllowed("Login")
	public Integer getVersion(int iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		return this.get(iGenericObjectId).getVersion();
	}

	/**
	 * @param gowdvo
	 * @param mpDependants
	 * @param bAll
	 * @return reload the dependant data of the genericobject, if bAll is false, only the direct
	 *         dependants (highest hierarchie of subforms) will be reloaded
	 * @throws CommonFinderException if no such object was found.
	 */
	@Override
	@RolesAllowed("Login")
	public DependantMasterDataMap reloadDependants(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bAll) throws CommonFinderException {
		LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

		final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			Modules.getInstance().getEntityNameByModuleId(govo.getModuleId()),govo.getId(),false);

		if (mpDependants == null) {
			mpDependants = new DependantMasterDataMap();
		}

		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
			// care only about dependant data which are on the highest level
			if (collSubEntities.get(eafn) == null) {
				final String sForeignKeyFieldName = LangUtils.defaultIfNull(eafn.getFieldName(), ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
				Collection<MasterDataVO> collmdVO = getMasterDataFacade().getDependantMasterData(eafn.getEntityName(), sForeignKeyFieldName, govo.getId());

				mpDependants.setData(eafn.getEntityName(), CollectionUtils.transform(collmdVO, new MasterDataToEntityObjectTransformer()));

				if (bAll) {
					for (MasterDataVO mdVO : collmdVO) {
						// now read all dependant data of the child subforms
						getMasterDataFacade().readAllDependants(eafn.getEntityName(), mdVO.getIntId(), mdVO.getDependants(), mdVO.isRemoved(), eafn.getEntityName(), collSubEntities);
					}
				}
			}
		}
		return mpDependants;
	}

	/**
	 * gets generic object vo for a given generic object id, along with its dependants.
	 * @param iGenericObjectId id of generic object
	 * @return generic object vo with dependants
	 * @throws CommonFinderException if no such object was found.
	 * deprecated merge with getWithDependants?
	 * @nucleus.permission mayRead(module)
	 */
	@Override
	@RolesAllowed("Login")
	public RuleObjectContainerCVO getRuleObjectContainerCVO(Event event, Integer iGenericObjectId)
			throws CommonPermissionException, CommonFinderException {
		// @todo merge with getWithDependants?

		final GenericObjectVO govo = this.get(iGenericObjectId);

		return new RuleObjectContainerCVO(event, govo, reloadDependants(govo, null, true));
	}

	/**
	 * gets generic object with dependants vo for a given generic object id (historical, readonly view)
	 * @param iGenericObjectId id of generic object to show historical information for
	 * @param dateHistorical date to show historical information for
	 * @return generic object with dependants vo at the given point in time
	 * @throws CommonFinderException if the given object didn't exist at the given point in time.
	 * @precondition dateHistorical != null
	 * @postcondition result != null
	 * @nucleus.permission mayRead(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO getHistorical(int iGenericObjectId, Date dateHistorical) throws CommonFinderException, CommonPermissionException {
		debug("Entering getHistorical(Integer iGenericObjectId, Date dateHistorical)");

		if (dateHistorical == null) {
			throw new NullArgumentException("dateHistorical");
		}
		final RuleObjectContainerCVO loccvo = this.getRuleObjectContainerCVO(Event.UNDEFINED, iGenericObjectId);
		final GenericObjectVO govoResult = loccvo.getGenericObject();
		final DependantMasterDataMap mpDependantsResult = loccvo.getDependants();

		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(dateHistorical);

		// If dateHistorical has no time components, we can assume that the user wants to see the state of the object at the end of that day.
		if (calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) == 0) {
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 0);
		}
		dateHistorical = calendar.getTime();

		// Check if the object existed at the historical date:
		final Date dateCreatedAt = govoResult.getCreatedAt();
		if (dateHistorical.before(dateCreatedAt)) {
			throw new CommonFinderException("genericobject.facade.exception.1");//"Das Objekt existierte zum angegebenen Zeitpunkt nicht.");
		}

		debug("Historical Date we want to see  : " + dateHistorical.toString());
		debug("Date of creation of record      : " + dateCreatedAt.toString());

		govoResult.setAttributes(GenericObjectFacadeHelper.getHistoricalAttributes(govoResult, dateHistorical, mpDependantsResult).values());

		this.debug("Leaving getHistorical(Integer iGenericObjectId, Date dateHistorical)");

		assert govoResult != null;

		return new GenericObjectWithDependantsVO(govoResult, mpDependantsResult);
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @return list of generic object value objects - without dependants and parent objects!
	 * @todo restrict permission - check module id!
	 */
	@Override
	@RolesAllowed("Login")
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds) {

		return this.getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, Collections.<String>emptySet());
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @return list of generic object value objects with specified dependants and without parent objects!
	 * @postcondition result != null
	 */
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames) {
		return helper.getGenericObjects(iModuleId, clctexpr, stRequiredSubEntityNames, this.getCurrentUserName());
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @return list of generic object value objects with specified dependants and without parent objects!
	 * @postcondition result != null
	 */
	@Override
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects) {
		return getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames);
	}

	/**
	 * gets all generic objects along with its dependants, that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr value object containing search expression
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects
	 * @param bIncludeSubModules Include submodules in search?
	 * @return list of generic object value objects
	 * @precondition stRequiredSubEntityNames != null
	 * @todo rename to getGenericObjectProxyList?
	 */
	@Override
	@RolesAllowed("Login")
	public ProxyList<GenericObjectWithDependantsVO> getGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules) {
		return new GenericObjectProxyList(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects);
	}

	/**
	 * gets all generic objects along with its dependants, that match a given search condition, but
	 * clears the values of all attributes on which the current user has no read permission
	 * NOTE: use only within report mechanism
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr value object containing search expression
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects
	 * @param bIncludeSubModules Include submodules in search?
	 * @return list of generic object value objects
	 * @precondition stRequiredSubEntityNames != null
	 * @todo rename to getGenericObjectProxyList?
	 */
	@Override
	@RolesAllowed("Login")
	public ProxyList<GenericObjectWithDependantsVO> getPrintableGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules) {

		stRequiredAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		stRequiredAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());

		ProxyList<GenericObjectWithDependantsVO> proxyList = this.getGenericObjectsWithDependants(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects, bIncludeSubModules);

		// remove all attribute and subform values on which the current user has no read permission
		for (GenericObjectWithDependantsVO gowdvo : proxyList) {
			Integer iStatus = gowdvo.getAttribute(NuclosEOField.STATE.getMetaData().getField(), AttributeCache.getInstance()).getValueId();

			if (iStatus != null) {
				for (DynamicAttributeVO dynamicAttributeVO : gowdvo.getAttributes()) {
					String sAtributeName = AttributeCache.getInstance().getAttribute(dynamicAttributeVO.getAttributeId()).getName();

					if (SecurityCache.getInstance().getAttributePermission(Modules.getInstance().getEntityNameByModuleId(gowdvo.getModuleId()), sAtributeName, iStatus) == null) {
						dynamicAttributeVO.setValue(null);
					}
				}

				DependantMasterDataMap dependants = gowdvo.getDependants();
				for (String entity : dependants.getEntityNames()) {
					Map<Integer, Permission> permission = SecurityCache.getInstance().getSubForm(getCurrentUserName(), entity);

					if (permission.get(iStatus) == null) {
						for (EntityObjectVO mdvo : dependants.getData(entity)) {
							mdvo.getFields().clear();
							mdvo.getFieldIds().clear();
						}
					}
				}
			}
		}

		return proxyList;
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param iMaxRowCount the maximum number of rows
	 * @return list of generic object value objects
	 * @precondition stRequiredSubEntityNames != null
	 * @precondition iMaxRowCount > 0
	 * @postcondition result.size() <= iMaxRowCount
	 */
	@Override
	@RolesAllowed("Login")
	public TruncatableCollection<GenericObjectWithDependantsVO> getRestrictedNumberOfGenericObjects(Integer iModuleId,
			CollectableSearchExpression clctexpr, Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames,
			int iMaxRowCount) {

		if (iMaxRowCount <= 0) {
			throw new IllegalArgumentException("iMaxRowCount == " + iMaxRowCount);
		}

		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId));
		final List<GenericObjectWithDependantsVO> lstResult = new ArrayList<GenericObjectWithDependantsVO>(Math.min(iMaxRowCount + 1, 1000));

		for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpression(appendRecordGrants(clctexpr, eMeta), iMaxRowCount + 1, true)) {
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				_fillDependants(go, stRequiredSubEntityNames);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			lstResult.add(go);
		}

//		final GenericObjectSearchExpressionUnparser unparser = helper.getUnparser(this.getCurrentUserName());
//
//		final String sSql = unparser.unparseExpression(iModuleId, clctexpr, iMaxRowCount + 1);
//
//		final List<GenericObjectWithDependantsVO> lstResult = new ArrayList<GenericObjectWithDependantsVO>(Math.min(iMaxRowCount + 1, 1000));
//		CollectionUtils.addAll(lstResult, helper.getGenericObjectsIteratorBySQL(iModuleId, this.getCurrentUserName(), sSql, helper.DEFAULT_PAGESIZE, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects));

		assert lstResult.size() <= iMaxRowCount + 1;

		final boolean bTruncated = (lstResult.size() == iMaxRowCount + 1);
		if (bTruncated) {
			// remove the last entry:
			lstResult.remove(iMaxRowCount);
			assert lstResult.size() == iMaxRowCount;
		}

		final int iTotalRowCount = bTruncated ? NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).count(clctexpr) : lstResult.size();

		final TruncatableCollection<GenericObjectWithDependantsVO> result =
				new TruncatableCollectionDecorator<GenericObjectWithDependantsVO>(lstResult, bTruncated, iTotalRowCount);
		assert result.size() <= iMaxRowCount;
		return result;
	}

	/**
	 * gets the ids of all leased objects that match a given search expression (ordered, when necessary)
	 * @param iModuleId id of module to search for leased objects in
	 * @param cond condition that the leased objects to be found must satisfy
	 * @return List<Integer> list of leased object ids
	 */
	@Override
	public List<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchExpression cse) {
		EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId));
		List<Long> ids = NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getIdsBySearchExprUserGroups(
			appendRecordGrants(cse, eMeta.getEntity()),	LangUtils.convertId(iModuleId), getCurrentUserName());
		return DalUtils.convertLongIdList(ids);
	}

	/**
	 * gets the ids of all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param cond condition that the generic objects to be found must satisfy
	 * @return List<Integer> list of generic object ids
	 */
	@Override
	public List<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchCondition cond) {
		return this.getGenericObjectIds(iModuleId, new CollectableSearchExpression(cond));
	}

	/**
	 * gets more leased objects that match a given search condition
	 * @param iModuleId					id of module to search for leased objects in
	 * @param clctexpr value object containing search condition
	 * @param lstSortOrderValues field values for building correct where clause
	 * @param iRecordCount					 number of records to get for search condition
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects

	 * @return list of leased object value objects
	 * @precondition stRequiredSubEntityNames != null
	 */
	@Override
	public Collection<GenericObjectWithDependantsVO> getGenericObjectsMore(Integer iModuleId, List<Integer> lstIds,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects) {

		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId));

		final List<EntityObjectVO> eos = NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpressionAndPrimaryKeys(appendRecordGrants(new CollectableSearchExpression(), eMeta), DalUtils.convertIntegerIdList(lstIds));
		List<GenericObjectWithDependantsVO> result = new ArrayList<GenericObjectWithDependantsVO>(eos.size());

		for (EntityObjectVO eo : eos) {
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				_fillDependants(go, stRequiredSubEntityNames);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			result.add(go);
		}

		return result;
	}


	/**
	 * creates a new generic object, along with its dependants.
	 * @param gowdvo containing the generic object data
	 * @param stRequiredSubEntityNames Set<String>
	 * @return the new generic object, containing the dependants for the specified sub entities.
	 *
	 * @precondition gowdvo.getId() == null
	 * @precondition Modules.getInstance().isSubModule(iModuleId.intValue()) --> gowdvo.getParentId() != null
	 * @precondition (gowdvo.getDependants() != null) -> gowdvo.getDependants().dependantsAreNew()
	 * @precondition stRequiredSubEntityNames != null
	 *
	 * @nucleus.permission mayWrite(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO create(GenericObjectWithDependantsVO gowdvo, Set<String> stRequiredSubEntityNames)
			throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException, CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new IllegalArgumentException("stRequiredSubEntityNames");
		}
		final GenericObjectVO govoCreated = this.create(gowdvo);

		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(govoCreated, new DependantMasterDataMap());
		_fillDependants(result, stRequiredSubEntityNames);

		return result;
	}

	/**
	 * creates a new generic object, along with its dependants.
	 * @param gowdvo the generic object, along with its dependants.
	 * @return the created object witout dependants.
	 * @throws CommonPermissionException if the current user doesn't have the right to create such an object.
	 * @throws NuclosBusinessRuleException if the object could not be created because a business rule failed.
	 * @throws CommonCreateException if the object could not be created for other reasons, eg. because of a database constraint violation.
	 *
	 * @precondition gowdvo.getId() == null
	 * @precondition Modules.getInstance().isSubModule(gowdvo.getModuleId()) --> gowdvo.getParentId() != null
	 * @precondition (gowdvo.getDependants() != null) --> gowdvo.getDependants().areAllDependantsNew()
	 *
	 * @nucleus.permission mayWrite(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectVO create(GenericObjectWithDependantsVO gowdvo)
			throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException {

		debug("Entering create(GenericObjectWithDependantsVO)");

		if (gowdvo.getId() != null) {
			throw new IllegalArgumentException("govo.getId()");
		}

		final int iModuleId = gowdvo.getModuleId();

		// Is the object to create a main module (as opposed to submodule) object?
		final boolean bMainModuleObject = Modules.getInstance().isMainModule(gowdvo.getModuleId());

//		if (!bMainModuleObject && gowdvo.getParentId() == null) {
//			throw new NullArgumentException("govo.getParentId");
//		}

		DependantMasterDataMap mpDependants = gowdvo.getDependants();

		final boolean useRuleEngine = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId)).getEntity(), false);
		if(useRuleEngine){
			/** @todo check if loccvoResult can safely be ignored */
			final RuleObjectContainerCVO loccvoResult = fireSaveEvent(Event.CREATE_BEFORE, gowdvo, mpDependants, false);
			mpDependants = loccvoResult.getDependants();
		}


		EntityObjectVO dalVO = DalSupportForGO.wrapGenericObjectVO(gowdvo);
		DalUtils.updateVersionInformation(dalVO, getCurrentUserName());
		dalVO.setId(DalUtils.getNextId());
		dalVO.flagNew();

		// generate system identifier:
		final String sCanonicalValueSystemIdentifier = BusinessIDFactory.generateSystemIdentifier(iModuleId);
		assert !StringUtils.isNullOrEmpty(sCanonicalValueSystemIdentifier);
		dalVO.getFields().put(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(), sCanonicalValueSystemIdentifier);

		DalCallResult dcr = NucletDalProvider.getInstance().getEntityObjectProcessor(dalVO.getEntity()).insertOrUpdate(dalVO);
		try {
			dcr.throwFirstBusinessExceptionIfAny();

			Date sysdate = new Date();
			/**
			 * new "old" generic object record
			 */
			EOGenericObjectVO eogo = new EOGenericObjectVO();
			eogo.setId(dalVO.getId());
			eogo.setCreatedBy(getCurrentUserName());
			eogo.setCreatedAt(InternalTimestamp.toInternalTimestamp(sysdate));
			eogo.setVersion(1);
			eogo.setChangedBy(getCurrentUserName());
			eogo.setChangedAt(InternalTimestamp.toInternalTimestamp(sysdate));
			eogo.setModuleId(LangUtils.convertId(iModuleId));
			eogo.flagNew();
			NucletDalProvider.getInstance().getEOGenericObjectProcessor().insertOrUpdate(eogo);

			helper.trackChangesToLogbook(null, dalVO);
		}
		catch(Exception e1) {
			throw new CommonCreateException(e1);
		}

		final Integer id = LangUtils.convertId(dalVO.getId());
		final GenericObjectVO goVO = DalSupportForGO.getGenericObjectVO(dalVO);

		// get default object group assigned to current user
		Integer groupId = null;

		for (final MasterDataVO mdvo : getMasterDataFacade().getMasterData(NuclosEntity.USER.getEntityName(), null, true)) {
			if (this.getCurrentUserName().equals(mdvo.getField("name"))) {
				groupId = (Integer)mdvo.getField("groupId");
			}
		}

		if (groupId != null) {
			try {
				this.addToGroup(id, groupId, false);
			}
			catch (CommonFinderException e) {
				throw new CommonFatalException(e);
			}
			catch (CommonPermissionException e) {
				checkWriteAllowedForObjectGroup(gowdvo.getModuleId(), null);
			}
		}
		else {
			checkWriteAllowedForObjectGroup(gowdvo.getModuleId(), null);
		}



		// create dependant records from subforms:
		if (mpDependants != null) {
			helper.createDependants(Modules.getInstance().getEntityNameByModuleId(iModuleId), id, mpDependants);
		}

		if (bMainModuleObject) {
			// Write a possible origin object into the logbook:
			final DynamicAttributeVO attrvoOrigin = gowdvo.getAttribute(NuclosEOField.ORIGIN.getMetaData().getId().intValue());
			if (attrvoOrigin != null) {
				createLogbookEntry(id, attrvoOrigin.getAttributeId(), null, null, null, null, null,
					null, null, null, null, (String) attrvoOrigin.getValue());
			}
		}

		GenericObjectVO result;

		if (Modules.getInstance().getUsesStateModel(iModuleId)) {
			// create first state entry of generic object:
			try {
				this.enterInitialState(goVO);



				// NUCLEUSINT-928: result was helper.getValueObject(goVO), see else clause.
				// But that object represents the object before the state change (version 1)
				// while the real object in the database is version 2.  Beside the version,
				// some other attributes may have been changed by state transition, too.
				// TODO: As workaround (because enterInitialState()/StateFacadeBean does not
				// return the changed object), we get a new one via get().
				result = get(id);
			}
			catch (NuclosNoAdequateStatemodelException ex) {
				// This is considered fatal:
				throw new CommonFatalException(ex.getMessage(), ex);
			}
			catch (CommonFinderException ex) {
				// This is considered fatal:
				throw new CommonFatalException(ex);
			}
			catch (NuclosBusinessException ex) {
				// This is considered fatal:
				throw new CommonFatalException(ex.getMessage(), ex);
			}
		} else {
			result = DalSupportForGO.getGenericObject(goVO.getId(), goVO.getModuleId());
		}

		if(useRuleEngine){
			fireSaveEvent(Event.CREATE_AFTER, result, mpDependants, true);
			try {
				result = get(id);
			} catch (CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}
		}

		debug("Leaving create(GenericObjectWithDependantsVO)");

		return result;
	}

	/**
	 * updates an existing generic object in the database and returns the written object along with its dependants.
	 * @param iModuleId module id for plausibility check
	 * @param lowdcvo containing the generic object data
	 * @return same generic object as value object
	 * @precondition iModuleId != null
	 * @precondition lowdcvo.getModuleId() == iModuleId
	 * @nucleus.permission mayWrite(module)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO modify(Integer iModuleId, GenericObjectWithDependantsVO lowdcvo)
			throws CommonCreateException, CommonFinderException, CommonRemoveException,
			CommonPermissionException, CommonStaleVersionException, NuclosBusinessException,
			CommonValidationException {

		if (iModuleId == null) {
			throw new NullArgumentException("iModuleId");
		}
		if (lowdcvo.getModuleId() != iModuleId) {
			throw new IllegalArgumentException("iModuleId");
		}

		final GenericObjectVO govoUpdated = this.modify(lowdcvo, lowdcvo.getDependants(),
			this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId)).getEntity(), false));


		final GenericObjectMetaDataCache lometadataprovider = GenericObjectMetaDataCache.getInstance();
		final AttributeProvider attrprovider = AttributeCache.getInstance();
		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(govoUpdated, new DependantMasterDataMap());
		final Set<String> collSubEntityNames = lometadataprovider.getSubFormEntityNamesByLayoutId(lometadataprovider.getBestMatchingLayoutId(govoUpdated.getUsageCriteria(attrprovider), false));
		_fillDependants(result, collSubEntityNames);

		return result;
	}

	/**
	 * modifies an existing generic object.
	 * @param govo containing the generic object data
	 * @param mpDependants Map of dependant masterdata objects. May be <code>null</code>. The dependant objects must
	 * have a field "genericObjectId" filled with their parent's id.
	 * @param bFireSaveEvent
	 * @return same generic object as value object
	 * @nucleus.permission mayWrite(module)
	 * @todo change signature into GenericObjectVO modify(GenericObjectWithDependantsVO lowdcvo, boolean bFireSaveEvent)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException{
		return modify(govo, mpDependants, bFireSaveEvent, true);
	}

	/**
	 * modifies an existing generic object.
	 * @param govo containing the generic object data
	 * @param mpDependants Map of dependant masterdata objects. May be <code>null</code>. The dependant objects must
	 * have a field "genericObjectId" filled with their parent's id.
	 * @param bFireSaveEvent
	 * @param bCheckPermission
	 * @return same generic object as value object
	 * @nucleus.permission mayWrite(module)
	 * @todo change signature into GenericObjectVO modify(GenericObjectWithDependantsVO lowdcvo, boolean bFireSaveEvent)
	 */
	@Override
	@RolesAllowed("Login")
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent, boolean bCheckPermission)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException {

		// @todo make the handling of mpDependants clean!
		this.debug("Entering modify(GenericObjectVO govo, DependantMasterDataMap mpDependants)");

		final GenericObjectVO dbGoVO_new;
		final Integer newId;

		this.debug("Modifying (Start find)");
		EntityObjectVO dbEoVO = DalSupportForGO.getEntityObject(govo.getId(), govo.getModuleId());
		GenericObjectVO dbGoVO = DalSupportForGO.getGenericObjectVO(dbEoVO);

		if (bCheckPermission){
			this.checkWriteAllowedForModule(govo.getModuleId(), govo.getId());
			RecordGrantUtils.checkWriteInternal(dbEoVO.getEntity(), dbEoVO.getId());
		}

		this.checkForStaleVersion(dbGoVO, govo);

		if (bFireSaveEvent) {
			this.debug("Modifying (Start rules)");
			final RuleObjectContainerCVO loccvoResult = this.fireSaveEvent(Event.MODIFY_BEFORE, govo, mpDependants, false);
			govo = loccvoResult.getGenericObject();
			DependantMasterDataMap mpFromRuleContainer = loccvoResult.getDependants();
			mergeDependants(mpDependants, mpFromRuleContainer);
			mpDependants = mpFromRuleContainer;
		}

		this.debug("Modifying (Start change genericobject)");

		EntityObjectVO eoUpdated = DalSupportForGO.wrapGenericObjectVO(govo);

		StateFacadeLocal statefacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		statefacade.checkMandatory(eoUpdated);

		DalUtils.updateVersionInformation(eoUpdated, getCurrentUserName());
		eoUpdated.flagUpdate();
		DalCallResult dcr = NucletDalProvider.getInstance().getEntityObjectProcessor(eoUpdated.getEntity()).insertOrUpdate(eoUpdated);
		dcr.throwFirstBusinessExceptionIfAny();

		helper.trackChangesToLogbook(dbEoVO, eoUpdated);


		if (mpDependants != null) {
			this.debug("Modifying (Start change dependants)");

			String entityNameByModuleId = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());

			mpDependants.setParent(entityNameByModuleId, govo.getId());

			// First protocol all the modified records
			final Set<Integer> stExcluded = new HashSet<Integer>();
			getMasterDataFacade().protocolDependantChanges(govo.getId(), mpDependants, stExcluded, false);

			LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityNameByModuleId,govo.getId(),false);

			for (String sEntityName : mpDependants.getEntityNames()) {
				for (EntityObjectVO mdVO : mpDependants.getData(sEntityName)) {
					getMasterDataFacade().readAllDependants(sEntityName, LangUtils.convertId(mdVO.getId()), mdVO.getDependants(), mdVO.isFlagRemoved(), sEntityName, collSubEntities);
				}
			}

			getMasterDataFacade().modifyDependants(entityNameByModuleId,govo.getId(),govo.isRemoved(),mpDependants);

			// .. and then all the newly created ones, excluding the already written (i.e. the modified).
			getMasterDataFacade().protocolDependantChanges(govo.getId(), mpDependants, stExcluded, true);
		}

		this.debug("Modifying (Start read)");
		GenericObjectVO result = get(dbGoVO.getId());

		if (bFireSaveEvent) {
			this.debug("Modifying (Start rules after save)");
			this.fireSaveEvent(Event.MODIFY_AFTER, result, mpDependants, true);
			result = get(dbGoVO.getId());
		}

		this.debug("Leaving modify(GenericObjectVO govo, DependantMasterDataMap mpDependants)");
		return result;
	}

	private void mergeDependants(DependantMasterDataMap mpAll, DependantMasterDataMap mpFromRuleContainer) {
		for(String subEntity : mpAll.getEntityNames()) {
			for(EntityObjectVO vo : mpAll.getData(subEntity)) {
				if(vo.isFlagRemoved()){
					mpFromRuleContainer.addData(subEntity, vo);
				}
			}
		}
	}

	/**
	 * fires a Save event, executing the corresponding business rules.
	 * @param govo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 * @precondition Modules.getInstance().getUsesRuleEngine(govo.getModuleId().intValue())
	 */
	private RuleObjectContainerCVO fireSaveEvent(Event event, GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean after) throws NuclosBusinessRuleException {
		if (!Modules.getInstance().getUsesRuleEngine(govo.getModuleId())) {
			throw new IllegalArgumentException("govo.getModuleId()");
		}
		String sEntity = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());

		RuleEngineFacadeLocal facade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
		RuleObjectContainerCVO ruleContainer = facade.fireRule(sEntity, after ? RuleEventUsageVO.SAVE_AFTER_EVENT : RuleEventUsageVO.SAVE_EVENT, new RuleObjectContainerCVO(event, govo, mpDependants != null ? mpDependants : new DependantMasterDataMap()));
		return ruleContainer;
	}

	/**
	 * fires a Delete event, executing the corresponding business rules.
	 * @param govo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 * @precondition Modules.getInstance().getUsesRuleEngine(govo.getModuleId())
	 */
	private void fireDeleteEvent(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean after) throws NuclosBusinessRuleException {
		if (!Modules.getInstance().getUsesRuleEngine(govo.getModuleId())) {
			throw new IllegalArgumentException("govo.getModuleId()");
		}
		String sEntityName = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());

		RuleEngineFacadeLocal facade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
		facade.fireRule(sEntityName, after ? RuleEventUsageVO.DELETE_AFTER_EVENT : RuleEventUsageVO.DELETE_EVENT, new RuleObjectContainerCVO(after?Event.DELETE_AFTER:Event.DELETE_BEFORE, govo, mpDependants != null ? mpDependants : new DependantMasterDataMap()));
	}

	/**
	 * delete generic object from database
	 * @param gowdvo containing the generic object data
	 * @param bDeletePhysically remove from db?
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @nucleus.permission mayDelete(module, bDeletePhysically)
	 */
	@Override
	@RolesAllowed("Login")
	public void remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically) throws NuclosBusinessException, CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException, CommonCreateException {
		this.debug("Entering remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically)");

		final int iModuleId;

		LayoutFacadeLocal layoutFacade =  ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

//		try {
//			final GenericObjectVO dbGoVO = MasterDataWrapper.getGenericObjectVO(getMasterDataFacade().get(NuclosEntity.GENERICOBJECT.getEntityName(), gowdvo.getId()));
			final GenericObjectVO dbGoVO = DalSupportForGO.getGenericObject(gowdvo.getId(), gowdvo.getModuleId());

			iModuleId = dbGoVO.getModuleId();
			String entity = MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId)).getEntity();

			this.checkDeleteAllowedForModule(iModuleId, dbGoVO.getId(), bDeletePhysically);
			RecordGrantUtils.checkDeleteInternal(entity, LangUtils.convertId(dbGoVO.getId()));

//			 prevent removal if dependant dynamic attributes exist:
			final Object oExternalId = gowdvo.getId();
			if (oExternalId == null) {
				throw new NuclosFatalException("genericobject.facade.exception.2");//"Der Datensatz hat eine leere Id.");
			}
//			if (oExternalId instanceof Integer) {
//				checkDependantAttributeValues((Integer) oExternalId);
//			}

			this.checkForStaleVersion(dbGoVO, gowdvo);

			// Is the object to delete a main module (as opposed to submodule) object?
//			final boolean bMainModuleObject = Modules.getInstance().isMainModule(gowdvo.getModuleId());
//
//			if (!bMainModuleObject && gowdvo.getParentId() == null) {
//				throw new NullArgumentException("gowdvo.getParentId");
//			}

			// load and set all attributes of given gowdvo, to have access on them within rules
			gowdvo.addAndSetAttribute(dbGoVO.getAttributes());

			DependantMasterDataMap mpDependants = gowdvo.getDependants();

			if (!bDeletePhysically) // NUCLOSINT-890
				gowdvo.setDeleted(true); // Only set deleted if logical deleted. This could be be useful in rules.

			final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				Modules.getInstance().getEntityNameByModuleId(dbGoVO.getModuleId()),dbGoVO.getId(),false);

			final boolean useRuleEngine = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(iModuleId)).getEntity(), false);
			if(useRuleEngine)
				this.fireDeleteEvent(gowdvo, gowdvo.getDependants(), false);

			DalCallResult dalResult;
			if (bDeletePhysically) {
				for (EntityAndFieldName eafn : collSubEntities.keySet()) {
					// care only about subforms which are on the highest level
					if (collSubEntities.get(eafn) == null) {
						// load dependant data, if not already done
						if (mpDependants.getData(eafn.getEntityName()).isEmpty()) {
							Set<String> stSubForm = new HashSet<String>();
							stSubForm.add(eafn.getEntityName());
							_fillDependants(gowdvo, stSubForm);
						}

						// mark all dependant data as removed
						for (EntityObjectVO mdVO : gowdvo.getDependants().getData(eafn.getEntityName())) {
							mdVO.flagRemove();
							getMasterDataFacade().readAllDependants(eafn.getEntityName(), LangUtils.convertId(mdVO.getId()), mdVO.getDependants(), mdVO.isFlagRemoved(), eafn.getEntityName(), collSubEntities);
						}
					}
				}

				// remove dependant data
				new MasterDataFacadeHelper().removeDependants(gowdvo.getDependants());
				removeDependants(dbGoVO);
				helper.removeLogBookEntries(dbGoVO.getId());
				helper.removeDependantTaskObjects(dbGoVO.getId());
				helper.removeGroupBelonging(dbGoVO.getId());
//				getMasterDataFacade().remove(NuclosEntity.GENERICOBJECT.getEntityName(), MasterDataWrapper.wrapGenericObjectVO(dbGoVO), false);
				dalResult = NucletDalProvider.getInstance().getEOGenericObjectProcessor().delete(LangUtils.convertId(dbGoVO.getId()));
				dalResult.throwFirstBusinessExceptionIfAny();
				dalResult = DalSupportForGO.getEntityObjectProcessor(dbGoVO.getModuleId()).delete(LangUtils.convertId(dbGoVO.getId()));
				dalResult.throwFirstBusinessExceptionIfAny();
			}
			else {
//				dbGoVO.setDeleted(true);
//				getMasterDataFacade().modify(NuclosEntity.GENERICOBJECT.getEntityName(), MasterDataWrapper.wrapGenericObjectVO(dbGoVO), null);

				EntityObjectVO eoToUpdate = DalSupportForGO.wrapGenericObjectVO(dbGoVO);

				helper.createLogBookEntryIfNecessary(eoToUpdate, null, new DynamicAttributeVO(LangUtils.convertId(NuclosEOField.LOGGICALDELETED.getMetaData().getId()), null, Boolean.TRUE));
				eoToUpdate.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), Boolean.TRUE);
				DalUtils.updateVersionInformation(eoToUpdate, getCurrentUserName());
				eoToUpdate.flagUpdate();

				dalResult = DalSupportForGO.getEntityObjectProcessor(dbGoVO.getModuleId()).insertOrUpdate(eoToUpdate);
				dalResult.throwFirstBusinessExceptionIfAny();
			}
//		}
//		catch (CommonValidationException ex) {
//			throw new CommonFatalException(ex);
//		}

		if (Modules.getInstance().isMainModule(iModuleId)) {
			if (isInfoEnabled()) {
				info("The entry " + gowdvo.getSystemIdentifier() + " (Id: " + gowdvo.getId() + ") has been deleted.");
			}
		}

		if(useRuleEngine)
			this.fireDeleteEvent(gowdvo, gowdvo.getDependants(), true);

		this.debug("Leaving remove(GenericObjectVO govo, boolean bDeletePhysically)");
	}

	// replaces the ejbRemove() from GenericObjectBean
	private void removeDependants(GenericObjectVO govo) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonRemoveException {
		final int iModuleId = govo.getModuleId();

		final Modules modules = Modules.getInstance();
		if (modules.isMainModule(iModuleId)) {
			// delete all dependant leased object relations:
			for (GenericObjectRelationVO vo : findRelationsByGenericObjectId(govo.getId())) {
				getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(),MasterDataWrapper.wrapGenericObjectRelationVO(vo),false);
			}
		}

		if (modules.getUsesStateModel(iModuleId)) {
			// delete all dependant state history entries:
			StateFacadeLocal stateFacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
			for (StateHistoryVO history : stateFacade.findStateHistoryByGenericObjectId(govo.getId()))
			{
				getMasterDataFacade().remove(NuclosEntity.STATEHISTORY.getEntityName(), MasterDataWrapper.wrapStateHistoryVO(history), false);
			}
		}

		if (modules.isLogbookTracking(iModuleId)) {
			DataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_UD_LOGBOOK", "INTID_T_UD_GENERICOBJECT", govo.getId()));
		}
	}

	/**
	 * restore GO marked as deleted
	 * @param gowdvo containing the generic object data
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 */
	@Override
	public void restore(Integer iId)throws CommonFinderException, CommonPermissionException, CommonBusinessException {
		this.debug("Entering restore(GenericObjectWithDependantsVO gowdvo)");

		GenericObjectWithDependantsVO goVO = this.getWithDependants(iId, null);
		goVO.setDeleted(false);
		this.modify(getModuleContainingGenericObject(iId), goVO); // NUCLOSINT-890

		this.debug("Leaving retstore(GenericObjectVO govo)");
	}

	/**
	 * Checks if dependant attribute values exist that refer to the given external id.
	 * @param iExternalId
	 * @throws CommonRemoveException if dependant attribute values exist.
	 */
//	static void checkDependantAttributeValues(Integer iExternalId) throws CommonRemoveException {
//		final String sSql = "select intid_t_md_attribute from T_UD_GO_ATTRIBUTE a " +
//			"join T_UD_GENERICOBJECT g on (g.intid = a.intid_t_ud_genericobject) " +
//			"where a.intid_external = " + iExternalId + " and g.intid = a.intid_t_ud_genericobject and g.blndeleted <> 1";
//
//		final Integer iAttributeId = NuclosSQLUtils.runSelect(NuclosDataSources.getDefaultDS(), sSql, new GetSingleIntegerOrDefault(null));
//		if (iAttributeId != null) {
//			final AttributeCVO attrcvo = AttributeCache.getInstance().getAttribute(iAttributeId);
//			throw new CommonRemoveException(StringUtils.getParameterizedExceptionMessage("genericobject.error.validation.attributes",
//				attrcvo.getName(), attrcvo.getResourceSIdForLabel()));
//		}
//	}

	/**
	 * causes the given generic object to enter its initial state.
	 * @param go
	 * @precondition Modules.getInstance().getUsesStateModel(go.getModuleId().intValue())
	 * @throws NuclosBusinessRuleException
	 * @throws NuclosNoAdequateStatemodelException
	 */
	private void enterInitialState(GenericObjectVO goVO) throws NuclosBusinessException,
			NuclosNoAdequateStatemodelException, CommonFinderException {

		if (!Modules.getInstance().getUsesStateModel(goVO.getModuleId())) {
			throw new IllegalArgumentException("goVO");
		}
		try {
			StateFacadeLocal statefacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
			final Integer iInitialStateId = statefacade.getInitialState(goVO.getId()).getId();
			statefacade.changeStateByRule(Integer.valueOf(goVO.getModuleId()), goVO.getId(), iInitialStateId);
		}
		catch (NuclosSubsequentStateNotLegalException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * method to get logbook entries for a generic object
	 *
	 * @param iGenericObjectId id of generic object to get logbook entries for
	 * @param iAttributeId		id of attribute to get logbook entries for
	 * @return collection of logbook entry value objects
	 * @precondition Modules.getInstance().isLogbookTracking(this.getModuleContainingGenericObject(iGenericObjectId))
	 * @nucleus.permission mayRead(module)
	 */
	@Override
	@RolesAllowed("Login")
	public Collection<LogbookVO> getLogbook(int iGenericObjectId, Integer iAttributeId)
			throws CommonFinderException, CommonPermissionException {

		final int iModuleId = this.getModuleContainingGenericObject(iGenericObjectId);

		if (!Modules.getInstance().isLogbookTracking(iModuleId)) {
			throw new CommonFatalException("genericobject.facade.exception.3");
//				"F\u00fcr diese Modulentit\u00e4t ist die Logbuchfunktion ausgeschaltet.\n"+
//					"Um die Statushistorie f\u00fcr diese Entit\u00e4t zu verwenden, aktivieren Sie bitte die Logbuchfunktion\n"+
//					"in der Administrationsmaske f\u00fcr Modulentit\u00e4ten.");
		}

		this.checkReadAllowedForModule(iModuleId, iGenericObjectId);

		final Collection<LogbookVO> result = new HashSet<LogbookVO>();

		MasterDataMetaVO metaVO = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.GENERICOBJECTLOGBOOK);
		CollectableSearchCondition cond = SearchConditionUtils.newMDReferenceComparison(metaVO,"genericObject", iGenericObjectId);

		if (iAttributeId != null) {
			cond = SearchConditionUtils.and(cond, SearchConditionUtils.newMDReferenceComparison(metaVO,"attribute", iAttributeId));
		}

      Collection<MasterDataVO> logbook = getMasterDataFacade().getMasterData(NuclosEntity.GENERICOBJECTLOGBOOK.getEntityName(), cond, true);
   	for (MasterDataVO mdVO : logbook) {
			final Integer iAttributeIdLogbook = mdVO.getField("attributeId", Integer.class);
			if (iAttributeIdLogbook == null) {
				addLogbookIfPossible(result, mdVO);
			}
			else if (!AttributeCache.getInstance().contains(iAttributeIdLogbook)) {
				// This may be the case when there is an old attribute entry in the logbook:
				addLogbookIfPossible(result, mdVO);
			}
			else {
				/** @todo consider attribute group permissions here ! */
				final AttributeCVO attrcvo = AttributeCache.getInstance().getAttribute(iAttributeIdLogbook);
				if (attrcvo.isLogbookTracking()/*&& (SecurityCache.getInstance().getAttributegroupsRO(getCurrentUserName()).contains(attrcvo.getAttributegroupId()))*/)
				{
					addLogbookIfPossible(result, mdVO);
				}
			}
		}

      return result;
	}

	private void addLogbookIfPossible(final Collection<LogbookVO> result, MasterDataVO mdVO) {
		try {
			result.add(MasterDataWrapper.getLogbookVO(mdVO));
		}
		catch(Exception e) {
			// logbook entry can't be added; skip value
		}
	}

	/**
	 * adds the generic object with the given id to the group with the given id.
	 * @param iGenericObjectId generic object id to be grouped.  Must be a main module object.
	 * @param iGroupId id of group to add generic object to
	 * @param blnCheckWriteAllowedForObject ; should only be set to false when creating a new generic object, otherwise always true
	 * @throws NuclosBusinessRuleException
	 * @nucleus.permission mayWrite(module)
	 */
	@Override
	@RolesAllowed("Login")
	public void addToGroup(int iGenericObjectId, int iGroupId, boolean blnCheckWriteAllowedForObject)
			throws CommonCreateException, CommonFinderException, CommonPermissionException, NuclosBusinessRuleException {

		final int iModuleId = this.getModuleContainingGenericObject(iGenericObjectId);

		if (!Modules.getInstance().isMainModule(iModuleId)) {
			throw new IllegalArgumentException("iModuleId");
		}

		this.checkWriteAllowedForObjectGroup(iModuleId, iGroupId);
		if (blnCheckWriteAllowedForObject) {
			this.checkWriteAllowedForModule(iModuleId, iGenericObjectId);
		}

		// XXX* this.gogrouphome.create(iGroupId, iGenericObjectId);
		GenericObjectGroupFacadeLocal goGroupFacade = ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
		goGroupFacade.addToGroup(iGenericObjectId, iGroupId);
	}

	/**
	 * removes the list of generic objects with the given id from the group with the given id.
	 * @param mpGOGroupRelation
	 * @throws CommonCreateException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	@RolesAllowed("Login")
	public void removeFromGroup(Map<Integer, Integer> mpGOGroupRelation)
		throws NuclosBusinessRuleException, CommonFinderException, CommonPermissionException,
			CommonRemoveException, CommonStaleVersionException, CommonCreateException
	{
		for(Integer iGenericObjectId : mpGOGroupRelation.keySet()) {
			this.removeFromGroup(iGenericObjectId, mpGOGroupRelation.get(iGenericObjectId));
		}
	}

	/**
	 * removes the generic object with the given id from the group with the given id.
	 * @param iGenericObjectId generic object id to be removed. Must be a main module object.
	 * @param iGroupId id of group to remove generic object from
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws NuclosBusinessRuleException
	 * @nucleus.permission mayWrite(module)
	 */
	@Override
	@RolesAllowed("Login")
	public void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
			CommonRemoveException, CommonStaleVersionException, CommonCreateException
	{

		final int iModuleId = this.getModuleContainingGenericObject(iGenericObjectId);

		if (!Modules.getInstance().isMainModule(iModuleId)) {
			throw new IllegalArgumentException("iModuleId");
		}

		checkWriteAllowedForObjectGroup(iModuleId, iGroupId);

		GenericObjectGroupFacadeLocal goGroupFacade = ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
		goGroupFacade.removeFromGroup(iGenericObjectId, iGroupId);
	}

	/**
	 * relates a generic object to another generic object.
	 * @param iModuleIdTarget module id of target generic object to be related
	 * @param iGenericObjectIdTarget id of target generic object to be related
	 * @param iGenericObjectIdSource id of source generic object to be related to
	 * @param relationType relation type
	 * @precondition isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)
	 * @precondition Modules.getInstance().isMainModule(iModuleIdTarget.intValue())
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	@RolesAllowed("Login")
	public void relate(Integer iModuleIdTarget, Integer iGenericObjectIdTarget,
			Integer iGenericObjectIdSource, String relationType)
			throws CommonFinderException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {

		this.relate(iModuleIdTarget, iGenericObjectIdTarget, iGenericObjectIdSource, relationType, null, null, null);
	}

	/**
	 * relates a generic object to another generic object.
	 * @param iModuleIdTarget module id of target generic object to be related (i.e. of request)
	 * @param iGenericObjectIdTarget id of target generic object to be related (i.e. request)
	 * @param iGenericObjectIdSource id of source generic object to be related to (i.e. demand)
	 * @param relationType relation type
	 * @param dateValidFrom Must be <code>null</code> for system defined relation types.
	 * @param dateValidUntil Must be <code>null</code> for system defined relation types.
	 * @param sDescription Must be <code>null</code> for system defined relation types.
	 * @precondition isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)
	 * @precondition Modules.getInstance().isMainModule(iModuleIdTarget.intValue())
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	@RolesAllowed("Login")
	public void relate(Integer iModuleIdTarget, Integer iGenericObjectIdTarget,
			Integer iGenericObjectIdSource, String relationType, Date dateValidFrom, Date dateValidUntil,
			String sDescription) throws CommonFinderException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}
		if (!Modules.getInstance().isMainModule(iModuleIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}

		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdSource);
		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdTarget);

		GenericObjectRelationVO relationVO = new GenericObjectRelationVO(
			new NuclosValueObject(),iGenericObjectIdSource, iGenericObjectIdTarget, relationType, dateValidFrom, dateValidUntil, sDescription);
		getMasterDataFacade().create(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), MasterDataWrapper.wrapGenericObjectRelationVO(relationVO), null);
	}

	/**
	 * removes the relation with the given id.
	 * @param mpGOTreeNodeRelation
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	@RolesAllowed("Login")
	public void removeRelation(Map<Integer, GenericObjectTreeNode> mpGOTreeNodeRelation) throws CommonBusinessException, CommonRemoveException, CommonFinderException {
		for(Integer iRelationId : mpGOTreeNodeRelation.keySet()) {
			this.removeRelation(iRelationId, mpGOTreeNodeRelation.get(iRelationId).getId(), mpGOTreeNodeRelation.get(iRelationId).getModuleId());
		}
	}

	/**
	 * removes the relation with the given id.
	 * Note that only the relation id is really needed, the other arguments are for security only.
	 * @param iRelationId the id of the relation
	 * @param iGenericObjectIdTarget the id of the target object
	 * @param iModuleIdTarget the module id of the target object
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @precondition isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)
	 * @precondition Modules.getInstance().isMainModule(iModuleIdTarget)
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	@RolesAllowed("Login")
	public void removeRelation(Integer iRelationId, Integer iGenericObjectIdTarget, Integer iModuleIdTarget)
			throws CommonRemoveException, CommonFinderException, CommonBusinessException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}
		if (!Modules.getInstance().isMainModule(iModuleIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}
		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdTarget);
		try {
			MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), iRelationId);
			GenericObjectRelationVO relationVO = MasterDataWrapper.getGenericObjectRelationVO(mdVO);
			if (!relationVO.getDestinationGOId().equals(iGenericObjectIdTarget)) {
				throw new IllegalArgumentException("iGenericObjectIdTarget and iRelationId don't match.");
			}
			this.checkWriteAllowedForModule(iModuleIdTarget, relationVO.getSourceGOId());
			getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), mdVO, false);
		}
		catch (CommonPermissionException ex) {
			throw new CommonBusinessException(ex);
		}
		catch (CommonStaleVersionException ex) {
			throw new CommonBusinessException(ex);
		}
	}

	/**
	 * unrelates a generic object from another generic object. All relations between these objects with the given
	 * relation type are removed.
	 * @param iModuleIdTarget module id of target generic object to be unrelated (i.e. request)
	 * @param iGenericObjectIdTarget id of target generic object to be unrelated (i.e. request)
	 * @param iGenericObjectIdSource id of source generic object to be unrelated (i.e. demand)
	 * @param relationType
	 * @precondition isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)
	 * @precondition Modules.getInstance().isMainModule(iModuleIdTarget.intValue())
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@Override
	public void unrelate(Integer iModuleIdTarget, Integer iGenericObjectIdTarget, Integer iGenericObjectIdSource, String relationType)
			throws CommonFinderException, NuclosBusinessRuleException, CommonPermissionException, CommonRemoveException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}
		if (!Modules.getInstance().isMainModule(iModuleIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}

		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdSource);
		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdTarget);

		try {
			for (GenericObjectRelationVO vo : findRelations(iGenericObjectIdSource, relationType, iGenericObjectIdTarget))
			{
				getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), MasterDataWrapper.wrapGenericObjectRelationVO(vo), false);
			}
		}
		catch (CommonStaleVersionException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * finds relations between two given generic objects.
	 * @param iGenericObjectIdSource
	 * @param relationType
	 * @param iGenericObjectIdTarget
	 * @return the relation.
	 * @throws FinderException if no such relation exists.
	 * @postcondition result != null
	 */
	@Override
	public Collection<GenericObjectRelationVO> findRelations(Integer iGenericObjectIdSource, String relationType, Integer iGenericObjectIdTarget) throws CommonFinderException, CommonPermissionException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_RELATION").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.column("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.column("INTID_T_UD_GO_1", Integer.class), iGenericObjectIdSource),
			builder.equal(t.column("INTID_T_UD_GO_2", Integer.class), iGenericObjectIdTarget),
			builder.equal(t.column("STRRELATIONTYPE", String.class), relationType)));
		return findRelationsImpl(query);
	}

	@Override
	public Collection<GenericObjectRelationVO> findRelationsByGenericObjectId(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_RELATION").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.column("INTID", Integer.class));
		query.where(builder.or(
			builder.equal(t.column("INTID_T_UD_GO_1", Integer.class), iGenericObjectId),
			builder.equal(t.column("INTID_T_UD_GO_2", Integer.class), iGenericObjectId)));
		return findRelationsImpl(query);
	}

	private Collection<GenericObjectRelationVO> findRelationsImpl(DbQuery<Integer> query) throws CommonFinderException, CommonPermissionException {
		Collection<GenericObjectRelationVO> result = new ArrayList<GenericObjectRelationVO>();

		for (Integer id : DataBaseHelper.getDbAccess().executeQuery(query.distinct(true)))
			result.add(MasterDataWrapper.getGenericObjectRelationVO(getMasterDataFacade().get(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), id)));

		return result;
	}

	/**
	 * changes a specific attribute of a specific generic object
	 * @param iGenericObjectId generic object id of object with attribute to change
	 * @param sAttribute			attribute name of attribute to change
	 * @param oValue					new value to set for attribute
	 */
	@Override
	public void setAttribute(Integer iGenericObjectId, String sAttribute, Integer iValueId, Object oValue)
			throws NuclosFatalException, CommonValidationException, NuclosBusinessException {

		// @todo get rid of this method - it doesn't belong to the facade
		// @todo ensure that oValue is not set for non editable attributes and iValue is only set for non editable attributes (throw CommonValidationException)
		// @todo Note that writing to the logbook is always enabled here
		// @todo Replace with LOFB.modify(..., false)?

		final AttributeCache attrcache = AttributeCache.getInstance();
		final Integer iAttributeId = attrcache.getAttribute(
				DalSupportForGO.getGenericObject(iGenericObjectId).getModuleId(), sAttribute).getId();
		final DynamicAttributeVO attrvo = new DynamicAttributeVO(iAttributeId, iValueId, oValue);
		updateGenericObjectAttribute(attrvo, iGenericObjectId, true);
	}

	/**
	 * checks if a given generic object belongs to a given module.
	 * @param iModuleId			 id of module to validate generic object for
	 * @param iGenericObjectId id of generic object to check
	 * @return true if in module or false if not
	 */
	@Override
	public boolean isGenericObjectInModule(Integer iModuleId, Integer iGenericObjectId) throws CommonFinderException {
		return LangUtils.equals(this.getModuleContainingGenericObject(iGenericObjectId), iModuleId);
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the module containing the generic object with the given id.
	 * @throws CommonFinderException if there is no generic object with the given id.
	 */
	@Override
	@RolesAllowed("Login")
	public int getModuleContainingGenericObject(int iGenericObjectId) throws CommonFinderException {
		final EOGenericObjectVO eogo = NucletDalProvider.getInstance().getEOGenericObjectProcessor()
				.getByPrimaryKey(LangUtils.convertId(iGenericObjectId));
		if (eogo == null) {
			throw new CommonFinderException();
		}
		if (eogo.getModuleId() == null) {
			throw new CommonFatalException("moduleId is null");
		}
		return LangUtils.convertId(eogo.getModuleId());
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the state of the generic object with the given id
	 * @throws CommonFinderException
	 */
	@Override
	@RolesAllowed("Login")
	public int getStateIdByGenericObject(int iGenericObjectId) throws CommonFinderException{
		final Integer iAttributeId = AttributeCache.getInstance().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue()).getId();
		return findAttributeByGoAndAttributeId(iGenericObjectId, iAttributeId).getValueId();
	}

	/**
	 * get all generic objects of a given module related to a given generic object
	 * always moves two steps through the relation hierarchy.
	 * only by doing so ie related assets from which orders were created can be found
	 * because demands are in between assets and orders most of the time
	 *
	 * @param iModuleId module to search for related generic objects in. If null, an empty set is returned.
	 * @param iGenericObjectId generic object to find related generic objects for. If null, an empty set is returned.
	 * @param direction direction to get related generic objects for
	 * @param relationType
	 * @return Collection<Integer> collection of generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	@Override
	@RolesAllowed("Login")
	public Set<Integer> getRelatedGenericObjectIds(final Integer iModuleId, final Integer iGenericObjectId, final RelationDirection direction, final String relationType) {
		// @todo optimize: the two steps are unnecessary for part-of relationships. 16.09.04 CR

		final Set<Integer> result = new HashSet<Integer>();
		if (iModuleId != null && iGenericObjectId != null) {

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom r = query.from("T_UD_GO_RELATION").alias("r");
			DbFrom l = query.from("T_UD_GENERICOBJECT").alias("l");
			DbColumnExpression<Integer> genericObject1 = r.column(direction.isReverse() ? "INTID_T_UD_GO_2" : "INTID_T_UD_GO_1", Integer.class);
			DbColumnExpression<Integer> genericObject2 = r.column(direction.isReverse() ? "INTID_T_UD_GO_1" : "INTID_T_UD_GO_2", Integer.class);
			query.multiselect(
				genericObject2,
				l.column("INTID_T_MD_MODULE", Integer.class));
			query.where(builder.and(
				builder.equal(genericObject1, iGenericObjectId),
				builder.equal(genericObject2, l.column("INTID", Integer.class)),
				builder.equal(r.column("STRRELATIONTYPE", String.class), relationType)));
			query.distinct(true);

			List<DbTuple> result1 = DataBaseHelper.getDbAccess().executeQuery(query);
			for (DbTuple tuple1 : result1) {
				final Integer intid1 = tuple1.get(0, Integer.class);
				if (tuple1.get(1, Integer.class).equals(iModuleId)) {
					result.add(intid1);
				}
				else {
					// Reuse existing query
					query.multiselect(genericObject2);
					query.where(builder.and(
						builder.equal(genericObject1, intid1),
						builder.equal(genericObject2, l.column("INTID", Integer.class)),
						builder.equal(r.column("STRRELATIONTYPE", String.class), relationType)));

					for (DbTuple tuple2 : DataBaseHelper.getDbAccess().executeQuery(query)) {
						result.add(tuple2.get(0, Integer.class));
					}
				}
			}

			assert result != null;
			assert !(iModuleId == null) || result.isEmpty();
			assert !(iGenericObjectId == null) || result.isEmpty();
		}

		return result;
	}


	/**
	 * fills the dependants of <code>lowdcvo</code> with the data from the required sub entities.
	 * @param lowdcvo
	 * @param stRequiredSubEntityNames
	 * @precondition stRequiredSubEntityNames != null
	 * @deprecated This method doesn't respect the foreign key field name. Replace with fillDependants().
	 */
	@Deprecated
	private void _fillDependants(GenericObjectWithDependantsVO lowdcvo, Set<String> stRequiredSubEntityNames) throws CommonFinderException {
		helper._fillDependants(lowdcvo, stRequiredSubEntityNames, null, this.getCurrentUserName());
	}

	/**
	 * attaches document to any object
	 * @param mdvoDocument master data value object with fields for table t_ud_go_document, has to be filled
	 * with following fields: comment, createdDate, createdUser, file and genericObjectId.
	 * @param mdvoDocument
	 * @return Id of created master data entry
	 * @todo restrict permission - check module id! requires the right to modify documents
	 */
	@Override
	@RolesAllowed("Login")
	public void attachDocumentToObject(MasterDataVO mdvoDocument) {
		try {
			DependantMasterDataMap map = new DependantMasterDataMap();
			map.addData((String)mdvoDocument.getField("entity"), DalSupportForMD.getEntityObjectVO(mdvoDocument));
			int genericObjectId = ((Integer)mdvoDocument.getField("genericObject")).intValue();
			int moduleId = getModuleContainingGenericObject(genericObjectId);
			helper.createDependants(Modules.getInstance().getEntityNameByModuleId(moduleId), genericObjectId, map);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
	}


	/**
	 * execute a list of rules for the given Object
	 * @param lstRuleVO
	 * @param govo
	 * @param bSaveAfterRuleExecution
	 * @throws CommonBusinessException
	 * @todo restrict permission - check module id!
	 */
	@Override
	@RolesAllowed("ExecuteRulesManually")
	public void executeBusinessRules(List<RuleVO> lstRuleVO, GenericObjectWithDependantsVO govo, boolean bSaveAfterRuleExecution) throws CommonBusinessException {
		RuleEngineFacadeLocal facade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);

		final RuleObjectContainerCVO loccvo = facade.executeBusinessRules(lstRuleVO, new RuleObjectContainerCVO(Event.USER, govo, govo.getDependants()), false);
		if (bSaveAfterRuleExecution) {
			this.modify(loccvo.getGenericObject(), loccvo.getDependants(), true);
		}
	}

	/**
	 * creates a LogbookEntry
	 */
	@Override
	public void createLogbookEntry(Integer genericObjectId, Integer attributeId, Integer masterdataMetaId, Integer masterdataMetaFieldId,
		Integer masterdataRecordId, String masterdataAction, Integer oldValueId, Integer oldValueExternalId,
		String oldValue, Integer newValueId, Integer newValueExternalId, String newValue)
	{
		MasterDataVO mdvo = MasterDataWrapper.wrapLogbookVO(new LogbookVO(new NuclosValueObject(),
			genericObjectId,
			attributeId,
			masterdataMetaId,
			masterdataMetaFieldId,
			masterdataRecordId,
			masterdataAction,
			oldValueId,
			oldValueExternalId,
			oldValue,
			newValueId,
			newValueExternalId,
			newValue));

		try {
			getMasterDataFacade().create(NuclosEntity.GENERICOBJECTLOGBOOK.getEntityName(), mdvo, null);
		}
		catch(NuclosBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * creates a GenericObjectAttribute with the given values
	 * @param genericObjectId
	 * @param attributeId
	 * @param valueId
	 * @param canonicalValue
	 * @return a collection of BadAttributeValueException
	 */
	@Override
	public Collection<BadAttributeValueException> createGenericObjectAttribute(Integer genericObjectId, Integer attributeId, Integer valueId, String canonicalValue, boolean logbookTracking) throws CreateException {
		final Collection<BadAttributeValueException> badAttributes = new ArrayList<BadAttributeValueException>();
		final String field = DalSupportForGO.getEntityFieldFromAttribute(attributeId);
		final EntityObjectVO eo = DalSupportForGO.getEntityObject(genericObjectId);
		eo.getFieldIds().put(field, LangUtils.convertId(valueId));
		eo.getFields().put(field, DalSupportForGO.convertFromCanonicalAttributeValue(attributeId, canonicalValue));
		eo.flagUpdate();

		DalCallResult dcr = DalSupportForGO.getEntityObjectProcesserForGenericObject(genericObjectId).insertOrUpdate(eo);
		if (dcr.hasException()) {
			for (DalBusinessException dbe : dcr.getExceptions()) {
				badAttributes.add(new BadAttributeValueException(-1, genericObjectId, canonicalValue, attributeId, AttributeCache.getInstance().getAttribute(attributeId), dbe.getMessage()));
			}
		}

		if (logbookTracking) {
			createLogbookEntry( genericObjectId, attributeId, null, null, null, null, null, null, null,
				DalSupportForGO.isEntityFieldForeign(attributeId)?null:valueId,
				DalSupportForGO.isEntityFieldForeign(attributeId)?valueId:null,
				canonicalValue);
		}
		return badAttributes;
	}


	/**
	 * updates a GenericObjectAttribute
	 */
	@Override
	public void updateGenericObjectAttribute(DynamicAttributeVO vo, Integer genericObjectId, boolean logbookTracking) throws NuclosBusinessException {
		final EntityObjectVO eo = DalSupportForGO.getEntityObjectProcesserForGenericObject(genericObjectId).getByPrimaryKey(LangUtils.convertId(genericObjectId));
		final String field = DalSupportForGO.getEntityFieldFromAttribute(vo.getAttributeId());

		if (logbookTracking) {
			helper.createLogBookEntryIfNecessary(eo, null, vo);
		}
		eo.getFieldIds().put(field, LangUtils.convertId(vo.getValueId()));
		eo.getFields().put(field, vo.getValue());
		eo.flagUpdate();

		DalCallResult dcr = NucletDalProvider.getInstance().getEntityObjectProcessor(eo.getEntity()).insertOrUpdate(eo);
		dcr.throwFirstBusinessExceptionIfAny();
	}

	/**
	 * returns the GenericObjectAttribute matching the given genericObjectId and attributeId
	 * @param genericObjectId
	 * @param attributeId
	 * @return the DynamicAttributeVO matching the given genericObjectId and attributeId
	 */
	@Override
	public DynamicAttributeVO findAttributeByGoAndAttributeId(Integer genericObjectId, Integer attributeId) throws CommonFinderException {

		EntityObjectVO eo = DalSupportForGO.getEntityObject(genericObjectId);
		String field = DalSupportForGO.getEntityFieldFromAttribute(attributeId);

		return DalSupportForGO.getDynamicAttributeVO(eo, field);
	}

	private boolean getUsesRuleEngine(String sEntityName, boolean userEvent) {

		DbQuery<DbTuple> query = DataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_RULE_EVENT").alias(ProcessorFactorySingleton.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.column("intid", Integer.class).alias("intid"));
		query.multiselect(columns);

		List<DbCondition> lstDbCondition = new ArrayList<DbCondition>();
		lstDbCondition.add(DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strmasterdata", String.class),
			DataBaseHelper.getDbAccess().getQueryBuilder().literal(sEntityName)));
		if(userEvent) {
			lstDbCondition.add(DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strevent", String.class),
				DataBaseHelper.getDbAccess().getQueryBuilder().literal(RuleEventUsageVO.USER_EVENT)));
		} else {
			DbCondition orCondition = DataBaseHelper.getDbAccess().getQueryBuilder().or(DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strevent", String.class),
				DataBaseHelper.getDbAccess().getQueryBuilder().literal(RuleEventUsageVO.SAVE_EVENT)),
				DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strevent", String.class),
					DataBaseHelper.getDbAccess().getQueryBuilder().literal(RuleEventUsageVO.DELETE_EVENT)),
					DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strevent", String.class),
						DataBaseHelper.getDbAccess().getQueryBuilder().literal(RuleEventUsageVO.SAVE_AFTER_EVENT)),
						DataBaseHelper.getDbAccess().getQueryBuilder().equal(from.column("strevent", String.class),
							DataBaseHelper.getDbAccess().getQueryBuilder().literal(RuleEventUsageVO.DELETE_AFTER_EVENT)));
			lstDbCondition.add(orCondition);
		}

		query.where(DataBaseHelper.getDbAccess().getQueryBuilder().and(lstDbCondition.toArray(new DbCondition[0])));

		List<DbTuple> usages = DataBaseHelper.getDbAccess().executeQuery(query);

		return (usages.size() > 0);
	}
}

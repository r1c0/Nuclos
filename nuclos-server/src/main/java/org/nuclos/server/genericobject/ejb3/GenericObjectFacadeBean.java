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

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.api.eventsupport.DeleteFinalSupport;
import org.nuclos.api.eventsupport.DeleteSupport;
import org.nuclos.api.eventsupport.InsertFinalSupport;
import org.nuclos.api.eventsupport.InsertSupport;
import org.nuclos.api.eventsupport.UpdateFinalSupport;
import org.nuclos.api.eventsupport.UpdateSupport;
import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
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
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeLocal;
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
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
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
import org.nuclos.server.validation.ValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all generic object management functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class GenericObjectFacadeBean extends NuclosFacadeBean implements GenericObjectFacadeRemote {

	private static final Logger LOG = Logger.getLogger(GenericObjectFacadeBean.class);

	//

	private GenericObjectFacadeHelper helper;

	private MasterDataFacadeLocal masterDataFacade;

	private AttributeCache attributeCache;

	private ValidationSupport validationSupport;

	/**
	 * @deprecated
	 */
	private MasterDataFacadeHelper masterDataFacadeHelper;

	public GenericObjectFacadeBean() {
	}

	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.masterDataFacadeHelper = masterDataFacadeHelper;
	}

	@Autowired
	void setGenericObjectFacadeHelper(GenericObjectFacadeHelper genericObjectFacadeHelper) {
		this.helper = genericObjectFacadeHelper;
	}

	@Autowired
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
	}

	private AttributeCache getAttributeCache() {
		if (attributeCache == null) {
			attributeCache = AttributeCache.getInstance();
		}
		return attributeCache;
	}

	private final MasterDataFacadeLocal getMasterDataFacade() {
		if (masterDataFacade == null) {
			masterDataFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		}
		return masterDataFacade;
	}

	@RolesAllowed("Login")
	public GenericObjectMetaDataVO getMetaData() {
		return GenericObjectMetaDataCache.getInstance().getMetaDataCVO();
	}

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
	@RolesAllowed("Login")
	public GenericObjectVO get(Integer iGenericObjectId, boolean bCheckPermission) throws CommonFinderException, CommonPermissionException {
//		GenericObjectVO govo = MasterDataWrapper.getGenericObjectVO(getMasterDataFacade().get(ENTITY_NAME_GENERICOBJECT, iGenericObjectId));
//		govo = helper.getValueObject(govo);
		
		final GenericObjectVO govo;
		try {
			govo = DalSupportForGO.getGenericObject(iGenericObjectId);
		} catch (Exception e) {
			throw new CommonFinderException();
		}
		
		if (bCheckPermission){
			checkReadAllowedForModule(govo.getModuleId(), iGenericObjectId);
			getRecordGrantUtils().checkInternal(MetaDataServerProvider.getInstance().getEntity(
					IdUtils.toLongId(govo.getModuleId())).getEntity(), IdUtils.toLongId(iGenericObjectId));
		}
		assert govo != null;
		return govo;
	}

	private void checkForStaleVersion(GenericObjectVO oldGO, GenericObjectVO newGO) throws CommonStaleVersionException {
		if (oldGO.getVersion() != newGO.getVersion()) {
			throw new CommonStaleVersionException("generic object", newGO.toDescription(), oldGO.toDescription());
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
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO getWithDependants(Integer iGenericObjectId, Set<String> stRequiredSubEntityNames, String customUsage)
			throws CommonPermissionException, CommonFinderException {

		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(this.get(iGenericObjectId), new DependantMasterDataMap());

		final Set<String> stSubEntityNames;
		if (stRequiredSubEntityNames == null) {
			final GenericObjectMetaDataCache lometadataprovider = GenericObjectMetaDataCache.getInstance();
			stSubEntityNames = lometadataprovider.getSubFormEntityNamesByLayoutId(lometadataprovider.getBestMatchingLayoutId(result.getUsageCriteria(lometadataprovider, customUsage), false));
		}
		else {
			stSubEntityNames = stRequiredSubEntityNames;
		}
		_fillDependants(result, result.getUsageCriteria(getAttributeCache(), customUsage), stSubEntityNames, customUsage);

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
	@RolesAllowed("Login")
	public DependantMasterDataMap reloadDependants(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bAll, String customUsage) throws CommonFinderException {
		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

		final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			Modules.getInstance().getEntityNameByModuleId(govo.getModuleId()),govo.getId(),false,customUsage);

		if (mpDependants == null) {
			mpDependants = new DependantMasterDataMap();
		}

		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
			// care only about dependant data which are on the highest level
			if (collSubEntities.get(eafn) == null) {
				final String entity = eafn.getEntityName();
				final String sForeignKeyFieldName = LangUtils.defaultIfNull(eafn.getFieldName(),
						ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
				Collection<MasterDataVO> collmdVO = getMasterDataFacade().getDependantMasterData(
						entity, sForeignKeyFieldName, govo.getId());

				mpDependants.setData(entity, CollectionUtils.transform(collmdVO,
						new MasterDataToEntityObjectTransformer(entity)));

				if (bAll) {
					for (MasterDataVO mdVO : collmdVO) {
						// now read all dependant data of the child subforms
						getMasterDataFacade().readAllDependants(
								entity, mdVO.getIntId(), mdVO.getDependants(), mdVO.isRemoved(),
								entity, collSubEntities);
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
	@RolesAllowed("Login")
	public RuleObjectContainerCVO getRuleObjectContainerCVO(Event event, Integer iGenericObjectId, String customUsage)
			throws CommonPermissionException, CommonFinderException {
		// @todo merge with getWithDependants?

		final GenericObjectVO govo = this.get(iGenericObjectId);

		return new RuleObjectContainerCVO(event, govo, reloadDependants(govo, null, true, customUsage));
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
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO getHistorical(int iGenericObjectId, Date dateHistorical, String customUsage) throws CommonFinderException, CommonPermissionException {
		debug("Entering getHistorical(Integer iGenericObjectId, Date dateHistorical)");

		if (dateHistorical == null) {
			throw new NullArgumentException("dateHistorical");
		}
		final RuleObjectContainerCVO loccvo = this.getRuleObjectContainerCVO(Event.UNDEFINED, iGenericObjectId, customUsage);
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds) {

		return this.getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, Collections.<String>emptySet(), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, String customUsage) {

		return this.getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, Collections.<String>emptySet(), customUsage);
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @return list of generic object value objects with specified dependants and without parent objects!
	 * @postcondition result != null
	 * @Deprecated use with customUsage
	 */
	@Deprecated
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames) {
		return helper.getGenericObjects(iModuleId, clctexpr, stRequiredSubEntityNames, this.getCurrentUserName(), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, String customUsage) {
		return helper.getGenericObjects(iModuleId, clctexpr, stRequiredSubEntityNames, this.getCurrentUserName(), customUsage);
	}

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @return list of generic object value objects with specified dependants and without parent objects!
	 * @postcondition result != null
	 * @Deprecated use with customUsage
	 */
	@Deprecated
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects) {
		return getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	public List<GenericObjectWithDependantsVO> getGenericObjects(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, String customUsage, boolean bIncludeParentObjects) {
			return getGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, customUsage);
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public ProxyList<GenericObjectWithDependantsVO> getGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules) {
		return new GenericObjectProxyList(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public ProxyList<GenericObjectWithDependantsVO> getGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules, String customUsage) {
		return new GenericObjectProxyList(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects, customUsage);
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public ProxyList<GenericObjectWithDependantsVO> getPrintableGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules) {
		return getPrintableGenericObjectsWithDependants(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects, bIncludeSubModules, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public ProxyList<GenericObjectWithDependantsVO> getPrintableGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, boolean bIncludeSubModules, String customUsage) {

		stRequiredAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		stRequiredAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());
		//stRequiredAttributeIds.add(NuclosEOField.STATEICON.getMetaData().getId().intValue());

		ProxyList<GenericObjectWithDependantsVO> proxyList = this.getGenericObjectsWithDependants(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects, bIncludeSubModules, customUsage);

		// remove all attribute and subform values on which the current user has no read permission
		for (GenericObjectWithDependantsVO gowdvo : proxyList) {
			Integer iStatus = gowdvo.getAttribute(NuclosEOField.STATE.getMetaData().getField(), getAttributeCache()).getValueId();

			if (iStatus != null) {
				for (DynamicAttributeVO dynamicAttributeVO : gowdvo.getAttributes()) {
					String sAtributeName = getAttributeCache().getAttribute(dynamicAttributeVO.getAttributeId()).getName();

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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public TruncatableCollection<GenericObjectWithDependantsVO> getRestrictedNumberOfGenericObjects(Integer iModuleId,
			CollectableSearchExpression clctexpr, Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames,
			int iMaxRowCount) {
		return getRestrictedNumberOfGenericObjects(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY), iMaxRowCount);
	}
	
	@RolesAllowed("Login")
	@Deprecated
	public TruncatableCollection<GenericObjectWithDependantsVO> getRestrictedNumberOfGenericObjects(Integer iModuleId,
			CollectableSearchExpression clctexpr, Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, String customUsage,
			int iMaxRowCount) {

		if (iMaxRowCount <= 0) {
			throw new IllegalArgumentException("iMaxRowCount == " + iMaxRowCount);
		}

		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId));
		final List<GenericObjectWithDependantsVO> lstResult = new ArrayList<GenericObjectWithDependantsVO>(Math.min(iMaxRowCount + 1, 1000));

		for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpression(appendRecordGrants(clctexpr, eMeta), iMaxRowCount + 1, true)) {
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				if (!stRequiredSubEntityNames.isEmpty())
					_fillDependants(go, go.getUsageCriteria(getAttributeCache(), customUsage), stRequiredSubEntityNames, customUsage);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			lstResult.add(go);
		}

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
	public List<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchExpression cse) {
		EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId));
		List<Long> ids = NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getIdsBySearchExprUserGroups(
			appendRecordGrants(cse, eMeta.getEntity()),	IdUtils.toLongId(iModuleId), getCurrentUserName());
		return DalUtils.convertLongIdList(ids);
	}

	/**
	 * gets the ids of all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param cond condition that the generic objects to be found must satisfy
	 * @return List<Integer> list of generic object ids
	 */
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
	public Collection<GenericObjectWithDependantsVO> getGenericObjectsMore(Integer iModuleId, List<Integer> lstIds,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, String customUsage, boolean bIncludeParentObjects) {

		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId));

		final List<EntityObjectVO> eos = NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta.getEntity()).getBySearchExpressionAndPrimaryKeys(new CollectableSearchExpression() /*appendRecordGrants(new CollectableSearchExpression(), eMeta) - No check of recordgrants here*/, DalUtils.convertIntegerIdList(lstIds));
		List<GenericObjectWithDependantsVO> result = new ArrayList<GenericObjectWithDependantsVO>(eos.size());

		for (EntityObjectVO eo : eos) {
			final GenericObjectWithDependantsVO go = new GenericObjectWithDependantsVO(DalSupportForGO.getGenericObjectVO(eo), new DependantMasterDataMap());
			try {
				_fillDependants(go, go.getUsageCriteria(getAttributeCache(), customUsage), stRequiredSubEntityNames, customUsage);
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
	 * @throws CommonFinderException 
	 * @throws CommonCreateException 
	 * @throws CommonPermissionException 
	 * @throws NuclosBusinessRuleException 
	 *
	 * @precondition gowdvo.getId() == null
	 * @precondition Modules.getInstance().isSubModule(iModuleId.intValue()) --> gowdvo.getParentId() != null
	 * @precondition (gowdvo.getDependants() != null) -> gowdvo.getDependants().dependantsAreNew()
	 * @precondition stRequiredSubEntityNames != null
	 *
	 * @nucleus.permission mayWrite(module)
	 * 
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public GenericObjectWithDependantsVO create(GenericObjectWithDependantsVO gowdvo, Set<String> stRequiredSubEntityNames) 
			throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException, CommonFinderException {
		return create(gowdvo, stRequiredSubEntityNames, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO create(GenericObjectWithDependantsVO gowdvo, Set<String> stRequiredSubEntityNames, String customUsage)
			throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException, CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new IllegalArgumentException("stRequiredSubEntityNames");
		}
		final GenericObjectVO govoCreated = this.create(gowdvo, customUsage);

		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(govoCreated, new DependantMasterDataMap());
		_fillDependants(result, result.getUsageCriteria(getAttributeCache(), customUsage), stRequiredSubEntityNames, customUsage);

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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public GenericObjectVO create(GenericObjectWithDependantsVO gowdvo)
			throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException {
		return create(gowdvo, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public GenericObjectVO create(GenericObjectWithDependantsVO gowdvo, String customUsage)
			throws CommonPermissionException, NuclosBusinessRuleException, CommonCreateException {

		debug("Entering create(GenericObjectWithDependantsVO)");
		
		if (gowdvo.getId() != null) {
			throw new IllegalArgumentException("govo.getId()");
		}

		final int iModuleId = gowdvo.getModuleId();

//		if (!bMainModuleObject && gowdvo.getParentId() == null) {
//			throw new NullArgumentException("govo.getParentId");
//		}

		DependantMasterDataMap mpDependants = gowdvo.getDependants();

		final boolean useRuleEngineSave = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
				IdUtils.toLongId(iModuleId)).getEntity(), RuleEventUsageVO.SAVE_EVENT);
		final boolean useEventSupports = this.getUsesEventSupports(iModuleId, InsertSupport.name);
		if(useRuleEngineSave || useEventSupports){
			/** @todo check if loccvoResult can safely be ignored */
			final RuleObjectContainerCVO loccvoResult = fireSaveEvent(Event.CREATE_BEFORE, gowdvo, mpDependants, false, customUsage);
			mpDependants = loccvoResult.getDependants();
		}

		EntityObjectVO dalVO = DalSupportForGO.wrapGenericObjectVO(gowdvo);
		final String user = getCurrentUserName();
		DalUtils.updateVersionInformation(dalVO, user);
		dalVO.setId(DalUtils.getNextId());
		dalVO.flagNew();

		// generate system identifier:
		final String sCanonicalValueSystemIdentifier = BusinessIDFactory.generateSystemIdentifier(iModuleId);
		assert !StringUtils.isNullOrEmpty(sCanonicalValueSystemIdentifier);
		dalVO.getFields().put(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(), sCanonicalValueSystemIdentifier);

		try {
			validationSupport.validate(dalVO, mpDependants);

			NucletDalProvider.getInstance().getEntityObjectProcessor(dalVO.getEntity()).insertOrUpdate(dalVO);

			Date sysdate = new Date();
			/**
			 * new "old" generic object record
			 */
			EOGenericObjectVO eogo = new EOGenericObjectVO();
			eogo.setId(dalVO.getId());
			eogo.setCreatedBy(user);
			eogo.setCreatedAt(InternalTimestamp.toInternalTimestamp(sysdate));
			eogo.setVersion(1);
			eogo.setChangedBy(user);
			eogo.setChangedAt(InternalTimestamp.toInternalTimestamp(sysdate));
			eogo.setModuleId(IdUtils.toLongId(iModuleId));
			eogo.flagNew();
			NucletDalProvider.getInstance().getEOGenericObjectProcessor().insertOrUpdate(eogo);

			helper.trackChangesToLogbook(null, dalVO);
		}
		catch(Exception e1) {
			throw new CommonCreateException(e1);
		}

		final Integer id = IdUtils.unsafeToId(dalVO.getId());
		final GenericObjectVO goVO = DalSupportForGO.getGenericObjectVO(dalVO);

		// get default object group assigned to current user
		Integer groupId = null;

		for (final MasterDataVO mdvo : getMasterDataFacade().getMasterData(NuclosEntity.USER.getEntityName(), null, true)) {
			if (user.equals(mdvo.getField("name"))) {
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
			String entityNameByModuleId = Modules.getInstance().getEntityNameByModuleId(gowdvo.getModuleId());

			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityNameByModuleId,id,false,customUsage);

			helper.createDependants(Modules.getInstance().getEntityNameByModuleId(iModuleId), id, mpDependants, collSubEntities, customUsage);
		}

		// Write a possible origin object into the logbook:
		final DynamicAttributeVO attrvoOrigin = gowdvo.getAttribute(NuclosEOField.ORIGIN.getMetaData().getId().intValue());
		if (attrvoOrigin != null) {
			createLogbookEntry(id, attrvoOrigin.getAttributeId(), null, null, null, null, null,
				null, null, null, null, (String) attrvoOrigin.getValue());
		}

		GenericObjectVO result;

		if (Modules.getInstance().getUsesStateModel(iModuleId)) {
			// create first state entry of generic object:
			try {
				this.enterInitialState(goVO, customUsage);



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

		final boolean useRuleEngineSaveAfter = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
				IdUtils.toLongId(iModuleId)).getEntity(), RuleEventUsageVO.SAVE_AFTER_EVENT);
		final boolean useFinalEventSupports = this.getUsesEventSupports(iModuleId, InsertFinalSupport.name);
		
		if(useRuleEngineSaveAfter || useFinalEventSupports){
			try {
				mpDependants = reloadDependants(result, mpDependants, true, customUsage);
				fireSaveEvent(Event.CREATE_AFTER, result, mpDependants, true, customUsage);
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public GenericObjectWithDependantsVO modify(Integer iModuleId, GenericObjectWithDependantsVO lowdcvo)
			throws CommonCreateException, CommonFinderException, CommonRemoveException,
			CommonPermissionException, CommonStaleVersionException, NuclosBusinessException,
			CommonValidationException {
		return modify(iModuleId, lowdcvo, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public GenericObjectWithDependantsVO modify(Integer iModuleId, GenericObjectWithDependantsVO lowdcvo, String customUsage)
			throws CommonCreateException, CommonFinderException, CommonRemoveException,
			CommonPermissionException, CommonStaleVersionException, NuclosBusinessException,
			CommonValidationException {

		if (iModuleId == null) {
			throw new NullArgumentException("iModuleId");
		}
		if (lowdcvo.getModuleId() != iModuleId) {
			throw new IllegalArgumentException("iModuleId");
		}

		final GenericObjectVO govoUpdated = this.modify(lowdcvo, lowdcvo.getDependants(), true, customUsage);

		final GenericObjectMetaDataCache lometadataprovider = GenericObjectMetaDataCache.getInstance();
		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(govoUpdated, new DependantMasterDataMap());
		final UsageCriteria usage = govoUpdated.getUsageCriteria(getAttributeCache(), customUsage);
		final Set<String> collSubEntityNames = lometadataprovider.getSubFormEntityNamesByLayoutId(
				lometadataprovider.getBestMatchingLayoutId(usage, false));
		_fillDependants(result, usage, collSubEntityNames, customUsage);

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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException{
		return modify(govo, mpDependants, bFireSaveEvent, true, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent, String customUsage)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException{
		return modify(govo, mpDependants, bFireSaveEvent, true, customUsage);
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent, boolean bCheckPermission)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException {
		return modify(govo, mpDependants, bFireSaveEvent, bCheckPermission, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	
	@RolesAllowed("Login")
	public GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent, boolean bCheckPermission, String customUsage)
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
			getRecordGrantUtils().checkWriteInternal(dbEoVO.getEntity(), dbEoVO.getId());
		}

		this.checkForStaleVersion(dbGoVO, govo);

		final boolean useRuleEngineSave = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
				IdUtils.toLongId(govo.getModuleId())).getEntity(), RuleEventUsageVO.SAVE_EVENT);
		final boolean useEventSupports = this.getUsesEventSupports(govo.getModuleId(), UpdateSupport.name);
		
		if (bFireSaveEvent && (useRuleEngineSave || useEventSupports)) {
			this.debug("Modifying (Start rules)");
			final RuleObjectContainerCVO loccvoResult = this.fireSaveEvent(Event.MODIFY_BEFORE, govo, mpDependants, false, customUsage);
			govo = loccvoResult.getGenericObject();
			mpDependants = loccvoResult.getDependants(true);
		}

		this.debug("Modifying (Start change genericobject)");

		EntityObjectVO eoUpdated = DalSupportForGO.wrapGenericObjectVO(govo);

		validationSupport.validate(eoUpdated, mpDependants);

		DalUtils.updateVersionInformation(eoUpdated, getCurrentUserName());
		eoUpdated.flagUpdate();
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(eoUpdated.getEntity()).insertOrUpdate(eoUpdated);
		}
		catch (DbException e) {
			throw new NuclosBusinessException(e);
		}

		helper.trackChangesToLogbook(dbEoVO, eoUpdated);


		if (mpDependants != null) {
			this.debug("Modifying (Start change dependants)");

			String entityNameByModuleId = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());

			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityNameByModuleId,govo.getId(),false,customUsage);

			mpDependants.setParent(entityNameByModuleId, govo.getId(), collSubEntities);

			// First protocol all the modified records
			final Set<Integer> stExcluded = new HashSet<Integer>();
			getMasterDataFacade().protocolDependantChanges(govo.getId(), mpDependants, stExcluded, false);

			for (String sEntityName : mpDependants.getEntityNames()) {
				for (EntityObjectVO mdVO : mpDependants.getData(sEntityName)) {
					getMasterDataFacade().readAllDependants(sEntityName, IdUtils.unsafeToId(mdVO.getId()), mdVO.getDependants(), mdVO.isFlagRemoved(), sEntityName, collSubEntities);
				}
			}

			getMasterDataFacade().modifyDependants(entityNameByModuleId,govo.getId(),govo.isRemoved(),mpDependants, customUsage);

			// .. and then all the newly created ones, excluding the already written (i.e. the modified).
			getMasterDataFacade().protocolDependantChanges(govo.getId(), mpDependants, stExcluded, true);
		}

		this.debug("Modifying (Start read)");
		GenericObjectVO result = get(dbGoVO.getId());

		final boolean useRuleEngineSaveAfter = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
				IdUtils.toLongId(govo.getModuleId())).getEntity(), RuleEventUsageVO.SAVE_AFTER_EVENT);
		final boolean useFinalEventSupports = this.getUsesEventSupports(govo.getModuleId(), UpdateFinalSupport.name);
		
		if (bFireSaveEvent && (useRuleEngineSaveAfter || useFinalEventSupports)) {
			this.debug("Modifying (Start rules after save)");
			mpDependants = reloadDependants(result, mpDependants, true, customUsage);
			this.fireSaveEvent(Event.MODIFY_AFTER, result, mpDependants, true, customUsage);
			result = get(dbGoVO.getId());
		}

		this.debug("Leaving modify(GenericObjectVO govo, DependantMasterDataMap mpDependants)");
		return result;
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
	private RuleObjectContainerCVO fireSaveEvent(Event event, GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean after, String customUsage) throws NuclosBusinessRuleException {
		if (!Modules.getInstance().getUsesRuleEngine(govo.getModuleId())) {
			throw new IllegalArgumentException("govo.getModuleId()");
		}
		
		EventSupportFacadeLocal evSuppFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
		RuleEngineFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);

		String sEntity = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());
		
		RuleObjectContainerCVO retVal = facade.fireRule(sEntity, after ? RuleEventUsageVO.SAVE_AFTER_EVENT : RuleEventUsageVO.SAVE_EVENT, 
				new RuleObjectContainerCVO(event, govo, mpDependants != null ? mpDependants : new DependantMasterDataMap()), null);
		
		String sSupportType = null;
		if (event.equals(Event.CREATE_AFTER)) sSupportType = InsertFinalSupport.name;
		else if (event.equals(Event.CREATE_BEFORE)) sSupportType = InsertSupport.name;
		else if (event.equals(Event.MODIFY_AFTER)) sSupportType = UpdateFinalSupport.name;
		else if (event.equals(Event.MODIFY_BEFORE)) sSupportType = UpdateSupport.name;
		
		retVal = evSuppFacade.fireSaveEventSupport(govo.getModuleId(),sSupportType, 
				new RuleObjectContainerCVO(event, govo, mpDependants != null ? mpDependants : new DependantMasterDataMap()));
		
		return retVal;

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
	private void fireDeleteEvent(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean after, String customUsage) throws NuclosBusinessRuleException {
		if (!Modules.getInstance().getUsesRuleEngine(govo.getModuleId())) {
			throw new IllegalArgumentException("govo.getModuleId()");
		}
		String sEntityName = Modules.getInstance().getEntityNameByModuleId(govo.getModuleId());
		RuleObjectContainerCVO ruleObjectContainerCVO = new RuleObjectContainerCVO(after?Event.DELETE_AFTER:Event.DELETE_BEFORE, govo, mpDependants != null ? mpDependants : new DependantMasterDataMap());
		
		RuleEngineFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
		facade.fireRule(sEntityName, after ? RuleEventUsageVO.DELETE_AFTER_EVENT : RuleEventUsageVO.DELETE_EVENT,ruleObjectContainerCVO, null);
		
		EventSupportFacadeLocal evSuppFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
		
		String sSupportType = null;
		if (after) {
			sSupportType = DeleteSupport.name;
		}
		else {
			sSupportType = DeleteFinalSupport.name;
		}
		
		evSuppFacade.fireDeleteEventSupport(govo.getModuleId(),sSupportType, ruleObjectContainerCVO, true);

	}

	/**
	 * delete generic object from database
	 * @param gowdvo containing the generic object data
	 * @param bDeletePhysically remove from db?
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @nucleus.permission mayDelete(module, bDeletePhysically)
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	public void remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically) throws NuclosBusinessException, CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException, CommonCreateException {
		remove(gowdvo, bDeletePhysically, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}
	@RolesAllowed("Login")
	public void remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically, String customUsage) throws NuclosBusinessException, CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException, CommonCreateException {
		this.debug("Entering remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically)");

		final int iModuleId;

		LayoutFacadeLocal layoutFacade =  ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

//		try {
//			final GenericObjectVO dbGoVO = MasterDataWrapper.getGenericObjectVO(getMasterDataFacade().get(NuclosEntity.GENERICOBJECT.getEntityName(), gowdvo.getId()));
			final GenericObjectVO dbGoVO = DalSupportForGO.getGenericObject(gowdvo.getId(), gowdvo.getModuleId());

			iModuleId = dbGoVO.getModuleId();
			String entity = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId)).getEntity();

			this.checkDeleteAllowedForModule(iModuleId, dbGoVO.getId(), bDeletePhysically);
			getRecordGrantUtils().checkDeleteInternal(entity, IdUtils.toLongId(dbGoVO.getId()));

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
				Modules.getInstance().getEntityNameByModuleId(dbGoVO.getModuleId()),dbGoVO.getId(),false, customUsage);

			final boolean useRuleEngineDelete = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
					IdUtils.toLongId(iModuleId)).getEntity(), RuleEventUsageVO.DELETE_EVENT);
			final boolean useEventSupports = this.getUsesEventSupports(iModuleId, DeleteSupport.name);
			
			if(useRuleEngineDelete || useEventSupports) {
				this.fireDeleteEvent(gowdvo, gowdvo.getDependants(), false, customUsage);
			}

			final UsageCriteria usage = gowdvo.getUsageCriteria(getAttributeCache(), customUsage);
			DalCallResult dalResult;
			if (bDeletePhysically) {
				//@see  	NUCLOS-708. clear dependants map first.
				for (String sDependantEntityName : gowdvo.getDependants().getEntityNames()) {
					gowdvo.getDependants().setData(sDependantEntityName, new ArrayList<EntityObjectVO>(0));
				}
				_fillDependants(gowdvo, usage, new HashSet<String>(), customUsage);

				for (EntityAndFieldName eafn : collSubEntities.keySet()) {
					// care only about subforms which are on the highest level
					if (collSubEntities.get(eafn) == null) {

						// mark all dependant data as removed
						for (EntityObjectVO mdVO : gowdvo.getDependants().getData(eafn.getEntityName())) {
							mdVO.flagRemove();
							getMasterDataFacade().readAllDependants(eafn.getEntityName(),
									IdUtils.unsafeToId(mdVO.getId()), mdVO.getDependants(), mdVO.isFlagRemoved(), eafn.getEntityName(), collSubEntities);
						}
					}
				}

				// remove dependant data
				masterDataFacadeHelper.removeDependants(gowdvo.getDependants(), customUsage);
				removeDependants(dbGoVO, customUsage);
				helper.removeLogBookEntries(dbGoVO.getId());
				helper.removeDependantTaskObjects(dbGoVO.getId());
				helper.removeGroupBelonging(dbGoVO.getId());
//				getMasterDataFacade().remove(NuclosEntity.GENERICOBJECT.getEntityName(), MasterDataWrapper.wrapGenericObjectVO(dbGoVO), false);
				try {
					NucletDalProvider.getInstance().getEOGenericObjectProcessor().delete(IdUtils.toLongId(dbGoVO.getId()));
					DalSupportForGO.getEntityObjectProcessor(dbGoVO.getModuleId()).delete(IdUtils.toLongId(dbGoVO.getId()));
				}
				catch (DbException e) {
					throw new NuclosBusinessException(e);
				}
			}
			else {
//				dbGoVO.setDeleted(true);
//				getMasterDataFacade().modify(NuclosEntity.GENERICOBJECT.getEntityName(), MasterDataWrapper.wrapGenericObjectVO(dbGoVO), null);

				EntityObjectVO eoToUpdate = DalSupportForGO.wrapGenericObjectVO(dbGoVO);

				helper.createLogBookEntryIfNecessary(eoToUpdate, null, new DynamicAttributeVO(
						IdUtils.unsafeToId(NuclosEOField.LOGGICALDELETED.getMetaData().getId()), null, Boolean.TRUE));
				eoToUpdate.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), Boolean.TRUE);
				DalUtils.updateVersionInformation(eoToUpdate, getCurrentUserName());
				eoToUpdate.flagUpdate();

				try {
					DalSupportForGO.getEntityObjectProcessor(dbGoVO.getModuleId()).insertOrUpdate(eoToUpdate);
				}
				catch (DbException e) {
					throw new NuclosBusinessException(e);
				}
			}
//		}
//		catch (CommonValidationException ex) {
//			throw new CommonFatalException(ex);
//		}

		if (isInfoEnabled()) {
			info("The entry " + gowdvo.getSystemIdentifier() + " (Id: " + gowdvo.getId() + ") has been deleted.");
		}

		final boolean useRuleEngineDeleteAfter = this.getUsesRuleEngine(MetaDataServerProvider.getInstance().getEntity(
				IdUtils.toLongId(iModuleId)).getEntity(), RuleEventUsageVO.DELETE_AFTER_EVENT);
		final boolean useFinalEventSupports = this.getUsesEventSupports(iModuleId, DeleteFinalSupport.name);
		
		if(useRuleEngineDeleteAfter || useFinalEventSupports) {
			this.fireDeleteEvent(gowdvo, gowdvo.getDependants(), true, customUsage);
		}

		this.debug("Leaving remove(GenericObjectVO govo, boolean bDeletePhysically)");
	}

	/**
	 * update generic object in database
	 * @param sEntityName name of the entity
	 * @throws NuclosBusinessRuleException
	 */
	@RolesAllowed("Login")
	public void updateGenericObjectEntries(String sEntityName) throws NuclosBusinessException {
		this.debug("Entering updateGenericObjectEntries(String sEntityName)");
		
		Integer iModuleId = null;
		if (Modules.getInstance().isModuleEntity(sEntityName))
			iModuleId =	Modules.getInstance().getModuleIdByEntityName(sEntityName);
		else
			iModuleId =	IdUtils.unsafeToId(MetaDataServerProvider.getInstance().getEntity(sEntityName).getId());
		
		if (!Modules.getInstance().isModuleEntity(sEntityName)) {
			 List<Integer> lstGenericObjectIds = getGenericObjectIds(iModuleId, (CollectableSearchCondition)null);
			 for (Integer id : lstGenericObjectIds) {
				// @todo helper.removeLogBookEntries(id);
				helper.removeDependantTaskObjects(id);
				helper.removeGroupBelonging(id);
				
				try {
					NucletDalProvider.getInstance().getEOGenericObjectProcessor().delete(IdUtils.toLongId(id));
				}
				catch (DbException e) {
					throw new NuclosBusinessException(e);
				}
			 }
		}
		else {
			CollectableSearchCondition cond = new CollectableIsNullCondition(
					new CollectableEOEntityField(NuclosEOField.SYSTEMIDENTIFIER.getMetaData(), sEntityName));
			TruncatableCollection<MasterDataVO> mdcvos = masterDataFacadeHelper.getGenericMasterData(sEntityName, cond, true);
			
			for (MasterDataVO mdvo : mdcvos) {
				EOGenericObjectVO eoGenericObjectVO = new EOGenericObjectVO();
				eoGenericObjectVO.flagNew();
				eoGenericObjectVO.setId(IdUtils.toLongId(mdvo.getIntId()));
				eoGenericObjectVO.setModuleId(IdUtils.toLongId(iModuleId));
				DalUtils.updateVersionInformation(eoGenericObjectVO, getCurrentUserName());
				
				try {
					NucletDalProvider.getInstance().getEOGenericObjectProcessor().insertOrUpdate(eoGenericObjectVO);
					
					EntityObjectVO dalVO = DalSupportForGO.getEntityObjectProcessor(iModuleId).getByPrimaryKey(IdUtils.toLongId(mdvo.getIntId()));

					final String sCanonicalValueSystemIdentifier = BusinessIDFactory.generateSystemIdentifier(iModuleId);
					assert !StringUtils.isNullOrEmpty(sCanonicalValueSystemIdentifier);
					dalVO.getFields().put(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(), sCanonicalValueSystemIdentifier);

					dalVO.flagUpdate();
					DalSupportForGO.getEntityObjectProcessor(iModuleId).insertOrUpdate(dalVO);
				}
				catch (DbException e) {
					throw new NuclosBusinessException(e);
				}
			}
		}

		this.debug("Leaving updateGenericObjectEntries(String sEntityName)");
	}

	// replaces the ejbRemove() from GenericObjectBean
	private void removeDependants(GenericObjectVO govo, String customUsage) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException, CommonRemoveException {
		final int iModuleId = govo.getModuleId();

		final Modules modules = Modules.getInstance();
		// delete all dependant leased object relations:
		for (GenericObjectRelationVO vo : findRelationsByGenericObjectId(govo.getId())) {
			getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(),MasterDataWrapper.wrapGenericObjectRelationVO(vo),false, customUsage);
		}

		if (modules.getUsesStateModel(iModuleId)) {
			// delete all dependant state history entries:
			StateFacadeLocal stateFacade = ServerServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
			for (StateHistoryVO history : stateFacade.findStateHistoryByGenericObjectId(govo.getId()))
			{
				getMasterDataFacade().remove(NuclosEntity.STATEHISTORY.getEntityName(), MasterDataWrapper.wrapStateHistoryVO(history), false, customUsage);
			}
		}

		if (modules.isLogbookTracking(iModuleId)) {
			dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_UD_LOGBOOK", "INTID_T_UD_GENERICOBJECT", govo.getId()));
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
	public void restore(Integer iId, String customUsage)throws CommonFinderException, CommonPermissionException, CommonBusinessException {
		this.debug("Entering restore(GenericObjectWithDependantsVO gowdvo)");

		GenericObjectWithDependantsVO goVO = this.getWithDependants(iId, null, customUsage);
		goVO.setDeleted(false);
		this.modify(getModuleContainingGenericObject(iId), goVO, customUsage); // NUCLOSINT-890

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
//			final AttributeCVO attrcvo = attributeCache.getAttribute(iAttributeId);
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
	private void enterInitialState(GenericObjectVO goVO, String customUsage) throws NuclosBusinessException,
			NuclosNoAdequateStatemodelException, CommonFinderException {

		if (!Modules.getInstance().getUsesStateModel(goVO.getModuleId())) {
			throw new IllegalArgumentException("goVO");
		}
		try {
			StateFacadeLocal statefacade = ServerServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
			final Integer iInitialStateId = statefacade.getInitialState(goVO.getId()).getId();
			statefacade.changeStateByRule(Integer.valueOf(goVO.getModuleId()), goVO.getId(), iInitialStateId, customUsage);
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
			else if (!getAttributeCache().contains(iAttributeIdLogbook)) {
				// This may be the case when there is an old attribute entry in the logbook:
				addLogbookIfPossible(result, mdVO);
			}
			else {
				/** @todo consider attribute group permissions here ! */
				final AttributeCVO attrcvo = getAttributeCache().getAttribute(iAttributeIdLogbook);
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
			LOG.info("addLogbookIfPossible: " + e);
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
	@RolesAllowed("Login")
	public void addToGroup(int iGenericObjectId, int iGroupId, boolean blnCheckWriteAllowedForObject)
			throws CommonCreateException, CommonFinderException, CommonPermissionException, NuclosBusinessRuleException {

		final int iModuleId = this.getModuleContainingGenericObject(iGenericObjectId);

		this.checkWriteAllowedForObjectGroup(iModuleId, iGroupId);
		if (blnCheckWriteAllowedForObject) {
			this.checkWriteAllowedForModule(iModuleId, iGenericObjectId);
		}

		// XXX* this.gogrouphome.create(iGroupId, iGenericObjectId);
		GenericObjectGroupFacadeLocal goGroupFacade = ServerServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
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
	@RolesAllowed("Login")
	public void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException,
			CommonRemoveException, CommonStaleVersionException, CommonCreateException
	{

		final int iModuleId = this.getModuleContainingGenericObject(iGenericObjectId);

		checkWriteAllowedForObjectGroup(iModuleId, iGroupId);

		GenericObjectGroupFacadeLocal goGroupFacade = ServerServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
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
	@RolesAllowed("Login")
	public void relate(Integer iModuleIdTarget, Integer iGenericObjectIdTarget,
			Integer iGenericObjectIdSource, String relationType, Date dateValidFrom, Date dateValidUntil,
			String sDescription) throws CommonFinderException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}

		//this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdSource); // why do we have do check if write is allowed here. ?? especially we have to have the sourceModuleId here - not the moduleId from the target
		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdTarget);

		GenericObjectRelationVO relationVO = new GenericObjectRelationVO(
			new NuclosValueObject(),iGenericObjectIdSource, iGenericObjectIdTarget, relationType, dateValidFrom, dateValidUntil, sDescription);
		getMasterDataFacade().create(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), MasterDataWrapper.wrapGenericObjectRelationVO(relationVO), null, null);
	}

	/**
	 * removes the relation with the given id.
	 * @param mpGOTreeNodeRelation
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @nucleus.permission mayWrite(targetModule)
	 */
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
	@RolesAllowed("Login")
	public void removeRelation(Integer iRelationId, Integer iGenericObjectIdTarget, Integer iModuleIdTarget)
			throws CommonRemoveException, CommonFinderException, CommonBusinessException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
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
			getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), mdVO, false, null);
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
	public void unrelate(Integer iModuleIdTarget, Integer iGenericObjectIdTarget, Integer iGenericObjectIdSource, String relationType)
			throws CommonFinderException, NuclosBusinessRuleException, CommonPermissionException, CommonRemoveException {

		if (!isGenericObjectInModule(iModuleIdTarget, iGenericObjectIdTarget)) {
			throw new IllegalArgumentException("iModuleIdTarget");
		}

		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdSource);
		this.checkWriteAllowedForModule(iModuleIdTarget, iGenericObjectIdTarget);

		try {
			for (GenericObjectRelationVO vo : findRelations(iGenericObjectIdSource, relationType, iGenericObjectIdTarget))
			{
				getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), MasterDataWrapper.wrapGenericObjectRelationVO(vo), false, null);
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
	public Collection<GenericObjectRelationVO> findRelations(Integer iGenericObjectIdSource, String relationType, Integer iGenericObjectIdTarget) throws CommonFinderException, CommonPermissionException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_RELATION").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_UD_GO_1", Integer.class), iGenericObjectIdSource),
			builder.equal(t.baseColumn("INTID_T_UD_GO_2", Integer.class), iGenericObjectIdTarget),
			builder.equal(t.baseColumn("STRRELATIONTYPE", String.class), relationType)));
		return findRelationsImpl(query);
	}

	public Collection<GenericObjectRelationVO> findRelationsByGenericObjectId(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_RELATION").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.or(
			builder.equal(t.baseColumn("INTID_T_UD_GO_1", Integer.class), iGenericObjectId),
			builder.equal(t.baseColumn("INTID_T_UD_GO_2", Integer.class), iGenericObjectId)));
		return findRelationsImpl(query);
	}

	private Collection<GenericObjectRelationVO> findRelationsImpl(DbQuery<Integer> query) throws CommonFinderException, CommonPermissionException {
		Collection<GenericObjectRelationVO> result = new ArrayList<GenericObjectRelationVO>();

		for (Integer id : dataBaseHelper.getDbAccess().executeQuery(query.distinct(true)))
			result.add(MasterDataWrapper.getGenericObjectRelationVO(getMasterDataFacade().get(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), id)));

		return result;
	}

	/**
	 * checks if a given generic object belongs to a given module.
	 * @param iModuleId			 id of module to validate generic object for
	 * @param iGenericObjectId id of generic object to check
	 * @return true if in module or false if not
	 */
	public boolean isGenericObjectInModule(Integer iModuleId, Integer iGenericObjectId) throws CommonFinderException {
		return LangUtils.equals(this.getModuleContainingGenericObject(iGenericObjectId), iModuleId);
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the module containing the generic object with the given id.
	 * @throws CommonFinderException if there is no generic object with the given id.
	 */
	@RolesAllowed("Login")
	public int getModuleContainingGenericObject(int iGenericObjectId) throws CommonFinderException {
		final EOGenericObjectVO eogo = NucletDalProvider.getInstance().getEOGenericObjectProcessor()
				.getByPrimaryKey(IdUtils.toLongId(iGenericObjectId));
		if (eogo == null) {
			throw new CommonFinderException();
		}
		if (eogo.getModuleId() == null) {
			throw new CommonFatalException("moduleId is null");
		}
		return IdUtils.unsafeToId(eogo.getModuleId());
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the state of the generic object with the given id
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	public int getStateIdByGenericObject(int iGenericObjectId) throws CommonFinderException{
		final Integer iAttributeId = getAttributeCache().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue()).getId();
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
	@RolesAllowed("Login")
	public Set<Integer> getRelatedGenericObjectIds(final Integer iModuleId, final Integer iGenericObjectId, final RelationDirection direction, final String relationType) {
		// @todo optimize: the two steps are unnecessary for part-of relationships. 16.09.04 CR

		final Set<Integer> result = new HashSet<Integer>();
		if (iModuleId != null && iGenericObjectId != null) {

			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom r = query.from("T_UD_GO_RELATION").alias("r");
			DbFrom l = query.from("T_UD_GENERICOBJECT").alias("l");
			DbColumnExpression<Integer> genericObject1 = r.baseColumn(direction.isReverse() ? "INTID_T_UD_GO_2" : "INTID_T_UD_GO_1", Integer.class);
			DbColumnExpression<Integer> genericObject2 = r.baseColumn(direction.isReverse() ? "INTID_T_UD_GO_1" : "INTID_T_UD_GO_2", Integer.class);
			query.multiselect(
				genericObject2,
				l.baseColumn("INTID_T_MD_MODULE", Integer.class));
			query.where(builder.and(
				builder.equal(genericObject1, iGenericObjectId),
				builder.equal(genericObject2, l.baseColumn("INTID", Integer.class)),
				builder.equal(r.baseColumn("STRRELATIONTYPE", String.class), relationType)));
			query.distinct(true);

			List<DbTuple> result1 = dataBaseHelper.getDbAccess().executeQuery(query);
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
						builder.equal(genericObject2, l.baseColumn("INTID", Integer.class)),
						builder.equal(r.baseColumn("STRRELATIONTYPE", String.class), relationType)));

					for (DbTuple tuple2 : dataBaseHelper.getDbAccess().executeQuery(query)) {
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
	private void _fillDependants(GenericObjectWithDependantsVO lowdcvo, UsageCriteria usage, Set<String> stRequiredSubEntityNames, String customUsage)
			throws CommonFinderException {

		helper._fillDependants(lowdcvo, usage, stRequiredSubEntityNames, null, this.getCurrentUserName(), customUsage);
	}

	/**
	 * attaches document to any object
	 * @param mdvoDocument master data value object with fields for table t_ud_go_document, has to be filled
	 * with following fields: comment, createdDate, createdUser, file and genericObjectId.
	 * @param mdvoDocument
	 * @return Id of created master data entry
	 * @todo restrict permission - check module id! requires the right to modify documents
	 */
	@RolesAllowed("Login")
	public void attachDocumentToObject(MasterDataVO mdvoDocument) {
		try {
			DependantMasterDataMap map = new DependantMasterDataMap();
			final String entity = (String) mdvoDocument.getField("entity");
			map.addData(entity, DalSupportForMD.getEntityObjectVO(entity, mdvoDocument));
			int genericObjectId = ((Integer)mdvoDocument.getField("genericObject")).intValue();
			int moduleId = getModuleContainingGenericObject(genericObjectId);
			helper.createDependants(Modules.getInstance().getEntityNameByModuleId(moduleId), genericObjectId, map, Collections.EMPTY_MAP, null);
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
	@RolesAllowed("ExecuteRulesManually")
	public void executeBusinessRules(List<RuleVO> lstRuleVO, GenericObjectWithDependantsVO govo, boolean bSaveAfterRuleExecution, String customUsage) throws CommonBusinessException {

		RuleEngineFacadeLocal ruleFacade = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
		EventSupportFacadeLocal eventSupportFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
		
		RuleObjectContainerCVO loccvo = ruleFacade.executeBusinessRules(lstRuleVO, new RuleObjectContainerCVO(Event.USER, govo, govo.getDependants()), false, null);
		loccvo = eventSupportFacade.fireCustomEventSupport(govo.getModuleId(), lstRuleVO, loccvo, false);

		if (bSaveAfterRuleExecution) {
			this.modify(loccvo.getGenericObject(), loccvo.getDependants(true), true, customUsage);
		}
	}

	/**
	 * creates a LogbookEntry
	 */
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
			getMasterDataFacade().create(NuclosEntity.GENERICOBJECTLOGBOOK.getEntityName(), mdvo, null, null);
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
	public Collection<BadAttributeValueException> createGenericObjectAttribute(
			Integer genericObjectId, Integer attributeId, Integer valueId, String canonicalValue, boolean logbookTracking) {
		final Collection<BadAttributeValueException> badAttributes = new ArrayList<BadAttributeValueException>();
		final String field = DalSupportForGO.getEntityFieldFromAttribute(attributeId);
		final EntityObjectVO eo = DalSupportForGO.getEntityObject(genericObjectId);
		eo.getFieldIds().put(field, IdUtils.toLongId(valueId));
		eo.getFields().put(field, DalSupportForGO.convertFromCanonicalAttributeValue(attributeId, canonicalValue));
		eo.flagUpdate();

		try {
			DalSupportForGO.getEntityObjectProcesserForGenericObject(genericObjectId).insertOrUpdate(eo);
		}
		catch (DbException dbe) {
			badAttributes.add(new BadAttributeValueException(-1, genericObjectId, canonicalValue, attributeId,
					getAttributeCache().getAttribute(attributeId), dbe.getMessage()));
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
	public void updateGenericObjectAttribute(DynamicAttributeVO vo, Integer genericObjectId, boolean logbookTracking) throws NuclosBusinessException {
		final EntityObjectVO eo = DalSupportForGO.getEntityObjectProcesserForGenericObject(genericObjectId).getByPrimaryKey(
				IdUtils.toLongId(genericObjectId));
		final String field = DalSupportForGO.getEntityFieldFromAttribute(vo.getAttributeId());

		if (logbookTracking) {
			helper.createLogBookEntryIfNecessary(eo, null, vo);
		}
		eo.getFieldIds().put(field, IdUtils.toLongId(vo.getValueId()));
		eo.getFields().put(field, vo.getValue());
		eo.flagUpdate();

		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(eo.getEntity()).insertOrUpdate(eo);
		}
		catch (DbException e) {
			throw new NuclosBusinessException(e);
		}
	}

	/**
	 * returns the GenericObjectAttribute matching the given genericObjectId and attributeId
	 * @param genericObjectId
	 * @param attributeId
	 * @return the DynamicAttributeVO matching the given genericObjectId and attributeId
	 */
	public DynamicAttributeVO findAttributeByGoAndAttributeId(Integer genericObjectId, Integer attributeId) throws CommonFinderException {

		EntityObjectVO eo = DalSupportForGO.getEntityObject(genericObjectId);
		String field = DalSupportForGO.getEntityFieldFromAttribute(attributeId);

		return DalSupportForGO.getDynamicAttributeVO(eo, field);
	}

	private boolean getUsesRuleEngine(String sEntityName, String event) {

		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("intid", Integer.class).alias("intid"));
		query.multiselect(columns);

		List<DbCondition> lstDbCondition = new ArrayList<DbCondition>();
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("strmasterdata", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(sEntityName)));
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("strevent", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(event)));

		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().and(lstDbCondition.toArray(new DbCondition[0])));

		List<DbTuple> usages = dataBaseHelper.getDbAccess().executeQuery(query);

		return (usages.size() > 0);
	}

	
	private boolean getUsesEventSupports(Integer iEntityId, String sEventSupportType) {

		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_EVENTSUPPORT_ENTITY").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("intid", Integer.class).alias("intid"));
		query.multiselect(columns);

		List<DbCondition> lstDbCondition = new ArrayList<DbCondition>();
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("intid_t_md_entity", Integer.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(iEntityId)));
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("streventsupporttype", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(sEventSupportType)));

		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().and(lstDbCondition.toArray(new DbCondition[0])));

		List<DbTuple> usages = dataBaseHelper.getDbAccess().executeQuery(query);

		return (usages.size() > 0);
	}

}

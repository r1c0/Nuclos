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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.BadAttributeValueException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectRelationVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;

// @Local
public interface GenericObjectFacadeLocal {

	/**
	 * @param iGenericObjectId
	 * @return the generic object with the given id
	 * @postcondition result != null
	 * @throws CommonFinderException if there is no object with the given id.
	 * @throws CommonPermissionException if the user doesn't have the permission to view the generic object with the given id.
	 * @nucleus.permission mayRead(module)
	 */
	@RolesAllowed("Login")
	GenericObjectVO get(Integer iGenericObjectId)
		throws CommonFinderException, CommonPermissionException;
	
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
	GenericObjectVO get(Integer iGenericObjectId, boolean bCheckPermission) throws CommonFinderException, CommonPermissionException;


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
	GenericObjectWithDependantsVO getWithDependants(
		Integer iGenericObjectId, Set<String> stRequiredSubEntityNames, String customUsage)
		throws CommonPermissionException, CommonFinderException;

	/**
	 * @param iGenericObjectId
	 * @return the version of the given genericobject id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	Integer getVersion(int iGenericObjectId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * @param gowdvo
	 * @param mpDependants
	 * @param bAll
	 * @return reload the dependant data of the genericobject, if bAll is false, only the direct
	 *         dependants (highest hierarchie of subforms) will be reloaded
	 * @throws CommonFinderException if no such object was found.
	 */
	@RolesAllowed("Login")
	DependantMasterDataMap reloadDependants(
		GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bAll, String customUsage)
		throws CommonFinderException;

	/**
	 * gets generic object vo for a given generic object id, along with its dependants.
	 * @param iGenericObjectId id of generic object
	 * @return generic object vo with dependants
	 * @throws CommonFinderException if no such object was found.
	 * deprecated merge with getWithDependants?
	 * @nucleus.permission mayRead(module)
	 */
	@RolesAllowed("Login")
	RuleObjectContainerCVO getRuleObjectContainerCVO(Event event,
		Integer iGenericObjectId, String customUsage) throws CommonPermissionException,
		CommonFinderException;

	/**
	 * gets all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @return list of generic object value objects - without dependants and parent objects!
	 * @todo restrict permission - check module id!
	 */
	@RolesAllowed("Login")
	List<GenericObjectWithDependantsVO> getGenericObjects(
		Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds, String customUsage);

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
	List<GenericObjectWithDependantsVO> getGenericObjects(
		Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds,
		Set<String> stRequiredSubEntityNames, String customUsage, boolean bIncludeParentObjects);

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
	@RolesAllowed("Login")
	ProxyList<GenericObjectWithDependantsVO> getGenericObjectsWithDependants(
		Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds,
		Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects,
		boolean bIncludeSubModules, String customUsage);

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
	@RolesAllowed("Login")
	ProxyList<GenericObjectWithDependantsVO> getPrintableGenericObjectsWithDependants(
		Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds,
		Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects,
		boolean bIncludeSubModules, String customUsage);

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
	@RolesAllowed("Login")
	TruncatableCollection<GenericObjectWithDependantsVO> getRestrictedNumberOfGenericObjects(
		Integer iModuleId, CollectableSearchExpression clctexpr,
		Set<Integer> stRequiredAttributeIds,
		Set<String> stRequiredSubEntityNames, String customUsage,
		int iMaxRowCount);

	/**
	 * gets the ids of all generic objects that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param cond condition that the generic objects to be found must satisfy
	 * @return List<Integer> list of generic object ids
	 */
	List<Integer> getGenericObjectIds(Integer iModuleId,
		CollectableSearchCondition cond);

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
	@RolesAllowed("Login")
	GenericObjectVO create(GenericObjectWithDependantsVO gowdvo, String customUsage)
		throws CommonPermissionException, NuclosBusinessRuleException,
		CommonCreateException;

	/**
	 * updates an existing generic object in the database and returns the written object along with its dependants.
	 * @param iModuleId module id for plausibility check
	 * @param lowdcvo containing the generic object data
	 * @return same generic object as value object
	 * @precondition iModuleId != null
	 * @precondition lowdcvo.getModuleId() == iModuleId
	 * @nucleus.permission mayWrite(module)
	 */
	@RolesAllowed("Login")
	GenericObjectWithDependantsVO modify(Integer iModuleId,
		GenericObjectWithDependantsVO lowdcvo, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonPermissionException, CommonStaleVersionException,
		NuclosBusinessException, CommonValidationException;

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
	@RolesAllowed("Login")
	GenericObjectVO modify(GenericObjectVO govo,
		DependantMasterDataMap mpDependants, boolean bFireSaveEvent, String customUsage)
		throws CommonPermissionException, CommonStaleVersionException,
		NuclosBusinessException, CommonValidationException,
		CommonCreateException, CommonFinderException, CommonRemoveException;

	/**
	 * delete generic object from database
	 * @param gowdvo containing the generic object data
	 * @param bDeletePhysically remove from db?
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @nucleus.permission mayDelete(module, bDeletePhysically)
	 */
	@RolesAllowed("Login")
	void remove(GenericObjectWithDependantsVO gowdvo,
		boolean bDeletePhysically, String customUsage) throws NuclosBusinessException,
		CommonFinderException,
		CommonRemoveException, CommonPermissionException,
		CommonStaleVersionException, NuclosBusinessRuleException,
		CommonCreateException;

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
	void restore(Integer iId, String customUsage) throws CommonFinderException,
		CommonPermissionException, CommonBusinessException;

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
	void relate(Integer iModuleIdTarget,
		Integer iGenericObjectIdTarget, Integer iGenericObjectIdSource,
		String relationType) throws CommonFinderException, CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

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
	void relate(Integer iModuleIdTarget,
		Integer iGenericObjectIdTarget, Integer iGenericObjectIdSource,
		String relationType, Date dateValidFrom, Date dateValidUntil,
		String sDescription) throws CommonFinderException,
		CommonCreateException, CommonPermissionException,
		NuclosBusinessRuleException;

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
	void unrelate(Integer iModuleIdTarget,
		Integer iGenericObjectIdTarget, Integer iGenericObjectIdSource,
		String relationType) throws CommonFinderException,
		NuclosBusinessRuleException, CommonPermissionException,
		CommonRemoveException;

	/**
	 * finds relations between two given generic objects.
	 * @param iGenericObjectIdSource
	 * @param relationType
	 * @param iGenericObjectIdTarget
	 * @return the relation.
	 * @throws FinderException if no such relation exists.
	 * @postcondition result != null
	 */
	Collection<GenericObjectRelationVO> findRelations(
		Integer iGenericObjectIdSource, String relationType,
		Integer iGenericObjectIdTarget) throws CommonFinderException,
		CommonPermissionException;

	Collection<GenericObjectRelationVO> findRelationsByGenericObjectId(
		Integer iGenericObjectId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * checks if a given generic object belongs to a given module.
	 * @param iModuleId			 id of module to validate generic object for
	 * @param iGenericObjectId id of generic object to check
	 * @return true if in module or false if not
	 */
	boolean isGenericObjectInModule(Integer iModuleId,
		Integer iGenericObjectId) throws CommonFinderException;

	/**
	 * @param iGenericObjectId
	 * @return the id of the module containing the generic object with the given id.
	 * @throws CommonFinderException if there is no generic object with the given id.
	 */
	@RolesAllowed("Login")
	int getModuleContainingGenericObject(int iGenericObjectId)
		throws CommonFinderException;

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
	Set<Integer> getRelatedGenericObjectIds(
		final Integer iModuleId, final Integer iGenericObjectId,
		final RelationDirection direction, final String relationType);

	/**
	 * @param usagecriteria
	 * @return the collection of calculated attributes for leased object with the given usagecriteria
	 * @throws CommonFinderException if no such object was found.
	 * @throws CommonPermissionException if the user doesn't have the permission to view the leased object.
	 * @throws IllegalArgumentException if the given module id doesn't match the leased object's module id.
	 * @postcondition result != null
	 * @postcondition result.isComplete()
	 */
//	public abstract Collection<DynamicAttributeVO> getCalculatedAttributeValuesForGenericObject(
//		UsageCriteria usagecriteria, Integer iGenericObjectId)
//		throws CommonFinderException, CommonPermissionException;

	/**
	 * creates a LogbookEntry
	 */
	void createLogbookEntry(Integer genericObjectId,
		Integer attributeId, Integer masterdataMetaId,
		Integer masterdataMetaFieldId, Integer masterdataRecordId,
		String masterdataAction, Integer oldValueId, Integer oldValueExternalId,
		String oldValue, Integer newValueId, Integer newValueExternalId,
		String newValue);

	/**
	 * creates a GenericObjectAttribute with the given values
	 * @param genericObjectId
	 * @param attributeId
	 * @param valueId
	 * @param canonicalValue
	 * @return a collection of BadAttributeValueException
	 */
	Collection<BadAttributeValueException> createGenericObjectAttribute(
		Integer genericObjectId, Integer attributeId, Integer valueId,
		String canonicalValue, boolean logbookTracking);
	
	/**
	 * updates a GenericObjectAttribute
	 */
	void updateGenericObjectAttribute(DynamicAttributeVO vo,
		Integer genericObjectId, boolean logbookTracking)
		throws NuclosBusinessException;

	/**
	 * returns the GenericObjectAttribute matching the given genericObjectId and attributeId
	 * @param genericObjectId
	 * @param attributeId
	 * @return the GenericObjectAttribute matching the given genericObjectId and attributeId
	 */
	DynamicAttributeVO findAttributeByGoAndAttributeId(
		Integer genericObjectId, Integer attributeId)
		throws CommonFinderException;

	Collection<GenericObjectWithDependantsVO> getGenericObjectsMore(Integer iModuleId, List<Integer> lstIds,
		Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, String customUsage, boolean bIncludeParentObjects);

	List<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchExpression cse);
	
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
	@RolesAllowed("Login")
	GenericObjectVO modify(GenericObjectVO govo, DependantMasterDataMap mpDependants, boolean bFireSaveEvent, boolean bCheckPermission, String customUsage)
			throws CommonPermissionException, CommonStaleVersionException,
			NuclosBusinessException, CommonValidationException,
			CommonCreateException, CommonFinderException, CommonRemoveException;
			
	String getCurrentUserName();
}

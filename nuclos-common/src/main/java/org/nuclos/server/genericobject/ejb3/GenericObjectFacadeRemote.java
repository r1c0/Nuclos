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
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

// @Remote
public interface GenericObjectFacadeRemote {

	@RolesAllowed("Login")
	GenericObjectMetaDataVO getMetaData();

	@RolesAllowed("Login")
	Map<Integer, String> getResourceMap();

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
	GenericObjectWithDependantsVO getHistorical(
		int iGenericObjectId, Date dateHistorical, String customUsage) throws CommonFinderException,
		CommonPermissionException;

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
	 * gets the ids of all leased objects that match a given search expression (ordered, when necessary)
	 * @param iModuleId id of module to search for leased objects in
	 * @param cond condition that the leased objects to be found must satisfy
	 * @return List<Integer> list of leased object ids
	 */
	List<Integer> getGenericObjectIds(Integer iModuleId,
		CollectableSearchExpression cse);

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
	Collection<GenericObjectWithDependantsVO> getGenericObjectsMore(
		Integer iModuleId, List<Integer> lstIds,
		Set<Integer> stRequiredAttributeIds,
		Set<String> stRequiredSubEntityNames, String customUsage, boolean bIncludeParentObjects);

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
	 * 
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	GenericObjectWithDependantsVO create(
		GenericObjectWithDependantsVO gowdvo, Set<String> stRequiredSubEntityNames)
		throws CommonPermissionException, NuclosBusinessRuleException,
		CommonCreateException, CommonFinderException;
	
	@RolesAllowed("Login")
	GenericObjectWithDependantsVO create(
		GenericObjectWithDependantsVO gowdvo, Set<String> stRequiredSubEntityNames, String customUsage)
		throws CommonPermissionException, NuclosBusinessRuleException,
		CommonCreateException, CommonFinderException;

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
	GenericObjectVO create(GenericObjectWithDependantsVO gowdvo)
		throws CommonPermissionException, NuclosBusinessRuleException,
		CommonCreateException;
	
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
	 * @Deprecated use with customUsage
	 */
	@RolesAllowed("Login")
	@Deprecated
	GenericObjectWithDependantsVO modify(Integer iModuleId,
		GenericObjectWithDependantsVO lowdcvo) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonPermissionException, CommonStaleVersionException,
		NuclosBusinessException, CommonValidationException;
	
	@RolesAllowed("Login")
	GenericObjectWithDependantsVO modify(Integer iModuleId,
		GenericObjectWithDependantsVO lowdcvo, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonPermissionException, CommonStaleVersionException,
		NuclosBusinessException, CommonValidationException;

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
	void remove(GenericObjectWithDependantsVO gowdvo,
		boolean bDeletePhysically) throws NuclosBusinessException,
		CommonFinderException,
		CommonRemoveException, CommonPermissionException,
		CommonStaleVersionException, NuclosBusinessRuleException,
		CommonCreateException;
	
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
	 * method to get logbook entries for a generic object
	 *
	 * @param iGenericObjectId id of generic object to get logbook entries for
	 * @param iAttributeId		id of attribute to get logbook entries for
	 * @return collection of logbook entry value objects
	 * @precondition Modules.getInstance().isLogbookTracking(this.getModuleContainingGenericObject(iGenericObjectId))
	 * @nucleus.permission mayRead(module)
	 */
	@RolesAllowed("Login")
	Collection<LogbookVO> getLogbook(int iGenericObjectId,
		Integer iAttributeId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * adds the generic object with the given id to the group with the given id.
	 * @param iGenericObjectId generic object id to be grouped.  Must be a main module object.
	 * @param iGroupId id of group to add generic object to
	 * @param blnCheckWriteAllowedForObject ; should only be set to false when creating a new generic object, otherwise always true
	 * @throws NuclosBusinessRuleException
	 * @nucleus.permission mayWrite(module)
	 */
	@RolesAllowed("Login")
	void addToGroup(int iGenericObjectId, int iGroupId,
		boolean blnCheckWriteAllowedForObject) throws CommonCreateException,
		CommonFinderException, CommonPermissionException,
		NuclosBusinessRuleException;

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
	void removeFromGroup(Map<Integer, Integer> mpGOGroupRelation)
		throws NuclosBusinessRuleException, CommonFinderException,
		CommonPermissionException, CommonRemoveException,
		CommonStaleVersionException, CommonCreateException;

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
	void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws CommonFinderException, CommonPermissionException,
		NuclosBusinessRuleException, CommonRemoveException,
		CommonStaleVersionException, CommonCreateException;

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
	 * removes the relation with the given id.
	 * @param mpGOTreeNodeRelation
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @nucleus.permission mayWrite(targetModule)
	 */
	@RolesAllowed("Login")
	void removeRelation(
		Map<Integer, GenericObjectTreeNode> mpGOTreeNodeRelation)
		throws CommonBusinessException, CommonRemoveException,
		CommonFinderException;

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
	void removeRelation(Integer iRelationId,
		Integer iGenericObjectIdTarget, Integer iModuleIdTarget)
		throws CommonRemoveException, CommonFinderException,
		CommonBusinessException;

	/**
	 * @param iGenericObjectId
	 * @return the id of the module containing the generic object with the given id.
	 * @throws CommonFinderException if there is no generic object with the given id.
	 */
	@RolesAllowed("Login")
	int getModuleContainingGenericObject(int iGenericObjectId)
		throws CommonFinderException;

	/**
	 * @param iGenericObjectId
	 * @return the id of the state of the generic object with the given id
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	int getStateIdByGenericObject(int iGenericObjectId)
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
	 * attaches document to any object
	 * @param mdvoDocument master data value object with fields for table t_ud_go_document, has to be filled
	 * with following fields: comment, createdDate, createdUser, file and genericObjectId.
	 * @param mdvoDocument
	 * @return Id of created master data entry
	 * @todo restrict permission - check module id! requires the right to modify documents
	 */
	@RolesAllowed("Login")
	void attachDocumentToObject(MasterDataVO mdvoDocument);

	/**
	 * execute a list of rules for the given Object
	 * @param lstRuleVO
	 * @param govo
	 * @param bSaveAfterRuleExecution
	 * @throws CommonBusinessException
	 * @todo restrict permission - check module id!
	 */
	@RolesAllowed("ExecuteRulesManually")
	void executeBusinessRules(List<RuleVO> lstRuleVO,
		GenericObjectWithDependantsVO govo, boolean bSaveAfterRuleExecution, String customUsage)
		throws CommonBusinessException;

}

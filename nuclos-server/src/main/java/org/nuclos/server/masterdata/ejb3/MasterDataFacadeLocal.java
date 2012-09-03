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
package org.nuclos.server.masterdata.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;

// @Local
public interface MasterDataFacadeLocal {

	/**
	 * method to get meta information for a master data entity
	 * @param sEntityName name of entity to get meta data for
	 * @return master data meta value object
	 * @postcondition result != null
	 * @throws NuclosFatalException if there is not metadata for the given entity.
	 */
	MasterDataMetaVO getMetaData(String sEntityName);

	/**
	 * method to get meta information for a master data entity
	 * @param iEntityId id of entity to get meta data for
	 * @return master data meta value object
	 * @postcondition result != null
	 * @throws ElisaFatalException if there is not metadata for the given entity.
	 */
	MasterDataMetaVO getMetaData(Integer iEntityId);

	/**
	 * @param iModuleId the id of the module whose subentities we are looking for
	 * @return Collection<MasterdataMetaCVO> the masterdata meta information for all entities having foreign keys to the given module.
	 */
	Collection<MasterDataMetaVO> getMetaDataByModuleId(
		Integer iModuleId);

	/**
	 * @param sEntityName
	 * @param clctexpr
	 * @return a proxy list containing the search result for the given search expression.
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	ProxyList<MasterDataWithDependantsVO> getMasterDataProxyList(
		String sEntityName, CollectableSearchExpression clctexpr);

	/**
	 * method to get master data records for a given entity and search condition
	 * @param sEntityName name of the entity to get master data records for
	 * @param cond search condition
	 * @return TruncatableCollection<MasterDataVO> collection of master data value objects
	 * @postcondition result != null
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	TruncatableCollection<MasterDataVO> getMasterData(
		String sEntityName, CollectableSearchCondition cond, boolean bAll);

	/**
	 * gets the ids of all masterdata objects that match a given search expression (ordered, when necessary)
	 * @param cond condition that the masterdata objects to be found must satisfy
	 * @return List<Integer> list of masterdata ids
	 */
	@RolesAllowed("Login")
	List<Object> getMasterDataIds(String sEntityName,
		CollectableSearchExpression cse);

	/**
	 * gets the ids of all masterdata objects
	 * @return List<Integer> list of masterdata ids
	 */
	@RolesAllowed("Login")
	List<Object> getMasterDataIds(String sEntityName);

	/**
	 * gets the dependant master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param sEntityName name of the entity to get all dependant master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	Collection<MasterDataVO> getDependantMasterData(
		String sEntityName, String sForeignKeyField, Object oRelatedId);

	@RolesAllowed("Login")
	Collection<EntityTreeViewVO> getDependantSubnodes(
		String sEntityName, String sForeignKeyField, Object oRelatedId);

	/**
	 * method to get a master data value object for given primary key id
	 * @param sEntityName name of the entity to get record for
	 * @param oId primary key id of master data record
	 * @return master data value object
	 * @throws CommonPermissionException
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	MasterDataVO get(String sEntityName, Object oId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * @param sEntityName
	 * @param oId
	 * @return the version of the given masterdata id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	Integer getVersion(String sEntityName, Object oId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * create a new master data record
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) --> mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	MasterDataVO create(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants, String customUsage) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * modifies an existing master data record.
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	Object modify(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	void remove(String sEntityName, MasterDataVO mdvo,
		boolean bRemoveDependants, String customUsage) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * read all dependant masterdata recursively
	 * if necessary, mark the read data as removed
	 * @param sEntityName
	 * @param mdvo
	 */
	@RolesAllowed("Login")
	DependantMasterDataMap readAllDependants(String sEntityName,
		Integer iId, DependantMasterDataMap mpDependants, Boolean bRemoved,
		String sParentEntity,
		Map<EntityAndFieldName, String> mpEntityAndParentEntityName);

	/**
	 * modifies the given dependants (local use only).
	 * @param dependants
	 * @precondition mpDependants != null
	 */
	void modifyDependants(String entityName, Integer id,
		Boolean removed, DependantMasterDataMap dependants, String customUsage)
		throws CommonCreateException, CommonFinderException, CommonPermissionException,
		CommonRemoveException, CommonStaleVersionException;

	/**
	 * notifies clients that the contents of an entity has changed.
	 * @param sCachedEntityName name of the cached entity.
	 * @precondition sCachedEntityName != null
	 */
	void notifyClients(String sCachedEntityName);

	/**
	 * Write changes to dependant masterdata into logbook table if specified for the field.
	 * @param iGenericObjectId id of the leased object
	 * @param mpDependants the dependant map of the leased object
	 * @param stExcluded a set of record ids to exclude from writing; i.e. which have been written already
	 * @param bOnlyNewEntries notifies this method that only newly created records are to be processed, which have no old value
	 * @throws CommonPermissionException
	 * @todo restrict permissions by entity name / module id
	 */
	@RolesAllowed("Login")
	void protocolDependantChanges(Integer iGenericObjectId,
		DependantMasterDataMap mpDependants, Set<Integer> stExcluded,
		boolean bOnlyNewEntries) throws CommonPermissionException;

	/**
	 * gets master data vo for a given master data id, along with its dependants.
	 * @param iObjectId id of master data
	 * @return master data vo with dependants
	 * @throws CommonFinderException if no such object was found.
	 * @throws NuclosBusinessException
	 */
	RuleObjectContainerCVO getRuleObjectContainerCVO(Event event,
		String sEntityName, Integer iObjectId, String customUsage) throws CommonPermissionException,
		CommonFinderException, NuclosBusinessException;

	/**
	 * @param sEntityName
	 * @param iId the object's id (primary key)
	 * @return the masterdata object with the given entity and id.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	MasterDataWithDependantsVO getWithDependants(
		String sEntityName, Integer iId, String customUsage) throws CommonFinderException,
		NuclosBusinessException, CommonPermissionException;

	/**
	 * @param sEntityName
	 * @param cond search condition
	 * @return the masterdata objects for the given entityname and search condition.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	Collection<MasterDataWithDependantsVO> getWithDependantsByCondition(
		String sEntityName, CollectableSearchCondition cond, String customUsage) ;

	/**
	 * @param sEntityName
	 * @param lstIntIds
	 * @param lstRequiredSubEntities
	 * @return the next chunk of the search result for a proxy list.
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	List<MasterDataWithDependantsVO> getMasterDataMore(
		String sEntityName, final List<?> lstIntIds,
		final List<EntityAndFieldName> lstRequiredSubEntities);

	/**
	 * @param user - the user for which to get subordinated users
	 * @return List<MasterDataVO> list of masterdata valueobjects
	 */
	List<MasterDataVO> getUserHierarchy(String user);
}

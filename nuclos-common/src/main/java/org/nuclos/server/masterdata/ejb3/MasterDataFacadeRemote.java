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

import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.EntityAndFieldName;
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
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


// @Remote
public interface MasterDataFacadeRemote {

	/**
	 * @return the masterdata meta information for the all entities.
	 */
	@RolesAllowed("Login")
	Collection<MasterDataMetaVO> getAllMetaData();

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
	 * convinience function to get all reports or forms used in AllReportsCollectableFieldsProvider.
	 * @param sEntityName name of the entity to get master data records for ("report" or "form")
	 * @return TruncatableCollection<MasterDataVO> collection of master data value objects
	 * @throws CommonFinderException if a row was deleted in the time between executing the search and fetching the single rows.
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	TruncatableCollection<MasterDataVO> getAllReports() throws CommonFinderException,
		CommonPermissionException;

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
		String sEntityName, String sForeignKeyField, Object oRelatedId, Map<String, Object> mpParams);

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
	 * @param sEntityName
	 * @return Does the entity with the given name use the rule engine?
	 */
	@RolesAllowed("Login")
	boolean getUsesRuleEngine(String sEntityName);

	/**
	 * execute a list of rules for the given Object
	 * @param lstRuleVO
	 * @param mdvo
	 * @param bSaveAfterRuleExecution
	 * @throws CommonBusinessException
	 * @todo restrict permission - check module id!
	 */
	@RolesAllowed("ExecuteRulesManually")
	void executeBusinessRules(String sEntityName,
		List<RuleVO> lstRuleVO, MasterDataWithDependantsVO mdvo,
		boolean bSaveAfterRuleExecution, String customUsage) throws CommonBusinessException;

	/**
	 * Get all subform entities of a masterdata entity
	 * @param entityName
	 */
	@RolesAllowed("Login")
	Set<EntityAndFieldName> getSubFormEntitiesByMasterDataEntity(
		String entityName, String customUsage);

	/**
	 * revalidates the cache. This may be used for development purposes only, in order to rebuild the cache
	 * after metadata entries in the database were changed.
	 */
	@RolesAllowed("UseManagementConsole")
	void revalidateMasterDataMetaCache();

	/**
	 * value list provider function (get processes by usage)
	 * @param iModuleId module id of usage criteria
	 * @param bSearchMode when true, validity dates and/or active sign will not be considered in the search.
	 * @return collection of master data value objects
	 */
	@RolesAllowed("Login")
	List<CollectableField> getProcessByUsage(Integer iModuleId, boolean bSearchMode);

	/**
	 * @param iModuleId the id of the module whose subentities we are looking for
	 * @return Collection<MasterDataMetaVO> the masterdata meta information for all entities having foreign keys to the given module.
	 */
	List<CollectableField> getSubEntities(Integer iModuleId);

	Map<String, String> getRuleEventsWithLocaleResource();

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
	 * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	 * The transaction type is "not supported" here in order to avoid a transaction timeout, as the whole operation may
	 * take some time.
	 * @param sOutputFileName the name of the csv file to which the results are written.
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED, noRollbackFor= {Exception.class})
	@RolesAllowed("UseManagementConsole")
	void checkMasterDataValues(String sOutputFileName);

	/**
	 * gets the file content of a generic object document
	 *
	 * @param iGenericObjectDocumentId generic object document id
	 * @return generic object document file content
	 * @todo restrict permission - check module id!
	 */
	@RolesAllowed("Login")
	byte[] loadContent(Integer iGenericObjectDocumentId,
		String sFileName, String sPath) throws CommonFinderException;

	/**
	 * @param user - the user for which to get subordinated users
	 * @return List<MasterDataVO> list of masterdata valueobjects
	 */
	List<MasterDataVO> getUserHierarchy(String user);
}

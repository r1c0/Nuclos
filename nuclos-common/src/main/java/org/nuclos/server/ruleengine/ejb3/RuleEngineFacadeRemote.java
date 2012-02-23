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
package org.nuclos.server.ruleengine.ejb3;

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.ruleengine.valueobject.RuleWithUsagesVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

// @Remote
public interface RuleEngineFacadeRemote {

	/**
	 * @return Collection<RuleVO> all rule definitions
	 * @throws CommonPermissionException
	 */
	Collection<RuleVO> getAllRules()
		throws CommonPermissionException;

	/**
	 * Get a collection of rules by the eventname independent of the module.
	 * @return Collection<RuleVO> all rule for a given event Name
	 * @throws CommonPermissionException
	 */
	List<RuleVO> getByEventOrdered(String sEventName)
		throws CommonPermissionException;

	/**
	 * Get a collection of rules by Eventname and ModuleId (ordered).
	 * @return Collection<RuleVO> all rule for a given event Name
	 * @throws CommonPermissionException
	 */
	List<RuleVO> getByEventAndEntityOrdered(String sEventName,
		String sEntity) throws CommonPermissionException;

	/**
	 * Create an ruleUsage for the given module and eventname.
	 * The oder of the new usage is dependent of the ruleBeforeId
	 *
	 * @param sEventname
	 * @param iModuleId
	 * @param ruleToInsertId
	 * @param ruleBeforeId - null the new usage is inserted at the end
	 * 					- not null the new usage is inserted after the usage with the ruleBeforeId
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	void createRuleUsageInEntity(String sEventname,
		String sEntity, Integer ruleToInsertId, Integer ruleBeforeId)
		throws CommonCreateException, CommonPermissionException;

	/**
	 * remove an rule usage for the rule with the given id in the given module and the eventName
	 * @param eventName
	 * @param moduleId
	 * @param iRuleIdToRemove id of rule to remove
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 */
	void removeRuleUsage(String eventName, String entity,
		Integer iRuleIdToRemove) 
				throws CommonPermissionException, NuclosBusinessRuleException, CommonFinderException, CommonRemoveException, CommonStaleVersionException;

	/**
	 *
	 * @param eventName
	 * @param moduleId
	 * @param ruleToMoveId
	 * @param ruleBeforeId
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	void moveRuleUsageInEntity(String eventName,
		String entity, Integer ruleToMoveId, Integer ruleBeforeId)
		throws CommonCreateException, CommonPermissionException;

	/**
	 * Get all object generations.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	Collection<GeneratorActionVO> getAllGenerations()
		throws CommonPermissionException;

	/**
	 * Get all object generations that have a rule assigned.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	Collection<GeneratorActionVO> getAllGenerationsWithRule()
		throws CommonPermissionException;

	/**
	 * Get all object generations that have a certain rule assigned.
	 * @return Collection<GeneratorActionVO>
	 * @throws CommonPermissionException
	 */
	Collection<GeneratorActionVO> getAllGenerationsForRuleId(
		Integer iRuleId) throws CommonPermissionException;

	/**
	 * Get all RuleGeneration for the given rule.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	Collection<RuleEngineGenerationVO> getAllRuleGenerationsForRuleId(
		Integer ruleId) throws CommonPermissionException;

	/**
	 * Get all RuleGeneration for the given generation.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	Collection<RuleEngineGenerationVO> getAllRuleGenerationsForGenerationId(
		Integer generationId) throws CommonPermissionException;

	/**
	 * Get all RuleTransition that have the given rule assigned.
	 * @return Collection<RuleEngineTransitionVO>
	 * @throws CommonPermissionException
	 */
	Collection<RuleEngineTransitionVO> getAllRuleTransitionsForRuleId(
		Integer ruleId) throws CommonPermissionException;

	/**
	 * Get all Rule Transition that have the given transition assigned.
	 * @return Collection<RuleEngineTransitionVO>
	 * @throws CommonPermissionException
	 */
	Collection<RuleEngineTransitionVO> getAllRuleTransitionsForTransitionId(
		Integer transitionId) throws CommonPermissionException;

	/**
	 * Get all state Models that have a given rule assigned.
	 * @return collection of generation actions
	 * @throws CommonPermissionException
	 */
	Collection<StateModelVO> getAllStateModelsForRuleId(
		Integer aRuleId) throws CommonPermissionException;

	/**
	 * Get all Rule engine Generations.
	 * @return Collection<RuleEngineGenerationVO>
	 * @throws CommonPermissionException
	 */
	Collection<RuleEngineGenerationVO> getAllRuleEngineGenerations()
		throws CommonPermissionException;

	/**
	 * Get all rule usages of a rule for a certain event.
	 * @return collection of state model vo
	 * @throws CommonPermissionException
	 */
	Collection<RuleEventUsageVO> getByEventAndRule(
		String sEventName, Integer iRuleId) throws CommonPermissionException;

	/**
	 * Get all referenced entity names for a certain rule event.
	 * @return collection of entity names
	 * @throws CommonPermissionException
	 */
	Collection<String> getRuleUsageEntityNamesByEvent(String sEventName) throws CommonPermissionException;

	/**
	 * gets a rule definition from the database by primary key.
	 * @param iId primary key of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	RuleVO get(Integer iId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * gets a rule definition from the database by name.
     * NUCLOSINT-743
	 * @param ruleName Name of rule definition
	 * @return rule value object
	 * @throws CommonPermissionException
	 */
	RuleVO get(String ruleName) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * create a new rule definition in the database
	 * @param rulevo containing the rule
	 * @param mpDependants
	 * @return same layout as value object
	 * @throws CommonPermissionException
	 * @precondition (mpDependants != null) -> mpDependants.dependantsAreNew()
	 */
	RuleVO create(RuleVO rulevo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonValidationException, CommonStaleVersionException,
		NuclosCompileException, CommonPermissionException;

	/**
	 * modify an existing rule definition in the database
	 * @param rulevo containing the rule
	 * @param mpDependants May be null.
	 * @return new rule as value object
	 * @throws NuclosCompileException
	 * @throws CommonPermissionException
	 */
	RuleVO modify(RuleVO rulevo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		NuclosCompileException, CommonPermissionException;

	/**
	 * delete rule definition from database
	 * @param rulevo containing the rule
	 * @throws CommonPermissionException
	 * @throws NuclosBusinessRuleException
	 */
	void remove(RuleVO rulevo) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException;

	/**
	 * imports the given rules, adding new and overwriting existing rules. The other existing rules are untouched.
	 * Currently, only the rule code is imported, not the usages. If one rule cannot be imported, the import will be aborted.
	 * @param collRuleWithUsages
	 */
	@RolesAllowed("UseManagementConsole")
	void importRules(
		Collection<RuleWithUsagesVO> collRuleWithUsages)
		throws CommonBusinessException;

	/**
	 * Check if compilation would be successful.
	 * @param ruleVO
	 * @throws NuclosCompileException
	 */
	@RolesAllowed("Login")
	void check(RuleVO ruleVO) throws NuclosCompileException;

	/**
	 * Returns a template for new rules to display in the rule editor.
	 * @return String containing class template
	 */
	String getClassTemplate();

	/**
	 * Delete the Output Path Directory.
	 */
	@RolesAllowed("UseManagementConsole")
	void deleteDirectoryOutputPath();

	/**
	 * invalidates the rule cache
	 */
	@RolesAllowed("Login")
	void invalidateCache();
	
   /**
    * finds rules by usage criteria
    * @param usagecriteria
    * @param sEventName
    * @return collection of rule usages
    */
	@RolesAllowed("Login")
   public Collection<RuleVO> findRulesByUsageAndEvent(String sEventName, UsageCriteria usagecriteria);


}

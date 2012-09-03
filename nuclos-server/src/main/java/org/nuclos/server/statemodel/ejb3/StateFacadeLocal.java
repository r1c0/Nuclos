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
package org.nuclos.server.statemodel.ejb3;

import java.util.Collection;
import java.util.Set;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.valueobject.MandatoryColumnVO;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

// @Local
public interface StateFacadeLocal {

	/**
	 * @param usagecriteria
	 * @return the initial state of the statemodel corresponding to <code>usagecriteria</code>.
	 */
	StateVO getInitialState(UsageCriteria usagecriteria);

	/**
	 * method to return the initial state for a given generic object
	 * @param iGenericObjectId id of leased object to get initial state for
	 * @return state id of initial state for given generic object
	 */
	StateVO getInitialState(Integer iGenericObjectId);

	/**
	 * @param iStateModelId
	 * @return the id of the statemodel corresponding to <code>usagecriteria</code>.
	 */
	Collection<StateVO> getStatesByModel(Integer iStateModelId);

	/**
	 * method to get all state models
	 * @return collection of state model vo
	 */
	Collection<StateModelVO> getStateModels();

	/**
	 * Retrieve all states in all state models for the module with the given id
	 * @param iModuleId id of module to retrieve states for
	 * @return Collection of all states for the given module
	 */
	Collection<StateVO> getStatesByModule(Integer iModuleId);

	/**
	 * method to return the actual state for a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId id of leased object to get actual state for
	 * @return state id of actual state for given leased object
	 * @nucleus.permission mayRead(module)
	 */
	StateVO getCurrentState(Integer iModuleId,
		Integer iGenericObjectId) throws CommonFinderException;

	/**
	 * method to return the possible subsequent states for a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId id of leased object to get subsequent states for
	 * @param bGetAutomaticStatesAlso should method also return states for automatic only transitions? false for returning subsequent states to client, which generates buttons for manual state changes
	 * @return set of possible subsequent states for given leased object
	 * @nucleus.permission mayRead(module)
	 */
	Collection<StateVO> getSubsequentStates(Integer iModuleId,
		Integer iGenericObjectId, boolean bGetAutomaticStatesAlso)
		throws NuclosNoAdequateStatemodelException, CommonFinderException;

	/**
	 * method to change the status of a given leased object (called by server only)
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iNumeral legal subsequent status numeral to set for given leased object
	 */
	void changeStateByRule(Integer iModuleId,
		Integer iGenericObjectId, int iNumeral, String customUsage)
		throws NuclosNoAdequateStatemodelException,
		NuclosSubsequentStateNotLegalException, NuclosBusinessException,
		CommonFinderException, CommonPermissionException,
		CommonCreateException;

	/**
	 * method to change the status of a given leased object (called by server only)
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 */
	void changeStateByRule(Integer iModuleId,
		Integer iGenericObjectId, Integer iTargetStateId, String customUsage)
		throws NuclosNoAdequateStatemodelException,
		NuclosSubsequentStateNotLegalException, NuclosBusinessException,
		CommonFinderException, CommonPermissionException,
		CommonCreateException;

	/**
	 * method to change the status of a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 * @nucleus.permission mayWrite(module)
	 */
	void changeStateByUser(Integer iModuleId,
		Integer iGenericObjectId, Integer iTargetStateId, String customUsage)
		throws NuclosBusinessException, CommonPermissionException,
		CommonPermissionException, CommonCreateException,
		NuclosNoAdequateStatemodelException,
		NuclosSubsequentStateNotLegalException, CommonFinderException;

	/**
	 * returns a StateTransitionVO for the given transitionId
	 */
	StateTransitionVO findStateTransitionById(
		Integer transitionId);

	/**
	 * returns a StateTransitionVO for the given sourceStateId
	 */
	Collection<StateTransitionVO> findStateTransitionBySourceState(
		Integer sourceStateId);

	/**
	 * returns a StateTransitionVO for the given sourceStateId without automatic
	 */
	Collection<StateTransitionVO> findStateTransitionBySourceStateNonAutomatic(
		Integer sourceStateId);

	/**
	 * returns a StateTransitionVO for the given targetStateId without a sourceStateId
	 */
	StateTransitionVO findStateTransitionByNullAndTargetState(
		Integer targetStateId);

	/**
	 * returns a StateTransitionVO for the given sourceStateId and targetStateId
	 */
	StateTransitionVO findStateTransitionBySourceAndTargetState(
		Integer sourceStateId, Integer targetStateId);

	/**
	 * returns the StateModelVO for the given Id
	 */
	StateModelVO findStateModelById(Integer id)
		throws CommonPermissionException, CommonFinderException;

	/**
	 * returns the StateModelVO for the given statemodel-name
	 */
	StateModelVO findStateModelByName(String name)
		throws CommonPermissionException, CommonFinderException;

	/**
	 * returns a Collection of StateModelVO which contains rule-transitions with the given ruleId
	 */
	Collection<StateModelVO> findStateModelsByRuleId(
		Integer ruleId) throws CommonPermissionException;

	/**
	 * returns a Collection of StateHistories for the given genericObjectId
	 */
	Collection<StateHistoryVO> findStateHistoryByGenericObjectId(
		Integer genericObjectId);

	/**
	 * 
	 * @param stateId
	 * @return
	 */
	Set<MandatoryFieldVO> findMandatoryFieldsByStateId(Integer stateId);
	
	/**
	 * 
	 * @param stateId
	 * @return
	 */
	Set<MandatoryColumnVO> findMandatoryColumnsByStateId(Integer stateId);
}

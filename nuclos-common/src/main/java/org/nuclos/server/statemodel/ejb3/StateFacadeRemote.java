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

import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common.statemodel.StatemodelClosure;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

// @Remote
public interface StateFacadeRemote {

	/**
	 * gets a complete state graph for a state model
	 * @param iModelId id of state model to get graph for
	 * @return state graph cvo containing the state graph information for the model with the given id
	 * @throws CommonPermissionException
	 */
	StateGraphVO getStateGraph(Integer iModelId)
		throws CommonFinderException, CommonPermissionException,
		NuclosBusinessException;

	/**
	 * method to insert, update or remove a complete state model in the database at once
	 * @param stategraphcvo state graph representation
	 * @return state model id
	 * @throws CommonPermissionException
	 */
	Integer setStateGraph(StateGraphVO stategraphcvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonValidationException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * method to remove a complete state model with all usages in the database at once
	 * @param statemodelvo state model value object
	 * @throws CommonPermissionException
	 */
	void removeStateGraph(StateModelVO statemodelvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonPermissionException,
		NuclosBusinessRuleException, NuclosBusinessException;

	/**
	 * @param usagecriteria
	 * @return the id of the initial state of the statemodel corresponding to <code>usagecriteria</code>.
	 */
	Integer getInitialStateId(UsageCriteria usagecriteria);

	/**
	 * @param usagecriteria
	 * @return the id of the statemodel corresponding to <code>usagecriteria</code>.
	 */
	Integer getStateModelId(UsageCriteria usagecriteria);

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
	 * method to return the sorted list of state history entries for a given leased object
	 * @param iModuleId id of module for plausibility check
	 * @param iGenericObjectId id of leased object
	 * @return set of state history entries
	 * @nucleus.permission mayRead(module)
	 */
	Collection<StateHistoryVO> getStateHistory(
		Integer iModuleId, Integer iGenericObjectId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * Retrieve all states in all state models for the module with the given id
	 * @param iModuleId id of module to retrieve states for
	 * @return Collection of all states for the given module
	 */
	Collection<StateVO> getStatesByModule(Integer iModuleId);

	/**
	 * method to return the possible subsequent states for a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId id of leased object to get subsequent states for
	 * @param bGetAutomaticStatesAlso should method also return states for automatic only 
	 * 	transitions? false for returning subsequent states to client, which generates buttons 
	 * 	for manual state changes
	 * @return set of possible subsequent states for given leased object
	 * @nucleus.permission mayRead(module)
	 */
	Collection<StateVO> getSubsequentStates(Integer iModuleId,
		Integer iGenericObjectId, boolean bGetAutomaticStatesAlso)
		throws NuclosNoAdequateStatemodelException, CommonFinderException;

	/**
	 * method to change the status of a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 * @nucleus.permission mayWrite(module)
	 */
	void changeStateByUser(Integer iModuleId,
		Integer iGenericObjectId, Integer iTargetStateId)
		throws NuclosBusinessException, CommonPermissionException,
		CommonPermissionException, CommonCreateException,
		NuclosNoAdequateStatemodelException,
		NuclosSubsequentStateNotLegalException, CommonFinderException;
	
	/**
	 * method to modify and change state of a given object
	 * @param iModuleId module id for plausibility check
	 * @param govo object to change status for
	 * @param iTargetStateId legal subsequent status id to set for given object
	 * @nucleus.permission mayWrite(module)
	 */
	void changeStateAndModifyByUser(Integer iModuleId, 
		GenericObjectWithDependantsVO gowdvo, Integer iTargetStateId)
		throws NuclosBusinessException, CommonPermissionException,
		CommonPermissionException, CommonCreateException, NuclosNoAdequateStatemodelException,
		NuclosSubsequentStateNotLegalException, CommonFinderException, CommonRemoveException, 
		CommonStaleVersionException, CommonValidationException, CommonFatalException;

	void invalidateCache();
	
	String getResourceSIdForName(Integer iStateId);
	
	String getResourceSIdForDescription(Integer iStateId);

	Statemodel getStatemodel(UsageCriteria usageCriteria);

	StatemodelClosure getStatemodelClosureForModule(Integer moduleId);
}

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
package org.nuclos.server.statemodel.valueobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowSorter.SortKey;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * Value object representing a complete state model with all its states and transitions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class StateGraphVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final StateModelVO statemodel;

	private Set<StateVO> mpStates = new HashSet<StateVO>();

	private Set<StateTransitionVO> mpTransitions = new HashSet<StateTransitionVO>();

	public StateGraphVO() {
		this(null);
	}

	public StateGraphVO(StateModelVO statemodelvo) {
		this.statemodel = statemodelvo;
	}

	/**
	 * get state model of graph
	 * @return state model of graph
	 */
	public StateModelVO getStateModel() {
		return this.statemodel;
	}

	/**
	 * get states of graph
	 * @return states of graph
	 */
	public Set<StateVO> getStates() {
		return this.mpStates;
	}

	/**
	 * set states of graph
	 * @param states states of graph
	 */
	public void setStates(Set<StateVO> states) {
		this.mpStates = states;
	}

	/**
	 * get transitions of graph
	 * @return transitions of graph
	 */
	public Set<StateTransitionVO> getTransitions() {
		return this.mpTransitions;
	}

	/**
	 * set transitions of graph
	 * @param transitions transitions of graph
	 */
	public void setTransitions(Set<StateTransitionVO> transitions) {
		this.mpTransitions = transitions;
	}

	/**
	 * validites this object.
	 */
	public void validate() throws CommonValidationException {
		/** @todo shouldn't it be: StateGraphVO.isRemoved()? */
		if (!this.getStateModel().isRemoved()) {
			if (StringUtils.isNullOrEmpty(this.getStateModel().getName())) {
				throw new CommonValidationException("statemachine.error.validation.graph.modelname");
			}
			if (StringUtils.isNullOrEmpty(this.getStateModel().getDescription())) {
				throw new CommonValidationException("statemachine.error.validation.graph.modeldescription");
			}

			/** @todo validate StateModelLayout! */

			this.validateTransitions(this.validateStates());
		}
	}

	/**
	 * validates the states and builds a map for checkTransitions.
	 * @return Map<Integer iClientStateId, String sStateName>
	 * @throws CommonValidationException
	 */
	private Map<Integer, String> validateStates() throws CommonValidationException {
		final Map<Integer, String> result = CollectionUtils.newHashMap();
		final Map<Integer, String> numerals = CollectionUtils.newHashMap();
		for (StateVO statevo : this.getStates()) {
			if (!statevo.isRemoved()) {		//ignore removed states
				if (StringUtils.isNullOrEmpty(statevo.getStatename())) {
					throw new CommonValidationException("statemachine.error.validation.graph.statename");
				}
				if (StringUtils.isNullOrEmpty(statevo.getDescription())) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statedescription",statevo.getStatename()));
				}
				if(statevo.getNumeral() == null) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statenumeral",statevo.getStatename()));
				}
				/** numeral is set as maximum 3,0 in the database, everything higher than 999 will cause a exception */
				if(statevo.getNumeral().intValue() > 999) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statenumeral.toolong",statevo.getStatename()));
				}

				/** @todo ? */
				/*if ((statevo.getButtonlabel() == null) || (statevo.getButtonlabel().equals(""))) {
					 for (Iterator j = this.getTransitionIds().iterator(); j.hasNext();) {
						 StateTransitionVO voStateTransition = (StateTransitionVO) j.next();
						 if ((voStateTransition.getStateTarget() == statevo.getClientId()) && (!voStateTransition.isAutomatic())) {
							 throw new CommonValidationException(
									 NuclosServerResources.getString("statemachine.error.validation.graph.statelabel"));
						 }
					 }
				 }*/

				if (result.containsKey(statevo.getClientId())) {
					throw new CommonValidationException("statemachine.error.validation.graph.duplicateid");
				}
				if (result.containsValue(statevo.getStatename())) {
					throw new CommonValidationException("statemachine.error.validation.graph.duplicatestate");
				}
				if (numerals.containsKey(statevo.getNumeral())) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.duplicatenumerals",
						 numerals.get(statevo.getNumeral()),  statevo.getStatename()));
				}
				result.put(statevo.getClientId(), statevo.getStatename());
				numerals.put(statevo.getNumeral(), statevo.getStatename());
			}
		}
		return result;
	}

	/**
	 * validates the transitions.
	 * @param mpStates Map<Integer iClientStateId, String sStateName>
	 * @throws CommonValidationException
	 */
	private void validateTransitions(Map<Integer, String> mpStates) throws CommonValidationException {
		int iStartTransitionCount = 0;
		for (StateTransitionVO statetransitionvo : this.getTransitions()) {
			// ignore deleted transitions
			if (!statetransitionvo.isRemoved()) {
				if (statetransitionvo.getStateSource() == null) {
					// we have a start transition here:
					++iStartTransitionCount;
					if (!statetransitionvo.isAutomatic()) {
						statetransitionvo.setAutomatic(true);
						//throw new CommonValidationException("statemachine.error.validation.graph.invalidstarttransition");
					}
				}
				else {
					if (!mpStates.containsKey(statetransitionvo.getStateSource())) {
						throw new CommonValidationException("statemachine.error.validation.graph.invalidstart");
					}
				}
				if (!mpStates.containsKey(statetransitionvo.getStateTarget())) {
					throw new CommonValidationException("statemachine.error.validation.graph.invalidend");
				}
				if (statetransitionvo.getStateTarget().equals(statetransitionvo.getStateSource())) {
					throw new CommonValidationException("statemachine.error.validation.graph.startequalsend");
				}

				this.checkDuplicateTransition(statetransitionvo);
				this.checkDuplicateDefaultTransition(statetransitionvo);
			}
		}

		if (iStartTransitionCount == 0) {
			throw new CommonValidationException("statemachine.error.validation.graph.nostartstate");
		}
		if (iStartTransitionCount > 1) {
			throw new CommonValidationException("statemachine.error.validation.graph.toomanystartstates");
		}
		
		this.validateDefaultTransitions();
	}

	/**
	 * @param statetransitionvo
	 * @throws CommonValidationException if the given transition is duplicated.
	 */
	private void checkDuplicateTransition(StateTransitionVO statetransitionvo) throws CommonValidationException {
		List<StateTransitionVO> duplicateTransitions = getDuplicateTransitions(statetransitionvo);
		if(duplicateTransitions != null && !duplicateTransitions.isEmpty()){
			throw new CommonValidationException("statemachine.error.validation.graph.duplicatetransition");
		}
	}

	private List<StateTransitionVO> getDuplicateTransitions(StateTransitionVO statetransitionvo) {
		List<StateTransitionVO> duplicateTransitions = new ArrayList<StateTransitionVO>();
		for (StateTransitionVO statetransitionvo2 : this.getTransitions()) {
			if (!statetransitionvo2.isRemoved()) {
				if (!statetransitionvo.getClientId().equals(statetransitionvo2.getClientId())) {
					if ((statetransitionvo.getStateSource() != null) && (statetransitionvo.getStateSource().equals(statetransitionvo2.getStateSource())) && (statetransitionvo.getStateTarget().equals(statetransitionvo2.getStateTarget())))
					{
						duplicateTransitions.add(statetransitionvo2);
					}
				}
			}
		}
		return duplicateTransitions;
	}

	/**
	 * @param statetransitionvo
	 * @throws CommonValidationException if the given transition is duplicated.
	 */
	private void validateDefaultTransitions() throws CommonValidationException {
		// validate if there is an valid path from an start to an end state.
		List<StateTransitionVO> transitionVOs = new LinkedList<StateTransitionVO>(getTransitions());
		if (CollectionUtils.indexOfFirst(transitionVOs, new Predicate<StateTransitionVO>() {
			@Override public boolean evaluate(StateTransitionVO t) { return !t.isRemoved() && t.isDefault(); }
		}) != -1) {
			// find start transition - there has to be one because of the checks before.
			StateTransitionVO startTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
				@Override public boolean evaluate(StateTransitionVO t) { return !t.isRemoved() && t.getStateSource() == null && t.isAutomatic() == true; }
			});
			
			List<Integer> checkedStateNumerals = new LinkedList<Integer>();
			
			// finde alle trans die als source den end der letzten haben.
			Integer iSubsequentState = startTransition.getStateTarget();
			while (iSubsequentState != null) {
				if (checkedStateNumerals.contains(iSubsequentState)) {
					throw new CommonValidationException("statemachine.error.validation.graph.defaulttransition");					
				}
				
				final Integer iSubsequentStateSource = iSubsequentState;
				StateTransitionVO subsequentTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
					@Override public boolean evaluate(StateTransitionVO t) { return !t.isRemoved() && t.getStateSource() == iSubsequentStateSource && t.isDefault() == true; }
				});
				
				if (subsequentTransition == null) {
					StateTransitionVO subsequentStateTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
						@Override public boolean evaluate(StateTransitionVO t) { return !t.isRemoved() && t.getStateSource() == iSubsequentStateSource && t.isDefault() == false; }
					});
					if (subsequentStateTransition != null) {
						throw new CommonValidationException("statemachine.error.validation.graph.defaulttransition");
					}
					break;
				}
				
				// iterate next.
				checkedStateNumerals.add(iSubsequentState);
				iSubsequentState = subsequentTransition.getStateTarget();
			}
		}
	}
	
	/**
	 * @param statetransitionvo
	 * @throws CommonValidationException if the given transition is duplicated.
	 */
	private void checkDuplicateDefaultTransition(StateTransitionVO statetransitionvo) throws CommonValidationException {
		List<StateTransitionVO> duplicateTransitions = getDuplicateDefaultTransitions(statetransitionvo);
		if(duplicateTransitions != null && !duplicateTransitions.isEmpty()){
			throw new CommonValidationException("statemachine.error.validation.graph.duplicatedefaulttransition");
		}
	}

	private List<StateTransitionVO> getDuplicateDefaultTransitions(StateTransitionVO statetransitionvo) {
		List<StateTransitionVO> duplicateTransitions = new ArrayList<StateTransitionVO>();
		for (StateTransitionVO statetransitionvo2 : this.getTransitions()) {
			if (!statetransitionvo2.isRemoved()) {
				if (!statetransitionvo.getClientId().equals(statetransitionvo2.getClientId())) {
					if ((statetransitionvo.getStateSource() != null) && (statetransitionvo.getStateSource().equals(statetransitionvo2.getStateSource())) && (statetransitionvo.isDefault() && statetransitionvo2.isDefault()))
					{
						duplicateTransitions.add(statetransitionvo2);
					}
				}
			}
		}
		return duplicateTransitions;
	}

}	// class StateGraphVO

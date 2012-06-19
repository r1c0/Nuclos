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
package org.nuclos.common.statemodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.TransformerUtils;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Simple transfer class representing a state model for a module and process
 * (thus: usage criteria), bound to the current user.
 * 
 * This includes all information needed for one state-based process, i.e. the
 * complete state model, the resource ids, the total transitions and a set
 * of possible user transitions.
 */
public class Statemodel implements Serializable {

	private UsageCriteria	                            usageCriteria;
	private Set<StateVO>	                            allStates;
	private Integer	                                    initialStateId;
	private Map<Integer, Collection<StateTransitionVO>>	stateTransitions;
	private Map<Integer, String>	                    labelResourceSIDs;
	private Map<Integer, String>	                    desriptionResourceSIDs;
	private Set<Integer>	                            userTransitionIDs;

	private Map<Integer, StateVO>	                    _stateLookup;

    public Statemodel(UsageCriteria usageCriteria) {
		this.usageCriteria = usageCriteria;
		stateTransitions = new HashMap<Integer, Collection<StateTransitionVO>>();
		labelResourceSIDs = new HashMap<Integer, String>();
		desriptionResourceSIDs = new HashMap<Integer, String>();
    }

    public boolean isComplete() {
        return usageCriteria != null
            && allStates != null
            && initialStateId != null
            && stateTransitions.size() == allStates.size()
            && labelResourceSIDs.size() == allStates.size()
            && desriptionResourceSIDs.size() == allStates.size()
            && userTransitionIDs != null;
    }

    public UsageCriteria getUsageCriteria() {
        return usageCriteria;
    }

    public void setUsageCriteria(UsageCriteria usageCriteria) {
        this.usageCriteria = usageCriteria;
    }

    public Set<StateVO> getAllStates() {
        return allStates;
    }

    public void setAllStates(Collection<StateVO> allStates) {
        this.allStates = new HashSet<StateVO>(allStates);
    }

    public Set<Integer> getStateIDs() {
        return CollectionUtils.transformIntoSet(allStates, new NuclosValueObject.GetId());
    }

    public Integer getInitialStateId() {
        return initialStateId;
    }

    public void setInitialStateId(Integer initialStateId) {
        this.initialStateId = initialStateId;
    }

    public void setTransitionsForState(Integer stateId, Collection<StateTransitionVO> transitions) {
        stateTransitions.put(stateId, transitions);
    }

    public void setResourceSIDsForState(Integer stateId, String labelResourceSId, String descriptionResourceSId) {
        labelResourceSIDs.put(stateId, labelResourceSId);
        desriptionResourceSIDs.put(stateId, descriptionResourceSId);
    }

    public Map<Integer, String> getLabelResourceSIDs() {
    	return labelResourceSIDs;
    }

	public Map<Integer, String> getDesriptionResourceSIDs() {
    	return desriptionResourceSIDs;
    }

	public Set<Integer> getUserTransitionIDs() {
        return userTransitionIDs;
    }

    public void setUserTransitionIDs(Collection<Integer> userTransitionIDs) {
        this.userTransitionIDs = new HashSet<Integer>(userTransitionIDs);
    }
    
    public StateVO getState(Integer iStateId) {
    	return getStateLookup().get(iStateId);
    }

    private Map<Integer, StateVO> getStateLookup() {
        if(_stateLookup == null)
            _stateLookup = CollectionUtils.generateLookupMap(allStates, TransformerUtils.<StateVO>getId());
        return _stateLookup;
    }

    public List<StateVO> getSubsequentStates(Integer stateId, final boolean includeAutomatic) {
        ArrayList<StateVO> res = new ArrayList<StateVO>();
        if(stateId == null) {
            res.add(getStateLookup().get(initialStateId));
        }
        else {
            Collection<StateTransitionVO> outgoingTransitions
            	= stateTransitions.get(stateId);
            if (outgoingTransitions != null) {
	            Collection<StateTransitionVO> filteredTransitions
		            = CollectionUtils.applyFilter(outgoingTransitions,
		                new Predicate<StateTransitionVO>() {
		                    @Override
		                    public boolean evaluate(StateTransitionVO t) {
		                        return includeAutomatic || !t.isAutomatic();
		                    }});
	            for(StateTransitionVO trans : filteredTransitions) {
	            	if(userTransitionIDs.contains(trans.getId()) || includeAutomatic)
	            		res.add(getStateLookup().get(trans.getStateTarget()));
	            }
            }
        }
        
		Collections.sort(res, new Comparator<StateVO>() {
			@Override
            public int compare(StateVO statevo1, StateVO statevo2) {
				return statevo1.getStatename().compareTo(statevo2.getStatename());
			}
		});
        return res;
    }

    public List<StateVO> getDefaultStatePath() {
    	List<StateVO> result = new LinkedList<StateVO>();
    	
		// validate if there is an valid path from an start to an end state.
		List<StateTransitionVO> transitionVOs = new LinkedList<StateTransitionVO>();
		for (Collection<StateTransitionVO> transitions : stateTransitions.values()) {
			transitionVOs.addAll(transitions);
		}
		if (CollectionUtils.indexOfFirst(transitionVOs, new Predicate<StateTransitionVO>() {
			@Override public boolean evaluate(StateTransitionVO t) { return t.isDefault(); }
		}) != -1) {
			// find start transition - there has to be one because of the checks before.
			StateTransitionVO startTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
				@Override public boolean evaluate(StateTransitionVO t) {
					return t.getStateSource().equals(initialStateId) && t.isDefault();
				}
			});
			result.add(getStateLookup().get(initialStateId));
			if (startTransition != null) {
				result.add(getStateLookup().get(startTransition.getStateTarget()));
			}
            
            List<Integer> checkedStateNumerals = new LinkedList<Integer>();
			
			// finde alle trans die als source den end der letzten haben.
            if (startTransition != null) {
            	Integer iSubsequentState = startTransition.getStateTarget();
				while (iSubsequentState != null) {
					if (checkedStateNumerals.contains(iSubsequentState)) {
						break;
						//throw new CommonValidationException("statemachine.error.validation.graph.defaulttransition");					
					}
					final Integer iSubsequentStateSource = iSubsequentState;
					StateTransitionVO subsequentTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
						@Override public boolean evaluate(StateTransitionVO t) { return t.getStateSource().equals(iSubsequentStateSource) && t.isDefault() == true; }
					});
					
					if (subsequentTransition == null) {
						break;
					}
					
					// iterate next.
					checkedStateNumerals.add(iSubsequentState);
					iSubsequentState = subsequentTransition.getStateTarget();
					StateVO state = getStateLookup().get(subsequentTransition.getStateTarget());
					state.setFromAutomatic(subsequentTransition.isAutomatic());
		            result.add(state);
				}
            }
		}
		return result;
    }
    
    public Integer getStateIdFromNumeral(Integer numeral) {
    	for (Iterator iterator = allStates.iterator(); iterator.hasNext();) {
			StateVO state = (StateVO) iterator.next();
			if (state.getNumeral().equals(numeral))
				return state.getId();
		}
    	return null;
    }
    public List<Integer> isStateReachableInDefaultPathByNumeral(final Integer stateCurrent, StateVO stateToReach) {
    	return isStateReachableInDefaultPath(getStateIdFromNumeral(stateCurrent), stateToReach);
    }    
    public List<Integer> isStateReachableInDefaultPath(final Integer stateCurrent, StateVO stateToReach) {
    	List<Integer> result = new LinkedList<Integer>();
    	
    	// state to reach is not in a default path
    	List<StateVO> statesDefaultPath = getDefaultStatePath();
    	if (!statesDefaultPath.contains(stateToReach)) {
    		return result;
    	}
    	
    	List<StateTransitionVO> transitionVOs = new LinkedList<StateTransitionVO>();
		for (Collection<StateTransitionVO> transitions : stateTransitions.values()) {
			transitionVOs.addAll(transitions);
		}

    	// we are not at defaultpath, but we can reach default path within one step.
		for (StateVO stateVO : getSubsequentStates(stateCurrent, false)) {
			if (stateVO.getId().equals(stateToReach.getId())) {
				result.add(stateToReach.getId());
    			return result;
			}
		}
		
		// find start transition
		StateTransitionVO startTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
			@Override public boolean evaluate(StateTransitionVO t) {
				return t.isDefault() && !t.isAutomatic() && t.getStateSource().equals(stateCurrent);
			}
		});
		
		if (startTransition == null)
			return result;

		List<Integer> checkedStateNumerals = new LinkedList<Integer>();
		Integer iSubsequentState = startTransition.getStateTarget();
		if (!startTransition.isAutomatic() && !result.contains(iSubsequentState)) {
			result.add(iSubsequentState);
		}
		while (iSubsequentState != null) {			
			if (iSubsequentState.equals(stateToReach.getId())) {
				return result;
			}
			
			final Integer iSubsequentStateSource = iSubsequentState;
			StateTransitionVO subsequentTransition = CollectionUtils.findFirst(transitionVOs, new Predicate<StateTransitionVO>() {
				@Override public boolean evaluate(StateTransitionVO t) { return t.isDefault() && !t.isAutomatic() && t.getStateSource().equals(iSubsequentStateSource); }
			});
			
			if (subsequentTransition == null) {
				break;
			}
			
			// add path.
			if (!subsequentTransition.isAutomatic() && !result.contains(iSubsequentState)) {
				result.add(iSubsequentState);
			}
			
			if (checkedStateNumerals.contains(subsequentTransition.getId())) {
				break;
			}
			
			if (iSubsequentState.equals(stateToReach.getId())) {
				return result;
			}
			
			// iterate next.
			checkedStateNumerals.add(subsequentTransition.getId());
			iSubsequentState = subsequentTransition.getStateTarget();
			if (iSubsequentState.equals(stateToReach.getId())) {
				if (!result.contains(iSubsequentState))
					result.add(iSubsequentState);
				return result;
			}
		}
		
    	return new LinkedList<Integer>();
    }
}

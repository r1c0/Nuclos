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
package org.nuclos.client.statemodel;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.nuclos.common.SpringApplicationContextHolder;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeRemote;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.InitializingBean;

/**
 * Repository for rules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RuleRepository implements InitializingBean {

	private static RuleRepository INSTANCE;
	
	//

	private final Map<Integer, StateModelRuleVO> mpRules = CollectionUtils.newHashMap();
	
	// Spring injection
	
	private RuleEngineFacadeRemote ruleEngineFacadeRemote;
	
	// end of Spring injection

	public static synchronized RuleRepository getInstance() throws RemoteException {
		if (INSTANCE == null) {
			// INSTANCE = new RuleRepository();
			// lazy support
			INSTANCE = SpringApplicationContextHolder.getBean(RuleRepository.class);
		}
		return INSTANCE;
	}

	RuleRepository() {
	}
	
	@Override
	public void afterPropertiesSet() {
		updateRules();
	}
	
	public final void setRuleEngineFacadeRemote(RuleEngineFacadeRemote ruleEngineFacadeRemote) {
		this.ruleEngineFacadeRemote = ruleEngineFacadeRemote;
	}

	public void updateRules() {
		mpRules.clear();

		try {
		  for (RuleVO rulevo : ruleEngineFacadeRemote.getAllRules()) {
		  	mpRules.put(rulevo.getId(), new StateModelRuleVO(rulevo));
		  }
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * @param iId
	 * @return the rule with the given id.
	 */
	public StateModelRuleVO getRule(Integer iId) {
		return mpRules.get(iId);
	}

	/**
	 * @param filterID
	 */
	public List<SortedRuleVO> selectRulesById(Collection<Integer> filterID, Collection<Integer> rulesRunAfterwards) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		for (Iterator<Integer> i = filterID.iterator(); i.hasNext();) {
			final Integer iId = i.next();
			if (iId != null) {
				try {
					RuleVO ruleVO = ruleEngineFacadeRemote.get(iId);
					final SortedRuleVO sortedRuleVO = new SortedRuleVO(ruleVO);
					if (sortedRuleVO.getId() != null) {
						sortedRuleVO.setOrder(0);
						result.add(sortedRuleVO);
					}
				} catch (Exception e) {
					Log.error(e.getMessage(), e);
				}
			}
		}
		return result;
	}
	
	public List<SortedRuleVO> selectRulesById(List<Pair<Integer, Boolean>> ruleIdsWithRunAfterwards) {
		List<Integer> ids = new ArrayList<Integer>();
		List<Integer> withRunAfterwards = new ArrayList<Integer>();
		for (Pair<Integer, Boolean> rule : ruleIdsWithRunAfterwards) {
			ids.add(rule.x);
			if (rule.y != null && rule.y)
				withRunAfterwards.add(rule.x);
		}
		return selectRulesById(ids, withRunAfterwards);
	}

	/**
	 * @param collsortedrulevoFilter
	 */
	public List<SortedRuleVO> filterRulesByVO(Collection<SortedRuleVO> collsortedrulevoFilter) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		final Map<Integer, SortedRuleVO> filterMap = CollectionUtils.newHashMap();
		for (SortedRuleVO sortedrulevo : collsortedrulevoFilter) {
			filterMap.put(sortedrulevo.getId(), sortedrulevo);
		}

		for (StateModelRuleVO stateModelRuleVO : mpRules.values()) {
			if (!filterMap.containsKey(stateModelRuleVO.getId())) {
				result.add(new SortedRuleVO(stateModelRuleVO));
			}
		}
		return result;
	}

	public List<SortedRuleVO> selectRulesByTransitionId(Integer transId, List<Pair<Integer, Boolean>> ruleIdsWithRunAfterwards) {
		
		List<SortedRuleVO> retVal = new ArrayList<SortedRuleVO>();
		
		for (Pair<Integer, Boolean> rule : ruleIdsWithRunAfterwards) {
			Integer iId = rule.x;
			if (iId != null) {
				try {
					final SortedRuleVO sortedRuleVO = new SortedRuleVO(ruleEngineFacadeRemote.get(iId));
					if (sortedRuleVO.getId() != null) {
						final Collection<RuleEngineTransitionVO> allRuleTransitionsForTransitionId = ruleEngineFacadeRemote.getAllRuleTransitionsForTransitionId(transId);
						for (RuleEngineTransitionVO retVO : allRuleTransitionsForTransitionId) {
							sortedRuleVO.setOrder(retVO.getOrder());
							sortedRuleVO.setRunAfterwards(rule.y != null && rule.y);
						}
						retVal.add(sortedRuleVO);
					}
				} catch (Exception e) {
					Log.error(e.getMessage(), e);
				}
			}
		}
		
		return retVal;
	}


}  // class RuleRepository

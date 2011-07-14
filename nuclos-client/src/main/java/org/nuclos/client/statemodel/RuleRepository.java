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

import javax.ejb.CreateException;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeRemote;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Repository for rules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RuleRepository {

	private static RuleRepository singleton;

	private final Map<Integer, StateModelRuleVO> mpRules = CollectionUtils.newHashMap();

	public static synchronized RuleRepository getInstance() throws CreateException, RemoteException {
		if (singleton == null) {
			singleton = new RuleRepository();
		}
		return singleton;
	}

	private RuleRepository() throws CreateException, RemoteException {
		updateRules();
	}

	public void updateRules() throws CreateException, RemoteException {
		mpRules.clear();

		final RuleEngineFacadeRemote ruleFacade = ServiceLocator.getInstance().getFacade(RuleEngineFacadeRemote.class);
		try {
		  for (RuleVO rulevo : ruleFacade.getAllRules()) {
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

		int iCount = 1;
		for (Iterator<Integer> i = filterID.iterator(); i.hasNext(); iCount++) {
			final Integer iId = i.next();
			final SortedRuleVO sortedRuleVO = new SortedRuleVO(mpRules.get(iId));
			if (sortedRuleVO.getId() != null) {
				sortedRuleVO.setOrder(iCount);
				sortedRuleVO.setRunAfterwards(rulesRunAfterwards.contains(sortedRuleVO.getId()));
				result.add(sortedRuleVO);
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


}  // class RuleRepository
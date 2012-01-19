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
package org.nuclos.client.explorer.node.rule;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.*;
import java.util.*;

/**
 * Treenode representing an Statemodelnode or transition in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class StateModelNode extends AbstractRuleTreeNode {

	private StateModelVO stateModelVo;
	private StateTransitionVO transitionVo;
	private final boolean isAllRuleSubnode;
	private final RuleVO parentRuleVo;

	/**
	 * create an Statemodel node
	 * @param aModelVo
	 * @param aSubNodeList
	 */
	public StateModelNode(StateModelVO aModelVo, List<? extends TreeNode> aSubNodeList
			, boolean hasRuleSubnodesFlag, RuleVO aParentRule) {
		super(aModelVo.getId(), aModelVo.getName(), aModelVo.getDescription(), aSubNodeList, RuleNodeType.TRANSITION);
		this.stateModelVo = aModelVo;
		this.isAllRuleSubnode = hasRuleSubnodesFlag;
		this.parentRuleVo = aParentRule;
	}

	/**
	 * create an state transition tree node
	 * @param aTransitionVo
	 * @param stateGraph
	 * @param aRuleList
	 */
	public StateModelNode(StateTransitionVO aTransitionVo, StateGraphVO stateGraph, List<? extends TreeNode> aRuleList
			, boolean hasRuleSubnodesFlag) {
		super(aTransitionVo.getId(), createNodeLabel(aTransitionVo, stateGraph)
				, aTransitionVo.getDescription(), aRuleList, RuleNodeType.STATEMODEL);
		this.transitionVo = aTransitionVo;
		this.stateModelVo = stateGraph.getStateModel();
		this.isAllRuleSubnode = hasRuleSubnodesFlag;
		this.parentRuleVo = null;
	}

	public StateModelNode(StateTransitionVO aTransitionVo, RuleEngineTransitionVO ruleTrans, StateGraphVO stateGraph, boolean hasRuleSubnodesFlag) {
		super(aTransitionVo.getId(), createNodeLabel(aTransitionVo, stateGraph) + " (" + ruleTrans.getOrder() + ")"
				, aTransitionVo.getDescription(), new ArrayList<TreeNode>(), RuleNodeType.STATEMODEL);
		this.transitionVo = aTransitionVo;
		this.stateModelVo = stateGraph.getStateModel();
		this.isAllRuleSubnode = hasRuleSubnodesFlag;
		this.parentRuleVo = null;
	}

	@Override
	public void refresh() {
		if (!this.isAllRuleSubnode) {
			try {
				if (RuleNodeType.TRANSITION.equals(getNodeType())) {
					StateGraphVO stateGraph = StateDelegate.getInstance().getStateGraph(stateModelVo.getId());

					List<StateModelNode> transitionSubNodeList = new ArrayList<StateModelNode>();
					for (StateTransitionVO transitionVo : stateGraph.getTransitions()) {

						if (transitionVo.getRuleIdsWithRunAfterwards() != null && transitionVo.getRuleIdsWithRunAfterwards().size() > 0) {
							transitionSubNodeList.add(new StateModelNode(transitionVo, stateGraph, null, false));
						}
					}

					setSubNodes(transitionSubNodeList);
				}
				else if (RuleNodeType.STATEMODEL.equals(getNodeType())) {

					final Map<Integer, RuleVO> ruleId2RuleVo = getAllRuleId2RuleMap();

					final Collection<RuleEngineTransitionVO> ruleTransColl = RuleDelegate.getInstance().getAllRuleTransitionsForTransitionId(transitionVo.getId());
					final List<RuleEngineTransitionVO> sortedTransList = new ArrayList<RuleEngineTransitionVO>(ruleTransColl);
					Collections.sort(sortedTransList, new Comparator<RuleEngineTransitionVO>() {
						@Override
						public int compare(RuleEngineTransitionVO o1, RuleEngineTransitionVO o2) {
							return o1.getOrder().compareTo(o2.getOrder());
						}
					});

					final List<TreeNode> ruleSubNodeList = new ArrayList<TreeNode>();
					for (RuleEngineTransitionVO curRuleTrans : sortedTransList) {
						ruleSubNodeList.add(new RuleNode(ruleId2RuleVo.get(curRuleTrans.getRuleId()), false));
					}
					setSubNodes(ruleSubNodeList);
				}
			}
			catch (CommonFinderException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("DirectoryRuleNode.5","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			catch (CommonBusinessException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("DirectoryRuleNode.5","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
		}
		else {
			try {
				if (RuleNodeType.TRANSITION.equals(getNodeType())) {
					final StateGraphVO stateGraph = StateDelegate.getInstance().getStateGraph(stateModelVo.getId());

					final Map<Integer, RuleEngineTransitionVO> transitionIdId2RuleTransitonVo = CollectionUtils.newHashMap();
					for (RuleEngineTransitionVO generationVO : RuleDelegate.getInstance().getAllRuleTransitionsForRuleId(this.parentRuleVo.getId()))
					{
						transitionIdId2RuleTransitonVo.put(generationVO.getTransitionId(), generationVO);
					}

					final List<StateModelNode> transitionSubNodeList = new ArrayList<StateModelNode>();
					for (StateTransitionVO transitionVo : stateGraph.getTransitions()) {
						boolean hasRule = false;
						if (transitionVo.getRuleIdsWithRunAfterwards() != null && transitionVo.getRuleIdsWithRunAfterwards().size() > 0) {

							for (Pair<Integer, Boolean> curRule : transitionVo.getRuleIdsWithRunAfterwards()) {
								if (curRule.x.equals(this.parentRuleVo.getId())) {
									hasRule = true;
								}
							}
						}
						if (hasRule) {
							final RuleEngineTransitionVO ruleTrans = transitionIdId2RuleTransitonVo.get(transitionVo.getId());
							transitionSubNodeList.add(new StateModelNode(transitionVo, ruleTrans, stateGraph, true));
						}
					}

					setSubNodes(transitionSubNodeList);
				}
			}
			catch (CommonFinderException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("DirectoryRuleNode.5","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			catch (CommonBusinessException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("DirectoryRuleNode.5","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
		}
	}

	private static String createNodeLabel(StateTransitionVO aTransitionVo, StateGraphVO stateGraph) {
		stateGraph.getStates();

		Integer startNumeral = null;
		Integer endNumeral = null;

		for (StateVO curState : stateGraph.getStates()) {

			if (aTransitionVo.getStateSource() != null && aTransitionVo.getStateSource().equals(curState.getId())) {
				startNumeral = curState.getNumeral();
			}
			if (aTransitionVo.getStateTarget() != null && aTransitionVo.getStateTarget().equals(curState.getId())) {
				endNumeral = curState.getNumeral();
			}
		}

		final String startNumText = (startNumeral == null) ? "initial" : startNumeral.toString();

		return CommonLocaleDelegate.getMessage("StateModelNode.1", "Status\u00fcbergang ") + startNumText + " -> " + endNumeral;
	}

	public StateModelVO getStateModelVo() {
		return stateModelVo;
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	public boolean isAllRuleSubnode() {
		return isAllRuleSubnode;
	}

}	// class StateModelNode

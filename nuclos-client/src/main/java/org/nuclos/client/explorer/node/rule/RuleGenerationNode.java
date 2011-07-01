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

import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import java.util.*;

/**
 * Treenode representing an Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class RuleGenerationNode extends AbstractRuleTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final GeneratorActionVO generationAd;
	private final boolean isAllRuleSubnode;

	public RuleGenerationNode(GeneratorActionVO aGenerationAd, List<RuleNode> aSubNodeList, boolean aIsAllRuleSubnodeFlag) {
		super(aGenerationAd.getId(), aGenerationAd.getName(), aGenerationAd.getLabel(), aSubNodeList, RuleNodeType.AD_GENERATION);
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.generationAd = aGenerationAd;
	}

	public RuleGenerationNode(GeneratorActionVO aGenerationAd, RuleEngineGenerationVO aRuleGeneration, List<RuleNode> aSubNodeList, boolean aIsAllRuleSubnodeFlag) {
		super(aGenerationAd.getId(), aGenerationAd.getName() + " (" + aRuleGeneration.getOrder() + ")", aGenerationAd.getLabel(), aSubNodeList, RuleNodeType.AD_GENERATION);
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.generationAd = aGenerationAd;
	}

	@Override
	public void refresh() {
		if (!this.isAllRuleSubnode) {
			List<RuleNode> ruleSubNodeList = new ArrayList<RuleNode>();

			Collection<RuleEngineGenerationVO> generationColl = (RuleDelegate.getInstance()).getAllRuleGenerationsForGenerationId(generationAd.getId());
			List<RuleEngineGenerationVO> sortedGenerationList = new ArrayList<RuleEngineGenerationVO>(generationColl);
			Collections.sort(sortedGenerationList, new Comparator<RuleEngineGenerationVO>() {
				@Override
				public int compare(RuleEngineGenerationVO o1, RuleEngineGenerationVO o2) {
					return o1.getOrder().compareTo(o2.getOrder());
				}
			});

			Map<Integer, RuleVO> ruleId2RuleVo = getAllRuleId2RuleMap();
			for (RuleEngineGenerationVO generationVO : sortedGenerationList) {
				ruleSubNodeList.add(new RuleNode(ruleId2RuleVo.get(generationVO.getRuleId()), generationVO, false));
			}
			setSubNodes(ruleSubNodeList);
		}
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	@Override
	public Boolean hasSubNodes() {
		return null;
	}

	public boolean isAllRuleSubnode() {
		return isAllRuleSubnode;
	}

}	// class RuleGenerationNode

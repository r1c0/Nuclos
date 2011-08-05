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
import org.nuclos.common2.CommonLocaleDelegate;

import org.nuclos.client.customcode.CodeDelegate;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.*;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import java.util.*;

/**
 * Treenode representing an Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class RuleNode extends AbstractRuleTreeNode {

	private static final long serialVersionUID = 1L;
	private RuleVO ruleVo;
	private String entity;
	private String eventName;
	private final boolean isAllRuleSubnode;
	public final boolean isTimeLimitRule;

	private RuleNode(RuleVO aRuleVo, Integer iId, String aLabel, String aDescription, ArrayList<? extends TreeNode> aSubNodeList
			, RuleNodeType aNodeType, boolean aIsAllRuleSubnodeFlag, boolean isTimeLimitRule) {

		super(iId, aLabel, aDescription, aSubNodeList, aNodeType);
		this.ruleVo = aRuleVo;
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.isTimeLimitRule = isTimeLimitRule;
	}

	public RuleNode(RuleVO aRuleVo, boolean aIsAllRuleSubnodeFlag) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getName(), aRuleVo.getDescription(), null, RuleNodeType.RULE, aIsAllRuleSubnodeFlag, false);
	}

	public RuleNode(RuleVO aRuleVo, String aEventName, String aEntity) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getName(), aRuleVo.getDescription(), new ArrayList<AbstractRuleTreeNode>(), RuleNodeType.RULE, false, false);
		this.eventName = aEventName;
		this.entity = aEntity;
	}

	public RuleNode(RuleVO aRuleVo, String aEventName, boolean isTimeLimitRule) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getName(), aRuleVo.getDescription(), new ArrayList<AbstractRuleTreeNode>(), RuleNodeType.RULE, false, isTimeLimitRule);
		this.eventName = aEventName;
	}

	public RuleNode(RuleVO aRuleVo, RuleEngineGenerationVO generationVO, boolean aIsAllRuleSubnodeFlag) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getName() // + " (" +generationVO.getOrder() + ")"
				, aRuleVo.getDescription(), new ArrayList<AbstractRuleTreeNode>(), RuleNodeType.RULE, aIsAllRuleSubnodeFlag, false);
	}

	public boolean isAllRuleSubnode() {
		return isAllRuleSubnode;
	}

	@Override
	public void refresh() {
		if (isAllRuleSubnode) {
			final List<TreeNode> subNodeList = new ArrayList<TreeNode>();

			final List<StateModelNode> transitionNodes = createTransitionNodes();
			if (transitionNodes != null && transitionNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, CommonLocaleDelegate.getMessage("RuleNode.11","Status\u00fcberg\u00e4nge"), CommonLocaleDelegate.getMessage("RuleNode.12","Status\u00fcberg\u00e4nge"), transitionNodes, true));
			}

			final List<RuleGenerationNode> generationNodes = createGenerationNodes();
			if (generationNodes != null && generationNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, CommonLocaleDelegate.getMessage("RuleNode.6","Objektgenerierung"), CommonLocaleDelegate.getMessage("RuleNode.7","Objektgenerierung"), generationNodes, true));
			}

			final List<EntityRuleNode> saveEntityNodes = createEventEntityRulesNode(RuleTreeModel.SAVE_EVENT_NAME);
			if (saveEntityNodes != null && saveEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, CommonLocaleDelegate.getMessage("RuleNode.9","Speichern pro Entit\u00e4t"), CommonLocaleDelegate.getMessage("RuleNode.10","Speichern pro Entit\u00e4t"), saveEntityNodes, true));
			}

			final List<EntityRuleNode> deleteEntityNodes = createEventEntityRulesNode(RuleTreeModel.DELETE_EVENT_NAME);
			if (deleteEntityNodes != null && deleteEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, CommonLocaleDelegate.getMessage("RuleNode.4","L\u00f6schen pro Entit\u00e4t"), CommonLocaleDelegate.getMessage("RuleNode.5","L\u00f6schen pro Entit\u00e4t"), deleteEntityNodes, true));
			}

			final List<EntityRuleNode> userEventEntityNodes = createEventEntityRulesNode(RuleTreeModel.USER_EVENT_NAME);
			if (userEventEntityNodes != null && userEventEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, CommonLocaleDelegate.getMessage("RuleNode.1","Benutzeraktion pro Entit\u00e4t"), CommonLocaleDelegate.getMessage("RuleNode.2","Entit\u00e4ten in denen der Benutzer diese Regel manuell starten kann"), userEventEntityNodes, true));
			}

			final Collection<RuleEventUsageVO> collTimelimit = RuleDelegate.getInstance().getByEventAndRule(RuleTreeModel.FRIST_EVENT_NAME, this.ruleVo.getId());
			if (collTimelimit != null && collTimelimit.size() > 0) {
				subNodeList.add(new TimelimitNode(CommonLocaleDelegate.getMessage("RuleNode.3","Fristen"), CommonLocaleDelegate.getMessage("RuleNode.8","Regeln die t\u00e4glich vom System ausgef\u00fchrt werden"), false));
			}

			final List<CodeVO> codes = CodeDelegate.getInstance().getAll();
			if (codes != null && codes.size() > 0) {
				subNodeList.add(new LibraryTreeNode(CommonLocaleDelegate.getText("treenode.rules.library.label"), CommonLocaleDelegate.getText("treenode.rules.library.description")));
			}
			setSubNodes(subNodeList);
		}
	}

	private List<EntityRuleNode> createEventEntityRulesNode(String sEventName) {
		final List<EntityRuleNode> result = new ArrayList<EntityRuleNode>();

		final Collection<RuleEventUsageVO> collSaveEvent = RuleDelegate.getInstance().getByEventAndRule(sEventName, this.ruleVo.getId());
		if (collSaveEvent != null && collSaveEvent.size() > 0) {
			for (RuleEventUsageVO usageVO : collSaveEvent) {
				result.add(new EntityRuleNode(sEventName, null, usageVO.getEntity() + " (" + usageVO.getOrder() + ")", null, false));
			}
		}

		sortNodeListByLabel(result);

		return result;
	}


	private List<RuleGenerationNode> createGenerationNodes() {
		final Map<Integer, RuleEngineGenerationVO> generationIdId2RuleGenerationVo = CollectionUtils.newHashMap();
		for (RuleEngineGenerationVO generationVO : RuleDelegate.getInstance().getAllRuleGenerationsForRuleId(this.ruleVo.getId()))
		{
			generationIdId2RuleGenerationVo.put(generationVO.getGenerationId(), generationVO);
		}

		final List<RuleGenerationNode> result = new ArrayList<RuleGenerationNode>();

		final Collection<GeneratorActionVO> collGeneration = RuleDelegate.getInstance().getAllAdGenerationsForRuleId(this.ruleVo.getId());
		if (collGeneration != null && collGeneration.size() > 0) {
			for (GeneratorActionVO generationVO : collGeneration) {
				final RuleEngineGenerationVO ruleGeneration = generationIdId2RuleGenerationVo.get(generationVO.getId());
				result.add(new RuleGenerationNode(generationVO, ruleGeneration, new ArrayList<RuleNode>(), true));
			}
		}

		sortNodeListByLabel(result);

		return result;
	}

	private List<StateModelNode> createTransitionNodes() {
		final List<StateModelNode> result = new ArrayList<StateModelNode>();

		final Collection<StateModelVO> collModels = RuleDelegate.getInstance().getAllStateModelsForRuleId(this.ruleVo.getId());
		if (collModels != null && collModels.size() > 0) {
			for (StateModelVO modelVO : collModels) {
				result.add(new StateModelNode(modelVO, null, true, this.ruleVo));
			}
		}

		sortNodeListByLabel(result);

		return result;
	}

	@Override
	public Boolean hasSubNodes() {
		return null;
	}

	@Override
	public RuleAndRuleUsageEntity getRuleEntity() {
		return new RuleAndRuleUsageEntity(this.ruleVo, this.eventName, this.entity);
	}

	public RuleVO getRuleVo() {
		return ruleVo;
	}

	public boolean isActive() {
		return getRuleVo() != null && getRuleVo().isActive();
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof RuleNode
				&& (ruleVo != null) ? ruleVo.equals(((RuleNode) obj).getRuleVo())
				: getLabel().equals(((RuleNode) obj).getLabel());
	}

	@Override
	public int hashCode() {
		return (ruleVo != null) ? ruleVo.hashCode() : getLabel().hashCode();
	}

}	// class RuleNode

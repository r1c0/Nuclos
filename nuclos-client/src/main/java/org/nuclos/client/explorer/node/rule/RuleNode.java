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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.client.explorer.node.rule.EntityRuleNode.EntityRuleUsageProcessNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode.EntityRuleUsageStatusNode;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Treenode representing an Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class RuleNode extends AbstractRuleTreeNode {

	private RuleVO ruleVo;
	private String entity;
	private String eventName;
	private Integer statusId = null;
	private Integer processId = null;
	private final boolean isAllRuleSubnode;
	public final boolean isTimeLimitRule;
	public boolean hasLibraryRules;
	public boolean hasTimeLimitRules;

	private RuleNode(RuleVO aRuleVo, Integer iId, String aLabel, String aDescription, ArrayList<? extends TreeNode> aSubNodeList
			, RuleNodeType aNodeType, boolean aIsAllRuleSubnodeFlag, boolean isTimeLimitRule) {

		super(iId, aLabel, aDescription, aSubNodeList, aNodeType);
		this.ruleVo = aRuleVo;
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.isTimeLimitRule = isTimeLimitRule;
		this.hasLibraryRules = false;
	}

	public RuleNode(RuleVO aRuleVo, boolean aIsAllRuleSubnodeFlag, boolean hasLibraryRules) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getRule(), aRuleVo.getDescription(), null, RuleNodeType.RULE, aIsAllRuleSubnodeFlag, false);
		if (aIsAllRuleSubnodeFlag) {
			this.hasLibraryRules = hasLibraryRules;
		}
	}

	public RuleNode(RuleVO aRuleVo, String aEventName, String aEntity, Integer processId, Integer statusId) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getRule(), aRuleVo.getDescription(), new ArrayList<AbstractRuleTreeNode>(), RuleNodeType.RULE, false, false);
		this.eventName = aEventName;
		this.entity = aEntity;
		this.statusId = statusId;
		this.processId = processId;
	}

	public RuleNode(RuleVO aRuleVo, String aEventName, boolean isTimeLimitRule) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getRule(), aRuleVo.getDescription(), new ArrayList<AbstractRuleTreeNode>(), RuleNodeType.RULE, false, isTimeLimitRule);
		this.eventName = aEventName;
	}

	public RuleNode(RuleVO aRuleVo, RuleEngineGenerationVO generationVO, boolean aIsAllRuleSubnodeFlag) {
		this(aRuleVo, aRuleVo.getId(), aRuleVo.getRule() // + " (" +generationVO.getOrder() + ")"
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
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage(
						"RuleNode.11","Status\u00fcberg\u00e4nge"), 
						getSpringLocaleDelegate().getMessage("RuleNode.12","Status\u00fcberg\u00e4nge"), transitionNodes, true));
			}

			final List<RuleGenerationNode> generationNodes = createGenerationNodes();
			if (generationNodes != null && generationNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.6","Objektgenerierung"), 
						getSpringLocaleDelegate().getMessage("RuleNode.7","Objektgenerierung"), generationNodes, true));
			}

			final List<EntityRuleNode> saveEntityNodes = createEventEntityRulesNode(RuleTreeModel.SAVE_EVENT_NAME);
			if (saveEntityNodes != null && saveEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.9","Speichern"), 
						getSpringLocaleDelegate().getMessage("RuleNode.10","Speichern"), saveEntityNodes, true));
			}

			final List<EntityRuleNode> saveAfterEntityNodes = createEventEntityRulesNode(RuleTreeModel.SAVE_AFTER_EVENT_NAME);
			if (saveAfterEntityNodes != null && saveAfterEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.13","Speichern (Im Anschluss)"), 
						getSpringLocaleDelegate().getMessage("RuleNode.14","Speichern (Im Anschluss)"), saveAfterEntityNodes, true));
			}

			final List<EntityRuleNode> deleteEntityNodes = createEventEntityRulesNode(RuleTreeModel.DELETE_EVENT_NAME);
			if (deleteEntityNodes != null && deleteEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.4","L\u00f6schen"), 
						getSpringLocaleDelegate().getMessage("RuleNode.5","L\u00f6schen"), deleteEntityNodes, true));
			}

			final List<EntityRuleNode> deleteAfterEntityNodes = createEventEntityRulesNode(RuleTreeModel.DELETE_AFTER_EVENT_NAME);
			if (deleteAfterEntityNodes != null && deleteAfterEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.15","L\u00f6schen (Im Anschluss)"), 
						getSpringLocaleDelegate().getMessage("RuleNode.16","L\u00f6schen (Im Anschluss)"), deleteAfterEntityNodes, true));
			}

			final List<EntityRuleNode> userEventEntityNodes = createEventEntityRulesNode(RuleTreeModel.USER_EVENT_NAME);
			if (userEventEntityNodes != null && userEventEntityNodes.size() > 0) {
				subNodeList.add(new DirectoryRuleNode(false, getSpringLocaleDelegate().getMessage("RuleNode.1","Benutzeraktion"), 
						getSpringLocaleDelegate().getMessage("RuleNode.2","Entit\u00e4ten in denen der Benutzer diese Regel manuell starten kann"), userEventEntityNodes, true));
			}

			//final Collection<RuleEventUsageVO> collTimelimit = RuleDelegate.getInstance().getByEventAndRule(RuleTreeModel.FRIST_EVENT_NAME, this.ruleVo.getId());
			/*if (collTimelimit != null && collTimelimit.size() > 0) {
			//if (hasTimeLimitRules) {
				subNodeList.add(new TimelimitNode(getSpringLocaleDelegate().getMessage("RuleNode.3","Fristen"), 
						getSpringLocaleDelegate().getMessage("RuleNode.8","Regeln die t\u00e4glich vom System ausgef\u00fchrt werden"), false));
			}*/

			//final List<CodeVO> codes = CodeDelegate.getInstance().getAll();
			//if (codes != null && codes.size() > 0) {
			if (hasLibraryRules) {
				subNodeList.add(new LibraryTreeNode(getSpringLocaleDelegate().getText("treenode.rules.library.label"), 
						getSpringLocaleDelegate().getText("treenode.rules.library.description")));
			}
			setSubNodes(subNodeList);
		}
	}

	private List<EntityRuleNode> createEventEntityRulesNode(String sEventName) {
		final List<EntityRuleNode> result = new ArrayList<EntityRuleNode>();

		final Map<String, Map<Integer, List<EntityRuleUsageStatusNode>>> entityRuleNodeMap = new HashMap<String, Map<Integer,List<EntityRuleUsageStatusNode>>>();

		final Collection<RuleEventUsageVO> collSaveEvent = RuleDelegate.getInstance().getByEventAndRule(sEventName, this.ruleVo.getId());
		if (collSaveEvent != null && collSaveEvent.size() > 0) {
			for (RuleEventUsageVO usageVO : collSaveEvent) {
				Map<Integer, List<EntityRuleUsageStatusNode>> ruleUsageProcessMap = entityRuleNodeMap.get(usageVO.getEntity());
				if (ruleUsageProcessMap == null) {
					ruleUsageProcessMap = new HashMap<Integer, List<EntityRuleUsageStatusNode>>();
				}
				
				Integer processId = usageVO.getProcessId() == null ? new Integer(-1) : usageVO.getProcessId();
				List<EntityRuleUsageStatusNode> ruleUsageStatusNodes = ruleUsageProcessMap.get(processId);
				if (ruleUsageStatusNodes == null) {
					ruleUsageStatusNodes = new ArrayList<EntityRuleUsageStatusNode>();
				}
				
				try {
					final String statusLabel;
					Integer statusId = usageVO.getStatusId() == null ? new Integer(-1) : usageVO.getStatusId();
					if (statusId.intValue() == -1)
						statusLabel = SpringLocaleDelegate.getInstance().getResource("EntityRuleNode.1", "<Jeder Status>");
					else {
						MasterDataVO mdvoStatus = MasterDataCache.getInstance().get(NuclosEntity.STATE.getEntityName(), statusId);
						statusLabel = mdvoStatus.getField("numeral").toString() + " " + mdvoStatus.getField("name").toString();
					}
					ruleUsageStatusNodes.add(new EntityRuleUsageStatusNode(sEventName, null, usageVO.getEntity(),
							usageVO.getProcessId(), usageVO.getStatusId(), statusLabel  + " (" + usageVO.getOrder() + ")", true));
					
					ruleUsageProcessMap.put(processId, ruleUsageStatusNodes);
					entityRuleNodeMap.put(usageVO.getEntity(), ruleUsageProcessMap);
				} catch (Exception e) {
					continue;
				}
			}
			
			for (Map.Entry<String, Map<Integer, List<EntityRuleUsageStatusNode>>> entityRuleNodeEntry : entityRuleNodeMap.entrySet()) {
				EntityRuleNode entityRuleNode
					= new EntityRuleNode(sEventName, null, entityRuleNodeEntry.getKey(), null, false, null, null);
				List<EntityRuleUsageProcessNode> entityRuleNodeSubNodes = new ArrayList<EntityRuleNode.EntityRuleUsageProcessNode>();
				for (Map.Entry<Integer, List<EntityRuleUsageStatusNode>> ruleUsageProcessNodeEntry : entityRuleNodeEntry.getValue().entrySet()) {
					try {
						final String processLabel;
						Integer processId = ruleUsageProcessNodeEntry.getKey();
						if (processId.intValue() == -1)
							processLabel = SpringLocaleDelegate.getInstance().getResource("EntityRuleNode.2", "<Alle Aktionen>");
						else
							processLabel = MasterDataCache.getInstance().get(NuclosEntity.PROCESS.getEntityName(), processId).getField("name").toString();
						EntityRuleUsageProcessNode entityRuleUsageProcessNode = new EntityRuleUsageProcessNode(sEventName, null, 
								entityRuleNodeEntry.getKey(), processId, processLabel);
						
						List<EntityRuleUsageStatusNode> entityRuleUsageStatusNodes
							= new ArrayList<EntityRuleNode.EntityRuleUsageStatusNode>(ruleUsageProcessNodeEntry.getValue());
						Collections.sort(entityRuleUsageStatusNodes, new Comparator<EntityRuleUsageStatusNode>() {
							@Override
							public int compare(EntityRuleUsageStatusNode o1,
									EntityRuleUsageStatusNode o2) {
								if (o1.statusId == null || o1.statusId.equals(new Integer(-1)))
									return -1;
								else if (o2.statusId == null || o2.statusId.equals(new Integer(-1)))
									return 1;
								else
									return o1.getLabel().compareTo(o2.getLabel());
							}
						});
						entityRuleUsageProcessNode.setSubNodes(entityRuleUsageStatusNodes);
						entityRuleNodeSubNodes.add(entityRuleUsageProcessNode);
					} catch (Exception e) {
						continue;
					}
					
					Collections.sort(entityRuleNodeSubNodes, new Comparator<EntityRuleUsageProcessNode>() {
						@Override
						public int compare(EntityRuleUsageProcessNode o1,
								EntityRuleUsageProcessNode o2) {
							if (o1.processId == null || o1.processId.equals(new Integer(-1)))
								return -1;
							else if (o2.processId == null || o2.processId.equals(new Integer(-1)))
								return 1;
							else
								return o1.getLabel().compareTo(o2.getLabel());
						}
					});
					
					entityRuleNode.setSubNodes(entityRuleNodeSubNodes);
				}
				result.add(entityRuleNode);
			}
		}

		sortNodeListByLabel(result);

		return result;
	}


	private List<RuleGenerationNode> createGenerationNodes() {
		final Map<Integer, RuleEngineGenerationVO> generationIdId2RuleGenerationVo = CollectionUtils.newHashMap();
		for (RuleEngineGenerationVO generationVO : RuleCache.getInstance().getAllRuleGenerationsForRuleId(this.ruleVo.getId()))
		{
			generationIdId2RuleGenerationVo.put(generationVO.getGenerationId(), generationVO);
		}

		final List<RuleGenerationNode> result = new ArrayList<RuleGenerationNode>();

		final Collection<GeneratorActionVO> collGeneration = RuleCache.getInstance().getAllAdGenerationsForRuleId(this.ruleVo.getId());
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

		final Collection<StateModelVO> collModels = RuleCache.getInstance().getAllStateModelsForRuleId(this.ruleVo.getId());
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
		return isAllRuleSubnode ? Boolean.FALSE : null;
	}

	@Override
	public RuleAndRuleUsageEntity getRuleEntity() {
		return new RuleAndRuleUsageEntity(this.ruleVo, this.eventName, this.entity, this.processId, this.statusId);
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

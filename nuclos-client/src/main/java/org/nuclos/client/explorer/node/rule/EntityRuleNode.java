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

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Treenode representing an module Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class EntityRuleNode extends AbstractRuleTreeNode {

	public static class EntityRuleUsageStatusNode extends EntityRuleNode {
		
		private final Map<RuleVO, Integer> lstRules;
		private final boolean isLeaf;
		
		public EntityRuleUsageStatusNode(String aEventName, String aEntity, String entityName, Integer processId, Integer statusId, String statusLabel, boolean isLeaf) {
			super(aEventName, aEntity, statusLabel, null, false, processId, statusId);
			
			this.lstRules = new HashMap<RuleVO, Integer>();
			this.isLeaf = isLeaf;
		}
		
		public boolean isLeaf() {
			return isLeaf;
		}
		
		public void putRules(RuleVO ruleVO, Integer iOrder) {
			lstRules.put(ruleVO, iOrder);
		}

		@Override
		public void refresh() {
			if (!isAllRuleSubnode) {
				removeSubNodes();
				
				final List<RuleNode> subNodeList = new ArrayList<RuleNode>();

				for (RuleVO ruleVO : lstRules.keySet()) {
					subNodeList.add(new RuleNode(ruleVO, eventName, entity,
							new Integer(-1).equals(processId) ? null : processId, new Integer(-1).equals(statusId) ? null : statusId));
				}
				
				Collections.sort(subNodeList, new Comparator<RuleNode>() {
					@Override
					public int compare(RuleNode o1,
							RuleNode o2) {
						return LangUtils.compare(lstRules.get(o1.getRuleVo()), lstRules.get(o2.getRuleVo()));
					}
				});

				setSubNodes(subNodeList);
			}
		}		
	}

	public static class EntityRuleUsageProcessNode extends EntityRuleNode {
		
		private final Map<RuleVO, List<RuleEventUsageVO>> mpRuleEventUsages;
		
		public EntityRuleUsageProcessNode(String aEventName, String aEntity, String entityName, Integer processId, String processLabel) {
			super(aEventName, aEntity, processLabel, null, false, processId, -1);
			
			this.mpRuleEventUsages = new HashMap<RuleVO, List<RuleEventUsageVO>>();
		}
		
		public void putRuleEventUsages(RuleVO ruleVO, RuleEventUsageVO ruleEventUsageVO) {
			List<RuleEventUsageVO> reRuleEventUsages = mpRuleEventUsages.get(ruleVO);
			if (reRuleEventUsages == null) {
				reRuleEventUsages = new ArrayList<RuleEventUsageVO>();
			}
			reRuleEventUsages.add(ruleEventUsageVO);
			mpRuleEventUsages.put(ruleVO, reRuleEventUsages);
		}

		@Override
		public void refresh() {
			if (!isAllRuleSubnode) {
				removeSubNodes();
				
				final List<AbstractRuleTreeNode> subNodeList = new ArrayList<AbstractRuleTreeNode>();

				Map<Integer, EntityRuleUsageStatusNode> ruleUsageStatusMap = new HashMap<Integer, EntityRuleUsageStatusNode>();
				for (RuleVO ruleVO : mpRuleEventUsages.keySet()) {
					final Collection<RuleEventUsageVO> collSaveEvent = mpRuleEventUsages.get(ruleVO);
					if (collSaveEvent != null && collSaveEvent.size() > 0) {
						for (RuleEventUsageVO ruleEventUsageVO : collSaveEvent) {
							try {
								Integer statusId = ruleEventUsageVO.getStatusId() == null ? new Integer(-1) : ruleEventUsageVO.getStatusId();
								EntityRuleUsageStatusNode entityRuleUsageStatusNode  = ruleUsageStatusMap.get(statusId);
								if (entityRuleUsageStatusNode == null) {
									final String statusLabel;
									if (statusId.intValue() == -1)
										statusLabel = SpringLocaleDelegate.getInstance().getResource("EntityRuleNode.1", "<Jeder Status>");
									else {
										MasterDataVO mdvoStatus = MasterDataCache.getInstance().get(NuclosEntity.STATE.getEntityName(), statusId);
										statusLabel = mdvoStatus.getField("numeral").toString() + " " + mdvoStatus.getField("name").toString();
									}
									entityRuleUsageStatusNode = new EntityRuleUsageStatusNode(eventName, entity, 
											SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(entity)),
											processId, statusId, statusLabel, false);
									
									ruleUsageStatusMap.put(statusId, entityRuleUsageStatusNode);
								}
								
								entityRuleUsageStatusNode.putRules(ruleVO, ruleEventUsageVO.getOrder());
							} catch (Exception e) {
								continue;
							}
						}
					}
				}
				
				List<EntityRuleUsageStatusNode> entityRuleUsageStatusNodes = new ArrayList<EntityRuleUsageStatusNode>(ruleUsageStatusMap.values());
				Collections.sort(entityRuleUsageStatusNodes, new Comparator<EntityRuleUsageStatusNode>() {
					@Override
					public int compare(EntityRuleUsageStatusNode o1,
							EntityRuleUsageStatusNode o2) {
						if (o1.statusId.equals(new Integer(-1)))
							return -1;
						else if (o2.statusId.equals(new Integer(-1)))
							return 1;
						else
							return o1.getLabel().compareTo(o2.getLabel());
					}
				});
				
				for (EntityRuleUsageStatusNode entityRuleUsageStatusNode : entityRuleUsageStatusNodes) {
					subNodeList.add(entityRuleUsageStatusNode);
				}

				setSubNodes(subNodeList);
			}
		}

	}

	protected final boolean isAllRuleSubnode;
	protected final String entity;
	protected final String eventName;
	protected final Integer statusId;
	protected final Integer processId;

	public EntityRuleNode(String aEventName, String aEntity, String entityName, List<RuleNode> saveRules,
			boolean aIsAllRuleSubnodeFlag, Integer processId, Integer statusId) {

		super(null, entityName, entityName, saveRules, RuleNodeType.ENTITY);

		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.entity = aEntity;
		this.eventName = aEventName;
		this.statusId = statusId;
		this.processId = processId;
	}

	@Override
	public void refresh() {
		if (!isAllRuleSubnode) {
			final List<AbstractRuleTreeNode> subNodeList = new ArrayList<AbstractRuleTreeNode>();

			Map<Integer, EntityRuleUsageProcessNode> ruleUsageProcessMap = new HashMap<Integer, EntityRuleUsageProcessNode>();
			for (RuleVO ruleVO : RuleDelegate.getInstance().getByEventAndEntityOrdered(eventName, entity)) {
				final Collection<RuleEventUsageVO> collSaveEvent = RuleCache.getInstance().getByEventAndRule(eventName, ruleVO.getId());
				if (collSaveEvent != null && collSaveEvent.size() > 0) {
					for (RuleEventUsageVO ruleEventUsageVO : collSaveEvent) {
						if (!ruleEventUsageVO.getEntity().equals(entity) || !ruleEventUsageVO.getEvent().equals(eventName))
							continue;
						
						try {
							Integer processId = ruleEventUsageVO.getProcessId() == null ? new Integer(-1) : ruleEventUsageVO.getProcessId();
							EntityRuleUsageProcessNode entityRuleUsageProcessNode  = ruleUsageProcessMap.get(processId);
							
							if (entityRuleUsageProcessNode == null) {
								final String processLabel;
								if (processId.intValue() == -1)
									processLabel = SpringLocaleDelegate.getInstance().getResource("EntityRuleNode.2", "<Alle Aktionen>");
								else
									processLabel = MasterDataCache.getInstance().get(NuclosEntity.PROCESS.getEntityName(), processId).getField("name").toString();
								entityRuleUsageProcessNode = new EntityRuleUsageProcessNode(eventName, entity, 
										SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(entity)), processId, processLabel);
								
								ruleUsageProcessMap.put(processId, entityRuleUsageProcessNode);
							}
							
							entityRuleUsageProcessNode.putRuleEventUsages(ruleVO, ruleEventUsageVO);
						} catch (Exception e) {
							continue;
						}
					}
				}
			}
			
			List<EntityRuleUsageProcessNode> ruleUsageProcessNodes = new ArrayList<EntityRuleUsageProcessNode>(ruleUsageProcessMap.values());
			Collections.sort(ruleUsageProcessNodes, new Comparator<EntityRuleUsageProcessNode>() {
				@Override
				public int compare(EntityRuleUsageProcessNode o1,
						EntityRuleUsageProcessNode o2) {
					if (o1.processId.equals(new Integer(-1)))
						return -1;
					else if (o2.processId.equals(new Integer(-1)))
						return 1;
					else
						return o1.getLabel().compareTo(o2.getLabel());
				}
			});
			for (EntityRuleUsageProcessNode ruleUsageProcessNode : ruleUsageProcessNodes) {
				subNodeList.add(ruleUsageProcessNode);
			}

			setSubNodes(subNodeList);
		}
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return !isAllRuleSubnode;
	}

	public boolean isAllRuleSubnode() {
		return isAllRuleSubnode;
	}

	@Override
	public void insertRule(List<RuleAndRuleUsageEntity>	ruleUsageEntityList, RuleVO ruleBefore) throws CommonBusinessException {
		try {
			//final Integer iEntityId = MasterDataDelegate.getInstance().getMetaData(Modules.getInstance().getEntityNameByModuleId(entityId)).getId();
			RuleAndRuleUsageEntity ruleToInsert = ruleUsageEntityList.get(0);
			// insert in under same parent
			if (ruleToInsert.getEventName() != null && ruleToInsert.getEntity() != null
					&& ruleToInsert.getEventName().equals(eventName)
					&& ruleToInsert.getEntity().equals(entity)
					&& LangUtils.equals(ruleToInsert.getProcessId(), processId)
					&& LangUtils.equals(ruleToInsert.getStatusId(), statusId)) {
				(RuleDelegate.getInstance()).moveRuleUsageForId(eventName, entity, new Integer(-1).equals(processId) ? null : processId, new Integer(-1).equals(statusId) ? null : statusId, ruleToInsert.getRuleVo().getId()
						, ruleBefore == null ? null : ruleBefore.getId());
			}
			else {
				(RuleDelegate.getInstance()).createRuleUsageForId(eventName, entity, new Integer(-1).equals(processId) ? null : processId, new Integer(-1).equals(statusId) ? null : statusId, ruleToInsert.getRuleVo().getId()
						, ruleBefore == null ? null : ruleBefore.getId());
			}
			refresh();
		}
		catch (CommonCreateException ex) {
			throw new CommonBusinessException(ex);
		}

	}

}	// class EntityRuleNode

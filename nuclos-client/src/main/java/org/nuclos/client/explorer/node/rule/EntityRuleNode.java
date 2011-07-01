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

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import java.util.ArrayList;
import java.util.List;

/**
 * Treenode representing an module Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class EntityRuleNode extends AbstractRuleTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final boolean isAllRuleSubnode;
	private final String entity;
	private final String eventName;

	public EntityRuleNode(String aEventName, String aEntity, String entityName, List<RuleNode> saveRules,
			boolean aIsAllRuleSubnodeFlag) {

		super(null, entityName, entityName, saveRules, RuleNodeType.ENTITY);

		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
		this.entity = aEntity;
		this.eventName = aEventName;
	}

	@Override
	public void refresh() {
		if (!isAllRuleSubnode) {
			final List<AbstractRuleTreeNode> subNodeList = new ArrayList<AbstractRuleTreeNode>();

			for (RuleVO ruleVO : RuleDelegate.getInstance().getByEventAndEntityOrdered(eventName, entity)) {
				subNodeList.add(new RuleNode(ruleVO, eventName, entity));
			}

			setSubNodes(subNodeList);
		}
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return !isAllRuleSubnode;
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
					) {
				(RuleDelegate.getInstance()).moveRuleUsageForId(eventName, entity, ruleToInsert.getRuleVo().getId()
						, ruleBefore == null ? null : ruleBefore.getId());
			}
			else {
				(RuleDelegate.getInstance()).createRuleUsageForId(eventName, entity, ruleToInsert.getRuleVo().getId()
						, ruleBefore == null ? null : ruleBefore.getId());
			}
			refresh();
		}
		catch (CommonCreateException ex) {
			throw new CommonBusinessException(ex);
		}

	}

}	// class EntityRuleNode

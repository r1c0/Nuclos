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

import org.nuclos.client.rule.TimelimitRuleDelegate;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Treenode representing the Timelimit directory node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class TimelimitNode extends AbstractRuleTreeNode {

	private boolean isInsertRuleAllowed;

	public TimelimitNode(String aLabel, String aDescription, boolean aIsInsertRuleAllowedFlag) {
		super(null, aLabel, aDescription, null, RuleNodeType.DIRECTORY);

		this.isInsertRuleAllowed = aIsInsertRuleAllowedFlag;
	}

	@Override
	public void refresh() {
		if (isInsertRuleAllowed) {
			final List<AbstractRuleTreeNode> subNodeList = new ArrayList<AbstractRuleTreeNode>();

			Collection<RuleVO> ruleColl = (TimelimitRuleDelegate.getInstance()).getAllTimelimitRules();

			for (RuleVO ruleVO : ruleColl) {
				subNodeList.add(new RuleNode(ruleVO, RuleTreeModel.FRIST_EVENT_NAME, true));
			}

			//sortNodeListByLabel(subNodeList);
			setSubNodes(subNodeList);
		}
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return isInsertRuleAllowed;
	}
}	// class TimelimitNode

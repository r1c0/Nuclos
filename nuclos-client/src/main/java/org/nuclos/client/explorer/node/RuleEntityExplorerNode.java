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
package org.nuclos.client.explorer.node;

import javax.swing.Icon;

import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode.EntityRuleUsageStatusNode;
import org.nuclos.client.explorer.node.rule.TimelimitNode;
import org.nuclos.client.ui.Icons;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a module in the rule tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public class RuleEntityExplorerNode extends AbstractRuleExplorerNode {

	public RuleEntityExplorerNode(TreeNode treenode) {
		super(treenode);
	}
	
	@Override
	public boolean isLeaf() {
		if (getTreeNode() instanceof EntityRuleUsageStatusNode)
			return ((EntityRuleUsageStatusNode)getTreeNode()).isLeaf();
		return super.isLeaf();
	}


	@Override
	public Icon getIcon() {
		final Icon result;
		switch (this.getTreeNode().getNodeType()) {
			case DIRECTORY:
				result = null;
				break;

			case ENTITY:
				result = Icons.getInstance().getIconGenericObject16();//NuclosIcons.getInstance().getIconModule();
				break;

			default:
				result = null;
				break;
		}

		return result;
	}

	@Override
	protected boolean isRefreshAvailable() {
		return (getUserObject() instanceof DirectoryRuleNode && !((DirectoryRuleNode) getUserObject()).isAllRuleSubnode())
				|| (getUserObject() instanceof TimelimitNode) && ((TimelimitNode) getUserObject()).isInsertRuleAllowd()
				|| (getUserObject() instanceof EntityRuleNode) && ((EntityRuleNode) getUserObject()).isInsertRuleAllowd();
	}

}

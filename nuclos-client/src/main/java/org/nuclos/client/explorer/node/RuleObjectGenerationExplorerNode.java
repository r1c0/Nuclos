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

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;

import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.rule.RuleGenerationNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing an object generation node.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public class RuleObjectGenerationExplorerNode extends AbstractRuleExplorerNode {

	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";

	public RuleObjectGenerationExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = null;
		switch (((RuleGenerationNode) getUserObject()).getNodeType()) {

			case AD_GENERATION:
				result = Icons.getInstance().getIconAdGeneration();
				break;

			default:
				result = null;
				break;
		}

		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ShowDetailsAction(tree));
		return result;
	}

	@Override
	protected boolean isRefreshAvailable() {
		return !((RuleGenerationNode) this.getTreeNode()).isAllRuleSubnode();
	}

	/**
	 * Shows the details the generation object in its collect controller.
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, getCommonLocaleDelegate().getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdShowDetails((ExplorerNode<?>) this.getJTree().getSelectionPath().getLastPathComponent());
		}

		/**
		 * @param explorernode
		 */
		private void cmdShowDetails(final ExplorerNode<?> explorernode) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					Main.getInstance().getMainController().showDetails(
							NuclosEntity.GENERATION.getEntityName(), explorernode.getTreeNode().getId());
				}
			});
		}
	}
}

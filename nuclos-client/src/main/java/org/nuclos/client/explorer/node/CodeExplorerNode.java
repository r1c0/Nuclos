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

import org.nuclos.client.customcode.CodeCollectController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.rule.CodeTreeNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a library rule.
 */
public class CodeExplorerNode extends AbstractRuleExplorerNode {

	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW_DETAILS";

	public CodeExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		return ((CodeTreeNode) getUserObject()).isActive() ? Icons.getInstance().getIconRuleNode() : Icons.getInstance().getIconRuleNodeDisabled();
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}

	@Override
	protected boolean isRefreshAvailable() {
		return false;
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		if (((CodeTreeNode) getUserObject()).getCodeVO() != null) {
			result.add(new ShowDetailsAction(tree));
		}

		return result;
	}

	/**
	 * Shows the details for the Rule in tis collect controller
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		public ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, getSpringLocaleDelegate().getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) tree.getSelectionPath().getLastPathComponent();
			this.cmdShowDetails(tree, (CodeExplorerNode) explorernode);
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdShowDetails(JTree tree, final CodeExplorerNode explorerNode) {
			CodeTreeNode codeNode = (CodeTreeNode) explorerNode.getTreeNode();
			CodeVO codevo = codeNode.getCodeVO();
			if (codevo != null) {
				final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
				CodeCollectController ctl = factory.newCodeCollectController(null);
				try {
					ctl.runViewSingleCollectableWithId(codevo.getId());
				} catch (CommonBusinessException e) {
					Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), e);
				}
			}
		}
	}	// inner class ShowDetailsAction
}

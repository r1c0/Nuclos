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

import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.rule.RuleNodeType;
import org.nuclos.client.explorer.node.rule.StateModelNode;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.statemodel.admin.StateModelCollectController;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * <code>ExplorerNode</code> representing a state model or transition in the rule tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */

public class RuleStateModelExplorerNode extends AbstractRuleExplorerNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";

	public RuleStateModelExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = null;
		switch (((StateModelNode) getUserObject()).getNodeType()) {

			case STATEMODEL:
				result = Icons.getInstance().getIconStateModel();
				break;

			case TRANSITION:
				result = Icons.getInstance().getIconStateTransitionExplorer();
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
	protected boolean isRefreshAvailable() {
		return !((StateModelNode) getUserObject()).isAllRuleSubnode() || ((StateModelNode) getUserObject()).getNodeType().equals(RuleNodeType.TRANSITION);
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ShowDetailsAction(tree));
		return result;
	}

	/**
	 * Shows the details for the state model in the collect controller
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) this.getJTree().getSelectionPath().getLastPathComponent();
			this.cmdShowDetails((RuleStateModelExplorerNode) explorernode);
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param explorerNode
		 */
		private void cmdShowDetails(final RuleStateModelExplorerNode explorerNode) {
			UIUtils.runCommand(getExplorerController().getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
					final StateModelVO statemodelvo = ((StateModelNode) explorerNode.getTreeNode()).getStateModelVo();
					final StateModelCollectController ctl = factory.newStateModelCollectController(MainFrame.getPredefinedEntityOpenLocation(NuclosEntity.STATEMODEL.getEntityName()), null);
					ctl.runViewSingleCollectable(ctl.readCollectable(new CollectableStateModel(statemodelvo)));
				}
			});
		}
	}
}

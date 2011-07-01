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

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.rule.RuleNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.masterdata.datatransfer.RuleCVOTransferable;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.rule.admin.CollectableRule;
import org.nuclos.client.rule.admin.RuleCollectController;
import org.nuclos.client.rule.admin.TimelimitRuleCollectController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * <code>ExplorerNode</code> representing a Rule.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */

public class RuleExplorerNode extends AbstractRuleExplorerNode {

	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";
	private static final String ACTIONCOMMAND_REMOVE_USAGE = "REMOVE_USAGE";

	public RuleExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = null;
		switch (((RuleNode) getUserObject()).getNodeType()) {
			case RULE:

				result = ((RuleNode) getUserObject()).isActive() ? Icons.getInstance().getIconRuleNode()
						: Icons.getInstance().getIconRuleNodeDisabled();
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
	public int getDataTransferSourceActions() {

		return (((RuleNode) getUserObject()).getRuleEntity() != null) ? DnDConstants.ACTION_COPY : DnDConstants.ACTION_NONE;
	}

	@Override
	public Transferable createTransferable(JTree tree) {
		final RuleNode ruleNode = (RuleNode) this.getTreeNode();
		return new RuleCVOTransferable(Collections.singletonList(ruleNode.getRuleEntity()));
	}

	@Override
	protected boolean isRefreshAvailable() {
		return ((RuleNode) this.getTreeNode()).isAllRuleSubnode();
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		if (((RuleNode) getUserObject()).getRuleVo() != null) {
			result.add(new ShowDetailsAction(tree));
		}

		RuleNode ruleNode = (RuleNode) this.getTreeNode();
		if(!ruleNode.isTimeLimitRule) {
			RuleAndRuleUsageEntity ruleUsage = (getTreeNode()).getRuleEntity();
			if (ruleUsage.getEventName() != null && ruleUsage.getRuleVo() != null) {
				result.add(new RemoveUsageAction(tree));
			}
		}
		return result;
	}

	/**
	 * Shows the details for the Rule in tis collect controller
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		public ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) tree.getSelectionPath().getLastPathComponent();
			this.cmdShowDetails(tree, (RuleExplorerNode) explorernode);
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdShowDetails(JTree tree, final RuleExplorerNode explorerNode) {
			RuleNode ruleNode = (RuleNode) explorerNode.getTreeNode();
			RuleVO rulevo = ruleNode.getRuleVo();
			if (rulevo != null) {
				if(ruleNode.isTimeLimitRule) {
					TimelimitRuleCollectController ctl = new TimelimitRuleCollectController(MainFrame.getPredefinedEntityOpenLocation(NuclosEntity.TIMELIMITRULE.getEntityName()), null);
					try {
						ctl.runViewSingleCollectableWithId(rulevo.getId());
					}
					catch(CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e);
					}
				}
				else {
					RuleCollectController ctl = new RuleCollectController(MainFrame.getPredefinedEntityOpenLocation(NuclosEntity.RULE.getEntityName()), null);
					ctl.runViewSingleCollectable(new CollectableRule(rulevo));
				}
			}
		}
	}	// inner class ShowDetailsAction

	/**
	 * Remove the selected Rule if it is a RuleUsage
	 */
	private class RemoveUsageAction extends TreeNodeAction {

		public RemoveUsageAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE_USAGE, CommonLocaleDelegate.getMessage("RuleExplorerNode.3","Verwendung l\u00f6schen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) tree.getSelectionPath().getLastPathComponent();
			this.cmdRemoveUsage(tree, (RuleExplorerNode) explorernode);
		}

		/**
		 * show the details
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdRemoveUsage(JTree tree, final RuleExplorerNode explorerNode) {
			final RuleAndRuleUsageEntity ruleUsage = ((RuleNode) explorerNode.getTreeNode()).getRuleEntity();
			if (ruleUsage != null) {
				try {
					String sMessage = CommonLocaleDelegate.getMessage("RuleExplorerNode.2","Soll die Verwendung wirklich gel\u00f6scht werden") + "?";
					final int btn = JOptionPane.showConfirmDialog(tree, sMessage, CommonLocaleDelegate.getMessage("RuleExplorerNode.4","Verwendung l\u00f6schen"), JOptionPane.YES_NO_OPTION);

					if (btn == JOptionPane.YES_OPTION) {
						RuleDelegate.getInstance().removeRuleUsage(ruleUsage.getEventName(), ruleUsage.getEntity(), ruleUsage.getRuleVo().getId());
						((ExplorerNode<?>) RuleExplorerNode.this.getParent()).refresh(tree);
					}
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(tree, ex);
				}
			}
		}
	}
}

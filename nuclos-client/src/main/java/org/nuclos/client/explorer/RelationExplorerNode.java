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
package org.nuclos.client.explorer;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.RelationTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * A node in the explorer tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class RelationExplorerNode extends ExplorerNode<RelationTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RelationExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		// general icons for relations:
		return this.getTreeNode().getDirection().isForward() ?
				Icons.getInstance().getIconRelationParentToChild() :
				Icons.getInstance().getIconRelationChildToParent();
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ShowDetailsAction(tree));
		result.add(new RemoveAction(tree));
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}

	/**
	 * inner class ShowDetailsAction. Shows the details for the coupling represented by this node.
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("RelationExplorerNode.3","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final RelationExplorerNode explorernode = (RelationExplorerNode) this.getJTree().getSelectionPath().getLastPathComponent();
			this.cmdShowDetails(explorernode);
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param explorernode
		 */
		private void cmdShowDetails(final RelationExplorerNode explorernode) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					Main.getMainController().showDetails(NuclosEntity.GENERICOBJECTRELATION.getEntityName(), explorernode.getTreeNode().getId());
				}
			});
		}
	}	// inner class ShowDetailsAction

	/**
	 * inner class RemoveAction. Removes this relation.
	 */
	private class RemoveAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param tree
		 */
		RemoveAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE, CommonLocaleDelegate.getMessage("RelationExplorerNode.1","Beziehung entfernen") + "...", tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final RelationExplorerNode explorernode = (RelationExplorerNode) tree.getSelectionPath().getLastPathComponent();
			this.cmdRemove(tree, explorernode);
		}

		/**
		 * removes the relation between this node and its parent.
		 * @param tree
		 * @param explorernode
		 */
		private void cmdRemove(final JTree tree, final RelationExplorerNode explorernode) {
			if (explorernode.getParent() instanceof GenericObjectExplorerNode) {
				final GenericObjectExplorerNode loexplorernodeParent = (GenericObjectExplorerNode) explorernode.getParent();

				final RelationTreeNode treenode = explorernode.getTreeNode();

				final GenericObjectTreeNode lotreenodeParent = loexplorernodeParent.getTreeNode();
				final GenericObjectTreeNode lotreenodeChild = (GenericObjectTreeNode) treenode.getSubNodes().get(0);

				final boolean bForward = treenode.getDirection().isForward();
				final GenericObjectTreeNode lotreenodeSource = bForward ? lotreenodeParent : lotreenodeChild;
				final GenericObjectTreeNode lotreenodeTarget = bForward ? lotreenodeChild : lotreenodeParent;

				final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectExplorerNode.4", "Soll die Beziehung von {0} zu {1} entfernt werden?", lotreenodeSource.getLabel(), lotreenodeTarget.getLabel());

				final int iBtn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, CommonLocaleDelegate.getMessage("RelationExplorerNode.2","Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
				if (iBtn == JOptionPane.OK_OPTION) {
					UIUtils.runCommand(this.getParent(), new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							final Integer iRelationId = treenode.getId();
							assert iRelationId != null;
							GenericObjectDelegate.getInstance().removeRelation(iRelationId, lotreenodeTarget.getId(), lotreenodeTarget.getModuleId());
							loexplorernodeParent.refresh(tree);
						}
					});
				}
			}
		}

	}	// inner class RemoveAction

}	// class RelationExplorerNode

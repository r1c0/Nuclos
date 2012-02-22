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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.ExplorerView;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.CompositeTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>OldMasterDataTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataExplorerNode<TN extends MasterDataTreeNode<Integer>> extends ExplorerNode<TN> {

	private static final Logger log = Logger.getLogger(MasterDataExplorerNode.class);

	public MasterDataExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(this.newShowDetailsAction(tree, false));
		result.add(this.newShowDetailsAction(tree, true));
		result.add(this.newShowListAction(tree));
		result.add(this.newRemoveAction(tree));

		// add generator actions here.
		TreeNodeAction newGeneratorAction = newGeneratorAction(tree);
		if (newGeneratorAction != null) {
			result.add(TreeNodeAction.newSeparatorAction());
			result.add(newGeneratorAction);
		}

		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return getDefaultObjectNodeAction();
	}

	protected MasterDataExplorerNode<TN>.RemoveAction newRemoveAction(JTree tree) {
		return new RemoveAction(tree, getSpringLocaleDelegate().getMessage("MasterDataExplorerNode.1", "L\u00f6schen")+ "...");
	}

	private TreeNodeAction newGeneratorAction(JTree tree) {
		final List<TreeNodeAction> lst = getGeneratorActions(tree, MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityName()).getId().intValue());
		if (lst.isEmpty()) {
			return null;
		}

		final CompositeTreeNodeAction result = new CompositeTreeNodeAction(
				getSpringLocaleDelegate().getMessage("RuleExplorerNode.5","Arbeitsschritte"), lst);
		return result;
	}

	private List<TreeNodeAction> getGeneratorActions(JTree tree, Integer iModuleId) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();

		final List<GeneratorActionVO> lstActions = GeneratorActions.getActions(iModuleId, null, null);
		if (lstActions.size() > 0) {
			for (Iterator<GeneratorActionVO> iterator = lstActions.iterator(); iterator.hasNext();) {
				GeneratorActionVO generatorActionVO = iterator.next();
				result.add(new GeneratorAction(tree, generatorActionVO, new UsageCriteria(iModuleId, null)));
			}
		}
		return result;
	}

	/**
	 * Action: remove node
	 */
	protected class RemoveAction extends TreeNodeAction {

		public RemoveAction(JTree tree, String sLabel) {
			super(ACTIONCOMMAND_REMOVE, sLabel, tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final String sName = getTreeNode().getLabel();
			final String sMessage = getSpringLocaleDelegate().getMessage("MasterDataExplorerNode.3", "Wollen Sie das Objekt \"{0}\" wirklich l\u00f6schen?", sName);
			final int iBtn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, 
					getSpringLocaleDelegate().getMessage("MasterDataExplorerNode.2", "Objekt l\u00f6schen"),
					JOptionPane.OK_CANCEL_OPTION);
			if (iBtn == JOptionPane.OK_OPTION) {
				cmdRemove();
			}
		}

		private void cmdRemove() {
			final Component parent = this.getParent();
			UIUtils.runCommand(parent, new Runnable() {
				@Override
				public void run() {
					try {
						final String sEntity = getTreeNode().getEntityName();
						final Object oId = getTreeNode().getId();
						final MasterDataVO mdvo = MasterDataDelegate.getInstance().get(sEntity, oId);
						MasterDataDelegate.getInstance().remove(sEntity, mdvo);
						final DefaultTreeModel dtm = (DefaultTreeModel) getJTree().getModel();
						final MasterDataExplorerNode<TN> explorernodeThis = MasterDataExplorerNode.this;
						if (explorernodeThis.getParent() != null) {
							dtm.removeNodeFromParent(explorernodeThis);
						}
						else if (dtm.getRoot() == explorernodeThis) {
							final ExplorerController ctlExplorer = getExplorerController();
							final ExplorerView view = ctlExplorer.getExplorerViewFor(explorernodeThis.getTreeNode());
							ctlExplorer.closeExplorerView(view);
						}
						else {
							log.warn("cmdRemove: node could not be removed from tree model.");
						}
					}
					catch (/* CommonBusiness */ Exception ex) {
						Errors.getInstance().showExceptionDialog(parent, ex);
					}
				}
			});
		}
	}	// inner class RemoveAction

	@Override
	public Icon getIcon() {
		Integer resId = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityName()).getResourceId();
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityName()).getNuclosResource();
		if(resId != null) {
			ImageIcon standardIcon = ResourceCache.getInstance().getIconResource(resId);
			return MainFrame.resizeAndCacheTabIcon(standardIcon);
		} else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return Icons.getInstance().getIconGenericObject16();
	}
}	// class MasterDataExplorerNode

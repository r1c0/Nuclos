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
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.searchfilter.SearchFilter;
import org.nuclos.client.searchfilter.SearchFilterDelegate;
import org.nuclos.client.searchfilter.SearchFilterTreeNode;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a (leased object) search filter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SearchFilterExplorerNode extends ExplorerNode<SearchFilterTreeNode> {

	private static final String ACTIONCOMMAND_REMOVEFILTER = "REMOVE_FILTER";
	private static final String ACTIONCOMMAND_SHOWINTASKPANEL = "SHOW_FILTER_IN_TASKPANEL";
	private static final String ACTIONCOMMAND_SHOW_RESULT = "SHOW RESULT";

	public SearchFilterExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		return Icons.getInstance().getIconFilter16();
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());

		TreeNodeAction actShowInTab = new ShowFilterInTaskPanelAction(tree);
		actShowInTab.setEnabled(getTreeNode().getSearchFilter().isValid());
		result.add(actShowInTab);

		result.add(new RemoveFilterAction(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(newShowListAction(tree));

		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_RESULT;
	}

	/**
	 * Action: remove filter
	 */
	private class RemoveFilterAction extends TreeNodeAction {

		RemoveFilterAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVEFILTER, CommonLocaleDelegate.getMessage("SearchFilterExplorerNode.1","Filter l\u00f6schen") + "...", tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final String sFilterName = getTreeNode().getFilterName();
			final String sMessage = CommonLocaleDelegate.getMessage("SearchFilterExplorerNode.5", "Wollen Sie den Filter \"{0}\" wirklich l\u00f6schen?", sFilterName);
			final int iBtn = JOptionPane.showConfirmDialog(this.getJTree(), sMessage, CommonLocaleDelegate.getMessage("SearchFilterExplorerNode.2","Filter l\u00f6schen"),
					JOptionPane.OK_CANCEL_OPTION);
			if (iBtn == JOptionPane.OK_OPTION) {
				cmdRemoveFilter();
			}
		}

		private void cmdRemoveFilter() {
			UIUtils.runCommand(getJTree(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					// Remove filter from task view if shown there:
					final TaskController ctlTask = getExplorerController().getTaskController();
					ctlTask.hideFilterInTaskPanel(getTreeNode().getSearchFilter());

					// Remove filter from tree:
					SearchFilterDelegate.getInstance().removeSearchFilter(getTreeNode().getSearchFilter());
					final DefaultTreeModel dtm = (DefaultTreeModel) getJTree().getModel();
					dtm.removeNodeFromParent(SearchFilterExplorerNode.this);
				}
			});
		}
	}	// inner class RemoveFilterAction

	/**
	 * Action: show filter in task panel
	 */
	private class ShowFilterInTaskPanelAction extends TreeNodeAction {

		ShowFilterInTaskPanelAction(JTree tree) {
			super(ACTIONCOMMAND_SHOWINTASKPANEL, CommonLocaleDelegate.getMessage("SearchFilterExplorerNode.3","Als Aufgabenliste anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdShowFilterInTaskPanel();
		}

		private void cmdShowFilterInTaskPanel() {
			UIUtils.runCommand(getJTree(), new Runnable() {
				@Override
				public void run() {
					final TaskController ctlTask = getExplorerController().getTaskController();
					ctlTask.cmdShowFilterInTaskPanel(getTreeNode().getSearchFilter());
				}
			});
		}
	}	// inner class ShowFilterInTaskPanelAction

	/**
	 * Action: show search result in a new internal frame
	 */
	private class ShowResultAction extends TreeNodeAction {

		ShowResultAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_RESULT, CommonLocaleDelegate.getMessage("SearchFilterExplorerNode.4","Suchergebnis anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdShowResult(this.getJTree(), (SearchFilterExplorerNode) this.getJTree().getSelectionPath().getLastPathComponent());
		}

		/**
		 * command: show the details of the entity represented by the given explorernode
		 * @param tree
		 * @param explorernode
		 */
		private void cmdShowResult(JTree tree, SearchFilterExplorerNode explorernode) {
			final SearchFilterTreeNode treenode = explorernode.getTreeNode();
			UIUtils.runCommand(tree.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					if (Modules.getInstance().isModuleEntity(treenode.getEntity())) {
						final Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(treenode.getEntity());
						final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
						newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iModuleId)).getEntity()), iModuleId, null);
						final SearchFilter searchfilter = treenode.getSearchFilter();
						ctlGenericObject.setSelectedSearchFilter(searchfilter);
						ctlGenericObject.runViewResults(searchfilter.getSearchCondition());
					}
					else {
						final MasterDataCollectController ctlMasterData = NuclosCollectControllerFactory.getInstance().
						newMasterDataCollectController(MainFrame.getPredefinedEntityOpenLocation(treenode.getEntity()), treenode.getEntity(), null);
						final SearchFilter searchfilter = treenode.getSearchFilter();
						ctlMasterData.setSelectedSearchFilter(searchfilter);
						ctlMasterData.runViewResults(searchfilter.getSearchCondition());
					}
				}
			});
		}
	}	// inner class ShowResultAction

}	// class SearchFilterExplorerNode

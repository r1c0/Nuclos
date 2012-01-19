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
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.client.datasource.admin.CollectableDataSource;
import org.nuclos.client.datasource.admin.DatasourceCollectController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.datasource.DatasourceNode;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.datatransfer.DatasourceEntity;
import org.nuclos.client.masterdata.datatransfer.DatasourceVOTransferable;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * <code>ExplorerNode</code> representing a datasource node.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public class DatasourceExplorerNode extends AbstractDatasourceExplorerNode {

	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";

	public DatasourceExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = null;
		switch (((DatasourceNode) getUserObject()).getUsage()) {
			case PARENT:
				result = Icons.getInstance().getIconDatasource();
				break;
			case USED:

				result = Icons.getInstance().getIconDatasourceUsed();
				break;
			case USING:

				result = Icons.getInstance().getIconDatasourceUsing();
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

		return DnDConstants.ACTION_COPY;
	}

	@Override
	protected boolean isRefreshPossible() {

		return ((DatasourceNode) this.getTreeNode()).getUsage().equals(DatasourceNode.DatasourceUsage.PARENT);
	}

	@Override
	public Transferable createTransferable(JTree tree) {
		final DatasourceNode sourceNode = (DatasourceNode) this.getTreeNode();
		return new DatasourceVOTransferable(new DatasourceEntity(Collections.singletonList(sourceNode.getDatasourceVo())));
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ShowDetailsAction(tree));

		return result;
	}

	@Override
	public void expandToDatasourceWithId(Integer datasourceIdToGoto, JTree jTree) {
		final DatasourceVO dataSourceVo = ((DatasourceNode) getTreeNode()).getDatasourceVo();

		if (dataSourceVo != null && dataSourceVo.getId().equals(datasourceIdToGoto)) {
			jTree.setSelectionPath(new TreePath(this.getPath()));
			jTree.scrollPathToVisible(new TreePath(this.getPath()));
		}
	}

	/**
	 * Shows the details for the datasource in its collect controller
	 */
	private static class ShowDetailsAction extends TreeNodeAction {

		public ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) tree.getSelectionPath().getLastPathComponent();
			this.cmdShowDetails(tree, (DatasourceExplorerNode) explorernode);
		}

		/**
		 * show the details
		 * @param tree
		 * @param explorerNode
		 */
		private void cmdShowDetails(final JTree tree, final DatasourceExplorerNode explorerNode) {
			final DatasourceVO datasourceVo = ((DatasourceNode) explorerNode.getTreeNode()).getDatasourceVo();
			if (datasourceVo != null) {
				UIUtils.runCommand(UIUtils.getFrameForComponent(tree), new CommonRunnable() {
					@Override
					public void run() {
						if (datasourceVo.getPermission() == DatasourceVO.PERMISSION_NONE) {
							Errors.getInstance().showExceptionDialog(tree, new NuclosBusinessException(CommonLocaleDelegate.getMessage("DatasourceExplorerNode.3", "Sie haben keine Berechtigungen auf die Datenquelle {0}", datasourceVo.getName())));
						}
						else {
							final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
							final DatasourceCollectController controller = factory.newDatasourceCollectController(
									MainFrame.getPredefinedEntityOpenLocation(NuclosEntity.DATASOURCE.getEntityName()), null);
							controller.runViewSingleCollectable(new CollectableDataSource(datasourceVo));
						}
					}
				});
			}
		}
	}

}	// class DatasourceExplorerNode

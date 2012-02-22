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
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.datasource.DatasourceReportFormularNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.report.valueobject.ReportVO;

/**
 * <code>ExplorerNode</code> representing a report in the datasource node.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */

public class DatasourceReportExplorerNode extends AbstractDatasourceExplorerNode {

	private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";

	public DatasourceReportExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = Icons.getInstance().getIconReport();

		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}

	@Override
	public int getDataTransferSourceActions() {

		return DnDConstants.ACTION_NONE;
	}

	@Override
	public Transferable createTransferable(JTree tree) {
		return null;
	}

	@Override
	protected boolean isRefreshPossible() {

		return false;
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ShowDetailsAction(tree));

		return result;
	}

	/**
	 * Shows the details for the Report
	 */
	private static class ShowDetailsAction extends TreeNodeAction {

		public ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, SpringLocaleDelegate.getInstance().getMessage(
					"RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final ExplorerNode<?> explorernode = (ExplorerNode<?>) tree.getSelectionPath().getLastPathComponent();
			this.cmdShowDetails(tree, (DatasourceReportExplorerNode) explorernode);
		}

		/**
		 *  show the details
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdShowDetails(JTree tree, final DatasourceReportExplorerNode explorerNode) {
			final ReportVO datasourceVo = ((DatasourceReportFormularNode) explorerNode.getTreeNode()).getReportVo();
			if (datasourceVo != null) {
				cmdShowDetails(NuclosEntity.REPORT.getEntityName(), explorerNode);
			}
		}

		private void cmdShowDetails(final String sEntityName, final DatasourceReportExplorerNode explorerNode) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					Main.getInstance().getMainController().showDetails(sEntityName, explorerNode.getTreeNode().getId());
				}
			});
		}

	}

}

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

import javax.swing.Icon;
import javax.swing.JTree;

import org.nuclos.client.explorer.node.datasource.AllDatasourceNode;
import org.nuclos.client.explorer.node.datasource.DatasourceNode;
import org.nuclos.client.explorer.node.datasource.DirectoryDatasourceNode;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a directory or a group in the datasource tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public class DatasourceDirectoryExplorerNode extends AbstractDatasourceExplorerNode {

	public DatasourceDirectoryExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return null;
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
	public void expandToDatasourceWithId(Integer datasourceIdToGoto, JTree jTree) throws CommonFinderException {
		if (((DirectoryDatasourceNode) getTreeNode()).isRoot()) {
			for (int i = 0; i < getChildCount(); i++) {
				final AbstractDatasourceExplorerNode childNode = (AbstractDatasourceExplorerNode) getChildAt(i);
				if (childNode.getTreeNode() instanceof AllDatasourceNode) {
					childNode.expandToDatasourceWithId(datasourceIdToGoto, jTree);
				}
			}
		}
		else if (getTreeNode() instanceof AllDatasourceNode) {
			this.refresh(jTree);
			for (int i = 0; i < getChildCount(); i++) {
				final AbstractDatasourceExplorerNode childNode = (AbstractDatasourceExplorerNode) getChildAt(i);
				if (childNode.getTreeNode() instanceof DatasourceNode
						&& ((DatasourceNode) childNode.getTreeNode()).getDatasourceVo() != null
						&& ((DatasourceNode) childNode.getTreeNode()).getDatasourceVo().getId().equals(datasourceIdToGoto)) {
					childNode.expandToDatasourceWithId(datasourceIdToGoto, jTree);
				}
			}
		}
	}

}	// class DatasourceDirectoryExplorerNode

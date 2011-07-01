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

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;

import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a node in the datasourcetree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public abstract class AbstractDatasourceExplorerNode extends ExplorerNode<TreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractDatasourceExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	/**
	 * supports the current node the refresh action
	 */
	protected boolean isRefreshPossible() {
		return true;
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();

		if (isRefreshPossible()) {
			result.add(new RefreshAction(tree));
			result.addAll(this.getExpandCollapseActions(tree));
		}

		return result;
	}

	/**
	 * expand the tree to this node, if it or its child contains the given datasourceIdToGoto
	 * @param datasourceIdToGoto
	 * @param jTree
	 */
	public void expandToDatasourceWithId(Integer datasourceIdToGoto, JTree jTree) throws CommonFinderException {
	}

}	// class AbstractDatasourceExplorerNode

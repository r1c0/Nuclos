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
package org.nuclos.client.ui.tree;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Utility methods for trees.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class TreeUtils {

	private TreeUtils() {
	}

	/**
	 * expands all nodes in the <code>tree</code>
	 * @param tree
	 * @precondition tree != null
	 */
	public static void expandAllNodes(JTree tree) {
		expandAllNodes(tree, (TreeNode) tree.getModel().getRoot());
	}

	/**
	 * expands all nodes in the <code>tree</code> that descend from <code>node</code>
	 * @param tree
	 * @param node
	 */
	private static void expandAllNodes(JTree tree, TreeNode node) {
		final TreePath treepath = new TreePath(((DefaultMutableTreeNode) node).getPath());
		tree.expandPath(treepath);

		for (int i = 0; i < node.getChildCount(); i++) {
			expandAllNodes(tree, node.getChildAt(i));
		}
	}

}	// class TreeUtils

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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.nuclos.server.navigation.treenode.TreeNode;

public class DefaultKeyListener extends KeyAdapter {

	private final JTree tree;

	public DefaultKeyListener(JTree tree) {
		super();
		this.tree = tree;
	}

	@Override
	public void keyTyped(KeyEvent ev) {
		final TreePath treepath = tree.getSelectionPath();
		final ExplorerNode<? extends TreeNode> node = (ExplorerNode<?>) treepath.getLastPathComponent();
		final JTree tree = (JTree) ev.getComponent();

		final TreePath[] aSelectionPaths = tree.getSelectionPaths();
		if (aSelectionPaths == null || !Arrays.asList(aSelectionPaths).contains(treepath)) {
			// select it (and unselect all others):
			tree.setSelectionPath(treepath);
		}

		assert tree.getSelectionCount() >= 1;
		if (tree.getSelectionCount() == 1) {
			node.handleKeyEvent(tree, ev);
		} else {
			// TODO "Sammelbearbeitung"
			/*for (ExplorerNode<?> exNode : getSelectedExplorerNodes(tree)) {
				exNode.handleKeyEvent(tree, ev);
			}*/
		}
	}


}

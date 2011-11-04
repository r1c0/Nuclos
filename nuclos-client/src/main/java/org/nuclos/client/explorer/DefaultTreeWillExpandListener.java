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

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.UIUtils;

public class DefaultTreeWillExpandListener implements TreeWillExpandListener {

	private static final Logger LOG = Logger.getLogger(DefaultTreeWillExpandListener.class);
	
	private final JTree tree;

	public DefaultTreeWillExpandListener(JTree tree) {
		super();
		this.tree = tree;
	}

	@Override
    public void treeWillExpand(final TreeExpansionEvent ev) {
		UIUtils.setWaitCursor(tree);
		UIUtils.runCommand(tree, new Runnable() {
			@Override
            public void run() {
				try {
					final ExplorerNode<?> explorernode = (ExplorerNode<?>) ev.getPath().getLastPathComponent();
					//NUCLEUSINT-1129
					//final TreeNode treenode = explorernode.getTreeNode();
					final boolean bSubNodesHaventBeenLoaded = !(explorernode.getChildCount() > 0); //(treenode.hasSubNodes() == null);
	
					if (bSubNodesHaventBeenLoaded) {
						//explorernode.unloadChildren();
						explorernode.loadChildren(true);
	
						for (int i = 0; i < explorernode.getChildCount(); i++) {
							final ExplorerNode<?> explorernodeChild = (ExplorerNode<?>) explorernode.getChildAt(i);
							ExplorerController.expandAllLoadedNodes(tree, ev.getPath().pathByAddingChild(explorernodeChild));
						}
					}
				}
				catch (Exception e) {
					LOG.error("DefaultTreeWillExpandListener.treeWillExpand: " + e, e);
				}            		
			}
		});
		tree.setCursor(null);
	}

	@Override
    public void treeWillCollapse(TreeExpansionEvent ev) {
		// do nothing
	}
}

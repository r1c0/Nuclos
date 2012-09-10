package org.nuclos.client.explorer.node.eventsupport;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.ui.UIUtils;

public class EventSupportTreeWillExpandListener implements
		TreeWillExpandListener {

private static final Logger LOG = Logger.getLogger(EventSupportTreeWillExpandListener.class);
	
	private final JTree tree;

	public EventSupportTreeWillExpandListener(JTree tree) {
		super();
		this.tree = tree;
	}
	
	@Override
	public void treeWillExpand(final TreeExpansionEvent event)
			throws ExpandVetoException {
		UIUtils.setWaitCursor(tree);
		UIUtils.runCommand(tree, new Runnable() {
			@Override
            public void run() {
				try {
					final ExplorerNode<?> explorernode = (ExplorerNode<?>) event.getPath().getLastPathComponent();
					//NUCLEUSINT-1129
					//final TreeNode treenode = explorernode.getTreeNode();
					final boolean bSubNodesHaventBeenLoaded = !(explorernode.getChildCount() > 0); //(treenode.hasSubNodes() == null);
	
					if (bSubNodesHaventBeenLoaded) {
						//explorernode.unloadChildren();
						explorernode.loadChildren(true);
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
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {}

}

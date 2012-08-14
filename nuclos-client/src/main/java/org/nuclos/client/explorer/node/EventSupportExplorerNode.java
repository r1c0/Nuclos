package org.nuclos.client.explorer.node;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.eventsupport.EventSupportTransferable;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.tree.ChainedTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportExplorerNode extends ExplorerNode<EventSupportTreeNode> {

	public EventSupportExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {
		return true;
	}
	
	
	@Override
	public boolean isLeaf() {
		return ((EventSupportTreeNode)getTreeNode()).getSubNodes().isEmpty();
	}
	
	@Override
	public int getDataTransferSourceActions() {

		return DnDConstants.ACTION_COPY_OR_MOVE;
	}
	
	
	@Override
	public boolean getAllowsChildren() {
		return isLeaf() ? false : super.getAllowsChildren();
	}
	
	@Override
	public Icon getIcon() {

		Icon result = null;
		
		EventSupportTargetType treeNodeType = ((EventSupportTreeNode) getUserObject()).getTreeNodeType();
		
		if (treeNodeType == null)
			 return null;
		
		
		switch (treeNodeType) 
		{
		case EVENTSUPPORT:
			result = Icons.getInstance().getIconRuleUsage16();
			break;
		case RULE:
			result = Icons.getInstance().getIconRuleUsage16();
			break;
		case STATE_TRANSITION:
			result = Icons.getInstance().getIconState();
			break;
		case ENTITY:
			result = Icons.getInstance().getIconGenericObject16();
			break;
		default:
			break;
		}

		return result;
	}
	
	@Override
	public Action getTreeNodeActionOnMouseClick(JTree tree) {
		return new EventSupportShowPropertyAction(tree);
	}
	
	private class EventSupportShowPropertyAction extends AbstractAction
	{
		JTree tree;
		
		public EventSupportShowPropertyAction(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// selected tree element			
			final EventSupportExplorerNode node= (EventSupportExplorerNode) tree.getSelectionPath().getLastPathComponent();
			// show infos and properties for this node
			EventSupportManagementController controller = node.getTreeNode().getController();
			controller.showSupportProperties(node.getTreeNode());
		}
	}
	
	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 * @throws CommonFinderException if the object presented by this node no longer exists.
	 */
	public void refresh(final JTree tree, boolean fullRefreshCurrent) throws CommonFinderException {
	
		DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
		unloadChildren();

		this.getTreeNode().refresh();
		loadChildren(true);
		dtm.nodeStructureChanged(this);
		
	}
}

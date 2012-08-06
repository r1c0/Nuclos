package org.nuclos.client.explorer.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTree;

import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.ui.Icons;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTargetExplorerNode extends ExplorerNode<EventSupportTreeNode> {
	
	public EventSupportTargetExplorerNode(TreeNode node)
	{
		super(node);
	}
	
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {
		return true;
	}
		
	@Override
	public boolean isLeaf() {
		if (getTreeNode() instanceof EventSupportTargetTreeNode)
			return ((EventSupportTargetTreeNode)getTreeNode()).isLeaf();
		return super.isLeaf();
	}
	
	@Override
	public int getDataTransferSourceActions() {

		return DnDConstants.ACTION_COPY_OR_MOVE;
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
		return new EventSupportTargetShowPropertyAction(tree);
	}
	
	private class EventSupportTargetShowPropertyAction extends AbstractAction
	{
		JTree tree;
		
		public EventSupportTargetShowPropertyAction(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// selected tree element			
			final EventSupportTargetExplorerNode node= (EventSupportTargetExplorerNode) tree.getSelectionPath().getLastPathComponent();
			
			// show infos and properties for this node
			EventSupportManagementController controller = node.getTreeNode().getController();
			controller.showTargetSupportProperties(node.getTreeNode());
		}
	}
	

}

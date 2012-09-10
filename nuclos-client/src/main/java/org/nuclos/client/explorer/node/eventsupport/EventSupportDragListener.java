package org.nuclos.client.explorer.node.eventsupport;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.nuclos.client.eventsupport.EventSupportDataFlavor;
import org.nuclos.client.eventsupport.EventSupportExplorerNodeTransferable;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.EventSupportExplorerNode;

public class EventSupportDragListener implements DragGestureListener {

	final List<DataFlavor> dataFlavs = new ArrayList<DataFlavor>();

	EventSupportExplorerNode transferObject;
	 
	public EventSupportDragListener() {
		dataFlavs.add(new EventSupportDataFlavor());
	}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		Component component = dge.getComponent();
		if (component instanceof JTree) {
			final JTree tree = (JTree) component;
			EventSupportExplorerNode explorerNode = (EventSupportExplorerNode) this.getSelectedTreeNode(tree);
			EventSupportTreeNode treeNode = explorerNode.getTreeNode();
			if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()))
			{
				dge.startDrag(null, new EventSupportExplorerNodeTransferable(explorerNode));				
			}
		}
	}


	private ExplorerNode<?> getSelectedTreeNode(final JTree tree) {
		TreePath treePath = tree.getSelectionPath();
		return (treePath == null) ? null : (ExplorerNode<?>) treePath.getLastPathComponent();
	}
}

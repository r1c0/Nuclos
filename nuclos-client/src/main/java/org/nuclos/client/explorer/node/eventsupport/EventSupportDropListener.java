package org.nuclos.client.explorer.node.eventsupport;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.JTree.DropLocation;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.EventSupportDataFlavor;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.EventSupportExplorerNode;
import org.nuclos.client.explorer.node.EventSupportTargetExplorerNode;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;

public class EventSupportDropListener implements DropTargetListener {

	private static final Logger LOG = Logger.getLogger(EventSupportDropListener.class);
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {		
	}

	void dropTargetDrag(DropTargetDragEvent ev) {
		try {
			// Target Tree
			JTree tree = (JTree) ((DropTarget)ev.getSource()).getComponent();
			// Transfer Source
			EventSupportExplorerNode esenSource = (EventSupportExplorerNode) 
					ev.getTransferable().getTransferData(new EventSupportDataFlavor());
			
			Point p = ev.getLocation();		
			int row = tree.getClosestRowForLocation(p.x, p.y);
			tree.setSelectionRow(row);
			
			EventSupportTargetExplorerNode node = (EventSupportTargetExplorerNode) getSelectedTreeNode(tree);
			 
			if (node != null && node.getTreeNode() != null && node.getTreeNode().getTreeNodeType() != null)
			{
				EventSupportTreeNode treeNode = esenSource.getTreeNode();
				
				switch (node.getTreeNode().getTreeNodeType()) {
				case ENTITY:
					if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()) && 
						!"org.nuclos.api.eventsupport.StateChangeSupport".equals(treeNode.getParentNode().getEntityName()))
					{	
						ev.acceptDrag(ev.getDropAction());
					}
					else
					{
						ev.rejectDrag();
					}
					break;
				case STATE_TRANSITION:
					if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()) && 
					 "org.nuclos.api.eventsupport.StateChangeSupport".equals(treeNode.getParentNode().getEntityName()))
						{	
							ev.acceptDrag(ev.getDropAction());
						}
						else
						{
							ev.rejectDrag();
						}
					break;
				default:
					ev.rejectDrag();
					break;
				}
			}
			else
			{
				ev.rejectDrag();
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			ev.rejectDrag();
		}
    }
	
	@Override
	public void drop(DropTargetDropEvent dtde) {
		
		boolean success = false;
		
		dtde.acceptDrop(dtde.getDropAction());
		
		try {
			
			DropTarget targetNode = (DropTarget) dtde.getSource();
			
			EventSupportTargetExplorerNode esenTarget = (EventSupportTargetExplorerNode) getSelectedTreeNode((JTree) targetNode.getComponent());
						
			// Get dragged Node
			EventSupportExplorerNode esenSource = (EventSupportExplorerNode) 
					dtde.getTransferable().getTransferData(new EventSupportDataFlavor());
			// and attach it to the target entity
			EventSupportEventVO addedEseVO = esenSource.getTreeNode().getController().addEventSupportToEntity(esenSource.getTreeNode(), esenTarget.getTreeNode());
			
			success = addedEseVO != null;
			
			if (success)
			{
				EventSupportRepository.getInstance().updateEventSupports();
			
				EventSupportTargetTreeNode treeNode = (EventSupportTargetTreeNode) esenTarget.getTreeNode();
				treeNode.setLstSubNodes(treeNode.getController().createTargetSubNodesByType(treeNode));
				esenTarget.refresh((JTree) targetNode.getComponent(), true);
			}
			
			dtde.dropComplete(success);
		
		} catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
		}		
	}

	private ExplorerNode<?> getSelectedTreeNode(final JTree tree) {
		TreePath treePath = tree.getSelectionPath();
		return (treePath == null) ? null : (ExplorerNode<?>) treePath.getLastPathComponent();
	}
	
}

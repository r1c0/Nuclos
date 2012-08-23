package org.nuclos.client.explorer.node.eventsupport;

import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.EventSupportDataFlavor;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.EventSupportExplorerNode;
import org.nuclos.client.explorer.node.EventSupportTargetExplorerNode;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;

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
						!"org.nuclos.api.eventsupport.StateChangeSupport".equals(treeNode.getParentNode().getEntityName()) && 
						!"org.nuclos.api.eventsupport.StateChangeFinalSupport".equals(treeNode.getParentNode().getEntityName()))
					{	
						ev.acceptDrag(ev.getDropAction());
					}
					else
					{
						ev.rejectDrag();
					}
					break;
				case STATEMODEL:
					if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()) && (
					 "org.nuclos.api.eventsupport.StateChangeSupport".equals(treeNode.getParentNode().getEntityName()) || 
					 "org.nuclos.api.eventsupport.StateChangeFinalSupport".equals(treeNode.getParentNode().getEntityName())))
						{	
							ev.acceptDrag(ev.getDropAction());
						}
						else
						{
							ev.rejectDrag();
						}
					break;
				case JOB:
					if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()) && 
							 "org.nuclos.api.eventsupport.TimelimitSupport".equals(treeNode.getParentNode().getEntityName()))
								{	
									ev.acceptDrag(ev.getDropAction());
								}
								else
								{
									ev.rejectDrag();
								}
					break;
				case GENERATION:
					if (EventSupportTargetType.EVENTSUPPORT.equals(treeNode.getTreeNodeType()) && (
							 "org.nuclos.api.eventsupport.GenerateSupport".equals(treeNode.getParentNode().getEntityName()) || 
							 "org.nuclos.api.eventsupport.GenerateFinalSupport".equals(treeNode.getParentNode().getEntityName())))
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
			
			if (esenTarget.getTreeNode().getTreeNodeType().equals(EventSupportTargetType.ENTITY)) {
				// and attach it to the target entity
				EventSupportEventVO addedEseVO = esenSource.getTreeNode().getController().addEventSupportToEntity(esenSource.getTreeNode(), esenTarget.getTreeNode());				
				success = addedEseVO != null;
			}
			else if(esenTarget.getTreeNode().getTreeNodeType().equals(EventSupportTargetType.STATEMODEL)) {
				// and attach it to the selected statetransition
				EventSupportTransitionVO addedEstVO = esenSource.getTreeNode().getController().addEventSupportToStateTransition(esenSource.getTreeNode(), esenTarget.getTreeNode());
				success = addedEstVO != null;
			}
			else if(esenTarget.getTreeNode().getTreeNodeType().equals(EventSupportTargetType.JOB)) {
				// and attach it to the selected jobcontroller
				EventSupportJobVO addedEstVO = esenSource.getTreeNode().getController().addEventSupportToJob(esenSource.getTreeNode(), esenTarget.getTreeNode());
				success = addedEstVO != null;
			}
			else if(esenTarget.getTreeNode().getTreeNodeType().equals(EventSupportTargetType.GENERATION)) {
				// and attach it to the selected jobcontroller
				EventSupportGenerationVO addedEstVO = esenSource.getTreeNode().getController().addEventSupportToGeneration(esenSource.getTreeNode(), esenTarget.getTreeNode());
				success = addedEstVO != null;
			}
			
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

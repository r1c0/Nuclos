package org.nuclos.client.explorer.node.eventsupport;

import org.nuclos.client.eventsupport.EventSupportManagementController;

public class EventSupportTargetTreeNode extends EventSupportTreeNode {


	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,EventSupportTargetTreeNode parent, 
			Object id, String name, String label, String description,
			EventSupportTargetType type, boolean isRoot, boolean isLeaf, String sSearchString) {
		super(ctrl, parent, id, name, label, description, type, isRoot, sSearchString);
		
	}

	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,EventSupportTargetTreeNode parent, 
			Object id, String name, String label, String description,
			EventSupportTargetType type, boolean isLeaf, String sSearchString) {
		super(ctrl, parent, id, name, label, description, type, false, sSearchString);
	}
	
	@Override
	public void refresh(){
		if (!isLeaf())
			setLstSubNodes(getController().createTargetSubNodesByType(this, getSearchString()));
	}
	
	public boolean isLeaf() {
		boolean retVal = EventSupportTargetType.EVENTSUPPORT_TYPE.equals(getTreeNodeType()) || 
						 EventSupportTargetType.STATE_TRANSITION.equals(getTreeNodeType());
		return retVal;
	}
	
}

package org.nuclos.client.explorer.node.eventsupport;

import org.nuclos.client.eventsupport.EventSupportManagementController;

public class EventSupportTargetTreeNode extends EventSupportTreeNode {


	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,EventSupportTargetTreeNode parent, 
			Object id, String name, String label, String description,
			EventSupportTargetType type, boolean isRoot) {
		super(ctrl, parent, id, name, label, description, type, isRoot);
		
	}

	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,EventSupportTargetTreeNode parent, 
			Object id, String name, String label, String description,
			EventSupportTargetType type) {
		super(ctrl, parent, id, name, label, description, type, false);
	}
	
	@Override
	public void refresh(){
		if (!isLeaf())
			setLstSubNodes(getController().createTargetSubNodesByType(this));
	}
}

package org.nuclos.client.explorer.node.eventsupport;

import org.nuclos.client.eventsupport.EventSupportManagementController;

public class EventSupportTargetTreeNode extends EventSupportTreeNode {

	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,
			Object id, String name, String label, String description,
			EventSupportTargetType type, boolean isRoot) {
		super(ctrl, id, name, label, description, type, isRoot);
	}

	public EventSupportTargetTreeNode(EventSupportManagementController ctrl,
			Object id, String name, String label, String description,
			EventSupportTargetType type) {
		super(ctrl, id, name, label, description, type, false);
	}
	
	@Override
	public void refresh(){
		if (!isLeaf())
			setLstSubNodes(getController().createSubNodesByTargetType(this));
	}

	
}

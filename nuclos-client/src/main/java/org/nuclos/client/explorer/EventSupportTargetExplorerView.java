package org.nuclos.client.explorer;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JTree;


import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.explorer.node.eventsupport.EventSupportDropListener;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTargetExplorerView  extends EventSupportExplorerView {

	public EventSupportTargetExplorerView(TreeNode tn, Map<ACTIONS, AbstractAction> actions) {
		super(tn, actions);
	}
	
	protected void addDNDFunctionality(JTree pTree) {
		EventSupportDropListener dndListener = new EventSupportDropListener();
		DropTarget drpTaget = new DropTarget(pTree, 
		        DnDConstants.ACTION_MOVE, dndListener);
	}
	
	protected void addToolBarComponents(JToolBar pToolbar) {	
		if (getActions().containsKey(ACTIONS.ACTION_REFRESH_TARGETTREE))
			pToolbar.add(new JButton(getActions().get(ACTIONS.ACTION_REFRESH_TARGETTREE)));
	}
}

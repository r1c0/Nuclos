package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportGenerationPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportJobPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportSourcePropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;

public class EventSupportView extends JPanel {

	EventSupportTreeNode 		treeEventSupports;
	EventSupportTargetTreeNode  treeEventSupportTargets;
	
	Map<ACTIONS, AbstractAction> actionMap;
	
	JSplitPane splitpn;
	
	public EventSupportView(EventSupportTreeNode pTreeEventSupports, EventSupportTargetTreeNode pTreeEventSupportTargets)
	{
		super(new BorderLayout());

		this.treeEventSupports = pTreeEventSupports;
		this.treeEventSupportTargets = pTreeEventSupportTargets;
	}

	public void setActionMap(Map<ACTIONS, AbstractAction> acts) {
		this.actionMap = acts;
	}
	
	public Map<ACTIONS, AbstractAction> getActionsMap() {
		return actionMap;
	}

	public void showGui() {
		
		splitpn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
		Border b1 = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY);
		Border b2 = BorderFactory.createMatteBorder(0, 1, 1, 0, Color.LIGHT_GRAY);
		
		splitpn.setTopComponent(
				new EventSupportSourceView(this, b1));
		splitpn.setBottomComponent(
				new EventSupportTargetView(this, b2));
		
		splitpn.setResizeWeight(0.5d);
		splitpn.setDividerSize(5);
		this.add(splitpn, BorderLayout.CENTER);
	}

	public EventSupportSourceView getSourceViewPanel()
	{
		return (EventSupportSourceView) splitpn.getTopComponent();
	}
	
	public EventSupportTargetView getTargetViewPanel()
	{
		return (EventSupportTargetView) splitpn.getBottomComponent();
	}
	
	public EventSupportTreeNode getTreeEventSupports() {
		return treeEventSupports;
	}

	public EventSupportTargetTreeNode getTreeEventSupportTargets() {
		return treeEventSupportTargets;
	}
}

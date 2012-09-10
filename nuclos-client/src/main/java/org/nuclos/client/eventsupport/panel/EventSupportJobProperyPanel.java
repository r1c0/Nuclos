package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.AbstractAction;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportJobPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;

public class EventSupportJobProperyPanel extends AbstractEventSupportPropertyPanel {

	private EventSupportJobPropertiesTableModel model;
	private Map<ACTIONS, AbstractAction> actionMapping;
	
	public EventSupportJobProperyPanel(Map<ACTIONS, AbstractAction> pActionMapping) {

		this.model = new EventSupportJobPropertiesTableModel();
		this.actionMapping = pActionMapping;
		
		setLayout(new BorderLayout());
		
		createPropertiesTable();		
	}
	
	@Override
	protected EventSupportPropertiesTableModel getPropertyModel() {
		return this.model;
	}

	@Override
	public Map<ACTIONS, AbstractAction> getActionMapping() {
		return this.actionMapping;
	}

	@Override
	public ActionToolBar[] getActionToolbarMapping() {
		return new ActionToolBar[] {
				new ActionToolBar(ACTIONS.ACTION_DELETE_JOB, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_UP_JOB, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_DOWN_JOB, true),
		};
	}

}

package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;

public class EventSupportStatePropertyPanel extends AbstractEventSupportPropertyPanel {
	
	private EventSupportStatePropertiesTableModel model;
	private Map<ACTIONS, AbstractAction> actionMapping;
	
	public EventSupportStatePropertyPanel(Map<ACTIONS, AbstractAction> pActionMapping) {

		this.model = new EventSupportStatePropertiesTableModel();
		this.actionMapping = pActionMapping;
		
		setLayout(new BorderLayout());
		
		createPropertiesTable();		
	}

	public void reloadTransitions() {
		getPropertyTable().getColumnModel().getColumn(1).setCellEditor(
				new DefaultCellEditor(new JComboBox(model.getTransitionsAsArray())));
	}
	
	@Override
	public EventSupportPropertiesTableModel getPropertyModel() {
		return model;
	}

	@Override
	public Map<ACTIONS, AbstractAction> getActionMapping() {
		return this.actionMapping;
	}

	@Override
	protected ActionToolBar[] getActionToolbarMapping() {
		return new ActionToolBar[] {
				new ActionToolBar(ACTIONS.ACTION_DELETE_STATETRANSITION, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_UP_STATETRANSITION, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_DOWN_STATETRANSITION, true) };
	}

}

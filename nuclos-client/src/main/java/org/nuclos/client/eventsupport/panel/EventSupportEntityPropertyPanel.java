package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.border.Border;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;

public class EventSupportEntityPropertyPanel extends AbstractEventSupportPropertyPanel {

	private EventSupportEntityPropertiesTableModel model;
	private Map<ACTIONS, AbstractAction> actionMapping;
	
	public EventSupportEntityPropertyPanel(Map<ACTIONS, AbstractAction> pActionMapping) {

		this.model = new EventSupportEntityPropertiesTableModel();
		this.actionMapping = pActionMapping;
		
		setLayout(new BorderLayout());
		
		createPropertiesTable();	
		
		TableColumnModel colModel = getPropertyTable().getColumnModel();
		colModel.getColumn(0).setMaxWidth(220);
		colModel.getColumn(0).setMinWidth(20);
		colModel.getColumn(0).setPreferredWidth(200);
		
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
				new ActionToolBar(ACTIONS.ACTION_DELETE_EVENT, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_UP_EVENT, true),
				new ActionToolBar(ACTIONS.ACTION_MOVE_DOWN_EVENT, true) };
	}

	public void reloadValueLists() {
		TableColumnModel colModel = getPropertyTable().getColumnModel();
		
		colModel.getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(this.model.getStatusAsArray())));
		colModel.getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(this.model.getProcessAsArray())));
	}
}

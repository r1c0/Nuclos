package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.nuclos.client.eventsupport.model.EventSupportSourcePropertiesTableModel;

public class EventSupportSourcePropertyPanel extends JPanel{

	private EventSupportSourcePropertiesTableModel model;
	JTable propertyTable;
		
	public EventSupportSourcePropertyPanel() {

		this.model = new EventSupportSourcePropertiesTableModel();
		
		setLayout(new BorderLayout());
		
		createPropertiesTable();		
	}

	protected EventSupportSourcePropertiesTableModel getPropertyModel() {
		return this.model;
	}
	
	protected void createPropertiesTable() {
		
		final JTable table = new JTable(getPropertyModel());
		
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(true);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY));
		add(scrollPane, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(0, 250));
		
		this.propertyTable = table;
	}

	public JTable getPropertyTable() {
		return this.propertyTable;
	}
	
}

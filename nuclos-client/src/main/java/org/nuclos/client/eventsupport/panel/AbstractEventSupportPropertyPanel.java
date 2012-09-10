package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTableDragListener;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTableDropListener;

public abstract class AbstractEventSupportPropertyPanel extends JPanel {

	protected abstract EventSupportPropertiesTableModel getPropertyModel();
	protected abstract Map<ACTIONS, AbstractAction> getActionMapping();
	protected abstract ActionToolBar[] getActionToolbarMapping();
	
	JTable propertyTable;
	
	protected void createPropertiesTable() {
		
		final JTable table = new JTable(getPropertyModel());
		final List<JButton> lstOfEnablabeTBButtons = new ArrayList<JButton>();
		final JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
		Map<ACTIONS, AbstractAction> actionsMap = getActionMapping();
		
		toolBar.setBorderPainted(false);	
		
		for (ActionToolBar actionInMap: getActionToolbarMapping()) {
			final JButton btnActionElement = new JButton(actionsMap.get(actionInMap.getAction()));
			if (actionInMap.isEditableInToolbar()) {
				btnActionElement.setEnabled(false);
				lstOfEnablabeTBButtons.add(btnActionElement);
			}
			toolBar.add(btnActionElement);
		}
		
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(true);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) {
					boolean enableButtons = false;
					if (table.getSelectedRow() >= 0) {
						enableButtons = true;
					}
					for (JButton tbButton: lstOfEnablabeTBButtons) {
						toolBar.getComponent(toolBar.getComponentIndex(tbButton)).setEnabled(enableButtons);
					}
				}
			}
		});
		
		// Add Drag functions for chaning the order
		EventSupportTargetTableDragListener dndListener = new EventSupportTargetTableDragListener();
		DragSource src = new DragSource();
		DragGestureRecognizer gestRec = src.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_MOVE, dndListener);	
		
		// Add Drop functions for chaning the order
		EventSupportTargetTableDropListener dndDropListener = new EventSupportTargetTableDropListener();
		DropTarget drpTaget = new DropTarget(table, DnDConstants.ACTION_MOVE, dndDropListener);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY));
		add(scrollPane, BorderLayout.CENTER);
		
		if (getActionToolbarMapping().length > 0)
			add(toolBar, BorderLayout.EAST);
		
		setPreferredSize(new Dimension(0, 250));
		
		this.propertyTable = table;
	}

	public JTable getPropertyTable() {
		return this.propertyTable;
	}
	
	public class ActionToolBar {
		
		private ACTIONS aAction;
		private boolean bEditableInToolbar;
		
		public ActionToolBar (ACTIONS pAction, boolean pEditableInToolbar) {
			this.aAction = pAction;
			this.bEditableInToolbar = pEditableInToolbar;
		}
		
		public ACTIONS getAction() {
			return this.aAction;
		}
		
		public boolean isEditableInToolbar() {
			return this.bEditableInToolbar;
		}
	}

}

package org.nuclos.client.explorer.node.eventsupport;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;

public class EventSupportTargetTableDropListener implements DropTargetListener {

	private static final Logger LOG = Logger.getLogger(EventSupportTargetTableDropListener.class);
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		dropTargetDrag(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {}

	@Override
	public void drop(DropTargetDropEvent ev) {

		JTable table = (JTable) ((DropTarget)ev.getSource()).getComponent();
		EventSupportPropertiesTableModel model = (EventSupportPropertiesTableModel) table.getModel();
		
		// Dropped at element of table
		int iTargetRow = table.rowAtPoint(ev.getLocation());
		
		// Switch rows
		try {
			model.switchRows(table.getSelectedRow(), iTargetRow);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	void dropTargetDrag(DropTargetDragEvent ev) {
	}
}

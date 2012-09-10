package org.nuclos.client.explorer.node.eventsupport;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.nuclos.client.eventsupport.EventSupportTargetTableTransferable;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportTargetTableDragListener implements DragGestureListener {

	final List<DataFlavor> dataFlavs = new ArrayList<DataFlavor>();
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		Component component = dge.getComponent();
		if (component instanceof JTable) {
			final JTable table = (JTable) component;
			EventSupportPropertiesTableModel model = (EventSupportPropertiesTableModel) table.getModel();
			
			EventSupportVO eventSupportVO = (EventSupportVO) model.getEntries().get(table.getSelectedRow());
			
			dge.startDrag(null, new EventSupportTargetTableTransferable(eventSupportVO));
		}
	}
}

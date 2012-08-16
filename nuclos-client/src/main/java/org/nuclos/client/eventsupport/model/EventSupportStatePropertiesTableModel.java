package org.nuclos.client.eventsupport.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;

public class EventSupportStatePropertiesTableModel extends AbstractTableModel {

	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_ORDER = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.1","Reihenfolge");
	
	static final String[] COLUMNS = new String[] {COL_ORDER, COL_EVENTSUPPORT};
	
	final List<EventSupportTransitionVO> entries = new ArrayList<EventSupportTransitionVO>();
	boolean isModelModified;
			
	public void addEntry(EventSupportTransitionVO eseVO)
	{
		entries.add(eseVO);
		fireTableRowsInserted(entries.size(), entries.size());
	}
	
	public void clear()
	{
		if (!entries.isEmpty())
		{
			int curSizeOfList = entries.size();
			entries.clear();
			fireTableRowsDeleted(0, curSizeOfList);
		}
	}
	
	public boolean isModelModified() {
		return isModelModified;
	}

	public void setModelModified(boolean isModelModified) {
		this.isModelModified = isModelModified;
	}

	
	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	
	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		EventSupportTransitionVO eseVO = entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			retVal = eseVO.getOrder();
			break;
		case 1:
			retVal = eseVO.getEventSupportClass();
			break;
		default:
			break;
		}
		return retVal;
	}
	
	public void moveUp(int selectedRow) {
		if (selectedRow != 0) {
			EventSupportTransitionVO eseVO = entries.get(selectedRow);
			EventSupportTransitionVO prevEseVO = entries.get(selectedRow - 1);
			
			entries.remove(selectedRow);
			entries.remove(selectedRow-1);
			
			eseVO.setOrder(eseVO.getOrder() - 1);
			prevEseVO.setOrder(prevEseVO.getOrder() + 1);
			
			entries.add(eseVO.getOrder() -1, eseVO);
			entries.add(prevEseVO.getOrder() -1, prevEseVO);
			
			setModelModified(true);
		}
	}
	
	public EventSupportTransitionVO getEntryByRowIndex(int id) {
		EventSupportTransitionVO retVal = null;
		if (id < entries.size())
			retVal = entries.get(id);
		
		return retVal;
	}
	
	public void moveDown(int selectedRow) {
		if (selectedRow < entries.size() - 1) {
			EventSupportTransitionVO eseVO = entries.get(selectedRow);
			EventSupportTransitionVO forwEseVO = entries.get(selectedRow + 1);
			
			eseVO.setOrder(eseVO.getOrder() + 1);
			forwEseVO.setOrder(forwEseVO.getOrder() - 1);
			
			setModelModified(true);
		}
	}
	
	public void removeEntry(int row) {
		if (row < entries.size()) {
			entries.remove(row);
		}
		
		for (int i=row; i <entries.size();i++) {
			EventSupportTransitionVO eventSupportEventVO = entries.get(i);
			eventSupportEventVO.setOrder(eventSupportEventVO.getOrder() - 1 );
		}
		
	}
	
}

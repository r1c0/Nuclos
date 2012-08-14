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
}

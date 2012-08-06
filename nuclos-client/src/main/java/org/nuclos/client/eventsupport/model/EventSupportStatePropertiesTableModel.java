package org.nuclos.client.eventsupport.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;

public class EventSupportStatePropertiesTableModel extends AbstractTableModel {

	public static final String COL_SOURCE_LITERAL = SpringLocaleDelegate.getInstance().getMessage("EventSupportStatePropertyModelColumn.1","Literal");
	public static final String COL_SOURCE_STATE = SpringLocaleDelegate.getInstance().getMessage("EventSupportStatePropertyModelColumn.2","Literal");
	public static final String COL_DEST_LITERAL = SpringLocaleDelegate.getInstance().getMessage("EventSupportStatePropertyModelColumn.3","Literal");
	public static final String COL_DEST_STATE = SpringLocaleDelegate.getInstance().getMessage("EventSupportStatePropertyModelColumn.4","Literal");
	
	final static String[] COLUMNS = new String[] {COL_SOURCE_LITERAL, COL_SOURCE_STATE, COL_DEST_LITERAL, COL_DEST_STATE};
	
	final List<EventSupportStatePropertiesTableEntry> entries = new ArrayList<EventSupportStatePropertiesTableEntry>();
	
			
	public void addEntry(Integer litSource, String nameSource, Integer litTarget, String nameTarget)
	{
		entries.add(new EventSupportStatePropertiesTableEntry(litSource,nameSource,litTarget, nameTarget));
		
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
		EventSupportStatePropertiesTableEntry eventSupportStatePropertiesTableEntry = entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			retVal = eventSupportStatePropertiesTableEntry.getLitSource();
			break;
		case 1:
			retVal = eventSupportStatePropertiesTableEntry.getStateSource();
			break;
		case 2:
			retVal = eventSupportStatePropertiesTableEntry.getLitDest();
			break;
		case 3:
			retVal = eventSupportStatePropertiesTableEntry.getStateDest();
			break;
		default:
			break;
		}
		return retVal;
	}

	class EventSupportStatePropertiesTableEntry
	{
		Integer iLitSource;
		String  sStateSource;
		Integer iLitDest;
		String  sStateDest;
		
		public EventSupportStatePropertiesTableEntry(Integer iLitSource,
				String sStateSource, Integer iLitDest, String sStateDest) {
			super();
			this.iLitSource = iLitSource;
			this.sStateSource = sStateSource;
			this.iLitDest = iLitDest;
			this.sStateDest = sStateDest;
		}
		
		public Integer getLitSource() {
			return iLitSource;
		}
		public void setLitSource(Integer iLitSource) {
			this.iLitSource = iLitSource;
		}
		public String getStateSource() {
			return sStateSource;
		}
		public void setStateSource(String sStateSource) {
			this.sStateSource = sStateSource;
		}
		public Integer getLitDest() {
			return iLitDest;
		}
		public void setLitDest(Integer iLitDest) {
			this.iLitDest = iLitDest;
		}
		public String getStateDest() {
			return sStateDest;
		}
		public void setStateDest(String sStateDest) {
			this.sStateDest = sStateDest;
		}
		
		
	}
}

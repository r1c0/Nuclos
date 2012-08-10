package org.nuclos.client.eventsupport.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;

public class EventSupportEntityPropertiesTableModel extends AbstractTableModel {
	
	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_ORDER = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.1","Reihenfolge");
	static final String COL_STATUS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.2","Status");
	static final String COL_PROCESS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.3","Aktion");
	
	static final String[] COLUMNS = new String[] {COL_EVENTSUPPORT, COL_ORDER, COL_STATUS, COL_PROCESS};
	
	List<EventSupportEntityPropertiesTableEntry> entries = new ArrayList<EventSupportEntityPropertiesTableEntry>();
	
	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	public void addEntry (String eventsupport, Integer order, String status, String process)
	{
		entries.add(new EventSupportEntityPropertiesTableEntry(eventsupport, order, status, process));
		fireTableRowsInserted(entries.size(), entries.size());
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		
		EventSupportEntityPropertiesTableEntry esepe = entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			retVal = esepe.getEventSupport();
			break;
		case 1:
			retVal = esepe.getOrder();
			break;
		case 2:
			retVal = esepe.getStatus();
			break;
		case 3:
			retVal = esepe.getProcess();
			break;
		default:
			break;
		}
		
		return retVal;
	}

	public void clear() {
		if (!entries.isEmpty())
		{
			int count = entries.size();
			entries.clear();
			fireTableRowsDeleted(0, count);
		}
	}
	
	class EventSupportEntityPropertiesTableEntry {
		
		private String  sEventSupport;
		private Integer iOrder;
		private String  sStatus;
		private String  sProcess;
		
		public EventSupportEntityPropertiesTableEntry(String sEventSupport, Integer iOrder,
				String sStatus, String sProcess) {
			super();
			this.sEventSupport = sEventSupport;
			this.iOrder = iOrder;
			this.sStatus = sStatus;
			this.sProcess = sProcess;
		}
		
		public Integer getOrder() {
			return iOrder;
		}
		public void setOrder(Integer iOrder) {
			this.iOrder = iOrder;
		}
		public String getStatus() {
			return sStatus;
		}
		public void setStatus(String sStatus) {
			this.sStatus = sStatus;
		}
		public String getProcess() {
			return sProcess;
		}
		public void setProcess(String sProcess) {
			this.sProcess = sProcess;
		}

		public String getEventSupport() {
			return sEventSupport;
		}

		public void setEventSupport(String sEventSupport) {
			this.sEventSupport = sEventSupport;
		}
		
	}
}

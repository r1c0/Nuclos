package org.nuclos.client.eventsupport.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportEntityPropertiesTableModel extends AbstractTableModel {
	
	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_ORDER = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.1","Reihenfolge");
	static final String COL_STATUS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.2","Status");
	static final String COL_PROCESS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.3","Aktion");
	
	static final String[] COLUMNS = new String[] {COL_ORDER, COL_EVENTSUPPORT, COL_STATUS, COL_PROCESS};
	
	final List<StateVO> status = new ArrayList<StateVO> ();
	final List<ProcessVO> process = new ArrayList<ProcessVO> ();
	
	List<EventSupportEventVO> entries = new ArrayList<EventSupportEventVO>();
	
	@Override
	public int getRowCount() {
		return entries.size();
	
	}
	
	public EventSupportEventVO getEntryByRowIndex(int id) {
		EventSupportEventVO retVal = null;
		if (id < entries.size())
			retVal = entries.get(id);
		
		return retVal;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 2 || columnIndex == 3)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	public void addEntry (EventSupportEventVO eseVO)
	{
		entries.add(eseVO);
		fireTableRowsInserted(entries.size(), entries.size());
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue != null) 
		{
			 String value = (String) aValue;
			 if (columnIndex == 2)
			 {
				 for (StateVO svo : status)
				 {
					 if (svo.getDescription().equals(value))
					 {
						 entries.get(rowIndex).setStateId(svo.getId());
						 entries.get(rowIndex).setStateName(svo.getDescription());
						 break;
					 }
				 }				 
			 }			
			 else if (columnIndex == 3)
			 {
				for (ProcessVO pvo : process) {
					if (pvo.getDescription().equals(value)) {
						 entries.get(rowIndex).setProcessId(pvo.getId());
						 entries.get(rowIndex).setProcessName(pvo.getDescription());
						 break;
					}
				}
		 	 }		
		}
    }
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
	
		EventSupportEventVO esepe = entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			retVal = esepe.getOrder();
			break;
		case 1:
			retVal = esepe.getEventSupportClass();
			break;
		case 2:
			if ( esepe.getStateName() != null)
				retVal = esepe.getStateName();
			break;
		case 3:
			if ( esepe.getProcessName() != null)
				retVal = esepe.getProcessName();
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
	
	public String[] getStatusAsArray() {
		String[] vals = new String[status.size()];
		int  idx = 0;
		for (StateVO svo : status) {
			vals[idx++] = svo.getDescription();
		}
		return vals;
	}
	
	public List<StateVO> getStatus()
	{
		return this.status;
	}

	public List<ProcessVO> getProcess()
	{
		return this.process;
	}
	
	public String[] getProcessAsArray() {
		String[] vals = new String[process.size()];
		int  idx = 0;
		for (ProcessVO svo : process) {
			vals[idx++] = svo.getDescription();
		}
		return vals;
	}
	
	public void addStatus(StateVO newStatus) {
		status.add(newStatus);
	}

	public void addProcess(ProcessVO newProcess) {
		process.add(newProcess);
	}
}

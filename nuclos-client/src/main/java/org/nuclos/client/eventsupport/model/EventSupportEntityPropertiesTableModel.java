package org.nuclos.client.eventsupport.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportEntityPropertiesTableModel extends EventSupportPropertiesTableModel {
	
	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_STATUS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.2","Status");
	static final String COL_PROCESS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.3","Aktion");
	
	static final String[] COLUMNS = new String[] {COL_EVENTSUPPORT, COL_STATUS, COL_PROCESS};
	
	final List<StateVO> status = new ArrayList<StateVO> ();
	final List<ProcessVO> process = new ArrayList<ProcessVO> ();
	
	List<EventSupportEventVO> entries = new ArrayList<EventSupportEventVO>();
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean retVal = false;
		if (columnIndex == 1 || columnIndex == 2)
		{
			retVal = true;
		}
		return retVal;
	}
	
	public void addEntry (EventSupportEventVO eseVO)
	{
		entries.add(eseVO);
		
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue != null) 
		{
			 String value = (String) aValue;
			 if (columnIndex == 1)
			 {
				 for (StateVO svo : status)
				 {
					 if (svo.getDescription().equals(value))
					 {
						 entries.get(rowIndex).setStateId(svo.getId());
						 entries.get(rowIndex).setStateName(svo.getDescription());
						 setModelModified(true);
						 break;
					 }
				 }				 
			 }			
			 else if (columnIndex == 2)
			 {
				for (ProcessVO pvo : process) {
					if (pvo.getDescription().equals(value)) {
						 entries.get(rowIndex).setProcessId(pvo.getId());
						 entries.get(rowIndex).setProcessName(pvo.getDescription());
						 setModelModified(true);
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
			retVal = esepe.getEventSupportClass();
			try {
				EventSupportSourceVO ese = EventSupportRepository.getInstance().getEventSupportByClassname(esepe.getEventSupportClass());
				if (ese != null && ese.getName() != null) {
					retVal = ese.getName();
				}
			} catch (RemoteException e) {
				Log.error(e.getMessage(), e);
			}
			
			break;
		case 1:
			if ( esepe.getStateName() != null)
				retVal = esepe.getStateName();
			break;
		case 2:
			if ( esepe.getProcessName() != null)
				retVal = esepe.getProcessName();
			break;
		default:
			break;
		}
		
		return retVal;
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

	@Override
	public List<? extends EventSupportVO> getEntries() {
		return this.entries;
	}

	@Override
	public String[] getColumns() {
		return this.COLUMNS;
	}

	@Override
	public void addEntry(int rowId, EventSupportVO elm) {
		this.entries.add(rowId, (EventSupportEventVO) elm);
	}
	
}

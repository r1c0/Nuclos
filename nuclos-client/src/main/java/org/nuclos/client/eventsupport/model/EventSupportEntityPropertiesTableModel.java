package org.nuclos.client.eventsupport.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportEntityPropertiesTableModel extends AbstractTableModel {
	
	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_ORDER = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.1","Reihenfolge");
	static final String COL_STATUS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.2","Status");
	static final String COL_PROCESS = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.3","Aktion");
	
	static final String[] COLUMNS = new String[] {COL_ORDER, COL_EVENTSUPPORT, COL_STATUS, COL_PROCESS};
	
	private boolean isModelModified= false;
	
	final List<StateVO> status = new ArrayList<StateVO> ();
	final List<ProcessVO> process = new ArrayList<ProcessVO> ();
	
	List<EventSupportEventVO> entries = new ArrayList<EventSupportEventVO>();
	
	
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
	
	public EventSupportEventVO getEntryByRowIndex(int id) {
		EventSupportEventVO retVal = null;
		if (id < entries.size())
			retVal = entries.get(id);
		
		return retVal;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean retVal = false;
		if (columnIndex == 2 || columnIndex == 3)
		{
			retVal = true;
		}
		return retVal;
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
						 setModelModified(true);
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
			retVal = esepe.getOrder();
			break;
		case 1:
			retVal = esepe.getEventSupportClass();
			try {
				EventSupportVO ese = EventSupportRepository.getInstance().getEventSupportByClassname(esepe.getEventSupportClass());
				if (ese != null && ese.getName() != null) {
					retVal = ese.getName();
				}
			} catch (RemoteException e) {
				Log.error(e.getMessage(), e);
			}
			
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
			entries.clear();
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

	public void moveUp(int selectedRow) {
		if (selectedRow != 0) {
			EventSupportEventVO eseVO = entries.get(selectedRow);
			EventSupportEventVO prevEseVO = entries.get(selectedRow - 1);
			
			entries.remove(selectedRow);
			entries.remove(selectedRow-1);
			
			eseVO.setOrder(eseVO.getOrder() - 1);
			prevEseVO.setOrder(prevEseVO.getOrder() + 1);
			
			entries.add(eseVO.getOrder() -1, eseVO);
			entries.add(prevEseVO.getOrder() -1, prevEseVO);
			
			setModelModified(true);
		}
	}
	
	public void moveDown(int selectedRow) {
		if (selectedRow < entries.size() - 1) {
			EventSupportEventVO eseVO = entries.get(selectedRow);
			EventSupportEventVO forwEseVO = entries.get(selectedRow + 1);
			
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
			EventSupportEventVO eventSupportEventVO = entries.get(i);
			eventSupportEventVO.setOrder(eventSupportEventVO.getOrder() - 1 );
		}
		
	}
	
}

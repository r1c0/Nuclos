package org.nuclos.client.eventsupport.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportJobPropertiesTableModel extends EventSupportPropertiesTableModel {

	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportJobPropertyModelColumn.2","EventSupport");
	
	static final String[] COLUMNS = new String[] {COL_EVENTSUPPORT};
		
	final List<EventSupportJobVO> entries = new ArrayList<EventSupportJobVO>();
	
	public void addEntry(EventSupportJobVO eseVO)
	{
		entries.add(eseVO);
		fireTableRowsInserted(entries.size(), entries.size());
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		
		EventSupportJobVO eventSupportJobVO = entries.get(rowIndex);
		switch (columnIndex) {
		case 0:
			retVal = eventSupportJobVO.getEventSupportClass();
			
			EventSupportSourceVO ese;
			try {
				ese = EventSupportRepository.getInstance().getEventSupportByClassname(eventSupportJobVO.getEventSupportClass());
				if (ese != null && ese.getName() != null) {
					retVal = ese.getName();
				}
			} catch (RemoteException e) {
				Log.error(e.getMessage(), e);
			}
			
			break;
		default:
			break;
		}
		return retVal;
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
		this.entries.add(rowId, (EventSupportJobVO) elm);
	}
}

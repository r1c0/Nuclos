package org.nuclos.client.eventsupport.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportGenerationPropertiesTableModel extends EventSupportPropertiesTableModel {

	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportGenerationPropertyModelColumn.2","EventSupport");
	
	static final String[] COLUMNS = new String[] {COL_EVENTSUPPORT};
	
	final List<EventSupportGenerationVO> entries = new ArrayList<EventSupportGenerationVO>();

	public void addEntry(EventSupportGenerationVO newEntry) {
		this.entries.add(newEntry);
	}
	

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		
		EventSupportGenerationVO rowEntry = this.entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			String value = rowEntry.getEventSupportClass();
			EventSupportSourceVO ese;
			try {
				ese = EventSupportRepository.getInstance().getEventSupportByClassname(value);
				if (ese != null && ese.getName() != null) {
					value = ese.getName();
				}
			} catch (RemoteException e) {
				Log.error(e.getMessage(), e);
			}
			retVal = value;
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
		this.entries.add(rowId, (EventSupportGenerationVO) elm);
	}
	
}

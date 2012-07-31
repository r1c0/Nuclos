package org.nuclos.client.eventsupport;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportPropertiesTableModel extends AbstractTableModel {
	
	static final String COL_ES_NAME = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.1","Name");
	static final String COL_ES_TYPE = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.2","Typ");
	static final String COL_ES_NUCLET = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.3","Nuclet");
	static final String COL_ES_PATH = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.4","Package");
	static final String COL_ES_CREATION_DATE = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.5","Erstellt am");
	
	static final String COLUMNS[] = new String[] { COL_ES_NAME, COL_ES_TYPE, COL_ES_NUCLET, COL_ES_PATH, COL_ES_CREATION_DATE};

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addEntry(EventSupportVO esVO)
	{
		
	}
}

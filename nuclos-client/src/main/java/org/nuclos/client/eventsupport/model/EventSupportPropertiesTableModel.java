package org.nuclos.client.eventsupport.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;

public class EventSupportPropertiesTableModel extends AbstractTableModel {
	
	public static final String COL_PROPERTY = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModelColumn.1","Eigenschaft");
	public static final String COL_VALUE = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModelColumn.2","Wert");
	
	public static final String ELM_ES_NAME = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.1","Name");
	public static final String ELM_ES_TYPE = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.2","Typ");
	public static final String ELM_ES_DESCRIPTION = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.6","Beschreibung");
	public static final String ELM_ES_NUCLET = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.3","Nuclet");
	public static final String ELM_ES_PATH = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.4","Package");
	public static final String ELM_ES_CREATION_DATE = SpringLocaleDelegate.getInstance().getMessage("EventSupportTableModel.5","Erstellt am");
	
	static final String[] COLUMNS = new String[] { COL_PROPERTY, COL_VALUE};
	
	static final List<String> PROPERTIES = new ArrayList<String>();	
	final List<EventSupportPropertyModelEntry> ENTRIES = new ArrayList<EventSupportPropertyModelEntry>();
		
	public EventSupportPropertiesTableModel()
	{
		super();
		PROPERTIES.addAll(
				java.util.Arrays.asList(new String[] {ELM_ES_NAME, ELM_ES_DESCRIPTION, ELM_ES_TYPE,ELM_ES_NUCLET,ELM_ES_PATH,ELM_ES_CREATION_DATE}));
	}
	 
	@Override
	public int getRowCount() {
		return ENTRIES.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		String retVal = null;
		
		if (columnIndex == 0)
		{
			retVal = PROPERTIES.get(rowIndex);
		}
		else
		{
			Iterator<EventSupportPropertyModelEntry> lstEntries = ENTRIES.iterator();
			
			while (lstEntries.hasNext())
			{
				EventSupportPropertyModelEntry next = lstEntries.next();
				if (next.getProperty().equals(PROPERTIES.get(rowIndex)))
				{
					retVal = next.getValue();
				}
			}
		}
		
		return retVal;
	}

	public void clear() {
		if (!ENTRIES.isEmpty())
		{
			int count = ENTRIES.size();
			ENTRIES.clear();
			fireTableRowsDeleted(0, count);
		}
	}
	
	public void addEntry(String propertyName, String value)
	{
		if (PROPERTIES.contains(propertyName))
		{
			ENTRIES.add(new EventSupportPropertyModelEntry(propertyName, value));
			fireTableRowsInserted(ENTRIES.size(), ENTRIES.size());
		}
	}
		
	class EventSupportPropertyModelEntry
	{
		String property;
		String value;
		
		public EventSupportPropertyModelEntry(String prop, String val)
		{
			this.property = prop;
			this.value = val;
		}

		public String getProperty() {
			return property;
		}

		public String getValue() {
			return value;
		}
	}
}

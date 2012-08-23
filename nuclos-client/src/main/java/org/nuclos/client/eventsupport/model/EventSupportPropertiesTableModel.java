package org.nuclos.client.eventsupport.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public abstract class EventSupportPropertiesTableModel extends AbstractTableModel {

	public abstract List<? extends EventSupportVO> getEntries();
	public abstract String[] getColumns();
	public abstract void addEntry(int rowId,EventSupportVO elm);
	
	private boolean isModelModified= false;
	
	@Override
	public int getRowCount() {
		return getEntries().size();
	}

	@Override
	public int getColumnCount() {
		return getColumns().length;
	}

	@Override
	public String getColumnName(int column) {
		return getColumns()[column];
	}
	
	public void clear() {
		if (!getEntries().isEmpty())
		{
			int count = getEntries().size();
			getEntries().clear();
			fireTableRowsDeleted(0, count);
		}
	}
	
	public boolean isModelModified() {
		return isModelModified;
	}

	public void setModelModified(boolean isModelModified) {
		this.isModelModified = isModelModified;
	}
	
	public void removeEntry(int row) {
		if (row < getEntries().size()) {
			getEntries().remove(row);
		}	
	}
	
	public void moveDown(int selectedRow) {
		if (selectedRow < getEntries().size() - 1) {
			EventSupportVO eseVO = getEntries().get(selectedRow);
			EventSupportVO forwEseVO = getEntries().get(selectedRow + 1);
			int order = eseVO.getOrder();
			eseVO.setOrder(forwEseVO.getOrder());
			forwEseVO.setOrder(order);
			
			setModelModified(true);
			
			Collections.sort(getEntries(), new Comparator<EventSupportVO>() {		
				@Override
				public int compare(EventSupportVO o1, EventSupportVO o2) {
					return o1.getOrder() > o2.getOrder() ? 1 : o1.getOrder() < o2.getOrder() ? -1: 0;
				}
			});
			fireTableRowsUpdated(selectedRow, selectedRow+1);
		}
	}

	public void moveUp(int selectedRow) {
		if (selectedRow != 0) {
			EventSupportVO eseVO = getEntries().get(selectedRow);
			EventSupportVO prevEseVO = getEntries().get(selectedRow - 1);
			int order = eseVO.getOrder();
			eseVO.setOrder(prevEseVO.getOrder());
			prevEseVO.setOrder(order);
			
			setModelModified(true);
			Collections.sort(getEntries(), new Comparator<EventSupportVO>() {		
				@Override
				public int compare(EventSupportVO o1, EventSupportVO o2) {
					return o1.getOrder() > o2.getOrder() ? 1 : o1.getOrder() < o2.getOrder() ? -1: 0;
				}
			});
			fireTableRowsUpdated(selectedRow-1, selectedRow);
		}
	}
	
	public EventSupportVO getEntryByRowIndex(int id) {
		EventSupportVO retVal = null;
		if (id < getEntries().size())
			retVal = getEntries().get(id);
		
		return retVal;
	}
		
}

//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui.multiaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.nuclos.client.ui.model.AbstractListTableModel;
import org.nuclos.common2.CommonLocaleDelegate;


class MultiActionProgressTableModel extends AbstractListTableModel<MultiActionProgressLine> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_RESULT = 1;
	public static final int COLUMN_STATE = 2;

	private static final String[] defaultColumnNames = {CommonLocaleDelegate.getMessage("MultiActionProgressTableModel.1","ID"), CommonLocaleDelegate.getMessage("MultiActionProgressTableModel.2","Ergebnis"), CommonLocaleDelegate.getMessage("MultiActionProgressTableModel.3","Status")};
	private String[] aColumnNames;

	private Comparator<MultiActionProgressLine> compResultColumn = new ResultColumnComparator();

	public MultiActionProgressTableModel(Collection<MultiActionProgressLine> colllogbookvo, String[] columnNames) {
		super(new ArrayList<MultiActionProgressLine>(colllogbookvo));
		if(columnNames != null && columnNames.length == 3){
			aColumnNames = columnNames;
		} else {
			aColumnNames = defaultColumnNames;
		}
		// sort by "state":
		Collections.sort(this.getRows(), compResultColumn);
	}

	@Override
	public String getColumnName(int iColumn) {
		return aColumnNames[iColumn];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case COLUMN_ID:
		case COLUMN_RESULT:
		case COLUMN_STATE:
			return String.class;
		default:
			return Object.class;
		}
	}

	@Override
	public int getColumnCount() {
		return aColumnNames.length;
	}

	public void setColumnNames(String[] columnNames){
		if(columnNames != null && columnNames.length == 3){
			aColumnNames = columnNames;
		}
	}

	protected int addRow(MultiActionProgressLine newLine){
		int rowIndex = this.getRows().size();
		this.getRows().add(newLine);
		// sort by "state":
		Collections.sort(this.getRows(), compResultColumn);
		this.fireTableDataChanged();
		return rowIndex;
	}

	private class ResultColumnComparator implements Comparator<MultiActionProgressLine> {
		@Override
		public int compare(MultiActionProgressLine line1, MultiActionProgressLine line2) {
			final int result = line1.getResult().compareTo(line2.getResult());
			return result;
		}
	} //class CreatedAtComparator

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		final Object result;
		final MultiActionProgressLine line = getRow(iRow);
		switch (iColumn) {
			case COLUMN_ID:
				result = line.getSourceObject();
				break;
			case COLUMN_RESULT:
				result = line.getResult();
				break;
			case COLUMN_STATE:
				result = line.getState();
				break;
			default:
				throw new IllegalArgumentException("iColumn");
		}
		return result;
	}

	public static int getPreferredColumnWidth(int iColumn) {
		final int result;
		switch(iColumn) {
			case COLUMN_ID:
				result = 50;
				break;
			case COLUMN_RESULT:
				result = 250;
				break;
			case COLUMN_STATE:
				result = 100;
				break;
			default:
				result = 100;
		}
		return result;
	}
}

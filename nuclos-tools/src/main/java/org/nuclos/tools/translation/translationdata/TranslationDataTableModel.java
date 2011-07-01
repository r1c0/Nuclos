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
package org.nuclos.tools.translation.translationdata;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.tools.translation.translationdata.columns.TranslationDataColumn;


@SuppressWarnings("all")
public class TranslationDataTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<TranslationData> data;

	public TranslationDataTableModel( ) {
		data = new ArrayList<TranslationData>();
	}

	public TranslationDataTableModel(List<TranslationData> data) {
		this.data = data;
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return TranslationDataColumn.NUMBER_OF_COLUMNS;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return TranslationDataColumn.makeTranslationDataColumn(columnIndex).getColumnName();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return TranslationDataColumn.makeTranslationDataColumn(columnIndex).getColumnClass();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return TranslationDataColumn.makeTranslationDataColumn(columnIndex).getValueAt(data.get(rowIndex));
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		TranslationDataColumn.makeTranslationDataColumn(columnIndex).setValueAt(value,data.get(rowIndex));
		//fireTableDataChanged();
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return TranslationDataColumn.makeTranslationDataColumn(columnIndex).isCellEditable(data.get(rowIndex));
	}

	public List<TranslationData> getData() {
		return data;
	}

	public void setData(List<TranslationData> data) {
		this.data = data;
		fireTableDataChanged();
	}

}

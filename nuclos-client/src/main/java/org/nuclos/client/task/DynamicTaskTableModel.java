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
package org.nuclos.client.task;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

@SuppressWarnings("serial")
public class DynamicTaskTableModel extends DefaultTableModel {

	private ResultVO resultvo;

	private List<String> columns;

	public DynamicTaskTableModel(TasklistDefinition def, ResultVO resultvo, List<String> columnOrder) {
		this.resultvo = resultvo;
		this.columns = new ArrayList<String>();
		for (String column : columnOrder) {
			if (isMetaColumn(def, column) || !containsColumn(column)) {
				continue;
			}
			else {
				this.columns.add(column);
			}
		}
		for (int i = 0; i < resultvo.getColumnCount(); i++) {
			ResultColumnVO col = resultvo.getColumns().get(i);
			if (isMetaColumn(def, col.getColumnLabel()) || !containsColumn(col.getColumnLabel()) || this.columns.contains(col.getColumnLabel())) {
				continue;
			}
			else {
				this.columns.add(col.getColumnLabel());
			}
		}
	}
	
	public void setData(ResultVO r) {
		this.resultvo = r;
		fireTableDataChanged();
	}

	public Object getValueByField(int i, String field) {
		for (int j = 0; j < resultvo.getColumnCount(); j++) {
			ResultColumnVO col = resultvo.getColumns().get(j);
			if (col.getColumnLabel().equals(field)) {
				return resultvo.getRows().get(i)[j];
			}
		}
		throw new RuntimeException();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public int getRowCount() {
		return resultvo != null ? resultvo.getRows().size() : 0;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		final String column = columns.get(columnIndex);
		final int index = getColumnIndex(column);
		return resultvo.getColumns().get(index).getColumnClass();
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		final String column = columns.get(iColumn);
		final int index = getColumnIndex(column);
		final ResultColumnVO columnVO = resultvo.getColumns().get(index);
		return resultvo.getRows().get(iRow)[index];
	}

	@Override
	public void setValueAt(Object oValue, int iRow, int iColumn) {
		// do nothing because this is a read only model
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex);
	}
	
	private boolean containsColumn(String column) {
		return getColumnIndex(column) > -1;
	}
	
	private int getColumnIndex(String column) {
		int i = 0;
		for (ResultColumnVO col : resultvo.getColumns()) {
			if (col.getColumnLabel().equals(column)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	private boolean isMetaColumn(TasklistDefinition def, String column) {
		if (column.equals(def.getDynamicTasklistIdFieldname())) {
			return true;
		}
		else if (column.equals(def.getDynamicTasklistEntityFieldname())) {
			return true;
		}
		return false;
	}
}
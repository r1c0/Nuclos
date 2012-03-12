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

import javax.swing.table.DefaultTableModel;

import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

@SuppressWarnings("serial")
public class DynamicTaskTableModel extends DefaultTableModel {

	private final ResultVO resultvo;

	public DynamicTaskTableModel(ResultVO resultvo) {
		this.resultvo = resultvo;
	}

	@Override
	public int getColumnCount() {
		return resultvo.getColumns().size();
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
		return String.class;
		//return resultvo.getColumns().get(columnIndex).getColumnClass();
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		final ResultColumnVO columnVO = resultvo.getColumns().get(iColumn);
		return columnVO.format(resultvo.getRows().get(iRow)[iColumn]);
	}

	@Override
	public void setValueAt(Object oValue, int iRow, int iColumn) {
		// do nothing because this is a read only model
	}

	@Override
	public String getColumnName(int columnIndex) {
		return resultvo.getColumns().get(columnIndex).getColumnLabel();
	}
}
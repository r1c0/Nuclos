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
package org.nuclos.client.datasource.querybuilder.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.nuclos.client.datasource.querybuilder.shapes.gui.ConstraintColumn;
import org.nuclos.common.database.query.definition.DataType;
import org.nuclos.common.database.query.definition.Table;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ColumnSelectionTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final int COLUMN_COUNT = 255;
	protected static final int TABLE_ROW = 0;
	protected static final int COLUMN_ROW = 1;
	protected static final int ALIAS_ROW = 2;
	protected static final int VISIBLE_ROW = 3;
	protected static final int GROUPBY_ROW = 4;
	protected static final int ORDERBY_ROW = 5;

	private final List<ColumnEntry> lstColumns = new ArrayList<ColumnEntry>();

	public ColumnSelectionTableModel() {
		for (int i = 0; i < COLUMN_COUNT; i++) {
			lstColumns.add(new ColumnEntry());
		}
	}

	/**
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @return number of rows
	 */
	@Override
	public int getRowCount() {
		return ColumnEntry.ROW_COUNT;
	}

	/**
	 * @param index
	 * @return column with specified index
	 */
	public ColumnEntry getColumn(int index) {
		return lstColumns.get(index);
	}

	/**
	 * @return List<ColumnEntry> of columns
	 */
	public List<ColumnEntry> getColumns() {
		return lstColumns;
	}

	/**
	 * remove all columns with the specified table alias
	 * @param sAlias
	 */
	public void removeColumnsForTable(String sAlias) {
		for (ColumnEntry entry : this.getColumns()) {
			final Table tableEntry = entry.getTable();
			final String sEntryAlias = (tableEntry == null) ? null : tableEntry.getAlias();
			if (sAlias.equals(sEntryAlias)) {
				entry.reset();
			}
		}
		fireTableDataChanged();
	}

	/**
	 * @param entry
	 */
	public void addColumn(ColumnEntry entry) {
		lstColumns.add(entry);
		fireTableDataChanged();
	}

	public void addColumn(int iIndex, ColumnEntry entry) {
		lstColumns.add(iIndex, entry);
		fireTableDataChanged();
	}

	/**
	 * @param rowIndex
	 * @param columnIndex
	 * @return value at specified index
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final ColumnEntry entry = lstColumns.get(columnIndex);
		switch (rowIndex) {
			case TABLE_ROW:
				return entry.getTable();
			case COLUMN_ROW:
				return entry.getColumn();
			case ALIAS_ROW:
				return entry.getAlias();
			case VISIBLE_ROW:
				return entry.isVisible();
			case GROUPBY_ROW:
				return entry.getGroupBy();
			case ORDERBY_ROW:
				return entry.getOrderBy();
			default:
				return entry.getCondition(rowIndex - ORDERBY_ROW - 1);
		}
	}

	/**
	 *
	 * @param oValue
	 * @param iRow
	 * @param iColumn
	 */
	@Override
	public void setValueAt(Object oValue, int iRow, int iColumn) {
		final ColumnEntry entry = lstColumns.get(iColumn);
		switch (iRow) {
			case TABLE_ROW:
				if (oValue instanceof Table) {
					entry.setTable((Table) oValue);
				}
				break;
			case COLUMN_ROW:
				if (oValue instanceof ConstraintColumn) {
					entry.setColumn((ConstraintColumn) oValue);
				}
				else {
					DataType dataType = DataType.VARCHAR;
					entry.setColumn(new ConstraintColumn(entry.getTable(), (String) oValue, dataType, 255, 0, 0, false));
				}
				break;
			case ALIAS_ROW:
				if (oValue instanceof String) {
					entry.setAlias((String) oValue);
				}
				break;
			case VISIBLE_ROW:
				if (oValue instanceof Boolean) {
					entry.setVisible((Boolean) oValue);
				}
				break;
			case GROUPBY_ROW:
				if (oValue instanceof String) {
					final String sGroupBy = (String) oValue;
					if (sGroupBy == null || sGroupBy.equals("keine")) {
						entry.setGroupBy(null);
					}
					else {
						entry.setGroupBy((String) oValue);
					}
				}
				break;
			case ORDERBY_ROW:
				if (oValue instanceof String) {
					final String sOrderBy = (String) oValue;
					if (sOrderBy == null || sOrderBy.equals("keine")) {
						entry.setOrderBy(null);
					}
					else {
						entry.setOrderBy((String) oValue);
					}
				}
				break;
			default:
				if (oValue instanceof String) {
					final String sCondition = ((String) oValue);
					// Allow the user to enclose a value in quotation marks for strings with spaces etc.
					// Internally we parse only for single quotation marks
//					sCondition = ((String)oValue).replaceAll("\"", "'");
					entry.setCondition(iRow - ORDERBY_ROW - 1, sCondition.length() > 0 ? sCondition : null);
				}
		}
		fireTableCellUpdated(iRow, iColumn);
	}

	/**
	 *
	 * @param iColumn
	 * @return name of column with specified index
	 */
	@Override
	public String getColumnName(int iColumn) {
		return " ";
	}

	/**
	 *
	 * @param iRow
	 * @param iColumn
	 * @return true if cell is editable
	 */
	@Override
	public boolean isCellEditable(int iRow, int iColumn) {
		return true;
	}

	/**
	 *
	 * @param iSelectedColumn
	 */
	public void removeColumn(int iSelectedColumn) {
		final ColumnEntry entry = lstColumns.get(iSelectedColumn);
		if (entry != null) {
			entry.reset();
			fireTableDataChanged();
		}
	}

	public void removeAllColumns() {
		for (ColumnEntry columnEntry : lstColumns) {
			columnEntry.reset();
			fireTableDataChanged();
		}
	}

	public boolean isGroupBySelected() {
		for (ColumnEntry columnEntry : lstColumns) {
			if (columnEntry != null && columnEntry.getGroupBy() != null && columnEntry.getGroupBy().equals("Gruppe")) {
				return true;
			}
		}
		return false;
	}

}	// class ColumnSelectionTableModel

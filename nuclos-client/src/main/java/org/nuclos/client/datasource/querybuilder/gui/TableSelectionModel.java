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

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common2.CommonLocaleDelegate;


import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

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
public class TableSelectionModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_TYPE = CommonLocaleDelegate.getMessage("TableSelectionModel.3","Typ");
	private static final String COLUMN_TABLE = CommonLocaleDelegate.getMessage("TableSelectionModel.2","Tabelle");
	private static final String COLUMN_DESCRIPTION = CommonLocaleDelegate.getMessage("TableSelectionModel.1","Beschreibung");

	private static final int COLUMN_TABLE_INDEX = 0;
	private static final int COLUMN_TYPE_INDEX = 1;
	private static final int COLUMN_DESCRIPTION_INDEX = 2;

	private final List<Table> lstTables = new ArrayList<Table>();

	/**
	 * @return number of rows
	 */
	@Override
	public int getRowCount() {
		return lstTables.size();
	}

	/**
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		return 3;
	}

	public void removeAll() {
		lstTables.clear();
	}

	/**
	 *
	 * @param rowIndex
	 * @param columnIndex
	 * @return value at specified index
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Table table = lstTables.get(rowIndex);
		Object result = null;
		switch (columnIndex) {
			case COLUMN_TABLE_INDEX:
				result = table.getName();
				break;
			case COLUMN_TYPE_INDEX:
				result = table.getType();
				break;
			case COLUMN_DESCRIPTION_INDEX:
				if (table.getComment() == null && table.getEntityName() != null) {
					result = CommonLocaleDelegate.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(table.getEntityName()));
				} else {
					result = table.getComment();
				}
				break;
		}
		return result;
	}

	/**
	 *
	 * @param lstTables
	 */
	public void addTables(List<Table> lstTables) {
		this.lstTables.addAll(lstTables);
		fireTableDataChanged();
	}

	/**
	 *
	 * @param column
	 * @return name of column with specified index
	 */
	@Override
	public String getColumnName(int column) {
		String result = "";
		switch (column) {
			case COLUMN_TABLE_INDEX:
				result = COLUMN_TABLE;
				break;
			case COLUMN_TYPE_INDEX:
				result = COLUMN_TYPE;
				break;
			case COLUMN_DESCRIPTION_INDEX:
				result = COLUMN_DESCRIPTION;
				break;
		}
		return result;
	}

	/**
	 *
	 * @param iRow
	 * @return row with specified index
	 */
	public Table getRow(int iRow) {
		return lstTables.get(iRow);
	}

	/**
	 *
	 * @param t
	 */
	public void addTable(Table t) {
		lstTables.add(t);
		fireTableDataChanged();
	}

}	// class TableSelectionModel

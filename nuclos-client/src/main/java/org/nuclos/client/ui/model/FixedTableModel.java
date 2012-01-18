//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.model;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Table for displaying items together with a 'fixed' checkbox.
 * In Nuclos this is used for the 'fixed column result feature'.
 *
 * @since Nuclos 3.1.01 this is a top-level class.
 */
public class FixedTableModel<T> extends AbstractTableModel {

	private MutableListModel<T> objectListModel;

	private final Set<T> fixedObjSet;

	public FixedTableModel(MutableListModel<T> objectColl) {
		super();
		this.fixedObjSet = new HashSet<T>();
		this.objectListModel = objectColl;
		this.objectListModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalAdded(ListDataEvent e) {
				FixedTableModel.this.fireTableRowsInserted(e.getIndex0(),
						e.getIndex1());
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				FixedTableModel.this.fireTableRowsDeleted(e.getIndex0(),
						e.getIndex1());
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				FixedTableModel.this.fireTableRowsUpdated(e.getIndex0(),
						e.getIndex1());
			}
		});
	}

	public void setFixedObjSet(Set<T> fixedColumns) {
		fixedObjSet.clear();
		fixedObjSet.addAll(fixedColumns);
		FixedTableModel.this.fireTableStructureChanged();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return objectListModel.getSize();
	}

	@Override
	public String getColumnName(int col) {
		return (col == 1) ? "Spalte" : "Fixiert";
	}

	@Override
	public Object getValueAt(int row, int col) {
		return (col == 1) ? objectListModel.getElementAt(row)
				: isObjectFixed(objectListModel.getElementAt(row));
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0 && aValue instanceof Boolean) {
			if (((Boolean) aValue).booleanValue()) {
				if (this.fixedObjSet.size() + 1 >= objectListModel.getSize()) {
					JOptionPane.showMessageDialog(
						null,
						CommonLocaleDelegate.getInstance().getMessage(
							"SelectFixedColumnsController.2",
							"Es d\u00fcrfen nicht alle Spalten ausgeblendet und fixiert werden"));

				} else {
					this.fixedObjSet.add((T) objectListModel.getElementAt(rowIndex));
				}
			} else {
				this.fixedObjSet.remove(objectListModel.getElementAt(rowIndex));
			}
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex != 1);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return (columnIndex == 1) ? Object.class : Boolean.class;
	}

	private Boolean isObjectFixed(Object rowObj) {
		return this.fixedObjSet.contains(rowObj);
	}

	public MutableListModel<T> getObjectListModel() {
		return objectListModel;
	}

	public Set<T> getFixedObjSet() {
		return fixedObjSet;
	}
}

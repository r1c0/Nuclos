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
package org.nuclos.client.ui.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.TableModelEvent;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.ui.table.AbstractListTableModel;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.genericobject.ProxyList;

/**
 * <br>TableModel implementation for <code>Collectable</code>s
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectableTableModelImpl <Clct extends Collectable>
		extends AbstractListTableModel<Clct>
		implements CollectableTableModel<Clct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the list of columns.
	 */
	private List<CollectableEntityField> lstclctefColumns;

	/*
	 * entity name
	 */
	private final String sEntityName;

	/**
	 * constructs an empty table model.
	 * @postcondition this.getRowCount == 0
	 * @postcondition this.getColumnCount == 0
	 * @postcondition this.getSortedColumn() == -1
	 */
	public CollectableTableModelImpl(String sEntityName) {
		this(sEntityName, new ArrayList<Clct>());

		assert this.getRowCount() == 0;
		assert this.getColumnCount() == 0;
	}

	/**
	 * constructs a table model, using lstRows as implementation.
	 * @param lstclct is taken directly, not copied.
	 * @precondition lstclct != null
	 * @postcondition this.getRows() == lstclct
	 * @postcondition this.getRowCount == lstclct.size()
	 * @postcondition this.getColumnCount == 0
	 * @postcondition this.getSortedColumn() == -1
	 */
	public CollectableTableModelImpl(String sEntityName, List<Clct> lstclct) {
		/** @todo check precondition */
		super(lstclct);
		this.lstclctefColumns = new ArrayList<CollectableEntityField>();
		this.sEntityName = sEntityName;

		// assert we don't copy the list:
		assert this.getRows() == lstclct;
		assert this.getRowCount() == lstclct.size();
		assert this.getColumnCount() == 0;
	}

	public String getEntityName() {
		return sEntityName;
	}

	/**
	 * @param lstclctefColumns List<CollectableEntityField>
	 * @precondition lstclctefColumns != null
	 * @postcondition this.getColumnCount() == lstclctefColumns.size()
	 * @postcondition this.getSortedColumn() < this.getColumnCount()
	 */
	@Override
	public void setColumns(List<? extends CollectableEntityField> lstclctefColumns) {
		if(lstclctefColumns == null) {
			throw new NullArgumentException("lstclctefColumns");
		}

		this.lstclctefColumns = new ArrayList<CollectableEntityField>(lstclctefColumns);

		super.fireTableStructureChanged();

		assert this.getColumnCount() == lstclctefColumns.size();
	}

	/**
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return this.lstclctefColumns.size();
	}

	@Override
	public CollectableField getValueAsCollectableField(Object oValue) {
		return (CollectableField) oValue;
	}

	/**
	 * adds <code>clctef</code> as column number <code>iColumn</code>
	 * @param iColumn
	 * @param clctef
	 */
	@Override
	public void addColumn(int iColumn, CollectableEntityField clctef) {
		this.lstclctefColumns.add(iColumn, clctef);
		super.fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW, TableModelEvent.HEADER_ROW, iColumn));
	}

	/**
	 * removes column number <code>iColumn</code>
	 * @param iColumn
	 * @postcondition this.getSortedColumn() < this.getColumnCount()
	 */
	@Override
	public void removeColumn(int iColumn) {
		this.lstclctefColumns.remove(iColumn);

		super.fireTableStructureChanged();
	}

	/**
	 * @param iColumn
	 * @return
	 * @precondition iColumn >= 0 && iColumn < this.getColumnCount()
	 */
	@Override
	public CollectableEntityField getCollectableEntityField(int iColumn) {
		return this.lstclctefColumns.get(iColumn);
	}

	/**
	 * @param sFieldName
	 * @return the index of the column with the given fieldname. -1 if none was found.
	 */
	@Override
	public int findColumnByFieldName(String sFieldName) {
		int result = -1;
		for (int iColumn = 0; iColumn < this.getColumnCount(); ++iColumn) {
			if (this.getCollectableEntityField(iColumn).getName().equals(sFieldName)) {
				result = iColumn;
				break;
			}
		}
		return result;
	}

	/**
	 * @param oId id of the <code>Collectable</code> to find.
	 * @return the index of the column with the given id. -1 if none was found.
	 */
	@Override
	public int findRowById(Object oId) {
		int result = -1;

		if (this.getRows() instanceof ProxyList<?>) {
			ProxyList<?> proxylist = (ProxyList<?>) this.getRows();
			return proxylist.getIndexById(oId);
		}

		for(int iRow = 0; iRow < this.getRowCount(); ++iRow) {
			if(LangUtils.equals(this.getCollectable(iRow).getId(), oId)) {
				result = iRow;
				break;
			}
		}
		return result;
	}

	/**
	 * @param iColumn
	 * @return the name of column <code>iColumn</code>, as shown in the table header
	 */
	@Override
	public String getColumnName(int iColumn) {
		return this.getCollectableEntityField(iColumn).getLabel();
	}

	@Override
	public Class<?> getColumnClass(int iColumn) {
		return CollectableField.class;
	}

	/**
	 * @param iRow
	 * @return the <code>Collectable</code> contained in row number <code>iRow</code>
	 */
	@Override
	public Clct getCollectable(int iRow) {
		return this.getRow(iRow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCollectable(int iRow, Clct clct) {
		this.getRows().set(iRow, clct);
		this.fireTableRowsUpdated(iRow, iRow);
	}

	@Override
	public List<Clct> getCollectables() {
		return Collections.unmodifiableList(this.getRows());
	}

	@Override
	public void setCollectables(List<Clct> lstclct) {
		this.setRows(lstclct);
		// fires tableDataChanged
	}

	/**
	 * @param clct
	 * @return the number of the row containing the Collectable with the given id
	 * @throws NoSuchElementException if no matching Collectable can be found
	 */
	private int indexOf(Collectable clct) {
		int result = 0;
		for (Iterator<? extends Collectable> iterator = this.getRows().iterator(); iterator.hasNext(); ++result) {
			final Collectable clct1 = iterator.next();
			if (clct.equals(clct1)) {
				break;
			}
		}
		if (result > this.getRows().size()) {
			throw new NoSuchElementException();
		}
		return result;
	}

	@Override
	public void remove(Collectable clct) {
		this.remove(indexOf(clct));
	}

	@Override
	public void setValueAt(Object oValue, int iRow, int iColumn) {
		final Collectable clct = this.getCollectable(iRow);
		final CollectableEntityField clctef = this.getCollectableEntityField(iColumn);
		CollectableField clctfValue = (CollectableField) oValue;

		// for compatibility reasons, we allow that oValue == null:
		if(clctfValue == null) {
			clctfValue = clctef.getNullField();
		}
		final String sFieldName = clctef.getName();

		if (!clct.getField(sFieldName).equals(clctfValue, false)) {
			clct.setField(sFieldName, clctfValue);

			this.fireTableCellUpdated(iRow, iColumn);
		}
	}

	/**
	 * @param iRow
	 * @param iColumn
	 * @return the <code>CollectableField</code> in the cell specified by <code>iRow</code> and <code>iColumn</code>
	 */
	@Override
	public CollectableField getValueAt(int iRow, int iColumn) {
		final Collectable clct = this.getCollectable(iRow);
		final String sFieldName = this.getCollectableEntityField(iColumn).getName();

		return clct.getField(sFieldName);
	}
}  //  class CollectableTableModelImpl

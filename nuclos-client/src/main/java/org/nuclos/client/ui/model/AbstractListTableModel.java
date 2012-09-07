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

package org.nuclos.client.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Abstract TableModel which manages its rows as List of type R.
 * @param R row type
 */
public abstract class AbstractListTableModel<R> extends AbstractTableModel implements Iterable<R> {

	private List<R> lstRows;
	
	public AbstractListTableModel() {
		setRows(new ArrayList<R>());
	}

	/**
	 * Constructs a table model, using lstRows as implementation.
	 * @param lstRows is taken directly, not copied.
	 * @postcondition this.getRows() == lstRows
	 */
	public AbstractListTableModel(List<R> lstRows) {
		setRows(lstRows);
	}
	
	/*
	 * Maven don't like this.
	 * {@link org.springframework.beans.factory.aspectj.AbstractInterfaceDrivenDependencyInjectionAspect}.
	public Object readResolve() throws ObjectStreamException {
		setSpringLocaleDelegate(SpringLocaleDelegate.getInstance());
		return this;
	}
	 */
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return SpringLocaleDelegate.getInstance();
	}

	public void setRows(List<R> lstRows) {
		this.lstRows = lstRows;
		this.fireTableDataChanged();
	}

	/**
	 * @return the list used as implementation of the model.
	 * BEWARE: When this list is altered by the caller, the caller itself must take care for firing table changed events.
	 */
	protected List<R> getRows() {
		return this.lstRows;
	}

	/**
	 * @param index
	 * @return the object (of type R) contained in row number <code>iRow</code>.
	 */
	public R getRow(int index) {
		return this.getRows().get(index);
	}

	public void add(int index, R row) {
		this.getRows().add(index, row);
		fireTableRowsInserted(index, index);
	}

	public void add(R row) {
		add(this.getRows().size(), row);
	}

	public void addAll(Collection<R> rows) {
		addAll(this.getRows().size(), rows);
	}

	public void addAll(int index, Collection<R> rows) {
		this.getRows().addAll(index, rows);
		this.fireTableRowsInserted(index, index + rows.size() - 1);
	}

	public void remove(int index) {
		this.getRows().remove(index);
		fireTableRowsDeleted(index, index);
	}

	public void remove(int[] indices) {
		List<R> objectsToRemove = new ArrayList<R>();
		int first = -1;
		int last = -1;
		for (int index : indices) {
			objectsToRemove.add(this.getRow(index));
			if (first == -1) {
				first = index;
			}
			else if (index < first) {
				first = index;
			}
			if (index > last) {
				last = index;
			}
		}
		this.getRows().removeAll(objectsToRemove);
		fireTableRowsDeleted(first, last);
	}

	public void clear() {
		if (!this.getRows().isEmpty()) {
			final int size = this.getRows().size();
			this.getRows().clear();
			this.fireTableRowsDeleted(0, size - 1);
		}
	}

	@Override
	public final Iterator<R> iterator() {
		return Collections.unmodifiableList(getRows()).iterator();
	}

	/**
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		return this.lstRows.size();
	}
}

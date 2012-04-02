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
package org.nuclos.client.ui.table;

import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

/**
 * A sortable table model (replacement for TableSorter).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @deprecated Use Swing RowSorter instead
 */
@Deprecated
public interface SortableTableModel extends TableModel {

	/**
	 * Check if a certain column is sortable or not (i.e. image columns)
	 */
	public boolean isSortable(int column);
	
	
	/**
	 * Returns the current sort keys, or an empty list.
	 */
	public List<? extends SortKey> getSortKeys();
	
	/** 
	 * Sets the current sort keys.
	 * Note: At the moment, one the first sort key has an effect.
	 * @throws IllegalArgumentException if a column index is outside the range of the model
	 */
	public void setSortKeys(List<? extends SortKey> sortKeys, boolean sortImmediately) throws IllegalArgumentException;
	
	/**
	 * Toggles the sort order of the given column or make the column the primary sort column.
	 */
	public void toggleSortOrder(int column, boolean sortImmediately);
	
	/**
	 * sorts the table model and notifies table model listeners by firing a {@link SortableTableModelEvent}.
	 */
	void sort();

	/**
	 * adds a listener that gets notified when the sorting changes.
	 * @param listener
	 */
	void addSortingListener(ChangeListener listener);

	/**
	 * removes a listener that gets notified when the sorting changes.
	 * @param listener
	 */
	void removeSortingListener(ChangeListener listener);

}	// interface SortableTableModel

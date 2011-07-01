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

import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.SortableTableModelEvent;
import org.nuclos.common.collect.collectable.Collectable;

/**
 * A sortable <code>CollectableTableModel</code>, nothing more, nothing less.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface SortableCollectableTableModel <Clct extends Collectable> extends CollectableTableModel<Clct>, SortableTableModel {

	/**
	 * Returns the current sort keys, or an empty list.
	 */
	@Override
	public List<? extends SortKey> getSortKeys();
	
	/** 
	 * Sets the current sort keys.
	 * Note: At the moment, one the first sort key has an effect.
	 * @throws IllegalArgumentException if a column index is outside the range of the model
	 */
	@Override
	public void setSortKeys(List<? extends SortKey> sortKeys, boolean sortImmediately) throws IllegalArgumentException;
	
	/**
	 * Toggles the sort order of the given column or make the column the primary sort column.
	 */
	@Override
	public void toggleSortOrder(int column, boolean sortImmediately);
	
	/**
	 * sorts the table model and notifies table model listeners by firing a {@link SortableTableModelEvent}.
	 */
	@Override
	void sort();

	/**
	 * adds a listener that gets notified when the sorting changes.
	 * @param listener
	 */
	@Override
	void addSortingListener(ChangeListener listener);

	/**
	 * removes a listener that gets notified when the sorting changes.
	 * @param listener
	 */
	@Override
	void removeSortingListener(ChangeListener listener);
}

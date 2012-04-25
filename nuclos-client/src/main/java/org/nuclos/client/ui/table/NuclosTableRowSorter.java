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

import java.util.LinkedList;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.nuclos.client.ui.collect.model.SortableCollectableTableModelImpl;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;

/**
 * Default RowSorter implementation for JTables in Nuclos. 
 * Implements default sorting behaviour: Sort 3 column, switch between ascending, descending and unsorted.
 * 
 * @see SortableCollectableTableModelImpl
 * @author thomas.schiffmann
 */
public class NuclosTableRowSorter<T extends TableModel> extends TableRowSorter<T> {

	public NuclosTableRowSorter(T model) {
		super(model);
	}

	@Override
	public void toggleSortOrder(final int column) {
		if (column < 0 || column >= getModelWrapper().getColumnCount()) {
            throw new IndexOutOfBoundsException("column beyond range of TableModel");
        }
        
		if (!isSortable(column)) {
			return;
		}
		
		List<SortKey> newSortKeys = new LinkedList<SortKey>(getSortKeys());
		int currentSortIndex = CollectionUtils.indexOfFirst(getSortKeys(), new Predicate<SortKey>() {
			@Override public boolean evaluate(SortKey t) { return t.getColumn() == column; }
		});

		SortOrder newSortOrder = SortOrder.ASCENDING;
		int lastSortIndex = newSortKeys.size();
		if (currentSortIndex != -1) {
			SortKey oldSortKey = newSortKeys.remove(currentSortIndex);
			if (oldSortKey.getSortOrder() != SortOrder.UNSORTED) {
				switch (oldSortKey.getSortOrder()) {
				case ASCENDING:
					newSortOrder = SortOrder.DESCENDING;
					newSortKeys.add(currentSortIndex, new SortKey(column, newSortOrder));
					break;
				case DESCENDING:
					newSortOrder = SortOrder.UNSORTED;
					// if descending, nothing will be added. 3-click behavior. asc,desc,unsorted.
					newSortKeys.add(currentSortIndex, new SortKey(column, newSortOrder));	
					break;
				case UNSORTED:
					newSortOrder = SortOrder.ASCENDING;	
					newSortKeys.add(currentSortIndex, new SortKey(column, newSortOrder));		
					break;
				}
			} else {
				newSortOrder = SortOrder.ASCENDING;
				newSortKeys.add(lastSortIndex == 0 ? 0 : (lastSortIndex > newSortKeys.size() ? lastSortIndex - 1 : lastSortIndex), new SortKey(column, newSortOrder));		
			}
		} else {
			newSortOrder = SortOrder.ASCENDING;
			newSortKeys.add(lastSortIndex == 0 ? 0 : (lastSortIndex > newSortKeys.size() ? lastSortIndex - 1 : lastSortIndex), new SortKey(column, newSortOrder));		
		}

		List<SortKey> newSortKeys1 = new LinkedList<SortKey>();
		List<SortKey> newSortKeys2 = new LinkedList<SortKey>();
		for (SortKey sortKey : newSortKeys) {
			if (sortKey.getSortOrder() == SortOrder.UNSORTED) {
				newSortKeys1.add(sortKey);
			} else {
				newSortKeys2.add(sortKey);
			}
		}
		
		newSortKeys.clear();
		newSortKeys.addAll(newSortKeys2.subList(newSortKeys2.size() < 3 ? 0 : newSortKeys2.size()-3, newSortKeys2.size()));
		newSortKeys.addAll(newSortKeys1);
		setSortKeys(newSortKeys);
	}
}

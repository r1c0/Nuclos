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

package org.nuclos.client.ui.collect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.table.SortableTableModelEvent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableComparatorFactory;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.ComparatorUtils;
import org.nuclos.common.collection.Predicate;

/**
 * CollectableTableModelImpl that supports sorting.
 */
public class SortableCollectableTableModelImpl <Clct extends Collectable>
		extends CollectableTableModelImpl<Clct> 
		implements SortableCollectableTableModel<Clct> {

	private List<SortKey> sortKeys = Collections.emptyList();

	private final transient List<ChangeListener> lstSortingListeners = new LinkedList<ChangeListener>();
	
	/**
	 * @deprecated Use {@link #SortableCollectableTableModelImpl(String, List)}.
	 */
	public SortableCollectableTableModelImpl(String entityName) {
		this(entityName, new ArrayList<Clct>());
	}

	public SortableCollectableTableModelImpl(String entityName, List<Clct> lstclct) {
		super(entityName, lstclct);
	}	

	@Override
	public void setColumns(List<? extends CollectableEntityField> lstclctefColumns) {
		ensureMaxColumnSortKeys(lstclctefColumns.size());
		super.setColumns(lstclctefColumns);
	}

	@Override
	public void removeColumn(int iColumn) {
		ensureMaxColumnSortKeys(this.getColumnCount() - 1);
		super.removeColumn(iColumn);
	}

	@Override
	public List<? extends SortKey> getSortKeys() {
		return sortKeys;
	}
	
	@Override
	public void setSortKeys(List<? extends SortKey> sortKeys, boolean sortImmediately) throws IllegalArgumentException {
		for (SortKey sortKey : sortKeys) {
			if (!(sortKey.getColumn() >= 0 && sortKey.getColumn() < this.getColumnCount()))
				throw new IllegalArgumentException("Invalid sort column " + sortKey.getColumn());
		}
		
		if (!this.sortKeys.equals(sortKeys)) {
			this.sortKeys = Collections.unmodifiableList(new ArrayList<SortKey>(sortKeys));
			fireSortingChanged();
		}

		if (sortImmediately) {
			this.sort();
		}
	}

	@Override
	public void toggleSortOrder(final int column, boolean sortImmediately) {
		List<SortKey> newSortKeys = new LinkedList<SortKey>(sortKeys);
		int currentSortIndex = CollectionUtils.indexOfFirst(sortKeys, new Predicate<SortKey>() {
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

		if (newSortKeys.size() > 3) {
			int i1 = 0;
			List<SortKey> newSortKeys1 = new LinkedList<SortKey>();
			List<SortKey> newSortKeys2 = new LinkedList<SortKey>();
			for (SortKey sortKey : newSortKeys) {
				if (sortKey.getSortOrder() == SortOrder.UNSORTED && i1 < 3) {
					newSortKeys1.add(sortKey);
					i1++;
				} else {
					newSortKeys2.add(sortKey);
				}
			}
			
			newSortKeys.clear();
			newSortKeys.addAll(newSortKeys2.subList(newSortKeys2.size() < 3 ? 0 : newSortKeys2.size()-3, newSortKeys2.size()));
			newSortKeys.addAll(newSortKeys1);
			//newSortKeys = newSortKeys.subList(0,3);
		}
		setSortKeys(newSortKeys, sortImmediately);
	}
	
	/**
	 * addititional Method for restoring the sorting order without fireing any changes etc.
	 * needed for the WYSIWYG Editor.
	 * @param iSortedColumn
	 * @param bSortedAscending
	 * @deprecated refactor to use regular setSortKeys 
	 */
	@Deprecated 
	public void restoreSortingOrder(int iSortedColumn, boolean bSortedAscending) {
		if (!(iSortedColumn >= -1 && iSortedColumn < this.getColumnCount())) {
			throw new IllegalArgumentException("iSortedColumn");
		}
		this.sortKeys = Collections.singletonList(new SortKey(iSortedColumn, bSortedAscending ? SortOrder.ASCENDING : SortOrder.DESCENDING));
	}

	/**
	 * to be implemented by concrete classes to specify how two rows are compared.
	 * @param iColumn
	 * @return the <code>Comparator</code> used for ascending sorting of the given column.
	 * Descending sorting is implemented by using the reverse comparator of <code>result</code>.
	 * @precondition iColumn >= 0 && iColumn < this.getColumnCount()
	 */
	protected Comparator<Clct> getComparator(int column) {
		return (Comparator<Clct>) CollectableComparatorFactory.getInstance().newCollectableComparator(getBaseEntityName(), this.getCollectableEntityField(column));
		//new CollectableComparator<Clct>(this.getCollectableEntityField(iColumn));
	}

	@Override
	public void sort() {
		if (!sortKeys.isEmpty()) {
			List<Comparator<Clct>> comparators = new ArrayList<Comparator<Clct>>(sortKeys.size());
			for (SortKey sortKey : sortKeys) {
				Comparator<Clct> columnComparator = getComparator(sortKey.getColumn());
				if (columnComparator == null)
					continue;
				switch (sortKey.getSortOrder()) {
				case DESCENDING:
					columnComparator = Collections.reverseOrder(columnComparator);
					// fall-through
				case ASCENDING:
					comparators.add(columnComparator);
					break;
				}
			}
			// always add id comparator as compound.
			comparators.add((Comparator<Clct>) CollectableComparatorFactory.getInstance().newCollectableIdComparator());
			
			//if (!comparators.isEmpty()) { // will never happen.
				// This can happen if some sort keys with UNSORTED ordering are provided
				Comparator<Clct> comparator = ComparatorUtils.compoundComparator(comparators);
				this.sort(comparator);
				this.fireTableDataSorted();
			//}
		}
	}

	@Deprecated
	private void ensureMaxColumnSortKeys(final int maxColumn) {
		List<? extends SortKey> currentSortKeys = getSortKeys();
		List<SortKey> newSortKeys = CollectionUtils.applyFilter(currentSortKeys, new Predicate<SortKey>() {
			@Override public boolean evaluate(SortKey x) { return x.getColumn() < maxColumn; } 
		});
		if (!newSortKeys.equals(currentSortKeys)) {
			setSortKeys(newSortKeys, false);
		}
	}

	private void fireTableDataSorted() {
		this.fireTableChanged(new SortableTableModelEvent(this));
	}

	/**
	 * Sorts the list using the given comparator.
	 * @param comparatorToUse
	 */
	private void sort(Comparator<Clct> comparatorToUse) {
		Collections.sort(this.getRows(), comparatorToUse);
	}

	@Override
	public synchronized void addSortingListener(ChangeListener listener) {
		this.lstSortingListeners.add(listener);
	}

	@Override
	public synchronized void removeSortingListener(ChangeListener listener) {
		this.lstSortingListeners.remove(listener);
	}

	private synchronized void fireSortingChanged() {
		final ChangeEvent ev = new ChangeEvent(this);
		for (ChangeListener listener : lstSortingListeners) {
			listener.stateChanged(ev);
		}
	}
	
}  //  class CollectableTableModelImpl

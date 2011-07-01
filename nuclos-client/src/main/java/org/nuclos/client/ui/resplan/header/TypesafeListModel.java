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

package org.nuclos.client.ui.resplan.header;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * A type-safe ListModel extension.
 * @param <E>
 */
public interface TypesafeListModel<E> extends ListModel {

	public static class StaticListModel<T> extends AbstractListModel implements TypesafeListModel<T> {
	
		private static final long serialVersionUID = 1L;

		private T[] data;
		
		public StaticListModel() {
		}
		
		public StaticListModel(List<? extends T> items) {
			setData(items);
		}
		
		@Override
		public int getSize() {
			return (data != null) ?  data.length : 0;
		}
	
		@Override
		public T getElementAt(int index) {
			if (data == null)
				throw new IndexOutOfBoundsException();
			return data[index];
		}
		
		@SuppressWarnings("unchecked")
		public void setData(List<? extends T> items) {
			int oldSize = getSize();
			this.data = null;
			fireIntervalRemoved(this, 0, oldSize);
			if (items != null && items.size() > 0) {
				this.data = (T[]) items.toArray();
				fireContentsChanged(this, 0, items.size());
			}
		}
		
		public List<? extends T> getData() {
			return data != null ? Arrays.asList(data) : Collections.<T>emptyList();
		}
		
		@Override
		public int getGroupCount() {
			return 1;
		}
		
		@Override
		public Object getGroupValue(int category, int index) {
			return null;
		}
	}
	
	public abstract int getGroupCount();

	public Object getGroupValue(int category, int index);

	@Override
	public abstract E getElementAt(int index);

	public abstract ListDataListener[] getListDataListeners();
}

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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.nuclos.common.CloneUtils;

/**
 * A simple implementation of {@link ComboBoxModel} based on a <em>immutable</em>
 * Collection.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class SimpleCollectionComboBoxModel<T> implements ComboBoxModel {
	
	private final Collection<T> c;
	
	private T selected;
	
	public SimpleCollectionComboBoxModel(Collection<T> c) {
		this.c = c;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selected = (T) anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

	@Override
	public int getSize() {
		return c.size();
	}

	/**
	 * Attention: As the wrapped is immutable, there are no {@link ListDataEvent}s
	 * to report. Hence this method does nothing. 
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
	}

	/**
	 * Attention: As the wrapped is immutable, there are no {@link ListDataEvent}s
	 * to report. Hence this method does nothing. 
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
	}

	@Override
	public Object getElementAt(int index) {
		T result;
		if (c instanceof List) {
			final List<T> l = (List<T>) c;
			result = l.get(index);
		}
		else if (c instanceof SortedSet) {
			final Iterator<T> it = c.iterator();
			int i = 0;
			do {
				result = it.next();
			} while(++i < index);
		}
		else {
			throw new IllegalStateException();
		}
		return result;
	}

}

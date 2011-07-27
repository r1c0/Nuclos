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
package org.nuclos.common.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.nuclos.common2.LangUtils;

/**
 * Implements <code>List</code> by using (wrapping) another list.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class ListAdapter<W, E> implements List<E> {

	private final List<W> lstAdaptee;

	protected ListAdapter(List<W> lstAdaptee) {
		this.lstAdaptee = lstAdaptee;
	}

	/**
	 * wraps the given element.
	 * @param w
	 * @return a wrapper for w.
	 */
	protected abstract E wrap(W w);

	/**
	 * unwraps the given element.
	 * @param e
	 * @return the <code>W</code> contained in <code>e</code>.
	 */
	protected abstract W unwrap(E e);

	/**
	 * @return the list this adapter is wrapped around.
	 */
	protected List<W> adaptee() {
		return this.lstAdaptee;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean containsAll(Collection<?> coll) {
		return this.adaptee().containsAll(unwrap((Collection<? extends E>) coll));
	}

	/**
	 * @see List#equals(Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if (!(o instanceof List<?>)) {
			return false;
		}
		return this.equals((List<E>) o);
	}

	private boolean equals(List<E> that) {
		boolean result = (this.size() == that.size());
		if (result) {
			final Iterator<E> iterThis = this.iterator();
			final Iterator<E> iterThat = that.iterator();
			while (iterThis.hasNext()) {
				assert iterThat.hasNext();
				if (!LangUtils.equals(iterThis.next(), iterThat.next())) {
					result = false;
					break;
				}
			}
			assert !iterThis.hasNext();
			assert !iterThat.hasNext();
		}
		return result;
	}

	/**
	 * @return the hash code, as defined in List.
	 * @see List#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 1;
		for (E e : this) {
			result = 31 * result + LangUtils.hashCode(e);
		}
		return result;
	}

	@Override
	public int size() {
		return this.adaptee().size();
	}

	@Override
	public boolean isEmpty() {
		return this.adaptee().isEmpty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		return this.adaptee().contains(wrap((W) o));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public int indexOf(Object o) {
		return this.adaptee().indexOf(wrap((W) o));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public int lastIndexOf(Object o) {
		return this.adaptee().lastIndexOf(wrap((W) o));
	}

	@Override
	public E get(int iIndex) {
		return wrap(this.adaptee().get(iIndex));
	}

	@Override
	public E set(int iIndex, E e) {
		return wrap(this.adaptee().set(iIndex, unwrap(e)));
	}

	@Override
	public void add(int iIndex, E e) {
		this.adaptee().add(iIndex, unwrap(e));
	}

	@Override
	public boolean add(E e) {
		return this.adaptee().add(unwrap(e));
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		return this.adaptee().addAll(unwrap(coll));
	}

	@Override
	public boolean addAll(int iIndex, Collection<? extends E> coll) {
		return this.adaptee().addAll(iIndex, unwrap(coll));
	}

	@Override
	public void clear() {
		this.adaptee().clear();
	}

	@Override
	public Iterator<E> iterator() {
		return this.listIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return new MyListIterator();
	}

	/** @todo implement when needed */
	@Override
	public ListIterator<E> listIterator(int index) {
		// Note that this implementation doesn't conform to the specification of
		// List.listIterator(int index).
		throw new UnsupportedOperationException("listIterator");
		//		return new MyListIterator(index);
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T> T[] toArray(T[] at) {
		final T[] result;
		if (at.length < this.size()) {
			result = (T[]) java.lang.reflect.Array.newInstance(at.getClass().getComponentType(), this.size());
		}
		else {
			result = at;
		}
		int i = 0;
		for (Iterator<E> iter = this.iterator(); iter.hasNext();) {
			result[i++] = (T) iter.next();
		}
		while (i < at.length) {
			result[i++] = null;
		}
		return result;
	}

	@Override
	public Object[] toArray() {
		return this.toArray(new Object[this.size()]);
	}

	/** @todo implement when needed */
	@Override
	public List<E> subList(int iFromIndex, int iToIndex) {
		throw new UnsupportedOperationException("subList");
	}

	@Override
	public E remove(int iIndex) {
		return wrap(this.adaptee().remove(iIndex));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean remove(Object o) {
		return this.adaptee().remove(wrap((W) o));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean removeAll(Collection<?> coll) {
		return this.adaptee().removeAll(unwrap((Collection<? extends E>) coll));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean retainAll(Collection<?> coll) {
		return this.adaptee().retainAll(unwrap((Collection<? extends E>) coll));
	}

	private List<W> unwrap(Collection<? extends E> coll) {
		return CollectionUtils.transform(coll, new Unwrap());
	}

	/**
	 * inner class Wrap
	 */
	protected class Wrap implements Transformer<W, E> {
		@Override
		public E transform(W w) {
			return wrap(w);
		}
	}

	/**
	 * inner class Unwrap
	 */
	protected class Unwrap implements Transformer<E, W> {
		@Override
		public W transform(E e) {
			return unwrap(e);
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append(lstAdaptee);
		result.append("]");
		return result.toString();
	}

	/**
	 * ListIterator for this list adapter.
	 */
	protected class MyListIterator implements ListIterator<E> {
		int index;

		protected MyListIterator() {
			this.index = -1;
		}

		@Override
		public void add(E o) {
			ListAdapter.this.add(index, o);
			++index;
		}

		@Override
		public boolean hasNext() {
			return index + 1 < ListAdapter.this.size();
		}

		@Override
		public boolean hasPrevious() {
			return index - 1 >= 0;
		}

		@Override
		public E next() {
			++index;
			return ListAdapter.this.get(index);
		}

		@Override
		public int nextIndex() {
			return index + 1;
		}

		@Override
		public E previous() {
			--index;
			return ListAdapter.this.get(index);
		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Override
		public void remove() {
			ListAdapter.this.remove(index);
			--index;
		}

		@Override
		public void set(E o) {
			ListAdapter.this.set(index, o);
		}

	}	// inner class MyListIterator

}	// class ListAdapter

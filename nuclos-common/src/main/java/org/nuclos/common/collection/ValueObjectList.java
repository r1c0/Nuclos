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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.nuclos.common2.exception.CommonFatalException;

/**
 * List of "value objects" (EJB Design Pattern).
 * Objects that are added after construction are stored in a separate private collection.
 * Removed objects are taken from the list itself,
 * but additionally, <code>markRemoved()</code> is called on those objects that were not added
 * after construction, and the object is stored in a collection of removed objects,
 * which can be queried via <code>getRemovedObjects()</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class ValueObjectList<E> implements List<E>, Cloneable {

	private List<E> lstvo;
	protected Collection<E> collAdded = new HashSet<E>();
	protected Collection<E> collRemoved = new HashSet<E>();

	public ValueObjectList() {
		this(null);
	}

	/**
	 * creates a value object list containing the elements of the given collection that are not marked removed.
	 * The elements that are marked removed will be contained in <code>getRemovedObjects()</code>.
	 * @param collvo
	 */
	public ValueObjectList(Collection<E> collvo) {
		this.lstvo = (collvo == null) ? new ArrayList<E>() : new ArrayList<E>(collvo);

		// move removed elements to the "collection of removed elements":
		for (Iterator<E> iter = this.lstvo.iterator(); iter.hasNext();) {
			final E e = iter.next();
			if (this.isMarkedRemoved(e)) {
				iter.remove();
				this.collRemoved.add(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValueObjectList<E> clone() {
		final ValueObjectList<E> result;
		try {
			result = (ValueObjectList<E>) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			throw new CommonFatalException(ex);
		}

		// deepen shallow copy:
		result.lstvo = new ArrayList<E>(lstvo);
		result.collAdded = new HashSet<E>(collAdded);
		result.collRemoved = new HashSet<E>(collRemoved);

		return result;
	}

	/**
	 * marks the given object removed.
	 * @param e
	 */
	protected abstract void markRemoved(E e);

	/**
	 * @param e
	 * @return Is the given object marked removed?
	 */
	protected abstract boolean isMarkedRemoved(E e);

	/**
	 * @return the objects that have been added since the construction of this list.
	 * @todo specify this more clearly
	 */
	public Collection<E> getAddedObjects() {
		return Collections.unmodifiableCollection(this.collAdded);
	}

	/**
	 * @return the objects that have been removed since the construction of this list.
	 * @todo specify this more clearly
	 */
	public Collection<E> getRemovedObjects() {
		return Collections.unmodifiableCollection(this.collRemoved);
	}

	/**
	 * removes the given object from this list.
	 * @param o
	 * @return {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if (this.collAdded.contains(o)) {
			this.collAdded.remove(o);
		}
		else {
			this.markRemoved((E) o);
			this.collRemoved.add((E) o);
		}
		return lstvo.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.lstvo.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		final int iOldSize = size();
		for (Object o : coll) {
			this.remove(o);
		}
		return (size() != iOldSize);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/** @todo calling remove on this iterator doesn't call markRemoved and
	 * doesn't add the object to the removed objects neither */
	@Override
	public Iterator<E> iterator() {
		return lstvo.iterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return lstvo.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int iFromIndex) {
		return lstvo.listIterator(iFromIndex);
	}

	@Override
	public List<E> subList(int iFromIndex, int iToIndex) {
		return lstvo.subList(iFromIndex, iToIndex);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ValueObjectList<?>)) {
			return false;
		}
		final ValueObjectList<?> that = (ValueObjectList<?>) o;

		return this.lstvo.equals(that.lstvo) && this.collRemoved.equals(that.collRemoved);
	}

	@Override
	public int hashCode() {
		return lstvo.hashCode() ^ collRemoved.hashCode();
	}

	@Override
	public int size() {
		return lstvo.size();
	}

	@Override
	public boolean isEmpty() {
		return lstvo.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return lstvo.contains(o);
	}

	@Override
	public int indexOf(Object o) {
		return lstvo.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return lstvo.lastIndexOf(o);
	}

	@Override
	public Object[] toArray() {
		return lstvo.toArray();
	}

	@Override
	public <T> T[] toArray(T[] at) {
		return lstvo.toArray(at);
	}

	@Override
	public E get(int iIndex) {
		return lstvo.get(iIndex);
	}

	@Override
	public E set(int iIndex, E e) {
		return lstvo.set(iIndex, e);
	}

	@Override
	public boolean add(E e) {
		final boolean result = this.lstvo.add(e);
		if (result) {
			this.collAdded.add(e);
		}
		return result;
	}

	@Override
	public void add(int iIndex, E e) {
		this.lstvo.add(iIndex, e);
		this.collAdded.add(e);
	}

	@Override
	public E remove(int iIndex) {
		final E result = this.get(iIndex);
		this.remove(result);
		return result;
	}

	@Override
	public void clear() {
		for (Iterator<E> iter = lstvo.listIterator(); iter.hasNext();) {
			this.remove(iter.next());
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		final boolean result = lstvo.addAll(coll);
		this.collAdded.addAll(coll);
		return result;
	}

	@Override
	public boolean addAll(int iIndex, Collection<? extends E> coll) {
		boolean result = lstvo.addAll(iIndex, coll);
		this.collAdded.addAll(coll);
		return result;
	}

}	// class ValueObjectList

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
import java.util.List;
import java.util.ListIterator;

/**
 * Decorator for a list. Implements the GoF Decorator pattern for lists.
 * Superclasses may provide changed or additional behavior for the list, as long as they
 * don't violate the contract of the List interface.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @deprecated Use Apache Commons Collections.
 */
public class ListDecorator<E> extends GenericCollectionDecorator<E, List<E>> implements List<E> {

	/**
	 * Note that it's not useful to create direct instances of this class.
	 * @param lst
	 */
	protected ListDecorator(List<E> lst) {
		super(lst);
	}

	@Override
	public void add(int iIndex, E e) {
		this.getWrapped().add(iIndex,  e);
	}

	@Override
	public boolean addAll(int iIndex, Collection<? extends E> coll) {
		return this.getWrapped().addAll(iIndex, coll);
	}

	@Override
	public E get(int iIndex) {
		return this.getWrapped().get(iIndex);
	}

	@Override
	public int indexOf(Object o) {
		return this.getWrapped().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.getWrapped().lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return this.getWrapped().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int iIndex) {
		return this.getWrapped().listIterator(iIndex);
	}

	@Override
	public E remove(int iIndex) {
		return this.getWrapped().remove(0);
	}

	@Override
	public E set(int iIndex, E e) {
		return this.getWrapped().set(iIndex, e);
	}

	@Override
	public List<E> subList(int iFromIndex, int iToIndex) {
		return this.getWrapped().subList(iFromIndex, iToIndex);
	}

}	// class ListDecorator

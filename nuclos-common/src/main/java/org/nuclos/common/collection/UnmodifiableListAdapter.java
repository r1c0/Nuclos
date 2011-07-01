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

/**
 * Implements <code>List</code> by using (wrapping) another list.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class UnmodifiableListAdapter<I, E> extends UnmodifiableList<E> {

	private final List<I> lst;

	protected UnmodifiableListAdapter(List<I> lst) {
		this.lst = lst;
	}

	protected abstract E wrap(I i);

	protected abstract I unwrap(E e);

	protected List<I> getList() {
		return this.lst;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return lst.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		// wrapping the whole list would be overkill here
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public int size() {
		return lst.size();
	}

	@Override
	public boolean isEmpty() {
		return lst.isEmpty();
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean contains(Object o) {
		return lst.contains(wrap((I) o));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public int indexOf(Object o) {
		return lst.indexOf(wrap((I) o));
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public int lastIndexOf(Object o) {
		return lst.lastIndexOf(wrap((I) o));
	}

	@Override
	public E get(int iIndex) {
		return wrap(lst.get(iIndex));
	}

	@Override
	public E set(int iIndex, E e) {
		return wrap(lst.set(iIndex, unwrap(e)));
	}

}	// class UnmodifiableListAdapter

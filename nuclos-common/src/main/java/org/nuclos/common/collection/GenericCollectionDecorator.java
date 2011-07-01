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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract decorator for a generic collection.
 * Implements the GoF Decorator pattern for collections.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericCollectionDecorator<E, Coll extends Collection<E>> implements Collection<E>, Serializable {

	private final Coll coll;

	/**
	 * Note that it's not useful to create direct instances of this class.
	 * @param coll
	 */
	protected GenericCollectionDecorator(Coll coll) {
		this.coll = coll;
	}

	protected Coll getWrapped() {
		return this.coll;
	}

	@Override
	public boolean add(E o) {
		return this.getWrapped().add(o);
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		return this.getWrapped().addAll(coll);
	}

	@Override
	public void clear() {
		this.getWrapped().clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.getWrapped().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return this.getWrapped().containsAll(coll);
	}

	@Override
	public boolean isEmpty() {
		return this.getWrapped().isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.getWrapped().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return this.getWrapped().remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return this.getWrapped().removeAll(coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return this.getWrapped().retainAll(coll);
	}

	@Override
	public int size() {
		return this.getWrapped().size();
	}

	@Override
	public Object[] toArray() {
		return this.getWrapped().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.getWrapped().toArray(a);
	}

	@Override
	public boolean equals(Object o) {
		return this.getWrapped().equals(o);
	}

	@Override
	public int hashCode() {
		return this.getWrapped().hashCode();
	}

	@Override
	public String toString() {
		return this.getWrapped().toString();
	}

}	// class GenericCollectionDecorator

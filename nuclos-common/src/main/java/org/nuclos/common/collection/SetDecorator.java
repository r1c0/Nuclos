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

import java.util.Set;

/**
 * Decorator for a set. Implements the GoF Decorator pattern for sets.
 * Superclasses may provide changed or additional behavior for the set, as long as they
 * don't violate the contract of the Set interface.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class SetDecorator<E> extends GenericCollectionDecorator<E, Set<E>> implements Set<E> {

	/**
	 * Note that it's not useful to create direct instances of this class.
	 * @param st
	 */
	protected SetDecorator(Set<E> st) {
		super(st);
	}

}	// class ListDecorator

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

import java.util.List;

/**
 * <code>ValueObjectList</code> that doesn't mark its elements as removed. Thus this list may only contain
 * objects that do not need to be marked as removed.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NonMarkingValueObjectList<E> extends ValueObjectList<E> {

	public NonMarkingValueObjectList(List<E> lstclct) {
		super(lstclct);
	}

	/**
	 * NOP (doesn't do anything). The objects in this list do not need to be marked as removed.
	 * @param e
	 */
	@Override
	protected void markRemoved(E e) {
		// do nothing
	}

	/**
	 * The objects in this list are per definition never marked as removed.
	 * @param e
	 * @return false
	 * @postcondition !result
	 */
	@Override
	protected boolean isMarkedRemoved(E e) {
		return false;
	}

}	// class NonMarkingValueObjectList

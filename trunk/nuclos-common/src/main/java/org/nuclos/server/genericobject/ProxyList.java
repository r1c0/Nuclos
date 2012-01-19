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
package org.nuclos.server.genericobject;

import javax.swing.event.ChangeListener;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Proxy list that loads its elements chunkwise. Can be used for huge search results that are displayed partially.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public interface ProxyList<E> extends List<E>, Serializable {


	/**
	 * fetches the data for all entries at least up to (including) the given index.
	 * @param iIndex
	 * @param changelistener Optional <code>ChangeListener</code> that gets notified when the last index was increased.
	 */
	void fetchDataIfNecessary(int iIndex, ChangeListener changelistener);

	/**
	 * @return the index of the last element that has already been read.
	 */
	int getLastIndexRead();

	boolean hasObjectBeenReadForIndex(int index);

	@Override
	public Iterator<E> iterator();

	/**
	 * get index by id of object in list
	 * @param oId the object's id
	 * @return index of object in list; -1 if not found
	 */
	public int getIndexById(Object oId);

}	// interface ProxyList

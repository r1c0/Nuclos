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
package org.nuclos.common2;

import org.nuclos.common.collection.GenericCollectionDecorator;
import org.apache.commons.lang.NullArgumentException;
import java.io.Serializable;
import java.util.Collection;

/**
 * Makes a regular <code>Collection</code> truncatable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class TruncatableCollectionDecorator<E>
		extends GenericCollectionDecorator<E, Collection<E>>
		implements TruncatableCollection<E>, Serializable {

	private final boolean bTruncated;
	private final int iTotalSize;

	/**
	 *
	 * @param coll
	 * @param bTruncated
	 * @param iTotalSize
	 * @precondition coll != null
	 * @precondition iTotalSize >= coll.size()
	 */
	public TruncatableCollectionDecorator(Collection<E> coll, boolean bTruncated, int iTotalSize) {
		super(coll);
		if(coll == null) {
			throw new NullArgumentException("coll");
		}
		if(iTotalSize < coll.size()) {
			throw new IllegalArgumentException("iTotalSize < coll.size()");
		}
		this.bTruncated = bTruncated;
		this.iTotalSize = iTotalSize;
	}

	@Override
	public boolean isTruncated() {
		return this.bTruncated;
	}

	@Override
	public int totalSize() {
		final int result = this.iTotalSize;
		assert result >= this.size();
		return result;
	}

	/** @todo decrement total record count here? */
	@Override
	public boolean remove(Object o) {
		return super.remove(o);
	}

	/** @todo respect truncated and total record count here? */
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	/** @todo respect truncated and total record count here? */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/** @todo respect truncated and total record count here! */
	@Override
	public boolean add(E o) {
		return super.add(o);
	}

	/** @todo respect truncated and total record count here! */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	/** @todo respect truncated and total record count here! */
	@Override
	public void clear() {
	   super.clear();
	}

}  // class TruncatableCollectionDecorator

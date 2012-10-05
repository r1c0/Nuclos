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

import org.nuclos.common.HasId;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.valueobject.AbstractNuclosValueObject;
import org.nuclos.server.masterdata.MasterDataProxyList;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Abstract implementation of a <code>ProxyList</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class AbstractProxyList<T, E extends HasId<T>> implements ProxyList<E>, Serializable {

	/** @todo tune the PAGESIZE, better: make it configurable in users advanced settings dialogue */
	public static final int PAGESIZE = 100;

	private static final long serialVersionUID = 815L;

	/**
	 * the index of the last element that has already bean read.
	 */
	private volatile int iLastIndexRead = 0;

	/**
	 * The real data containers: all ids
	 */
	private ArrayList<T> lstIds;
	
	/**
	 * The real data containers: id -> values
	 */
	protected final HashMap<T, E> mpObjects;

	public AbstractProxyList() {
		mpObjects = new HashMap<T, E>();
	}

	/**
	 * reads the first chunk if necessary. Must be called as the last statement of the ctor in subclasses.
	 */
	protected void initialize() {
		fillListOfIds();

		if (this.size() > 0) {
			this.get(0);
		}
	}

	/**
	 * read ids of all displayable objects in advance
	 */
	protected abstract void fillListOfIds() ;

	protected final void setListOfIds(List<T> lstIds) {
		this.lstIds = new ArrayList<T>(lstIds);
	}

	/**
	 * fetches the next chunk from the underlying data source.
	 * @return
	 * @throws RemoteException
	 */
	protected abstract Collection<E> fetchNextChunk(List<T> lstIntIds) throws RemoteException;
	protected abstract Collection<E> fetchChunk(int istart, int iend) throws RemoteException;

	@Override
	public int size() {
		return this.lstIds.size();
	}

	private static boolean newLazyLoad = false;
	protected boolean isNewLL() {
		return newLazyLoad && this instanceof MasterDataProxyList;
	}

	/**
	 * @param iIndex
	 * @return
	 */
	@Override
	public E get(int iIndex) {
		if (isNewLL()) {
			this.fetchDataIfNecessary(iIndex, null);
			return mpObjects.get(iIndex);
		}
		if (iIndex >= this.size()) {
			throw new IndexOutOfBoundsException(Integer.toString(iIndex));
		}
		this.fetchDataIfNecessary(iIndex, null);
		return mpObjects.get(lstIds.get(iIndex));
	}

	/**
	 * gets the data for all entries at least up to (including) the given index.
	 * @param iIndex
	 * @param changelistener Optional <code>ChangeListener</code> that gets notified when the last index was increased.
	 */
	@Override
	public void fetchDataIfNecessary(int iIndex, ChangeListener changelistener) {
		if (isNewLL()) {
			this.fetchData(iIndex, changelistener);
			return;
		}
		if (iIndex >= size()) {
			throw new IndexOutOfBoundsException(Integer.toString(iIndex));
		}
		if (!hasObjectBeenReadForIndex(iIndex)) {
			// Reading an element that has already been loaded is not a problem.
			// If a higher index is requested however, we have to wait if the next chunk is being fetched already:
			synchronized (this) {
				if (!hasObjectBeenReadForIndex(iIndex)) {
					this.fetchData(iIndex, changelistener);
				}
			}
		}
	}
	
	private void fetchDataChunk(int istart, int iend) throws RemoteException {
		System.out.println("FDC: " + istart + "/" + iend);
		Collection<E> mdwv = this.fetchChunk(istart, iend);
		int icount = 0;
		for (Iterator<E> iter = mdwv.iterator(); iter.hasNext();) {
			E eRow = iter.next();
			T tIndex = (T)Integer.valueOf(icount + istart);
			mpObjects.put(tIndex, eRow);
			icount++;
			System.out.println("PUT:" + icount + " OBJ:" + eRow);
			if (icount + istart > iLastIndexRead) iLastIndexRead = icount + istart;
		}
	}

	private void fetchDataChunk(int iIndex, ChangeListener changelistener) {
		try {
			int iIndexStart = Math.max(0, iIndex - PAGESIZE);
			int iIndexEnd = Math.min(size(), iIndex + PAGESIZE);
			
			System.out.println("FDC1: " + iIndexStart + "/" + iIndexStart);
			
			int blockstart = -1;
			for (int i = iIndexStart; i <= iIndexEnd; i++) {
				if (mpObjects.get(i) == null) {
					if (blockstart == -1) blockstart = i;
					continue;
				}
				if (blockstart == -1) continue;
				fetchDataChunk(blockstart, i);
				blockstart = -1;
			}
			if (blockstart != -1) fetchDataChunk(blockstart, iIndexEnd);
			
			if (changelistener != null) {
				changelistener.stateChanged(new ChangeEvent(this));
			}
		}
		catch (RemoteException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	

	@Override
	public boolean hasObjectBeenReadForIndex(int index) {
		if (isNewLL()) {
			return mpObjects.get(index) != null;
		}
		return (index >= lstIds.size()) || (index == -1) ? false : mpObjects.get(lstIds.get(index)) != null;
	}

	/**
	 * fetches the data for all entries around the given index (one pagesize before and one pagesize after).
	 * @param iIndex
	 * @param changelistener Optional <code>ChangeListener</code> that gets notified when the last index was increased.
	 * @precondition iIndex > this.getLastIndexRead()
	 */
	private void fetchData(int iIndex, ChangeListener changelistener) {
		if (isNewLL()) {
			fetchDataChunk(iIndex, changelistener);
			return;
		}
		try {
			// read next page:
			E eRow = null;

			int iIndexStart = Math.max(0, iIndex - PAGESIZE);
			int iIndexEnd = Math.min(size(), iIndex + PAGESIZE);

			// Determine which objects are still to read by their indexes
			final List<T> lstIndexesToRead = new ArrayList<T>(iIndexEnd - iIndexStart);
			for(int iCurrentIndex = iIndexStart; iCurrentIndex < iIndexEnd; iCurrentIndex++) {
				if(mpObjects.get(lstIds.get(iCurrentIndex)) == null) {
					lstIndexesToRead.add(lstIds.get(iCurrentIndex));
				}
			}

			// Read the objects into here
			for (Iterator<E> iter = this.fetchNextChunk(lstIndexesToRead).iterator(); iter.hasNext();) {
				eRow = iter.next();
				lstIndexesToRead.remove(eRow.getId()); // Remember it could be read
				mpObjects.put((T) eRow.getId(), eRow);
			}
			//
			iLastIndexRead = iIndexEnd - 1 - lstIndexesToRead.size();
			Logger.getLogger(this.getClass()).debug("iLastIndexRead=" + iLastIndexRead);
			// Remove all objects and indexes from memory which could not be read
			// (could have been deleted in between)
			for(Object o : lstIndexesToRead) {
				remove(o);
			}
			if (changelistener != null) {
				changelistener.stateChanged(new ChangeEvent(this));
			}
		}
		catch (RemoteException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public boolean add(E element) {
		assert element != null;
		T intId = (T) element.getId();
		lstIds.add(intId);
		mpObjects.put(intId, element);

		return true;
	}

	@Override
	public void add(int index, E element) {
		assert element != null;
		T intId = (T) element.getId();
		lstIds.add(index, intId);
		mpObjects.put(intId, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		for(E element : coll) {
			add(element);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		if(o instanceof AbstractNuclosValueObject<?>) {
			return lstIds.contains(((AbstractNuclosValueObject<T>)o).getId());
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o : c) {
			if(!contains(o)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		if(o instanceof AbstractNuclosValueObject<?>) {
			AbstractNuclosValueObject<T> aevo = (AbstractNuclosValueObject<T>) o;
			final Object oId = aevo.getId();
			mpObjects.remove(oId);
			return lstIds.remove(oId);
		}

		return false;
	}

	@Override
	public E remove(int iIndex) {
		assert(lstIds.size() > iIndex);

		final Object oId = lstIds.remove(iIndex);
		final E result = mpObjects.remove(oId);

		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		Object oOldId = lstIds.get(index);
		E oldValue = mpObjects.remove(oOldId);

		T intId = (T) element.getId();
		lstIds.set(index, intId);
		mpObjects.put(intId, element);

		return oldValue;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <X> X[] toArray(X[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		this.lstIds.clear();
		this.mpObjects.clear();
	}

	@Override
	public boolean isEmpty() {
		return lstIds.isEmpty();
	}

	@Override
	public int getLastIndexRead() {
		return this.iLastIndexRead;
	}

	protected void setLastIndexRead(int iIndex) {
		this.iLastIndexRead = iIndex;
	}

	/**
	 * get index by id of object in list
	 * @param oId the object's id
	 * @return index of object in list; -1 if not found
	 */
	@Override
	public int getIndexById(Object oId) {
		for (int i = 0; i < lstIds.size(); i++) {
			T checkid = lstIds.get(i);
			if (LangUtils.equals(oId, checkid)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return an <code>Iterator</code> that doesn't check for concurrent comodification.
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int iIndex = -1;

			@Override
			public boolean hasNext() {
				return this.iIndex + 1 < AbstractProxyList.this.size();
			}

			@Override
			public E next() {
				return AbstractProxyList.this.get(++this.iIndex);
			}

			@Override
			public void remove() {
				AbstractProxyList.this.remove(this.iIndex);
			}
		};
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final int size = size();
		result.append(getClass().getName()).append("[");
		result.append("size=").append(size);
		mapDescription(result, mpObjects, 5);
		result.append("]");
		return result.toString();
	}
	
	protected void mapDescription(StringBuilder result, Map<T,E> map, int max) {
		int i = 0;
		for (T id: mpObjects.keySet()) {
			final E vo = mpObjects.get(id);
			result.append(", id:").append(id).append(" -> ").append(vo);
			if (++i > max) break; 
		}
	}

}  // class AbstractProxyList

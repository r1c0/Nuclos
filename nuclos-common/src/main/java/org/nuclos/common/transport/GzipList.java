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
package org.nuclos.common.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Delegating list to an underlying array list. Compresses contents for
 * serialization.
 */
public class GzipList<T> implements List<T>, Serializable, Externalizable {
	private ArrayList<T>  delegate;
	
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		byte[] b = new byte[len];
		in.readFully(b);

		ByteArrayInputStream bi = new ByteArrayInputStream(b);
		ObjectInputStream oi = new ObjectInputStream(new GZIPInputStream(bi));
		delegate = (ArrayList<T>) oi.readObject();
		oi.close();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(new GZIPOutputStream(bo));
		os.writeObject(delegate);
		os.close();      
		byte[] b = bo.toByteArray();
		out.writeInt(b.length);
		out.write(b);
	}


	public GzipList(List<T> delegate) {
		if(delegate instanceof ArrayList)
			this.delegate = (ArrayList<T>) delegate;
		else
			this.delegate = new ArrayList<T>(delegate);
    }

	public GzipList() {
		this.delegate = new ArrayList<T>();
	}
	
	public void trimToSize() {
	    delegate.trimToSize();
    }

	public void ensureCapacity(int minCapacity) {
	    delegate.ensureCapacity(minCapacity);
    }

	@Override
    public int size() {
	    return delegate.size();
    }

	@Override
    public boolean isEmpty() {
	    return delegate.isEmpty();
    }

	@Override
    public boolean contains(Object o) {
	    return delegate.contains(o);
    }

	@Override
    public int indexOf(Object o) {
	    return delegate.indexOf(o);
    }

	@Override
    public int lastIndexOf(Object o) {
	    return delegate.lastIndexOf(o);
    }

	@Override
    public Iterator<T> iterator() {
	    return delegate.iterator();
    }

	@Override
    public boolean containsAll(Collection<?> c) {
	    return delegate.containsAll(c);
    }

	@Override
    public Object clone() {
	    return delegate.clone();
    }

	@Override
    public ListIterator<T> listIterator() {
	    return delegate.listIterator();
    }

	@Override
    public Object[] toArray() {
	    return delegate.toArray();
    }

	@Override
    public ListIterator<T> listIterator(int index) {
	    return delegate.listIterator(index);
    }

    @Override
    public <T> T[] toArray(T[] a) {
	    return delegate.toArray(a);
    }

	@Override
    public boolean removeAll(Collection<?> c) {
	    return delegate.removeAll(c);
    }

	@Override
    public boolean retainAll(Collection<?> c) {
	    return delegate.retainAll(c);
    }

	@Override
    public T get(int index) {
	    return delegate.get(index);
    }

	@Override
    public T set(int index, T element) {
	    return delegate.set(index, element);
    }

	@Override
    public boolean add(T e) {
	    return delegate.add(e);
    }

	@Override
    public void add(int index, T element) {
	    delegate.add(index, element);
    }

	@Override
    public String toString() {
	    return delegate.toString();
    }

	@Override
    public List<T> subList(int fromIndex, int toIndex) {
	    return delegate.subList(fromIndex, toIndex);
    }

	@Override
    public T remove(int index) {
	    return delegate.remove(index);
    }

	@Override
    public boolean remove(Object o) {
	    return delegate.remove(o);
    }

	@Override
    public boolean equals(Object o) {
	    return delegate.equals(o);
    }

	@Override
    public void clear() {
	    delegate.clear();
    }

	@Override
    public boolean addAll(Collection<? extends T> c) {
	    return delegate.addAll(c);
    }

	@Override
    public boolean addAll(int index, Collection<? extends T> c) {
	    return delegate.addAll(index, c);
    }

	@Override
    public int hashCode() {
	    return delegate.hashCode();
    }
	
}

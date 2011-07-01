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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipMap<K, V> implements Map<K, V>, Serializable, Externalizable {
    private static final long serialVersionUID = -8900892350061284005L;
    
	private HashMap<K, V> delegate;

	public GzipMap() {
		delegate = new HashMap<K, V>();
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		byte[] b = new byte[len];
		in.readFully(b);

		ByteArrayInputStream bi = new ByteArrayInputStream(b);
		ObjectInputStream oi = new ObjectInputStream(new GZIPInputStream(bi));
		delegate = (HashMap<K, V>) oi.readObject();
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
	
	@Override
    public int size() {
	    return delegate.size();
    }

	@Override
    public boolean isEmpty() {
	    return delegate.isEmpty();
    }

	@Override
    public V get(Object key) {
	    return delegate.get(key);
    }

	@Override
    public boolean equals(Object o) {
	    return delegate.equals(o);
    }

	@Override
    public boolean containsKey(Object key) {
	    return delegate.containsKey(key);
    }

	@Override
    public V put(K key, V value) {
	    return delegate.put(key, value);
    }

	@Override
    public int hashCode() {
	    return delegate.hashCode();
    }

	@Override
    public String toString() {
	    return delegate.toString();
    }

	@Override
    public void putAll(Map<? extends K, ? extends V> m) {
	    delegate.putAll(m);
    }

	@Override
    public V remove(Object key) {
	    return delegate.remove(key);
    }

	@Override
    public void clear() {
	    delegate.clear();
    }

	@Override
    public boolean containsValue(Object value) {
	    return delegate.containsValue(value);
    }

	@Override
    public Object clone() {
	    return delegate.clone();
    }

	@Override
    public Set<K> keySet() {
	    return delegate.keySet();
    }

	@Override
    public Collection<V> values() {
	    return delegate.values();
    }

	@Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
	    return delegate.entrySet();
    }
}

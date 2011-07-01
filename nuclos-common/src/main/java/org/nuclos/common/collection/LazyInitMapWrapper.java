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
import java.util.Map;
import java.util.Set;


public class LazyInitMapWrapper<K, V> implements Map<K, V> {
	private enum CreationType {
		FACTORY, TRANSFORM;
	}

	private Map<K, V>          delegateMap;
	private Factory<V>         vFact;
	private Transformer<K, V>  transformer;
	private CreationType       creType;
	
	public LazyInitMapWrapper(Map<K, V> map, Factory<V> vFact) {
		this.delegateMap = map;
		this.vFact = vFact;
		this.creType = CreationType.FACTORY;
	}

	public LazyInitMapWrapper(Map<K, V> map, Transformer<K, V> transformer) {
		this.delegateMap = map;
		this.transformer = transformer;
		this.creType = CreationType.TRANSFORM;
	}

	private V create(K k) {
		switch(creType) {
		case FACTORY:
			return vFact.create();
		case TRANSFORM:
			return transformer.transform(k);
		}
		return null;  // cannot happen
	}
	
	public Map<K, V> getDelegateMap() {
		return delegateMap;
	}
	
	@Override
    public int size() {
	    return delegateMap.size();
    }

	@Override
    public boolean isEmpty() {
	    return delegateMap.isEmpty();
    }

	@Override
    public boolean containsKey(Object key) {
	    return delegateMap.containsKey(key);
    }

	@Override
    public boolean containsValue(Object value) {
	    return delegateMap.containsValue(value);
    }

	@SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
	    V v = delegateMap.get(key);
	    if(v == null) {
	    	K k = (K) key;
	    	v = create(k);
	    	delegateMap.put(k, v);
	    }
	    return v;
    }

	@Override
    public V put(K key, V value) {
	    return delegateMap.put(key, value);
    }

	@Override
    public V remove(Object key) {
	    return delegateMap.remove(key);
    }

	@Override
    public void putAll(Map<? extends K, ? extends V> m) {
	    delegateMap.putAll(m);
    }

	@Override
    public void clear() {
	    delegateMap.clear();
    }

	@Override
    public Set<K> keySet() {
	    return delegateMap.keySet();
    }

	@Override
    public Collection<V> values() {
	    return delegateMap.values();
    }

	@Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
	    return delegateMap.entrySet();
    }

	@Override
    public boolean equals(Object o) {
	    return delegateMap.equals(o);
    }

	@Override
    public int hashCode() {
	    return delegateMap.hashCode();
    }
}

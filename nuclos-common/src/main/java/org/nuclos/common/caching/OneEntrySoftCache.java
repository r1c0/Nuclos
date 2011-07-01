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

package org.nuclos.common.caching;

import java.lang.ref.SoftReference;

import org.nuclos.common.caching.NBCache.LookupProvider;

/**
 * A cache which stores at most one key/value pair. The value is 
 * stored as {@link SoftReference soft reference}.
 * If a {@link LookupProvider} is specified, the {@link #get(Object)}
 * method will automatically perform a lookup if no value is stored
 * for the given key and update the cache.
 */
public class OneEntrySoftCache<K, V> {

	private final LookupProvider<K, V> lookupProvider;
	private K cachedKey;
	private SoftReference<V> cachedValue;
	
	public OneEntrySoftCache() {
		this(null);
	}
	
	public OneEntrySoftCache(LookupProvider<K, V> lookupProvider) {
		this.lookupProvider = lookupProvider;
	}
	
	public synchronized V get(K key) {
		V value = null;
		if (key.equals(cachedKey)) {
			value = cachedValue.get();
		}
		if (value == null && lookupProvider != null) {
			value = lookupProvider.lookup(key);
			put(key, value);
		}
		return value;
	}
	
	public synchronized void put(K key, V value) {
		clear();
		cachedKey = key;
		cachedValue = new SoftReference<V>(value);
	}
	
	public synchronized void clear() {
		cachedKey = null;
		if (cachedValue != null) {
			cachedValue.clear();
			cachedValue = null;
		}
	}
}

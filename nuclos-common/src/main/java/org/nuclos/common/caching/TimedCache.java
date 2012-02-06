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
/*
 * Created on 26.08.2009
 */
package org.nuclos.common.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Pair;

public class TimedCache<K, V> implements NBCache<K, V> {
	
	private static final Logger LOG = Logger.getLogger(TimedCache.class);
		
	private LookupProvider<K, V>                 look;
	private long                                 maxAgeMillis;
	private Map<K, Pair<V, Long>>            map;
	
	public TimedCache(LookupProvider<K, V> lookupProvider, int maxAgeSeconds) {
		this.look = lookupProvider;
		this.maxAgeMillis = maxAgeSeconds * 1000L;
		this.map = new ConcurrentHashMap<K, Pair<V, Long>>();
	}
	
	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public V get(K key) {
		boolean log = LOG.isDebugEnabled();
		
		long ct = System.currentTimeMillis();
		if(map.containsKey(key)) {
			Pair<V, Long> p = map.get(key);
			if((ct - p.y) < maxAgeMillis) {
				if (log) LOG.debug("cache hit for key " + key);
				return p.x;
			}
			else {
				if (log) { 
					LOG.debug("cache expired for key " + key + " (" + (ct - p.y) + " >= " + maxAgeMillis +  ")");
				}
			}
		}
		else {
			if (log) {
				LOG.debug("cache miss for key " + key);
			}
		}
		// reached this point: either the key is not present, or the value is
		// too old
		V v = look.lookup(key);
		map.put(key, new Pair<V, Long>(v, ct));
		return v;
	}
	
}

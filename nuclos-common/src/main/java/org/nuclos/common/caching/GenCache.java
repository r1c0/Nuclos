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

import org.apache.log4j.Logger;

public class GenCache<K, V> implements NBCache<K, V> {
	
	private static final Logger LOG = Logger.getLogger(GenCache.class);
	
	private LookupProvider<K, V>   look;
	private HashMap<K, V>          map;

	public GenCache(LookupProvider<K, V> lookupProvider) {
		this.look = lookupProvider;
		map = new HashMap<K, V>();
	}

	@Override
	public void clear() {
		map.clear();
		LOG.info("Cleared cache " + this);
	}

	@Override
	public V get(K key) {
		if(!map.containsKey(key))
			map.put(key, look.lookup(key));
		return map.get(key);
	}

   public boolean containsKey(K key) {
   	return map.containsKey(key);
   }
}

//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.server.api.eventsupport;

import java.util.Map;

abstract class AbstractEventObjectWithCache {
	
	private final Map<String, Object> eventCache;
	
	public AbstractEventObjectWithCache(Map<String, Object> eventCache) {
		if (eventCache == null) {
			throw new IllegalArgumentException("cache must not be null");
		}
		this.eventCache = eventCache;
	}
	
	public Object getEventCacheValue(String key) {
		return eventCache.get(key);
	}
	
	public void addEventCacheValue(String key, Object value) {
		if (eventCache.containsKey(key)) {
			throw new IllegalArgumentException(String.format("Event cache contains key %s", key));
		}
		eventCache.put(key, value);
	}
	
	public void removeEventCacheValue(String key) {
		eventCache.remove(key);
	}

}

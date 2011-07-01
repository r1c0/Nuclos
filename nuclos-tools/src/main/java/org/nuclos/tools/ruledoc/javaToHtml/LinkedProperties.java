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
package org.nuclos.tools.ruledoc.javaToHtml;

import com.sun.tools.jdi.LinkedHashMap;
import java.util.*;

/**
 * A properties implementation that remembers the order of its entries.
 *
 *
 */
@SuppressWarnings("serial")
public class LinkedProperties extends Properties {
	private LinkedHashMap map = new LinkedHashMap();

	@Override
	public synchronized Object put(Object key, Object value) {
		return map.put(key, value);
	}

	@Override
	public synchronized Object get(Object key) {
		return map.get(key);
	}

	@Override
	public synchronized void clear() {
		map.clear();
	}

	@Override
	public synchronized Object clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public synchronized boolean contains(Object value) {
		return containsValue(value);
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized Enumeration elements() {
		return new IteratorEnumeration(map.values().iterator());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet() {
		return map.entrySet();
	}

	@Override
	public synchronized boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized Enumeration keys() {
		return new IteratorEnumeration(map.keySet().iterator());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set keySet() {
		return map.keySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration propertyNames() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void putAll(Map t) {
		map.putAll(t);
	}

	@Override
	public synchronized Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public synchronized int size() {
		return map.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProperty(String key) {
		Object oval = get(key);
		String sval = (oval instanceof String) ? (String) oval : null;
		return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
	}
}

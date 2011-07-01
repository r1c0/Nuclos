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
package org.nuclos.common.collection.multimap;

import static org.nuclos.common2.LangUtils.implies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.nuclos.common.collection.CollectionUtils;

/**
 * <code>HashMap</code>-based implementation of a <code>MultiMap</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo define clone, equals, hashCode
 */
public class MultiListHashMap<K,V> implements MultiListMap<K, V>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<K, List<V>> mp;

	/**
	 * creates a new empty MultiHashMap.
	 * @postcondition this.isEmpty()
	 */
	public MultiListHashMap() {
		this(new HashMap<K, List<V>>());
		assert this.isEmpty();
	}

	/**
	 * creates a new empty MultiHashMap with the given initial capacity.
	 * @param iInitialCapacity
	 * @postcondition this.isEmpty()
	 */
	public MultiListHashMap(int iInitialCapacity) {
		this(new HashMap<K, List<V>>(iInitialCapacity));
		assert this.isEmpty();
	}

	/**
	 * creates a new MultiHashMap<K, V> as a wrapper for the given Map<K, Collection<V>>.
	 * @param mp is not copied, but directly worked on.
	 */
	public MultiListHashMap(Map<K, List<V>> mp) {
		this.mp = mp;
	}

	@Override
	public void addValue(K key, V value) {
		this.getOrCreateValues(key).add(value);

		assert this.containsKey(key);
		assert this.getValues(key).contains(value);
	}

	/**
	 * @precondition collvalue != null
	 * @postcondition !collvalue.isEmpty() --> this.containsKey(key)
	 * @postcondition this.getValues(key).containsAll(collvalue)
	 */
	@Override
	public void addAllValues(K key, Collection<? extends V> collvalue) {
		if (!collvalue.isEmpty()) {
			this.getOrCreateValues(key).addAll(collvalue);
		}

		assert implies(!collvalue.isEmpty(), this.containsKey(key));
		assert this.getValues(key).containsAll(collvalue);
	}

	/**
	 * gets the Collection of values for the given key. If there is no entry for the given key yet, creates one.
	 * @param key
	 * @return the values for the given key.
	 * @postcondition this.asMap().containsKey(key)
	 * @postcondition this.getValues(key) == result
	 */
	private Collection<V> getOrCreateValues(K key) {
		List<V> result = this.asMap().get(key);
		if (result == null) {
			result = new ArrayList<V>();
			this.asMap().put(key, result);
		}
		assert this.asMap().containsKey(key);
		assert this.getValues(key) == result;
		return result;
	}

	@Override
	public void removeValue(K key, V value) {
		final Collection<V> collv = this.asMap().get(key);
		if (collv != null) {
			collv.remove(value);
			if (collv.isEmpty()) {
				this.asMap().remove(key);
			}
		}
	}

	@Override
	public void removeKey(K key) {
		this.asMap().remove(key);
		assert !this.containsKey(key);
	}

	@Override
	public boolean containsKey(K key) {
		return CollectionUtils.isNonEmpty(this.asMap().get(key));
	}

	@Override
	public List<V> getValues(K key) {
		return CollectionUtils.emptyListIfNull(this.asMap().get(key));
	}

	@Override
	public Set<V> getAllValues() {
		return CollectionUtils.unionAll(this.asMap().values());
	}

	@Override
	public boolean isEmpty() {
		return this.asMap().isEmpty();
	}

	@Override
	public void clear() {
		this.asMap().clear();

		assert this.isEmpty();
	}

	@Override
	public int getKeyCount() {
		return this.asMap().size();
	}

	@Override
	public Set<K> keySet() {
		return this.asMap().keySet();
	}

	@Override
	public Map<K, List<V>> asMap() {
		final Map<K, List<V>> result = this.mp;
		assert result != null;
		return result;
	}

	@Override
	public void normalize() {
		for (Entry<K, List<V>> entry : new ArrayList<Entry<K, List<V>>>(this.asMap().entrySet())) {
			if (CollectionUtils.isNullOrEmpty(entry.getValue())) {
				this.asMap().remove(entry.getKey());
			}
		}
	}

}	// class MultiHashMap

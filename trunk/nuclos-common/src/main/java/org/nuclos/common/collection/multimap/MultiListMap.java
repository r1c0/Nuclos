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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map that holds a list of values for each key.
 * <p>
 * Note: The idea is taken from org.apache.commons.collections.MultiMap,
 * but this version doesn't have the design flaw of the apache version.
 * Especially, a MultiMap is <i>not</i> a Map - it has different semantics when it comes to adding and getting values.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 *
 * @invariant this.isEmpty() == this.asMap().isEmpty()
 * @invariant	this.getKeyCount() == this.asMap().size()
 */
public interface MultiListMap<K,V> {

	/**
	 * associates the given value with the given key.
	 * @param key
	 * @param value
	 * @postcondition this.containsKey(key)
	 * @postcondition this.getValues(key).contains(value)
	 */
	void addValue(K key, V value);

	/**
	 * associates the given values with the given key.
	 * Note that if the given <code>collvalue</code> is empty, <code>this</code> will not be altered.
	 * @param key
	 * @param collvalue
	 * @precondition collvalue != null
	 * @postcondition !collvalue.isEmpty() --> this.containsKey(key)
	 * @postcondition this.getValues(key).containsAll(collvalue)
	 */
	void addAllValues(K key, Collection<? extends V> collvalue);

	/**
	 * removes the given value (if any) associated with the given key.
	 * @param key
	 * @param value
	 */
	void removeValue(K key, V value);

	/**
	 * removes the given key and all values (if any) associated with it.
	 * @param key
	 * @postcondition !this.containsKey(key)
	 */
	void removeKey(K key);

	/**
	 * @param key
	 * @return Does this MultiListMap contain values for the given key?
	 */
	boolean containsKey(K key);

	/**
	 * @param key
	 * @return the values for the given keys.
	 * @postcondition result != null
	 */
	List<V> getValues(K key);

	/**
	 * @return a Set of all values contained in this MultiListMap.
	 * Note that for a meaningful result it is required that V defines <code>equals</code>/<code>hashCode</code>
	 * in a meaningful way (depending on the semantics of V).<p>
	 * If you want a list of all values with a deterministic order, don't use this method,
	 * but iterate the keys via <code>this.asSet().keySet()</code> and concatenate the lists of respective values.
	 * @postcondition result != null
	 */
	Set<V> getAllValues();

	/**
	 * Does <i>not</i> normalize the map.
	 * @return Is this map empty?
	 * @postcondition result == (this.getKeyCount() == 0)
	 * @see #normalize()
	 */
	boolean isEmpty();

	/**
	 * removes all entries.
	 * @postcondition this.isEmpty()
	 */
	void clear();

	/**
	 * @return the number of keys contained in this MultiListMap. Does <i>not</i> normalize the map.
	 * @postcondition result == this.asMap().size()
	 * @postcondition result >= 0
	 * @see #normalize()
	 */
	int getKeyCount();

	/**
	 * @return the keys of this MultiListMap. Does <i>not</i> normalize the map.
	 * @postcondition result == this.asMap().keySet()
	 * @postcondition result != null
	 * @see #normalize()
	 */
	Set<K> keySet();

	/**
	 * @return a Map representation of this MultiListMap, which may be manipulated via regular Map operations.
	 * @postcondition result != null
	 */
	Map<K, List<V>> asMap();

	/**
	 * By manipulating the underlying map directly (via <code>asMap()</code>), it's possible to have different
	 * representations of that map when it comes to <code>this.getValues(key).isEmpty()</code>:
	 * <ol>
	 * <li><code>!this.asMap().containsKey(key)</code></li>
	 * <li><code>this.asMap().get(key) == null</code></li>
	 * <li><code>this.asMap().get(key).isEmpty()</code></li>
	 * </ol>
	 * This also affects the value returned for <code>this.getKeyCount()</code>.<br>
	 * This method removes all empties whose value is null or empty,
	 * so it ensures that <code>this.getValues(key).isEmpty() == !this.asMap().containsKey(key)</code> for all keys,
	 * and <code>this.getKeyCount()</code> returns the proper value.
	 */
	void normalize();

}	// interface MultiListMap

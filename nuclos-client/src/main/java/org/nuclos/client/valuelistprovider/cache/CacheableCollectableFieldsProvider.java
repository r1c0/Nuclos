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
package org.nuclos.client.valuelistprovider.cache;

import org.nuclos.common.collect.collectable.CollectableFieldsProvider;


/**
 * A {@link CollectableFieldsProvider} (CFP) that supports caching.
 * <p>
 * A CacheableCollectableFieldsProvider has an additioanl {@link #getCacheKey()} method.
 * This method should return an which is suitable for as a key for a cache. Usually the key
 * will represent the current configuration (parameter set) of the CFP in some way.
 * <p>
 * Note that the cache may be shared between different {@code CacheableCollectableFieldsProvider}
 * instances of the same CFP class.
 */
public interface CacheableCollectableFieldsProvider extends CollectableFieldsProvider {

	/**
	 * Returns a key which can be used for caching.
	 *
	 * Since the key will be used a key for the cache, a key must be immutable (or at least
	 * should not change its state with respect to {@link Object#hashCode()} and {@link Object#equals(Object)}.
	 * If the key is {@code null}, no caching should be performed.
	 *
	 * @see CacheableCollectableFieldsProvider class description
	 */
	Object getCacheKey();
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nuclos.client.valuelistprovider.DefaultValueProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A cache for values provided by {@link CollectableFieldsProvider}s.
 * <p>
 * The cache can be used for one or more
 */
public class CollectableFieldsProviderCache {

	private final Map<Pair<Class<? extends CacheableCollectableFieldsProvider>, Object>, Pair<List<CollectableField>, CollectableField>> cache;

	public CollectableFieldsProviderCache() {
		// Was a LinkedHashMap before
		cache = new ConcurrentHashMap<Pair<Class<? extends CacheableCollectableFieldsProvider>, Object>, Pair<List<CollectableField>, CollectableField>>();
	}

	/**
	 * Clears the cache.
	 */
	public void clear() {
		cache.clear();
	}

	/**
	 * Wraps the given {@link CollectableFieldsProviderFactory} so that all created {@link CollectableFieldsProvider}
	 * will use this cache.
	 */
	public CollectableFieldsProviderFactory makeCachingFieldsProviderFactory(CollectableFieldsProviderFactory factory) {
		return new CachingCollectableFieldsProviderFactory(factory);
	}


	/**
	 * If the given {@link CollectableFieldsProvider} supports {@linkplain CacheableCollectableFieldsProvider caching},
	 * a proxy provider will be created that uses this case.
	 */
	public CollectableFieldsProvider makeCachingFieldsProvider(CollectableFieldsProvider cfp) {
		if (cfp instanceof CacheableCollectableFieldsProvider) {
			return new CachingCollectableFieldsProvider((CacheableCollectableFieldsProvider) cfp);
		} else {
			return cfp;
		}
	}

	private class CachingCollectableFieldsProviderFactory implements CollectableFieldsProviderFactory {

		private final CollectableFieldsProviderFactory delegate;

		CachingCollectableFieldsProviderFactory(CollectableFieldsProviderFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public CollectableFieldsProvider newDefaultCollectableFieldsProvider(String entityName, String fieldName) {
			return makeCachingFieldsProvider(delegate.newDefaultCollectableFieldsProvider(entityName, fieldName));
		}

		@Override
		public CollectableFieldsProvider newDependantCollectableFieldsProvider(String entityName, String fieldName) {
			return makeCachingFieldsProvider(delegate.newDependantCollectableFieldsProvider(entityName, fieldName));
		}

		@Override
		public CollectableFieldsProvider newCustomCollectableFieldsProvider(String customType, String entityName, String fieldName) {
			return makeCachingFieldsProvider(delegate.newCustomCollectableFieldsProvider(customType, entityName, fieldName));
		}
	}

	public class CachingCollectableFieldsProvider extends ManagedCollectableFieldsProvider implements CollectableFieldsProvider, DefaultValueProvider {

		private final CacheableCollectableFieldsProvider delegate;

		CachingCollectableFieldsProvider(CacheableCollectableFieldsProvider delegate) {
			this.delegate = delegate;
		}

		@Override
		public void setParameter(String name, Object value) {
			delegate.setParameter(name, value);
		}

		@Override
		public List<CollectableField> getCollectableFields() throws CommonBusinessException {
			Object cacheKey = delegate.getCacheKey();
			if (cacheKey == null) {
				// No caching
				return delegate.getCollectableFields();
			}

			Pair<Class<? extends CacheableCollectableFieldsProvider>, Object> qualifiedCacheKey =
					new Pair<Class<? extends CacheableCollectableFieldsProvider>, Object>(
						delegate.getClass(), cacheKey);

			Pair<List<CollectableField>, CollectableField> result = cache.get(qualifiedCacheKey);
			List<CollectableField> values;
			if (result == null || result.getX() == null) {
				synchronized (delegate) {
					values = delegate.getCollectableFields();
				}
				if (values == null) {
					//make sure empty result can be differentiated from not yet loaded
					values = new ArrayList<CollectableField>();
				}
				if (result == null) {
					cache.put(qualifiedCacheKey, new Pair<List<CollectableField>, CollectableField>(values, null));
				}
				else {
					result.setX(values);
				}
			}

			return cache.get(qualifiedCacheKey).getX();
		}

		/**
         * @return the delegate
         */
        public CollectableFieldsProvider getDelegate() {
	        return delegate;
        }

    	@Override
		public void setIgnoreValidity(Boolean iIgnoreValidity) {
    		if(delegate instanceof ManagedCollectableFieldsProvider){
    			((ManagedCollectableFieldsProvider)delegate).setIgnoreValidity(iIgnoreValidity);
    		}
    	}

    	@Override
		public Boolean getIgnoreValidity() {
    		if(delegate instanceof ManagedCollectableFieldsProvider){
    			return ((ManagedCollectableFieldsProvider)delegate).getIgnoreValidity();
    		}
    		return false;
    	}

    	public void clear() {
    		Object cacheKey = delegate.getCacheKey();
			if (cacheKey == null) {
				return;
			}

			Pair<Class<? extends CacheableCollectableFieldsProvider>, Object> qualifiedCacheKey =
				new Pair<Class<? extends CacheableCollectableFieldsProvider>, Object>(delegate.getClass(), cacheKey);

			cache.remove(qualifiedCacheKey);
    	}

		@Override
		public CollectableField getDefaultValue() throws CommonBusinessException {
			Object cacheKey = delegate.getCacheKey();
			if (delegate instanceof DefaultValueProvider) {
				DefaultValueProvider dvp = (DefaultValueProvider) delegate;
				if (cacheKey == null) {
					// No caching
					return dvp.getDefaultValue();
				}

				Pair<Class<? extends CacheableCollectableFieldsProvider>, Object> qualifiedCacheKey =
						new Pair<Class<? extends CacheableCollectableFieldsProvider>, Object>(
							delegate.getClass(), cacheKey);

				Pair<List<CollectableField>, CollectableField> result = cache.get(qualifiedCacheKey);
				CollectableField defaultvalue;
				if (result == null || result.getY() == null) {
					synchronized (delegate) {
						defaultvalue = dvp.getDefaultValue();
					}
					if (result == null) {
						cache.put(qualifiedCacheKey, new Pair<List<CollectableField>, CollectableField>(null, defaultvalue));
					}
					else {
						result.setY(defaultvalue);
					}
				}

				return cache.get(qualifiedCacheKey).getY();
			}
			return null;
		}

		@Override
		public boolean isSupported() {
			if (delegate instanceof DefaultValueProvider) {
				return ((DefaultValueProvider) delegate).isSupported();
			}
			return false;
		}
	}
}

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
package org.nuclos.client.genericobject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.CacheableListener;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;

/**
 * Contains meta information about a leased object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectEntity implements CollectableEntity, Serializable {

	/**
	 * Caches CollectableGenericObjectEntities by module.
	 */
	private static class Cache implements CacheableListener, Serializable {
		private static Cache singleton;

		private Map<Integer, CollectableGenericObjectEntity> mpByModuleId =
				new HashMap<Integer, CollectableGenericObjectEntity>(Modules.getInstance().getModuleCount());

		public synchronized static Cache getInstance() {
			if (singleton == null) {
				singleton = new Cache();
			}
			return singleton;
		}

		private Cache() {
			// Note that this listener is never removed. However this should not be an issue.
			GenericObjectMetaDataCache.getInstance().addCacheableListener(this);

			/** @todo Is this still necessary? */
			AttributeCache.getInstance().addCacheableListener(this);
		}

		@Override
		public void cacheableChanged() {
			this.invalidate();
		}

		public synchronized void invalidate() {
			mpByModuleId.clear();
		}

		/**
		 * @param iModuleId
		 * @return
		 * @precondition iModuleId != null
		 * @postcondition result != null
		 */
		public synchronized CollectableEntity getCollectableEntityByModule(Integer iModuleId) {
			if (iModuleId == null) {
				throw new NullArgumentException("iModuleId");
			}
			if (!mpByModuleId.containsKey(iModuleId)) {
				final Modules modules = Modules.getInstance();
				final String sName = modules.getEntityNameByModuleId(iModuleId);
				final String sLabel = modules.getEntityLabelByModuleId(iModuleId);
				// Note that only attributes occuring in Details layouts are taken into account for building the entity:
				/* the collection from the AttributeCache must not be modified. A new one is created instead */
				final Collection<String> collFieldNames = new HashSet<String>(GenericObjectMetaDataCache.getInstance().getAttributeNamesByModuleId(iModuleId, Boolean.FALSE));
				mpByModuleId.put(iModuleId, new CollectableGenericObjectEntity(sName, sLabel, collFieldNames));
			}
			final CollectableEntity result = mpByModuleId.get(iModuleId);
			assert result != null;
			return result;
		}
	}	// inner class Cache

	private final String sName;
	private final String sLabel;
	private final Set<String> stFieldNames;

	/**
	 * creates a leased object entity with the given field names.
	 * @param sName
	 * @param sLabel
	 * @param collFieldNames
	 * @precondition collFieldNames != null
	 */
	public CollectableGenericObjectEntity(String sName, String sLabel, Collection<String> collFieldNames) {
		if (collFieldNames == null) {
			throw new NullArgumentException("collFieldNames");
		}
		this.sName = sName;
		this.sLabel = sLabel;
		this.stFieldNames = new HashSet<String>(collFieldNames);
	}

	/**
	 * creates a leased object entity for the given module.
	 * @param iModuleId If <code>null</code>, returns the module-independent entity for all attributes.
	 * @return
	 * @postcondition result != null
	 */
	public static CollectableEntity getByModuleId(Integer iModuleId) {
		final CollectableEntity result;
		result = Cache.getInstance().getCollectableEntityByModule(iModuleId);

		assert result != null;
		return result;
	}

	@Override
	public String getName() {
		return this.sName;
	}

	@Override
	public String getLabel() {
		return this.sLabel;
	}

	@Override
	public CollectableEntityField getEntityField(String sFieldName) {
		checkContained(sFieldName);
		
		CollectableEntityField result = new CollectableGenericObjectEntityField(
			AttributeCache.getInstance().getAttribute(this.getName(), sFieldName),
			MetaDataClientProvider.getInstance().getEntityField(getName(), sFieldName),
			getName());
		result.setCollectableEntity(this);
		return result;
	}

	private void checkContained(String sFieldName) {
		/** @todo shouldn't this throw an exception when stFieldNames == null? */
		if (this.stFieldNames != null) {
			if (!this.stFieldNames.contains(sFieldName)) {
				/** @todo ENABLE THIS AGAIN, DOES NOT WORK FOR COMMON SEARCH HERE YET */
				//throw new NuclosFatalException("Das Attribut " + sFieldName + " ist nicht im Modul " + this.getLabel() + " vorhanden.");
			}
		}
	}

	@Override
	public Set<String> getFieldNames() {
		return Collections.unmodifiableSet(this.stFieldNames);
	}

	@Override
	public String getIdentifierFieldName() {
		return NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();
	}

	@Override
	public int getFieldCount() {
		return this.stFieldNames.size();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("name=").append(sName);
		result.append(",label=").append(sLabel);
		result.append(",fieldNames=").append(stFieldNames);
		result.append("]");
		return result.toString();
	}

}	// class CollectableGenericObjectEntity

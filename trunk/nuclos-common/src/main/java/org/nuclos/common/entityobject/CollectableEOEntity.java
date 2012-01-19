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
package org.nuclos.common.entityobject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

public class CollectableEOEntity implements CollectableEntity {
	
	private final EntityMetaDataVO eMeta;
	private final Map<String, EntityFieldMetaDataVO> mapEfMeta;

	public CollectableEOEntity(EntityMetaDataVO eMeta, Map<String, EntityFieldMetaDataVO> mapEfMeta) {
		if (eMeta == null) {
			throw new NullArgumentException("eMeta");
		}
		if (mapEfMeta == null) {
			throw new NullArgumentException("mapEfMeta");
		}
		this.eMeta = eMeta;
		this.mapEfMeta = new HashMap<String, EntityFieldMetaDataVO>(mapEfMeta);
	}

	@Override
	public CollectableEntityField getEntityField(String field) {
		final EntityFieldMetaDataVO efVO = mapEfMeta.get(field);
		if (efVO == null) {
			throw new IllegalArgumentException("No such field '" + field + "' in entity '" + eMeta.getEntity() + 
					"', field are: " + mapEfMeta.keySet());
		}
		CollectableEntityField result = new CollectableEOEntityField(efVO, eMeta.getEntity());
		result.setCollectableEntity(this);
		return result;
	}

	@Override
	public int getFieldCount() {
		return mapEfMeta.size();
	}

	@Override
	public Set<String> getFieldNames() {
		return mapEfMeta.keySet();
	}

	@Override
	public String getIdentifierFieldName() {
		return mapEfMeta.containsKey("name")? mapEfMeta.get("name").getField(): null;
	}

	/**
	 * @deprecated Sollte nicht mehr verwendet werden da dies \u00fcber die Locale gesteuert wird!
	 */
	@Override
	public String getLabel() {
		// sollte nicht mehr verwendet werden da dies \u00fcber die Locale gesteuert wird!
		return null;
	}

	/**
	 * @deprecated use {@link #getMeta()}.getEntity().
	 */
	@Override
	public String getName() {
		return eMeta.getEntity();
	}
	
	public EntityMetaDataVO getMeta() {
		return eMeta;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("meta=").append(eMeta);
		result.append(", fields=").append(mapEfMeta);
		result.append("]");
		return result.toString();
	}

}

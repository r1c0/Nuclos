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
package org.nuclos.common;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * An implementation of MetaDataProvider for defining a desired state of the DB meta data.
 * <p>
 * You use this class to define desired <em>changes</em> to your DB (meta) data model.
 * </p>
 */
public class StaticMetaDataProvider extends AbstractProvider implements MetaDataProvider {

	public StaticMetaDataProvider(){
	}

	private Map<String, EntityMetaDataVO> mapMetaDataByEntity = new LinkedHashMap<String, EntityMetaDataVO>();
	private Map<Long, EntityMetaDataVO> mapMetaDataById = new LinkedHashMap<Long, EntityMetaDataVO>();
	private Map<String, Map<String, EntityFieldMetaDataVO>> mapFieldMetaData = new LinkedHashMap<String, Map<String, EntityFieldMetaDataVO>>();

	@Override
	public Collection<EntityMetaDataVO> getAllEntities() {
		return mapMetaDataByEntity.values();
	}

	@Override
	public EntityMetaDataVO getEntity(Long id) {
		final EntityMetaDataVO result = mapMetaDataById.get(id);
		if (result == null) {
			throw new CommonFatalException("entity with id " + id + " does not exists.");
		}
		return result;
	}

	@Override
	public EntityMetaDataVO getEntity(String entity) {
		final EntityMetaDataVO result = mapMetaDataByEntity.get(entity);
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	@Override
	public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		final Map<String, EntityFieldMetaDataVO> result = mapFieldMetaData.get(entity);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	public EntityFieldMetaDataVO getRefField(String baseEntity, String subform) {
		// TODO: caching
		final Map<String, EntityFieldMetaDataVO>  fields = getAllEntityFieldsByEntity(baseEntity);
		EntityFieldMetaDataVO result = null;
		for (EntityFieldMetaDataVO f: fields.values()) {
			if (baseEntity.equals(f.getForeignEntity())) {
				result = f;
				break;
			}
		}
		return result;
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) {
		final EntityFieldMetaDataVO result = getAllEntityFieldsByEntity(entity).get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(EntityFieldMetaDataVO fieldMeta : getAllEntityFieldsByEntity(entity).values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}


	public void addEntity(EntityMetaDataVO entityMeta) {
		mapMetaDataByEntity.put(entityMeta.getEntity(), entityMeta);
		mapMetaDataById.put(entityMeta.getId(), entityMeta);
		mapFieldMetaData.put(entityMeta.getEntity(), new LinkedHashMap<String, EntityFieldMetaDataVO>());
	}

	public void addEntityField(String entity, EntityFieldMetaDataVO fieldMeta) {
		mapFieldMetaData.get(entity).put(fieldMeta.getField(), fieldMeta);
	}

	public void addEntityField(EntityFieldMetaDataVO fieldMeta) {
		addEntityField(getEntity(fieldMeta.getEntityId()).getEntity(), fieldMeta);
	}

	public void addEntityFields(Collection<? extends EntityFieldMetaDataVO> fieldsMeta) {
		for (EntityFieldMetaDataVO fieldMeta : fieldsMeta) {
			addEntityField(fieldMeta);
		}
	}

	public void addEntityFields(String entity, Collection<? extends EntityFieldMetaDataVO> fieldsMeta) {
		for(EntityFieldMetaDataVO fieldMeta : fieldsMeta) {
			addEntityField(entity, fieldMeta);
		}
	}

	@Override
	public EntityMetaDataVO getEntity(NuclosEntity entity) {
		return getEntity(entity.getEntityName());
	}

	@Override
	public String getBaseEntity(String dynamicentityname) {
		throw new UnsupportedOperationException("getBaseEntity() is not applicable for StaticMetaDataProvider.");
	}
}

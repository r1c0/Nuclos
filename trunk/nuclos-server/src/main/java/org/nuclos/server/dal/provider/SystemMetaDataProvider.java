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
package org.nuclos.server.dal.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.autosync.SystemMasterDataMetaVO;
import org.nuclos.server.autosync.XMLEntities;

/**
 * An implementation for accessing the system (i.e. JSON, like 'masterdata.json')
 * meta data information on the server side.
 */
public class SystemMetaDataProvider implements MetaDataProvider {

	private Map<String, SystemEntityMetaDataVO> entities = new HashMap<String, SystemEntityMetaDataVO>();

	public static SystemMetaDataProvider getSystemMetaDataProvider() {
		return new SystemMetaDataProvider(XMLEntities.getSystemEntities().values());
	}

	private SystemMetaDataProvider(Collection<? extends SystemMasterDataMetaVO> entities) {
		for (SystemMasterDataMetaVO mdMeta : entities) {
			registerEntity(mdMeta);
		}
	}

	@Override
	public Collection<EntityMetaDataVO> getAllEntities() {
		Collection<EntityMetaDataVO> result = new ArrayList<EntityMetaDataVO>();
		for (String entity : entities.keySet()) {
			result.add(entities.get(entity));
		}
		return result;
	}

	@Override
	public SystemEntityMetaDataVO getEntity(String entity) {
		if (entities.containsKey(entity))
			return entities.get(entity);
		return null;
	}

	@Override
	public EntityMetaDataVO getEntity(Long id) {
		for (SystemEntityMetaDataVO metaDataVO : entities.values()) {
			if (id.equals(metaDataVO.getId()))
				return metaDataVO;
		}
		return null;
	}

	protected SystemEntityMetaDataVO registerEntity(SystemMasterDataMetaVO mdMeta) {
		SystemEntityMetaDataVO wrapper = new SystemEntityMetaDataVO(mdMeta);
		entities.put(mdMeta.getEntityName(), wrapper);
		return wrapper;
	}

	@Override
	public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		if (getEntity(entity) == null)
			return Collections.<String, EntityFieldMetaDataVO>emptyMap();
		return Collections.<String, EntityFieldMetaDataVO>unmodifiableMap(getEntity(entity).getEntityFields());
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
		SystemEntityFieldMetaDataVO result = getEntity(entity).getEntityFields().get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(EntityFieldMetaDataVO fieldMeta : getEntity(entity).getEntityFields().values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}

	@Override
	public EntityMetaDataVO getEntity(NuclosEntity entity) {
		return getEntity(entity.getEntityName());
	}

	@Override
	public String getBaseEntity(String dynamicentityname) {
		throw new UnsupportedOperationException("getBaseEntity() is not applicable for system entites.");
	}

	@Override
	public List<String> getEntities(String nuclet) {
		if (!NAMESPACE_NUCLOS.equals(nuclet)) {
			throw new UnsupportedOperationException(getClass().getName() + " does not support nuclets");
		}
		else {
			return CollectionUtils.transform(entities.values(), new Transformer<SystemEntityMetaDataVO, String>() {
				@Override
				public String transform(SystemEntityMetaDataVO i) {
					return i.getEntity();
				}
			});
		}
	}

}

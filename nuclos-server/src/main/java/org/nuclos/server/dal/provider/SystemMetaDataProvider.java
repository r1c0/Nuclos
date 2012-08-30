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

import org.nuclos.common.LafParameterMap;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.autosync.SystemMasterDataMetaVO;
import org.nuclos.server.autosync.XMLEntities;

/**
 * An implementation for accessing the system (i.e. JSON, like 'masterdata.json')
 * meta data information on the server side.
 */
public class SystemMetaDataProvider implements MetaDataProvider<SystemEntityMetaDataVO, SystemEntityFieldMetaDataVO> {

	private static final SystemMetaDataProvider INSTANCE = new SystemMetaDataProvider(XMLEntities.getSystemEntities().values());
	
	private final Map<String, SystemEntityMetaDataVO> entities = new HashMap<String, SystemEntityMetaDataVO>();

	public static SystemMetaDataProvider getInstance() {
		return INSTANCE;
	}

	private SystemMetaDataProvider(Collection<? extends SystemMasterDataMetaVO> entities) {
		for (SystemMasterDataMetaVO mdMeta : entities) {
			registerEntity(mdMeta);
		}
	}

	@Override
	public Collection<SystemEntityMetaDataVO> getAllEntities() {
		Collection<SystemEntityMetaDataVO> result = new ArrayList<SystemEntityMetaDataVO>();
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
	public SystemEntityMetaDataVO getEntity(Long id) {
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
	public Map<String, SystemEntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		if (getEntity(entity) == null)
			return Collections.<String, SystemEntityFieldMetaDataVO>emptyMap();
		return Collections.<String, SystemEntityFieldMetaDataVO>unmodifiableMap(getEntity(entity).getEntityFields());
	}

	public SystemEntityFieldMetaDataVO getRefField(String baseEntity, String subform) {
		// TODO: caching
		final Map<String, SystemEntityFieldMetaDataVO>  fields = getAllEntityFieldsByEntity(baseEntity);
		SystemEntityFieldMetaDataVO result = null;
		for (SystemEntityFieldMetaDataVO f: fields.values()) {
			if (baseEntity.equals(f.getForeignEntity())) {
				result = f;
				break;
			}
		}
		return result;
	}

	@Override
	public SystemEntityFieldMetaDataVO getEntityField(String entity, String field) {
		SystemEntityFieldMetaDataVO result = getEntity(entity).getEntityFields().get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}

	@Override
	public SystemEntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(SystemEntityFieldMetaDataVO fieldMeta : getEntity(entity).getEntityFields().values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}

	@Override
	public SystemEntityMetaDataVO getEntity(NuclosEntity entity) {
		return getEntity(entity.getEntityName());
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

	@Override
	public Map<Long, LafParameterMap> getAllLafParameters() {
		return null;
	}

	@Override
	public LafParameterMap getLafParameters(Long entityId) {
		return null;
	}

}

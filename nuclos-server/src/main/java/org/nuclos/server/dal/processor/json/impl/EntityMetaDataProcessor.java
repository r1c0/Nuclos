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
package org.nuclos.server.dal.processor.json.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.server.dal.processor.json.AbstractJsonDalProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityMetaDataProcessor;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;

public class EntityMetaDataProcessor extends AbstractJsonDalProcessor<EntityMetaDataVO> 
implements JsonEntityMetaDataProcessor {

	protected EntityMetaDataProcessor() {
		super(EntityMetaDataVO.class);
	}
	
	@Override
	public List<EntityMetaDataVO> getAll() {
		List<EntityMetaDataVO> result = new ArrayList<EntityMetaDataVO>();
		
		MetaDataProvider jsonEntityProvider = SystemMetaDataProvider.getSystemMetaDataProvider();
		
		List<EntityMetaDataVO> entities = new ArrayList<EntityMetaDataVO>(jsonEntityProvider.getAllEntities());
		Collections.sort(entities, new Comparator<EntityMetaDataVO>() {
			@Override
			public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
				return o1.getEntity().compareTo(o2.getEntity());
			}});
		
		for (EntityMetaDataVO eMeta : jsonEntityProvider.getAllEntities()) {
			eMeta.processor("org.nuclos.server.dal.processor.json.impl.EntityMetaDataProcessor");
			result.add(eMeta);
		}
		
		return result;
	}

	@Override
	public EntityMetaDataVO getByPrimaryKey(Long id) {
		for (EntityMetaDataVO eMeta : getAll()) {
			if (id.equals(eMeta.getId())) {
				return eMeta;
			}
		}
		return null;
	}

	@Override
	public List<EntityMetaDataVO> getByPrimaryKeys(List<Long> ids) {
		List<EntityMetaDataVO> result = new ArrayList<EntityMetaDataVO>(ids.size());
		for (EntityMetaDataVO eMeta : getAll()) {
			if (ids.contains(eMeta.getId())) {
				result.add(eMeta);
			}
		}
		return result;
	}

	@Override
	public List<Long> getAllIds() {
		List<Long> result = new ArrayList<Long>();
		for (EntityMetaDataVO eMeta : getAll()) {
			result.add(eMeta.getId());
		}
		return result;
	}

}

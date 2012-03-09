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
import java.util.List;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.server.dal.processor.json.AbstractJsonDalProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityFieldMetaDataProcessor;
import org.nuclos.server.dal.provider.SystemEntityFieldMetaDataVO;
import org.nuclos.server.dal.provider.SystemEntityMetaDataVO;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;

public class EntityFieldMetaDataProcessor extends AbstractJsonDalProcessor<EntityFieldMetaDataVO>
implements JsonEntityFieldMetaDataProcessor{

	protected EntityFieldMetaDataProcessor() {
		super(EntityFieldMetaDataVO.class);
	}
	
	@Override
	public List<EntityFieldMetaDataVO> getAll() {
		List<EntityFieldMetaDataVO> result = new ArrayList<EntityFieldMetaDataVO>();
		
		MetaDataProvider<SystemEntityMetaDataVO, SystemEntityFieldMetaDataVO> jsonEntityProvider = SystemMetaDataProvider.getInstance();
		
		for (EntityMetaDataVO eMeta : jsonEntityProvider.getAllEntities()) {
			for (EntityFieldMetaDataVO efMeta : jsonEntityProvider.getAllEntityFieldsByEntity(eMeta.getEntity()).values()) {
				result.add(efMeta);
			}
		}
		
		return result;
	}

	@Override
	public List<EntityFieldMetaDataVO> getByParent(String parent) {
		List<EntityFieldMetaDataVO> result = new ArrayList<EntityFieldMetaDataVO>();
		
		MetaDataProvider<SystemEntityMetaDataVO, SystemEntityFieldMetaDataVO> jsonEntityProvider = SystemMetaDataProvider.getInstance();
		
		for (EntityFieldMetaDataVO efMeta : jsonEntityProvider.getAllEntityFieldsByEntity(parent).values()) {
			result.add(efMeta);
		}
		
		return result;
	}

	@Override
	public EntityFieldMetaDataVO getByPrimaryKey(Long id) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<EntityFieldMetaDataVO> getByPrimaryKeys(List<Long> ids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Long> getAllIds() {
		List<Long> result = new ArrayList<Long>();
		for (EntityFieldMetaDataVO efMeta : getAll()) {
			result.add(efMeta.getId());
		}
		return result;
	}

}

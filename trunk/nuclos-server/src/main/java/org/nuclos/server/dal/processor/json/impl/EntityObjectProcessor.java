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

import java.util.Collection;
import java.util.List;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.processor.json.AbstractJsonDalProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityObjectProcessor;

public class EntityObjectProcessor extends AbstractJsonDalProcessor<EntityObjectVO>	
implements JsonEntityObjectProcessor{
	
	public EntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta) {
		super(EntityObjectVO.class);
	}

	@Override
	public List<EntityObjectVO> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityObjectVO getByPrimaryKey(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EntityObjectVO> getByPrimaryKeys(List<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getAllIds() {
		// TODO Auto-generated method stub
		return null;
	}

}

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
package org.nuclos.server.dal.processor.jdbc.impl;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.server.dal.specification.IDalReadSpecification;

public class DynamicFieldMetaDataProcessor implements IDalReadSpecification<EntityFieldMetaDataVO> {
	private EntityMetaDataVO entity;
	
	public DynamicFieldMetaDataProcessor(EntityMetaDataVO entity) {
	    this.entity = entity;
    }

	@Override
    public List<EntityFieldMetaDataVO> getAll() {
		return new ArrayList<EntityFieldMetaDataVO>(DynamicMetaDataProcessor.getDynamicFieldsForView(entity.getDbEntity(), entity.getId()).values());
    }

	@Override
	public EntityFieldMetaDataVO getByPrimaryKey(final Long id) {
		return CollectionUtils.findFirst(getAll(), new Predicate<EntityFieldMetaDataVO>() {
			@Override
            public boolean evaluate(EntityFieldMetaDataVO t) {
	            return id.equals(t.getId());
            }});
	}

	@Override
	public List<EntityFieldMetaDataVO> getByPrimaryKeys(final List<Long> ids) {
		return CollectionUtils.applyFilter(getAll(), new Predicate<EntityFieldMetaDataVO>() {
			@Override
            public boolean evaluate(EntityFieldMetaDataVO t) {
	            return ids.contains(t.getId());
            }});
	}

	@Override
	public List<Long> getAllIds() {
		return CollectionUtils.transform(getAll(), DalTransformations.getId());
	}
}

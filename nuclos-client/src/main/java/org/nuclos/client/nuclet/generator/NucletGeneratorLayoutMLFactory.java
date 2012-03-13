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
package org.nuclos.client.nuclet.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.util.LangUtils;
import org.nuclos.client.layout.AbstractLayoutMLFactory;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class NucletGeneratorLayoutMLFactory extends AbstractLayoutMLFactory {
	
	private final NucletGenerator generator;
	private List<EntityMetaDataVO> metaEntity;
	private List<EntityFieldMetaDataVO> metaEntityField;
	private Map<Long, String> attributeGroups;

	public NucletGeneratorLayoutMLFactory(NucletGenerator generator) {
		super();
		this.generator = generator;
	}
	
	@PostConstruct
	void init() {
		metaEntity = new ArrayList<EntityMetaDataVO>();
		for (EntityObjectVO eo : generator.getEntities()) {
			metaEntity.add(new EntityMetaDataVO(eo));
		}
		metaEntityField = new ArrayList<EntityFieldMetaDataVO>();
		for (EntityObjectVO eo : generator.getEntityFields()) {
			metaEntityField.add(new EntityFieldMetaDataVO(eo));
		}
		attributeGroups = new HashMap<Long, String>();
		for (EntityObjectVO eo : generator.getEntityFieldGroups()) {
			attributeGroups.put(eo.getId(), eo.getField("name", String.class));
		}
	}

	@Override
	public String getResourceText(String resourceId) {
		return generator.getResourceText(resourceId);
	}

	@Override
	public Collection<EntityMetaDataVO> getEntityMetaData() {
		return metaEntity;
	}

	@Override
	public List<EntityFieldMetaDataVO> getEntityFieldMetaData(String entity) {
		final List<EntityFieldMetaDataVO> result = new ArrayList<EntityFieldMetaDataVO>();
		Long id = null;
		for (EntityMetaDataVO eMeta : metaEntity) {
			if (LangUtils.equals(eMeta.getEntity(), entity)) {
				id = eMeta.getId();
			}	
		}
		for (EntityFieldMetaDataVO efMeta : metaEntityField) {
			if (LangUtils.equals(id, efMeta.getEntityId())) {
				result.add(efMeta);
			}
		}
		return result;
	}

	@Override
	public Map<Long, String> getAttributeGroups() {
		return attributeGroups;
	}
	
	public String generateLayout(String entity, boolean groupAttributes, boolean withSubforms, boolean withEditFields) throws CommonBusinessException {
		final String result = generateLayout(entity, getEntityFieldMetaData(entity), groupAttributes, withSubforms, withEditFields);
		return result;
	}

}

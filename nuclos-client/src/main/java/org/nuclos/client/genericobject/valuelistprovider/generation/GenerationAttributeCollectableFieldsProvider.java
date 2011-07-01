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
package org.nuclos.client.genericobject.valuelistprovider.generation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Value list provider to get attributes by source and target modules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class GenerationAttributeCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(GenerationAttributeCollectableFieldsProvider.class);

	private String column;
	private Integer iSourceModuleId;
	private Integer iTargetModuleId;
	private Integer iSourceAttributeId;
	private Integer iTargetAttributeId;
	private String sourceType;
	private Integer parameterEntityId;

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"sourcemodule" = source module id</li>
	 *   <li>"targetmodule" = target module id</li>
	 *   <li>"sourceattribute" = source attribute id</li>
	 *   <li>"targetattribute" = target attribute id</li>
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("column")) {
			this.column = (String) oValue;
		}
		else if (sName.equals("sourcemodule")) {
			this.iSourceModuleId = (Integer) oValue;
		}
		else if (sName.equals("targetmodule")) {
			this.iTargetModuleId = (Integer) oValue;
		}
		else if (sName.equals("sourceattribute")) {
			this.iSourceAttributeId = (Integer) oValue;
		}
		else if (sName.equals("targetattribute")) {
			if(oValue instanceof Long) {
				this.iTargetAttributeId = ((Long)oValue).intValue();
			}
			else 
				this.iTargetAttributeId = (Integer) oValue;
		}
		else if (sName.equals("sourceType")) {
			this.sourceType = (String) oValue;
		}
		else if (sName.equals("parameterEntity")) {
			this.parameterEntityId = (Integer) oValue;
		}
		else {
			// ignore
		}
	}


	private List<EntityFieldMetaDataVO> getFields(Integer entityId, Integer restrictionEntityId, Integer restrictionFieldId) {
		if (entityId == null)
			return null;

		MetaDataClientProvider provider = MetaDataClientProvider.getInstance();
		EntityMetaDataVO entity = provider.getEntity(entityId.longValue());
		Map<String, EntityFieldMetaDataVO> fields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity.getEntity());

		final String restrictionDataType;
		if (restrictionEntityId != null && restrictionFieldId != null) {
			EntityMetaDataVO restrictionEntity = provider.getEntity(restrictionEntityId.longValue());
			EntityFieldMetaDataVO restrictionField = provider.getEntityField(
				restrictionEntity.getEntity(), restrictionFieldId.longValue());
			restrictionDataType = restrictionField.getDataType();
		} else {
			restrictionDataType = null;
		}

		return CollectionUtils.applyFilter(fields.values(), new Predicate<EntityFieldMetaDataVO>() {
			@Override public boolean evaluate(EntityFieldMetaDataVO f) {
				if (f.getId() < 0)
					return false;
				if (restrictionDataType != null && !LangUtils.equals(restrictionDataType, f.getDataType()))
					return false;
				return true;
			}
		});
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");

		List<EntityFieldMetaDataVO> fields = null;

		Integer realSourceId = "parameter".equals(sourceType) ? parameterEntityId : iSourceModuleId;

		if ("source".equals(column)) {
			fields = getFields(realSourceId, iTargetModuleId, iTargetAttributeId);
		} else if ("target".equals(column)) {
			fields = getFields(iTargetModuleId, realSourceId, iSourceAttributeId);
		}

		if (fields == null)
			return Collections.emptyList();

		Collections.sort(fields, new Comparator<EntityFieldMetaDataVO>() {
			@Override
			public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {				
				return o1.getField().toUpperCase().compareTo(o2.getField().toUpperCase());
			}
		});
		return CollectionUtils.transform(fields, new Transformer<EntityFieldMetaDataVO, CollectableField>() {
			@Override public CollectableField transform(EntityFieldMetaDataVO f) {
				return new CollectableValueIdField(f.getId().intValue(), f.getField());
			}
		});
	}

}	// class GenerationAttributeCollectableFieldsProvider

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
package org.nuclos.server.validation;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

@Validation(order = 1)
public class MandatoryFieldValidator implements Validator {

	private MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider;

	private StateCache stateCache;

	@Autowired
	public void setMetaDataProvider(MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	@Autowired
	final void setStateCache(StateCache stateCache) {
		this.stateCache = stateCache;
	}

	@Override
	public void validate(EntityObjectVO object, ValidationContext c) {
		EntityMetaDataVO meta = metaDataProvider.getEntity(object.getEntity());
		for (EntityFieldMetaDataVO fieldmeta : metaDataProvider.getAllEntityFieldsByEntity(meta.getEntity()).values()) {
			if (!fieldmeta.isNullable()) {
				final Object value;
				if (fieldmeta.getForeignEntity() != null) {
					if (LangUtils.equals(fieldmeta.getForeignEntity(), c.getParent())) {
						continue;
					}
					value = object.getFieldId(fieldmeta.getField());
				}
				else {
					value = object.getField(fieldmeta.getField());
				}
				if (value == null) {
					String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.3", fieldmeta.getLocaleResourceIdForLabel());
					c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
				}
			}
		}

		if (meta.isStateModel()) {
			if (object.getFieldIds().containsKey(NuclosEOField.STATE.getName())) {
				Long stateId = object.getFieldId(NuclosEOField.STATE.getName());
				if (stateId != null) {
					StateVO state = stateCache.getState(stateId.intValue());
					if (!state.getMandatoryFields().isEmpty()) {
						final String entity = meta.getEntity();
	
						for (MandatoryFieldVO mandatoryField : state.getMandatoryFields()) {
							final EntityFieldMetaDataVO efMeta = metaDataProvider.getEntityField(entity, mandatoryField.getFieldId().longValue());
							final String field = efMeta.getField();
	
							if (efMeta.getForeignEntity() != null && LangUtils.equals(efMeta.getForeignEntity(), c.getParent())) {
								continue;
							}
	
							if ((efMeta.getForeignEntity() != null && object.getFieldId(field) == null)
								|| (efMeta.getForeignEntity() == null && object.getFields().get(field) == null)) {
								String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.3", efMeta.getLocaleResourceIdForLabel());
								c.addFieldError(meta.getEntity(), efMeta.getField(), error);
							}
						}
					}
				}
			}
		}
	}
}

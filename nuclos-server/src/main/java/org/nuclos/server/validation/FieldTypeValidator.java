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

import java.util.Date;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

@Validation(order = 2)
public class FieldTypeValidator implements Validator {

	private MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider;
	
	@Autowired
	public void setMetaDataProvider(MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}
	
	@Override
	public void validate(EntityObjectVO object, ValidationContext context) {
		EntityMetaDataVO meta = metaDataProvider.getEntity(object.getEntity());
		for (EntityFieldMetaDataVO fieldmeta : metaDataProvider.getAllEntityFieldsByEntity(meta.getEntity()).values()) {
			if (fieldmeta.getForeignEntity() == null) {
				final Object oValue = object.getField(fieldmeta.getField());
				if (oValue != null) {
					final Class<?> clsValue = oValue.getClass();
					Class<?> clsEntity;
					try {
						clsEntity = Class.forName(fieldmeta.getDataType());
						if(InternalTimestamp.class.equals(clsEntity) && Date.class.isAssignableFrom(clsValue)) {
							continue;
						}
						
						if (!clsEntity.isAssignableFrom(clsValue) && !NuclosPassword.class.equals(clsEntity)) {
							String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.5", oValue, fieldmeta.getLocaleResourceIdForLabel(), clsEntity.getName(), clsValue.getName());
							context.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
						}
					}
					catch (ClassNotFoundException e) {
						throw new NuclosFatalException(e);
					}
				}
			}
		}	
	}

}

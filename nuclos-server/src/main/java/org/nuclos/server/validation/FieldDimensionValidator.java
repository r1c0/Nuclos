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

import java.math.BigDecimal;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

@Validation(order = 3)
public class FieldDimensionValidator implements Validator {
	
	private MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider;
	
	@Autowired
	public void setMetaDataProvider(MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}
	
	@Override
	public void validate(EntityObjectVO object, ValidationContext c) {
		EntityMetaDataVO meta = metaDataProvider.getEntity(object.getEntity());
		for (EntityFieldMetaDataVO fieldmeta : metaDataProvider.getAllEntityFieldsByEntity(meta.getEntity()).values()) {
			if (fieldmeta.getForeignEntity() == null) {
				final Object oValue = object.getField(fieldmeta.getField());
				if (oValue != null && fieldmeta.getScale() != null) {
					int maxlength = fieldmeta.getScale();

					if (oValue instanceof Integer) {
						if (((Integer) oValue).toString().length() > maxlength) {
							String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.6", oValue, fieldmeta.getLocaleResourceIdForLabel());
							c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
						}
					} else if (oValue instanceof String) {
						if (((String) oValue).length() > maxlength) {
							String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.7", oValue, fieldmeta.getLocaleResourceIdForLabel());
							c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
						}
					} else if (oValue instanceof Double){
						BigDecimal bd = BigDecimal.valueOf((Double) oValue);
						int digitsBeforeSep = bd.precision() - bd.scale();
						if (digitsBeforeSep > maxlength - fieldmeta.getPrecision()) {
							String error = StringUtils.getParameterizedExceptionMessage("CollectableUtils.8", oValue, fieldmeta.getLocaleResourceIdForLabel());
							c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
						}
					}
				}
			}
		}	
	}
}

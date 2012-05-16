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
import java.util.Date;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

@Validation(order = 5)
public class FieldRangeValidator implements Validator {

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
				boolean result = true;

				if (oValue != null && !StringUtils.isNullOrEmpty(fieldmeta.getFormatInput()) && !(oValue instanceof String)) {
					String[] saMinMax = fieldmeta.getFormatInput().split(" ");
					boolean hasMax = (saMinMax.length == 2);
					if (oValue instanceof Integer) {
						// min/max check
						if (!"".equals(saMinMax[0])) {
							if (((Integer) oValue).intValue() < Integer.parseInt(saMinMax[0])) {
								result = false;
							}
						}
						if (hasMax && !"".equals(saMinMax[1])) {
							if (((Integer) oValue).intValue() > Integer.parseInt(saMinMax[1])) {
								result = false;
							}
						}
					}
					else if (oValue instanceof Double) {
						// min/max check
						if (!"".equals(saMinMax[0])) {
							if (((Double) oValue).doubleValue() < Double.parseDouble(saMinMax[0])) {
								result = false;
							}
						}
						if (hasMax && !"".equals(saMinMax[1])) {
							if (((Double) oValue).doubleValue() > Double.parseDouble(saMinMax[1])) {
								result = false;
							}
						}
					}
					else if (oValue instanceof Date) {
						// min/max check
						if (!"".equals(saMinMax[0])) {
							if (((Date) oValue).getTime() < Long.parseLong(saMinMax[0])) {
								result = false;
							}
						}
						if (hasMax && !"".equals(saMinMax[1])) {
							if (((Date) oValue).getTime() > Long.parseLong(saMinMax[1])) {
								result = false;
							}
						}
					}
					else if (oValue instanceof BigDecimal) {
						// min/max check
						if (!"".equals(saMinMax[0])) {
							if (((BigDecimal) oValue).compareTo(new BigDecimal(saMinMax[0])) < 0) {
								result = false;
							}
						}
						if (hasMax && !"".equals(saMinMax[1])) {
							if (((BigDecimal) oValue).compareTo(new BigDecimal(saMinMax[1])) > 0) {
								result = false;
							}
						}
					}
				}
				
				if (!result) {
					String error = StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.boundaries",
							fieldmeta.getLocaleResourceIdForLabel(), meta.getLocaleResourceIdForLabel());
					c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
				}
			}
		}	
	}
}

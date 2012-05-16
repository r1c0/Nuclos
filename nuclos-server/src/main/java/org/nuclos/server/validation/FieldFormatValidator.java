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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

@Validation(order = 4)
public class FieldFormatValidator implements Validator {

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
				if (oValue != null && !StringUtils.isNullOrEmpty(fieldmeta.getFormatInput())) {
					if (oValue instanceof String) {
						// check against regex
						Pattern p = Pattern.compile(fieldmeta.getFormatInput());
						Matcher m = p.matcher((String) oValue);
						if (!m.matches()) {
							String error = StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.formatinput",
									fieldmeta.getLocaleResourceIdForLabel(), meta.getLocaleResourceIdForLabel(), fieldmeta.getFormatInput());
							c.addFieldError(meta.getEntity(), fieldmeta.getField(), error);
						}
					}
				}
			}
		}	
	}
}

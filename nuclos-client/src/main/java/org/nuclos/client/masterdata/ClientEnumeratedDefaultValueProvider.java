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
package org.nuclos.client.masterdata;

import java.util.Collection;
import java.util.Set;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.masterdata.EnumeratedDefaultValueProvider;
import org.nuclos.common.masterdata.MakeMasterDataValueIdField;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class ClientEnumeratedDefaultValueProvider implements EnumeratedDefaultValueProvider {

	@Override
	public CollectableField getDefaultValue(MasterDataMetaFieldVO fieldmeta) {
		try {
			final String sDefault = fieldmeta.getDefaultValue();

			Set<String> setFields = StringUtils.getFieldsFromTreeViewPattern(fieldmeta.getForeignEntityField());

			if(setFields.size() == 1) {
				String sField = setFields.iterator().next();
				CollectableComparison cond = SearchConditionUtils.newComparison(fieldmeta.getForeignEntity(), sField, ComparisonOperator.EQUAL, sDefault);
				Collection<MasterDataVO> colVo = MasterDataDelegate.getInstance().getMasterData(fieldmeta.getForeignEntity(), cond);
				if(colVo.size() > 0) {
					Integer iId = colVo.iterator().next().getIntId();
					CollectableValueIdField idField = new CollectableValueIdField(iId, sDefault);
					if(idField.getValue() != null) {
						return idField;
					}
				}
			}
			else if(setFields.size() > 1) {
				Collection<MasterDataVO> colVo = MasterDataCache.getInstance().get(fieldmeta.getForeignEntity());
				for(MasterDataVO mdvo : colVo) {
					MakeMasterDataValueIdField util = new MakeMasterDataValueIdField(fieldmeta.getForeignEntityField());
					CollectableField field = util.transform(mdvo);
					if(field.getValue().equals(sDefault)) {
						return field;
					}
				}
			}
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("error.enumerated.default.value.notfound", fieldmeta.getResourceSIdForLabel()));
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	@Override
	public CollectableField getDefaultValue(EntityFieldMetaDataVO fieldmeta) {
		try {
			final String sDefault = fieldmeta.getDefaultValue();
			
			String dataType = fieldmeta.getDataType();
			if(dataType.equals("java.lang.Double")) {
				return new CollectableValueField(Double.parseDouble(sDefault.replace(',', '.')));
			}
			else if(dataType.equals("java.lang.Integer")) {
				return new CollectableValueField(Integer.parseInt(sDefault));
			}
			else if(dataType.equals("java.lang.Boolean")) {
				if("ja".equals(sDefault))
					return new CollectableValueField(Boolean.TRUE);
				else
					return new CollectableValueField(Boolean.FALSE);
			}
			else if(dataType.equals("java.util.Date")) {
				if(RelativeDate.today().toString().equals(sDefault)) {
					return new CollectableValueField(DateUtils.today());
				}
				else {
					return new CollectableValueField(sDefault);
				}
			}
			else {
				return new CollectableValueField(sDefault);
			}
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

}

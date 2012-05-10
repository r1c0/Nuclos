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
package org.nuclos.server.report;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.server.report.valueobject.ResultVO;

public abstract class ReportFieldDefinitionFactory {

	public static List<ReportFieldDefinition> getFieldDefinitions(ResultVO vo) {
		List<ReportFieldDefinition> fields = new ArrayList<ReportFieldDefinition>();
		for(int i = 0; i < vo.getColumnCount(); i++) {
			Class<?> clazz = String.class;
			int rowCount = vo.getRowCount();
			rowCount = rowCount > 100 ? 100 : rowCount;
			int result = 25;
			for(int j = 0; j < rowCount; j++) {
				final Object oValue = vo.getRows().get(j)[i];
				if(oValue != null) {
					clazz = oValue.getClass();
					final int iLength = oValue.toString().length();
					result = (iLength < result) ? result : iLength;
				}
			}
			ReportFieldDefinition def = new ReportFieldDefinition(getFieldName(vo.getColumns().get(i).getColumnLabel()), clazz, vo.getColumns().get(i).getColumnLabel());
			def.setMaxLength(result);
			fields.add(def);
		}
		return fields;
	}
	
	public static List<ReportFieldDefinition> getFieldDefinitions(List<? extends CollectableEntityField> entityfields) {
		List<ReportFieldDefinition> fields = CollectionUtils.transform(entityfields, new Transformer<CollectableEntityField, ReportFieldDefinition>() {
			@Override
			public ReportFieldDefinition transform(CollectableEntityField i) {
				ReportFieldDefinition def = new ReportFieldDefinition(getFieldName(i), i.getJavaClass(), getLabel(i));
				if (i.getMaxLength() != null) {
					def.setMaxLength(i.getMaxLength());
				}
				def.setOutputformat(i.getFormatOutput());
				return def;
			}
		});
		return fields;
	}
	
	private static String getFieldName(String nuclosfieldname) {
		final String result = "fld" + nuclosfieldname.replace('[', '_').replace(']', '_');
		return result;
	}
	
	public static String getFieldName(CollectableEntityField field) {
		final String rawName;
		if (field instanceof CollectableEOEntityField) {
			final CollectableEOEntityField f = (CollectableEOEntityField) field;
			final PivotInfo pinfo = f.getMeta().getPivotInfo();
			if (pinfo != null) {
				rawName = field.getEntityName() + ":" + pinfo.getKeyField() 
					+ ":" + f.getName() + ":" + pinfo.getValueField();   
			}
			else {
				rawName = field.getEntityName() + "." + field.getName();
			}
		}
		else {
			rawName = field.getEntityName() + "." + field.getName();
		}
		return getFieldName(rawName);
	}
	
	public static String getLabel(CollectableEntityField field) {
		final String result;
		if (field instanceof CollectableEOEntityField) {
			final CollectableEOEntityField f = (CollectableEOEntityField) field;
			final PivotInfo pinfo = f.getMeta().getPivotInfo();
			if (pinfo != null) {
				result = f.getName() + ":" + pinfo.getValueField();
			}
			else {
				result = field.getLabel();
			}
		}
		else {
			result = field.getLabel();
		}
		return result;
	}
}

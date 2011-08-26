//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.common.dal.vo;

import java.io.Serializable;

import org.nuclos.common2.StringUtils;

/**
 * For displaying pivot subforms as part of the result list of a entity, we
 * need some additional information.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotInfo implements Comparable<PivotInfo>, Serializable {
	
	private final String subform;
	
	private final String keyField;
	
	private final String valueField;
	
	private final Class<?> valueType;
	
	public PivotInfo(String subform, String keyField, String valueField, Class<?> valueType) {
		if (subform == null) throw new NullPointerException();
		this.subform = subform;
		this.keyField = keyField;
		this.valueField = valueField;
		this.valueType = valueType;
	}

	public String getSubform() {
		return subform;
	}

	public String getKeyField() {
		return keyField;
	}

	public String getValueField() {
		return valueField;
	}
	
	public Class<?> getValueType() {
		return valueType;
	}
	
	public String getPivotTableAlias(String keyValue) {
		// final String result = "\"" + getSubform() + "_" + keyValue + "\"";
		final String result = StringUtils.makeSQLIdentifierFrom(keyValue);
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PivotInfo)) return false;
		final PivotInfo other = (PivotInfo) o;
		return subform.equals(other.subform) && keyField.equals(other.keyField) &&
			valueField.equals(other.valueField);
	}
	
	@Override
	public int hashCode() {
		int result = subform.hashCode() + 82781;
		result += 7 * keyField.hashCode() + 123;
		result += 11 * valueField.hashCode() + 721;
		return result;
	}

	@Override
	public int compareTo(PivotInfo o) {
		int result = subform.compareTo(o.subform);
		if (result == 0) {
			result = keyField.compareTo(o.keyField);
			if (result == 0 && valueField instanceof Comparable) {
				result = ((Comparable<String>) valueField).compareTo(o.valueField);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("PivotInfo[").append(subform).append(", ");
		result.append(keyField).append(", ").append(valueField);
		if (valueType != null) {
			result.append(", ").append(valueType.getName());
		}
		result.append("]");
		return result.toString();
	}

}

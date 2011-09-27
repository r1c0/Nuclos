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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.nuclos.common2.StringUtils;

/**
 * For displaying pivot subforms as part of the result list of a entity, we
 * need some additional information.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotInfo implements Comparable<PivotInfo>, Serializable {
	
	private static AtomicInteger ai = new AtomicInteger(0);
	
	private final String subform;
	
	private final String keyField;
	
	private final String valueField;
	
	private final Class<?> valueType;
	
	/**
	 * keyValue -> tableAlias mapping.
	 */
	private Map<String,String> tableAliases = new HashMap<String, String>();
	
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
		if (keyValue == null) throw new IllegalArgumentException();
		String result = tableAliases.get(keyValue);
		if (result == null) {
			// allow string with only numbers in, and trailing '_' in oracle db
			result = StringUtils.makeSQLIdentifierFrom("a_", getSubform(), keyValue, Integer.toString(ai.incrementAndGet()));
			tableAliases.put(keyValue, result);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PivotInfo)) return false;
		final PivotInfo other = (PivotInfo) o;
		boolean result = subform.equals(other.subform) && keyField.equals(other.keyField);
		if (result) { 
			if (valueField != null) {
				result = valueField.equals(other.valueField);
			}
			else {
				result = (other.valueField == null);
			}
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = subform.hashCode() + 82781;
		result += 7 * keyField.hashCode() + 123;
		if (valueField != null) {
			result += 11 * valueField.hashCode() + 721;
		}
		return result;
	}

	@Override
	public int compareTo(PivotInfo o) {
		int result = subform.compareTo(o.subform);
		if (result == 0) {
			result = keyField.compareTo(o.keyField);
			if (result == 0 && valueField instanceof Comparable) {
				if (valueField != null) {
					result = ((Comparable<String>) valueField).compareTo(o.valueField);
				}
				else {
					result = -1;
				}
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

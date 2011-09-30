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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.nuclos.common.collection.Pair;
import org.nuclos.common2.StringUtils;

/**
 * For displaying pivot subforms as part of the result list of a entity, we
 * need some additional information.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotInfo implements Comparable<PivotInfo>, Serializable {
	
	private static final transient AtomicInteger AI = new AtomicInteger(0);
	
	/**
	 * The table alias for each pivot (subform, key) pair must be unique (and repeatable).
	 * Hence we store this class-wise.
	 * <p>
	 * (Pair (subform, key) -> tableAlias) mapping.
	 * </p><p>
	 * TODO:
	 * Is PivotInfo the right place to store this information?
	 * </p>
	 */
	private static final transient Map<Pair<String,String>,String> TABLE_ALIASES = new ConcurrentHashMap<Pair<String,String>, String>();
	
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
	
	/**
	 * Special serialization handling.
	 */
	Object readResolve() throws ObjectStreamException {
		// save handling of static final fields...
		assert AI != null && TABLE_ALIASES != null;
		return new PivotInfo(subform, keyField, valueField, valueType);
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
	
	/**
	 * Attention: A call to this method is only valid on the <em>server</em> side!
	 * <p>
	 * TODO:
	 * This seems not 100% thread-safe at present!
	 * </p>
	 */
	public String getPivotTableAlias(String keyValue) {
		try {
			assert AI != null && Class.forName("org.nuclos.server.dal.processor.ProcessorFactorySingleton") != null;
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("getPivotTableAlias can only be called on the server side!");
		}
		if (keyValue == null) throw new IllegalArgumentException();
		final Pair<String,String> pair = new Pair<String,String>(subform, keyValue);
		String result = TABLE_ALIASES.get(pair);
		if (result == null) {
			// allow string with only numbers in, and trailing '_' in oracle db
			result = StringUtils.makeSQLIdentifierFrom("a_", getSubform(), keyValue, Integer.toString(AI.incrementAndGet()));
			TABLE_ALIASES.put(pair, result);
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

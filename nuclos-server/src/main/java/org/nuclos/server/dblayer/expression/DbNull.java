// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.dblayer.expression;

import java.util.HashMap;
import java.util.Map;

import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;

/**
 * A typed representation of database NULL. Use this instead of Java's null
 * value.
 * <p>
 * Thomas Pasch's annotation: I'm personally opposed to the whole idea of this.
 * As in Nuclos the user entities must by represented dynamically in the system,
 * i.e. with something like a Map, a <em>typed</em> representation of NULL makes
 * our code more complicated.
 * </p>
 */
public class DbNull<T> implements DbSpecialValue<T> {

	/**
	 * A NULL value suited for normal cases, ignoring all the type stuff.
	 * 
	 * @author Thomas Pasch
	 * @since 3.1.01
	 */
	public static final DbNull<String>	NULL	= new DbNull<String>(String.class);

	public static <T> Object escapeNull(T value, Class<T> javaType) {
		return (value != null) ? value : new DbNull<T>(javaType);
	}

	/**
	 * Escape all NULL values in a Key -> Value map. This ignores all the type
	 * stuff.
	 * 
	 * @author Thomas Pasch
	 * @since 3.1.01
	 */
	public static <K, V> Map<K, Object> escapeNull(Map<K, V> map) {
		final Map<K, Object> result = new HashMap<K, Object>(map.size());
		for(K k : map.keySet()) {
			Object value = map.get(k);
			if(value == null) {
				value = NULL;
			}
			result.put(k, value);
		}
		return result;
	}

	public static <T> DbNull<T> forType(Class<T> javaType) {
		return new DbNull<T>(javaType);
	}

	public static DbNull<?> forType(DbGenericType type) {
		return forType(type.getPreferredJavaType());
	}

	public static boolean isNull(Object obj) {
		return obj instanceof DbNull<?>;
	}

	private final Class<T>	javaType;

	public DbNull(Class<T> javaType) {
		this.javaType = javaType;
	}

	public Class<T> getJavaType() {
		return javaType;
	}

	@Override
	public String toString() {
		return "DbNull(" + javaType + ")";
	}
}

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
package org.nuclos.server.dblayer.impl.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.nuclos.server.dblayer.DbTuple;

public class DbTupleImpl implements DbTuple, Serializable {

	public static class DbTupleElementImpl<T> implements DbTuple.DbTupleElement<T> {
		
		private final String alias;
		private final Class<T> javaType;

		public DbTupleElementImpl(String alias, Class<T> javaType) {
			this.alias = alias;
			this.javaType = javaType;
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public Class<T> getJavaType() {
			return javaType;
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append(getClass().getName()).append("[");
			result.append("alias=").append(alias);
			result.append(", type=").append(javaType);
			result.append("]");
			return result.toString();
		}

	}
	
	private final DbTupleElementImpl<?>[] elements;
	private final Object[] values;

	public DbTupleImpl(DbTupleElementImpl<?>[] elements, Object[] values) {
		this.elements = elements;
		this.values = values;
	}
	
	@Override
	public Object get(int index) {
		return values[index];
	}
	
	@Override
	public Object get(String alias) {
		for (int i = 0, n = elements.length; i < n; i++) {
			if (alias.equals(elements[i].getAlias()))
				return values[i];
		}
		throw new IllegalArgumentException("Unknown alias " + alias);
	}
	
	@Override
	public <T> T get(int index, Class<T> type) {
		return type.cast(get(index));
	}
	
	@Override
	public <T> T get(String alias, Class<T> type) {
		return type.cast(get(alias));
	}
	
	@Override
	public <T> T get(DbTupleElement<T> element) {
		for (int i = 0, n = elements.length; i < n; i++) {
			if (element == elements[i])
				return element.getJavaType().cast(values[i]);
		}
		throw new IllegalArgumentException("Invlaid element " + element);
	}
	
	@Override
	public List<DbTupleElement<?>> getElements() {
		return Arrays.<DbTupleElement<?>>asList(elements);
	}

	@Override
	public Object[] toArray() {
		return values;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
}

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

/**
 * An immutable string together with a list of values.
 */
public final class PreparedString implements CharSequence, Serializable {
 
	private static final Object[] EMPTY = new Object[0];
	
	public static PreparedString valueOf(CharSequence cs) {
		return new PreparedString(cs);
	}
	
	private final String string;
	private final Object[] parameters;

	public PreparedString(CharSequence cs) {
		this(cs, EMPTY);
	}
	
	public PreparedString(CharSequence cs, Object...parameters) {
		this.string = cs.toString();
		this.parameters = parameters;
	}

	public Object[] getParameters() {
		return parameters;
	}
	
	public boolean hasParameters() {
		return parameters.length > 0;
	}
	
	public boolean isSameString(PreparedString other) {
		return string.equals(other.string);
	}
	
	@Override
	public char charAt(int index) {
		return string.charAt(index);
	}

	@Override
	public int length() {
		return string.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return string.subSequence(start, end);
	}
	
	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof PreparedString) {
			PreparedString other = (PreparedString) obj;
			return string.equals(other.string) && Arrays.equals(parameters, other.parameters);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return string;
	}
}

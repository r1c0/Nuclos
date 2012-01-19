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
package org.nuclos.tools.ruledoc.javaToHtml;

/**
 * Provides convenient methods for checking contract parameters.
 */
public class Ensure {

	public Ensure() {
		super();
	}

	public static void ensureArgumentNotNull(String message, Object object) throws IllegalArgumentException {
		ensureTrue(message, object != null);
	}

	public static void ensureArgumentNotNull(Object object) throws IllegalArgumentException {
		ensureArgumentNotNull("Object must not be null", object);
	}

	public static void ensureArgumentFalse(boolean state) throws IllegalArgumentException {
		ensureTrue("boolean must be false", !state);
	}

	public static void ensureArgumentFalse(String message, boolean state) throws IllegalArgumentException {
		ensureTrue(message, !state);
	}

	public static void ensureArgumentTrue(boolean state) throws IllegalArgumentException {
		ensureTrue("boolean must be true", state);
	}

	public static void ensureTrue(String message, boolean state) throws IllegalArgumentException {
		if (!state) {
			throw new IllegalArgumentException(message);
		}
	}
}

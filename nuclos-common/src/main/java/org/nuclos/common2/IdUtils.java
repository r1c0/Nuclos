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
package org.nuclos.common2;

/**
 * Temporary Util class to make the primary key transition from Integer to Long
 * more easy.
 *
 * @author Thomas Pasch
 * @since 3.1.01
 */
public class IdUtils {

	private IdUtils() {
		// Never invoked.
	}

	public static Long toLongId(Integer i) {
		if (i == null) return null;
		return Long.valueOf(i.longValue());
	}

	public static Long toLongId(int i) {
		return Long.valueOf(i);
	}

	public static Long toLongId(Object o) {
		if (o == null) return null;
		if (o instanceof Integer) return toLongId((Integer) o);
		else if (o instanceof Long) return (Long) o;
		else throw new IllegalArgumentException("toLongId(" + o + ")");
	}

	public static Integer unsafeToId(Object o) {
		if (o == null) return null;
		if (o instanceof Integer) return (Integer) o;
		else if (o instanceof Long) return unsafeToId((Long) o);
		else throw new IllegalArgumentException("unsafeToId(" + o + ")");
	}

	public static Integer unsafeToId(Long id) {
		if (id == null) return null;
		return Integer.valueOf(id.intValue());
	}

	public static Integer unsafeToId(long id) {
		return Integer.valueOf((int) id);
	}

	public static boolean equals(Long i1, Object i2) {
		if (i1 == null) return i2 == null;
		return i1.equals(toLongId(i2));
	}

	public static boolean equals(Integer i1, Object i2) {
		if (i1 == null) return i2 == null;
		return toLongId(i1).equals(toLongId(i2));
	}

	public static boolean equals(Object i1, Object i2) {
		if (i1 == null) return i2 == null;
		if (i1 instanceof Integer) {
			return equals((Integer) i1, i2);
		}
		else if (i1 instanceof Long) {
			return equals((Long) i1, i2);
		}
		else {
			throw new IllegalArgumentException("IdUtils.equals on " + i1 + " (" + i1.getClass().getName()+ ")");
		}
	}

}

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
package org.nuclos.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Support for cloning java objects.
 *
 * @see Cloneable
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class CloneUtils {
	
	private static final Class<?>[] NO_ARGS = new Class<?>[0];

	private CloneUtils() {
		// Never invoked.
	}

	public static <T> Collection<T> cloneCollection(Collection<T> l) {
		if (l == null)
			return null;
		final Collection<T> result;
		final Class<?> clazz = l.getClass();
		// Don't try this on the unmodifiable stuff
		if (!(l instanceof Cloneable) || clazz.getName().startsWith("java.util.Collections")) {
			if (l instanceof List) {
				return new ArrayList<T>(l);
			}
			else if (l instanceof SortedSet) {
				final SortedSet<T> ss = (SortedSet<T>) l;
				if (ss.comparator() != null) {
					result = new TreeSet<T>(ss.comparator());
					result.addAll(l);
				}
				else {
					result = new TreeSet<T>(l);
				}
			}
			else if (l instanceof Set) {
				result = new HashSet<T>(l);
			}
			else {
				throw new IllegalArgumentException();
			}
			return result;
		}
		try {
			Method m = clazz.getMethod("clone", NO_ARGS);
			result = (Collection<T>) m.invoke(l);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e.toString());
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.toString());
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e.getTargetException().toString());
		} catch (SecurityException e) {
			throw new IllegalStateException(e.toString());
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e.toString());
		}
		return result;
	}

}

package org.nuclos.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CloneUtils {
	
	private static final Class<?>[] NO_ARGS = new Class<?>[0];

	private CloneUtils() {
		// Never invoked.
	}

	public static <T> Collection<T> cloneCollection(Collection<T> l) throws CloneNotSupportedException {
		if (l == null)
			return null;
		final Class<?> clazz = l.getClass();
		// Don't try this on the unmodifiable stuff
		if (clazz.getName().startsWith("java.util.Collections")) {
			if (l instanceof List) {
				return new ArrayList<T>(l);
			}
			else if (l instanceof SortedSet) {
				final SortedSet<T> ss = (SortedSet<T>) l;
				final TreeSet<T> result;
				if (ss.comparator() != null) {
					result = new TreeSet<T>(ss.comparator());
					result.addAll(l);
				}
				else {
					result = new TreeSet<T>(l);
				}
				return result;
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		final Collection<T> result;
		try {
			Method m = clazz.getMethod("clone", NO_ARGS);
			result = (Collection<T>) m.invoke(l);
		} catch (IllegalArgumentException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (IllegalAccessException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (InvocationTargetException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (SecurityException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (NoSuchMethodException e) {
			throw new CloneNotSupportedException(e.toString());
		}
		return result;
	}

}

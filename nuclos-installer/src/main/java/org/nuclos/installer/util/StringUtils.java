package org.nuclos.installer.util;

import java.util.Collection;

public class StringUtils {
	
	private StringUtils() {
		// Never invoked.
	}
	
	public static String join(Object... args) {
		final StringBuilder result = new StringBuilder();
		join(result, args);
		return result.substring(0, result.length() - 1);
	}
	
	public static void join(StringBuilder result, Object[] args) {
		for (Object o: args) {
			join(result, o);
		}
	}
	
	public static void join(StringBuilder result, Collection<?> c) {
		for (Object o: c) {
			join(result, o);
		}
	}
	
	public static void join(StringBuilder result, Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
		if (o instanceof Object[]) {
			final Object[] p = (Object[]) o;
			final int len = p.length;
			for (int i = 0; i < len; ++i) {
				join(result, p[i]);
			}
		}
		else if (o instanceof Collection) {
			join(result, (Collection<?>) o);
		}
		else {
			result.append(o.toString());
			result.append(" ");
		}
	}
	
}

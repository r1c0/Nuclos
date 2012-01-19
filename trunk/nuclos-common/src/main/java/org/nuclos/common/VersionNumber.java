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

package org.nuclos.common;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed version number.
 * 
 * The used version number scheme is similar to the improved version scheme supported 
 * by Maven 3 (http://docs.codehaus.org/display/MAVEN/Versioning).
 */
public class VersionNumber implements Serializable, Comparable<VersionNumber> {
	
	private final List<?> parts;

	public VersionNumber(String versionString) {
		this.parts = parse(versionString);
	}
	
	@Override
	public int compareTo(VersionNumber that) {
		if (this.parts.size() < that.parts.size()) {
			return -that.compareTo(this);
		}
		
		int cmp = 0;
		for (int i = 0, n = parts.size(); i < n; i++) {
			Object p1 = this.parts.get(i);
			Object p2 = i < that.parts.size() ? that.parts.get(i) : null;
			
			if (p1 instanceof BigInteger) {
				if (p2 instanceof String) {
					return 1;
				} else if (p2 == null) {
					p2 = BigInteger.ZERO;
				}
				cmp = ((BigInteger) p1).compareTo((BigInteger) p2);
			} else if (p1 instanceof String) {
				if (p2 instanceof BigInteger) {
					return -1;
				} else if (p2 == null) {
					p2 = "";
				}
				cmp = resolveQualifier((String) p1).compareTo(resolveQualifier((String) p2));
			} else {
				throw new IllegalStateException();
			}
			
			if (cmp != 0)
				break;
		}
		
		return cmp;
	}
	
	@Override
	public int hashCode() {
		return parts.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VersionNumber) {
			return this.compareTo((VersionNumber) obj) == 0;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return parts.toString();
	}
	
	private static List<Object> parse(String versionString) throws IllegalArgumentException {
		LinkedList<Object> list = new LinkedList<Object>();
		String[] split = SPLIT_PATTERN.split(versionString.trim().toLowerCase(Locale.ENGLISH));
		for (String s : split) {
			if (s.isEmpty()) {
				list.add(BigInteger.ZERO);
			} else {
				Matcher m = STRING_PATTERN.matcher(s);
				while (m.find()) {
					if (m.group(1) != null) {
						list.add(new BigInteger(m.group(1)));
					} else {
						list.add(m.group(2));
					}
				}
			}
		}
		// Remove trailing zeros
		while (list.getLast().equals(BigInteger.ZERO) || list.getLast().equals("")) {
			list.removeLast();
		}
		return list;
	}	
	
	private static String resolveQualifier(String s) {
		if (ALIASES.containsKey(s))
			s = ALIASES.get(s);
		int q = QUALIFIERS.indexOf(s);
		return (q != -1) ? q + "" : QUALIFIERS.size() + "-" + s;
	}
	
	private static final Pattern SPLIT_PATTERN = Pattern.compile("[.-]");
	private static final Pattern STRING_PATTERN = Pattern.compile("(\\d+)|([^\\d]+)");
	
	private static final List<String> QUALIFIERS;
	private static final Map<String, String> ALIASES;
	
	static {
		QUALIFIERS = Arrays.asList("alpha", "beta", "milestone", "rc", "snapshot", "", "sp");
		ALIASES = new HashMap<String, String>();
		ALIASES.put("final", "");
		ALIASES.put("ga", "");
		ALIASES.put("cr", "rc");
	}
	
	public static int compare(String versionString1, String versionString2) {
		return new VersionNumber(versionString1).compareTo(new VersionNumber(versionString2));
	}
}
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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Table containing style options (
 * JavaSourceStyleEntry for different types of
 * source codeJavaSourceType.
 *
 *
 */
public class JavaSourceStyleTable {
	private Hashtable<String, JavaSourceStyleEntry> table;
	private static JavaSourceStyleEntry defaultColorEntry = new JavaSourceStyleEntry(RGB.BLACK);
	private String name;

	private JavaSourceStyleTable(String name) {
		setName(name);
		table = new Hashtable<String, JavaSourceStyleEntry>();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JavaSourceStyleTable)) {
			return false;
		}
		JavaSourceStyleTable other = (JavaSourceStyleTable) obj;

		if (!name.equals(other.name)) {
			return false;
		}
		if (other.table.size() != table.size()) {
			return false;
		}

		Enumeration<String> Enumeration = table.keys();
		while (Enumeration.hasMoreElements()) {
			String key = Enumeration.nextElement();
			JavaSourceStyleEntry value = get(key);
			if (!value.equals(other.table.get(key))) {
				return false;
			}
		}
		return true;
	}

	public JavaSourceStyleTable getClone() {
		JavaSourceStyleTable clone = new JavaSourceStyleTable(getName());
		Enumeration<String> Enumeration = table.keys();
		while (Enumeration.hasMoreElements()) {
			String key = Enumeration.nextElement();
			JavaSourceStyleEntry value = get(key);
			clone.table.put(key, value);
		}
		return clone;
	}

	public static JavaSourceStyleTable[] getPredefinedTables() {
		return new JavaSourceStyleTable[] {
				createDefaultEclipseStyleTable(),
				createDefaultKawaStyleTable(),
				createDefaultMonochromeStyleTable(),
		};
	}

	/**
	 * Returns the style table with the given name or <code>null</code> if
	 * there is none having the give name.
	 *
	 * @throws IllegalArgumentException
	 *           if the name is null.
	 */
	public static JavaSourceStyleTable getPredefinedTable(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name is null.");
		}
		JavaSourceStyleTable[] tables = getPredefinedTables();
		for (int i = 0; i < tables.length; ++i) {
			if (tables[i].getName().equalsIgnoreCase(name)) {
				return tables[i];
			}
		}
		return null;
	}

	private static JavaSourceStyleTable createDefaultKawaStyleTable() {
		JavaSourceStyleTable table = new JavaSourceStyleTable("Kawa");
		table.put(JavaSourceType.UNDEFINED, new JavaSourceStyleEntry(new RGB(255, 97, 0)));
		table.put(JavaSourceType.CODE, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.KEYWORD, new JavaSourceStyleEntry(new RGB(0, 0, 192), true, false));
		table.put(JavaSourceType.CODE_TYPE, new JavaSourceStyleEntry(new RGB(192, 0, 0), true, false));
		table.put(JavaSourceType.STRING, new JavaSourceStyleEntry(new RGB(153, 0, 0))); //darker
		// red
		table.put(JavaSourceType.COMMENT_LINE, new JavaSourceStyleEntry(new RGB(0, 128, 0))); //green
		table.put(JavaSourceType.COMMENT_BLOCK, new JavaSourceStyleEntry(new RGB(0, 128, 0))); //green
		table.put(JavaSourceType.JAVADOC, new JavaSourceStyleEntry(new RGB(0, 128, 0))); //green
		table.put(JavaSourceType.JAVADOC_KEYWORD, new JavaSourceStyleEntry(new RGB(0, 85, 0)));
		//dark green
		//    set.put(JavaSourceType.BACKGROUND, new JavaSourceStyleEntry(new
		// Color(255,251,240)));
		table.put(JavaSourceType.BACKGROUND, new JavaSourceStyleEntry(new RGB(255, 255, 255)));
		table.put(JavaSourceType.NUM_CONSTANT, new JavaSourceStyleEntry(new RGB(153, 0, 0)));
		//dark red
		table.put(JavaSourceType.CHAR_CONSTANT, new JavaSourceStyleEntry(new RGB(153, 0, 0)));
		//dark red
		table.put(JavaSourceType.PARENTHESIS, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.JAVADOC_HTML_TAG, new JavaSourceStyleEntry(new RGB(0, 128, 0)));
		table.put(JavaSourceType.JAVADOC_LINKS, new JavaSourceStyleEntry(new RGB(0, 128, 0)));
		table.put(JavaSourceType.LINE_NUMBERS, new JavaSourceStyleEntry(new RGB(128, 128, 128)));
		return table;
	}

	private static JavaSourceStyleTable createDefaultEclipseStyleTable() {
		JavaSourceStyleTable table = new JavaSourceStyleTable("Eclipse");
		table.put(JavaSourceType.CODE, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.KEYWORD, new JavaSourceStyleEntry(new RGB(127, 0, 85), true, false));
		table.put(JavaSourceType.CODE_TYPE, new JavaSourceStyleEntry(new RGB(127, 0, 85), true, false));
		table.put(JavaSourceType.STRING, new JavaSourceStyleEntry(new RGB(42, 0, 255)));
		table.put(JavaSourceType.COMMENT_LINE, new JavaSourceStyleEntry(new RGB(63, 127, 95)));
		table.put(JavaSourceType.COMMENT_BLOCK, new JavaSourceStyleEntry(new RGB(63, 127, 95)));
		table.put(JavaSourceType.JAVADOC, new JavaSourceStyleEntry(new RGB(63, 95, 191)));
		table.put(JavaSourceType.JAVADOC_KEYWORD, new JavaSourceStyleEntry(new RGB(127, 159, 191)));
		table.put(JavaSourceType.NUM_CONSTANT, new JavaSourceStyleEntry(new RGB(153, 0, 0)));
		table.put(JavaSourceType.CHAR_CONSTANT, new JavaSourceStyleEntry(new RGB(153, 0, 0)));
		table.put(JavaSourceType.PARENTHESIS, new JavaSourceStyleEntry(new RGB(0, 0, 0)));
		table.put(JavaSourceType.JAVADOC_HTML_TAG, new JavaSourceStyleEntry(new RGB(127, 127, 159)));
		table.put(JavaSourceType.JAVADOC_LINKS, new JavaSourceStyleEntry(new RGB(63, 63, 191)));
		table.put(JavaSourceType.UNDEFINED, new JavaSourceStyleEntry(new RGB(255, 97, 0)));
		table.put(JavaSourceType.BACKGROUND, new JavaSourceStyleEntry(new RGB(255, 255, 255)));
		table.put(JavaSourceType.LINE_NUMBERS, new JavaSourceStyleEntry(new RGB(128, 128, 128)));
		return table;
	}

	private static JavaSourceStyleTable createDefaultMonochromeStyleTable() {
		JavaSourceStyleTable table = new JavaSourceStyleTable("Monochrome");
		table.put(JavaSourceType.CODE, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.KEYWORD, new JavaSourceStyleEntry(RGB.BLACK, true, false));
		table.put(JavaSourceType.CODE_TYPE, new JavaSourceStyleEntry(RGB.BLACK, true, false));
		table.put(JavaSourceType.STRING, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.COMMENT_LINE, new JavaSourceStyleEntry(RGB.BLACK, false, true));
		table.put(JavaSourceType.COMMENT_BLOCK, new JavaSourceStyleEntry(RGB.BLACK, false, true));
		table.put(JavaSourceType.JAVADOC, new JavaSourceStyleEntry(RGB.BLACK, false, true));
		table.put(JavaSourceType.JAVADOC_KEYWORD, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.NUM_CONSTANT, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.CHAR_CONSTANT, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.PARENTHESIS, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.JAVADOC_HTML_TAG, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.JAVADOC_LINKS, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.UNDEFINED, new JavaSourceStyleEntry(RGB.BLACK));
		table.put(JavaSourceType.BACKGROUND, new JavaSourceStyleEntry(RGB.WHITE));
		table.put(JavaSourceType.LINE_NUMBERS, new JavaSourceStyleEntry(RGB.BLACK));
		return table;
	}

	/**
	 * Sets the style for the given source type to the given style.
	 *
	 * @see #get(JavaSourceType)
	 */
	public void put(JavaSourceType key, JavaSourceStyleEntry javaSourceStyleEntry) {
		put(key.getName(), javaSourceStyleEntry);
	}

	public void put(String key, JavaSourceStyleEntry javaSourceStyleEntry) {
		table.put(key, javaSourceStyleEntry);
	}

	/**
	 * Gets a default style table.
	 *
	 * @see #getDefaultEclipseStyleTable()
	 * @see #getDefaultKawaStyleTable()
	 */
	public static JavaSourceStyleTable getDefault() {
		return createDefaultEclipseStyleTable();
	}

	/**
	 * Gets a style table similar to the one from the Kawa IDE.
	 *
	 * @see #getDefault()
	 * @see #getDefaultEclipseStyleTable()
	 * @see #getDefaultMonochromeStyleTable()
	 */
	public static JavaSourceStyleTable getDefaultKawaStyleTable() {
		return createDefaultKawaStyleTable();
	}

	/**
	 * Gets a style table similar to the one from the IBM Eclipse IDE.
	 *
	 * @see #getDefault()
	 * @see #getDefaultKawaStyleTable()
	 * @see #getDefaultMonochromeStyleTable()
	 */
	public static JavaSourceStyleTable getDefaultEclipseStyleTable() {
		return createDefaultEclipseStyleTable();
	}

	/**
	 * Gets a style table for monochromatic output.
	 *
	 * @see #getDefault()
	 * @see #getDefaultEclipseStyleTable()
	 * @see #getDefaultKawaStyleTable()
	 */
	public static JavaSourceStyleTable getDefaultMonochromeStyleTable() {
		return createDefaultMonochromeStyleTable();
	}

	/**
	 * Returns the style for the given source type defined by this styletable.
	 *
	 * @see #put(JavaSourceType, JavaSourceStyleEntry)
	 * @see #put(JavaSourceType, JavaSourceStyleEntry)
	 */
	public JavaSourceStyleEntry get(JavaSourceType key) {
		return get(key.getName());
	}

	public JavaSourceStyleEntry get(String key) {
		JavaSourceStyleEntry e = table.get(key);
		return e == null ? defaultColorEntry : e;
	}

	public String getName() {
		return name;
	}

	/** @deprecated As of Jan 2, 2004 (Markus Gebhard): Changing the name of a style table is not intended */
	@Deprecated
	public void setName(String name) {
		this.name = name;
	}
}

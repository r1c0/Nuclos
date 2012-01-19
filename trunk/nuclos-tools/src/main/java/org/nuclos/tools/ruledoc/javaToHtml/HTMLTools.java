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

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

/**
 * Some methods for converting text to valid HTML.
 *
 */
public class HTMLTools {
	protected static Hashtable entityTableEncode;

	protected final static String[] ENTITIES = {
			// IGNORE (during encoding!!)
			" ", " ", "-", "-", "'", "'", "`", "`",

			// UPPERCASE
			"&Uuml;", "\u00dc", "&Auml;", "\u00c4", "&Ouml;", "\u00d6", "&Euml;", "\u00cb",
			"&Ccedil;", "\u00c7", "&AElig;", "\u00c6", "&Aring;", "\u00c5", "&Oslash;", "\u00d8",

			// OTHERS -> ignorecase!
			"&uuml;", "\u00fc", "&auml;", "\u00e4", "&ouml;", "\u00f6", "&euml;", "\u00eb",
			"&ccedil;", "\u00e7", "&aring;", "\u00e5", "&oslash;", "\u00f8", "&grave;", "`",
			"&agrave;", "\u00e0", "&egrave;", "\u00e8", "&igrave;", "\u00ec", "&ograve;", "\u00f2",
			"&ugrave;", "\u00f9", "&amp;", "&", "&#34;", "\"",
			// same as &quot; - but &quot; is not part of HTML3.2!!!
			"&szlig;", "\u00df", "&nbsp;", " ", "&gt;", ">", "&lt;", "<", "&copy;",
			"(C)", "&cent;", "\u00a2", "&pound;", "\u00a3", "&laquo;", "\u00ab", "&raquo;",
			"\u00bb", "&reg;", "(R)", "&middot;", " - ", "&times;", " x ",
			"&acute;", "'", "&aacute;", "\u00e1", "&uacute;", "\u00fa", "&oacute;", "\u00f3",
			"&eacute;", "\u00e9", "&iacute;", "\u00ed", "&ntilde;", "\u00f1", "&sect;", "\u00a7",
			"&egrave;", "\u00e8", "&icirc;", "\u00ee", "&ocirc;", "\u00f4", "&acirc;", "\u00e2",
			"&ucirc;", "\u00fb", "&ecirc;", "\u00ea", "&aelig;", "\u00e6", "&iexcl;", "\u00a1",
			"&#151;", "-", "&#0151;", "-", "&#0146;", "'", "&#146;", "'",
			"&#0145;", "'", "&#145;", "'", "&quot;", "\"",};

	private HTMLTools() {
		// No instance available
	}

	protected static void buildEntityTables() {
		entityTableEncode = new Hashtable(ENTITIES.length);

		for (int i = 0; i < ENTITIES.length; i += 2) {
			if (!entityTableEncode.containsKey(ENTITIES[i + 1])) {
				entityTableEncode.put(ENTITIES[i + 1], ENTITIES[i]);
			}
		}
	}

	/**
	 * Converts a String to HTML by converting all special characters to
	 * HTML-entities.
	 */
	public final static String encode(String s, String ignore) {
		return encode(s, 0, s.length(), ignore);
	}

	/**
	 * Converts a String to HTML by converting all special characters to
	 * HTML-entities. Only s,substring(start,end) will be encoded.
	 */
	public final static String encode(String s, int start, int end,
			String ignore) {
		if (entityTableEncode == null) {
			buildEntityTables();
		}

		StringBuffer sb = new StringBuffer((end - start) * 2);
		char ch;
		for (int i = start; i < end; ++i) {
			ch = s.charAt(i);
			if ((ch >= 63 && ch <= 90) || (ch >= 97 && ch <= 122)
					|| ignore.indexOf(ch) != -1) {
				sb.append(ch);
			}
			else {
				sb.append(encodeSingleChar(String.valueOf(ch)));
			}
		}
		return sb.toString();
	}

	/**
	 * Converts a single character to HTML
	 */
	protected final static String encodeSingleChar(String ch) {
		String s = (String) entityTableEncode.get(ch);
		return (s == null) ? ch : s;
	}

	/**
	 * Converts the given Color object to a String contaning the html
	 * description of the color. E.g.: #FF8080.
	 */
	public final static String toHTML(RGB color) {
		String red = Integer.toHexString(color.getRed());
		String green = Integer.toHexString(color.getGreen());
		String blue = Integer.toHexString(color.getBlue());

		if (red.length() == 1) {
			red = "0" + red;
		}
		if (green.length() == 1) {
			green = "0" + green;
		}
		if (blue.length() == 1) {
			blue = "0" + blue;
		}

		return "#" + red + green + blue;
	}
}

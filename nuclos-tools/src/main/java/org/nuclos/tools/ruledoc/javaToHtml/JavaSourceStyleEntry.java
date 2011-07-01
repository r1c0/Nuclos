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
 * Object defining color and other style options for output.
 *
 *
 */
public class JavaSourceStyleEntry {
	private RGB color;
	private String htmlColor;
	private boolean bold;
	private boolean italic;

	public JavaSourceStyleEntry(RGB color) {
		this(color, false, false);
	}

	public JavaSourceStyleEntry(RGB color, boolean bold, boolean italic) {
		this.color = color;
		this.italic = italic;
		this.bold = bold;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JavaSourceStyleEntry)) {
			return false;
		}
		JavaSourceStyleEntry other = (JavaSourceStyleEntry) obj;
		return color.equals(other.color) && bold == other.bold && italic == other.italic;
	}

	public JavaSourceStyleEntry getClone() {
		return new JavaSourceStyleEntry(color, bold, italic);
	}

	public String getHtmlColor() {
		if (htmlColor == null) {
			htmlColor = HTMLTools.toHTML(getColor());
		}
		return htmlColor;
	}

	public RGB getColor() {
		return color;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}
}

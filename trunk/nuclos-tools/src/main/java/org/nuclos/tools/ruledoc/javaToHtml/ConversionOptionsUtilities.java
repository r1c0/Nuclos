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

public class ConversionOptionsUtilities {
	private ConversionOptionsUtilities() {
		//nothing to do
	}

	public static String[] getPredefinedStyleTableNames() {
		JavaSourceStyleTable[] tables = JavaSourceStyleTable.getPredefinedTables();
		String[] names = new String[tables.length];
		for (int i = 0; i < tables.length; i++) {
			names[i] = tables[i].getName();
		}
		return names;
	}

	public static String getPredefinedStyleTableNameString() {
		String[] names = getPredefinedStyleTableNames();
		return ConversionOptionsUtilities.getCommaSeparatedString(names);
	}

	public static String[] getAvailableHorizontalAlignmentNames() {
		HorizontalAlignment[] tables = HorizontalAlignment.getAll();
		String[] names = new String[tables.length];
		for (int i = 0; i < tables.length; i++) {
			names[i] = tables[i].getName();
		}
		return names;
	}

	public static String getAvailableHorizontalAlignmentNameString() {
		String[] names = getAvailableHorizontalAlignmentNames();
		return ConversionOptionsUtilities.getCommaSeparatedString(names);
	}

	private static String getCommaSeparatedString(String[] names) {
		return getSeparatedString(names, ", ");
	}

	public static String getSeparatedString(String[] strings, String separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(strings[i]);
		}
		return sb.toString();
	}
}

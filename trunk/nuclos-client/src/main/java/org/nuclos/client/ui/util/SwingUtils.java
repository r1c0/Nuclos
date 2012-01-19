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
package org.nuclos.client.ui.util;

import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SwingUtils {
	
	private SwingUtils() {
		// Never invoked.
	}
	
	public static String headerString(JTable t) {
		return headerString(t.getColumnModel());
	}
	
	public static String headerString(TableColumnModel m) {
		final StringBuilder result = new StringBuilder();
		result.append('[');
		for (Enumeration<TableColumn> en = m.getColumns(); en.hasMoreElements();) {
			final TableColumn tc = en.nextElement();
			result.append(headerString(tc));
			if (en.hasMoreElements()) {
				result.append(", ");
			}
		}
		result.append(']');
		return result.toString();
	}
	
	public static String headerString(TableColumn tc) {
		final StringBuilder result = new StringBuilder();
		result.append(tc.getHeaderValue());
		result.append('[').append(tc.getPreferredWidth()).append(']');		
		return result.toString();
	}

}

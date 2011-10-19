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

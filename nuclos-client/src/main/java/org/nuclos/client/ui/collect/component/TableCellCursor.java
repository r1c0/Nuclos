package org.nuclos.client.ui.collect.component;

import java.awt.Cursor;

public interface TableCellCursor {

	public Cursor getCursor(Object cellValue, int cellWidth, int x);
}

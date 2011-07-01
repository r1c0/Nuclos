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
package org.nuclos.client.ui.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Utility methods for tables.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class TableUtils {
	public static final int TABLE_INSETS = 10;

	protected TableUtils() {
	}

	// Table width utilities:

	/**
	 * calculates the preferred header width by asking the header renderer.
	 * @param tbl
	 * @param iColumn
	 * @return the preferred header width for column <code>iColumn</code> of the given table
	 */
	public static int getPreferredHeaderWidth(JTable tbl, int iColumn) {
		TableColumn column = tbl.getColumnModel().getColumn(iColumn);

		TableCellRenderer renderer = column.getHeaderRenderer();
		if (renderer == null) {
			renderer = tbl.getTableHeader().getDefaultRenderer();
		}
		final Component compHeader = renderer.getTableCellRendererComponent(tbl, column.getHeaderValue(), false, false, 0,
				iColumn);

		int result = compHeader.getPreferredSize().width;

		result += tbl.getIntercellSpacing().getWidth();
		// this is necessary for JRE 1.4+

		return result;
	}

	/**
	 * @param table
	 * @param iColumn
	 * @param oValueToConsider the value to consider
	 * @return the preferred cell width for column iColumn of table based on <code>oValueToConsider</code>
	 */
	public static int getPreferredCellWidth(JTable table, int iColumn, Object oValueToConsider) {
		final TableCellRenderer cellRenderer = table.getDefaultRenderer(table.getModel().getColumnClass(iColumn));
		if (cellRenderer == null) {
			final String sMessage = "JTable.getDefaultRenderer is buggy. Use CommonJTable as a workaround.";
			throw new CommonFatalException(sMessage);
		}
		Component compCell = cellRenderer.getTableCellRendererComponent(table, oValueToConsider, false, false, 0, iColumn);

		return compCell.getPreferredSize().width + (int) Math.round(table.getIntercellSpacing().getWidth());
	}  // getPreferredCellWidth

	/**
	 * @return the preferred cell width for column iColumn of table based on
	 *   the values in the table's model.
	 * @param tbl
	 * @param iColumn
	 * @param iMaxRowsToConsider the first <code>iMaxRowsToConsider</code> in the table's model
	 *   are considered.
	 */
	public static int getPreferredCellWidth(JTable tbl, int iColumn, int iMaxRowsToConsider) {
		int result = 0;
		int iMaxRow = Math.min(iMaxRowsToConsider, tbl.getModel().getRowCount());
		for (int iRow = 0; iRow < iMaxRow; ++iRow) {
			final Object oValue = tbl.getValueAt(iRow, iColumn);

			// see how much space the cell renderer needs:
			final TableCellRenderer cellRenderer = tbl.getCellRenderer(iRow, iColumn);
			if (cellRenderer == null) {
				final String sMessage = "JTable.getDefaultRenderer is buggy. Use CommonJTable as a workaround.";
				throw new CommonFatalException(sMessage);
			}
			final Component compCellRenderer = cellRenderer.getTableCellRendererComponent(tbl, oValue, false, false, iRow,
					iColumn);
			result = Math.max(result, compCellRenderer.getPreferredSize().width);

			// see how much space the cell editor needs:
			final TableCellEditor cellEditor = tbl.getCellEditor(iRow, iColumn);
			if (cellEditor == null) {
				/** @todo CommonJTable must do the same for the editor as for the renderer. */
				final String sMessage = "JTable.getDefaultRenderer is buggy. Use CommonJTable as a workaround.";
				throw new CommonFatalException(sMessage);
			}
			final Component compCellEditor = cellEditor.getTableCellEditorComponent(tbl, oValue, false, iRow, iColumn);
			// another workaround here: the cell editor component may be null
			final int iWidth = (compCellEditor == null) ? 0 : compCellEditor.getPreferredSize().width;
			result = Math.max(result, iWidth);
		}

		result += tbl.getIntercellSpacing().getWidth();

		return result;
	}

	/**
	 * gets the preferred column width as maximum of the preferred header width
	 * and the preferred cell width.
	 * @param table
	 * @param iColumn
	 * @param oValueToConsider @see #getPreferredCellWidth()
	 * @param iInsets additional space in a cell
	 * @todo quick&dirty. do this in TableCellRenderer / TableHeaderRenderer
	 */
	public static int getPreferredColumnWidth(JTable table, int iColumn, Object oValueToConsider, int iInsets) {
		final int headerWidth = getPreferredHeaderWidth(table, iColumn);
		final int cellWidth = getPreferredCellWidth(table, iColumn, oValueToConsider);

		return Math.max(headerWidth, cellWidth) + iInsets;
	}

	/**
	 * gets the preferred column width as maximum of the preferred header width
	 * and the preferred cell width.
	 * @param table
	 * @param iColumn
	 * @param iMaxRowsToConsider the first iMaxRowsToConsider in the table's model
	 * are considered.
	 * @param iInsets additional space in a cell
	 */
	public static int getPreferredColumnWidth(JTable table, int iColumn, int iMaxRowsToConsider, int iInsets) {
		final int headerWidth = getPreferredHeaderWidth(table, iColumn);
		final int cellWidth = getPreferredCellWidth(table, iColumn, iMaxRowsToConsider);

		return Math.max(headerWidth, cellWidth) + iInsets;
	}

	/**
	 * sets the preferred column width as maximum of the preferred header width
	 * and the preferred cell width.
	 * @param table
	 * @param iColumn
	 * @param oValueToConsider @see #getPreferredCellWidth()
	 * @param iInsets additional space in a cell
	 * @todo quick&dirty. do this in TableCellRenderer / TableHeaderRenderer
	 */
	public static void setPreferredColumnWidth(JTable table, int iColumn, Object oValueToConsider, int iInsets) {
		final TableColumn column = table.getColumnModel().getColumn(iColumn);

		column.setPreferredWidth(getPreferredColumnWidth(table, iColumn, oValueToConsider, iInsets));
	}

	/**
	 * sets the preferred column width as maximum of the preferred header width
	 * and the preferred cell width.
	 * @param table
	 * @param iColumn
	 * @param iMaxRowsToConsider the first iMaxRowsToConsider in the table's model
	 * are considered.
	 * @param iInsets additional space in a cell
	 */
	public static void setPreferredColumnWidth(JTable table, int iColumn, int iMaxRowsToConsider, int iInsets) {
		final TableColumn column = table.getColumnModel().getColumn(iColumn);

		column.setPreferredWidth(getPreferredColumnWidth(table, iColumn, iMaxRowsToConsider, iInsets));
	}

	/**
	 * sets the preferred column width as maximum of the preferred header width
	 * and the preferred cell width, for all columns in the table.
	 * @param table
	 * @param iMaxRowsToConsider the first iMaxRowsToConsider in the table's model
	 * are considered.
	 * @param iInsets additional space in a cell
	 */
	public static void setPreferredColumnWidth(JTable table, int iMaxRowsToConsider, int iInsets) {
		for (int iColumn = 0; iColumn < table.getModel().getColumnCount(); ++iColumn) {
			setPreferredColumnWidth(table, iColumn, iMaxRowsToConsider, iInsets);
		}
	}


	/**
	 * Sets the optimal width (relating to columns contents) for the column with specified index.
	 * It sets default values for insets and maxRowstoConsider, and also calls setWidth,
	 * which seems sometimes necessary.
	 * @param tbl table to process
	 * @param iColumn index of the column to resize
	 */
	public static void setOptimalColumnWidth(JTable tbl, int iColumn) {
		final TableColumn column = tbl.getColumnModel().getColumn(iColumn);
		final int iPreferredCellWidth = getPreferredColumnWidth(tbl, iColumn, 50, TABLE_INSETS);
		column.setPreferredWidth(iPreferredCellWidth);
		column.setWidth(iPreferredCellWidth);
	}

	/**
	 * Sets the optimal width (relating to column contents) for all columns of a table.
	 * @param tbl the table to process
	 */
	public static void setOptimalColumnWidths(JTable tbl) {
		for (int iColumn = 0; iColumn < tbl.getColumnCount(); iColumn++) {
			setOptimalColumnWidth(tbl, iColumn);
		}

		tbl.revalidate();
	}

	/**
	 * @param tbl
	 * @return the last visible row in the given table.
	 */
	public static int getLastVisibleRow(JTable tbl) {
		return getLastVisibleRow(tbl, UIUtils.getMaxVisibleY(tbl));
	}

	/**
	 * @param tbl
	 * @param iYMax the maximum visible y-value in the table.
	 * @return the last visible row for the given table.
	 */
	public static int getLastVisibleRow(JTable tbl, int iYMax) {
		return tbl.rowAtPoint(new Point(0, iYMax));
	}

	/**
	 * adds a mouse listener to the table header to trigger a table sort when a column heading is clicked in the JTable.
	 * @param tbl
	 * @param tblmodel
	 */
	public static void addMouseListenerForSortingToTableHeader(JTable tbl, SortableTableModel tblmodel) {
		TableUtils.addMouseListenerForSortingToTableHeader(tbl, tblmodel, null);
	}

	/**
	 * adds a mouse listener to the table header to trigger a table sort when a column heading is clicked in the JTable.
	 * @param tbl
	 * @param tblmodel
	 * @param runnableSort Runnable to execute for sorting the table. If <code>null</code>, <code>tblmodel.sort()</code> is performed.
	 */
	public static void addMouseListenerForSortingToTableHeader(JTable tbl, SortableTableModel tblmodel, CommonRunnable runnableSort) {
		// clicking header does not mean column selection, but sorting:
		tbl.setColumnSelectionAllowed(false);

		tbl.getTableHeader().addMouseListener(new TableHeaderMouseListenerForSorting(tbl, tblmodel, runnableSort));
	}

	/**
	 * removes all mouse listeners that were added with <code>addMouseListenerForSortingToTableHeader</code>
	 * from the table header.
	 * @param tbl
	 */
	public static void removeMouseListenersForSortingFromTableHeader(JTable tbl) {
		final MouseListener[] amouselisteners = tbl.getTableHeader().getListeners(MouseListener.class);
		for (int i = 0; i < amouselisteners.length; i++) {
			if (amouselisteners[i] instanceof TableHeaderMouseListenerForSorting) {
				tbl.getTableHeader().removeMouseListener(amouselisteners[i]);
			}
		}
	}
}  // class TableUtils

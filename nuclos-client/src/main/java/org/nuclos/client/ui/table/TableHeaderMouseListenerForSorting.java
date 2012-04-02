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
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * MouseListener implementing sorting by clicking the table header and resizing
 * by double clicking between two columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version	01.00.00
 */

public class TableHeaderMouseListenerForSorting extends MouseAdapter {
	private final JTable tbl;
	private SortableTableModel tblmodel;
	private final CommonRunnable runnableSort;

	private final static int colSeparatorLeftWidth = 4;
	private final static int colSeparatorRightWidth = 3;

	public TableHeaderMouseListenerForSorting(JTable tbl, final SortableTableModel tblmodel) {
		this(tbl, tblmodel, null);
	}

	/**
	 * @param tbl
	 * @param tblmodel
	 * @param runnableSort Runnable to execute for sorting the table. If <code>null</code>, <code>tblmodel.sort()</code> is performed.
	 */
	public TableHeaderMouseListenerForSorting(JTable tbl, final SortableTableModel tblmodel, CommonRunnable runnableSort) {
		this.tbl = tbl;
		this.tblmodel = tblmodel;

		this.runnableSort = (runnableSort != null) ? runnableSort : new CommonRunnable() {
			@Override
            public void run() {
				tblmodel.sort();
			}
		};
	}

	@Override
	public void mouseClicked(final MouseEvent ev) {
		UIUtils.runShortCommand(this.getParentForTable(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				if ((ev.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
					// try to stop editing in case editing is going on:
					if (!tbl.isEditing() || tbl.getCellEditor().stopCellEditing()) {
						// Define the widths of the column header separator zone to the left and right of the margin
						// There seems to be no way to get or set these values via the API, neither control nor UI

						int iColumn = tbl.getColumnModel().getColumnIndexAtX(ev.getX());

						// Determine if click is on column separator
						boolean bOnColumnSeparator = false;
						Rectangle rect = tbl.getTableHeader().getHeaderRect(iColumn);
						int iColLeftMargin = (int) rect.getX();
						int iColRightMargin = (int) (rect.getX() + rect.getWidth());
						if (iColLeftMargin + colSeparatorRightWidth > ev.getX()) {
							// In column separator zone of the column to the left of the header
							bOnColumnSeparator = true;

							// Adjust the column to the column left of the header where the click happened
							if (iColumn > 0) {
								iColumn--;
							}
						}
						else if (iColRightMargin - colSeparatorLeftWidth < ev.getX()) {
							// In column separator zone of the same header
							bOnColumnSeparator = true;
						}
						if (bOnColumnSeparator && ev.getClickCount() == 2 && iColumn > -1) {
							TableUtils.setOptimalColumnWidth(tbl, iColumn);
							return;
						}

						if (iColumn != -1 && !bOnColumnSeparator && ev.getClickCount() == 1) {

							// Map physical column to column in model; not desired for column width, but essential for sorting...
							iColumn = convertColumnIndexToModel(iColumn);
							if (iColumn == -1 || tblmodel.isSortable(iColumn)) {
								sortColumn( iColumn, tbl);
							}
						}
					}
				}
			}
		});
	}

	/** sort the column in the table model
	 * 
	 * @param iColumn
	 * @param windowComponent
	 * @throws CommonBusinessException
	 */
	protected void sortColumn( int iColumn, Component windowComponent) throws CommonBusinessException
	{
		final Window window = UIUtils.getWindowForComponent(tbl);
		try {
			// Set cursor to wait for calls not executed in a command, which sets the cursor for itself
			window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// If a new column is clicked, sort ascending.
			// If the same column is clicked again, toggle order (ascending/descending):
			tblmodel.toggleSortOrder(iColumn, false);
			runnableSort.run();
		}
		finally {
			window.setCursor(null);
		}
	}
	
	/**
	 * map the column model index to the model index
	 * @param viewColumnIndex
	 * @return the model column index
	 */
	protected int convertColumnIndexToModel(int viewColumnIndex) {
		
		return tbl.convertColumnIndexToModel(viewColumnIndex);
	}
		 
	public void setTableModel(SortableTableModel aTablmodel ) {
		
		this.tblmodel = aTablmodel;
	}
	
	/**
	 * tries to find a suitable parent for error messages.
	 * @return the enclosing internal frame, if any - or the enclosing scrollpane, if any - or the table itself.
	 */
	private Component getParentForTable() {
		final Component result;
		final Component compInternalFrame = UIUtils.getTabForComponent(this.tbl);
		if (compInternalFrame != null) {
			result = compInternalFrame;
		}
		else {
			final Component compTableParent = this.tbl.getParent();
			if (compTableParent != null && (compTableParent instanceof JScrollPane)) {
				result = compTableParent;
			}
			else {
				result = this.tbl;
			}
		}
		return result;
	}

}  // class TableHeaderMouseListenerForSorting

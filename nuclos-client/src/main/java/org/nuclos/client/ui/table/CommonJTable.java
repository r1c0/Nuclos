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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Workaround for JTable flaws. Nothing more than that, just a fixed implementation for the regular JTable.
 * <i>Don't provide custom functionality in this class. Don't use this class in interfaces.</i>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonJTable extends JTable {

	/**
	 * The <code>JTable.getDefaultRenderer</code> returns <code>null</code>
	 * if <code>clsColumn</code> is an interface.
	 * @param clsColumn
	 * @return
	 */
	@Override
	public TableCellRenderer getDefaultRenderer(Class<?> clsColumn) {
		TableCellRenderer result = super.getDefaultRenderer(clsColumn);
		if (result == null) {
			result = super.getDefaultRenderer(Object.class);
		}
		return result;
	}

	/**
	 * The <code>JTable.getDefaultEditor</code> returns <code>null</code>
	 * if <code>clsColumn</code> is an interface.
	 * @param clsColumn
	 * @return
	 */
	@Override
	public TableCellEditor getDefaultEditor(Class<?> clsColumn) {
		TableCellEditor result = super.getDefaultEditor(clsColumn);
		if (result == null) {
			result = super.getDefaultEditor(Object.class);
		}
		return result;
	}

	/**
	 * Workaround for known bug: table cancels editing when user resizes column
	 * (see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4330950">Bug 4330950 in the Sun Bug Database</a>)
	 * @param ev
	 */
	@Override
	public void columnMarginChanged(ChangeEvent ev) {
		if (isEditing()) {
			getCellEditor().stopCellEditing();
		}
		super.columnMarginChanged(ev);
	}

	/**
	 * Workaround for JTable flaw: JTable.getToolTipText(MouseEvent) calls the renderer component's getToolTipText().
	 * This doesn't work for dynamic tooltips implemented in the renderer components children.
	 */
	@Override
	public String getToolTipText(MouseEvent ev) {
		String result = null;
		final Point p = ev.getPoint();

		final int iColumn = this.columnAtPoint(p);
		final int iRow = this.rowAtPoint(p);

		if ((iColumn != -1) && (iRow != -1)) {
			// Locate the renderer under the event location:
			final TableCellRenderer renderer = this.getCellRenderer(iRow, iColumn);
			final Component comp = this.prepareRenderer(renderer, iRow, iColumn);

			// Now have to see if the component is a JComponent before getting the tip:
			if (comp instanceof JComponent) {
				// convert the event to the renderer's coordinate system:
				final Rectangle rect = this.getCellRect(iRow, iColumn, false);
				p.translate(-rect.x, -rect.y);

				// We have to set the bounds (esp. the size) of the component as that is not done by prepareRenderer:
				comp.setBounds(rect);

				// find the topmost child component:
				final Component compTarget = ((JComponent) comp).findComponentAt(p);
				if(compTarget instanceof JComponent) {
					// and convert the point to its coordinate system:
					p.translate(-compTarget.getX(), -compTarget.getY());

					final MouseEvent evTranslated = new MouseEvent(compTarget, ev.getID(),
							ev.getWhen(), ev.getModifiers(),
							p.x, p.y, ev.getClickCount(),
							ev.isPopupTrigger());

					result = ((JComponent) compTarget).getToolTipText(evTranslated);
				}
			}
		}

		// No result from the renderer get our own result:
		if (result == null) {
			result = this.getToolTipText();
		}

		return result;
	}
}  // class CommonJTable

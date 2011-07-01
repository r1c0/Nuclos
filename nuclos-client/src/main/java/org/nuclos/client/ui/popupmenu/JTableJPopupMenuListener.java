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
package org.nuclos.client.ui.popupmenu;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Decorator for a <code>JPopupListener</code> that can be used on a <code>JTable</code>.
 * This decorator cares for proper selection behaviour when pressing the right mouse button,
 * before opening the popup menu itself. The latter is handled by the delegate.
 * Note that this isn't really platform independent, but should be working on all platforms where popup menus are
 * opened with the right mouse button.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class JTableJPopupMenuListener extends DefaultJPopupMenuListener {

	private final JTable tbl;

	public JTableJPopupMenuListener(JTable tbl, JPopupMenuFactory factory) {
		super(factory);
		this.tbl = tbl;
	}

	@Override
	public void mousePressed(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			// 1. select/deselect the row:
			final int iRow = this.tbl.rowAtPoint(ev.getPoint());
			// Only select the row if it isn't selected already.
			// Note that right-control-clicking an already selected row won't deselect it, as that wouldn't be useful
			// behavior when a popup menu is about to be opened.
			if (!this.tbl.isRowSelected(iRow)) {
				if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					// Control pressed: add row to selection:
					this.tbl.addRowSelectionInterval(iRow, iRow);
				}
				else {
					// Otherwise select just this row:
					this.tbl.setRowSelectionInterval(iRow, iRow);
				}
			}
		}

		// 2. After that, delegate the event (open the popup menu):
		super.mousePressed(ev);
	}

}	// class JTableJPopupMenuListener

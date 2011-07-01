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

package org.nuclos.client.ui.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JTable;

public abstract class PopupMenuMouseAdapter implements MouseListener {
	private final JTable   table;
	
	public PopupMenuMouseAdapter(JTable table) {
		this.table = table;
	}
	
	public PopupMenuMouseAdapter() {
		this(null);
	}
	
	public abstract void doPopup(MouseEvent e);

	private void checkAndDo(MouseEvent e) {
		if(e.isPopupTrigger()) {
			if(table != null) {
				int clickRow = table.rowAtPoint(e.getPoint());
				int clickCol = table.columnAtPoint(e.getPoint());
				int[] selectedRows = table.getSelectedRows();
				
				if(clickRow >= 0 && clickCol >= 0 && Arrays.binarySearch(selectedRows, clickRow) < 0)
					table.changeSelection(clickRow, clickCol, false, false);
			}
			doPopup(e);
		}
	}
	
	@Override
    public void mouseClicked(MouseEvent e) {
		checkAndDo(e);
    }

	@Override
    public void mousePressed(MouseEvent e) {
		checkAndDo(e);
    }

	@Override
    public void mouseReleased(MouseEvent e) {
		checkAndDo(e);
    }

	@Override
    public void mouseEntered(MouseEvent e) {
    }

	@Override
    public void mouseExited(MouseEvent e) {
    }
}

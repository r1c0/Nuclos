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
package org.nuclos.client.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;

public class TableRowMouseOverAdapter extends MouseAdapter {
	
	public static TableRowMouseOverAdapter add(JComponent comp) {
		TableRowMouseOverAdapter result = new TableRowMouseOverAdapter();
		comp.addMouseListener(result);
		comp.addMouseMotionListener(result);
		return result;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if(e.getSource() instanceof TableRowMouseOverSupport) {
			TableRowMouseOverSupport trmos = (TableRowMouseOverSupport) e.getSource();
			trmos.setMouseOverRow(-1);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if(e.getSource() instanceof JTable) {
			JTable table = (JTable)e.getSource();
			int row = table.rowAtPoint(e.getPoint());	
			
			if(e.getSource() instanceof TableRowMouseOverSupport) {
				TableRowMouseOverSupport trmos = (TableRowMouseOverSupport) e.getSource();
				if (row != trmos.getMouseOverRow()) {
					trmos.setMouseOverRow(row);
				}
			}
		}
	}
}

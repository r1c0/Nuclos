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
package org.nuclos.client.ui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * This TableCellRenderer prevents (strange) <em>vertical</em> alignment of cells filled with 
 * HTML JLabels.  
 * <p>
 * This sometimes occurs when the HTML text is wrapped and the table cell is scrolled.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class TopTableCellRendererDelegate implements TableCellRenderer {
	
	private final TableCellRenderer wrapped;
	
	public TopTableCellRendererDelegate(TableCellRenderer wrapped) {
		if (wrapped == null) {
			throw new NullPointerException();
		}
		this.wrapped = wrapped;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		final Component result = wrapped.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (result instanceof JLabel) {
			final JLabel jlabel = (JLabel) result;
			jlabel.setVerticalAlignment(SwingConstants.TOP);
			jlabel.setVerticalTextPosition(SwingConstants.TOP);
		}
		return result;
	}

}

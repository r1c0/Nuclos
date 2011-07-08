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
package org.nuclos.client.ui.collect;

import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;


/**
 * A specialization of SelectFixedColumnsPanel for also choosing 'columns'
 * from a pivot (key -> value) table.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotPanel extends SelectFixedColumnsPanel {
	
	private static class Header extends JPanel {
		
		private final JCheckBox checkbox;
		
		private Header() {
			checkbox = new JCheckBox("Test");
			add(checkbox);
			setVisible(true);
			checkbox.setVisible(true);
			checkbox.setEnabled(true);
			checkbox.setSelected(false);
		}
		
		public JCheckBox getCheckbox() {
			return checkbox;
		}
	}
	
	public PivotPanel() {
		super(new Header());		
	}
	
	public void addActionListener(ActionListener l) {
		if (getHeader() != null)
			getHeader().getCheckbox().addActionListener(l);
	}
	
	private Header getHeader() {
		return (Header) getHeaderComponent();
	}
		
}

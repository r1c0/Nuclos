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
package org.nuclos.client.masterdata.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

public class ColorCellRenderer extends JLabel implements ListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Component getListCellRendererComponent(
      JList list,
      MasterDataWithDependantsVOWrapper value,            // value to display
      int index,               // cell index
      boolean isSelected,      // is the cell selected
      boolean cellHasFocus)    // the list and the cell have the focus
    {
		String s = value.toString();
		setText(s);
		Color color = Color.RED;
		if(value.isWrapped()){
			color = Color.ORANGE;
		} else {
			if(value.isMapped()){
				color = Color.BLUE;
			} 
		}
		setForeground(color);
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			//setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			//setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}

    @Override
    public Component getListCellRendererComponent(
    	      JList list,
    	      Object value,            // value to display
    	      int index,               // cell index
    	      boolean isSelected,      // is the cell selected
    	      boolean cellHasFocus)    // the list and the cell have the focus
    	    {
    			if(value instanceof MasterDataWithDependantsVOWrapper){
    				return getListCellRendererComponent(list, (MasterDataWithDependantsVOWrapper)value, index, isSelected, cellHasFocus);
    			} else {
    				return this;
    			}
    	    }
}

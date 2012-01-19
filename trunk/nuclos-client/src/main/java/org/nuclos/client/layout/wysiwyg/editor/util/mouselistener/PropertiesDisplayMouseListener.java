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
package org.nuclos.client.layout.wysiwyg.editor.util.mouselistener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;

/**
 * Small {@link MouseListener} class adding opening {@link PropertiesPanel} on double click on a {@link WYSIWYGComponent}
 * 
 * NUCLEUSINT-556
 * @author hartmut.beckschulze
 *
 */
public class PropertiesDisplayMouseListener implements MouseListener {
	
	private WYSIWYGComponent component;
	private TableLayoutUtil tableLayoutUtil;
	
	public PropertiesDisplayMouseListener(WYSIWYGComponent component, TableLayoutUtil tableLayoutUtil) {
		this.component = component;
		this.tableLayoutUtil = tableLayoutUtil;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
			// NUCLOSINT-681
			if (!PropertiesPanel.checkIfAlreadyShowingForComponent(component))
				PropertiesPanel.showPropertiesForComponent(component, tableLayoutUtil);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		 // NUCLEUSINT-992
		((JComponent)component).requestFocus();
	}

	@Override
	public void mouseReleased(MouseEvent e) {}
	
}

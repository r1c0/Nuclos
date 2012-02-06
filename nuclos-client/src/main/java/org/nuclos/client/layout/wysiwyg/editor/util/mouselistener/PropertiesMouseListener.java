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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ComponentPopUp;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;

/**
 * This class controls the {@link ComponentPopUp} for editing the Properties of a {@link WYSIWYGComponent}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertiesMouseListener implements MouseListener {

	private TableLayoutPanel container;

	/**
	 * The Constructor
	 * 
	 * @param container
	 */
	public PropertiesMouseListener(TableLayoutPanel container) {
		this.container = container;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This Method opens the {@link ComponentPopUp} when the User clicks with {@link MouseEvent#BUTTON3}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			/** show the properties for the component */
			ComponentPopUp popup = new ComponentPopUp(container.getCurrentTableLayoutUtil(), e.getComponent(), e.getX());
			Point loc = container.getCurrentTableLayoutUtil().getContainer().getMousePosition();
			if (loc == null) {
				loc = (Point)e.getLocationOnScreen().clone();
				SwingUtilities.convertPointFromScreen(loc, e.getComponent());
			}
			popup.showComponentPropertiesPopup(loc);
		}
	}

}

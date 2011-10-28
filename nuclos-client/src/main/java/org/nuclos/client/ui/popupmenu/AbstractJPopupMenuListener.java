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

import java.awt.IllegalComponentStateException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

/**
 * Abstract implementation of <code>JPopupMenuListener</code>.
 * Encapsulates the platform-specific logic about which mouse events lead to opening a popup menu,
 * in a platform independent way.
 * This class may be directly subclassed by clients (implementing getJPopupMenu()) or indirectly by
 * using the direct subclasses listed below.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @see DefaultJPopupMenuListener
 * @see DynamicJPopupMenuListener
 * @see LazyJPopupMenuListener
 */
public abstract class AbstractJPopupMenuListener extends MouseAdapter {

	private static final Logger LOG = Logger.getLogger(AbstractJPopupMenuListener.class);

	/**
	 * For some look&feels, a popup menu is opened when the mouse is pressed on a component.
	 * @param ev
	 */
	@Override
	public void mousePressed(MouseEvent ev) {
		if (ev.isPopupTrigger()) {
			showPopupMenu(ev);
		}
	}

	/**
	 * For other look&feels, a popup menu is opened when the mouse is released on a component.
	 * @param ev
	 */
	@Override
	public void mouseReleased(MouseEvent ev) {
		if (ev.isPopupTrigger()) {
			this.showPopupMenu(ev);
		}
	}

	/**
	 * shows the popup menu, if any.
	 * @param ev
	 */
	protected void showPopupMenu(MouseEvent ev) {
		final JPopupMenu popupmenu = this.getJPopupMenu(ev);
		if (popupmenu != null) {
			try {
				popupmenu.show(ev.getComponent(), ev.getX(), ev.getY());
			}
			catch(IllegalComponentStateException e){
				// popup kann nicht angezeigt werden
				LOG.info("showPopupMenu failed: " + e, e);
			}
		}
	}
	
	protected abstract JPopupMenu getJPopupMenu(MouseEvent ev);

}	// class AbstractJPopupMenuListener

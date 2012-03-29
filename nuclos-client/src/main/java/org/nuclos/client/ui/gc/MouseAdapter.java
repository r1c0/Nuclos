//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
//
package org.nuclos.client.ui.gc;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;

class MouseAdapter extends EventAdapter implements MouseListener {

	MouseAdapter(EventListener wrapped) {
		super(wrapped);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		final MouseListener l = (MouseListener) wrapped.get();
		if (l != null) {
			l.mouseClicked(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		final MouseListener l = (MouseListener) wrapped.get();
		if (l != null) {
			l.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		final MouseListener l = (MouseListener) wrapped.get();
		if (l != null) {
			l.mouseReleased(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		final MouseListener l = (MouseListener) wrapped.get();
		if (l != null) {
			l.mouseEntered(e);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		final MouseListener l = (MouseListener) wrapped.get();
		if (l != null) {
			l.mouseExited(e);
		}
	}

}

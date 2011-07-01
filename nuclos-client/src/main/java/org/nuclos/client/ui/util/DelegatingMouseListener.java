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

package org.nuclos.client.ui.util;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;


public abstract class DelegatingMouseListener implements MouseInputListener {

	@Override
	public void mouseEntered(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseEntered(evt);
		}
	}

	@Override
	public void mouseMoved(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseMoved(evt);
		}
	}

	@Override
	public void mouseExited(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseExited(evt);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseClicked(evt);
		}
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mousePressed(evt);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseDragged(evt);
		}
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		MouseInputListener listener = getListener(evt);
		if (listener != null) {
			listener.mouseReleased(evt);
		}
	}

	protected abstract MouseInputListener getListener(MouseEvent evt);
}
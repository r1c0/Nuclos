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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventListener;

class WindowAdapter extends EventAdapter implements WindowListener {

	WindowAdapter(EventListener wrapped) {
		super(wrapped);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowOpened(e);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowClosing(e);
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowClosed(e);
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowIconified(e);
		}
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowDeiconified(e);
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowActivated(e);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		final WindowListener l = (WindowListener) wrapped.get();
		if (l != null) {
			l.windowDeactivated(e);
		}
	}

}

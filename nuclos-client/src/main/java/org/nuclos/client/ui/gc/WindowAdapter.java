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

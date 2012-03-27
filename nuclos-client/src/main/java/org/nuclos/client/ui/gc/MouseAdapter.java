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

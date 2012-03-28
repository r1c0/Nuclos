package org.nuclos.client.ui.gc;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;

class KeyAdapter extends EventAdapter implements KeyListener {

	KeyAdapter(EventListener wrapped) {
		super(wrapped);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		final KeyListener l = (KeyListener) wrapped.get();
		if (l != null) {
			l.keyTyped(e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		final KeyListener l = (KeyListener) wrapped.get();
		if (l != null) {
			l.keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		final KeyListener l = (KeyListener) wrapped.get();
		if (l != null) {
			l.keyReleased(e);
		}
	}

}

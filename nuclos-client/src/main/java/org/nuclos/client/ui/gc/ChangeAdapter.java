package org.nuclos.client.ui.gc;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ChangeAdapter extends EventAdapter implements ChangeListener {

	ChangeAdapter(ChangeListener wrapped) {
		super(wrapped);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		final ChangeListener l = (ChangeListener) wrapped.get();
		if (l != null) {
			l.stateChanged(e);
		}
	}

}

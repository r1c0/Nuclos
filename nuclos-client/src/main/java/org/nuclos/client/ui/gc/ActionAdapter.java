package org.nuclos.client.ui.gc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ActionAdapter extends EventAdapter implements ActionListener {
	
	ActionAdapter(ActionListener wrapped) {
		super(wrapped);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final ActionListener l = (ActionListener) wrapped.get();
		if (l != null) {
			l.actionPerformed(e);
		}
	}

}

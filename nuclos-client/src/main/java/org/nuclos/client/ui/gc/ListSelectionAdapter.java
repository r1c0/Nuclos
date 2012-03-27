package org.nuclos.client.ui.gc;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class ListSelectionAdapter extends EventAdapter implements ListSelectionListener {

	ListSelectionAdapter(ListSelectionListener wrapped) {
		super(wrapped);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		final ListSelectionListener l = (ListSelectionListener) wrapped.get();
		if (l != null) {
			l.valueChanged(e);
		}
	}

}

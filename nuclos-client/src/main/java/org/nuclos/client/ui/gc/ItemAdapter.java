package org.nuclos.client.ui.gc;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class ItemAdapter extends EventAdapter implements ItemListener {

	ItemAdapter(ItemListener wrapped) {
		super(wrapped);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		final ItemListener l = (ItemListener) wrapped.get();
		if (l != null) {
			l.itemStateChanged(e);
		}
	}

}

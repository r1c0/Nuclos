package org.nuclos.client.ui.gc;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DocumentAdapter extends EventAdapter implements DocumentListener {

	DocumentAdapter(DocumentListener wrapped) {
		super(wrapped);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.insertUpdate(e);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.removeUpdate(e);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		final DocumentListener l = (DocumentListener) wrapped.get();
		if (l != null) {
			l.changedUpdate(e);
		}
	}

}

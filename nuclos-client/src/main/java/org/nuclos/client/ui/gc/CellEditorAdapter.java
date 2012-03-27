package org.nuclos.client.ui.gc;

import java.awt.event.ActionListener;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

class CellEditorAdapter extends EventAdapter implements CellEditorListener {

	CellEditorAdapter(ActionListener wrapped) {
		super(wrapped);
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		final CellEditorListener l = (CellEditorListener) wrapped.get();
		if (l != null) {
			l.editingStopped(e);
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		final CellEditorListener l = (CellEditorListener) wrapped.get();
		if (l != null) {
			l.editingCanceled(e);
		}
	}

}

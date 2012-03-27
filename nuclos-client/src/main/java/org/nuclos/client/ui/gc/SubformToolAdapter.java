package org.nuclos.client.ui.gc;

import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;

class SubformToolAdapter extends EventAdapter implements SubFormToolListener {

	SubformToolAdapter(SubFormToolListener wrapped) {
		super(wrapped);
	}

	@Override
	public void toolbarAction(String actionCommand) {
		final SubFormToolListener l = (SubFormToolListener) wrapped.get();
		if (l != null) {
			l.toolbarAction(actionCommand);
		}
	}

}

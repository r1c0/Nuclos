package org.nuclos.client.ui.gc;

import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;

public class LookupAdapter extends EventAdapter implements LookupListener {

	LookupAdapter(LookupListener wrapped) {
		super(wrapped);
	}

	@Override
	public void lookupSuccessful(LookupEvent e) {
		final LookupListener l = (LookupListener) wrapped.get();
		if (l != null) {
			l.lookupSuccessful(e);
		}
	}

	@Override
	public int getPriority() {
		final LookupListener l = (LookupListener) wrapped.get();
		return l.getPriority();
	}

}

package org.nuclos.client.ui.gc;

import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.EventListener;

class EventAdapter implements EventListener {
	
	protected final WeakReference<EventListener> wrapped;

	EventAdapter(EventListener wrapped) {
		if (wrapped == null) {
			throw new NullPointerException();
		}
		this.wrapped = new WeakReference<EventListener>(wrapped, QueueSingleton.getInstance().getQueue());
	}
	
	Reference<EventListener> getReference() {
		return wrapped;
	}
	
}

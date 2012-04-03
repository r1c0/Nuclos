package org.nuclos.common;

import java.io.Serializable;

public class JMSOnceSync implements IJMSOnce {
	
	private IJMSOnce wrapped;
	
	public JMSOnceSync(IJMSOnce wrapped) {
		if (wrapped == null) {
			throw new NullPointerException();
		}
		this.wrapped = wrapped;
	}

	@Override
	public synchronized void queue(String topic, String text) {
		wrapped.queue(topic, text);
	}

	@Override
	public synchronized void queue(String topic, Serializable object) {
		wrapped.queue(topic, object);
	}

	@Override
	public synchronized void clear() {
		wrapped.clear();
	}

	@Override
	public synchronized void once() {
		wrapped.once();
	}

}

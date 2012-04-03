package org.nuclos.common;

import java.io.Serializable;

import org.nuclos.common2.Delayer;

public class JMSOnceSyncDelayed implements IJMSOnce {
	
	private final Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			wrapped.once();
		}
	};
	
	private IJMSOnce wrapped;
	
	public JMSOnceSyncDelayed(IJMSOnce wrapped) {
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
		Delayer.runOnlyOnce(300L, runnable);
	}

}

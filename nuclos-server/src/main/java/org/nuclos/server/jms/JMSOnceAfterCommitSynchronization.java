package org.nuclos.server.jms;

import java.io.Serializable;

import org.nuclos.common.IJMSOnce;
import org.springframework.transaction.support.TransactionSynchronization;

public class JMSOnceAfterCommitSynchronization implements TransactionSynchronization {
	
	private final IJMSOnce once;
	
	public JMSOnceAfterCommitSynchronization(IJMSOnce once) {
		if (once == null) {
			throw new NullPointerException();
		}
		this.once = once;
	}
	
	public void queue(String topic, String text) {
		once.queue(topic, text);
	}
	
	public void queue(String topic, Serializable object) {
		once.queue(topic, object);
	}
	
	//

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void beforeCommit(boolean readOnly) {
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCommit() {
		once.once();
	}

	@Override
	public void afterCompletion(int status) {
	}

}

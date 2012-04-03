package org.nuclos.server.jms;

import java.io.Serializable;

import org.springframework.transaction.support.TransactionSynchronization;

public class JMSSendOnceAfterCommitSynchronization implements TransactionSynchronization {
	
	private final JMSSendOnce sendOnce = new JMSSendOnce();
	
	public JMSSendOnceAfterCommitSynchronization() {
	}
	
	public void queue(String topic, String text) {
		sendOnce.queue(topic, text);
	}
	
	public void queue(String topic, Serializable object) {
		sendOnce.queue(topic, object);
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
		sendOnce.once();
	}

	@Override
	public void afterCompletion(int status) {
	}

}

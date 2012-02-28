//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.jms;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class MultiMessageListenerContainer extends SimpleMessageListenerContainer {
	
	private static final Logger LOG = Logger.getLogger(MultiMessageListenerContainer.class);
	
	private LinkedList<WeakReference<MessageListener>> lstMessageListener;
	
	public MultiMessageListenerContainer() {
	}
	
	public synchronized void addMessageListener(WeakReferenceMessageListener ml) {
		if (ml == null) {
			throw new NullPointerException();
		}
		if(lstMessageListener == null) {
			lstMessageListener = new LinkedList<WeakReference<MessageListener>>();
		}
		
		boolean add = true;
		for (WeakReference<MessageListener> ref : lstMessageListener) {
			final MessageListener registered = ref.get();
			if (registered != null) {
				if (ml.equals(registered)) {
					add = false;
					break;
				}
			}
		}
		if (add) {
			lstMessageListener.add(new WeakReference<MessageListener>(ml));
			LOG.info("addMessageListener " + ml + " to " + this);
		}
	}
	
	public synchronized void deleteMessageListener(WeakReferenceMessageListener ml) {
		for (Iterator<WeakReference<MessageListener>> it = lstMessageListener.iterator(); it.hasNext(); ) {
			final WeakReference<MessageListener> wr = it.next();
			final MessageListener l = wr.get();
			if (l == ml || l == null) {
				it.remove();
				LOG.info("deleteMessageListener " + ml + " from " + this);
			}
		}
		LOG.info("deleteMessageListener ended");
	}
	
	/**
	 * Invoke the specified listener: either as standard JMS MessageListener
	 * or (preferably) as Spring SessionAwareMessageListener.
	 * <p>
	 * TODO: Maybe it is better to invoke the 'real' listeners thread (i.e. with
	 * a thread pool)? (tp)
	 * </p>
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageListener
	 */
	@Override
	protected void invokeListener(Session session, Message message) throws JMSException {
		if (lstMessageListener != null) {
			final LinkedList<WeakReference<MessageListener>> listeners;
			synchronized(this) {
				listeners = (LinkedList<WeakReference<MessageListener>>) lstMessageListener.clone();
			}
			for (WeakReference<MessageListener> wr : listeners) {
				final MessageListener listener = wr.get();
				if (listener instanceof SessionAwareMessageListener) {
					doInvokeListener((SessionAwareMessageListener) listener, session, message);
					LOG.info("doInvokeListener(" + listener + ", " + session + ", " + message + ")");
				} else if (listener instanceof MessageListener) {
					doInvokeListener((MessageListener) listener, message);
					LOG.info("doInvokeListener(" + listener + ", " + message + ")");
				} else if (listener != null) {
					throw new IllegalArgumentException(
							"Only MessageListener and SessionAwareMessageListener supported: " + listener);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("MultiMessageListenerContainer[");
		for (WeakReference<MessageListener> wr : lstMessageListener) {
			final MessageListener ml = wr.get();
			if (ml != null) {
				result.append(ml).append(", ");
			}
		}
		result.append("super=").append(super.toString());
		result.append("]");
		return result.toString();
	}

}

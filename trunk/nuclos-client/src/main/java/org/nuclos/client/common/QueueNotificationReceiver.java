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
package org.nuclos.client.common;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.naming.Context;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Receives queue based JMS messages in the client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */

public class QueueNotificationReceiver {

	private static final Logger LOG = Logger.getLogger(QueueNotificationReceiver.class);
	
	private static Context ctx;
	private static QueueConnection queueconnection;

	/**
	 * list that holds all registered listeners for this queue receiver
	 */
	private static List<WeakReferenceMessageListener> weakmessagelistener = new LinkedList<WeakReferenceMessageListener>();

	/**
	 * subscribes to the JMS queue
	 * @param queuename
	 * @param messagelistener
	 */
	public static void subscribe(String queuename, MessageListener messagelistener) {
		WeakReferenceMessageListener weakrefmsglistener = new WeakReferenceMessageListener(queuename, messagelistener);
		weakrefmsglistener.subscribe();
		weakmessagelistener.add(weakrefmsglistener);
	}

	/**
	 * unsubscribes all registered JMS queues
	 */
	private static void unsubscribeAll() {
		List<WeakReferenceMessageListener> tmp = new LinkedList<WeakReferenceMessageListener>(weakmessagelistener);
		for (WeakReferenceMessageListener ref : tmp) {
			unsubscribe(ref.getReference().get());
		}
	}

	/**
	 * unsubscribes the given <code>MessageListener<code> from the queue receiver
	 * @param messagelistener
	 */
	public static void unsubscribe(MessageListener messagelistener) {
		List<WeakReferenceMessageListener> tmp = new LinkedList<WeakReferenceMessageListener>(weakmessagelistener);
		for (WeakReferenceMessageListener ref : tmp) {
			if (ref.getReference().get() == messagelistener) {
				ref.unsubscribe();
				weakmessagelistener.remove(ref);
				ref = null;
				break;
			}
		}
	}

	/**
	 * holds a weak reference of a <code>MessageListener</code> with the corresponding
	 * <code>QueueSession</code> and <code>QueueSubscriber</code> which are closed, when the reference
	 * does not exists any more
	 */
	private static class WeakReferenceMessageListener implements MessageListener {
		private String queuename;
		private WeakReference<MessageListener> reference;
		private QueueSession queuesession;
		private QueueReceiver queuereceiver;

		public WeakReferenceMessageListener(String queuename, MessageListener delegate) {
			this.queuename = queuename;
			this.reference = new WeakReference<MessageListener>(delegate);
		}

		public WeakReference<MessageListener> getReference() {
			return this.reference;
		}

		/**
		 * subscribes this JMS queue
		 */
		public void subscribe() {
			try {
				SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) SpringApplicationContextHolder.getBean(queuename);
				container.setMessageListener(this);
			}
			catch (Exception ex) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("TopicNotificationReceiver.4", "Der JMS-Abonnent f\u00fcr die Queue {0} konnte nicht registriert werden.", queuename), ex);
			}
		}

		/**
		 * unsubscribes this JMS queue
		 */
		public void unsubscribe() {
			try {
				if (queuereceiver != null) {
					queuereceiver.close();
					queuereceiver = null;
				}
				if (queuesession != null) {
					queuesession.close();
					queuesession = null;
				}
			}
			catch(JMSException e) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("QueueNotificationReceiver.3", "Der JMS-Abonnent f\u00fcr die Queue {0} konnte nicht registriert werden.", queuename), e);
			}
		}

		@Override
		public void onMessage(Message msg) {
			MessageListener delegate = reference.get();
			if(delegate != null) {
				delegate.onMessage(msg);
			}
			else {
				unsubscribe();
			}
		}
	}
}	// class QueueNotificationReceiver

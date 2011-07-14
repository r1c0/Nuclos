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
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;

import org.nuclos.client.jms.MultiMessageListenerContainer;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Receives topic based JMS messages in the client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class TopicNotificationReceiver {

	private static Context ctx;
	private static TopicConnection topicconn;

	/**
	 * list that holds all registered listeners for this topic receiver
	 */
	private static List<WeakReferenceMessageListener> weakmessagelistener = new LinkedList<WeakReferenceMessageListener>();

	static {
		try {
			final ConnectionFactory connectionFactory = (ConnectionFactory) SpringApplicationContextHolder.getBean("jmsFactory");
			topicconn = (TopicConnection) connectionFactory.createConnection();


			ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_JMS_TOPICS,
				new Thread() {
				@Override
				public void run() {
					TopicNotificationReceiver.unsubscribeAll();
					try {
						topicconn.close();
						topicconn = null;
					}
					catch(JMSException e) {
						throw new NuclosFatalException(CommonLocaleDelegate.getMessage("TopicNotificationReceiver.2", "Die JMS-Topic-Verbindung konnte nicht abgebaut werden."), e);
					}
				}
			});

			topicconn.start();
		}
		catch (JMSException e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("TopicNotificationReceiver.3", "Die JMS-Topic-Verbindung konnte nicht aufgebaut werden."), e);
		}
	}

	/**
	 * subscribes to the JMS topic with a JMS selector
	 * @param sTopicName
	 * @param messageSelector
	 * @param messagelistener
	 */
	public static void subscribe(String sTopicName, String correlationId, MessageListener messagelistener) {
		WeakReferenceMessageListener weakrefmsglistener = new WeakReferenceMessageListener(sTopicName, correlationId, messagelistener);
		weakrefmsglistener.subscribe();
		weakmessagelistener.add(weakrefmsglistener);
	}

	/**
	 * subscribes to the JMS topic
	 * @param sTopicName
	 * @param messagelistener
	 */
	public static void subscribe(String sTopicName, MessageListener messagelistener) {
		subscribe(sTopicName, null, messagelistener);
	}

	/**
	 * unsubscribes all registered JMS topics
	 */
	private static void unsubscribeAll() {
		List<WeakReferenceMessageListener> tmp = new LinkedList<WeakReferenceMessageListener>(weakmessagelistener);
		for (WeakReferenceMessageListener ref : tmp) {
			unsubscribe(ref.getReference().get());
		}
	}

	/**
	 * unsubscribes the given <code>MessageListener<code> from the topic receiver
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
	 * <code>TopicSession</code> and <code>TopicSubscriber</code> which are closed, when the reference
	 * does not exists any more
	 */
	private static class WeakReferenceMessageListener implements MessageListener {
		private String topicname;
		private String correlationId;
		private WeakReference<MessageListener> reference;
		private TopicSession topicsession;
		private TopicSubscriber topicsubscriber;

		public WeakReferenceMessageListener(String topicname, String correlationId, MessageListener delegate) {
			this.topicname = topicname;
			this.correlationId = correlationId;
			this.reference = new WeakReference<MessageListener>(delegate);
		}

		public WeakReference<MessageListener> getReference() {
			return this.reference;
		}

		/**
		 * subscribes this JMS topic
		 */
		public void subscribe() {
			try {
				Object bean = SpringApplicationContextHolder.getBean(topicname);
				if (bean instanceof SimpleMessageListenerContainer) {
					SimpleMessageListenerContainer container = (SimpleMessageListenerContainer)bean;

					if(container instanceof MultiMessageListenerContainer) {
						MultiMessageListenerContainer multiContainer = (MultiMessageListenerContainer)container;
						multiContainer.addMessageListener(this);
					}
					else {
						container.setMessageListener(this);
					}
				}
				else if (bean instanceof Topic){
					Topic topic = (Topic) bean;
					this.topicsession = topicconn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
					String idSelector = MessageFormat.format("JMSCorrelationID = ''{0}''", correlationId);
					this.topicsubscriber = topicsession.createSubscriber(topic, idSelector, false);
					this.topicsubscriber.setMessageListener(this);
				}
				else {
					throw new NuclosFatalException("Invalid bean class:" + bean.getClass().getName());
				}
			}
			catch (Exception ex) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("TopicNotificationReceiver.4", "Der JMS-Abonnent f\u00fcr das Topic {0} konnte nicht registriert werden.", topicname), ex);
			}
		}

		/**
		 * unsubscribes this JMS topic
		 */
		public void unsubscribe() {
			try {
				if (this.topicsubscriber != null) {
					if (topicsubscriber != null) {
						topicsubscriber.close();
						topicsubscriber = null;
					}
					if (topicsession != null) {
						topicsession.close();
						topicsession = null;
					}
				}
				else {
					SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) SpringApplicationContextHolder.getBean(topicname);
					container.shutdown();
				}
			}
			catch(Exception e) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("TopicNotificationReceiver.4", "Der JMS-Abonnent f\u00fcr das Topic {0} konnte nicht registriert werden.", topicname), e);
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
}	// class TopicNotificationReceiver
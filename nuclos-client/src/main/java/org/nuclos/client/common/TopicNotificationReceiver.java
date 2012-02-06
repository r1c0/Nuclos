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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.log4j.Logger;
import org.nuclos.client.jms.MultiMessageListenerContainer;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Receives topic based JMS messages in the client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Component
public class TopicNotificationReceiver {

	private static final Logger LOG = Logger.getLogger(TopicNotificationReceiver.class);
	
	private static TopicNotificationReceiver INSTANCE;
	
	private static final DummyMessageListener dummyListener = new DummyMessageListener();

	private volatile boolean deferredSubscribe = true;

	private TopicConnection topicconn;
	
	private List<TopicInfo> infos = new ArrayList<TopicInfo>();
	
	// private CommonLocaleDelegate cld;
	
	private ConnectionFactory jmsFactory;

	/**
	 * list that holds all registered listeners for this topic receiver
	 */
	private List<WeakReferenceMessageListener> weakmessagelistener = new LinkedList<WeakReferenceMessageListener>();

	TopicNotificationReceiver() {
	}
	
	@PostConstruct
	void init() {
		try {
			// final ConnectionFactory connectionFactory = (ConnectionFactory) SpringApplicationContextHolder.getBean("jmsFactory");
			topicconn = (TopicConnection) jmsFactory.createConnection();


			ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_JMS_TOPICS,
				new Thread() {
				@Override
				public void run() {
					unsubscribeAll();
					try {
						topicconn.close();
						topicconn = null;
					}
					catch(JMSException e) {
						throw new NuclosFatalException("Can't shutdown JMS connection", e);
						/*
						throw new NuclosFatalException(cld.getMessage(
								"TopicNotificationReceiver.2", "Die JMS-Topic-Verbindung konnte nicht abgebaut werden."), e);
						 */
					}
				}
			});

			topicconn.start();
			
			subscribe(JMSConstants.TOPICNAME_HEARTBEAT, heartBeatListener);
		}
		catch (JMSException e) {
			throw new NuclosFatalException("Can't establish JMS connection", e);
			/*
			throw new NuclosFatalException(cld.getMessage(
					"TopicNotificationReceiver.3", "Die JMS-Topic-Verbindung konnte nicht aufgebaut werden."), e);
			 */
		}
		INSTANCE = this;
	}
	
	@Autowired
	void setConnectionFactory(ConnectionFactory jmsFactory) {
		this.jmsFactory = jmsFactory;
	}
	
	// @Autowired
	void setCommonLocaleDelegate(CommonLocaleDelegate cld) {
		// this.cld = cld;
	}
	
	public static TopicNotificationReceiver getInstance() {
		return INSTANCE;
	}

	/**
	 * subscribes to the JMS topic with a JMS selector
	 * @param sTopicName
	 * @param messageSelector
	 * @param messagelistener
	 */
	public void subscribe(String sTopicName, String correlationId, MessageListener messagelistener) {
		if (sTopicName == null) {
			throw new NullPointerException("No topic name");
		}
		if (messagelistener == null) {
			throw new NullPointerException("No MessageListener");
		}
		infos.add(new TopicInfo(sTopicName, correlationId, messagelistener));
		if (!deferredSubscribe) {
			realSubscribe();
		}
	}
	
	public synchronized void realSubscribe() {
		deferredSubscribe = false;
		for (TopicInfo i: infos) {
			WeakReferenceMessageListener weakrefmsglistener = new WeakReferenceMessageListener(i);
			weakrefmsglistener.subscribe();
			weakmessagelistener.add(weakrefmsglistener);
		}
		infos.clear();
	}

	/**
	 * subscribes to the JMS topic
	 * @param sTopicName
	 * @param messagelistener
	 */
	public void subscribe(String sTopicName, MessageListener messagelistener) {
		subscribe(sTopicName, null, messagelistener);
	}

	/**
	 * unsubscribes all registered JMS topics
	 */
	private void unsubscribeAll() {
		List<WeakReferenceMessageListener> tmp = new LinkedList<WeakReferenceMessageListener>(weakmessagelistener);
		for (WeakReferenceMessageListener ref : tmp) {
			unsubscribe(ref.getReference().get());
		}
	}

	/**
	 * unsubscribes the given <code>MessageListener<code> from the topic receiver
	 * @param messagelistener
	 */
	public void unsubscribe(MessageListener messagelistener) {
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
	// @Configurable
	private static class WeakReferenceMessageListener implements MessageListener {
		
		private String topicname;
		private String correlationId;
		private WeakReference<MessageListener> reference;
		private TopicSession topicsession;
		private TopicSubscriber topicsubscriber;
		
		// private CommonLocaleDelegate cld;

		public WeakReferenceMessageListener(TopicInfo info) {
			this.topicname = info.getTopic();
			this.correlationId = info.getCorrelationId();
			this.reference = new WeakReference<MessageListener>(info.getMessageListener());
		}
		
		// @Autowired
		void setCommonLocaleDelegate(CommonLocaleDelegate cld) {
			// this.cld = cld;
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
					if (!container.isActive()) {
						throw new NuclosFatalException(container + " is not active, you can't subscribe");
					}
					
					if(container instanceof MultiMessageListenerContainer) {
						MultiMessageListenerContainer multiContainer = (MultiMessageListenerContainer)container;
						multiContainer.addMessageListener(this);
						LOG.info("Subscribe " + this + " to MultiMessageListenerContainer");
					}
					else {
						if (container.getMessageListener() != null  && container.getMessageListener() != dummyListener) {
							throw new NuclosFatalException("On " + container + " the MessageListener has already been set: " + container.getMessageListener());
						}
						container.setMessageListener(this);
						LOG.info("Subscribe " + this + " to SimpleMessageListenerContainer");
					}
				}
				else if (bean instanceof Topic) {
					Topic topic = (Topic) bean;
					this.topicsession = INSTANCE.topicconn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
					String idSelector = MessageFormat.format("JMSCorrelationID = ''{0}''", correlationId);
					this.topicsubscriber = topicsession.createSubscriber(topic, idSelector, false);
					this.topicsubscriber.setMessageListener(this);
					LOG.info("Subscribe " + this + " to Topic");
				}
				else {
					throw new NuclosFatalException("Invalid bean class:" + bean.getClass().getName());
				}
			}
			catch (Exception ex) {
				throw new NuclosFatalException("Can't subscribe to JMS topic " + topicname, ex);
				/*
				throw new NuclosFatalException(cld.getMessage(
						"TopicNotificationReceiver.4", "Der JMS-Abonnent f\u00fcr das Topic {0} konnte nicht registriert werden.", topicname), ex);
				 */
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
					LOG.info("Unsubscribe " + this + " from Topic");
				}
				else {
					SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) SpringApplicationContextHolder.getBean(topicname);
					// container.shutdown();
					if (container instanceof MultiMessageListenerContainer) {
						final MessageListener ml = getReference().get();
						if (ml != null) {
							MultiMessageListenerContainer multiContainer = (MultiMessageListenerContainer)container;
							multiContainer.deleteMessageListener(ml);
							LOG.info("Unsubscribe " + this + " from MultiMessageListenerContainer");
						}
					}
					else {
						// not allowed from Spring, throws IllegalArgumentException
						// container.setMessageListener(null);
						container.setMessageListener(dummyListener);
						LOG.info("Unsubscribe " + this + " from SimpleMessageListenerContainer " + container);
					}
				}
			}
			catch(Exception e) {
				throw new NuclosFatalException("Can't unsubscribe to JMS topic " + topicname, e);
				/*
				throw new NuclosFatalException(cld.getMessage(
						"TopicNotificationReceiver.4", "Der JMS-Abonnent f\u00fcr das Topic {0} konnte nicht registriert werden.", topicname), e);
				 */
			}
		}

		@Override
		public void onMessage(Message msg) {
			MessageListener delegate = reference.get();
			LOG.info(MessageFormat.format("Received message from topic {0}: {1}\n\tfor {2}", topicname, msg, delegate));
			if(delegate != null) {
				delegate.onMessage(msg);
			}
			else {
				unsubscribe();
			}
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append("WeakRefMsgListener[");
			result.append("topic=").append(topicname);
			final MessageListener ml = getReference().get();
			if (ml != null) {
				result.append(",delegate=").append(ml);
			}
			if (topicsession != null) {
				result.append(",session=").append(topicsession);
			}
			if (topicsubscriber != null) {
				result.append(",subscriber=").append(topicsubscriber);
			}
			if (correlationId != null) {
				result.append(",corrId=").append(correlationId);
			}
			result.append("]");
			return result.toString();
		}
	}
	
	private static class TopicInfo {
		
		private final String sTopicName;
		private final String correlationId; 
		private MessageListener messagelistener;
		
		private TopicInfo(String sTopicName, String correlationId, MessageListener messagelistener) {
			this.sTopicName = sTopicName;
			this.correlationId = correlationId;
			this.messagelistener = messagelistener;
		}
		
		public String getTopic() {
			return sTopicName;
		}
		
		public String getCorrelationId() {
			return correlationId;
		}
		
		public MessageListener getMessageListener() {
			return messagelistener;
		}
		
	}
	
	private static class HeartBeatMessageListener implements MessageListener {
		
		public HeartBeatMessageListener() {
		}

		@Override
		public void onMessage(Message message) {
			LOG.info("Received heartbeat message " + message);
		}		
		
	}
	
	private static class DummyMessageListener implements MessageListener {
		
		public DummyMessageListener() {
		}

		@Override
		public void onMessage(Message message) {
			LOG.info("Dummy listener received and ignores message " + message);
		}		
		
	}
	
}	// class TopicNotificationReceiver

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TopicConnection;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ShutdownActions;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
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
// @Component
public class TopicNotificationReceiver {

	private static final Logger LOG = Logger.getLogger(TopicNotificationReceiver.class);
	
	/**
	 * Subscribed to TOPICNAME_HEARTBEAT.
	 * 
	 * @see #init()
	 */
	private static final HeartBeatMessageListener heartBeatListener = new HeartBeatMessageListener();
	
	static final DummyMessageListener dummyListener = new DummyMessageListener();

	private static TopicNotificationReceiver INSTANCE;
	
	//
	
	private boolean deferredSubscribe = true;

	private TopicConnection topicconn;
	
	private List<TopicInfo> infos = new ArrayList<TopicInfo>();
	
	// private SpringLocaleDelegate cld;
	
	private ConnectionFactory jmsFactory;

	/**
	 * list that holds all registered listeners for this topic receiver
	 */
	private List<WeakReferenceMessageListener> weakmessagelistener = new LinkedList<WeakReferenceMessageListener>();

	TopicNotificationReceiver() {
		INSTANCE = this;
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
	}
	
	// @Autowired
	public final void setConnectionFactory(ConnectionFactory jmsFactory) {
		this.jmsFactory = jmsFactory;
	}
	
	// @Autowired
	public final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		// this.cld = cld;
	}
	
	public static TopicNotificationReceiver getInstance() {
		return INSTANCE;
	}
	
	public TopicConnection getTopicConnection() {
		return topicconn;
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
		synchronized (this) {
			infos.add(new TopicInfo(sTopicName, correlationId, messagelistener));
			if (!deferredSubscribe) {
				realSubscribe();
			}
		}
	}
	
	public synchronized final void realSubscribe() {
		if (infos.isEmpty()) return;
		
		final List<TopicInfo> copy = new ArrayList<TopicInfo>(infos);
		infos.clear();
		assert deferredSubscribe || infos.isEmpty();
		final Runnable run = new Runnable() {

			@Override
			public void run() {
				for (TopicInfo i : copy) {
					WeakReferenceMessageListener weakrefmsglistener = new WeakReferenceMessageListener(i);
					weakrefmsglistener.subscribe();
					weakmessagelistener.add(weakrefmsglistener);
				}
				synchronized (TopicNotificationReceiver.this) {
					deferredSubscribe = false;
				}
			}
		};
		new Thread(run, "TopicNotificationReceiver.realSubscribe").start();
		assert deferredSubscribe || infos.isEmpty();
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

	private static class HeartBeatMessageListener implements MessageListener {
		
		public HeartBeatMessageListener() {
		}

		@Override
		public void onMessage(Message message) {
			LOG.info("onMessage: Received heartbeat message " + message);
		}		
		
	}
	
	private static class DummyMessageListener implements MessageListener {
		
		public DummyMessageListener() {
		}

		@Override
		public void onMessage(Message message) {
			LOG.info("onMessage: Dummy listener received and ignores message " + message);
		}		
		
	}
	
}	// class TopicNotificationReceiver

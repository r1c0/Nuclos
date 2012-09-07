//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.SpringApplicationSubContextsHolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * holds a weak reference of a <code>MessageListener</code> with the corresponding
 * <code>TopicSession</code> and <code>TopicSubscriber</code> which are closed, when the reference
 * does not exists any more
 */
class WeakReferenceMessageListener implements MessageListener {
	
	private static final Logger LOG = Logger.getLogger(WeakReferenceMessageListener.class);
	
	private final Map<String,Object> topic2Bean = new ConcurrentHashMap<String, Object>();
	
	private String topicname;
	private String correlationId;
	private WeakReference<MessageListener> reference;
	private TopicSession topicsession;
	private TopicSubscriber topicsubscriber;
	
	// former Spring injection
	
	private TopicConnection topicConnection;
	
	// end of former Spring injection

	public WeakReferenceMessageListener(TopicInfo info, ClassPathXmlApplicationContext startupContext) {
		this.topicname = info.getTopic();
		this.correlationId = info.getCorrelationId();
		this.reference = new WeakReference<MessageListener>(info.getMessageListener());
		
		setTopicConnection(startupContext.getBean(TopicNotificationReceiver.class).getTopicConnection());
	}
	
	final void setTopicConnection(TopicConnection topicConnection) {
		this.topicConnection = topicConnection;
	}
	
	final TopicConnection getTopicConnection() {
		return topicConnection;
	}
	
	public WeakReference<MessageListener> getReference() {
		return this.reference;
	}

	/**
	 * subscribes this JMS topic
	 */
	public void subscribe() {
		try {
			final Object bean = SpringApplicationSubContextsHolder.getInstance().searchBean(topicname);
			topic2Bean.put(topicname, bean);
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
					if (container.getMessageListener() != null  && container.getMessageListener() != TopicNotificationReceiver.dummyListener) {
						throw new NuclosFatalException("On " + container + " the MessageListener has already been set: " + container.getMessageListener());
					}
					container.setMessageListener(this);
					LOG.info("Subscribe " + this + " to SimpleMessageListenerContainer");
				}
			}
			else if (bean instanceof Topic) {
				Topic topic = (Topic) bean;
				this.topicsession = getTopicConnection().createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
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
			LOG.error("Can't subscribe to JMS topic " + topicname, ex);
			// throw new NuclosFatalException("Can't subscribe to JMS topic " + topicname, ex);
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
				// SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) SpringApplicationContextHolder.getBean(topicname);
				final SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) topic2Bean.get(topicname);
				// container.shutdown();
				if (container instanceof MultiMessageListenerContainer) {
					/*
					final MessageListener ml = getReference().get();
					if (ml != null) {
						MultiMessageListenerContainer multiContainer = (MultiMessageListenerContainer)container;
						multiContainer.deleteMessageListener(ml);
						LOG.info("Unsubscribe " + this + " from MultiMessageListenerContainer");
					}
					 */
					final MultiMessageListenerContainer multiContainer = (MultiMessageListenerContainer) container;
					multiContainer.deleteMessageListener(this);				
					LOG.info("Unsubscribe " + this + " from MultiMessageListenerContainer");
				}
				else {
					// not allowed from Spring, throws IllegalArgumentException
					// container.setMessageListener(null);
					container.setMessageListener(TopicNotificationReceiver.dummyListener);
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
		LOG.info(MessageFormat.format("onMessage: Received message from topic {0}: {1}\n\tfor {2}", topicname, msg, delegate));
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


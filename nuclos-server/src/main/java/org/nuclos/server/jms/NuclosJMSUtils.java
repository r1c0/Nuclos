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
package org.nuclos.server.jms;

import java.io.Serializable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class NuclosJMSUtils {
	
	private static final Logger LOG = Logger.getLogger(NuclosJMSUtils.class);
	
	public static void sendMessageAfterCommit(final String sMessageText, String topic) {
		sendMessageAfterCommit(sMessageText, topic, null);
	}
	
	public static void sendMessage(final String sMessageText, String topic) {
		sendMessage(sMessageText, topic, null);
	}
	
	public static void sendMessageAfterCommit(final String sMessageText, final String topic, final String sReceiver) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			
			@Override
			public void afterCommit() {
				LOG.info("afterCommit: " + this + " JMS send: topic=" + topic + " receiver=" + sReceiver + ": " + sMessageText);
				sendMessage(sMessageText, topic, sReceiver);
			}
			
		});
	}
	
	public static void sendMessage(final String sMessageText, final String topic, final String sReceiver) {
		try {
			final JmsTemplate jmsTemplate = getTemplate(topic);
	    	jmsTemplate.send(new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					Message message = session.createTextMessage(sMessageText);
					if(sReceiver != null)
						message.setJMSCorrelationID(sReceiver);
					logSendMessage(topic, sMessageText, message);
					return message;
				}
			});
    	}
    	catch(Exception ex) {
    		throw new NuclosFatalException(ex);
    	}
	}
	
	public static void sendObjectMessageAfterCommit(final Serializable object, final String topic, final String sReceiver) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			
			@Override
			public void afterCommit() {
				LOG.info("afterCommit: " + this + " JMS object send: topic=" + topic + " receiver=" + sReceiver + ": " + object);
				sendObjectMessage(object, topic, sReceiver);
			}
			
		});		
	}
	
	/**
	 * Commit or Rollback
	 * @param object
	 * @param topic
	 * @param sReceiver
	 */
	public static void sendObjectMessageAfterCompletion(final Serializable object, final String topic, final String sReceiver) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCompletion(int status) {
				LOG.info("afterCompletion: " + this + " JMS object send: topic=" + topic + " receiver=" + sReceiver + ": " + object);
				sendObjectMessage(object, topic, sReceiver);
			}
		});		
	}
	
	public static void sendObjectMessage(final Serializable object, final String topic, final String sReceiver) {
		try {
			final JmsTemplate jmsTemplate = getTemplate(topic);
	    	jmsTemplate.send(new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					Message message = session.createObjectMessage(object);
					if(sReceiver != null)
						message.setJMSCorrelationID(sReceiver);
					logSendMessage(topic, object, message);
					return message;
				}
			});
    	}
    	catch(Exception ex) {
    		throw new NuclosFatalException(ex);
    	}
	}
	
	public static void sendOnceAfterCommitDelayed(String text, String topic) {
		final JMSOnceAfterCommitSynchronization ts = getJMSOnceAfterCommitSynchronization();
		ts.queue(topic, text);
	}
	
	public static void sendOnceAfterCommitDelayed(Serializable object, String topic) {
		final JMSOnceAfterCommitSynchronization ts = getJMSOnceAfterCommitSynchronization();
		ts.queue(topic, object);
	}
	
	private static JMSOnceAfterCommitSynchronization getJMSOnceAfterCommitSynchronization() {
		final List<TransactionSynchronization> txSyncs = TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization ts: txSyncs) {
			if (ts instanceof JMSOnceAfterCommitSynchronization) {
				return (JMSOnceAfterCommitSynchronization) ts;
			}
		}
		final JMSOnceAfterCommitSynchronization tsNew = new JMSOnceAfterCommitSynchronization(new JMSSendToGlobalQueue());
		TransactionSynchronizationManager.registerSynchronization(tsNew);
		return tsNew;
	}
	
	private static JmsTemplate getTemplate(String topic) {
		final ApplicationContext context = SpringApplicationContextHolder.getApplicationContext();
    	final JmsTemplate jmsTemplate = (JmsTemplate)context.getBean(topic);
    	return jmsTemplate;
	}
	
	private static void logSendMessage(String topic, Object body, Message msg) throws JMSException {
		LOG.info("JMS send to topic '" + topic + "' msg=" + body + " details=" + toString(msg));
	}
	
	private static String toString(Message msg) throws JMSException {
		final StringBuilder result = new StringBuilder();
		result.append("Message[");
		result.append("type=").append(msg.getJMSType());
		result.append(",corrId=").append(msg.getJMSCorrelationID());
		result.append(",msgId=").append(msg.getJMSMessageID());
		result.append("]");
		return result.toString();
	}
	
}

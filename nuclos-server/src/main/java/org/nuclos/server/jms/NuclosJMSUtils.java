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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class NuclosJMSUtils {
	
	public static void sendMessage(final String sMessageText, String topic) {
		NuclosJMSUtils.sendMessage(sMessageText, topic, null);
	}
	
	public static void sendMessage(final String sMessageText, String topic, final String sReceiver) {
		try {
    		ApplicationContext context = SpringApplicationContextHolder.getApplicationContext();

	    	JmsTemplate jmsTemplate = (JmsTemplate)context.getBean(topic);

	    	jmsTemplate.send(new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					Message message = session.createTextMessage(sMessageText);
					if(sReceiver != null)
						message.setJMSCorrelationID(sReceiver);
					return message;
				}
			});
    	}
    	catch(Exception ex) {
    		throw new NuclosFatalException(ex);
    	}
	}
	
	public static void sendObjectMessage(final Serializable object, String topic, final String sReceiver) {
		try {
    		ApplicationContext context = SpringApplicationContextHolder.getApplicationContext();
	    	JmsTemplate jmsTemplate = (JmsTemplate)context.getBean(topic);
	    	jmsTemplate.send(new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					Message message = session.createObjectMessage(object);
					if(sReceiver != null)
						message.setJMSCorrelationID(sReceiver);
					return message;
				}
			});
    	}
    	catch(Exception ex) {
    		throw new NuclosFatalException(ex);
    	}
	}
	
}

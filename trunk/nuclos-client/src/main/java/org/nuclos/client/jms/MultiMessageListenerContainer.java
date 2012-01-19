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
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class MultiMessageListenerContainer extends SimpleMessageListenerContainer {
	
	List<MessageListener> lstMessageListener;
	
	
	public void addMessageListener(MessageListener ml) {
		if(lstMessageListener == null)
			lstMessageListener = new ArrayList<MessageListener>();
		
		lstMessageListener.add(ml);
	}
	
	/**
	 * Invoke the specified listener: either as standard JMS MessageListener
	 * or (preferably) as Spring SessionAwareMessageListener.
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageListener
	 */
	@Override
	protected void invokeListener(Session session, Message message) throws JMSException {
		if(lstMessageListener == null) 
			lstMessageListener = new ArrayList<MessageListener>();
		for(Object listener : new ArrayList<MessageListener>(lstMessageListener)) { // new list here, otherwise java.util.ConcurrentModificationException
			if (listener instanceof SessionAwareMessageListener) {
				doInvokeListener((SessionAwareMessageListener) listener, session, message);
			}
			else if (listener instanceof MessageListener) {
				doInvokeListener((MessageListener) listener, message);
			}
			else if (listener != null) {
				throw new IllegalArgumentException(
						"Only MessageListener and SessionAwareMessageListener supported: " + listener);
			}
			else {
				throw new IllegalStateException("No message listener specified - see property 'messageListener'");
			}
		}
	}

}

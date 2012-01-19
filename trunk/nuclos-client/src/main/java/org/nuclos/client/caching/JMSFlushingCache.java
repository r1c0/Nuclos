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
/*
 * Created on 31.08.2009
 */
package org.nuclos.client.caching;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.common.caching.GenCache;


public class JMSFlushingCache<K, V> extends GenCache<K, V> implements MessageListener {
	
	private static final Logger LOG = Logger.getLogger(JMSFlushingCache.class);
	
	private String pat;

	public JMSFlushingCache(String topic, String messagePattern, LookupProvider<K, V> lookupProvider) {
		super(lookupProvider);
		pat = messagePattern;
		TopicNotificationReceiver.subscribe(topic, this);
	}

	@Override
	public void onMessage(Message m) {
		if(m instanceof TextMessage) {
			String t;
			try {
				t = ((TextMessage) m).getText();
				if(t != null && t.matches(pat))
					clear();
			}
			catch(JMSException e) {
				LOG.warn("onMessage failed: " + e, e);
			}
		}
	}
}

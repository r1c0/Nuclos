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

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.caching.GenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @deprecated Only one usage in nuclos. Get rid of it!
 */
@Configurable
public class JMSFlushingCache<K, V> extends GenCache<K, V> implements MessageListener {
	
	private static final Logger LOG = Logger.getLogger(JMSFlushingCache.class);
	
	private String topic;
	
	private TopicNotificationReceiver tnr;

	public JMSFlushingCache(String topic, LookupProvider<K, V> lookupProvider) {
		super(lookupProvider);
		this.topic = topic;
	}
	
	@PostConstruct
	void init() {
		tnr.subscribe(topic, this);
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}

	@Override
	public void onMessage(Message m) {
		if(m instanceof TextMessage) {
			String t;
			try {
				t = ((TextMessage) m).getText();
				LOG.info("onMessage " + this + " pattern matches, clear cache...");
				clear();
			}
			catch(JMSException e) {
				LOG.warn("onMessage failed: " + e, e);
			}
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("JMSFlushingCache[");
		result.append(",topic=").append(topic);
		result.append(",receiver=").append(tnr);
		result.append(",super=").append(super.toString());
		result.append("]");
		return result.toString();
	}
}

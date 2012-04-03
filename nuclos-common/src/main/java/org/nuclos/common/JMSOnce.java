package org.nuclos.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class JMSOnce implements IJMSOnce {
	
	private static final Logger LOG = Logger.getLogger(JMSOnce.class);
	
	//
	
	protected final Map<String,Set<String>> topic2TextMessage 
		= new HashMap<String, Set<String>>();
	
	protected final Map<String,Set<Serializable>> topic2ObjectMessage 
		= new HashMap<String, Set<Serializable>>();
	
	protected JMSOnce() {	
	}
	
	@Override
	public void queue(String topic, String text) {
		Set<String> set = topic2TextMessage.get(topic);
		if (set == null) {
			set = new HashSet<String>();
			topic2TextMessage.put(topic, set);
		}
		final boolean modified = set.add(text);
		LOG.info("queued to topic " + topic + ": '" + text + "': " + modified);
	}
	
	@Override
	public void queue(String topic, Serializable object) {
		Set<Serializable> set = topic2ObjectMessage.get(topic);
		if (set == null) {
			set = new HashSet<Serializable>();
			topic2ObjectMessage.put(topic, set);
		}
		final boolean modified = set.add(object);
		LOG.info("queued to topic " + topic + ": '" + object + "': " + modified);
	}
	
	@Override
	public void clear() {
		topic2ObjectMessage.clear();
		topic2TextMessage.clear();
	}
	
}

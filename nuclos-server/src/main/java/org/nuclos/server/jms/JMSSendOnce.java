package org.nuclos.server.jms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSOnce;

public class JMSSendOnce extends JMSOnce {
	
	private static final Logger LOG = Logger.getLogger(JMSSendOnce.class);
	
	//
	
	public JMSSendOnce() {	
	}
	
	public void once() {
		LOG.info("Now sending all JMS messages: textSize=" + topic2TextMessage.size() 
				+ " objectSize=" + topic2ObjectMessage.size());
		for (String topic: topic2TextMessage.keySet()) {
			final Set<String> set = topic2TextMessage.get(topic);
			for (String text: set) {
				NuclosJMSUtils.sendMessage(text, topic);
			}
		}
		for (String topic: topic2ObjectMessage.keySet()) {
			final Set<Serializable> set = topic2ObjectMessage.get(topic);
			for (Serializable text: set) {
				NuclosJMSUtils.sendObjectMessage(text, topic, null);
			}
		}
		clear();
	}

}

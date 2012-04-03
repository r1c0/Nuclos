package org.nuclos.server.jms;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSOnceSyncDelayed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class JMSSendToGlobalQueue extends JMSSendOnce {

	private static final Logger LOG = Logger.getLogger(JMSSendToGlobalQueue.class);
	
	//
	
	private JMSOnceSyncDelayed global;
	
	//
	
	public JMSSendToGlobalQueue() {	
	}
	
	@Autowired
	void setJMSOnceSync(JMSOnceSyncDelayed global) {
		this.global = global;
	}
	
	public void once() {
		LOG.info("Now sending all JMS messages to global queue: textSize=" + topic2TextMessage.size() 
				+ " objectSize=" + topic2ObjectMessage.size());
		for (String topic: topic2TextMessage.keySet()) {
			final Set<String> set = topic2TextMessage.get(topic);
			for (String text: set) {
				global.queue(topic, text);
			}
		}
		for (String topic: topic2ObjectMessage.keySet()) {
			final Set<Serializable> set = topic2ObjectMessage.get(topic);
			for (Serializable object: set) {
				global.queue(topic, object);
			}
		}
		// trigger real sending with delay
		global.once();
		clear();
	}
	
}

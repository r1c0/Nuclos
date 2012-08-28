package org.nuclos.client;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

public class DeserializingFactoryBean {
	
	private final static Logger LOG = Logger.getLogger(DeserializingFactoryBean.class);
	
    public DeserializingFactoryBean() {
    }
    
    public Object createInstance(Class<?> clazz) {
    	try {
    		Object object = LocalUserCaches.getInstance().getObject(clazz);
    		return (object != null) ? object : BeanUtils.instantiateClass(clazz);
    	}
    	/*
    	 * If something goes wrong here (i.e. NPE in deserialize(clazz)), just
    	 * ignore the serialized stuff. (tp)
    	 */
    	catch (Exception e) {
    		LOG.warn("Client caches: recreating bean for " + clazz.getName() + " failed: " + e, e);
    		return BeanUtils.instantiateClass(clazz);
    	}
    }
}


package org.nuclos.client;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class DeserializingFactoryBean {
	
	private final static Logger LOG = Logger.getLogger(DeserializingFactoryBean.class);
	
	// Spring injection
	
	private LocalUserCaches localUserCaches;
	
	// end of Spring injection
	
    public DeserializingFactoryBean() {
    }
    
    @Autowired
    final void setLocalUserCaches(LocalUserCaches localUserCaches) {
    	this.localUserCaches = localUserCaches;
    }
    
    public Object createInstance(Class<?> clazz) {
    	try {
    		Object object = localUserCaches.getObject(clazz);
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


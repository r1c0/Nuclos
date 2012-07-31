package org.nuclos.client;

import org.springframework.beans.BeanUtils;

public class DeserializingFactoryBean {
	
    public DeserializingFactoryBean() {
    }
    
    public void afterPropertiesSet() throws Exception {
    }
    
    public Object createInstance(Class clazz) throws Exception {
    	Object object = LocalUserCaches.getInstance().getObject(clazz);
    	return (object != null) ? object : BeanUtils.instantiateClass(clazz);
    }
}


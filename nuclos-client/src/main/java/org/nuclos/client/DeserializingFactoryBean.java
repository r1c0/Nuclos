package org.nuclos.client;

import org.nuclos.client.common.MetaDataClientProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;

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


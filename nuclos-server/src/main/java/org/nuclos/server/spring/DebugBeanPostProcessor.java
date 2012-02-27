//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.spring;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class DebugBeanPostProcessor implements BeanPostProcessor {
	
	private static final Logger LOG = Logger.getLogger(DebugBeanPostProcessor.class);

	DebugBeanPostProcessor() {
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (shouldLog(beanName)) {
			final Class<?> beanClass = bean.getClass();
			LOG.info("after init: " + beanName + " -> " + beanClass.getName() + " (" + bean 
					+ ") direct interfaces: " + Arrays.asList(beanClass.getInterfaces()));
		}
		return bean;
	}
	
	private boolean shouldLog(String beanName) {
		return beanName.endsWith("Local") || beanName.startsWith("org.nuclos.server.spring.");
	}
}

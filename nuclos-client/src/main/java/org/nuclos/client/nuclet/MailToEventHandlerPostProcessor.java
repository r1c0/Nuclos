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
package org.nuclos.client.nuclet;

import org.nuclos.api.ui.MailToEventHandler;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

// @Component
public class MailToEventHandlerPostProcessor implements BeanPostProcessor, Ordered {
	
	private MailToEventHandlerRepository repository;
	
	public MailToEventHandlerPostProcessor() {
	}
	
	@Autowired
	public void setMailToEventHandlerRepository(MailToEventHandlerRepository repository) {
		this.repository = repository;
	}

	@Override
	public int getOrder() {
		return 99999;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AopInfrastructureBean) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}
//		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (bean instanceof MailToEventHandler) {
			repository.addMailToEventHandler((MailToEventHandler) bean);
		}
		
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)	throws BeansException {
		return bean;
	}

}

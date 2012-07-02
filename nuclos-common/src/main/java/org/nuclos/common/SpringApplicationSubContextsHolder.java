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
package org.nuclos.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class SpringApplicationSubContextsHolder {

	private static final Logger LOG = Logger.getLogger(SpringApplicationSubContextsHolder.class);

	private static ApplicationContext applicationContext;

	private static SpringApplicationSubContextsHolder INSTANCE;

	//

	private final ArrayList<AbstractXmlApplicationContext> subContexts = new ArrayList<AbstractXmlApplicationContext>();

	/**
	 * private Constructor which
	 * initialize Spring ApplicationContext
	 */
	SpringApplicationSubContextsHolder() {
		INSTANCE = this;
	}

	public static SpringApplicationSubContextsHolder getInstance() {
		return INSTANCE;
	}

	public synchronized void registerSubContext(AbstractXmlApplicationContext ctx) {
		subContexts.add(ctx);
	}

	/*
	 * try to find a Bean in the Spring ApplicationContext
	 * 
	 * @param strBean String
	 * @return Object
	 */
	public Object searchBean(String strBean) {
		Object bean = null;
		try {
			final List<AbstractXmlApplicationContext> subs;
			synchronized (this) {
				subs = (List<AbstractXmlApplicationContext>) subContexts.clone();
			}
			for (AbstractXmlApplicationContext c : subs) {
				if (c.containsBean(strBean)) {
					bean = c.getBean(strBean);
					break;
				}
			}
			if (bean == null) {
				throw new NoSuchBeanDefinitionException(strBean);
			}
		}
		catch (BeansException e) {
			throw new NuclosFatalException(e);
		}
		return bean;
	}

}

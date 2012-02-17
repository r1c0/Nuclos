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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.nuclos.server.common.NuclosRemoteContextHolder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Used in conjuction with {@link FacadeLocalProxyBeanFactoryPostProcessor}. 
 * Do not annotate as Component!
 * 
 * @author Thomas Pasch
 */
public class FacadeLocalProxyFactoryBean<T> implements FactoryBean<T> {
	
	private Object facadeBean;
	
	private Class<T> facadeLocalInterface;
	
	private NuclosRemoteContextHolder ctx;
	
	public FacadeLocalProxyFactoryBean() {
	}
	
	public void setFacadeBean(Object facadeBean) {
		Assert.notNull(facadeBean);
		this.facadeBean = facadeBean;
	}
	
	public void setFacadeLocalInterface(Class<T> facadeLocalInterface) {
		Assert.notNull(facadeLocalInterface);
		this.facadeLocalInterface = facadeLocalInterface;
	}
	
	public void setNuclosRemoteContextHolder(NuclosRemoteContextHolder ctx) {
		Assert.notNull(ctx);
		this.ctx = ctx;
	}

	@Override
	public T getObject() throws Exception {
		if (facadeLocalInterface == null) {
			throw new NullPointerException();
		}
		final Class<?> fbc = facadeBean.getClass();
		final Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { facadeLocalInterface }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				try {
					ctx.setRemotly(false);
					final Method realMethod = fbc.getMethod(method.getName(), method.getParameterTypes());
					return realMethod.invoke(facadeBean, args);
				} catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				} finally {
					ctx.pop();
				}
			}
		});
		return (T) proxy;
	}

	@Override
	public Class<?> getObjectType() {
		return facadeLocalInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

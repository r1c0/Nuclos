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
package org.nuclos.server.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.nuclos.common2.ServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerServiceLocator extends ServiceLocator {
	
	private NuclosRemoteContextHolder ctx;
	
	public ServerServiceLocator() {
	}
	
	@Autowired
	void setNuclosRemoteContextHolder(NuclosRemoteContextHolder ctx) {
		this.ctx = ctx;
	}

	@Override
	public <T> T getFacade(Class<T> c) {
		final Object target = super.getFacade(c);
		final Class<?> targetClass = target.getClass();
		if (c.getSimpleName().endsWith("Local")) {
			Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {c}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					try {
						ctx.setRemotly(false);
						final Method realMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
						return realMethod.invoke(target, args);
					} catch (InvocationTargetException ex) {
						throw ex.getTargetException();
					} finally {
						ctx.pop();
					}
				}
			});
			return (T) proxy;
		}
		else {
			return (T) target;
		}
	}
}

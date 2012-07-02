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
package org.nuclos.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;

// @Component
public class NuclosHttpInvokerAttributeContext implements DisposableBean {

	private ThreadLocal<Boolean> supported = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	private ThreadLocal<HashMap<String, Serializable>> threadLocal = new ThreadLocal<HashMap<String, Serializable>>() {
		@Override
		protected HashMap<String, Serializable> initialValue() {
			return new HashMap<String, Serializable>();
		}
	};
	
	public NuclosHttpInvokerAttributeContext() {
	}

	public void put(String key, Serializable object) {
		threadLocal.get().put(key, object);
	}

	public void putAll(Map<String, Serializable> entries) {
		threadLocal.get().putAll(entries);
	}

	public HashMap<String, Serializable> get() {
		return threadLocal.get();
	}

	public Serializable get(String key) {
		return threadLocal.get().get(key);
	}

	public void clear() {
		threadLocal.get().clear();
	}

	public void setSupported(boolean value) {
		supported.set(Boolean.valueOf(value));
	}

	public boolean isSupported() {
		final boolean result;
		// bean has already been destroyed.
		if (supported == null) {
			result = false;
		}
		else {
			final Boolean b = supported.get();
			if (b == null) {
				result = false;
			}
			else {
				result = b.booleanValue();
			}
		}
		return result;
	}
	
	// @PreDestroy
	public synchronized void destroy() {
		if (supported != null) {
			supported.remove();
		}
		if (threadLocal != null) {
			threadLocal.remove();
		}
		supported = null;
		threadLocal = null;
	}
}

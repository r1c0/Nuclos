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

import java.util.TimeZone;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

@Component
public class NuclosUserDetailsContextHolder {
	
	private ThreadLocal<TimeZone> threadLocal = new ThreadLocal<TimeZone>();
	
	public NuclosUserDetailsContextHolder() {
	}
	
	public void setTimeZone(TimeZone tz) {
		threadLocal.set(tz);
	}
	
	public TimeZone getTimeZone() {
		return threadLocal.get();
	}
	
	public synchronized void clear() {
		threadLocal.remove();
	}
	
	@PreDestroy
	public synchronized void destroy() {
		if (threadLocal == null) return;
		
		threadLocal.remove();
		threadLocal = null;
	}	

}

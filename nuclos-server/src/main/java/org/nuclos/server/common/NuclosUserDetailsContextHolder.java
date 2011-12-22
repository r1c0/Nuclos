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

import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class NuclosUserDetailsContextHolder {
	
	private static final ThreadLocal<TimeZone> threadLocal = new ThreadLocal<TimeZone>();
	
	public static void setTimeZone(TimeZone tz) {
		threadLocal.set(tz);
	}
	
	public static TimeZone getTimeZone() {
		return threadLocal.get();
	}
	
	@PreDestroy
	public static void clear() {
		threadLocal.remove();
	}

}

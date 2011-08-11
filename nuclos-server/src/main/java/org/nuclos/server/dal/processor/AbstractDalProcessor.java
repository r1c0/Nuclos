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
package org.nuclos.server.dal.processor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;

public abstract class AbstractDalProcessor<DalVO extends IDalVO> {

	private Logger log;

	private Class<?> dalVOClzz;

	/**
	 * Data Types
	 */
	protected final static Class<String>	        DT_STRING	         = java.lang.String.class;
	protected final static Class<Integer>	        DT_INTEGER	         = java.lang.Integer.class;
	protected final static Class<Long>	            DT_LONG	             = java.lang.Long.class;
	protected final static Class<Date>	            DT_DATE	             = java.util.Date.class;
	protected final static Class<InternalTimestamp>	DT_INTERNALTIMESTAMP = org.nuclos.common2.InternalTimestamp.class;
	protected final static Class<Boolean>	        DT_BOOLEAN	         = java.lang.Boolean.class;

	protected AbstractDalProcessor() {
		initLogger();
	}

	public final String getProcessor() {
		Class<?>[] interfaces = getClass().getInterfaces();
		if (interfaces.length == 0)
			return "<[none]>";
		return interfaces[0].getName();
	}

	protected DalVO newDalVOInstance(){
		try {
			DalVO newInstance = (DalVO) getDalVOClass().newInstance();
			return newInstance;
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	public Class<DalVO> getDalVOClass() {
		if (dalVOClzz == null) {
			Type genericSuperClass = getClass().getGenericSuperclass();
			while (!(genericSuperClass instanceof ParameterizedType) && genericSuperClass instanceof Class<?>) {
				genericSuperClass = ((Class<?>)genericSuperClass).getGenericSuperclass();
			}
			Type actualType = ((ParameterizedType)genericSuperClass).getActualTypeArguments()[0];
			if(actualType instanceof ParameterizedType) {
				dalVOClzz = (Class<?>) ((ParameterizedType) actualType).getRawType();
			}
			else {
				dalVOClzz = (Class<?>) actualType;
			}
		}
		return (Class<DalVO>) dalVOClzz;
	}

	protected void initLogger() {
		this.log = Logger.getLogger(this.getClass());
	}

	/**
	 * @return a logger for the class of this object.
	 */
	public Logger getLogger() {
		return this.log;
	}

	protected void debug(Object o) {
		this.log(Level.DEBUG, o);
	}

	protected void info(Object o) {
		this.log(Level.INFO, o);
	}

	protected void warn(Object o) {
		this.log(Level.WARN, o);
	}

	protected void error(Object o) {
		this.log(Level.ERROR, o);
	}

	protected void fatal(Object o) {
		this.log(Level.FATAL, o);
	}

	protected void log(Priority priority, Object oMessage, Throwable t) {
		this.getLogger().log(priority, oMessage, t);
	}

	protected void log(Priority priority, Object oMessage) {
		this.getLogger().log(priority, oMessage);
	}

	protected boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}
}

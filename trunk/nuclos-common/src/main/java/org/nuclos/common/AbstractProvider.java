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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class AbstractProvider {
	
	private Logger log;
	
	public AbstractProvider() {
		initLogger();
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

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
package org.nuclos.common2;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.exception.CommonFatalException;
import org.springframework.context.ApplicationContext;

/**
 * ServiceLocator (pattern from "Core J2EE Patterns").
 * The properties for the initial context must be specified by the environment,
 * as stated in the documentation of the <code>InitialContext</code> class.
 * The possibilites for this include system properties specified in the arguments of the JVM call
 * as well as <code>jndi.properties</code> files located in the classpath.
 * It is also possible to explicitly specify the environment via the <code>setEnvironment</code> method.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @deprecated All services are in the spring context - please use spring injection.
 * 		This is especially important on the client. (tp)
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * 
 * @deprecated Use Spring injection instead of this.
 */
public class ServiceLocator {

	private static final Logger LOG = Logger.getLogger(ServiceLocator.class);
	
	private static ServiceLocator INSTANCE;
	
	//

	private boolean enableLoggingProxy;
	private String  stackTraceMatchRegexp;

	/**
	 * private constructor as we want to allow no more than one instance.
	 */
	protected ServiceLocator() throws CommonFatalException {
		enableLoggingProxy = Boolean.valueOf(System.getProperty("loggingproxy.enable", "false"));
		stackTraceMatchRegexp = StringUtils.nullIfEmpty(System.getProperty("loggingproxy.tracematch"));
		INSTANCE = this;
	}

	/**
	 * @return the one (and only) instance of ServiceLocator
	 */
	public static ServiceLocator getInstance() throws CommonFatalException {
		// return (ServiceLocator) SpringApplicationContextHolder.getBean("serviceLocator");
		return INSTANCE;
	}

	private <T> T getFacade(Class<T> c, String bean, boolean server) {
		try {
			T result;
			if(server) {
	    		ApplicationContext context = SpringApplicationContextHolder.getApplicationContext();
				result = (T) context.getBean(bean);
			}
			else {
				try {
					result = SpringApplicationContextHolder.getBean(c);
				}
				catch(Exception e) {
					LOG.info("getFacade: spring failed on type '" + c.getName() 
							+ "', now trying bean name '" + bean
							+ "':\n\t" + e );
					result = (T) SpringApplicationContextHolder.getBean(bean);
				}
			}
			if (result == null) {
				throw new NuclosFatalException("Bean for class=" + c + " name=" + bean + " server=" + server);
			}
			return result;
		}
		catch (Exception ex) {
			throw new NuclosFatalException("Can't get remote facade for " 
					+ bean + " (" + c.getName() + ") server=" + server, ex);
		}
	}

	public <T> T getFacade(Class<T> c) {
		boolean isLocal = false;
		try {
			if (c.getSimpleName().endsWith("Local")) {
				isLocal = true;
			}
			else if (c.getSimpleName().endsWith("Remote")) {
				isLocal = false;
			}
			String springBeanName = c.getSimpleName().substring(0, c.getSimpleName().lastIndexOf("Facade")) + "Service";
			springBeanName = org.apache.commons.lang.StringUtils.uncapitalize(springBeanName);
			return getFacade(c, springBeanName, isLocal);
		}
		catch(Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

}	// class ServiceLocator

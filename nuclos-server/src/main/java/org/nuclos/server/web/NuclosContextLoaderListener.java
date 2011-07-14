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

package org.nuclos.server.web;


import java.io.FileNotFoundException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.log4j.Logger;
import org.nuclos.common.SpringApplicationContextHolder;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.util.Log4jWebConfigurer;
import org.springframework.web.util.WebUtils;

public class NuclosContextLoaderListener extends ContextLoaderListener {

	public static final String JNDI_LOG4J_PATH = "java:comp/env/nuclos-conf-log4j";
	public static final String JNDI_LOG4J_INTERVAL = "java:comp/env/nuclos-conf-log4j-refresh";

	protected Logger log;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		initLogging(event.getServletContext());

		super.contextInitialized(event);

		this.log = Logger.getLogger(this.getClass());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// We perform some additional clean-up actions to free
		// all system classes from Nuclos references.
		// Any such reference will lead to a memory leak after
		// undeploying/stopping the server because it prevents
		// Java from releasing and garbage-collecting the app.

		ClassLoader webappClassLoader = getClass().getClassLoader(); // used for checking

		// Get some beans before we destroy the context
		XBeanBrokerService activeMQBroker = (XBeanBrokerService) SpringApplicationContextHolder.getBean("broker");
		org.apache.activemq.thread.Scheduler activeMQScheduler = org.apache.activemq.thread.Scheduler.getInstance();

		// Let Spring perform its default clean-ups...
		super.contextDestroyed(event);

		// Shutdown ActiveMQ thread and broker
		try {
			if (activeMQScheduler != null && activeMQScheduler.getClass().getClassLoader() == webappClassLoader) {
				activeMQScheduler.shutdown();
			}
			if (activeMQBroker != null && activeMQBroker.getClass().getClassLoader() == webappClassLoader) {
				activeMQBroker.stop();
				activeMQBroker.destroy();
			}
		} catch (Exception ex) {
			log.warn(ex);
		}

		// Unregister all JDBC driver registered by this webapp
		// See also DBCP-332: possible memory leak during Tomcat context reloads
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		List<Driver> webAppDrivers = new ArrayList<Driver>();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == webappClassLoader)
				webAppDrivers.add(driver);
		}
		for (Driver driver : webAppDrivers) {
			try {
				DriverManager.deregisterDriver(driver);
			} catch (Exception ex) {
				log.warn(ex);
			}
		}

		shutdownLogging(event.getServletContext());
		// TODO: there are still some "open" references...
	}


	/**
	 * Initialize log4j logging from JNDI configuration (or default paths).
	 * @param servletContext the current ServletContext
	 * @see Log4jWebConfigurer#initLogging
	 */
	public static void initLogging(ServletContext servletContext) {
		String location = null;
		try {
			JndiTemplate template = new JndiTemplate();
			location = template.lookup(JNDI_LOG4J_PATH, String.class);
		}
		catch (NamingException ex) {
			System.out.println(JNDI_LOG4J_PATH + " not found.");
		}

		if (location == null) {
			location = "WEB-INF/log4j.properties";
		}

		Integer interval = null;
		try {
			JndiTemplate template = new JndiTemplate();
			interval = template.lookup(JNDI_LOG4J_INTERVAL, Integer.class);
		}
		catch (NamingException ex) {
			System.out.println(JNDI_LOG4J_INTERVAL + " not found.");
		}

		if (location != null) {
			// Perform actual log4j initialization; else rely on log4j's default initialization.
			try {
				// Return a URL (e.g. "classpath:" or "file:") as-is;
				// consider a plain file path as relative to the web application root directory.
				if (!ResourceUtils.isUrl(location)) {
					// Resolve system property placeholders before resolving real path.
					location = SystemPropertyUtils.resolvePlaceholders(location);
					location = WebUtils.getRealPath(servletContext, location);
				}

				// Write log message to server log.
				servletContext.log("Initializing log4j from [" + location + "]");

				if (interval != null) {
					// Initialize with refresh interval, i.e. with log4j's watchdog thread,
					// checking the file in the background.
					Log4jConfigurer.initLogging(location, interval);
				}
				else {
					// Initialize without refresh check, i.e. without log4j's watchdog thread.
					Log4jConfigurer.initLogging(location);
				}
			}
			catch (FileNotFoundException ex) {
				throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ex.getMessage());
			}
		}
	}

	/**
	 * Shut down log4j.
	 * @param servletContext the current ServletContext
	 * @see Log4jWebConfigurer#shutdownLogging
	 */
	public static void shutdownLogging(ServletContext servletContext) {
		servletContext.log("Shutting down log4j");
		try {
			Log4jConfigurer.shutdownLogging();
		}
		catch (Exception ex) {
			servletContext.log("log4j shutdown exception", ex);
		}
	}
}
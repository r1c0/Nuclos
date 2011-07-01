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


import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContextEvent;

import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.log4j.Logger;
import org.nuclos.common.SpringApplicationContextHolder;
import org.springframework.web.context.ContextLoaderListener;

public class NuclosContextLoaderListener extends ContextLoaderListener {

	protected Logger log = Logger.getLogger(this.getClass());

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

		// TODO: there are still some "open" references...
	}

}

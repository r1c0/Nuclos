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
package org.nuclos.server.mbean;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.log4j.Logger;


/**
 * Static methods for adding MBeans to the default MBeanServer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version	01.00.00
 */
public abstract class MBeanAgent  {
	private static final Logger log = Logger.getLogger(MBeanAgent.class);

	private final static String CACHE_DOMAIN = "NuclosCaches";
	private final static String CONFIG_DOMAIN = "NuclosConfiguration";
	
	/**
	 * register MBean in the Nucleus Cache Domain
	 * @param implementation
	 * @param beanClass
	 */
	public static <T> void registerCache(T implementation, Class<T> beanClass) {
		registerMBean(CACHE_DOMAIN, implementation, beanClass);
	}

	/**
	 * register MBena in the Nucleus Configuration Domain
	 * @param implementation
	 * @param beanClass
	 */
	public static <T> void registerConfiguration(T implementation, Class<T> beanClass) {
		registerMBean(CONFIG_DOMAIN, implementation, beanClass);
	}

	/**
	 * register a mbean in the PlatformMBeanServer
	 * @param domain
	 * @param implementation
	 * @param beanClass
	 */
	private static <T> void registerMBean(String domain, T implementation, Class<T> beanClass) {
		try {
			final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			final ObjectName objectName = getObjectName(domain, implementation);

			if (!mbs.isRegistered(objectName)) {
				mbs.registerMBean(new StandardMBean(implementation, beanClass), objectName);
			}

			/*
			 * all errors during MBean registration should not prevent objects from
			 * being initiated so these errors are only logged
			 */
		} catch (InstanceAlreadyExistsException e) {
			log.warn("MBean of class " + beanClass.toString() + " already registered");
		} catch (Exception e) {
			log.error("Registration of MBean for " + beanClass.toString() + " failed", e);
		}
	}
	
	
	/**
	 * helper method to get a unique ObjectName for a MBean
	 * @param domain
	 * @param implementation
	 * @return
	 * @throws MalformedObjectNameException
	 * @throws NullPointerException
	 */
	private static ObjectName getObjectName(String domain, Object implementation) throws MalformedObjectNameException, NullPointerException {
		final String[] arr = implementation.getClass().getName().split("\\.");
		final String implementationClassName = arr.length > 0 ? arr[arr.length - 1] : implementation.getClass().getName();
		return new ObjectName(domain + ":name=" + implementationClassName);
	}
	
}

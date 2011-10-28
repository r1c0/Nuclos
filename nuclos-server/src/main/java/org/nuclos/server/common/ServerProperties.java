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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

public class ServerProperties {

	private static final Logger LOG = Logger.getLogger(ServerProperties.class);
	
	public static final String JNDI_SERVER_PROPERTIES = "java:comp/env/nuclos-conf-server";

	private ServerProperties() {
	}

	public static Properties loadProperties(String jndiname) {
		return loadProperties(jndiname, true);
	}

	public static Properties loadProperties(String jndiname, boolean failIfMissing) {
		return loadProperties(new Properties(), jndiname, failIfMissing);
	}

	public static Properties loadProperties(Properties properties, String jndiname, boolean failIfMissing) {
		// try to get the path of the properties-file from JNDI
		String location = null;
		try {
			JndiTemplate template = new JndiTemplate();
			location = template.lookup(jndiname, String.class);
		}
		catch (NamingException ex) {
			LOG.error(jndiname + " not found.", ex);
		}

		InputStream is = null;
		if (location != null) {
			String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
			try {
				is = new FileInputStream(ResourceUtils.getFile(resolvedLocation));
			}
			catch (FileNotFoundException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		
		if (is == null) {
			if (failIfMissing) {
				throw new NuclosFatalException("Missing server properties file from jndi " + jndiname);
			} else {
				return null;
			}
		}
		try {
			properties.load(is);
			is.close();
		} catch (IOException ex) {
			throw new NuclosFatalException("Error loading server properties file from jndi " + jndiname, ex);
		}
		return properties;
	}
}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.nuclos.common.NuclosFatalException;

public class ServerProperties {

	public static final String NUCLOS_SERVER_PROPERTIES = "nuclos-server.properties";

	private ServerProperties() {
	}

	public static Properties loadProperties(String filename) {
		return loadProperties(filename, true);
	}

	public static Properties loadProperties(String filename, boolean failIfMissing) {
		return loadProperties(new Properties(), filename, failIfMissing);
	}
	
	public static Properties loadProperties(Properties properties, String filename, boolean failIfMissing) {
		InputStream is = ServerProperties.class.getClassLoader().getResourceAsStream(filename);
		if (is == null) {
			if (failIfMissing) {
				throw new NuclosFatalException("Missing server properties file " + filename);
			} else {
				return null;
			}
		}
		try {
			properties.load(is);
			is.close();
		} catch (IOException ex) {
			throw new NuclosFatalException("Error loading server properties file " + filename, ex);
		}
		return properties;
	}
}

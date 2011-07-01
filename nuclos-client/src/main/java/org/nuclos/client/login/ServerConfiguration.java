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
package org.nuclos.client.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosFatalException;

/**
 * A server configuration.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ServerConfiguration {

	/**
	 * name of the properties file containing the server configurations
	 */
	private static final String NUCLOS_CLIENT_PROPERTIES = "nuclos-client.properties";

	/** Property name for the Server JNP url. */	
	public static final String NUCLOS_SERVER_URL = "nuclos.server.url";
	/** Property name for the Server name. */
	public static final String NUCLOS_SERVER_NAME = "nuclos.server.name";
	public static final String DEFAULT_SERVER_NAME = "Nuclos";

	private final String sName;
	private final String sUrl;

	public ServerConfiguration(String sName, String sUrl) {
		if (sName == null) {
			throw new NullArgumentException("sName");
		}
		this.sName = sName;
		this.sUrl = sUrl;
	}

	public String getName() {
		return sName;
	}

	public String getUrl() {
		return sUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServerConfiguration)) {
			return false;
		}
		final ServerConfiguration that = (ServerConfiguration) o;
		return sName.equals(that.sName);
	}

	@Override
	public int hashCode() {
		return sName.hashCode();
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public Hashtable<String, String> getNamingContext() {
		final Hashtable<String, String> result = new Hashtable<String, String>();
		result.put(Context.PROVIDER_URL, this.getUrl());

		return result;
	}

	public static Map<String, ServerConfiguration> getServerConfigurations() {
		final Map<String, ServerConfiguration> result = new LinkedHashMap<String,ServerConfiguration>();

		ServerConfiguration systemServerConfiguration = getSystemServerConfiguration();
		if (systemServerConfiguration != null) {
			result.put(systemServerConfiguration.getName(), systemServerConfiguration);
		}
		
		File f = new File(NUCLOS_CLIENT_PROPERTIES);
		if (f.exists()) {
			final Properties props = new Properties();
			try {
				final InputStream is = new FileInputStream(NUCLOS_CLIENT_PROPERTIES);
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}
			catch (IOException ex) {
				final String sMessage = "The server configuration could not be loaded from the file " + NUCLOS_CLIENT_PROPERTIES;//"Die Serverkonfiguration konnte nicht aus der Datei " + NUCLOS_CLIENT_PROPERTIES + " geladen werden.";
				throw new NuclosFatalException(sMessage, ex);
			}
	
			if (props.containsKey(NUCLOS_SERVER_NAME) && props.containsKey(NUCLOS_SERVER_URL)) {
				final String sName = props.getProperty(NUCLOS_SERVER_NAME);
				final String sUrl = props.getProperty(NUCLOS_SERVER_URL);
				result.put(sName, new ServerConfiguration(sName, sUrl));
			}
			
			for (int iConfiguration = 1; ; ++iConfiguration) {
				final String sKeyName = NUCLOS_SERVER_NAME + "." + iConfiguration;
				final String sKeyUrl = NUCLOS_SERVER_URL + "." + iConfiguration;
				final String sName = props.getProperty(sKeyName);
				final String sUrl = props.getProperty(sKeyUrl);
				if (sName == null || sUrl == null) {
					break;
				}
				result.put(sName, new ServerConfiguration(sName, sUrl));
			}
		}

		if (result.isEmpty()) {
			result.put("default", new ServerConfiguration("default", "default"));
//			final String sMessage = "No server configuration found";
//			throw new NuclosFatalException(sMessage);
		}
		
		return result;
	}
	
	/**
	 * Returns the default configuration. 
	 */
	public static ServerConfiguration getDefaultServerConfiguration() {
		Map<String, ServerConfiguration> serverConfigurations = getServerConfigurations();
		return serverConfigurations.values().iterator().next();
	}
	
	public static String getSystemServerConfigName() {
		return System.getProperty(ServerConfiguration.NUCLOS_SERVER_NAME);
	}
	
	public static ServerConfiguration getSystemServerConfiguration() {
      final String propertyServerURL = System.getProperty(ServerConfiguration.NUCLOS_SERVER_URL);
      if(propertyServerURL != null) {
      	String propertyServerName = getSystemServerConfigName();      	
      	if (propertyServerName == null) {
      		propertyServerName = DEFAULT_SERVER_NAME;
      	}
      	return new ServerConfiguration(propertyServerName, propertyServerURL);
      }
      return null;
	}

}	// class ServerConfiguration

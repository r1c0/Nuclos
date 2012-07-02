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
package org.nuclos.server.common.ejb3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;

import org.nuclos.api.service.ServerPropertyService;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.springframework.transaction.annotation.Transactional;

@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class ApiServerPropertyService implements ServerPropertyService {

	@Override
	public void setServerProperty(String sPackage, String sKey, String sValue) {
		File confPath = NuclosSystemParameters.getDirectory(NuclosSystemParameters.PACAKGE_PROPERTIES_CONF_PATH);
		if (!confPath.isDirectory()) {
			if (!confPath.mkdirs()) {
				throw new NuclosFatalException(String.format("package-properties conf path \"%s\" not created.", 
						NuclosSystemParameters.getString(NuclosSystemParameters.PACAKGE_PROPERTIES_CONF_PATH)));
			}
		}
		
		File propertiesFile = new File(confPath, sPackage + ".properties");
		
		Properties properties = new Properties();
		
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(propertiesFile));
			properties.load(is);
			is.close();
		} catch (IOException ex) {
			// is new
		}
		
		properties.setProperty(sKey, sValue);
		
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(propertiesFile));
			properties.store(os, String.format("Api server properties for pachage \"%s\"", sPackage));
			os.close();
		} catch (IOException ex) {
			throw new NuclosFatalException(
					String.format("Server property not stored (sPackage=%s, sKey=%s, sValue=%s)", sPackage, sKey, sValue), ex);
		}
	}

	@Override
	public String getServerProperty(String sPackage, String sKey) {
		File confPath = NuclosSystemParameters.getDirectory(NuclosSystemParameters.PACAKGE_PROPERTIES_CONF_PATH);
		if (confPath.isDirectory()) {
			File propertiesFile = new File(confPath, sPackage + ".properties");
			
			Properties properties = new Properties();
			
			InputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream(propertiesFile));
				properties.load(is);
				is.close();
			} catch (IOException ex) {
				// no properties file
			}
			
			return properties.getProperty(sKey);
		}
		return null;
	}

	@Override
	public void removeClientProperty(String sPackage, String sKey) {
		File confPath = NuclosSystemParameters.getDirectory(NuclosSystemParameters.PACAKGE_PROPERTIES_CONF_PATH);
		if (confPath.isDirectory()) {
			File propertiesFile = new File(confPath, sPackage + ".properties");
			
			Properties properties = new Properties();
			
			InputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream(propertiesFile));
				properties.load(is);
				is.close();
				
				properties.remove(sKey);
				
				OutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(propertiesFile));
					properties.store(os, String.format("Api server properties for pachage \"%s\"", sPackage));
					os.close();
				} catch (IOException ex) {
					throw new NuclosFatalException(
							String.format("Server property not removed (sPackage=%s, sKey=%s)", sPackage, sKey), ex);
				}
			} catch (IOException ex) {
				// no properties file
			}
		}
	}

}

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
package org.nuclos.installer.unpack;

import java.util.List;

import org.nuclos.installer.InstallException;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.mode.Installer;


/**
 * Unpacker (performs installation)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public interface Unpacker {

	/**
	 * Get default value for a property.
	 * @param key Installation setting / property name
	 * @return default value
	 */
	public String getDefaultValue(String key);

	/**
	 * Validate an installation setting / property value
	 * @param key installation setting / property name
	 * @param value installation setting / property value
	 * @throws InstallException if installation fails
	 */
	public void validate(String key, String value) throws InstallException;

	/**
	 * Check if current user has administration privileges.
	 * @return true if user has administration privileges, otherwise false
	 */
	public boolean isPrivileged();
	
	/**
	 * Check if the current user is able to install programs (i.e. Postgres).
	 * On Linux, we try to use an GUI su if the user is not root.
	 * 
	 * @return true if the user can install programs.
	 */
	public boolean canInstall();

	/**
	 * Check if product is registered, i.e. Windows-Registry or /etc/nuclos.ini
	 * @return true if product is registered, otherwise false
	 */
	public boolean isProductRegistered();

	/**
	 * Check if a PostgreSQL-Installer is bundled and if it can be installed on the current platform
	 * @return true if matching installer is bundled
	 */
	public boolean isPostgresBundled();

	/**
	 * Get a list of installed PostgreSQL-Services from Windows-Registry or /etc/postgres-[version].ini
	 * @return List of found PostgreSQL-Services
	 */
	public List<PostgresService> getPostgresServices();

	/**
	 * Check if server is currently running
	 * @param port
	 * @return
	 */
	public boolean isServerRunning();

	/**
	 * Shutdown server before update.
	 * @param cb
	 * @throws InstallException
	 */
	public void shutdown(Installer cb) throws InstallException;

	/**
	 * Perform installation
	 * @param cb
	 * @throws InstallException
	 */
	public void unpack(Installer cb) throws InstallException;


	public void rollback(Installer cb) throws InstallException;

	/**
	 * Perform uninstallation
	 * @param cb
	 * @throws InstallException
	 */
	public void remove(Installer cb) throws InstallException;

	/**
	 * Startup server after installation.
	 * @param cb
	 * @throws InstallException
	 */
	public void startup(Installer cb) throws InstallException;
}

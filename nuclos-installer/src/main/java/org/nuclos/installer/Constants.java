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

package org.nuclos.installer;

/**
 * Constants.
 */
public interface Constants {

	/**
	 *  Nuclos home is the target installation path
	 */
	public static final String NUCLOS_HOME = "server.home";

	/**
	 *  Nuclos.xml filename
	 */
	public static final String NUCLOS_XML = "nuclos.xml";

	/**
	 *  JRE home
	 */
	public static final String JAVA_HOME = "server.java.home";


	/**
	 * JVM DLL (Required for windows service)
	 */
	public static final String JAVA_JVMDLL = "server.java.jvm";

	/**
	 * JVM DLL (Required for windows service)
	 */
	public static final String NUCLOS_INSTANCE = "server.name";

	public static final String HTTP_ENABLED = "server.http.enabled";

	public static final String HTTP_PORT = "server.http.port";

	public static final String HTTPS_ENABLED = "server.https.enabled";

	public static final String HTTPS_PORT = "server.https.port";

	public static final String HTTPS_KEYSTORE_FILE = "server.https.keystore.file";

	public static final String HTTPS_KEYSTORE_PASSWORD = "server.https.keystore.password";

	/**
	 * Shutdown Port
	 */
	public static final String SHUTDOWN_PORT = "server.shutdown.port";

	/**
	 * Server jvm heap size (-Xmx setting)
	 */
	public static final String HEAP_SIZE = "server.heap.size";

	/**
	 * Launch server on startup
	 */
	public static final String LAUNCH_STARTUP = "server.launch.on.startup";

	/**
	 * Single instance (Webstart)
	 */
	public static final String CLIENT_SINGLEINSTANCE = "client.singleinstance";

	public static final String DATABASE_ADAPTER = "database.adapter";
	public static final String DATABASE_DRIVERJAR = "database.driverjar";
	public static final String DATABASE_SERVER = "database.server";
	public static final String DATABASE_PORT = "database.port";
	public static final String DATABASE_NAME = "database.name";
	public static final String DATABASE_USERNAME = "database.username";
	public static final String DATABASE_PASSWORD = "database.password";
	public static final String DATABASE_SCHEMA = "database.schema";
	public static final String DATABASE_TABLESPACE = "database.tablespace";
	public static final String DATABASE_TABLESPACEINDEX = "database.tablespace.index";
	public static final String DATABASE_SETUP = "database.setup";

	public static final String POSTGRES_PREFIX = "postgres.prefix";
	public static final String POSTGRES_DATADIR = "postgres.datadir";
	public static final String POSTGRES_SUPERUSER = "postgres.superuser";
	public static final String POSTGRES_SUPERPWD = "postgres.superpw";
	public static final String POSTGRES_TABLESPACEPATH = "postgres.tablespacepath";

	public static final String SERVER_TOMCAT_DIR = "server.tomcat.dir";

	public static final String OPEN_WEBSTART = "client.open.webstart";

	public static final String DBOPTION_INSTALL = "install";
	public static final String DBOPTION_SETUP = "setup";
	public static final String DBOPTION_USE = "use";

	public static final String UNINSTALL_REMOVEDATAANDLOGS = "uninstall.removedataandlogs";
	
	// versions
	
	public static final String POSTGRESQL_MAIN_VERSION = "9.1";
	
	public static final String POSTGRESQL_FULL_VERSION = "9.1.3-1";
	
	public static final String TOMCAT_FULL_VERSION = "7.0.26";
	
	public static final String TOMCAT_VERSION = "apache-tomcat-" + TOMCAT_FULL_VERSION;

}

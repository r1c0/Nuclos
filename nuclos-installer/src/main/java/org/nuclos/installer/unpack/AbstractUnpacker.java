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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.Main;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.database.DbSetup;
import org.nuclos.installer.database.DbSetup.SetupAction;
import org.nuclos.installer.database.PostgresDbSetup;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.tomcat.Catalina;
import org.nuclos.installer.util.EnvironmentUtils;
import org.nuclos.installer.util.FileUtils;
import org.nuclos.installer.util.PropUtils;

public abstract class AbstractUnpacker implements Unpacker, Constants {

	private static final Logger log = Logger.getLogger(AbstractUnpacker.class);

	protected static final Integer TIMEOUT_STARTUP = 20;
	protected static final Integer TIMEOUT_SHUTDOWN = 20;

	@Override
	public String getDefaultValue(String key) {
		if (NUCLOS_HOME.equals(key)) {
			return System.getProperty("user.home") + "/nuclos";
		}
		else if (JAVA_HOME.equals(key)) {
			String jhenv = System.getenv("JAVA_HOME");
			if (jhenv == null || jhenv.isEmpty()) {
				jhenv = System.getProperty("java.home");
			}
			return jhenv;
		}
		else if (NUCLOS_INSTANCE.equals(key)) {
			return "nuclos";
		}
		else if (HTTP_ENABLED.equals(key)) {
			return "true";
		}
		else if (HTTP_PORT.equals(key)) {
			return "80";
		}
		else if (HTTPS_ENABLED.equals(key)) {
			return "false";
		}
		else if (HTTPS_PORT.equals(key)) {
			return "443";
		}
		else if (SHUTDOWN_PORT.equals(key)) {
			return "8005";
		}
		else if (HEAP_SIZE.equals(key)) {
			return "1024";
		}
		else if (CLIENT_SINGLEINSTANCE.equals(key)) {
			return new Boolean(false).toString();
		}
		else if (DATABASE_ADAPTER.equals(key)) {
			return "postgresql";
		}
		else if (DATABASE_SERVER.equals(key)) {
			return "localhost";
		}
		else if (DATABASE_PORT.equals(key)) {
			return "5432";
		}
		else if (DATABASE_NAME.equals(key)) {
			return "nuclosdb";
		}
		else if (DATABASE_USERNAME.equals(key)) {
			return "nuclos";
		}
		else if (DATABASE_PASSWORD.equals(key)) {
			return "nuclos";
		}
		else if (DATABASE_SCHEMA.equals(key)) {
			return "nuclos";
		}
		return null;
	}

	@Override
	public void validate(String key, String value) throws InstallException {
		if (key != null && key.contains("pass")) {
			log.info(MessageFormat.format("Validate password property {0}=***", key));
		}
		else {
			log.info(MessageFormat.format("Validate property {0}={1}", key, value));
		}
		if (NUCLOS_HOME.equals(key)) {
			validateNuclosHome(value);
		}
		else if (NUCLOS_INSTANCE.equals(key)) {
			String old = ConfigContext.getProperty(NUCLOS_INSTANCE);
			ConfigContext.setProperty(NUCLOS_INSTANCE, value);
			if (ConfigContext.hasPropertyChanged(NUCLOS_INSTANCE) && isProductRegistered()) {
				ConfigContext.setProperty(NUCLOS_INSTANCE, old);
				throw new InstallException("validation.instance.name.taken", ConfigContext.getProperty(NUCLOS_INSTANCE));
			}
		}
		else if (JAVA_HOME.equals(key)) {
			validateJavaHome(value);
		}
		else if (HTTP_PORT.equals(key)) {
			validateHttpPort(value);
		}
		else if (HTTPS_PORT.equals(key)) {
			validateHttpsPort(value);
		}
		else if (SHUTDOWN_PORT.equals(key)) {
			validateShutdownPort(value);
		}
		else if (DATABASE_PORT.equals(key)) {
			validateDbPort(value);
		}
		else if (DATABASE_DRIVERJAR.equals(key)) {
			validateDriverJar(value);
		}
		else if (HTTPS_KEYSTORE_FILE.equals(key)) {
			validateKeystore(value);
		}
	}

	@Override
	public boolean isServerRunning() {
		try {
			String sport = "true".equals(ConfigContext.getProperty(HTTPS_ENABLED)) ? ConfigContext.getProperty(HTTPS_PORT) : ConfigContext.getProperty(HTTP_PORT);
			Integer port = Integer.parseInt(sport);
			InetAddress host = InetAddress.getByName("localhost");
			SocketAddress url = new InetSocketAddress(host, port);

			Socket socket = new Socket();
			socket.connect(url, 1 * 1000);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	protected boolean waitStartup(Integer seconds) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, seconds);

		while (Calendar.getInstance().compareTo(c) < 0) {
			if (isServerRunning()) {
				return true;
			}
		}
		return false;
	}

	protected boolean waitShutdown(Integer seconds) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, seconds);

		while (Calendar.getInstance().compareTo(c) < 0) {
			if (!isServerRunning()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void unpack(final Installer cb) throws InstallException {
		try {
			// Setup database
			String dbsetup = ConfigContext.getProperty(DATABASE_SETUP);

			if (DBOPTION_INSTALL.equals(dbsetup) && isPrivileged()) {
				cb.info("unpack.step.installdb");
				installPostgres(cb);
			}

			if (DBOPTION_INSTALL.equals(dbsetup) || DBOPTION_SETUP.equals(dbsetup)) {
				setupDatabase(cb);
			}

			cb.info("unpack.step.unpack");
			File nuclosHome = ConfigContext.getFileProperty(NUCLOS_HOME);
			if (!ConfigContext.isUpdate()) {
				// Create nuclos home directory
				FileUtils.forceMkdir(nuclosHome);
			}

			List<String> files = new ArrayList<String>();

			files.addAll(FileUtils.unpack(getClass().getClassLoader().getResourceAsStream("resources.zip"), nuclosHome, cb));

			File extensiondir = new File(nuclosHome, "extensions");
			if (extensiondir.exists() && !FileUtils.isEmptyDir(extensiondir, true)) {
				File commonextensions = new File(extensiondir, "common");
				if (commonextensions.exists() && !FileUtils.isEmptyDir(commonextensions, true)) {
					files.addAll(FileUtils.getFiles(commonextensions));
					files.addAll(FileUtils.copyDirectory(commonextensions, new File(ConfigContext.getFileProperty("server.webapp.dir"), "WEB-INF/lib/"), cb));
					files.addAll(FileUtils.copyDirectory(commonextensions, new File(ConfigContext.getFileProperty("server.webapp.dir"), "app/extensions"), cb));
					files.addAll(FileUtils.copyDirectory(commonextensions, new File(nuclosHome, "client"), cb));
				}
				File serverextensions = new File(extensiondir, "server");
				if (serverextensions.exists() && !FileUtils.isEmptyDir(serverextensions, true)) {
					files.addAll(FileUtils.getFiles(serverextensions));
					files.addAll(FileUtils.copyDirectory(serverextensions, new File(ConfigContext.getFileProperty("server.webapp.dir"), "WEB-INF/lib/"), cb));
				}
				File clientextensions = new File(extensiondir, "client");
				if (clientextensions.exists() && !FileUtils.isEmptyDir(clientextensions, true)) {
					files.addAll(FileUtils.getFiles(clientextensions));
					files.addAll(FileUtils.copyDirectory(clientextensions, new File(ConfigContext.getFileProperty("server.webapp.dir"), "app/extensions"), cb));
					files.addAll(FileUtils.copyDirectory(clientextensions, new File(nuclosHome, "client"), cb));
				}
			}

			files.addAll(FileUtils.unpack(getClass().getClassLoader().getResourceAsStream(TOMCAT_VERSION + ".zip"), new File(nuclosHome, "tomcat"), cb));

			// Database driver (if necessary)
			if (!"postgresql".equals(ConfigContext.getProperty(DATABASE_ADAPTER))) {
				File driver = new File(ConfigContext.getProperty(DATABASE_DRIVERJAR));
				File target = new File(ConfigContext.getFileProperty("server.webapp.dir"), "WEB-INF/lib/" + driver.getName());
				if (driver.exists() && !driver.equals(target)) {
					files.add(FileUtils.copyFile(driver, target, false, cb));
					ConfigContext.setProperty(DATABASE_DRIVERJAR, target.getAbsolutePath());
				}
				else if (target.exists()) {
					files.add(target.getAbsolutePath());
				}
			}

			files.addAll(createDataAndLogsDirectory());
			files.addAll(createConfDirectory(cb));

			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/client.bat"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/startup.bat"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/shutdown.bat"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/uninstall.bat"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/startup.sh"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/shutdown.sh"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/uninstall.sh"), ConfigContext.getCurrentConfig(), null);
			PropUtils.replaceTextParameters(new File(nuclosHome, "extra/context.xml"), ConfigContext.getCurrentConfig(), "UTF-8");
			PropUtils.replaceTextParameters(new File(nuclosHome, "bin/launchd.sh"), ConfigContext.getCurrentConfig(), null);

			Catalina catalina = new Catalina(ConfigContext.getFileProperty(SERVER_TOMCAT_DIR), EnvironmentUtils.isWindows());
			catalina.checkTomcat();
			File contextXmlTarget = new File(ConfigContext.getFileProperty(SERVER_TOMCAT_DIR), "conf/Catalina/localhost/" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ".xml");
			files.add(FileUtils.copyFile(new File(nuclosHome, "extra/context.xml"), contextXmlTarget, false, cb));

			catalina.disableAjpConnector();

			String catalinaShutdownPort = ConfigContext.getProperty(SHUTDOWN_PORT);
			if (catalinaShutdownPort != null && !catalinaShutdownPort.isEmpty()) {
				catalina.configureServerShutdownPort(catalinaShutdownPort);
			}

			if ("true".equals(ConfigContext.getProperty(HTTP_ENABLED))) {
				String catalinaHttpPort = ConfigContext.getProperty(HTTP_PORT);
				if (catalinaHttpPort != null && !catalinaHttpPort.isEmpty()) {
					catalina.configureServerHttpPort(catalinaHttpPort);
				}
			}
			else {
				catalina.disableHttpConnector();
			}

			if ("true".equals(ConfigContext.getProperty(HTTPS_ENABLED))) {
				String catalinaHttpsPort = ConfigContext.getProperty(HTTPS_PORT);
				if (catalinaHttpsPort != null && !catalinaHttpsPort.isEmpty()) {
					// Keystore file
					File keystore = new File(ConfigContext.getProperty(HTTPS_KEYSTORE_FILE));
					File target = new File(ConfigContext.getFileProperty(NUCLOS_HOME), "extra/.keystore");
					if (keystore.exists()) {
						files.add(FileUtils.copyFile(keystore, target, false, cb));
					}
					else if (target.exists()) {
						files.add(target.getAbsolutePath());
					}
					catalina.configureHttpsConnector(catalinaHttpsPort, target.getAbsolutePath(), ConfigContext.getProperty(HTTPS_KEYSTORE_PASSWORD));
				}
			}
			else {
				// nothin to do - https is disabled by default
			}

			files.addAll(FileUtils.copyDirectory(new File(nuclosHome, "webapp/app"), new File(nuclosHome, "client"), cb));

			File nativeJar = new File(nuclosHome, "client/nuclos-native.jar");
			if (nativeJar.exists()) {
				files.addAll(FileUtils.unpack(new FileInputStream(nativeJar), new File(nuclosHome, "client"), cb));
				nativeJar.delete();
			}

			// Cleanup for previous versions
			try {
				if (ConfigContext.containsKey("tomcat.home.dir")) {
					File externaltomcatconfiguration = new File(ConfigContext.getProperty("tomcat.home.dir"), "conf/Catalina/localhost/" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ".xml");
					if (externaltomcatconfiguration.exists()) {
						externaltomcatconfiguration.delete();
					}
				}
			}
			catch (Exception ex) {
				cb.warn("error.unregister.tomcat.external", ConfigContext.getPreviousConfig().getProperty("tomcat.home.dir"));
			}

			boolean update = false;
			if (ConfigContext.containsKey("nuclos.home") && !ConfigContext.containsKey("server.home")) {
				update = true;
			}

			// Migrate previous installations
			if (ConfigContext.containsKey("tomcat.home.dir") && ConfigContext.containsKey("tomcat.server.name") && !update) {
				if (askMigration(cb, "Tomcat", ConfigContext.getProperty("tomcat.home.dir") + "/webapps/" + ConfigContext.getProperty("tomcat.server.name"))) {
					String tomcathome = ConfigContext.getProperty("tomcat.home.dir");
					String servername = ConfigContext.getProperty("tomcat.server.name");
					migrateFromExternalTomcat(cb, new File(nuclosHome, "data"), tomcathome, servername);
				}
			}
			else if (ConfigContext.containsKey("jboss.home.dir") && ConfigContext.containsKey("jboss.server.name")) {
				if (askMigration(cb, "JBoss", ConfigContext.getProperty("jboss.home.dir") + "/server/" + ConfigContext.getProperty("jboss.server.name"))) {
					String jbosshome = ConfigContext.getProperty("jboss.home.dir");
					String servername = ConfigContext.getProperty("jboss.server.name");
					migrateFromJBoss(cb, new File(new File(nuclosHome, "data"), "documents"), new File(new File(nuclosHome, "data"), "resource"), jbosshome, servername);
				}
			}

			File temp30Tomcat = new File(nuclosHome, "apache-tomcat-7.0.11");
			try {
				if (temp30Tomcat.exists() && temp30Tomcat.isDirectory()) {
					FileUtils.delete(temp30Tomcat, true);
				}
			}
			catch (Exception ex) {
				cb.warn("error.remove.bundled.tomcat", temp30Tomcat.getAbsolutePath());
			}

			// update check (remove files that were not unpacked within the current installation
			for (String file : FileUtils.getFiles(nuclosHome,
					new File(nuclosHome, "data"),
					new File(nuclosHome, "logs"),
					new File(ConfigContext.getFileProperty("server.tomcat.dir"), "logs"))) {
				if (!files.contains(file)) {
					File f = new File(file);
					if (f.isFile()) {
						cb.info("info.remove.file", f.getAbsolutePath());
						try {
							FileUtils.delete(f, true);
						}
						catch (Exception ex) {
							log.error(ex);
							cb.warn("error.update.remove", f.getAbsolutePath());
						}
					}
				}
			}

			register(cb, "true".equals(ConfigContext.getProperty(LAUNCH_STARTUP)));
		}
		catch (Exception ex) {
			log.error("Error", ex);
			throw new InstallException(ex.getMessage());
		}
	}

	@Override
	public void rollback(Installer cb) throws InstallException {
		throw new UnsupportedOperationException("rollback.not.supported");
	}

	@Override
	public void remove(Installer cb) throws InstallException {
		unregister(cb);
		File nuclosHome = ConfigContext.getCurrentConfig().getFileProperty(NUCLOS_HOME);
		try {
			cb.info("remove.step.remove");
			FileUtils.delete(new File(nuclosHome, "extra"), true);
			FileUtils.delete(new File(nuclosHome, "webapp"), true);
			FileUtils.delete(new File(nuclosHome, "tomcat"), true);
			File bin = new File(nuclosHome, "bin");
			for (File f : bin.listFiles()) {
				f.deleteOnExit();
			}
			bin.deleteOnExit();

			FileUtils.delete(new File(nuclosHome, "client"), true);
			FileUtils.delete(new File(nuclosHome, "nuclos.xml"), true);
			FileUtils.delete(new File(nuclosHome, "nuclos-version.properties"), true);

			if ("true".equals(ConfigContext.getProperty(UNINSTALL_REMOVEDATAANDLOGS))) {
				FileUtils.delete(new File(nuclosHome, "data"), true);
				FileUtils.delete(new File(nuclosHome, "logs"), true);
			}
		}
		catch (IOException ex) {
			throw new InstallException(ex);
		}
	}

	public abstract void installPostgres(Installer cb) throws InstallException;

	public abstract void register(Installer cb, boolean systemlaunch) throws InstallException;

	public abstract void unregister(Installer cb) throws InstallException;

	private void validateJavaHome(String javahome) throws InstallException {
		EnvironmentUtils.validateJavaHome(javahome);
	}

	private void validateNuclosHome(String nucloshome) throws InstallException {
		ConfigContext.setUpdate(false);
		File f = new File(nucloshome);
		try {
			if (!f.exists()) {
				if (!f.mkdirs()) {
					throw new InstallException("error.security.targetpath", nucloshome);
				}
			}
			if (f.isDirectory()) {
				File nuclosxml;
				if (FileUtils.isEmptyDir(f, "data", "logs", "webapp", "extensions")) {
					nuclosxml = new File(f, "nuclos.xml");
					try {
						nuclosxml.createNewFile();
					}
					catch (Exception ex) {
						log.error("Cannot create test file: ", ex);
						throw new InstallException("error.security.targetpath", nucloshome);
					}
					finally {
						if (nuclosxml.exists()) {
							nuclosxml.delete();
						}
					}
					return;
				}
				else {
					File nuclosXml = new File(f, NUCLOS_XML);
					if (!nuclosXml.isFile()) {
						throw new InstallException("validation.targetdir.not.empty", nucloshome);
					}
					else {
						try {
							FileUtils.touch(nuclosXml);
						}
						catch (IOException ex) {
							throw new InstallException("error.security.targetpath", nucloshome);
						}
					}

					// update mode, load settings and check if update can be performed
					ConfigContext.update(nuclosXml);
					if (isProductRegistered() && ! isPrivileged()) {
						throw new InstallException("error.update.privileg.installation", nucloshome);
					}
				}
			}
			else {
				throw new InstallException("validation.targetdir.not.directory", nucloshome);
			}
		}
		catch (SecurityException ex) {
			throw new InstallException("error.security.targetpath", nucloshome);
		}
		catch (IOException ex) {
			throw new InstallException("error.unknown", ex.getMessage());
		}

	}

	private void validateHttpPort(String port) throws InstallException {
		if (!"true".equals(ConfigContext.getProperty(HTTP_ENABLED))) {
			return;
		}

		if (port == null || port.isEmpty()) {
			throw new InstallException("validation.httpport.empty");
		}

		if (ConfigContext.isUpdate() && port.equals(ConfigContext.getPreviousConfig().get(HTTP_PORT))) {
			return;
		}

		try {
			int iport = Integer.parseInt(port);
			if (EnvironmentUtils.checkPortRange(iport)) {
				if (!EnvironmentUtils.checkPort(iport)) {
					if (!EnvironmentUtils.isWindows() && iport < 1024 && !isPrivileged()) {
						throw new InstallException("validation.httpport.binderror.unix", port);
					}
					else {
						throw new InstallException("validation.httpport.binderror.windows", port);
					}
				}
			}
			else {
				throw new InstallException("validation.port.invalid");
			}
		}
		catch (NumberFormatException ex) {
			throw new InstallException("validation.port.invalid");
		}
	}

	private void validateHttpsPort(String port) throws InstallException {
		if (!"true".equals(ConfigContext.getProperty(HTTPS_ENABLED))) {
			return;
		}

		if (port == null || port.isEmpty()) {
			throw new InstallException("validation.httpsport.empty");
		}

		if (ConfigContext.isUpdate() && port.equals(ConfigContext.getPreviousConfig().get(HTTPS_PORT))) {
			return;
		}

		try {
			int iport = Integer.parseInt(port);
			if (EnvironmentUtils.checkPortRange(iport)) {
				if (!EnvironmentUtils.checkPort(iport)) {
					if (!EnvironmentUtils.isWindows() && iport < 1024 && !isPrivileged()) {
						throw new InstallException("validation.httpport.binderror.unix", port);
					}
					else {
						throw new InstallException("validation.httpport.binderror.windows", port);
					}
				}
			}
			else {
				throw new InstallException("validation.port.invalid");
			}
		}
		catch (NumberFormatException ex) {
			throw new InstallException("validation.port.invalid");
		}
	}

	private void validateShutdownPort(String port) throws InstallException {
		if (port == null || port.isEmpty()) {
			throw new InstallException("validation.shutdownport.empty");
		}

		if (ConfigContext.isUpdate() && port.equals(ConfigContext.getPreviousConfig().get(SHUTDOWN_PORT))) {
			return;
		}

		try {
			int iport = Integer.parseInt(port);
			if (EnvironmentUtils.checkPortRange(iport)) {
				if (!EnvironmentUtils.checkPort(iport)) {
					if (!EnvironmentUtils.isWindows() && iport < 1024 && !isPrivileged()) {
						throw new InstallException("validation.httpport.binderror.unix", port);
					}
					else {
						throw new InstallException("validation.httpport.binderror.windows", port);
					}
				}
			}
			else {
				throw new InstallException("validation.port.invalid");
			}
		}
		catch (NumberFormatException ex) {
			throw new InstallException("validation.port.invalid");
		}
	}

	private void validateDbPort(String value) throws InstallException {
		if (value == null || value.isEmpty()) {
			throw new InstallException("validation.dbport.empty");
		}

		String httpport = ConfigContext.getProperty(HTTP_PORT);
		try {
			if (httpport != null && httpport.equals(value)) {
				throw new InstallException("validation.port.pgequalshttp");
			}

			if (!EnvironmentUtils.checkPort(Integer.parseInt(value)) && "install".equals(ConfigContext.getProperty(DATABASE_SETUP))) {
				throw new InstallException("validation.dbport.taken", value);
			}
		}
		catch (NumberFormatException ex) {
			throw new InstallException("validation.port.invalid");
		}
	}

	private void validateDriverJar(String value) throws InstallException {
		if (!"postgresql".equals(ConfigContext.getProperty(DATABASE_ADAPTER))) {
			if (ConfigContext.hasPropertyChanged(DATABASE_ADAPTER)) {
				if (value == null || value.isEmpty()) {
					throw new InstallException("validation.select.driverjar");
				}
				File f = new File(value);
				if (!f.isFile()) {
					throw new InstallException("validation.select.driverjar");
				}
			}
		}
	}

	private void validateKeystore(String value) throws InstallException {
		if ("true".equals(ConfigContext.getProperty(HTTPS_ENABLED))) {
			if (ConfigContext.hasPropertyChanged(HTTPS_ENABLED)) {
				if (value == null || value.isEmpty()) {
					throw new InstallException("validation.select.keystore");
				}
				File f = new File(value);
				if (!f.isFile()) {
					throw new InstallException("validation.select.keystore");
				}
			}
		}
	}

	protected int exec(List<String> command) throws InstallException {
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			return p.waitFor();
		}
		catch (Exception ex) {
			log.info(ex);
			return 1;
		}
	}

	protected int exec(List<String> command, String errormessage) throws InstallException {
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			return p.waitFor();
		}
		catch (Exception ex) {
			throw new InstallException(errormessage);
		}
	}

	/**
	 * Support for older nuclos version (2.6, 2.7)
	 */

	private boolean askMigration(Installer cb, String server, String path) {
		return cb.askQuestion("migrate.question", Installer.QUESTION_YESNO, Installer.ANSWER_YES, server, path) == Installer.ANSWER_YES;
	}

	private void migrateFromJBoss(Installer cb, File targetDocumentdir, File targetResourceDir, String jbosshome, String servername) throws InstallException, IOException {
		try{
			cb.info("migrate.jboss.data", targetDocumentdir.getAbsolutePath());
			if (targetDocumentdir.exists() && !FileUtils.isEmptyDir(targetDocumentdir, true)) {
				throw new InstallException("migrate.data.notempty");
			}

			File oldDataDir = new File(jbosshome + "/server/" + servername + "/documents");
			if (oldDataDir.exists()) {
				FileUtils.copyDirectory(oldDataDir, targetDocumentdir, cb);
			}

			File oldResourceDir = new File(jbosshome + "/server/" + servername + "/resource");
			if (oldResourceDir.exists()) {
				FileUtils.copyDirectory(oldResourceDir, targetResourceDir, cb);
			}

			if (cb.askQuestion("migrate.jboss.remove.question", Installer.QUESTION_YESNO, Installer.ANSWER_YES) == Installer.ANSWER_YES) {
				cb.info("migrate.jboss.remove");
				FileUtils.delete(new File(jbosshome + "/server/" + servername), true);
			}
		}
		catch (Exception ex) {
			log.error(ex);
			cb.warn("migrate.error", ex.getMessage());
		}
	}

	private void migrateFromExternalTomcat(Installer cb, File targetDatadir, String tomcathome, String servername) throws InstallException, IOException {
		try {
			cb.info("migrate.tomcat.data", targetDatadir.getAbsolutePath());
			if (targetDatadir.exists() && !FileUtils.isEmptyDir(targetDatadir, true)) {
				throw new InstallException("migrate.data.notempty");
			}

			File oldDataDir = new File(tomcathome + "/webapps/" + servername + "/WEB-INF/data");
			if (oldDataDir.exists()) {
				FileUtils.copyDirectory(oldDataDir, targetDatadir, cb);
			}

			if (cb.askQuestion("migrate.tomcat.remove.question", Installer.QUESTION_YESNO, Installer.ANSWER_YES) == Installer.ANSWER_YES) {
				cb.info("migrate.tomcat.remove");
				FileUtils.delete(new File(tomcathome + "/webapps/" + servername), true);
			}
		}
		catch (Exception ex) {
			log.error(ex);
			cb.warn("migrate.error", ex.getMessage());
		}
	}

	protected static void setupDatabase(final Installer cb) throws SQLException {
		cb.info("unpack.step.setupdb");
		PostgresDbSetup dbSetup = new PostgresDbSetup();
		String superuser = ConfigContext.getProperty(POSTGRES_SUPERUSER);
		String superpwd = ConfigContext.getProperty(POSTGRES_SUPERPWD);

		dbSetup.init(ConfigContext.getCurrentConfig(), superuser, superpwd);
		dbSetup.prepare();

		dbSetup.run(new DbSetup.Callback() {
			@Override
			public void message(SetupAction a) {
				if (a.getKind() == DbSetup.Kind.ERROR) {
					cb.error(a.getMessage());
				}
				else if (a.getKind() == DbSetup.Kind.WARN) {
					cb.warn(a.getMessage());
				}
				else {
					cb.info(a.getMessage());
				}
			}
		});
	}

	protected static List<String> createConfDirectory(Installer cb) throws IOException {
		List<String> result = new ArrayList<String>();

		File nuclosHome = ConfigContext.getFileProperty(NUCLOS_HOME);
		File conf = new File(nuclosHome, "conf");

		FileUtils.forceMkdir(conf);
		result.add(conf.getAbsolutePath());

		//Unpack configfiles
		result.add(FileUtils.copyFile(AbstractUnpacker.class.getClassLoader().getResourceAsStream("nuclos.xml"), new File(nuclosHome, "nuclos.xml"), cb));
		result.add(FileUtils.copyFile(AbstractUnpacker.class.getClassLoader().getResourceAsStream("jdbc.properties"), new File(conf, "jdbc.properties"), cb));
		result.add(FileUtils.copyFile(AbstractUnpacker.class.getClassLoader().getResourceAsStream("server.properties"), new File(conf, "server.properties"), cb));
		result.add(FileUtils.copyFile(AbstractUnpacker.class.getClassLoader().getResourceAsStream("quartz.properties"), new File(conf, "quartz.properties"), cb));
		result.add(FileUtils.copyFile(AbstractUnpacker.class.getClassLoader().getResourceAsStream("log4j.properties"), new File(conf, "log4j.properties"), cb));

		// Write settings
		PropUtils.replaceTextParameters(new File(nuclosHome, "nuclos.xml"), ConfigContext.getCurrentConfig(), "UTF-8");
		PropUtils.replacePropertyParameters(new File(conf, "jdbc.properties"), ConfigContext.getCurrentConfig());
		PropUtils.replacePropertyParameters(new File(conf, "server.properties"), ConfigContext.getCurrentConfig());
		PropUtils.replacePropertyParameters(new File(conf, "quartz.properties"), ConfigContext.getCurrentConfig());
		PropUtils.replacePropertyParameters(new File(conf, "log4j.properties"), ConfigContext.getCurrentConfig());

		return result;

	}

	protected static List<String> createDataAndLogsDirectory() throws IOException {
		List<String> result = new ArrayList<String>();

		File nuclosHome = ConfigContext.getFileProperty(NUCLOS_HOME);
		File datadir = new File(nuclosHome, "data");

		result.add(mkDir(datadir));
		result.add(mkDir(new File(datadir, "documents")));
		result.add(mkDir(new File(datadir, "resource")));
		result.add(mkDir(new File(datadir, "expimp")));
		result.add(mkDir(new File(datadir, "codegenerator")));
		result.add(mkDir(new File(datadir, "compiled-reports")));
		result.add(mkDir(new File(nuclosHome, "logs")));

		return result;
	}

	private static String mkDir(File dir) throws IOException {
		FileUtils.forceMkdir(dir);
		return dir.getAbsolutePath();
	}
}

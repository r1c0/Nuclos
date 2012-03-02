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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.util.EnvironmentUtils;
import org.nuclos.installer.util.FileUtils;

import at.jta.Key;
import at.jta.RegistryErrorException;
import at.jta.Regor;

/**
 * Unpacking actions for Windows operating systems
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class WindowsUnpacker extends AbstractUnpacker {

	private static final Logger LOG = Logger.getLogger(WindowsUnpacker.class);

	private static final String X86_INSTALLER = "postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-windows.exe";
	private static final String X64_INSTALLER = "postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-windows-x64.exe";

	private static final String HKLM_SOFTWARE_POSTGRES_INSTALLATIONS = "SOFTWARE\\PostgreSQL\\Installations";
	private static final String HKLM_SOFTWARE_POSTGRES_SERVICES = "SOFTWARE\\PostgreSQL\\Services";
	private static final String HKLM_UNINSTALL_ROOT = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\";
	private static final String HKLM_SERVICE_ROOT = "SYSTEM\\CurrentControlSet\\Services\\nuclos.";
	private static final String HKLM_SYSTEM_ARCHITECURE = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";

	private Boolean privileged = null;

	@Override
	public String getDefaultValue(String key) {
		if (NUCLOS_HOME.equals(key)) {
			return System.getenv("ProgramFiles") + "\\Nuclos";
		}
		else if (POSTGRES_PREFIX.equals(key)) {
			return System.getenv("ProgramFiles") + "\\PostgreSQL\\"  + Constants.POSTGRESQL_MAIN_VERSION;
		}
		else if (POSTGRES_DATADIR.equals(key)) {
			return System.getenv("ProgramFiles") + "\\PostgreSQL\\"  + Constants.POSTGRESQL_MAIN_VERSION + "\\data";
		}
		else if (POSTGRES_TABLESPACEPATH.equals(key)) {
			return System.getenv("ProgramFiles") + "\\PostgreSQL\\" + Constants.POSTGRESQL_MAIN_VERSION + "\\data";
		}
		else {
			return super.getDefaultValue(key);
		}
	}

	@Override
	public void validate(String key, String value) throws InstallException {
		if (JAVA_HOME.equals(key)) {
			super.validate(key, value);
			ConfigContext.setProperty(JAVA_JVMDLL, EnvironmentUtils.getJvmDll(value));
		}
		else {
			super.validate(key, value);
		}
	}

	@Override
	public void shutdown(Installer cb) throws InstallException {
		if (ConfigContext.isUpdate()) {
			if (isServiceInstalled() && isPrivileged() && isServerRunning()) {
				if (cb.askQuestion("question.stop.server", Installer.QUESTION_YESNO, Installer.ANSWER_YES) == Installer.ANSWER_YES) {
					try {
						Process p = Runtime.getRuntime().exec("net stop nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE));
						p.waitFor();
					}
					catch (Exception ex) {
						LOG.error("Error stopping server", ex);
						cb.warn("error.stop.server");
					}
				}
			}

			if (isServerRunning()) {
				if (cb.askQuestion("warn.server.running", Installer.QUESTION_OKCANCEL, Installer.ANSWER_CANCEL) == Installer.ANSWER_CANCEL) {
					cb.cancel();
				}
			}

			try {
				while (true) {
					try {
						String testfilename = ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\tomcat\\" + TOMCAT_VERSION + "\\bin\\bootstrap.jar";
						File testfile = new File(testfilename);
						if (testfile.exists()) {
							if (!testfile.delete()) {
								throw new InstallException();
							}
						}

						testfilename = ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\nuclos" + (isAmd64()?"x64":"x86") + ".exe";
						testfile = new File(testfilename);
						if (testfile.exists()) {
							if (!testfile.delete()) {
								throw new InstallException();
							}
						}
						return;
					}
					catch (Exception ex) {
						if (cb.askQuestion("warn.stop.server.lock", Installer.QUESTION_OKCANCEL, Installer.ANSWER_CANCEL) == Installer.ANSWER_CANCEL) {
							cb.cancel();
						}
					}
				}
			}
			catch (Exception ex) {
				LOG.error("Error stopping server", ex);
				cb.warn("error.stop.server");
			}
		}
	}

	@Override
	public void unpack(Installer cb) throws InstallException {
		super.unpack(cb);

		File nuclosHome = ConfigContext.getFileProperty(NUCLOS_HOME);
		File tomcatTarget = ConfigContext.getFileProperty(SERVER_TOMCAT_DIR);
		FileUtils.setExecutable(new File(nuclosHome, "bin"), ".*\\.bat");
		FileUtils.setExecutable(new File(tomcatTarget, "bin"), ".*\\.bat");
	}

	@Override
	public void startup(Installer cb) throws InstallException {
		try {
			if (isPrivileged() && isServiceInstalled()) {
				if (cb.askQuestion("question.start.server", Installer.QUESTION_YESNO, Installer.ANSWER_NO) == Installer.ANSWER_YES) {
					Process p = Runtime.getRuntime().exec("net start nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE));
					if (p.waitFor() != 0) {
						cb.warn("error.start.server");
					}
					else {
						if (!waitStartup(TIMEOUT_STARTUP)) {
							cb.warn("error.start.server");
						}
					}
				}
			}
		}
		catch (Exception ex) {
			LOG.error("Error starting server", ex);
			cb.warn("error.start.server");
		}
	}

	private URL getPostgresInstallerUrl() throws InstallException {
		URL result = null;
		if (!isAmd64()) {
			result = getClass().getClassLoader().getResource(X86_INSTALLER);
		}
		else if (isAmd64()) {
			result = getClass().getClassLoader().getResource(X64_INSTALLER);
			// on Windows we always could install 32 bit programs
			if (result == null) {
				result = getClass().getClassLoader().getResource(X86_INSTALLER);				
			}
		}
		LOG.info("Postgresql installer URL is " + result);
		return result;
	}

	private boolean isServiceInstalled() throws InstallException {
		try {
			Regor regor = new Regor();
			return regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_SERVICE_ROOT + ConfigContext.getProperty(NUCLOS_INSTANCE), Regor.KEY_READ) != null;
		}
		catch (Exception e) {
			throw new InstallException("error.registry.access");
		}
	}

	private boolean isAmd64() throws InstallException {
		try {
			Regor regor = new Regor();
			Key key = regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_SYSTEM_ARCHITECURE, Regor.KEY_READ);
			if (key != null) {
				String val = regor.readValueAsString(key, "PROCESSOR_ARCHITECTURE");
				if (val != null) {
					return val.contains("64");
				}
			}
			// expect x86 as default;
			return false;
		}
		catch (Exception e) {
			throw new InstallException("error.registry.access");
		}
	}

	@Override
	public List<PostgresService> getPostgresServices() {
		List<PostgresService> installedServices = new ArrayList<PostgresService>();
		try {
			Regor regor = new Regor();
			if (regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_SOFTWARE_POSTGRES_INSTALLATIONS) == null) {
				return Collections.emptyList();
			}
			LOG.info(HKLM_SOFTWARE_POSTGRES_INSTALLATIONS + " found.");

			String serviceId = null;
			String baseDirectory = null;
			String version = null;

			List<String> installations = regor.listKeys(Regor.HKEY_LOCAL_MACHINE, HKLM_SOFTWARE_POSTGRES_INSTALLATIONS);
			if (installations != null) {
				for (String instkeyname : installations) {
					LOG.info(instkeyname);
					// String instkeyname = (String) instkey;
					Key installation = regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_SOFTWARE_POSTGRES_INSTALLATIONS + "\\" + instkeyname);

					for (Object instval : regor.listValueNames(installation)) {
						String valuename = (String)instval;
						if ("Service ID".equals(valuename)) {
							serviceId = regor.readValueAsString(installation, valuename);
						}
						else if ("Base Directory".equals(valuename)) {
							baseDirectory = regor.readValueAsString(installation, valuename);
						}
						else if ("Version".equals(valuename)) {
							version = regor.readValueAsString(installation, valuename);
						}
					}

					if (version != null && serviceId != null) {
						Integer port = null;
						String superUser = null;
						String dataDirectory = null;

						Key service = regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_SOFTWARE_POSTGRES_SERVICES + "\\" + serviceId);
						for (Object servval : regor.listValueNames(service)) {
							String valuename = (String)servval;
							if ("Port".equals(valuename)) {
								port = Integer.decode(regor.readDword(service, valuename));
							}
							else if ("Database Superuser".equals(valuename)) {
								superUser = regor.readValueAsString(service, valuename);
							}
							else if ("Data Directory".equals(valuename)) {
								dataDirectory = regor.readValueAsString(service, valuename);
							}
						}
						PostgresService pgservice = new PostgresService();
						pgservice.serviceId = serviceId;
						pgservice.version = version;
						pgservice.port = port;
						pgservice.superUser = superUser;
						pgservice.baseDirectory = baseDirectory;
						pgservice.dataDirectory = dataDirectory;
						installedServices.add(pgservice);
						LOG.info("Service found: " + pgservice);
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error("Error listing postgresql services.", e);
			return Collections.emptyList();
		}
		Collections.sort(installedServices);
		return installedServices;
	}

	@Override
	public boolean isPrivileged() {
		if (privileged == null) {
			synchronized (this) {
				File f = null;
				try {
		            String protectedpath = System.getenv("ProgramFiles");
		            if (protectedpath == null) {
		            	// check for another option
		                privileged = false;
		            }
		            else {
		            	File ftest = null;
		            	try {
		            		ftest = File.createTempFile("nuclos_", ".txt", new File(protectedpath));
		            		privileged = true;
		            	}
		            	finally {
		            		if (ftest != null) {
		            			ftest.delete();
		            		}
		            	}
		            }
				}
	        	catch (Exception ex) {
	        		LOG.warn(ex);
	        		privileged = false;
	        	}
	        	finally {
	        		if (f != null) {
	        			try {
	        				f.delete();
	        			}
	        			catch (Exception ex) {
	        				LOG.warn(ex);
	        			}
	        		}
	        	}
			}
		}
		return privileged.booleanValue();
	}

	@Override
	public boolean canInstall() {
		return isPrivileged();
	}
	
	@Override
	public boolean isProductRegistered() {
		String uninstallname = "Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")";
		try {
			Regor regor = new Regor();
			if (regor.openKey(Regor.HKEY_LOCAL_MACHINE, HKLM_UNINSTALL_ROOT + uninstallname) != null) {
				return true;
			}
			else {
				return false;
			}
		} catch (RegistryErrorException e) {
			throw new RuntimeException("error.registry.access");
		}
	}

	@Override
	public boolean isPostgresBundled() {
		try {
			return isPrivileged() && getPostgresInstallerUrl() != null;
		}
		catch (InstallException ex) {
			LOG.error(ex);
			return false;
		}
	}

	@Override
	public void installPostgres(Installer cb) throws InstallException {
		// unjar bundled postgresql installer
		URL installerurl = getPostgresInstallerUrl();
		if (installerurl == null) {
			cb.error("error.postgresql.notbundled");
		}

		final File f;
		if (isAmd64()) {
			f = new File(ConfigContext.getProperty(NUCLOS_HOME) + "/postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-windows-x64.exe");
		}
		else {
			f = new File(ConfigContext.getProperty(NUCLOS_HOME) + "/postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-windows.exe");
		}
		f.deleteOnExit();
		try {
			FileUtils.copyInputStreamToFile(installerurl.openStream(), f, false);
		}
		catch(IOException e) {
			LOG.error("installPostgres failed: " + e, e);
			cb.error("error.unpack.postgresql.installer");
		}

		// Build process args, see http://www.enterprisedb.com/resources-community/pginst-guide
		List<String> command = Arrays.asList(
			f.getAbsolutePath(),
			"--mode", "unattended",
			"--prefix", ConfigContext.getProperty(POSTGRES_PREFIX),
			"--datadir", ConfigContext.getProperty(POSTGRES_DATADIR),
			"--serverport", ConfigContext.getProperty(DATABASE_PORT),
			"--superpassword", ConfigContext.getProperty(POSTGRES_SUPERPWD));
		LOG.info(command);

		InputStreamReader reader = null;
		try {
			// Note: This process requires UAC elevation on Windows Vista/7
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				reader = new InputStreamReader(p.getInputStream());
			    StringBuffer val = new StringBuffer();

			    int n = 0;
				char[] buffer = new char[1024];
	            while ((n = reader.read(buffer, 0, 1024)) > -1) {
	            	val.append(buffer, 0, n);
	            }
			    LOG.info("PostgreSQL Installation result: " + val);
			    cb.warn(val.toString());
			}
		}
		catch (Exception ex) {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.warn(e);
				}
			}
			LOG.error(ex);
			cb.warn("error.postgres.installation");
		}
	}

	@Override
	public void register(Installer cb, boolean systemlaunch) throws InstallException {
		try {
			LOG.info("Register uninstaller in registry...");
			cb.info("unpack.step.register.product");
			String uninstallname = "Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")";
	        String keyname = HKLM_UNINSTALL_ROOT + uninstallname;
	        String cmd = "\"" + ConfigContext.getProperty(JAVA_HOME) + "\\bin\\java.exe\" -jar \""
	        	+ ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\uninstaller.jar\" \""
	        	+ ConfigContext.getProperty(NUCLOS_HOME) + "\\nuclos.xml\"";
	        String appVersion = VersionInformation.getInstance().getVersion();
	        String appUrl = "http://www.nuclos.de";

	        Regor regor = new Regor();
	        Key uninstallkey = regor.openKey(Regor.HKEY_LOCAL_MACHINE, keyname);
	        if (uninstallkey == null) {
	        	uninstallkey = regor.createKey(Regor.HKEY_LOCAL_MACHINE, keyname);
	        	regor.closeKey(uninstallkey);
	        }
	        uninstallkey = regor.openKey(Regor.HKEY_LOCAL_MACHINE, keyname);
	        regor.saveValue(uninstallkey, "DisplayName", uninstallname);
	        regor.saveValue(uninstallkey, "UninstallString", cmd);
	        regor.saveValue(uninstallkey, "DisplayVersion", appVersion);
	        regor.saveValue(uninstallkey, "HelpLink", appUrl);
	        regor.saveValue(uninstallkey, "DisplayName", uninstallname);
	        regor.saveValue(uninstallkey, "InstallLocation", ConfigContext.getProperty(NUCLOS_HOME));
	        regor.saveValue(uninstallkey, "Publisher", "Novabit Informationssysteme GmbH");
	        regor.saveValue(uninstallkey, "DisplayIcon", ConfigContext.getProperty(NUCLOS_HOME) + "\\extra\\uninstaller.ico");
	        regor.closeKey(uninstallkey);
		}
		catch (Exception ex) {
			LOG.error(ex);
			throw new InstallException(ex);
        }

		cb.info("unpack.step.register.service");
		String startup = "true".equals(ConfigContext.getProperty(LAUNCH_STARTUP)) ? "auto" : "manual";
		if (!isServiceInstalled()) {
			try {
				String bin = ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\nuclos" + (isAmd64()?"x64":"x86") + ".exe";

				List<String> command = Arrays.asList(
					bin, "//IS//nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE),
					"--DisplayName=Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")",
					"--Description=Nuclos Server (Instanz: " + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")",
					"--Install=" + bin,
					"--Startup=" + startup);
				LOG.info(command);

				ProcessBuilder pb = new ProcessBuilder(command);
				Process p = pb.start();
				if (p.waitFor() != 0) {
					cb.error("error.install.service");
				}
			}
			catch (Exception ex) {
				LOG.error(ex);
				cb.error("error.install.service");
			}
		}

		try {
			String bin = ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\nuclos" + (isAmd64()?"x64":"x86") + ".exe";
			String tomcatdir = ConfigContext.getProperty(SERVER_TOMCAT_DIR);
			List<String> command = Arrays.asList(
				bin, "//US//nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE),
				"--Startup=" + startup,
				"--LogPrefix=nuclossrv",
				"--LogPath=" + ConfigContext.getProperty(NUCLOS_HOME) + "\\logs",
				"--Classpath=" + tomcatdir + "\\bin\\bootstrap.jar;" + tomcatdir + "\\bin\\tomcat-juli.jar;" + tomcatdir + "\\bin\\tomcat-juli.jar",
				"--StartClass=org.apache.catalina.startup.Bootstrap",
				"--StopClass=org.apache.catalina.startup.Bootstrap",
				"--StartParams=start",
				"--StopParams=stop",
				"--Jvm=" + ConfigContext.getProperty(JAVA_JVMDLL),
				"--JvmOptions=-Dcatalina.base=" + tomcatdir + ";-Dcatalina.home=" + tomcatdir + ";-Djava.endorsed.dirs=" + tomcatdir + "\\endorsed",
				"++JvmOptions=-Djava.io.tmpdir=" + tomcatdir + "\\temp;-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager;-Djava.util.logging.config.file=" + tomcatdir + "\\conf\\logging.properties",
				"--StartMode=jvm",
				"--StopMode=jvm",
				"--JavaHome=" + ConfigContext.getProperty(JAVA_HOME),
				"--JvmMs=256",
				"--JvmMx=" + ConfigContext.getProperty(HEAP_SIZE));
			LOG.info(command);

			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				cb.error("error.install.service");
			}
		}
		catch (Exception ex) {
			LOG.error(ex);
			cb.error("error.install.service");
		}
	}

	@Override
	public void unregister(Installer cb) throws InstallException {
		try {
			cb.info("remove.step.unregister.service");
			String bin = ConfigContext.getProperty(NUCLOS_HOME) + "\\bin\\nuclos" + (isAmd64()?"x64":"x86") + ".exe";

			List<String> command = Arrays.asList(
				bin, "//DS//nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE));
			LOG.info(command);

			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				cb.error(command.toString());
			}
		}
		catch (Exception ex) {
			cb.error("error.uninstall.service");
		}

		try {
			cb.info("remove.step.unregister.product");
			String uninstallname = "Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")";
	        String keyname = HKLM_UNINSTALL_ROOT + uninstallname;

	        Regor regor = new Regor();
	        if (regor.delKey(regor.HKEY_LOCAL_MACHINE, keyname) != Regor.ERROR_SUCCESS) {
	        	cb.error("error.uninstall.product");
	        }
		}
		catch (Exception ex) {
			LOG.error(ex);
			cb.error("error.uninstall.product");
        }
	}
}

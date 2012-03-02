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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.util.FileUtils;
import org.nuclos.installer.util.PropUtils;

public class MacUnpacker extends UnixoidUnpacker {

	private static final Logger LOG = Logger.getLogger(MacUnpacker.class);

	private static final String PG_NSTALLER_IMAGE = "postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-osx.dmg";

	@Override
	public String getDefaultValue(String key) {
		if (NUCLOS_HOME.equals(key)) {
			return "/Library/Nuclos";
		}
		else if (POSTGRES_PREFIX.equals(key)) {
			return "/Library/PostgreSQL/" + Constants.POSTGRESQL_MAIN_VERSION;
		}
		else if (POSTGRES_DATADIR.equals(key)) {
			return "/Library/PostgreSQL/" + Constants.POSTGRESQL_MAIN_VERSION + "/data";
		}
		else if (POSTGRES_TABLESPACEPATH.equals(key)) {
			return "/Library/PostgreSQL/"  + Constants.POSTGRESQL_MAIN_VERSION + "/data";
		}
		else if (JAVA_HOME.equals(key)) {
			return "/Library/Java/Home";
		}
		else {
			return super.getDefaultValue(key);
		}
	}

	@Override
	public void shutdown(Installer cb) throws InstallException {
		Integer port = Integer.parseInt(ConfigContext.getProperty(HTTP_PORT));
		try {
			if (isServiceInstalled() && isServerRunning()) {
				if (cb.askQuestion("question.stop.server", Installer.QUESTION_YESNO, Installer.ANSWER_YES) == Installer.ANSWER_YES) {
					try {
						Process p = Runtime.getRuntime().exec("launchctl stop org.nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE));
						p.waitFor();
					}
					catch (Exception ex) {
						LOG.error("Error stopping server", ex);
						cb.warn("error.stop.server");
					}
				}
			}

			if (!waitShutdown(TIMEOUT_SHUTDOWN)) {
				if (cb.askQuestion("warn.server.running", Installer.QUESTION_OKCANCEL, Installer.ANSWER_CANCEL) == Installer.ANSWER_CANCEL) {
					cb.cancel();
				}
			}
		}
		catch (Exception ex) {
			LOG.error("Failed to stop server", ex);
			cb.warn("error.stop.server");
		}
	}

	@Override
	public void startup(Installer cb) throws InstallException {
		try {
			if (isServiceInstalled()) {
				if (cb.askQuestion("question.start.server", Installer.QUESTION_YESNO, Installer.ANSWER_NO) == Installer.ANSWER_YES) {
					Process p = Runtime.getRuntime().exec("launchctl start org.nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE));
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
			LOG.error("Failed to start server", ex);
			cb.warn("error.start.server");
		}
	}

	private URL getPostgresInstallerUrl() throws InstallException {
		return getClass().getClassLoader().getResource(PG_NSTALLER_IMAGE);
	}

	@Override
	public boolean canInstall() {
		return isPrivileged();
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

		File f = new File(ConfigContext.getProperty(NUCLOS_HOME) + "/" + PG_NSTALLER_IMAGE);
		f.deleteOnExit();
		try {
			FileUtils.copyInputStreamToFile(installerurl.openStream(), f, false);
		}
		catch(IOException e) {
			LOG.error("installPostgres failed: " + e, e);
			cb.error("error.unpack.postgresql.installer");
		}
		f.setExecutable(true);

		// mount dmg
		mount(cb, f);

		// set installbuilder.sh filename
		File installbuilder = new File("/Volumes/PostgreSQl " + Constants.POSTGRESQL_FULL_VERSION 
				+ "/postgresql-" + Constants.POSTGRESQL_FULL_VERSION + "-osx.app/Contents/MacOS/installbuilder.sh");

		// Build process args, see http://www.enterprisedb.com/resources-community/pginst-guide
		List<String> command = Arrays.asList(
				installbuilder.getAbsolutePath(),
			"--mode", "unattended",
			"--prefix", ConfigContext.getProperty(POSTGRES_PREFIX),
			"--datadir", ConfigContext.getProperty(POSTGRES_DATADIR),
			"--serverport", ConfigContext.getProperty(DATABASE_PORT),
			"--superpassword", ConfigContext.getProperty(POSTGRES_SUPERPWD));
		LOG.info(command);

		InputStreamReader reader = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			int i = p.waitFor();
			reader = new InputStreamReader(p.getInputStream());
		    StringBuffer val = new StringBuffer();
		    int n = 0;
			char[] buffer = new char[1024];
            while ((n = reader.read(buffer, 0, 1024)) > -1) {
            	val.append(buffer, 0, n);
            }
		    LOG.info("PostgreSQL Installation result: " + val);
		    String result = val.toString();

		    // Simply check for two texts to ensure that we have the expected result
		    if (result.contains("Your shared memory configuration has been adjusted") && result.contains("Please reboot the system")) {
		    	// unmount dmg
				unmount(cb);
		    	cb.error("error.postgres.memory");
		    }
		    else {
		    	if (i != 0) {
		    		cb.warn(result);
				}
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

		// unmount dmg
		unmount(cb);
	}

	private void mount(Installer cb, File f) {
		List<String> command = Arrays.asList("hdiutil", "attach", f.getAbsolutePath());
		LOG.info(command);

		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				cb.error("error.postgres.mount");
			}
		}
		catch (Exception ex) {
			LOG.error(ex);
			cb.error("error.postgres.mount");
		}
	}

	private void unmount(Installer cb) {
		List<String> command = Arrays.asList("hdiutil", "detach", "/Volumes/PostgreSQl " + Constants.POSTGRESQL_FULL_VERSION);
		LOG.info(command);

		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				cb.warn("error.postgres.unmount");
			}
		}
		catch (Exception ex) {
			LOG.error(ex);
			cb.warn("error.postgres.unmount");
		}
	}

	@Override
	public void register(Installer cb, boolean systemlaunch) throws InstallException {
		super.register(cb, systemlaunch);

		cb.info("unpack.step.register.service");
		// create service (/Library/LaunchDaemons/org.nuclos.<instance>.plist)
		File serviceconfiguration = new File("/Library/LaunchDaemons/org.nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase() + ".plist");
		try {
			FileUtils.copyFile(new File(ConfigContext.getFileProperty(NUCLOS_HOME), "extra/template-launchd.plist"), serviceconfiguration, true, cb);
			PropUtils.replaceTextParameters(serviceconfiguration, ConfigContext.getCurrentConfig(), "UTF-8");
		} catch (IOException e) {
			LOG.error("Failed to install service.", e);
			cb.warn("error.install.service");
		}

		// load service by launchctl
		List<String> command = Arrays.asList("launchctl", "load", serviceconfiguration.getAbsolutePath());
		LOG.info(command);

		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			if (p.waitFor() != 0) {
				cb.warn("error.install.service");
			}
		}
		catch (Exception ex) {
			LOG.error("Failed to install service.", ex);
			cb.warn("error.install.service");
		}
	}

	@Override
	public void unregister(Installer cb) throws InstallException {
		if (isServiceInstalled()) {
			cb.info("remove.step.unregister.service");
			File serviceconfiguration = new File("/Library/LaunchDaemons/org.nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase() + ".plist");

			// unload service by launchctl
			List<String> command = Arrays.asList("launchctl", "unload", serviceconfiguration.getAbsolutePath());
			LOG.info(command);

			try {
				ProcessBuilder pb = new ProcessBuilder(command);
				Process p = pb.start();
				if (p.waitFor() != 0) {
					cb.warn("error.uninstall.service");
				}
			}
			catch (Exception ex) {
				LOG.error("Failed to uninstall service.", ex);
				cb.warn("error.uninstall.service");
			}

			// remove service (/Library/LaunchDaemons/org.nuclos.<instance>.plist)
			try {
				if (!serviceconfiguration.delete()) {
					cb.warn("error.uninstall.service");
				}
			}
			catch (SecurityException ex) {
				LOG.error("Failed to uninstall service.", ex);
				cb.warn("error.uninstall.service");
			}
		}
		super.unregister(cb);
	}

	private boolean isServiceInstalled() throws InstallException {
		try {
			File serviceconfiguration = new File("/Library/LaunchDaemons/org.nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase() + ".plist");
			return serviceconfiguration.exists();
		}
		catch (Exception e) {
			throw new InstallException(e.getMessage(), e);
		}
	}
}

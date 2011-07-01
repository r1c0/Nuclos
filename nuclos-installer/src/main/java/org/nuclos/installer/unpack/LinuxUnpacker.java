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
import org.nuclos.installer.InstallException;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.util.FileUtils;
import org.nuclos.installer.util.PropUtils;

public class LinuxUnpacker extends UnixoidUnpacker {

	private static final Logger log = Logger.getLogger(LinuxUnpacker.class);

	private static final String X86_INSTALLER = "postgresql-9.0.4-1-linux.bin";
	private static final String X64_INSTALLER = "postgresql-9.0.4-1-linux-x64.bin";

	@Override
	public String getDefaultValue(String key) {
		if (NUCLOS_HOME.equals(key)) {
			return "/opt/nuclos";
		}
		else if (POSTGRES_PREFIX.equals(key)) {
			return "/opt/PostgreSQL/9.0";
		}
		else if (POSTGRES_DATADIR.equals(key)) {
			return "/opt/PostgreSQL/9.0/data";
		}
		else if (POSTGRES_TABLESPACEPATH.equals(key)) {
			return "/opt/PostgreSQL/9.0/data";
		}
		else {
			return super.getDefaultValue(key);
		}
	}

	@Override
	public void shutdown(Installer cb) throws InstallException {
		try {
			if (isServiceInstalled() && isServerRunning()) {
				if (cb.askQuestion("question.stop.server", Installer.QUESTION_YESNO, Installer.ANSWER_YES) == Installer.ANSWER_YES) {
					try {
						Process p = Runtime.getRuntime().exec("/etc/init.d/nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE) + " stop");
						p.waitFor();
					}
					catch (Exception ex) {
						log.error("Error stopping server", ex);
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
			log.error("Failed to stop server", ex);
			cb.warn("error.stop.server");
		}
	}

	@Override
	public void startup(Installer cb) throws InstallException {
		try {
			if (isServiceInstalled()) {
				if (cb.askQuestion("question.start.server", Installer.QUESTION_YESNO, Installer.ANSWER_NO) == Installer.ANSWER_YES) {
					Process p = Runtime.getRuntime().exec("/etc/init.d/nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE) + " start");
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
			log.error("Failed to start server", ex);
			cb.warn("error.start.server");
		}
	}

	private URL getPostgresInstallerUrl() throws InstallException {
		if (!isAmd64() && getClass().getClassLoader().getResource(X86_INSTALLER) != null ) {
			return getClass().getClassLoader().getResource(X86_INSTALLER);
		}
		else if (isAmd64() && getClass().getClassLoader().getResource(X64_INSTALLER) != null) {
			return getClass().getClassLoader().getResource(X64_INSTALLER);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isPostgresBundled() {
		try {
			return isPrivileged() && getPostgresInstallerUrl() != null;
		}
		catch (InstallException ex) {
			log.error(ex);
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

		File f = new File(ConfigContext.getProperty(NUCLOS_HOME) + "/postgresql-9.0.3-1-linux.bin");
		f.deleteOnExit();
		try {
			FileUtils.copyInputStreamToFile(installerurl.openStream(), f, false);
		}
		catch(IOException e) {
			log.error(e);
			cb.error("error.unpack.postgresql.installer");
		}
		f.setExecutable(true);

		// Build process args, see http://www.enterprisedb.com/resources-community/pginst-guide
		List<String> command = Arrays.asList(
			f.getAbsolutePath(),
			"--mode", "unattended",
			"--prefix", ConfigContext.getProperty(POSTGRES_PREFIX),
			"--datadir", ConfigContext.getProperty(POSTGRES_DATADIR),
			"--serverport", ConfigContext.getProperty(DATABASE_PORT),
			"--superpassword", ConfigContext.getProperty(POSTGRES_SUPERPWD));
		log.info(command);

		InputStreamReader reader = null;
		try {
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
			    log.info("PostgreSQL Installation result: " + val);
			    cb.warn(val.toString());
			}
		}
		catch (Exception ex) {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
			log.error(ex);
			cb.warn("error.postgres.installation");
		}
	}

	@Override
	public void register(Installer cb, boolean systemlaunch) throws InstallException {
		super.register(cb, systemlaunch);

		cb.info("unpack.step.register.service");

		// create service (/etc/init.d/nuclos.<instance>)
		File servicecontroller = new File("/etc/init.d/nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
		try {
			FileUtils.copyFile(new File(ConfigContext.getFileProperty(NUCLOS_HOME), "extra/template-service.sh"), servicecontroller, true, cb);
			PropUtils.replaceTextParameters(servicecontroller, ConfigContext.getCurrentConfig(), "UTF-8");
			servicecontroller.setExecutable(true);
		} catch (IOException e) {
			log.error("Failed to install service.", e);
			cb.warn("error.install.service");
		}

		// create or remove runlevel entries /etc/rcX.d/) if launch on startup setting has changed
		if (ConfigContext.hasPropertyChanged(LAUNCH_STARTUP)) {
			if ("true".equals(ConfigContext.getProperty(LAUNCH_STARTUP))) {
				boolean success = false;

				// try "/sbin/chkconfig"
				List<String> command = Arrays.asList("/sbin/chkconfig", "--add", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
				log.info("Try chkconfig: " + command);

				success = (exec(command) == 0);

				if (!success) {
					// try "/usr/sbin/update-rc.d"
					command = Arrays.asList("/usr/sbin/update-rc.d", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase(), "defaults", "90", "10");
					log.info("Try update-rc.d: " + command);

					success = (exec(command) == 0);
				}

				if (!success) {
					cb.warn("error.install.service.autostart");
				}
			}
			else {
				if (ConfigContext.isUpdate()) {
					boolean success = false;

					// try "/sbin/chkconfig"
					List<String> command = Arrays.asList("/sbin/chkconfig", "--del", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
					log.info("Try chkconfig: " + command);

					success = (exec(command) == 0);

					if (!success) {
						// try "/usr/sbin/update-rc.d"
						command = Arrays.asList("/usr/sbin/update-rc.d", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase(), "remove");
						log.info("Try update-rc.d: " + command);

						success = (exec(command) == 0);
					}

					if (!success) {
						cb.warn("error.install.service.autostart");
					}
				}
			}
		}
	}

	@Override
	public void unregister(Installer cb) throws InstallException {
		cb.info("remove.step.unregister.service");
		if ("true".equals(ConfigContext.getProperty(LAUNCH_STARTUP))) {
			boolean success = false;

			// try "/sbin/chkconfig"
			List<String> command = Arrays.asList("/sbin/chkconfig", "--del", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
			log.info("Try chkconfig: " + command);

			success = (exec(command) == 0);

			if (!success) {
				// try "/usr/sbin/update-rc.d"
				command = Arrays.asList("/usr/sbin/update-rc.d", "nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase(), "remove");
				log.info("Try update-rc.d: " + command);

				success = (exec(command) == 0);
			}

			if (!success) {
				cb.warn("error.uninstall.service.autostart");
			}
		}

		if (isServiceInstalled()) {
			// remove service (/etc/init.d/nuclos.<instance>)
			File servicecontroller = new File("/etc/init.d/nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
			try {
				if (!servicecontroller.delete()) {
					cb.warn("error.uninstall.service");
				}
			}
			catch (SecurityException ex) {
				cb.warn("error.uninstall.service");
			}
		}
		super.unregister(cb);
	}

	private boolean isServiceInstalled() throws InstallException {
		try {
			File servicecontroller = new File("/etc/init.d/nuclos." + ConfigContext.getProperty(NUCLOS_INSTANCE).toLowerCase());
			return servicecontroller.exists();
		}
		catch (Exception e) {
			throw new InstallException(e.getMessage(), e);
		}
	}
}

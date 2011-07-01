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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.util.FileUtils;

/**
 * Unpacking actions for Unix-like operating systems (Linux, Mac OS X)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public abstract class UnixoidUnpacker extends AbstractUnpacker {

	private static final Logger log = Logger.getLogger(UnixoidUnpacker.class);

	protected static final String FILENAME_REGISTRY = "/etc/nuclos.ini";

	@Override
	public void unpack(Installer cb) throws InstallException {
		super.unpack(cb);
		File nuclosHome = ConfigContext.getFileProperty(NUCLOS_HOME);
		File tomcatTarget = ConfigContext.getFileProperty(SERVER_TOMCAT_DIR);
		FileUtils.setExecutable(new File(nuclosHome, "bin"), ".*\\.sh");
		FileUtils.setExecutable(new File(tomcatTarget, "bin"), ".*\\.sh");
	}

	@Override
	public boolean isPrivileged() {
		String username = System.getProperty("user.name");
		return (username == null ? false : username.equals("root"));
	}

	@Override
	public boolean isProductRegistered() {
		String name = ConfigContext.getProperty(NUCLOS_INSTANCE);
		String uninstallname = "Nuclos (" + name + ")";

		try {
			File registry = new File(FILENAME_REGISTRY);
			if (!registry.exists()) {
				return false;
			}
			else {
				FileInputStream fio = new FileInputStream(registry);
				Ini ini = new Ini(fio);
				boolean registered = ini.containsKey(uninstallname);
				fio.close();
				return registered;
			}
		}
		catch (Exception e) {
			throw new RuntimeException("error.registryfile.invalid");
		}
	}

	@Override
	public boolean isPostgresBundled() {
		return false;
	}

	private List<PostgresService> pgservices;

	@Override
	public synchronized List<PostgresService> getPostgresServices() {
		if (pgservices == null) {
			try {
				pgservices = new ArrayList<PostgresService>();
				File postgres_registration = new File("/etc/postgres-reg.ini");
				if (postgres_registration.exists()) {
					FileInputStream fio = new FileInputStream(postgres_registration);
					Ini ini = new Ini(fio);

					for (Section section : ini.values()) {
						if (section == null || section.getName() == null || section.getName().trim().isEmpty()) {
							continue;
						}

						PostgresService pgservice = new PostgresService();
						pgservice.serviceId = section.get("ServiceID", String.class);
						pgservice.version = section.get("Version", String.class);
						pgservice.port = section.get("Port", Integer.class);
						pgservice.superUser = section.get("Superuser", String.class);
						pgservice.baseDirectory = section.get("InstallationDirectory", String.class);
						pgservice.dataDirectory = section.get("DataDirectory", String.class);
						log.info("Service found: " + pgservice);
						pgservices.add(pgservice);
					}

					fio.close();
				}
			}
			catch (Exception ex) {
				log.error("Error listing postgresql services.", ex);
				throw new RuntimeException(ex);
			}
		}
		return pgservices;
	}

	@Override
	public void register(Installer cb, boolean systemlaunch) throws InstallException {
		try {
			cb.info("unpack.step.register.product");
			File nuclos_registration = new File(FILENAME_REGISTRY);
			if (!nuclos_registration.exists()) {
				nuclos_registration.createNewFile();
			}
			Ini ini = new Ini(nuclos_registration);
			String productname = "Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")";
			ini.put(productname, "Version", VersionInformation.getInstance().toString());
			ini.put(productname, "Path", ConfigContext.getProperty(NUCLOS_HOME));
			ini.store();
		}
		catch (Exception ex) {
			log.error(ex);
			throw new InstallException(ex);
		}
	}

	@Override
	public void unregister(Installer cb) throws InstallException {
		try {
			cb.info("remove.step.unregister.product");
			File nuclos_registration = new File(FILENAME_REGISTRY);
			if (nuclos_registration.exists()) {
				String productname = "Nuclos (" + ConfigContext.getProperty(NUCLOS_INSTANCE) + ")";
				Ini ini = new Ini(nuclos_registration);
				ini.remove(productname);
				ini.store();
			}
		}
		catch (Exception ex) {
			log.error(ex);
			throw new InstallException(ex);
		}
	}

	protected final boolean isAmd64() throws InstallException {
		BufferedReader reader = null;
		try {
			Process p = new ProcessBuilder(Arrays.asList("/bin/uname", "-m")).start();
			if (p.waitFor() == 0) {
			    reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			    String val = reader.readLine();
			    reader.close();
			    log.info("Machine is " + val);
			    return val.contains("64");
			}
			else {
				throw new InstallException();
			}
		}
		catch (Exception ex) {
			log.error(ex);
			throw new InstallException(ex);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}
}

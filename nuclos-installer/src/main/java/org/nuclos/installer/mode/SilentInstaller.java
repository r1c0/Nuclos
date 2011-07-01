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
package org.nuclos.installer.mode;

import java.io.Console;
import java.io.File;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.nuclos.installer.AbstractLauncher;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.L10n;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.unpack.Unpacker;

/**
 * <code>SilentInstaller</code> performs a silent installation like in Nuclos Versions below 3.0.<br>
 * This installation implementation is triggered by calling the installer with the parameter -s or --silent
 * Moreover, a configuration file needs to be supplied as last call parameter that is not prefixed by a hyphen. Example:<br>
 * <code>java -jar nuclos-installer-generic.jar -s nuclos.xml</code> <br>
 * <br>
 * The XML-file that is supplied by the parameter contains the installation configuration.<br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class SilentInstaller implements Installer, Constants {

	private static final Logger log = Logger.getLogger(SilentInstaller.class);

	private final Console console;

	private final PrintWriter writer;

	private Unpacker unpacker;

	private VersionInformation version = VersionInformation.getInstance();

	public SilentInstaller(Console console) {
		this.console = console;
		if (this.console != null) {
			this.writer = this.console.writer();
		} else {
			this.writer = new PrintWriter(System.out);
		}
	}

	@Override
	public void install(Unpacker os) throws InstallException {
		this.unpacker = os;
		info("cli.info.installer", version.getName(), version.getVersion(), version.getDate());
		File f = new File(AbstractLauncher.LOGFILENAME);
		info("gui.wizard.info.logfile", f.getAbsolutePath());

		String target = ConfigContext.getProperty(NUCLOS_HOME);
		try {
			unpacker.validate(NUCLOS_HOME, target);
		} catch (InstallException e) {
			error("error.installation.failed", e.getMessage());
		}

		if (ConfigContext.isUpdate()) {
			info("cli.info.update", target);
		} else {
			info("cli.info.newinstallation", target);
		}

		ConfigContext.getCurrentConfig().setDbDefaults(unpacker, DBOPTION_USE);

		for (Object key : ConfigContext.getCurrentConfig().keySet()) {
			try {
				String property = key.toString();
				unpacker.validate(property, ConfigContext.getProperty(property));
			} catch (InstallException ex) {
				log.error(ex.getMessage(), ex);
				error("error.installation.failed", ex.getMessage());
			}
		}

		try {
			ConfigContext.getCurrentConfig().verify();
		} catch (InstallException e) {
			error(e.getLocalizedMessage());
		}

		try {
			unpacker.unpack(this);
			info("installation.finished");
		} catch (InstallException ex) {
			error("error.installation.failed", ex.getMessage());
		}
	}

	@Override
	public void info(String message, Object... args) {
		log.info(L10n.getMessage(message, args));
		this.writer.println(L10n.getMessage(message, args));
		this.writer.flush();
	}

	@Override
	public void warn(String message, Object... args) {
		log.warn(L10n.getMessage(message, args));
		this.writer.println(L10n.getMessage(message, args));
		this.writer.flush();
		System.exit(1);
	}

	@Override
	public void error(String message, Object... args) {
		log.error(L10n.getMessage(message, args));
		this.writer.println(L10n.getMessage(message, args));
		this.writer.flush();
		System.exit(1);
	}

	@Override
	public int askQuestion(String text, int questiontype, int defaultanswer, Object...arg) {
		return defaultanswer;
	}

	@Override
	public void uninstall(Unpacker os) throws InstallException {
		os.remove(this);
	}

	@Override
	public void close() {
		System.exit(0);
	}

	@Override
	public void cancel() {
		System.exit(0);
	}

}

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

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.installer.AbstractLauncher;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.L10n;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.database.PostgresDbSetup;
import org.nuclos.installer.unpack.GenericUnpacker;
import org.nuclos.installer.unpack.Unpacker;

/**
 * CLI based interactive installation.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class CliInstaller implements Installer, Constants {

	private static final Logger LOG = Logger.getLogger(CliInstaller.class);

	private final Console console;

	private final PrintWriter writer;
	private final BufferedReader reader;

	private Unpacker unpacker;

	private VersionInformation version = VersionInformation.getInstance();

	public CliInstaller(Console console) {
		this.console = console;
		if (this.console != null) {
			this.writer = this.console.writer();
			this.reader = new BufferedReader(this.console.reader());
		}
		else {
			this.writer = new PrintWriter(System.out);
			this.reader = new BufferedReader(new InputStreamReader(System.in));
		}
	}

	@Override
	public void install(Unpacker unpacker) throws InstallException {
		this.unpacker = unpacker;
		info("cli.info.installer", version.getName(), version.getVersion(), version.getDate());
		File f = new File(AbstractLauncher.LOGFILENAME);
		info("gui.wizard.info.logfile", f.getAbsolutePath());

		getProperty(NUCLOS_HOME);
		String target = ConfigContext.getProperty(NUCLOS_HOME);

		if (ConfigContext.isUpdate()) {
			info("info.update.backup");
			ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_USE);
			unpacker.shutdown(this);
		}
		else {
			info("cli.info.newinstallation", target);
			ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_SETUP);
		}

		info("cli.step.database");
		getProperty(DATABASE_SETUP, DBOPTION_SETUP, DBOPTION_USE);
		ConfigContext.getCurrentConfig().setDbDefaults(unpacker, ConfigContext.getProperty(DATABASE_SETUP));

		if (DBOPTION_SETUP.equals(ConfigContext.getProperty(DATABASE_SETUP))) {
			boolean validconnection = false;
			while (!validconnection) {
				getProperty(DATABASE_SERVER);
				getProperty(DATABASE_PORT);
				getProperty(POSTGRES_SUPERUSER);
				getPasswordProperty(POSTGRES_SUPERPWD);

				PostgresDbSetup dbSetup = new PostgresDbSetup();
				String superuser = ConfigContext.getProperty(POSTGRES_SUPERUSER);
				String superpwd = ConfigContext.getProperty(POSTGRES_SUPERPWD);

				try {
					dbSetup.init(ConfigContext.getCurrentConfig(), superuser, superpwd);
					dbSetup.checkConnection();
					validconnection = true;
				}
				catch (Exception ex) {
					LOG.error("Cannot connect to database.", ex);
					validconnection = false;
				}
			}
			getProperty(DATABASE_NAME);
			getProperty(DATABASE_USERNAME);
			getPasswordProperty(DATABASE_PASSWORD);
			getProperty(DATABASE_SCHEMA);
			getProperty(DATABASE_TABLESPACE);
			getProperty(POSTGRES_TABLESPACEPATH);
		}
		else {
			getProperty(DATABASE_ADAPTER, "postgresql", "oracle", "mssql", "sybase");
			getProperty(DATABASE_SERVER);
			getProperty(DATABASE_PORT);
			getProperty(DATABASE_NAME);
			getProperty(DATABASE_SCHEMA);
			getProperty(DATABASE_USERNAME);
			getPasswordProperty(DATABASE_PASSWORD);
			getProperty(DATABASE_TABLESPACE);
			getProperty(DATABASE_TABLESPACEINDEX);
		}

		info("cli.step.server");
		getProperty(JAVA_HOME);
		getProperty(NUCLOS_INSTANCE);

		if (ConfigContext.isUpdate()) {
			ConfigContext.setProperty("temp.choose.protocol", "true".equals(ConfigContext.getProperty(HTTP_ENABLED)) ? "http" : "https");
		}
		else {
			ConfigContext.setProperty("temp.choose.protocol", "http");
		}

		getProperty("temp.choose.protocol", "http", "https");

		if ("http".equals(ConfigContext.getProperty("temp.choose.protocol"))) {
			ConfigContext.setProperty(HTTP_ENABLED, "true");
			ConfigContext.setProperty(HTTPS_ENABLED, "false");
			getProperty(HTTP_PORT);
		}
		else {
			ConfigContext.setProperty(HTTP_ENABLED, "false");
			ConfigContext.setProperty(HTTPS_ENABLED, "true");
			getProperty(HTTPS_PORT);
			getProperty(HTTPS_KEYSTORE_FILE);
			getPasswordProperty(HTTPS_KEYSTORE_PASSWORD);
		}

		getProperty(SHUTDOWN_PORT);
		getProperty(HEAP_SIZE);
		if (!(unpacker instanceof GenericUnpacker)) {
			getProperty(LAUNCH_STARTUP, "true", "false");
		}

		info("cli.step.client");
		getProperty(CLIENT_SINGLEINSTANCE, "true", "false");

		try {
			ConfigContext.getCurrentConfig().setDerivedProperties();
			ConfigContext.getCurrentConfig().verify();
		}
		catch (InstallException e) {
			LOG.error("Validation of installation settings failed.", e);
			error("error.installation.failed", e.getLocalizedMessage());
		}

		try {
			unpacker.unpack(this);
			unpacker.startup(this);
			info("installation.finished");
		}
		catch (InstallException ex) {
			LOG.error("Installation failed.", ex);
			error("error.installation.failed", ex.getLocalizedMessage());
		}
	}

	private void getProperty(String property, String...aValues) {
		List<String> values = aValues == null ? new ArrayList<String>() : Arrays.asList(aValues);
		while (true) {
			String value = readLine("cli.property." + property, ConfigContext.getProperty(property));
			if (value == null || value.isEmpty()) {
				value = ConfigContext.containsKey(property) ? ConfigContext.getProperty(property) : "";
			}

			if (values.size() > 0 && !values.contains(value)) {
				info("cli.enum.error", values.toString());
			}
			else {
				try {
					unpacker.validate(property, value);
					ConfigContext.setProperty(property, value);
					return;
				}
				catch (InstallException ex) {
					info(ex.getLocalizedMessage());
				}
			}
		}
	}

	private void getPasswordProperty(String property) {
		while (true) {
			char[] password = readPassword("cli.property." + property, ConfigContext.getProperty(property));
			String value;
			if (password == null || password.length == 0) {
				value = ConfigContext.containsKey(property) ? ConfigContext.getProperty(property) : "";
			}
			else {
				value = new String(password);
			}
			try {
				unpacker.validate(property, value);
				ConfigContext.setProperty(property, value);
				return;
			}
			catch (InstallException ex) {
				info(ex.getLocalizedMessage());
			}
		}
	}

	@Override
	public void info(String message, Object... args) {
		LOG.info(L10n.getMessage(message, args));
		this.writer.println(L10n.getMessage(message, args));
		this.writer.flush();
	}

	@Override
	public void warn(String message, Object... args) {
		LOG.warn(L10n.getMessage(message, args));
		if (askQuestion(L10n.getMessage(message, args) + System.getProperty("line.separator") + L10n.getMessage("question.continue"), QUESTION_YESNO, ANSWER_NO) == ANSWER_NO) {
			System.exit(1);
		}
	}

	@Override
	public void error(String message, Object... args) {
		LOG.error(L10n.getMessage(message, args));
		this.writer.println(L10n.getMessage(message, args));
		this.writer.flush();
		System.exit(1);
	}

	public String readLine(String message, Object... args) {
		this.writer.write(L10n.getMessage(message, args));
		this.writer.flush();
		try {
			return reader.readLine();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public char[] readPassword(String message, Object... args) {
		if (console != null) {
			return console.readPassword(L10n.getMessage(message, args));
		}
		else {
			String s = readLine(message, args);
			if (s != null) {
				return s.toCharArray();
			}
			else {
				return null;
			}
		}
	}

	@Override
	public int askQuestion(String text, int questiontype, int defaultanswer, Object...args) {
		while (true) {
			this.writer.print(L10n.getMessage(text, args));
			if (QUESTION_YESNO == questiontype) {
				this.writer.print("(Y/N)");
			}
			if (QUESTION_OKCANCEL == questiontype) {
				this.writer.print(L10n.getMessage("cli.question.okcancel"));
			}
			this.writer.flush();
			String line;
			try {
				line = this.reader.readLine();
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
			if (QUESTION_YESNO == questiontype) {
				if ("y".equalsIgnoreCase(line)) {
					return ANSWER_YES;
				} else if ("n".equalsIgnoreCase(line)) {
					return ANSWER_NO;
				}
			}
			if (QUESTION_OKCANCEL == questiontype) {
				if ("0".equals(line)) {
					return ANSWER_CANCEL;
				} else if ("1".equals(line)) {
					return ANSWER_OK;
				}
			}
			this.writer.print(L10n.getMessage("cli.question.invalidanswer"));
			this.writer.flush();
		}
	}

	@Override
	public void uninstall(Unpacker os) throws InstallException {
		os.shutdown(this);
		getProperty(UNINSTALL_REMOVEDATAANDLOGS, "true", "false");
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

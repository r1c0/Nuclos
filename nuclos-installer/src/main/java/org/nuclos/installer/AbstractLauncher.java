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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.nuclos.installer.icons.InstallerIcons;
import org.nuclos.installer.mode.CliInstaller;
import org.nuclos.installer.mode.GuiInstaller;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.mode.SilentInstaller;
import org.nuclos.installer.unpack.GenericUnpacker;
import org.nuclos.installer.unpack.LinuxUnpacker;
import org.nuclos.installer.unpack.MacUnpacker;
import org.nuclos.installer.unpack.Unpacker;
import org.nuclos.installer.unpack.WindowsUnpacker;
import org.nuclos.installer.util.EnvironmentUtils;

public abstract class AbstractLauncher {

	private static final Logger LOG = Logger.getLogger(AbstractLauncher.class);

	private List<String> args;

	public static final String LOGFILENAME = System.getProperty("java.io.tmpdir") + "/nuclos-installer_" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm")).format(Calendar.getInstance().getTime()) + ".log";

	protected void run(String[] argArray) {
		args = new ArrayList<String>(Arrays.asList(argArray));
		if (args.contains("-h") || args.contains("--help")) {
			help();
			return;
		}

		System.setProperty("nuclos.installer.logfile", LOGFILENAME);
		PropertyConfigurator.configure(Main.class.getClassLoader().getResource("org/nuclos/installer/log4j.properties"));

		try {
			Unpacker u = getUnpacker();
			final Installer i = getInstaller();

			if (EnvironmentUtils.isMac()) {
				try {
					Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
					Object macAppObject = macAppClass.getConstructor().newInstance();
					// set Nuclos dock icon
					macAppClass.getMethod("setDockIconImage", java.awt.Image.class).invoke(macAppObject, InstallerIcons.getFrameIcon().getImage());

					// register quit handler
		            Class<?> macQuitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
		            Method macAppSetQuitHandlerMethod = macAppClass.getDeclaredMethod("setQuitHandler", new Class[] { macQuitHandlerClass });

		            Object macQuitHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macQuitHandlerClass }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
							if (method != null && "handleQuitRequestWith".equals(method.getName()) && args.length == 2) {
								i.cancel();
							}
							return null;
						}
					});
		            macAppSetQuitHandlerMethod.invoke(macAppObject, new Object[] { macQuitHandler });
				}
				catch (Exception e) {
					// Ok! (tp)
					e.printStackTrace();
					LOG.error("run failed: " + e, e);
				}
			}
			run(i, u);
		}
		catch (InstallException e) {
			// Ok! (tp)
			e.printStackTrace();
			LOG.error("run failed: " + e, e);
		}
	}

	private Unpacker getUnpacker() throws InstallException {
		if (args.contains("-d") || args.contains("--dev")) {
			return new GenericUnpacker();
		}

		if (EnvironmentUtils.isWindows()) {
			return new WindowsUnpacker();
		}
		else if (EnvironmentUtils.isLinux()) {
			return new LinuxUnpacker();
		}
		else if (EnvironmentUtils.isMac()) {
			return new MacUnpacker();
		}
		else {
			return new GenericUnpacker();
		}
	}

	private Installer getInstaller() throws InstallException {
		File nuclosXml = null;
		if (!args.isEmpty()) {
			String lastArg = args.get(args.size() - 1);
			if (!lastArg.startsWith("-")) {
				nuclosXml = new File(args.remove(args.size() - 1));
			}
		}

		if (nuclosXml != null) {
			try {
				ConfigContext.setTemplate(nuclosXml);
			}
			catch (Exception ex) {
				throw new InstallException("error.read.template", ex);
			}
		}

		Installer installer;
		if (GraphicsEnvironment.isHeadless()) {
			if (args.contains("--cli") || args.contains("-c")) {
				installer = new CliInstaller(System.console());
			}
			else if (args.contains("--silent") || args.contains("-s")) {
				installer = new SilentInstaller(System.console());
			}
			else {
				installer = new CliInstaller(System.console());
			}
		}
		else {
			if (args.contains("--gui") || args.contains("-g")) {
				installer = new GuiInstaller();
			}
			else if (args.contains("--cli") || args.contains("-c")) {
				installer = new CliInstaller(System.console());
			}
			else if (args.contains("--silent") || args.contains("-s")) {
				installer = new SilentInstaller(System.console());
			}
			else {
				installer = new GuiInstaller();
			}
		}
		return installer;
	}

	protected void help() {
		PrintWriter writer;
		if (System.console() != null) {
			writer = System.console().writer();
		}
		else {
			writer = new PrintWriter(System.out);
		}
		VersionInformation version = VersionInformation.getInstance();

		writer.println(L10n.getMessage("cli.info.installer", version.getName(), version.getVersion(), version.getDate()));
		writer.println();
		writer.println("Usage: java -jar <this filename> [-options] [template-file, i.e. nuclos.xml from target installation path]");
		writer.println();
		writer.println("where options include:");
		writer.println("    --help    or -h: this usage description");
		writer.println("    --gui     or -g: graphical interactive mode");
		writer.println("    --cli     or -c: command line (headless) interactive mode");
		writer.println("    --silent  or -s: command line (headless) silent mode");
		writer.println("    --dev     or -d: development mode (no OS-specific integration or registration)");
	}

	public abstract void run(Installer i, Unpacker u) throws InstallException;

}

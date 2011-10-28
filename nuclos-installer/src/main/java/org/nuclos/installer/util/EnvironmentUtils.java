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
package org.nuclos.installer.util;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.installer.InstallException;

public class EnvironmentUtils {

	private static final Logger LOG = Logger.getLogger(EnvironmentUtils.class);

	public static void validateJavaHome(String javahome) throws InstallException {
		javahome = new File(javahome).getAbsolutePath();

		if (!isMac()) {
			// TODO find a way to check for Mac OS X? no rt.jar, tools.jar etc.

			boolean isJDK = false;
			File rtJar = new File(javahome, "jre/lib/rt.jar");
			if (rtJar.exists()) {
				// jre subfolder is part of JDK, test lib/tools.jar to be sure
				isJDK = new File(javahome, "lib/tools.jar").exists();
			} else {
				rtJar = new File(javahome, "lib/rt.jar");
			}

			if (!rtJar.exists()) {
				throw new InstallException("Java home does not valid Java runtime: " + javahome);
			}

			Manifest manifest;
			try {
				manifest = FileUtils.extractManifest(rtJar);
			} catch(IOException e) {
				throw new InstallException("Error accessing " + rtJar.getAbsolutePath());
			}

			String implVersion = manifest.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION);
			if (implVersion == null) {
				throw new InstallException("Unknown Java version: " + javahome);
			}

			Matcher m = Pattern.compile("(\\d\\.\\d\\.\\d)(?:_(\\d+))(-.+)?").matcher(implVersion);
			boolean valid = m.matches() && "1.6.0".equals(m.group(1)) && Integer.parseInt(m.group(2)) >= 10;
			if (!valid) {
				throw new InstallException("Invalid Java version: " + javahome + " is " + implVersion);
			}
		}
	}

	public static String getJvmDll(String javahome) {
		boolean isJDK = false;
		File rtJar = new File(javahome, "jre/lib/rt.jar");
		if (rtJar.exists()) {
			isJDK = new File(javahome, "lib/tools.jar").exists();
		} else {
			rtJar = new File(javahome, "lib/rt.jar");
		}

		File jrebase = new File(javahome);
		if (isJDK) {
			jrebase = new File(jrebase, "jre");
		}
		File jvmdll = new File(jrebase, "bin/server/jvm.dll");
		if (!jvmdll.exists()) {
			jvmdll = new File(jrebase, "bin/client/jvm.dll");
		}
		if (!jvmdll.exists()) {
			return "auto";
		}
		else {
			return jvmdll.getAbsolutePath();
		}
	}

	public static boolean checkPortRange(int port) {
		return (port >= 0 && port <= 65535);
	}

	public static boolean checkPort(int port) {
		boolean ok = true;
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		}
		catch (IOException e) {
		    ok = false;
		}
		finally {
		    if (socket != null) {
		    	try {
					socket.close();
				}
				catch(IOException e) {
					// Ok! (tp)
					e.printStackTrace();
					LOG.error("checkPort failed: " + e, e);
				}
		    }
		}
		return ok;
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("win") > -1;
	}

	public static boolean isMac() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("mac") > -1;
	}

	public static boolean isLinux() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("nux") > -1;
	}

	public static boolean isUnix() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("nix") > -1 || osName.indexOf("nux") > -1;
	}
}

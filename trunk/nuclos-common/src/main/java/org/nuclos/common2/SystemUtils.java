//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.common2;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class SystemUtils {

	private static final Logger LOG = Logger.getLogger(SystemUtils.class);

	public static enum OS {
		WINDOWS, LINUX, MAC, OTHER;
	}

	private static final List<String> LINUX_PDF;

	private static final List<String> LINUX_BROWSER;

	static {
		List<String> list = new ArrayList<String>();
		list.add("acroreader");
		list.add("evince");
		list.add("okular");
		list.add("sensible-browser");
		list.add("firefox");
		list.add("google-chrome");
		list.add("chromium-browser");
		list.add("konqueror");
		LINUX_PDF = Collections.unmodifiableList(list);

		list = new ArrayList<String>();
		list.add("sensible-browser");
		list.add("firefox");
		list.add("google-chrome");
		list.add("chromium-browser");
		list.add("konqueror");
		LINUX_BROWSER = Collections.unmodifiableList(list);
	}

	private SystemUtils() {
		// Never invoked.
	}

	public static OS getOsName() {
		final OS os;
		final String name = System.getProperty("os.name").toLowerCase();
		if (name.indexOf("windows") > -1) {
			os = OS.WINDOWS;
		} else if (name.indexOf("linux") > -1) {
			os = OS.LINUX;
		} else if (name.indexOf("mac") > -1) {
			os = OS.MAC;
		} else {
			os = OS.OTHER;
		}
		return os;
	}

	/**
	 * Try to open a file.
	 * @throws IOException 
	 */
	public static void open(String filename) throws IOException {
		final File file = new File(filename);
		open(file);
	}

	public static void open(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		file = file.getCanonicalFile();
		if (!file.canRead()) {
			throw new IOException("No permissions to read " + file);
		}
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} else {
			openWithoutDesktop(file);
		}
	}

	private static void openWithoutDesktop(File file) throws IOException {
		String[] exe = getExecArrayForOpen(file);
		if (exe != null && exe.length > 0) {
			LOG.info("trying to exec " + Arrays.asList(exe));
			Runtime.getRuntime().exec(exe);
		} else {
			LOG.info("Don't know how to openWithoutDesktop " + file);
		}
	}

	private static String[] getExecArrayForOpen(File file) {
		String[] result = null;
		final OS os = getOsName();
		switch (os) {
		case WINDOWS:
			result = new String[] { "cmd", "/c", file.toString() };
			break;
		case LINUX:
			final String c1 = findExecutableFor(os, file);
			if (c1 != null) {
				result = new String[] { c1, file.toString() };
			}
			break;
		case MAC:
		case OTHER:
		default:
		}
		return result;
	}

	private static String findExecutableFor(OS os, File f) {
		final String filename = f.toString();
		List<String> exe = null;
		switch (os) {
		case LINUX:
			if (filename.endsWith(".pdf")) {
				exe = LINUX_PDF;
			}
			// always try an browser at the end
			else {
				exe = LINUX_BROWSER;
			}
			break;
		case MAC:
		case OTHER:
		default:
		}

		if (exe != null) {
			return findExecutableFrom(os, exe);
		} else {
			LOG.info("Unable to find exe for " + os + " " + f);
			return null;
		}
	}

	private static String findExecutableFrom(OS os, List<String> list) {
		String result = null;
		File f = null;
		for (String s : list) {
			switch (os) {
			case LINUX:
				f = new File("/usr/bin", s);
				break;
			case WINDOWS:
			case MAC:
			case OTHER:
			default:
			}
			if (f != null) {
				if (f.canExecute()) {
					result = f.toString();
					break;
				}
			}
			f = null;
		}
		return result;
	}

}

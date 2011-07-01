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

import java.io.File;
import java.io.FileInputStream;

public class ConfigContext implements Constants {

	private static final ConfigContext singleton = new ConfigContext();

	private Config previousconfig = new Config();
	private Config currentconfig = new Config();

	private boolean update = false;
	private boolean template = false;

	private ConfigContext() { }

	private static ConfigContext getInstance() {
		return singleton;
	}

	public static void update(File nuclosxml) throws InstallException {
		try {
			getPreviousConfig().loadFromXML(new FileInputStream(nuclosxml));
			if (!isTemplate()) {
				getCurrentConfig().putAll(ConfigContext.getPreviousConfig());
			}
			getInstance().update = true;
		}
		catch (Exception ex) {
			throw new InstallException("error.load.settings", nuclosxml.getAbsolutePath());
		}
	}

	public static boolean containsKey(String property) {
		return getCurrentConfig().containsKey(property);
	}

	public static String getProperty(String property) {
		return getCurrentConfig().getProperty(property);
	}

	public static File getFileProperty(String property) {
		return new File(getProperty(property));
	}

	public static void setProperty(String property, String value) {
		getCurrentConfig().setProperty(property, value);
	}

	public static Config getCurrentConfig() {
		return getInstance().currentconfig;
	}

	public static Config getPreviousConfig() {
		return getInstance().previousconfig;
	}

	public static boolean isUpdate() {
		return getInstance().update;
	}

	public static void setUpdate(boolean value) {
		getInstance().update = value;
	}

	public static boolean hasPropertyChanged(String property) {
		if (!isUpdate()) {
			return true;
		}
		else {
			Object p1 = getPreviousConfig().getProperty(property);
			Object p2 = getCurrentConfig().getProperty(property);
			return (p1 == null) ? (p2 != null) : !p1.equals(p2);
		}
	}

	public static void setTemplate(File nuclosxml) {
		try {
			getCurrentConfig().loadFromXML(new FileInputStream(nuclosxml));
			getInstance().template = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isTemplate() {
		return getInstance().template;
	}
}

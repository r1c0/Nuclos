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

import java.io.IOException;
import java.util.Properties;

public class VersionInformation {

	private static VersionInformation singleton = new VersionInformation();

	private final Properties versionproperties;

	private VersionInformation() {
		versionproperties = new Properties();
		try {
			versionproperties.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static VersionInformation getInstance() {
		return singleton;
	}

	public String getName() {
		return versionproperties.getProperty("nuclos.name");
	}

	public String getVersion() {
		return versionproperties.getProperty("nuclos.version.number");
	}

	public String getDate() {
		return versionproperties.getProperty("nuclos.version.date");
	}

	@Override
	public String toString() {
		return getName() + " " + getVersion() + " (" + getDate() + ")";
	}
}

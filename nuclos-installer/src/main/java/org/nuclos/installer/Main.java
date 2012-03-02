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

import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.unpack.GenericUnpacker;
import org.nuclos.installer.unpack.Unpacker;

public class Main extends AbstractLauncher {

	@Override
	public void run(Installer i, Unpacker u) throws InstallException {
		System.setProperty("jdbc.drivers", "org.postgresql.Driver");
		if (!(u instanceof GenericUnpacker) && !u.isPrivileged()) {
			i.warn("warn.privileged.access");
			if (!u.canInstall()) {
				u = new GenericUnpacker(u);
			}
		}
		ConfigContext.getCurrentConfig().setDefaults(u);
		i.install(u);
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Nuclos Installer " + VersionInformation.getInstance().getVersion());
		Main main = new Main();
		main.run(args);
	}
}

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

import org.nuclos.installer.mode.GuiInstaller;
import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.unpack.DevUnpacker;
import org.nuclos.installer.unpack.Unpacker;

public class Dev {

	public static void main(String[] args) throws Exception {
		Installer i = new GuiInstaller();
		Unpacker u = new DevUnpacker();
		i.install(u);
	}
}

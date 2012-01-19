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
package org.nuclos.client.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

/**
 * Static utility methods for desktop integration functionality.
 *
 * @author thomas.schiffmann
 */
public class DesktopUtils {

	private static final Logger LOG = Logger.getLogger(DesktopUtils.class);

	public static void open(File f) throws IOException {
		if (Desktop.isDesktopSupported()) {
			LOG.debug(MessageFormat.format("Open file {0} via java.awt.Desktop.", f.getName()));
			Desktop.getDesktop().open(f);
		} else {
			if (!System.getProperty("os.name").startsWith("Windows")) {
				throw new UnsupportedOperationException("CollectableDocumentFileChooserBase.4");
			}
			final String sCommand = "cmd.exe /c \"\"" + f.getAbsolutePath() + "\"\"";
			LOG.debug("Start external process: " + sCommand);
			Runtime.getRuntime().exec(sCommand);
			LOG.debug("Finished external process.");
		}
	}
}

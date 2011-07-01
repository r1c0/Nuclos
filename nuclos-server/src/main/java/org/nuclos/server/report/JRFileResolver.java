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
package org.nuclos.server.report;

import java.io.File;

import net.sf.jasperreports.engine.util.FileResolver;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.NuclosSystemParameters;

/**
* File resolver for report execution.
* Tries to load files from resource folder.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
public class JRFileResolver implements FileResolver {

	@Override
	public File resolveFile(String arg0) {
		String filename = arg0.substring(LangUtils.max(arg0.lastIndexOf('\\'), arg0.lastIndexOf('/')) + 1);

		File file = new File(NuclosSystemParameters.getString(NuclosSystemParameters.RESOURCE_PATH), filename);
		if (file.exists()) {
			return file;
		}
		else {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("report.exception.filenotresolved", filename));
		}
	}
}
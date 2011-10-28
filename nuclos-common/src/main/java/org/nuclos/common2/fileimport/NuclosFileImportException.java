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
package org.nuclos.common2.fileimport;

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Class for file import exceptions
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version	01.00.00
 */
public class NuclosFileImportException extends CommonBusinessException {

	public NuclosFileImportException(String sMessage) {
		super(sMessage);
	}

	public NuclosFileImportException(String sMessage, Throwable cause) {
		super(sMessage, cause);
	}

	public NuclosFileImportException(Throwable cause) {
		super(cause);
	}

}

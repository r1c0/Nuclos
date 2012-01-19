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
package org.nuclos.server.genericobject;

import org.nuclos.common.NuclosBusinessException;

/**
 * Exception to be thrown on unparseable invoices.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">Uwe Allner</a>
 * @version	00.01.000
 */
public class GenericObjectImportParseException extends NuclosBusinessException {

	private static final String sDefaultMessage = "goimport.parse.exception";

	public GenericObjectImportParseException() {
		super(sDefaultMessage);
	}

	/**
	 * @param tCause wrapped exception
	 */
	public GenericObjectImportParseException(Throwable tCause) {
		super(sDefaultMessage, tCause);
	}

	/**
	 * @param sMessage exception message
	 */
	public GenericObjectImportParseException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public GenericObjectImportParseException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}
}

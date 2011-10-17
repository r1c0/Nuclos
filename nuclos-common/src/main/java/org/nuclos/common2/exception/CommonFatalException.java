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
package org.nuclos.common2.exception;

import javax.ejb.EJBException;

/**
 * General Novabit runtime (fatal, unchecked) exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version	01.00.00
 */

public class CommonFatalException extends RuntimeException {

	public CommonFatalException() {
		super();
	}

	/**
	 * @param sMessage exception message
	 */
	public CommonFatalException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public CommonFatalException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

	/**
	 * @param tCause wrapped exception
	 */
	public CommonFatalException(Throwable tCause) {
		// If the cause is already a Novabit-Exception, the default cause.toString()
		// garbles the embedded resource id. So we use your own message extraction method.
		super(getMessage(tCause), tCause);
	}

	protected static String getMessage(Throwable cause) {
		Throwable t = cause;
		if (t instanceof EJBException && t.getCause() != null) {
			t = t.getCause();
		}
		if (t.getMessage() != null && (t instanceof CommonBusinessException || t instanceof CommonFatalException)) {
			return t.getMessage();
		}
		return cause.toString();
	}

}

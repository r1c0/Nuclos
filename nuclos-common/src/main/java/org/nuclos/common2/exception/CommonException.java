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

/**
 * General base exception for all Novabit applications.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version	01.00.00
 * @deprecated This class is currently unused. It may be resurrected as an interface.
 * @todo CommonException should be an interface, so CommonBusinessException
 * and CommonFatalException can be derived from CommonException.
 * @todo A better alternative would be to merge CommonBusinessException with CommonException
 * and to rename CommonFatalException to NovabitRuntimeException - in accordance to java.lang.
 * The real distinction is not between fatal and nonfatal, as that is something the catcher must
 * decide, not the thrower. The real distinction on (exception) class level is between checked and unchecked.
 */

@Deprecated
class CommonException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CommonException() {
		super();
	}

	/**
	 * @param sMessage exception message
	 */
	private CommonException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	private CommonException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

	/**
	 * @param tCause wrapped exception
	 */
	private CommonException(Throwable tCause) {
		super(tCause);
	}
}

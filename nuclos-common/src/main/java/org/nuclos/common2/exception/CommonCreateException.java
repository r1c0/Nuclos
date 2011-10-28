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
 * Wrapper exception for create exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version	00.01.000
 */

public class CommonCreateException extends CommonBusinessException {

	public CommonCreateException() {
		super(CommonBusinessException.CREATE);
	}

	/**
	 * @param tCause wrapped exception
	 */
	public CommonCreateException(Throwable tCause) {
		super(CommonBusinessException.CREATE, tCause);
	}

	/**
	 * @param sMessage exception message
	 */
	public CommonCreateException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public CommonCreateException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}
}

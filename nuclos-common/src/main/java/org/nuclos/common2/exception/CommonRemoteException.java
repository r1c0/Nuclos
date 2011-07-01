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
 * Wraps a checked <code>RemoteException</code> in an unchecked <code>CommonFatalException</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonRemoteException extends CommonFatalException {
	public CommonRemoteException() {
		this((Throwable) null);
	}

	public CommonRemoteException(Throwable tCause) {
		this(CommonBusinessException.REMOTE, tCause);
	}

	public CommonRemoteException(String sMessage) {
		this(sMessage, null);
	}

	public CommonRemoteException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

}  // class CommonRemoteException

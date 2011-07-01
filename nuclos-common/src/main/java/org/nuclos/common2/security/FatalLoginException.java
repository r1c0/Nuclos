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
package org.nuclos.common2.security;

import javax.security.auth.login.LoginException;

/**
 * A fatal <code>LoginException</code>. JAAS doesn't provide a possibility to distinguish fatal login errors
 * from non-fatal errors. This class is meant for technical errors like "server down" or "database down" etc.
 * in contrast to "bad password".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class FatalLoginException extends LoginException {
	private final Throwable tCause;

	public FatalLoginException(String sMessage) {
		this(sMessage, null);
	}

	public FatalLoginException(Throwable tCause) {
		this(tCause.getMessage(), tCause);
	}

	public FatalLoginException(String sMessage, Throwable tCause) {
		super(sMessage);
		this.tCause = tCause;
	}

	@Override
	public Throwable getCause() {
		return this.tCause;
	}
}

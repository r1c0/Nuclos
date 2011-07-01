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
package org.nuclos.common.security;

import javax.security.auth.login.LoginException;

public class NuclosLoginException extends LoginException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static Integer DEFAULT_ERROR_CODE = 0;
	public final static Integer AUTHENTICATION_ERROR = 1;
	public final static Integer AUTHORISATION_ERROR = 2;
	public final static Integer COMMUNICATION_ERROR = 3;

	private final Integer _error_code;

	public NuclosLoginException(String msg) {
		super(msg);
		_error_code = DEFAULT_ERROR_CODE;
	}

	public NuclosLoginException(String msg, Integer error_code) {
		super(msg);
		_error_code = error_code;
	}

	public Integer getErrorCode(){
		return _error_code;
	}
}

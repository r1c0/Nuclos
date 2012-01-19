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
package org.nuclos.installer;

public class InstallException extends Exception {

	private static final long serialVersionUID = 9109849795289470063L;

	private Object[] args;

	public InstallException() { }

	public InstallException(String message) {
		super(message);
	}

	public InstallException(String message, Object... args) {
		super(message);
		this.args = args;
	}

	public InstallException(Throwable cause) {
		super(cause);
	}

	public InstallException(String message, Throwable cause) {
		super(cause);
	}

	@Override
	public String getLocalizedMessage() {
		return L10n.getMessage(getMessage(), args);
	}
}

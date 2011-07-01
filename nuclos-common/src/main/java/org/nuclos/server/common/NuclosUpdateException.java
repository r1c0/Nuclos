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
package org.nuclos.server.common;

import org.nuclos.common.NuclosBusinessException;

/**
 * Wrapper exception for create exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 00.01.000
 */
public class NuclosUpdateException extends NuclosBusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NuclosUpdateException() {
		super("common.error.exception.nucleusupdateexception");
	}

	/**
	 * @param tCause wrapped exception
	 */
	public NuclosUpdateException(Throwable tCause) {
		super("common.error.exception.nucleusupdateexception", tCause);
	}

	/**
	 * @param sMessage exception message
	 */
	public NuclosUpdateException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public NuclosUpdateException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}
}

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
package org.nuclos.common;

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * General Nucleus business (non-fatal) exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class NuclosBusinessException extends CommonBusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NuclosBusinessException() {
		super("common.error.exception.nucleusbusinessexception");
		//super(NuclosServerResources.getString("common.error.exception.nucleusbusinessexception"));
	}

	/**
	 * @param tCause wrapped exception
	 */
	public NuclosBusinessException(Throwable tCause) {
		//@todo: super( tCause);
		super("common.error.exception.nucleusbusinessexception", tCause);
		//super(NuclosServerResources.getString("common.error.exception.nucleusbusinessexception"), tCause);
	}

	/**
	 * @param sMessage exception message
	 */
	public NuclosBusinessException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public NuclosBusinessException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

}	// class NuclosBusinessException

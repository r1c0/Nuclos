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

import java.util.Set;

import org.nuclos.common.validation.FieldValidationError;


/**
 * General exception for validation of values.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version	00.01.000
 */
public class CommonValidationException extends CommonBusinessException {

	private Set<String> errors;

	private Set<FieldValidationError> fielderrors;

	public CommonValidationException() {
		super(CommonBusinessException.VALIDATION);
	}

	/**
	 * @param tCause wrapped exception
	 */
	public CommonValidationException(Throwable tCause) {
		super(CommonBusinessException.VALIDATION, tCause);
	}

	/**
	 * @param sMessage exception message
	 */
	public CommonValidationException(String sMessage) {
		super(sMessage, null);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public CommonValidationException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

	public CommonValidationException(Set<String> errors, Set<FieldValidationError> fielderrors) {
		super(CommonBusinessException.VALIDATION);
		this.errors = errors;
		this.fielderrors = fielderrors;
	}

	public Set<String> getErrors() {
		return errors;
	}

	public Set<FieldValidationError> getFieldErrors() {
		return fielderrors;
	}

	@Override
	public String toString() {
		return "CommonValidationException [getMessage()=" + getMessage() + ", errors=" + errors + ", fielderrors=" + fielderrors + "]";
	}
}

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
package org.nuclos.server.ruleengine;

import org.nuclos.common.NuclosBusinessException;

/**
 * Base exception for all business exceptions occurring within a rule.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class NuclosBusinessRuleException extends NuclosBusinessException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String originMessage;

	public NuclosBusinessRuleException() {
		super("ruleengine.error.exception.nucleusbusinessruleexception");
		originMessage = null;
	}

	/**
	 * @param tCause wrapped exception
	 */
	public NuclosBusinessRuleException(Throwable tCause) {
		super(tCause.getMessage() == null ? "ruleengine.error.exception.nucleusbusinessruleexception" : tCause.getMessage(), tCause);
		originMessage = null;
	}

	/**
	 * @param sMessage exception message
	 */
	public NuclosBusinessRuleException(String sMessage) {
		super(sMessage);
		originMessage = null;
	}
	
	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public NuclosBusinessRuleException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
		originMessage = null;
	}

	/**
	 * @param sMessage exception message
	 * @param originMessage (without rule name and line)
	 * @param tCause wrapped exception
	 */
	public NuclosBusinessRuleException(String sMessage, String originMessage, Throwable tCause) {
		super(sMessage, tCause);
		this.originMessage = originMessage;
	}
	
	public String getOriginMessage() {
		return this.originMessage;
	}
	
	/**
	 * 
	 * @param ex
	 * @return
	 */
	public static NuclosBusinessRuleException extractNuclosBusinessRuleExceptionIfAny(Exception ex) {
		if (ex instanceof NuclosBusinessRuleException) {
			return (NuclosBusinessRuleException) ex;
		} else if (ex.getCause() != null && ex.getCause() instanceof Exception) {
			return extractNuclosBusinessRuleExceptionIfAny((Exception)ex.getCause());
		} else {
			return null;
		}
	}
	
	public static String extractOriginFromNuclosBusinessRuleExceptionIfAny(Exception ex) {
		if (ex instanceof NuclosBusinessRuleException && ((NuclosBusinessRuleException) ex).getOriginMessage() != null) {
			return ((NuclosBusinessRuleException) ex).getOriginMessage();
		} else if (ex.getCause() != null && ex.getCause() instanceof Exception) {
			return extractOriginFromNuclosBusinessRuleExceptionIfAny((Exception)ex.getCause());
		} else {
			return null;
		}
	}
}

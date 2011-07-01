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
package org.nuclos.server.dblayer;

import java.io.ObjectStreamException;
import java.sql.SQLException;

import org.nuclos.common.NuclosFatalException;

/**
 * Exception class for database-specific (fatal) exceptions. 
 */
public class DbException extends NuclosFatalException {

   private static final long serialVersionUID = 1L;
	
	private int errorCode;
	// A transient copy of the real SQLException
	private transient SQLException sqlException;
	
	public DbException(String message) {
		super(message);
	}
	
	public DbException(String message, Throwable cause) {
		super(message != null ? message : cause.getMessage());
		if (cause != null)
			initCause(cause);
	}
	
	public DbException(Exception cause) {
		super(cause != null ? cause.getMessage() : null);
		initCause(cause);
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	public SQLException getSQLException() {
		return sqlException;
	}
	
	@Override
	public Throwable getCause() {
		Throwable cause = super.getCause();
		return (cause != null) ? cause : sqlException;
	
	}	
	@Override
	public synchronized Throwable initCause(Throwable cause) {
		// Because of serialization troubles with driver-specific SQLExceptions, 
		// we never store the original SQLException as cause.  Instead we make
		// a plain copy (without chained exceptions etc. but with the original
		// stacktrace).
		if (cause instanceof SQLException) {
			sqlException = ((SQLException) cause);
			errorCode = sqlException.getErrorCode();
			cause = new SQLException(sqlException.getMessage(), sqlException.getSQLState(), sqlException.getErrorCode());
			cause.setStackTrace(sqlException.getStackTrace());
		}
		super.initCause(cause);
		return this;
	}

	private Object readResolve() throws ObjectStreamException {
		if (sqlException == null && (getCause() instanceof SQLException)) {
			sqlException = (SQLException) getCause();
		}
		return this;
	}
}

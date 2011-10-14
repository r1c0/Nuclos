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

import java.sql.SQLException;
import java.util.List;

import org.nuclos.common.dal.exception.DalBusinessException;

/**
 * Exception class for database-specific (fatal) exceptions. 
 */
public class DbException extends DalBusinessException {

	private Long id;
	
	private List<String> statements;
	
	private int errorCode;
	
	public DbException(String message) {
		this(null, message, null, null);
	}

	public DbException(Long id, String message, Throwable cause) {
		this(id, message, null, cause);
	}

	public DbException(Long id, String message, List<String> statements) {
		this(id, message, statements, null);
	}
	
	public DbException(Long id, String message, List<String> statements, Throwable cause) {
		super(message, cause);
		this.id = id;
		this.statements = statements;
		initCause(cause);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setIdIfNull(Long id) {
		if (this.id != null) {
			this.id = id;
		}
	}

	public List<String> getStatements() {
    	return statements;
    }
	
	public void setStatementsIfNull(List<String> statements) {
		if (this.statements == null) {
			this.statements = statements;
		}
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	@Override
	public synchronized Throwable initCause(Throwable cause) {
		// Because of serialization troubles with driver-specific SQLExceptions, 
		// we never store the original SQLException as cause.  Instead we make
		// a plain copy (without chained exceptions etc. but with the original
		// stacktrace).
		if (cause instanceof SQLException) {
			final SQLException sqlException = ((SQLException) cause);
			errorCode = sqlException.getErrorCode();
			cause = new SQLException(sqlException.getMessage(), sqlException.getSQLState(), sqlException.getErrorCode());
			cause.setStackTrace(sqlException.getStackTrace());
		}
		super.initCause(cause);
		return this;
	}

}

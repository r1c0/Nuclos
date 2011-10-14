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
package org.nuclos.common.dal;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.dal.exception.DalBusinessException;
import org.nuclos.server.dblayer.DbException;

public class DalCallResult implements Serializable {

	private static final int OKAY = 1;
	private static final int HAS_EXCEPTION = 0;
	
	private int resultType = OKAY;
	
	private List<DalBusinessException> lstException;
		
	/*
	public void addBusinessException(Long id, String message, List<String> statements, SQLException e) {
		resultType = HAS_EXCEPTION;
		if (lstException == null) {
			lstException = new ArrayList<DalBusinessException>();
		}
		lstException.add(new DbException(id, message, statements, e));
	}
	 */
	
	public void addBusinessException(Long id, List<String> statements, SQLException e) {
		resultType = HAS_EXCEPTION;
		if (lstException == null) {
			lstException = new ArrayList<DalBusinessException>();
		}
		lstException.add(new DbException(id, getReadableMessage(e), statements, e));
	}
	
	public void addBusinessException(DbException e) {
		resultType = HAS_EXCEPTION;
		if (lstException == null) {
			lstException = new ArrayList<DalBusinessException>();
		}
		lstException.add(e);
	}
	
	protected String getReadableMessage(SQLException ex) {
		return ex.toString();
	}

	public List<DalBusinessException> getExceptions() {
		return lstException;
	}
	
	public void throwFirstBusinessExceptionIfAny() throws NuclosBusinessException {
		if (hasException()) {
			throw new NuclosBusinessException(
				getExceptions().get(0).getMessage(),
				getExceptions().get(0));
		}
	}
	
	public boolean isOkay() {
		return resultType == OKAY;
	}
	
	public boolean hasException() {
		return resultType == HAS_EXCEPTION;
	}
}

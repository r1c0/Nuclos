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
import org.nuclos.server.dblayer.DbException;

/**
 * A wrapper class for a sequence of DbException.
 * <p>
 * Hence a type like List<DalCallResult> is strongly discouraged.
 * </p>
 */
public class DalCallResult implements Serializable {

	private static final int OKAY = 1;
	private static final int HAS_EXCEPTION = 0;
	
	private int resultType = OKAY;
	
	private List<DbException> lstException;
	
	private int numberOfDbChanges = -1;
	
	public DalCallResult() {
	}
	
	public int getNumberOfDbChanges() {
		return numberOfDbChanges;
	}
	
	public void addToNumberOfDbChanges(int changes) {
		if (numberOfDbChanges < 0) {
			numberOfDbChanges = changes;
		}
		else {
			numberOfDbChanges += changes;
		}
	}
	
	public void add(DalCallResult other) {
		if (other.hasException()) {
			_add();
			lstException.addAll(other.lstException);
		}
	}
	
	private void _add() {
		resultType = HAS_EXCEPTION;
		if (lstException == null) {
			lstException = new ArrayList<DbException>();
		}
	}
		
	public void addBusinessException(Long id, List<String> statements, SQLException e) {
		_add();
		lstException.add(new DbException(id, getReadableMessage(e), statements, e));
	}
	
	public void addBusinessException(DbException e) {
		_add();
		lstException.add(e);
	}
	
	protected String getReadableMessage(SQLException ex) {
		return ex.toString();
	}

	public List<DbException> getExceptions() {
		return lstException;
	}
	
	/**
	 * @deprecated Use {@link #throwFirstException()} instead.
	 */
	public void throwFirstAsBusinessException() throws NuclosBusinessException {
		if (hasException()) {
			throw new NuclosBusinessException(
				getExceptions().get(0).getMessage(),
				getExceptions().get(0));
		}
	}
	
	public void throwFirstException() throws DbException {
		if (hasException()) {
			throw getExceptions().get(0);
		}
	}
	
	public boolean isOkay() {
		return resultType == OKAY;
	}
	
	public boolean hasException() {
		return resultType == HAS_EXCEPTION;
	}
	
	@Override 
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("DalCallResult[ok=" + isOkay()).append(", changes=").append(numberOfDbChanges);
		if (lstException != null && !lstException.isEmpty()) {
			result.append(" " + lstException.size() + " db exceptions:\n");
			for (DbException e: lstException) {
				result.append("\n").append(e).append("\n");
			}
		}
		return result.toString();
	}
}

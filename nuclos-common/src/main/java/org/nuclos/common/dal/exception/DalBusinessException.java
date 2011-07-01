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
package org.nuclos.common.dal.exception;

import java.io.Serializable;
import java.util.List;

public class DalBusinessException extends Exception implements Serializable{
	private static final long serialVersionUID = 1012920874365879641L;
	private final Long id;
	private final List<String> statements;
	
	public DalBusinessException(Long id, String message) {
		this(id, message, (Throwable) null);
	}

	public DalBusinessException(Long id, String message, Throwable cause) {
		this(id, message, null, cause);
	}

	public DalBusinessException(Long id, String message, List<String> statements) {
		this(id, message, statements, null);
	}
	
	public DalBusinessException(Long id, String message, List<String> statements, Throwable cause) {
		super(message, cause);
		this.id = id;
		this.statements = statements;
	}
	
	public Long getId() {
		return id;
	}

	public List<String> getStatements() {
    	return statements;
    }	
}

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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * DbException class thrown when several statements are executed in a batch.
 */
public class DbBatchException extends DbException {
	
	private final SortedMap<Integer, DbException> exceptions;
	
	public DbBatchException(String message) {
		super(message);
		this.exceptions = new TreeMap<Integer, DbException>();
	}
	
	public DbBatchException(SortedMap<Integer, DbException> exceptions) {
		super(getMessageText(exceptions));
		this.exceptions = exceptions;
	}

	public DbBatchException(Map<Integer, DbException> exceptions) {
		this(new TreeMap<Integer, DbException>(exceptions));
	}
	
	public DbException getFirstException() {
		if (!exceptions.isEmpty()) {
			return exceptions.get(exceptions.firstKey());
		} else {
			return null; 
		}
	}

	private static String getMessageText(SortedMap<Integer, DbException> exceptions) {
		if (exceptions.isEmpty()) {
			return null;
		} else {
			Integer index = exceptions.firstKey();
			if (exceptions.size() == 1) {
				return exceptions.get(index).getMessage();
			} else {
				return String.format("%s (#%d), and %d more exceptions", exceptions.get(index), index, exceptions.size()-1);
			}
		}
	}
}

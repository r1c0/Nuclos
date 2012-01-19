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

import java.io.Serializable;
import java.util.Date;

import org.nuclos.common2.InternalTimestamp;

public class NuclosDateTime extends InternalTimestamp implements Serializable, NuclosAttributeExternalValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NuclosDateTime(long millis) {
		super(millis);
	}
	
	public static NuclosDateTime toNuclosDateTime(Date date) {
		if (date == null) {
			return null;
		} else {
			return new NuclosDateTime(date.getTime());
		}
	}

}

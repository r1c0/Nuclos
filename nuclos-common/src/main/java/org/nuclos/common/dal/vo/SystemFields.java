//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.common.dal.vo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common2.InternalTimestamp;

/**
 * Constants for fields common to all Nuclos entities.
 * 
 * @see org.nuclos.common.NuclosEOField
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class SystemFields {
	
	public static final String CHANCHED_AT = "changedAt";
	public static final String CHANCHED_AT_TYPE = "org.nuclos.common2.InternalTimestamp";
	public static final Class<?> CHANCHED_AT_CLASS = InternalTimestamp.class;
	
	public static final String CHANCHED_BY = "changedBy";
	public static final String CHANCHED_BY_TYPE = "java.lang.String";
	public static final Class<?> CHANCHED_BY_CLASS = String.class;
	
	public static final String CREATED_AT = "createdAt";
	public static final String CREATED_AT_TYPE = "org.nuclos.common2.InternalTimestamp";
	public static final Class<?> CREATED_AT_CLASS = InternalTimestamp.class;
	
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_BY_TYPE = "java.lang.String";
	public static final Class<?> CREATED_BY_CLASS = String.class;
	
	public static final String VERSION = "version";
	public static final String VERSION_TYPE = "java.lang.Integer";
	public static final Class<?> VERSION_CLASS = Integer.class;
	
	public static final String ID = "id";
	// ???
	public static final String ID_TYPE = "java.lang.Long";
	// ???
	public static final Class<?> ID_CLASS = Long.class;
	
	public static final Map<String,String> FIELDS2TYPES_MAP;
	
	static {
		final Map<String,String> map = new HashMap<String, String>();
		map.put(CHANCHED_AT, CHANCHED_AT_TYPE);
		map.put(CHANCHED_BY, CHANCHED_BY_TYPE);
		map.put(CREATED_AT, CREATED_BY_TYPE);
		map.put(CREATED_BY, CREATED_BY_TYPE);
		map.put(ID, ID_TYPE);
		map.put(VERSION, VERSION_TYPE);
		
		FIELDS2TYPES_MAP = Collections.unmodifiableMap(map);
	}
	
	private SystemFields() {
		// Never invoked.
	}

}

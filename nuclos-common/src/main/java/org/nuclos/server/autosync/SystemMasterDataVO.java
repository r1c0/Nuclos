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
package org.nuclos.server.autosync;

import java.util.Date;
import java.util.Map;

import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class SystemMasterDataVO extends MasterDataVO {
	
	private static final Date SYSDATE = new Date();
	private static final String NUCLOS = "NUCLOS";

	public static final Date CREATED_DATE = SYSDATE;
	public static final String CREATED_USER = NUCLOS;
	public static final Date CHANGED_DATE = SYSDATE;
	public static final String CHANGED_USER = NUCLOS;
	public static final int VERSION = 1;	
	
	SystemMasterDataVO(String entity, Object id, Map<String, Object> fields) {
		super(entity, id, CREATED_DATE, CREATED_USER, CHANGED_DATE, CHANGED_USER, VERSION, fields, true);
	}
}

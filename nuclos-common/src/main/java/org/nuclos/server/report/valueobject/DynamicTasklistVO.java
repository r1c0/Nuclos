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
package org.nuclos.server.report.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class DynamicTasklistVO extends DatasourceVO {
	
	public DynamicTasklistVO(NuclosValueObject evo, String sName,
		String sDescription, String sEntity, Boolean bValid, String sDatasourceXML, Integer nucletId) {
		super(evo, sName, sDescription, bValid, sDatasourceXML, nucletId, PERMISSION_NONE);
	}

	public DynamicTasklistVO(String sName, String sDescription,
		String sDatasourceXML, String sEntity, Boolean bValid, Integer nucletId) {
		super(sName, sDescription, sDatasourceXML, bValid, nucletId);
	}
}

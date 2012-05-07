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

import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;

import static org.nuclos.server.autosync.SystemMasterDataVO.CHANGED_DATE;
import static org.nuclos.server.autosync.SystemMasterDataVO.CHANGED_USER;
import static org.nuclos.server.autosync.SystemMasterDataVO.CREATED_DATE;
import static org.nuclos.server.autosync.SystemMasterDataVO.CREATED_USER;
import static org.nuclos.server.autosync.SystemMasterDataVO.VERSION;

public class SystemMasterDataMetaFieldVO extends MasterDataMetaFieldVO {

	private final boolean onDeleteCascade;
	private final boolean isResourceField;
	
	SystemMasterDataMetaFieldVO(Integer iId, String sFieldName,
		String sDbFieldName, String sLabel, String sDescription,
		String sDefaultValue, String sForeignEntityName, String sForeignEntityFieldName, 
		String sUnreferencedForeignEntityName, String sUnreferencedForeignEntityFieldName,
		String sLookupEntityName, String sLookupEntityFieldName,
		Class<?> clsDataType, String sDefaultComponentType,
		Integer iDataScale, Integer iDataPrecision, String sInputFormat,
		String sOutputFormat, boolean bNullable, boolean bSearchable,
		boolean bUnique, boolean bInvariant, boolean bLogToLogbook,
		String resourceIdForLabel, String resourceIdForDescription,
		boolean bIndexed, boolean onDeleteCascade, Integer iOrder, boolean isResourceField)
	{
		super(iId, sFieldName, sDbFieldName, sLabel, sDescription, sDefaultValue,
			sForeignEntityName, sForeignEntityFieldName, sUnreferencedForeignEntityName, sUnreferencedForeignEntityFieldName,
			sLookupEntityName, sLookupEntityFieldName,
			clsDataType, sDefaultComponentType, iDataScale,
			iDataPrecision, sInputFormat, sOutputFormat, bNullable, bSearchable,
			bUnique, bInvariant, bLogToLogbook, CREATED_DATE, CREATED_USER,
			CHANGED_DATE, CHANGED_USER, VERSION, resourceIdForLabel,
			resourceIdForDescription, bIndexed, iOrder);
		this.onDeleteCascade = onDeleteCascade;
		this.isResourceField = isResourceField;
	}
	
	public boolean isOnDeleteCascade() {
		return onDeleteCascade;
	}
	
	public boolean isResourceField() {
		return isResourceField;
	}
}

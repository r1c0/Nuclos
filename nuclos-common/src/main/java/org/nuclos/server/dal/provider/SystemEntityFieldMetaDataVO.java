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
package org.nuclos.server.dal.provider;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.server.autosync.SystemMasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;

public class SystemEntityFieldMetaDataVO extends EntityFieldMetaDataVO {

	private final MasterDataMetaFieldVO mdFieldMeta;

	SystemEntityFieldMetaDataVO(Long entityId, MasterDataMetaFieldVO mdFieldMeta) {
		this.mdFieldMeta = mdFieldMeta;
		this.setId(mdFieldMeta.getId().longValue());
		this.setEntityId(entityId);
		this.setField(mdFieldMeta.getFieldName());
		this.setDbColumn(mdFieldMeta.getDBFieldName());
		this.setDataType(mdFieldMeta.getJavaClass().getName());
		this.setDefaultComponentType(mdFieldMeta.getDefaultComponentType());
		this.setScale(mdFieldMeta.getDataScale());
		this.setPrecision(mdFieldMeta.getDataPrecision());
		this.setNullable(mdFieldMeta.isNullable());
		this.setForeignEntity(mdFieldMeta.getForeignEntity());
		this.setForeignEntityField(mdFieldMeta.getForeignEntityField());
		this.setUnreferencedForeignEntity(mdFieldMeta.getUnreferencedForeignEntityName());
		this.setUnreferencedForeignEntityField(mdFieldMeta.getUnreferencedForeignEntityFieldName());
		this.setOrder(mdFieldMeta.getOrder());

		this.setReadonly(Boolean.FALSE);
		this.setUnique(mdFieldMeta.isUnique());
		this.setIndexed(mdFieldMeta.isIndexed());
		this.setModifiable(Boolean.FALSE);
		this.setInsertable(Boolean.FALSE);
		this.setLogBookTracking(mdFieldMeta.getLogToLogbook());
		this.setShowMnemonic(Boolean.FALSE);
		this.setSearchable(Boolean.FALSE);

		this.setDefaultValue(mdFieldMeta.getDefaultValue());
		this.setFormatInput(mdFieldMeta.getInputFormat());
		this.setFormatOutput(mdFieldMeta.getOutputFormat());

		this.setLocaleResourceIdForLabel(mdFieldMeta.getResourceSIdForLabel());
		this.setLocaleResourceIdForDescription(mdFieldMeta.getResourceSIdForDescription());

		this.setCreatedAt(InternalTimestamp.toInternalTimestamp(mdFieldMeta.getCreatedAt()));
		this.setCreatedBy(mdFieldMeta.getCreatedBy());
		this.setChangedAt(InternalTimestamp.toInternalTimestamp(mdFieldMeta.getChangedAt()));
		this.setChangedBy(mdFieldMeta.getCreatedBy());
		this.setVersion(mdFieldMeta.getVersion());
		if (mdFieldMeta instanceof SystemMasterDataMetaFieldVO) {
			this.setOnDeleteCascade(((SystemMasterDataMetaFieldVO) mdFieldMeta).isOnDeleteCascade());
		}
	}
	
	public boolean isResourceField() {
		if (mdFieldMeta instanceof SystemMasterDataMetaFieldVO) {
			return ((SystemMasterDataMetaFieldVO) mdFieldMeta).isResourceField();
		}
		return false;
	}
}

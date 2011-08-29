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

import java.util.LinkedHashMap;
import java.util.Map;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.autosync.SystemMasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class SystemEntityMetaDataVO extends EntityMetaDataVO {

	private final MasterDataMetaVO mdMeta;
	private Map<String, SystemEntityFieldMetaDataVO> entityFields;

	SystemEntityMetaDataVO(SystemMasterDataMetaVO mdMeta) {
		this.mdMeta = mdMeta;
		this.setId(mdMeta.getId().longValue());
		this.setEntity(mdMeta.getEntityName());
		this.setDbEntity(mdMeta.getDBEntity());

		this.setStateModel(Boolean.FALSE);
		this.setLogBookTracking(Boolean.FALSE);
		this.setCacheable(mdMeta.isCacheable());
		this.setSearchable(mdMeta.isSearchable());
		this.setEditable(mdMeta.isEditable());
		this.setTreeRelation(Boolean.FALSE);
		this.setTreeGroup(Boolean.FALSE);
		this.setFieldValueEntity(Boolean.FALSE);

		this.setFieldsForEquality(StringUtils.join(";", mdMeta.getFieldsForEquality()));
		this.setCacheable(mdMeta.isCacheable());

		this.setImportExport(mdMeta.getIsImportExport());
		this.setAcceleratorModifier(mdMeta.getAcceleratorModifier());
		this.setAccelerator(mdMeta.getAccelerator());
		this.setLocaleResourceIdForLabel(mdMeta.getResourceSIdForLabel());
		this.setLocaleResourceIdForMenuPath(mdMeta.getResourceSIdForMenuPath());
		this.setLocaleResourceIdForTreeView(mdMeta.getResourceSIdForTreeView());
		this.setLocaleResourceIdForTreeViewDescription(mdMeta.getResourceSIdForTreeViewDescription());
		this.setNuclosResource(mdMeta.getNuclosResource());

		this.setCreatedAt(InternalTimestamp.toInternalTimestamp(mdMeta.getCreatedAt()));
		this.setCreatedBy(mdMeta.getCreatedBy());
		this.setChangedAt(InternalTimestamp.toInternalTimestamp(mdMeta.getChangedAt()));
		this.setChangedBy(mdMeta.getCreatedBy());
		this.setVersion(mdMeta.getVersion());

		this.setUniqueFieldCombinations(mdMeta.getUniqueFieldCombinations());
		this.setLogicalUniqueFieldCombinations(mdMeta.getLogicalUniqueFieldCombinations());
	}

	public Map<String, SystemEntityFieldMetaDataVO> getEntityFields() {
		if (entityFields == null) {
			entityFields = new LinkedHashMap<String, SystemEntityFieldMetaDataVO>();
			for (MasterDataMetaFieldVO field : mdMeta.getFields()) {
				entityFields.put(field.getFieldName(), new SystemEntityFieldMetaDataVO(getId(), field));
			}
			entityFields.put(NuclosEOField.CREATEDAT.getMetaData().getField(), new SystemEntityFieldMetaDataVO(
				getId(), DalSupportForMD.wrapEntityFieldMetaDataVO(NuclosEOField.CREATEDAT.getMetaData())));
			entityFields.put(NuclosEOField.CREATEDBY.getMetaData().getField(), new SystemEntityFieldMetaDataVO(
				getId(), DalSupportForMD.wrapEntityFieldMetaDataVO(NuclosEOField.CREATEDBY.getMetaData())));
			entityFields.put(NuclosEOField.CHANGEDAT.getMetaData().getField(), new SystemEntityFieldMetaDataVO(
				getId(), DalSupportForMD.wrapEntityFieldMetaDataVO(NuclosEOField.CHANGEDAT.getMetaData())));
			entityFields.put(NuclosEOField.CHANGEDBY.getMetaData().getField(), new SystemEntityFieldMetaDataVO(
				getId(), DalSupportForMD.wrapEntityFieldMetaDataVO(NuclosEOField.CHANGEDBY.getMetaData())));
		}
		return entityFields;
	}
}

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

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;

/**
 * @see org.nuclos.common.dal.vo.SystemFields
 */
public enum NuclosEOField implements NuclosEOFieldConstants{
	STATE(getState(), true),
	STATENUMBER(getStateNumber(), true),
	SYSTEMIDENTIFIER(getSystemIdentifier(), false),
	PROCESS(getProcess(), false),
	CREATEDAT(getCreatedAt(), false),
	CREATEDBY(getCreatedBy(), false),
	CHANGEDAT(getChangedAt(), false),
	CHANGEDBY(getChangedBy(), false),
	ORIGIN(getOrigin(), false),
	LOGGICALDELETED(getLogicalDeleted(), false);

	private final EntityFieldMetaDataVO fieldMeta;

	private static final String PROCESSOR = "org.nuclos.server.dal.processor.nuclos.JsonEntityFieldMetaDataProcessor";

	private final boolean forceValueSearch;

	private NuclosEOField(EntityFieldMetaDataVO fieldMeta, boolean forceValueSearch) {
		this.fieldMeta = fieldMeta;
		this.forceValueSearch = forceValueSearch;
	}

	public EntityFieldMetaDataVO getMetaData() {
		return fieldMeta;
	}

	public String getName() {
		return getMetaData().getField();
	}

	public boolean isForceValueSearch() {
		return forceValueSearch;
	}

	public static boolean isEOFieldWithForceValueSearch(String field) {
		NuclosEOField eoField = getByField(field);
		if (eoField != null) {
			return eoField.isForceValueSearch();
		}
		return false;
	}

	public static NuclosEOField getByField(String field) {
		for (NuclosEOField eoField : NuclosEOField.values()) {
			if (eoField.checkField(field))
				return eoField;
		}
		return null;
	}

	public boolean checkField(String field) {
		return (field != null) && field.equals(getMetaData().getField());
	}

	private static EntityFieldMetaDataVO getState() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosState");
		result.setDbColumn("STRVALUE_NUCLOSSTATE");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10010l);

		result.setForeignEntity(NuclosEntity.STATE.getEntityName());
		result.setForeignEntityField("name");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.state.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.state.description");

		return result;
	}

	private static EntityFieldMetaDataVO getStateNumber() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosStateNumber");
		result.setDbColumn("INTVALUE_NUCLOSSTATE");
		result.setDataType(java.lang.Integer.class.getName());
		result.setScale(3);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(true);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10011l);

		result.setForeignEntity(NuclosEntity.STATE.getEntityName());
		result.setForeignEntityField("numeral");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.statenumeral.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.statenumeral.description");

		return result;
	}

	private static EntityFieldMetaDataVO getSystemIdentifier() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosSystemId");
		result.setDbColumn("STRNUCLOSSYSTEMID");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10012l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.systemidentifier.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.systemidentifier.description");

		return result;
	}

	private static EntityFieldMetaDataVO getProcess() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosProcess");
		result.setDbColumn("STRVALUE_NUCLOSPROCESS");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_WRITE);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10013l);

		result.setForeignEntity(NuclosEntity.PROCESS.getEntityName());

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.process.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.process.description");

		return result;
	}

	private static EntityFieldMetaDataVO getCreatedAt() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("createdAt");
		result.setDbColumn("DATCREATED");
		result.setDataType(org.nuclos.common2.InternalTimestamp.class.getName());
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10014l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.createdat.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.createdat.description");

		return result;
	}

	private static EntityFieldMetaDataVO getCreatedBy() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("createdBy");
		result.setDbColumn("STRCREATED");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10015l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.createdby.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.createdby.description");

		return result;
	}

	private static EntityFieldMetaDataVO getChangedAt() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("changedAt");
		result.setDbColumn("DATCHANGED");
		result.setDataType(org.nuclos.common2.InternalTimestamp.class.getName());
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10016l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.changedat.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.changedat.description");

		return result;
	}

	private static EntityFieldMetaDataVO getChangedBy() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("changedBy");
		result.setDbColumn("STRCHANGED");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10017l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.changedby.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.changedby.description");

		return result;
	}

	private static EntityFieldMetaDataVO getOrigin() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosOrigin");
		result.setDbColumn("STRNUCLOSORIGIN");
		result.setDataType(java.lang.String.class.getName());
		result.setScale(255);
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10018l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.origin.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.origin.description");

		return result;
	}

	private static EntityFieldMetaDataVO getLogicalDeleted() {
		EntityFieldMetaDataVO result = new EntityFieldMetaDataVO();
		result.setEntityId(-100l);
		result.setField("nuclosDeleted");
		result.setDbColumn("BLNNUCLOSDELETED");
		result.setDataType(java.lang.Boolean.class.getName());
		result.setFieldGroupId(GROUP_ID_READ);

		result.setReadonly(false);
		result.setNullable(false);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.processor(PROCESSOR);

		result.setId(-10019l);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.logicaldeleted.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.logicaldeleted.description");

		return result;
	}
}

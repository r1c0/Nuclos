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
package org.nuclos.common.dal.vo;

import org.apache.commons.lang.StringUtils;


/**
 * Entity field meta data vo
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik.Stueker</a>
 * @version 01.00.00
 */
public class EntityFieldMetaDataVO extends AbstractDalVOWithVersion {

	private static final long serialVersionUID = 7938377056541228804L;

	private Long entityId;
	private Long fieldGroupId;

	private String entityIdAsString;

	private String field;

	/**
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	private PivotInfo pivot;
	
	private String dbColumn;

	private String foreignEntity;
	private String foreignEntityField;
	private String unreferencedForeignEntity;
	private String unreferencedForeignEntityField;

	private String dataType;
	private Integer scale;
	private Integer precision;
	private String formatInput;
	private String formatOutput;

	private Long defaultForeignId;
	private String defaultValue;

	private Boolean readonly;
	private Boolean unique;
	private Boolean nullable;
	private Boolean searchable;
	private Boolean modifiable;
	private Boolean insertable;
	private Boolean logBookTracking;
	private Boolean showMnemonic;
	private Boolean permissiontransfer;
	private Boolean indexed;
	private boolean dynamic;

	private String calcFunction;
	private String sortorderASC;
	private String sortorderDESC;

	private String fallbacklabel;
	private String localeResourceIdForLabel;
	private String localeResourceIdForDescription;
	
	private String defaultMandatory;

	/**
	 * @deprecated Only for serialization, should be protected.
	 */
	public EntityFieldMetaDataVO() {
		super();
	}

	public EntityFieldMetaDataVO(EntityObjectVO eo) {
		super(eo);
		setEntityId(eo.getFieldId("entity"));
		setFieldGroupId(eo.getFieldId("entityfieldgroup"));

		setField(eo.getField("field", String.class));
		setDbColumn(eo.getField("dbfield", String.class));

		setForeignEntity(eo.getField("foreignentity", String.class));
		setForeignEntityField(eo.getField("foreignentityfield", String.class));

		setDataType(eo.getField("datatype", String.class));
		setScale(eo.getField("datascale", Integer.class));
		setPrecision(eo.getField("dataprecision", Integer.class));
		setFormatInput(eo.getField("formatinput", String.class));
		setFormatOutput(eo.getField("formatoutput", String.class));

		setDefaultForeignId(eo.getField("foreigndefault", Long.class));
		setDefaultValue(eo.getField("valuedefault", String.class));

		setReadonly(eo.getField("readonly", Boolean.class));
		setUnique(eo.getField("unique",  Boolean.class));
		setNullable(eo.getField("nullable", Boolean.class));
		setIndexed(eo.getField("indexed", Boolean.class));
		setSearchable(eo.getField("searchable",  Boolean.class));
		setModifiable(eo.getField("modifiable",  Boolean.class));
		setInsertable(eo.getField("insertable",  Boolean.class));
		setLogBookTracking(eo.getField("logbooktracking",  Boolean.class));
		setShowMnemonic(eo.getField("showmnemonic",  Boolean.class));
		setPermissiontransfer(eo.getField("permissiontransfer", Boolean.class));

		setCalcFunction(eo.getField("calcfunction", String.class));
		setSortorderASC(eo.getField("sortationasc", String.class));
		setSortorderDESC(eo.getField("sortationdesc", String.class));
		setLocaleResourceIdForLabel(eo.getField("localeresourcel", String.class));
		setLocaleResourceIdForDescription(eo.getField("localeresourced", String.class));
		setDefaultMandatory(eo.getField("defaultmandatory", String.class));
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
		if(entityId != null)
			entityIdAsString = String.valueOf(entityId.longValue());
	}

	public void setFieldGroupId(Long fieldGroupId) {
		this.fieldGroupId = fieldGroupId;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	public void setPivotInfo(PivotInfo pivot) {
		this.pivot = pivot;
	}

	public void setDbColumn(String dbColumn) {
		this.dbColumn = dbColumn;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public void setFormatInput(String formatInput) {
		this.formatInput = formatInput;
	}

	public void setFormatOutput(String formatOutput) {
		this.formatOutput = formatOutput;
	}

	public void setDefaultForeignId(Long defaultForeignId) {
		this.defaultForeignId = defaultForeignId;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public void setModifiable(Boolean modifiable) {
		this.modifiable = modifiable;
	}

	public void setInsertable(Boolean insertable) {
		this.insertable = insertable;
	}

	public void setLogBookTracking(Boolean logBookTracking) {
		this.logBookTracking = logBookTracking;
	}

	public void setShowMnemonic(Boolean showMnemonic) {
		this.showMnemonic = showMnemonic;
	}

	public void setCalcFunction(String calcFunction) {
		this.calcFunction = calcFunction;
	}

	public void setSortorderASC(String sortorderASC) {
		this.sortorderASC = sortorderASC;
	}

	public void setSortorderDESC(String sortorderDESC) {
		this.sortorderDESC = sortorderDESC;
	}

	public void setLocaleResourceIdForLabel(String localeResourceIdForLabel) {
		this.localeResourceIdForLabel = localeResourceIdForLabel;
	}

	public void setLocaleResourceIdForDescription(
		String localeResourceIdForDescription) {
		this.localeResourceIdForDescription = localeResourceIdForDescription;
	}

	public Long getEntityId() {
		return entityId;
	}

	public Long getFieldGroupId() {
		return fieldGroupId;
	}

	public String getField() {
		return field;
	}
	
	/**
	 * @return Get the associated PivotInfo if this is a pivot field.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	public PivotInfo getPivotInfo() {
		return pivot;
	}

	public String getDbColumn() {
		return dbColumn;
	}

	public String getDataType() {
		return dataType;
	}

	public Integer getScale() {
		return scale;
	}

	public Integer getPrecision() {
		return precision;
	}

	public String getFormatInput() {
		return formatInput;
	}

	public String getFormatOutput() {
		return formatOutput;
	}

	public Long getDefaultForeignId() {
		return defaultForeignId;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Boolean isReadonly() {
		return readonly;
	}

	public Boolean isUnique() {
		return unique;
	}

	public Boolean isNullable() {
		return nullable;
	}

	public Boolean isSearchable() {
		return searchable;
	}

	public Boolean isModifiable() {
		return modifiable;
	}

	public Boolean isInsertable() {
		return insertable;
	}

	public Boolean isLogBookTracking() {
		return logBookTracking;
	}

	public Boolean isShowMnemonic() {
		return showMnemonic;
	}

	public String getCalcFunction() {
		return calcFunction;
	}

	public String getSortorderASC() {
		return sortorderASC;
	}

	public String getSortorderDESC() {
		return sortorderDESC;
	}

	public String getLocaleResourceIdForLabel() {
		return localeResourceIdForLabel;
	}

	public String getLocaleResourceIdForDescription() {
		return localeResourceIdForDescription;
	}

	public String getForeignEntity() {
		return foreignEntity;
	}

	public void setForeignEntity(String foreignEntity) {
		this.foreignEntity = foreignEntity;
	}

	public String getForeignEntityField() {
		return foreignEntityField;
	}

	public void setForeignEntityField(String foreignEntityField) {
		this.foreignEntityField = foreignEntityField;
	}

	public String getUnreferencedForeignEntity() {
		return unreferencedForeignEntity;
	}

	public void setUnreferencedForeignEntity(String unreferencedForeignEntity) {
		this.unreferencedForeignEntity = unreferencedForeignEntity;
	}

	public String getUnreferencedForeignEntityField() {
		return unreferencedForeignEntityField;
	}

	public void setUnreferencedForeignEntityField(String unreferencedForeignEntityField) {
		this.unreferencedForeignEntityField = unreferencedForeignEntityField;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EntityFieldMetaDataVO) {
			EntityFieldMetaDataVO that = (EntityFieldMetaDataVO)obj;
			return StringUtils.equals(getField(), that.getField());
		}
		else
			return false;
	}

	public String getEntityIdAsString() {
		return entityIdAsString;
	}

	public void setEntityIdAsString(String entityIdAsString) {
		this.entityIdAsString = entityIdAsString;
	}

	public Boolean getPermissiontransfer() {
		return permissiontransfer == null ? Boolean.FALSE : permissiontransfer;
	}

	public void setPermissiontransfer(Boolean permissiontransfer) {
		this.permissiontransfer = permissiontransfer;
	}

	public String getFallbacklabel() {
    	return fallbacklabel;
    }

	public void setFallbacklabel(String fallbacklabel) {
    	this.fallbacklabel = fallbacklabel;
    }

	public boolean isDynamic() {
    	return dynamic;
    }

	public void setDynamic(boolean dynamic) {
    	this.dynamic = dynamic;
    }

	public Boolean isIndexed() {
		return indexed;
	}
	
	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}
	
	public String getDefaultMandatory() {
		return defaultMandatory;
	}

	public void setDefaultMandatory(String defaultMandatory) {
		this.defaultMandatory = defaultMandatory;
	}
	
	@Override
    public int hashCode() {	    
	    return getField().hashCode();
    }
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("field=").append(getField());
		result.append(",column=").append(getDbColumn());
		result.append(",entityId=").append(getEntityId());
		result.append(",foreign=").append(getForeignEntity());
		result.append(",ffield=").append(getForeignEntityField());
		result.append(",id=").append(getId());
		result.append(",version=").append(getVersion());
		result.append("]");
		return result.toString();
	}	
	
}

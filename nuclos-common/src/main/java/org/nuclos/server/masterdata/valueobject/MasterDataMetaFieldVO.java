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
package org.nuclos.server.masterdata.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Generic value object representing meta information for master data entity fields.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 00.01.000
 * @deprecated use EntityFieldMetaDataVO
 */
@Deprecated
public class MasterDataMetaFieldVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sFieldName;
	private String sDBFieldName;
	private String sLabel;
	private String sDescription;
	private String sDefaultValue;
	private String sForeignEntityName;
	private String sForeignEntityFieldName;
	private String sUnreferencedForeignEntityName;
	private String sUnreferencedForeignEntityFieldName;
	private Class<?> clsDataType;
	private Integer iDataScale;
	private Integer iDataPrecision;
	private String sFormatInput;
	private String sFormatOutput;
	private boolean bNullable;
	private boolean bSearchable;
	private boolean bUnique;
	private boolean invariant;
	private boolean bIndexed;
	
	private boolean bLogToLogbook;

	private String sResourceIdForLabel;
	private String sResourceIdForDescription;

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param sFieldName field name of master data field
	 * @param sDbFieldName name of underlying database field
	 * @param sLabel label of master data field
	 * @param sDescription description of master data field
	 * @param sForeignEntityName name of foreign entity of master data field
	 * @param clsDataType data type of master data field
	 * @param iDataScale data scale of master data field
	 * @param iDataPrecision data precision of master data field
	 * @param sInputFormat input format of master data field
	 * @param sOutputFormat output format of master data field
	 * @param bNullable is nullable? of master data field
	 * @param bSearchable is searchable? of master data field
	 * @param dateCreatedAt creation date of underlying database record
	 * @param sCreatedBy creator of underlying database record
	 * @param dateChangedAt last changed date of underlying database record
	 * @param sChangedBy last changer of underlying database record
	 */
	public MasterDataMetaFieldVO(Integer iId, String sFieldName, String sDbFieldName, String sLabel, String sDescription, String sDefaultValue,
			String sForeignEntityName, String sForeignEntityFieldName, String sUnreferencedForeignEntityName, String sUnreferencedForeignEntityFieldName, 
			Class<?> clsDataType, Integer iDataScale, Integer iDataPrecision, String sInputFormat,
			String sOutputFormat, boolean bNullable, boolean bSearchable, boolean bUnique, boolean bInvariant, boolean bLogToLogbook, 
			java.util.Date dateCreatedAt, String sCreatedBy, java.util.Date dateChangedAt, String sChangedBy, Integer iVersion,
			String resourceIdForLabel, String resourceIdForDescription, boolean bIndexed) {
		super(iId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.sFieldName = sFieldName;
		this.sDBFieldName = sDbFieldName;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.sDefaultValue = sDefaultValue;
		this.sForeignEntityName = sForeignEntityName;
		this.sForeignEntityFieldName = sForeignEntityFieldName;
		this.sUnreferencedForeignEntityName = sUnreferencedForeignEntityName;
		this.sUnreferencedForeignEntityFieldName = sUnreferencedForeignEntityFieldName;
		this.clsDataType = clsDataType;
		this.iDataScale = iDataScale;
		this.iDataPrecision = iDataPrecision;
		this.sFormatInput = sInputFormat;
		this.sFormatOutput = sOutputFormat;
		this.bNullable = bNullable;
		this.bSearchable = bSearchable;
		this.bUnique = bUnique;
		this.invariant = bInvariant;
		this.bLogToLogbook = bLogToLogbook;
		this.sResourceIdForLabel = resourceIdForLabel;
		this.sResourceIdForDescription = resourceIdForDescription;
		this.bIndexed = bIndexed;
	}

	/**
	 * Constructor for dynamic entity fields
	 * @param iId
	 * @param sFieldName
	 * @param clsDataType
	 * @param iDataScale
	 * @param iDataPrecision
	 * @param sOutputFormat
	 */
	public MasterDataMetaFieldVO(Integer iId, String sFieldName, String sDbFieldName, String sLabel, String sDescription, String sDefaultValue,
			String sForeignEntityName, String sForeignEntityFieldName, Class<?> clsDataType, Integer iDataScale, Integer iDataPrecision, String sOutputFormat, boolean bUnique) {
		super(iId, null, null, null, null, 1);
		this.sFieldName = sFieldName;
		this.sDBFieldName = sDbFieldName;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.sDefaultValue = sDefaultValue;
		this.sForeignEntityName = sForeignEntityName;
		this.sForeignEntityFieldName = sForeignEntityFieldName;
		this.clsDataType = clsDataType;
		this.iDataScale = iDataScale;
		this.iDataPrecision = iDataPrecision;
		this.sFormatInput = null;
		this.sFormatOutput = sOutputFormat;
		this.bNullable = true;
		this.bSearchable = false;
		this.bUnique = bUnique;
		this.invariant = false;
		this.bLogToLogbook = false;
	}

	/**
	 * @return field name of underlying database record
	 */
	public String getFieldName() {
		return this.sFieldName;
	}

	/**
	 * @return db field name of underlying database record
	 */
	public String getDBFieldName() {
		return this.sDBFieldName;
	}

	/**
	 * @return the name of the id field in the database
	 * @todo this is an almost undocumented matching without error handling. It simply fails when this naming convention
	 * isn't used in the metadata.
	 */
	public String getDBIdFieldName() {
		return getDBFieldName().toUpperCase().replaceAll("STRVALUE_", "INTID_");
	}
	
	public Class<?> getDBIdFieldType() {
		return getDBIdFieldName().startsWith("STRID_") ? String.class : Integer.class;
	}

	/**
	 * @return label of underlying database record
	 */
	public String getLabel() {
		return sLabel;
	}

	/**
	 * @return description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * @return default value of underlying database record
	 */
	public String getDefaultValue() {
		return sDefaultValue;
	}

	/**
	 * @return foreign entity of underlying database record, if any.
	 */
	public String getForeignEntity() {
		return sForeignEntityName;
	}

	/**
	 * @return foreign entity field of underlying database record, if any.
	 */
	public String getForeignEntityField() {
		return sForeignEntityFieldName;
	}

	public String getUnreferencedForeignEntityName() {
		return sUnreferencedForeignEntityName;
	}

	public String getUnreferencedForeignEntityFieldName() {
		return sUnreferencedForeignEntityFieldName;
	}

	/**
	 * @return data type of underlying database record
	 */
	public Class<?> getJavaClass() {
		return this.clsDataType;
	}

	/**
	 * @return data scale of underlying database record
	 */
	public Integer getDataScale() {
		return this.iDataScale;
	}

	/**
	 * @return data precision of underlying database record
	 */
	public Integer getDataPrecision() {
		return this.iDataPrecision;
	}

	/**
	 * @return input format of underlying database record
	 */
	public String getInputFormat() {
		return this.sFormatInput;
	}

	/**
	 * @return output format of underlying database record
	 */
	public String getOutputFormat() {
		return this.sFormatOutput;
	}

	/**
	 * @return Is this field nullable?
	 */
	public boolean isNullable() {
		return this.bNullable;
	}

	/**
	 * @return Is this field searchable?
	 */
	public boolean isSearchable() {
		return this.bSearchable;
	}

	/**
	 * @return Is this a unique field?
	 * note: one or more fields can make a masterdata record unique
	 */
	public boolean isUnique() {
		return this.bUnique;
	}

	/**
	 * @return Shall this field be logged in the logbook?
	 */
	public boolean getLogToLogbook() {
		return this.bLogToLogbook;
	}	
	
	public boolean isInvariant() {
		return this.invariant;
	}
	
	
	public String getResourceSIdForLabel() {
		return this.sResourceIdForLabel;
	}

	public void setResourceSIdForLabel(String sResourceId) {
		this.sResourceIdForLabel = sResourceId;
	}
	
	public String getResourceSIdForDescription() {
		return this.sResourceIdForDescription;
	}

	public void setResourceSIdForDescription(String sResourceId) {
		this.sResourceIdForDescription = sResourceId;
	}
	
	public boolean isIndexed() {
		return bIndexed;
	}
}	// class MasterDataMetaFieldVO

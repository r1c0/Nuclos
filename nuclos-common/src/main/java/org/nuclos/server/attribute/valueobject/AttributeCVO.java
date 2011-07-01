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
package org.nuclos.server.attribute.valueobject;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a dynamic attribute with all its possible values.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @version 00.01.000
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @todo "scale" and "precision" are mixed up.
 * @deprecated use EntityFieldMetaDataVO
 */
@Deprecated
public class AttributeCVO extends NuclosValueObject implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int MAX_SCALE = 2000;

	private Integer iAttributeGroupId;
	private String sAttributeGroup;
	private String sCalcFunction;
	private String sName;
	private String sLabel;
	private String sDescription;
	private DataType datatype;
	private Integer iDataScale;
	private Integer iDataPrecision;
	private String sInputFormat;
	private String sOutputFormat;
	private boolean bNullable;
	private boolean bSearchable;
	private boolean bModifiable;
	private boolean bInsertable;
	private boolean bLogbookTracking;
	private boolean bSystemAttribute;
	private boolean bShowMnemonic;
	private Integer iDefaultValueId;
	private Object oDefaultValue;
	private String sSortationAsc;
	private String sSortationDesc;
	private String sExternalEntityName;
	private String sExternalEntityFieldName;
	private Collection<AttributeValueVO> collValues = new HashSet<AttributeValueVO>();
	protected Map<Integer, Permission> mpPermissions = CollectionUtils.newHashMap();
	
	private String sResourceIdForLabel;
	private String sResourceIdForDescription;

	/**
	 * constructor to be called by server only
	 * @param evo contains the common fields
	 * @param iAttributegroupId id of group of underlying database record
	 * @param sAttributegroup name of group of underlying database record
	 * @param sName name of underlying database record
	 * @param sLabel attributelabel of underlying database record
	 * @param sDescription description of underlying database record
	 * @param sDataType datatype of underlying database record
	 * @param iDataScale datascale of underlying database record
	 * @param iDataPrecision dataprecision of underlying database record
	 * @param sInputFormat input format of underlying database record
	 * @param sOutputFormat output format of underlying database record
	 * @param bNullable is nullable? of underlying database record
	 * @param bSearchable is searchable? of underlying database record
	 * @param bModifiable is modifiable? of underlying database record
	 * @param bInsertable is insertable? of underlying database record
	 * @param bLogbookTracking is logbook tracking? of underlying database record
	 * @param bSystemAttribute is system attribute? of underlying database record
	 * @param bShowMnemonic show mnemonic? of underlying database record
	 * @param iDefaultValueId default value id of underlying database record
	 * @param oDefaultValue default value of underlying database record
	 * @param sSortationAsc ascending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sSortationDesc descending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sExternalEntityName external table of underlying database record
	 * @param sExternalEntityFieldName
	 * @param mpPermissions permission information for attribute
	 */
	public AttributeCVO(NuclosValueObject evo, Integer iAttributegroupId, String sAttributegroup, String sCalcFunction,
			String sName, String sLabel, String sDescription, String sDataType, Integer iDataScale, Integer iDataPrecision,
			String sInputFormat, String sOutputFormat, boolean bNullable,
			boolean bSearchable, boolean bModifiable, boolean bInsertable, boolean bLogbookTracking,
			boolean bSystemAttribute, boolean bShowMnemonic, Integer iDefaultValueId, Object oDefaultValue, String sSortationAsc,
			String sSortationDesc, String sExternalEntityName, String sExternalEntityFieldName, Map<Integer, Permission> mpPermissions,
			String sResourceIdForLabel, String sResourceIdForDescription) {
		super(evo);
		this.iAttributeGroupId = iAttributegroupId;
		this.sAttributeGroup = sAttributegroup;
		this.sCalcFunction = sCalcFunction;
		this.sName = sName;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.datatype = DataType.getByJavaClassName(sDataType);
		this.iDataScale = iDataScale;
		this.iDataPrecision = iDataPrecision;
		this.sInputFormat = sInputFormat;
		this.sOutputFormat = sOutputFormat;
		this.bNullable = bNullable;
		this.bSearchable = bSearchable;
		this.bModifiable = bModifiable;
		this.bInsertable = bInsertable;
		this.bLogbookTracking = bLogbookTracking;
		this.bSystemAttribute = bSystemAttribute;
		this.bShowMnemonic = bShowMnemonic;
		this.iDefaultValueId = iDefaultValueId;
		this.oDefaultValue = oDefaultValue;
		this.sSortationAsc = sSortationAsc;
		this.sSortationDesc = sSortationDesc;
		this.sExternalEntityName = sExternalEntityName;
		this.sExternalEntityFieldName = sExternalEntityFieldName;
		this.mpPermissions = new HashMap<Integer, Permission>(mpPermissions);
		this.sResourceIdForLabel = sResourceIdForLabel;
		this.sResourceIdForDescription = sResourceIdForDescription;
	}

	/**
	 * creates a new attribute.
	 */
	public AttributeCVO() {
		this(null, null, null, null, null, null, null, null, null, null, true, false, true, false, false, false, false, null, null, null, null, null, null, null);
	}

	/**
	 * @param iAttributeGroupId id of group of underlying database record
	 * @param sName name of underlying database record
	 * @param sLabel attributelabel of underlying database record
	 * @param sDescription description of underlying database record
	 * @param sDataType datatype of underlying database record
	 * @param iDataScale datascale of underlying database record
	 * @param iDataPrecision dataprecision of underlying database record
	 * @param sFormatInput input format of underlying database record
	 * @param sFormatOutput output format of underlying database record
	 * @param bNullable is nullable? of underlying database record
	 * @param bSearchable is searchable? of underlying database record
	 * @param bModifiable is modifiable? of underlying database record
	 * @param bInsertable is insertable? of underlying database record
	 * @param bLogbookTracking is logbook tracking? of underlying database record
	 * @param bSystemAttribute is system attribute? of underlying database record
	 * @param bShowMnemonic show mnemonic? of underlying database record
	 * @param iDefaultValueId default value id of underlying database record
	 * @param oDefaultValue default value of underlying database record
	 * @param sSortationAsc ascending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sSortationDesc descending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sExternalEntityName external table of underlying database record
	 */
	private AttributeCVO(Integer iAttributeGroupId, String sName, String sLabel, String sDescription,
			String sCalcFunction, String sDataType, Integer iDataScale, Integer iDataPrecision,
			String sFormatInput, String sFormatOutput, boolean bNullable,
			boolean bSearchable, boolean bModifiable, boolean bInsertable, boolean bLogbookTracking,
			boolean bSystemAttribute, boolean bShowMnemonic, Integer iDefaultValueId, Object oDefaultValue, String sSortationAsc,
			String sSortationDesc, String sExternalEntityName, Integer iResourceIdForLabel, Integer iResourceIdForDescription) {
		super();
		this.iAttributeGroupId = iAttributeGroupId;
		this.sAttributeGroup = null;
		this.sName = sName;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.sCalcFunction = sCalcFunction;
		this.datatype = DataType.getByJavaClassName(sDataType);
		this.iDataScale = iDataScale;
		this.iDataPrecision = iDataPrecision;
		this.sInputFormat = sFormatInput;
		this.sOutputFormat = sFormatOutput;
		this.bNullable = bNullable;
		this.bSearchable = bSearchable;
		this.bModifiable = bModifiable;
		this.bInsertable = bInsertable;
		this.bLogbookTracking = bLogbookTracking;
		this.bSystemAttribute = bSystemAttribute;
		this.bShowMnemonic = bShowMnemonic;
		this.iDefaultValueId = iDefaultValueId;
		this.oDefaultValue = oDefaultValue;
		this.sSortationAsc = sSortationAsc;
		this.sSortationDesc = sSortationDesc;
		this.sExternalEntityName = sExternalEntityName;
	}

	/**
	 * get name of underlying database record
	 * @return name of underlying database record
	 */
	public String getName() {
		return sName;
	}

	/**
	 * set name of underlying database record
	 * @param sName name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get label of underlying database record
	 * @return label of underlying database record
	 */
	public String getLabel() {
		return sLabel;
	}

	/**
	 * set label of underlying database record
	 * @param sLabel label of underlying database record
	 */
	public void setLabel(String sLabel) {
		this.sLabel = sLabel;
	}

	/**
	 * get description of underlying database record
	 * @return description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set description of underlying database record
	 * @param sDescription of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * Enumeration of data types for leased object attributes.
	 */
	public static enum DataType {
		STRING(String.class, "Text"),
		DATE(Date.class, "Datum"),
		DATETIME(DateTime.class, "Datum mit Zeit"),
		INTEGER(Integer.class, "Ganze Zahl"),
		DOUBLE(Double.class, "Dezimalzahl"),
		BOOLEAN(Boolean.class, "Ja/Nein"),
		IMAGE(NuclosImage.class, "Image"),
		//NUCLEUSINT-1142
		PASSWORD(NuclosPassword.class, "Passwort"),
		INTERNALTIMESTAMP(InternalTimestamp.class, "Timestamp");

		private final Class<?> cls;
		private final String sLabel;

		private DataType(Class<?> cls, String sLabel) {
			this.cls = cls;
			this.sLabel = sLabel;
		}

		public Class<?> getJavaClass() {
			return this.cls;
		}

		public String getLabel() {
			return this.sLabel;
		}

		@Override
		public String toString() {
			return this.getLabel();
		}

		public static DataType getByJavaClassName(String sJavaClassName) {
			if (sJavaClassName == null) {
				return null;
			}
			for (DataType datatype : values()) {
				if (datatype.getJavaClass().getName().equals(sJavaClassName)) {
					return datatype;
				}
			}
			//?? throw new NuclosFatalException(CommonLocaleDelegate.getMessage("AttributeCVO.1","{0} ist kein g\u00fcltiger Datentyp f\u00fcr Attribute.", sJavaClassName));
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("attributecvo.exception.2", sJavaClassName)); 
				//sJavaClassName + " ist kein g\u00fcltiger Datentyp f\u00fcr Attribute.");
		}

	}	// enum DataType

	public DataType getDataType() {
		return this.datatype;
	}

	public void setDataType(DataType datatype) {
		this.datatype = datatype;
	}

	/**
	 * get java class of underlying database record
	 * @return the java class represented by the data type
	 * @precondition this.getDataType() != null
	 * @todo deprecate?
	 */
	public Class<?> getJavaClass() {
		return this.getDataType().getJavaClass();
	}

	/**
	 * get data scale of underlying database record
	 * @return data scale of underlying database record
	 */
	public Integer getDataScale() {
		return this.iDataScale;
	}

	/**
	 * set data scale of underlying database record
	 * @param iDataScale data scale of underlying database record
	 */
	public void setDataScale(Integer iDataScale) {
		this.iDataScale = iDataScale;
	}

	/**
	 * get data precision of underlying database record
	 * @return data precision of underlying database record
	 */
	public Integer getDataPrecision() {
		return this.iDataPrecision;
	}

	/**
	 * set data precision of underlying database record
	 * @param iDataPrecision data precision of underlying database record
	 */
	public void setDataPrecision(Integer iDataPrecision) {
		this.iDataPrecision = iDataPrecision;
	}

	/**
	 * get input format of underlying database record
	 * @return input format of underlying database record
	 */
	public String getInputFormat() {
		return this.sInputFormat;
	}

	/**
	 * set format of underlying database record
	 * @param sInputFormat input format of underlying database record
	 */
	public void setInputFormat(String sInputFormat) {
		this.sInputFormat = sInputFormat;
	}

	/**
	 * get output format of underlying database record
	 * @return output format of underlying database record
	 */
	public String getOutputFormat() {
		return this.sOutputFormat;
	}

	/**
	 * set format of underlying database record
	 * @param sOutputFormat output format of underlying database record
	 */
	public void setOutputFormat(String sOutputFormat) {
		this.sOutputFormat = sOutputFormat;
	}

	/**
	 * is attribute nullable? of underlying database record
	 * @return boolean value
	 */
	public boolean isNullable() {
		return this.bNullable;
	}

	/**
	 * set if attribute is nullable of underlying database record
	 * @param bNullable boolean value
	 */
	public void setNullable(boolean bNullable) {
		this.bNullable = bNullable;
	}

	/**
	 * is attribute searchable? of underlying database record
	 * @return boolean value
	 */
	public boolean isSearchable() {
		return this.bSearchable;
	}

	/**
	 * set if attribute is searchable of underlying database record
	 * @param bSearchable boolean value
	 */
	public void setSearchable(boolean bSearchable) {
		this.bSearchable = bSearchable;
	}

	/**
	 * is attribute modifiable? of underlying database record
	 * @return boolean value
	 */
	public boolean isModifiable() {
		return this.bModifiable;
	}

	/**
	 * set if attribute is modifiable of underlying database record
	 * @param bModifiable boolean value
	 */
	public void setModifiable(boolean bModifiable) {
		this.bModifiable = bModifiable;
	}

	/**
	 * is attribute insertable? of underlying database record
	 * @return boolean value
	 */
	public boolean isInsertable() {
		return this.bInsertable;
	}

	/**
	 * set if attribute is insertable of underlying database record
	 * @param bInsertable boolean value
	 */
	public void setInsertable(boolean bInsertable) {
		this.bInsertable = bInsertable;
	}

	/**
	 * @return Is this a pure value field?
	 */
	private boolean isValueField() {
		return this.isModifiable() && !this.isSearchable() && !this.isInsertable();
	}

	/**
	 * @return Is this an id field?
	 */
	public boolean isIdField() {
		return !this.isValueField();
	}

	/**
	 * is attribute in logbook? of underlying database record
	 * @return boolean value
	 */
	public boolean isLogbookTracking() {
		return this.bLogbookTracking;
	}

	/**
	 * set if attribute is in logbook of underlying database record
	 * @param bLogbookTracking boolean value
	 */
	public void setLogbookTracking(boolean bLogbookTracking) {
		this.bLogbookTracking = bLogbookTracking;
	}

	/**
	 * is system attribute? of underlying database record
	 * @return boolean value
	 */
	public boolean isSystemAttribute() {
		return this.bSystemAttribute;
	}

	/**
	 * show menmonic? of underlying database record
	 * @return boolean value
	 */
	public boolean isShowMnemonic() {
		return this.bShowMnemonic;
	}

	/**
	 * set if mnemonic is shown of underlying database record
	 * @param isShowMnemonic boolean value
	 */
	public void setShowMnemonic(boolean isShowMnemonic) {
		this.bShowMnemonic = isShowMnemonic;
	}

	/**
	 * get default value id of underlying database record
	 * @return default value id of underlying database record
	 */
	public Integer getDefaultValueId() {
		return iDefaultValueId;
	}

	/**
	 * set default value id of underlying database record
	 * @param iDefaultValueId default value id of underlying database record
	 */
	public void setDefaultValueId(Integer iDefaultValueId) {
		this.iDefaultValueId = iDefaultValueId;
	}

	/**
	 * get default value of underlying database record
	 * @return default value of underlying database record
	 */
	public Object getDefaultValue() {
		return this.oDefaultValue;
	}

	/**
	 * set default value of underlying database record
	 * @param oDefaultValue default value of underlying database record
	 */
	public void setDefaultValue(Object oDefaultValue) {
		this.oDefaultValue = oDefaultValue;
	}

	/**
	 * @return ascending sortation clause
	 */
	public String getSortationAsc() {
		return this.sSortationAsc;
	}

	/**
	 * set ascending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sSortationAsc
	 */
	public void setSortationAsc(String sSortationAsc) {
		this.sSortationAsc = sSortationAsc;
	}

	/**
	 * @return descending sortation clause
	 */
	public String getSortationDesc() {
		return this.sSortationDesc;
	}

	/**
	 * set descending sortation clause (SQL syntax) used within sorting this attribute in the search result table
	 * @param sSortationDesc
	 */
	public void setSortationDesc(String sSortationDesc) {
		this.sSortationDesc = sSortationDesc;
	}

	/**
	 * get external masterdata source of underlying database record
	 * @return external masterdata source of underlying database record
	 */
	public String getExternalEntity() {
		return this.sExternalEntityName;
	}

	/**
	 * set external masterdata source of underlying database record
	 * @param sExternalEntityName external masterdata source of underlying database record
	 */
	public void setExternalEntity(String sExternalEntityName) {
		this.sExternalEntityName = sExternalEntityName;
	}

	/**
	 * @return the field in the external entity which is the value for this attribute.
	 */
	public String getExternalEntityFieldName() {
		return this.sExternalEntityFieldName;
	}

	public void setExternalEntityFieldName(String sExternalEntityFieldName) {
		this.sExternalEntityFieldName = sExternalEntityFieldName;
	}

	/**
	 * get permission for derived genericobject attributes
	 * @return permission for derived genericobject attributes
	 * @postcondition result != null
	 */
	public Permission getPermission(Integer iState) {
		return LangUtils.defaultIfNull(mpPermissions.get(iState), Permission.NONE);
	}

	/**
	 * get group id of underlying database record
	 * @return group id of underlying database record
	 */
	public Integer getAttributegroupId() {
		return iAttributeGroupId;
	}

	/**
	 * set group id of underlying database record
	 * @param iAttributegroupId group id of underlying database record
	 */
	public void setAttributegroupId(Integer iAttributegroupId) {
		this.iAttributeGroupId = iAttributegroupId;
	}

	/**
	 * get group name of underlying database record (joined)
	 * @return group name of underlying database record
	 */
	public String getAttributegroup() {
		return sAttributeGroup;
	}

	/**
	 * set group name. Note that this value is never written to the database but is needed for the client for consistency reasons.
	 * @param sAttributegroup
	 */
	public void setAttributegroup(String sAttributegroup) {
		this.sAttributeGroup = sAttributegroup;
	}

	/**
	 * get possible values for attribute
	 * @return Collection<AttributeValueVO> possible values for attribute
	 */
	public Collection<AttributeValueVO> getValues() {
		return this.collValues;
	}

	/**
	 * set possible values for attribute
	 * @param collValues Collection<AttributeValueVO> possible values for attribute
	 */
	public void setValues(Collection<AttributeValueVO> collValues) {
		this.collValues = new HashSet<AttributeValueVO>(collValues);
	}

	/**
	 * sets the permissions for this attribute
	 * @param mpPermissions permissions for attribute
	 */
	public void setPermissions(Map<Integer, Permission> mpPermissions) {
		this.mpPermissions = new HashMap<Integer, Permission>(mpPermissions);
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		// check not nullable fields:
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("attribute.error.validation.attribute.name");
		}
		if (StringUtils.isNullOrEmpty(this.getLabel())) {
			throw new CommonValidationException("attribute.error.validation.attribute.label");
		}
		//check attribute group
		if (this.getAttributegroupId() == null) {
			throw new CommonValidationException("attribute.error.validation.attributegroup.group");
		}

		//check data type, scale and precision combinations
		final DataType datatype = this.getDataType();
		if (datatype == null) {
			throw new CommonValidationException("attribute.error.validation.attribute.datatype");
		}

		this.validateScaleAndPrecision();

		// check getSearchable, getModifiable and getInsertable combinations:
		final boolean[][] aabValidFlags = new boolean[4][3];
		aabValidFlags[0][0] = false;
		aabValidFlags[0][1] = false;
		aabValidFlags[0][2] = false;	// simple dropdown
		aabValidFlags[1][0] = true;
		aabValidFlags[1][1] = false;
		aabValidFlags[1][2] = false;	// list of values
		aabValidFlags[2][0] = false;
		aabValidFlags[2][1] = true;
		aabValidFlags[2][2] = false;	// simple textfield
		aabValidFlags[3][0] = false;
		aabValidFlags[3][1] = true;
		aabValidFlags[3][2] = true;		// combo dropdown

		boolean bValid = false;
		for (boolean[] abValidFlag : aabValidFlags) {
			if ((this.isSearchable() == abValidFlag[0]) && (this.isModifiable() == abValidFlag[1]) && (this.isInsertable() == abValidFlag[2]))
			{
				bValid = true;
			}
		}
		if (!bValid) {
			throw new CommonValidationException("attribute.error.validation.attribute.invalidflags");
		}

		// check external table plausibilities:
		if (this.getExternalEntity() == null) {
			if (this.isModifiable()) {
				if (this.getDefaultValueId() != null) {
					throw new CommonValidationException("attribute.error.validation.attribute.default");
				}
			}
		}
		else {
			if (this.isModifiable()) {
				throw new CommonValidationException("attribute.error.validation.attribute.externalnotmodifiable");
			}
		}

		// check attribute values:
		for (AttributeValueVO attrvaluevo : this.getValues()) {
			attrvaluevo.validate();
		}

		/** @todo format change for existing data? */
		/** @todo check input and output format field contents */
		/** @todo modifiable change -> normalize/denormalize on the fly? */
		/** @todo change of externaltable -> atuomatic sync with t_md_attribute_value? */
		/** @todo check showMnemonic (only if value list, not external) */
		/** @todo update T_UD_GO_ATTRIBUTE if showMnemonic is changed (cascading) */
	}

	private void validateScaleAndPrecision() throws CommonValidationException {
		final Integer iDataScale = this.getDataScale();
		final Integer iPrecision = this.getDataPrecision();

		if (iDataScale != null && iDataScale > MAX_SCALE) {
			throwDataScaleException();
		}

		switch (this.getDataType()) {
			case STRING:
				if (iDataScale == null) {
					throwDataScaleException();
				}
				if (iPrecision != null) {
					throwDataPrecisionException();
				}
				break;
			case DATE:
				if (iDataScale != null) {
					throwDataScaleException();
				}
				if (iPrecision != null) {
					throwDataPrecisionException();
				}
				break;
			case INTEGER:
				if (iDataScale == null) {
					throwDataScaleException();
				}
				if (iPrecision != null) {
					throwDataPrecisionException();
				}
				break;
			case DOUBLE:
				if (iDataScale == null) {
					throwDataScaleException();
				}
				if (iPrecision == null) {
					throwDataPrecisionException();
				}
				break;
			case BOOLEAN:
				if (iDataScale != null) {
					throwDataScaleException();
				}
				if (iPrecision != null) {
					throwDataPrecisionException();
				}
				break;
			case IMAGE:
				break;
			case PASSWORD:
				//NUCLEUSINT-1142
				break;
			default:
				assert false;
		}
	}

	private static void throwDataPrecisionException() throws CommonValidationException {
		throw new CommonValidationException("attribute.error.validation.attribute.dataprecision");
	}

	private static void throwDataScaleException() throws CommonValidationException {
		throw new CommonValidationException("attribute.error.validation.attribute.datascale");
	}

	@Override
	public AttributeCVO clone() {
		final AttributeCVO result = (AttributeCVO) super.clone();

		result.mpPermissions = new HashMap<Integer, Permission>(this.mpPermissions);
		result.collValues = new HashSet<AttributeValueVO>(this.collValues);

		return result;
	}

	/**
	 * @return the name of a pl/sql function which is used for calculating the attribute value
	 */
	public String getCalcFunction() {
		return sCalcFunction;
	}

	/**
	 * sets the name of a pl/sql function which is used for calculating the attribute value
	 */
	public void setCalcFunction(String sCalcFunction) {
		this.sCalcFunction = sCalcFunction;
	}

	/**
	 * @return Is this attribute calculated?
	 */
	public boolean isCalculated() {
		return this.getCalcFunction() != null;
	}

	public Object getMinValue() {
		Object result = null;
		if (sInputFormat != null) {
			final String[] saMinMax = sInputFormat.split(" ");
			if (!"".equals(saMinMax[0])) {
				switch (this.getDataType()) {
					case INTEGER:
						result = Integer.parseInt(saMinMax[0]);
						break;
					case DOUBLE:
						result = Double.parseDouble(saMinMax[0]);
						break;
					case DATE:
						result = new Date(Long.parseLong(saMinMax[0]));
						break;
					default:
						/** @todo ?! */
				}
			}
		}
		return result;
	}

	public Object getMaxValue() {
		Object result = null;
		if (sInputFormat != null) {
			final String[] saMinMax = sInputFormat.split(" ");
			if (saMinMax.length == 2 && !"".equals(saMinMax[1])) {
				switch (this.getDataType()) {
					case INTEGER:
						result = Integer.parseInt(saMinMax[1]);
						break;
					case DOUBLE:
						result = Double.parseDouble(saMinMax[1]);
						break;
					case DATE:
						result = new Date(Long.parseLong(saMinMax[1]));
						break;
					default:
						/** @todo ?! */
				}
			}
		}
		return result;
	}

	/**
	 * Predicate IsCalculated
	 */
	public static class IsCalculated implements Predicate<AttributeCVO> {
		@Override
		public boolean evaluate(AttributeCVO attrcvo) {
			return attrcvo.isCalculated();
		}
	}

	/**
	 * Transformer GetName
	 */
	public static class GetName implements Transformer<AttributeCVO, String> {
		@Override
		public String transform(AttributeCVO attrcvo) {
			return attrcvo.getName();
		}
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

	public void setResourceIdForDescription(String sResourceId) {
		this.sResourceIdForDescription = sResourceId;
	}

}	// class AttributeCVO

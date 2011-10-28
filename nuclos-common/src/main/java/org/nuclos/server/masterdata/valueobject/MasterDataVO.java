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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.ValueValidationHelper;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.interval.DateIntervalUtils;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.valueobject.AbstractNuclosValueObject;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Generic value object representing a master data record.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 00.01.000
 */
public class MasterDataVO extends AbstractNuclosValueObject<Object> {

	private static final long serialVersionUID = 16392087823428951L;
	/**
	 * name of the field, if any, that contains the name.
	 */
	public static final String FIELDNAME_NAME = "name";
	/**
	 * name of the field, if any, that contains the mnemonic.
	 */
	public static final String FIELDNAME_MNEMONIC = "mnemonic";
	/**
	 * name of the field, if any, that contains the description.
	 */
	public static final String FIELDNAME_DESCRIPTION = "description";

	private Object oId;

	/** @todo kick out created by/at, changed by/at fields? for better performance */

	/**
	 * Map<String, Object>
	 */
	private Map<String, Object> mpFields;

	/**
	 * Has this object been changed since its creation?
	 */
	private boolean bChanged;

	// map for dependant child subform data
	private DependantMasterDataMap mpDependants = new DependantMasterDataMap();

	/**
	 * If this object represents a system record, i.e. a record which cannot
	 * manipulated by the user.
	 */
	private boolean systemRecord;

	/**
	 * constructor to be called by server and client
	 * @param oId primary key of underlying database record
	 * @param dateCreatedAt creation date of underlying database record
	 * @param sCreatedBy creator of underlying database record
	 * @param dateChangedAt last changed date of underlying database record
	 * @param sChangedBy last changer of underlying database record
	 * @param iVersion version of underlying database record
	 * @param mpFields May be <code>null</code>.
	 * @precondition sEntity != null
	 * @postcondition this.getId() == oId
	 */
	public MasterDataVO(Object oId, Date dateCreatedAt, String sCreatedBy,
			Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields) {
		this(oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, false);
	}

	public MasterDataVO(Object oId, Date dateCreatedAt, String sCreatedBy,
		Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields, boolean systemRecord) {
		super(dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.oId = oId;
		this.mpFields = CollectionUtils.emptyMapIfNull(mpFields);
		this.systemRecord = systemRecord;
		assert this.getId() == oId;
	}

	/**
	 * "copy constructor"
	 * @param mdvo
	 */
	protected MasterDataVO(MasterDataVO mdvo) {
		this(mdvo.getId(), mdvo.getCreatedAt(), mdvo.getCreatedBy(), mdvo.getChangedAt(), mdvo.getChangedBy(), mdvo.getVersion(), new HashMap<String, Object>(mdvo.getFields()), mdvo.isSystemRecord());
	}

	/**
	 * @postcondition this.getId() == null
	 */
	private MasterDataVO(Map<String, Object> mpFields) {
		this(null, null, null, null, null, null, mpFields);
		assert this.getId() == null;
	}

	/**
	 * constructor to be called by client only
	 * @param mdmetavo the meta data of the master data object to create
	 * @param bSetBooleansToFalse Are booleans to be set to <code>false</code> rather than <code>null</code>?
	 * <code>true</code> is for compatibility only and shouldn't be used for new code.
	 * @precondition mdmetavo != null
	 * @precondition mdmetavo.getEntityName() != null
	 * @postcondition this.getId() == null
	 */
	public MasterDataVO(MasterDataMetaVO mdmetavo, boolean bSetBooleansToFalse) {
		this((Map<String, Object>) null);

		// create fields:
		for (String sFieldName : mdmetavo.getFieldNames()) {
			final MasterDataMetaFieldVO mdmetafieldvo = mdmetavo.getField(sFieldName);

			// enter default value:
			// FALSE for Boolean, null otherwise
			final Object oValue = (bSetBooleansToFalse && (mdmetafieldvo.getJavaClass() == Boolean.class)) ? Boolean.FALSE : null;

			this.mpFields.put(sFieldName, oValue);

			// for id fields, add an id entry as well:
			if (mdmetafieldvo.getForeignEntity() != null) {
				this.mpFields.put(sFieldName + "Id", null);
			}
		}
		assert this.getId() == null;
	}

	/**
	 * @return a clone of <code>this</code>.
	 * @postcondition result.isChanged() == this.isChanged()
	 * @postcondition result.isRemoved() == this.isRemoved()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == this.getId()
	 * @see #copy()
	 */
	@Override
	public MasterDataVO clone() {
		final MasterDataVO result = (MasterDataVO) super.clone();

		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		result.mpFields = new HashMap<String, Object>(this.mpFields);

		assert result.isChanged() == this.isChanged();
		assert result.isRemoved() == this.isRemoved();
		assert result.mpFields != this.mpFields;
//		assert result.getFields().equals(this.getFields());  // This is checked in a JUnit Test (expensive).
		assert result.getId() == this.getId();
		return result;
	}

	public void setChanged(boolean changed) {
		this.bChanged = changed;
	}


	@Override
	public boolean equals(Object obj) {

		if(!(obj instanceof MasterDataVO)) {
			return false;
		}
		MasterDataVO that = (MasterDataVO)obj;

		if(ObjectUtils.equals(that.getId(),this.getId()) && LangUtils.equals(this.getFields(), that.getFields())) {
			return true;
		}
		else {
			return false;
		}


	}

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	public MasterDataVO copy() {
		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		final MasterDataVO result = new MasterDataVO(new HashMap<String, Object>(this.mpFields));
		result.setDependants(this.getDependants());

		assert !result.isChanged();
		assert result.mpFields != this.mpFields;
//		assert result.getFields().equals(this.getFields());  // This is checked in a JUnit Test (expensive).
		assert result.getId() == null;
		return result;
	}

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	public MasterDataVO copy(boolean blnWithDependants) {
		MasterDataVO result = this.copy();
		if(!blnWithDependants) {
			result.setDependants(new DependantMasterDataMap());
		}

		return result;
	}



	/**
	 * @return this object's primary key
	 */
	@Override
	public Object getId() {
		return this.oId;
	}

	/**
	 * @return this object's primary key, which must be an Integer, otherwise a ClassCastException is thrown. Use getId()
	 * if you're not sure about the primary key's type.
	 */
	public Integer getIntId() {
		return (Integer) this.getId();
	}

	/**
	 * Set the integer id of this object.
	 * This is only allowed to keep the mpDependants up to date for newly created masterdata records, which is necessary for the logbook!
	 * @param iId
	 */
	public void setId(Object iId) {
		this.oId = iId;
	}

	/**
	 * Returns true if this record is a system record.
	 */
	public boolean isSystemRecord() {
		return systemRecord;
	}

	/**
	 * @param sFieldName field name
	 * @return the value of the field with the given name.
	 */
	public Object getField(String sFieldName) {
		return this.mpFields.get(sFieldName);
	}

	/**
	 * generic (typed) version of getField(String).
	 * Note that Class<T>.cast() is about 10-15 times slower than a plain old cast.
	 * For optimum performance (where it's necessary) use the non-generic version of getField(String).
	 * @param sFieldName the name of the field.
	 * @param cls the class of the field.
	 * @return the value of the field with the given name, casted to the given class.
	 * @throws ClassCastException if the value of the field doesn't have the given class.
	 * @see #getField(String)
	 */
	public <T> T getField(String sFieldName, Class<T> cls) {
		return cls.cast(this.getField(sFieldName));
	}

	/**
	 * sets the field with the given name to the given value.
	 * @param sFieldName
	 * @param oValue
	 * @postcondition this.isChanged()
	 * @todo setChanged() only if the given value is different from the old value.
	 */
	public void setField(String sFieldName, Object oValue) {
		this.mpFields.put(sFieldName, oValue);
		this.bChanged = true;
	}

	/**
	 * get all fields of master data record
	 * @return map of all fields for master data record
	 * @postcondition result != null
	 */
	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(this.mpFields);
	}

	/**
	 * sets the given fields
	 * @param mpFields Map<String sFieldName, Object oValue>
	 * @precondition mpFields != null
	 * @postcondition this.isChanged()
	 */
	public void setFields(Map<String, Object> mpFields) {
		if (mpFields == null) {
			throw new NullArgumentException("mpFields");
		}
		for (String sFieldName : mpFields.keySet()) {
			this.setField(sFieldName, mpFields.get(sFieldName));
		}
		assert this.isChanged();
	}

	/**
	 * CAUTION: this method should only be used in the context of reports, to hide the data
	 * of subforms if the user has no read permission on it
	 *
	 * set all fields to null
	 */
	public void clearAllFields() {
		for (String field : this.mpFields.keySet()) {
			this.mpFields.put(field, null);
		}
	}

	/**
	 * checks if all fields in this masterdata cvo are empty (the given foreign key field is ignored)
	 * @param sForeignKey foreign key field to ignore (always filled anyway)
	 * @return Are all fields except for the foreign key field empty?
	 * @todo What is "the" foreign key field? There can be more than one per entity!
	 */
	public boolean isEmpty(String sForeignKey) {
		for (String sFieldName : this.mpFields.keySet()) {
			final Object oValue = getField(sFieldName);
			if (!sFieldName.equals(sForeignKey) && (oValue != null)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return Has this object been changed since its creation?
	 */
	public boolean isChanged() {
		return this.bChanged;
	}

	/**
	 * generic validity checker for master data records
	 * @param mdmetavo meta information to validateField against
	 * @precondition mdmetavo != null
	 */
	public void validate(MasterDataMetaVO mdmetavo) throws CommonValidationException {
		for (String sFieldName : mdmetavo.getFieldNames()) {
			if (!ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME.equals(sFieldName)) {
				Object oValueId = null;
				try {
					oValueId = this.getField(sFieldName+"Id");
				}
				catch (Exception e) {
					//do nothing
				}
				MasterDataVO.validateField(oValueId, this.getField(sFieldName), mdmetavo.getField(sFieldName), mdmetavo.getEntityName());
			}
		}
		this.validateValidityInterval();
	}

	private static void validateField(final Object oValueId, final Object oValue, MasterDataMetaFieldVO mdmetafield, String sEntity) throws CommonValidationException {
		MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
		if (cache != null) {
			//NUCLEUSINT-754
			final Object labelOrResource = mdmetafield.getResourceSIdForLabel() != null ? mdmetafield.getResourceSIdForLabel() : mdmetafield.getLabel();
			final String msg = "validateField failed, entity=" + sEntity + " field=" + mdmetafield.getFieldName() 
					+ " ref=" + mdmetafield.getForeignEntity() + " value=" + oValue + ":\n"; 
			if (mdmetafield.getForeignEntity() != null) {
				if ((oValueId == null) && !mdmetafield.isNullable()) {
					throw new CommonValidationException(msg + "valueId is null but not nullable: "
							+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.value",
						cache.getMetaData(sEntity).getResourceSIdForLabel(), labelOrResource));
				}
			} else {
				if ((oValue == null || "".equals(oValue)) && !mdmetafield.isNullable()) {
					throw new CommonValidationException(msg + "value is null or empty but not nullable"
							+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.value",
						cache.getMetaData(sEntity).getResourceSIdForLabel(), labelOrResource));
				}
			}
			if (oValueId == null && oValue != null && !mdmetafield.getJavaClass().isAssignableFrom(oValue.getClass())) {
				if (InternalTimestamp.class.getName().equals(mdmetafield.getJavaClass().getName()) &&
					Date.class.isAssignableFrom(oValue.getClass())) {
					// InternalTimestamp can be mapped to Date
				} else {
					throw new CommonValidationException(msg + "type expected" + mdmetafield.getJavaClass() + " given " + oValue.getClass()
							+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.datatype",
						labelOrResource, cache.getMetaData(sEntity).getResourceSIdForLabel()));
				}
			}
			// check against data scale, for string values at least:
			if (oValueId == null && oValue != null && oValue instanceof String && mdmetafield.getDataScale() != null && ((String) oValue).length() > mdmetafield.getDataScale())
			{
				throw new CommonValidationException(msg
						+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.datascale",
					labelOrResource, cache.getMetaData(sEntity).getResourceSIdForLabel()));
			}
			//check against data precision
			// TODO: integrate correct check here 
			//check against input format
			final String sInputFormat = mdmetafield.getInputFormat();
			if (!ValueValidationHelper.validateInputFormat(oValue, sInputFormat)) {
				throw new CommonValidationException(msg
						+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.formatinput",
					labelOrResource, cache.getMetaData(sEntity).getResourceSIdForLabel(), sInputFormat));
			}
			if (!ValueValidationHelper.validateBoundaries(oValue, sInputFormat)) {
				throw new CommonValidationException(msg
						+ StringUtils.getParameterizedExceptionMessage("masterdata.error.validation.boundaries",
					labelOrResource, cache.getMetaData(sEntity).getResourceSIdForLabel()));
			}
		}
	}

	/**
	 * @throws CommonValidationException if the validity interval is empty.
	 */
	private void validateValidityInterval() throws CommonValidationException {
		final Date dateFrom = (Date) this.getField("validFrom");
		final Date dateTo = (Date) this.getField("validUntil");
		if (DateIntervalUtils.isEmptyDateInterval(dateFrom, dateTo)) {
			throw new CommonValidationException("masterdata.error.validation.validity");
		}
	}

	public String getDebugInfo() {
		final StringBuffer sb = new StringBuffer();
		sb.append("MasterDataVO {");
		sb.append("Id: " + this.getId() + " - ");
		sb.append("Fields: ");
		for (Iterator<String> iter = this.mpFields.keySet().iterator(); iter.hasNext();) {
			final String sFieldName = iter.next();
			sb.append("{" + sFieldName + ": " + this.mpFields.get(sFieldName) + "}");
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		if (this.isRemoved()) {
			sb.append(" - REMOVED");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @return the contents of the name field, if any - otherwise this object's id, if any.
	 */
	@Override
	public String toString() {
		final Object oName = mpFields.get("name");
		return (oName != null) ? oName.toString() : LangUtils.toString(this.getId());
	}

	/**
	 * @return the common fields of this object. Note that this may only be called for entities which have an Integer id.
	 * @see #getIntId()
	 */
	public NuclosValueObject getNuclosValueObject() {
		return new NuclosValueObject(this.getIntId(), this.getCreatedAt(), this.getCreatedBy(),
				this.getChangedAt(), this.getChangedBy(), this.getVersion());
	}

	/**
	 * inner class <code>GetId</code>: transforms a <code>MasterDataVO</code> into its id.
	 */
	public static class GetId implements Transformer<MasterDataVO, Object> {
		@Override
        public Object transform(MasterDataVO mdvo) {
			return mdvo.getId();
		}
	}

	/**
	 * Transformer: gets the field with the given name
	 */
	public static class GetField implements Transformer<MasterDataVO, Object> {
		private final String sFieldName;

		public GetField(String sFieldName) {
			this.sFieldName = sFieldName;
		}

		@Override
        public Object transform(MasterDataVO mdvo) {
			return mdvo.getField(this.sFieldName);
		}
	}

	/**
	 * Transformer: gets the field with the given name, casted to the given type.
	 */
	public static class GetTypedField<T> implements Transformer<MasterDataVO, T> {
		private final String sFieldName;
		private final Class<T> cls;

		public GetTypedField(String sFieldName, Class<T> cls) {
			this.sFieldName = sFieldName;
			this.cls = cls;
		}

		/**
		 * @param mdvo
		 * @throws ClassCastException if the value of the field doesn't have the given type.
		 */
		@Override
        public T transform(MasterDataVO mdvo) {
			return mdvo.getField(this.sFieldName, this.cls);
		}
	}

	/**
	 * inner class <code>NameComparator</code>. Compares <code>MasterDataVO</code>s by their names.
	 */
	public static class NameComparator implements Comparator<MasterDataVO> {
		private final Collator collator = LangUtils.getDefaultCollator();

		@Override
        public int compare(MasterDataVO mdvo1, MasterDataVO mdvo2) {
			return this.collator.compare(mdvo1.getField(FIELDNAME_NAME), mdvo2.getField(FIELDNAME_NAME));
		}
	}	// inner class LabelComparator

	public void setDependants(DependantMasterDataMap mpDependants) {
		this.mpDependants = mpDependants;
	}

	public DependantMasterDataMap getDependants() {
		return this.mpDependants;
	}
}	// class MasterDataVO

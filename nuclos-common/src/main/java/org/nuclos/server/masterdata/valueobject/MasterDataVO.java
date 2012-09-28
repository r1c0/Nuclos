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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.interval.DateIntervalUtils;
import org.nuclos.server.common.valueobject.INuclosValueObject;
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
public class MasterDataVO implements IMasterDataVO, INuclosValueObject<Object> {

	private static final long serialVersionUID = 16392087823428951L;
	
	private EntityObjectVO wrapped;
	
	/**
	 * If this object represents a system record, i.e. a record which cannot
	 * manipulated by the user.
	 */
	private boolean systemRecord;
	
	/**
	 * If this object contains fields for resource-ids, a list of translations can be supplied.
	 */
	private List<TranslationVO> resources;
	
	public MasterDataVO(EntityObjectVO wrapped, boolean systemRecord) {
		this.wrapped = wrapped;
		this.systemRecord = systemRecord;
	}
	
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
	 * 
	 * @deprecated As we want to migrate away from MasterDataVO to EntityObjectVO, it is *much* saver
	 * 		to use {@link #MasterDataVO(String, Object, Date, String, Date, String, Integer, Map)}
	 */
	public MasterDataVO(Object oId, Date dateCreatedAt, String sCreatedBy,
				Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields) {
			this(null, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, false);
	}

	public MasterDataVO(String entity, Object oId, Date dateCreatedAt, String sCreatedBy,
			Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields) {
		this(entity, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, false);
	}

	public MasterDataVO(String entity, Object oId, Date dateCreatedAt, String sCreatedBy,
		Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields, boolean systemRecord) {
		// super(dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		wrapped = new EntityObjectVO();
		wrapped.setEntity(entity);
		final int size = mpFields == null ? 0 : mpFields.size(); 
		wrapped.initFields(size, size);
		wrapped.setId(IdUtils.toLongId(oId));
		if (dateChangedAt != null) {
			wrapped.setChangedAt(new InternalTimestamp(dateChangedAt.getTime()));
		}
		wrapped.setChangedBy(sChangedBy);
		if (dateCreatedAt != null) {
			wrapped.setCreatedAt(new InternalTimestamp(dateCreatedAt.getTime()));
		}
		wrapped.setCreatedBy(sCreatedBy);
		wrapped.setVersion(iVersion);
		this.systemRecord = systemRecord;

		final Map<String,Object> fields = wrapped.getFields();
		final Map<String,Long> idFields = wrapped.getFieldIds();
		if (mpFields != null) {
			for (String f: mpFields.keySet()) {
				final Object value = mpFields.get(f);
				if (f.endsWith("Id")) {
					idFields.put(fieldId(f), IdUtils.toLongId(value));
				}
				else {
					fields.put(f, value);
				}
			}
		}
		
		assert IdUtils.equals(getId(), oId);
	}
	
	private static String fieldId(String fieldIdName) {
		assert fieldIdName.endsWith("Id");
		return fieldIdName.substring(0, fieldIdName.length() - 2);
	}

	/**
	 * "copy constructor"
	 * @param mdvo
	 */
	protected MasterDataVO(String entity, MasterDataVO mdvo) {
		this(entity, mdvo.getId(), mdvo.getCreatedAt(), mdvo.getCreatedBy(), mdvo.getChangedAt(), mdvo.getChangedBy(), mdvo.getVersion(), new HashMap<String, Object>(mdvo.getFields()), mdvo.isSystemRecord());
	}

	/**
	 * @postcondition this.getId() == null
	 */
	private MasterDataVO(String entity, Map<String, Object> mpFields) {
		this(entity, null, null, null, null, null, null, mpFields);
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
		this(mdmetavo.getEntityName(), (Map<String, Object>) null);

		final Map<String,Object> fields = wrapped.getFields();
		final Map<String,Long> idFields = wrapped.getFieldIds();
		
		// create fields:
		for (String sFieldName : mdmetavo.getFieldNames()) {
			final MasterDataMetaFieldVO mdmetafieldvo = mdmetavo.getField(sFieldName);

			// enter default value:
			// FALSE for Boolean, null otherwise
			final Object oValue = (bSetBooleansToFalse && (mdmetafieldvo.getJavaClass() == Boolean.class)) ? Boolean.FALSE : null;

			fields.put(sFieldName, oValue);

			// for id fields, add an id entry as well:
			if (mdmetafieldvo.getForeignEntity() != null) {
				idFields.put(sFieldName + "Id", null);
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
		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		/*
		final MasterDataVOImpl result = (MasterDataVOImpl) super.clone();
		result.mpFields = new HashMap<String, Object>(this.mpFields);
		 */
		final MasterDataVO result = new MasterDataVO(wrapped.copy(), systemRecord);
		result.setResources(getResources());
		result.setDependants(new DependantMasterDataMapImpl());

		assert result.isChanged() == this.isChanged();
		assert result.isRemoved() == this.isRemoved();
		assert result.getId() == this.getId();
		return result;
	}

	@Override
	public void setChanged(boolean changed) {
		wrapped.flagUpdate();
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
	@Override
	public MasterDataVO copy() {
		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		final EntityObjectVO copy = wrapped.copy();
		return new MasterDataVO(copy, systemRecord);
	}

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	@Override
	public MasterDataVO copy(boolean blnWithDependants) {
		final MasterDataVO copy = copy();
		if (!blnWithDependants) {
			copy.setDependants(new DependantMasterDataMapImpl());
		}
		return copy;
	}



	/**
	 * @return this object's primary key
	 */
	@Override
	public Object getId() {
		return IdUtils.unsafeToId(wrapped.getId());
	}

	/**
	 * @return this object's primary key, which must be an Integer, otherwise a ClassCastException is thrown. Use getId()
	 * if you're not sure about the primary key's type.
	 */
	@Override
	public Integer getIntId() {
		return IdUtils.unsafeToId(wrapped.getId());
	}

	/**
	 * Set the integer id of this object.
	 * This is only allowed to keep the mpDependants up to date for newly created masterdata records, which is necessary for the logbook!
	 * @param iId
	 */
	@Override
	public void setId(Object iId) {
		wrapped.setId(IdUtils.toLongId(iId));
	}

	/**
	 * Returns true if this record is a system record.
	 */
	@Override
	public boolean isSystemRecord() {
		return systemRecord;
	}

	/**
	 * @param sFieldName field name
	 * @return the value of the field with the given name.
	 */
	@Override
	public Object getField(String sFieldName) {
		Object result;
		if (sFieldName.endsWith("Id")) {
			result = IdUtils.unsafeToId(wrapped.getFieldId(fieldId(sFieldName)));
		}
		else {
			result = wrapped.getField(sFieldName);
		}
		return result;
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
	@Override
	public <T> T getField(String sFieldName, Class<T> cls) {
		return cls.cast(getField(sFieldName));
	}

	/**
	 * sets the field with the given name to the given value.
	 * @param sFieldName
	 * @param oValue
	 * @postcondition this.isChanged()
	 * @todo setChanged() only if the given value is different from the old value.
	 */
	@Override
	public void setField(String sFieldName, Object oValue) {
		if (sFieldName.endsWith("Id")) {
			wrapped.getFieldIds().put(fieldId(sFieldName), IdUtils.toLongId(oValue));
		}
		else {
			wrapped.getFields().put(sFieldName, oValue);
		}
		wrapped.flagUpdate();
	}

	/**
	 * get all fields of master data record
	 * @return map of all fields for master data record
	 * @postcondition result != null
	 */
	@Override
	public Map<String, Object> getFields() {
		// return Collections.unmodifiableMap(this.mpFields);
		final Map<String,Object> result = new HashMap<String, Object>(wrapped.getFields());
		for (String id: wrapped.getFieldIds().keySet()) {
			final String idWithId = id + "Id";
			result.put(idWithId, IdUtils.unsafeToId(wrapped.getFieldIds().get(id)));
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * sets the given fields
	 * @param mpFields Map<String sFieldName, Object oValue>
	 * @precondition mpFields != null
	 * @postcondition this.isChanged()
	 */
	@Override
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
	@Override
	public void clearAllFields() {
		wrapped.getFields().clear();
		wrapped.getFieldIds().clear();
	}

	/**
	 * checks if all fields in this masterdata cvo are empty (the given foreign key field is ignored)
	 * @param sForeignKey foreign key field to ignore (always filled anyway)
	 * @return Are all fields except for the foreign key field empty?
	 * @todo What is "the" foreign key field? There can be more than one per entity!
	 */
	@Override
	public boolean isEmpty(String sForeignKey) {
		/*
		for (String sFieldName : this.mpFields.keySet()) {
			final Object oValue = getField(sFieldName);
			if (!sFieldName.equals(sForeignKey) && (oValue != null)) {
				return false;
			}
		}
		return true;
		*/
		for (String sFieldName : wrapped.getFields().keySet()) {
			final Object oValue = getField(sFieldName);
			if (!sFieldName.equals(sForeignKey) && (oValue != null)) {
				return false;
			}
		}
		for (String sFieldName : wrapped.getFieldIds().keySet()) {
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
	@Override
	public boolean isChanged() {
		return wrapped.isFlagUpdated();
	}

	/**
	 * generic validity checker for master data records
	 * @param mdmetavo meta information to validateField against
	 * @precondition mdmetavo != null
	 * @deprecated Validation is performed by org.nuclos.server.validation.ValidationSupport.
	 */
	@Override
	public void validate(MasterDataMetaVO mdmetavo) throws CommonValidationException {
		this.validateValidityInterval();
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

	/**
	 * @deprecated use {@link #toDescription()}
	 */
	@Override
	public String getDebugInfo() {
		return toDescription();
	}

	/**
	 * @return the contents of the name field, if any - otherwise this object's id, if any.
	 */
	@Override
	public String toString() {
		final Object oName = wrapped.getFields().get("name");
		return (oName != null) ? oName.toString() : LangUtils.toString(this.getId());
	}
	
	@Override
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("MdVO[id=").append(wrapped.getId());
		if (wrapped.isFlagUpdated()) {
			result.append(",changed=").append(wrapped.isFlagUpdated());
		}
		if (systemRecord) {
			result.append(",sr=").append(systemRecord);
		}
		result.append(",fields=").append(wrapped.getFields());
		final DependantMasterDataMap deps = wrapped.getDependants();
		if (deps != null && !deps.isEmpty()) {
			result.append(",deps=").append(deps);
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * @return the common fields of this object. Note that this may only be called for entities which have an Integer id.
	 * @see #getIntId()
	 */
	@Override
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

	@Override
	public void setDependants(DependantMasterDataMap mpDependants) {
		wrapped.setDependants(mpDependants);
	}

	@Override
	public DependantMasterDataMap getDependants() {
		return wrapped.getDependants();
	}
	
	/**
	 * @deprecated Use {@link #getDependants()}
	 */
	protected final DependantMasterDataMap getMdDependants() {
		return wrapped.getDependants();
	}

	@Override
	public List<TranslationVO> getResources() {
		return resources;
	}

	@Override
	public void setResources(List<TranslationVO> resources) {
		this.resources = resources;
	}

	/**
	 * Return the underlying EntityObjectVO.
	 * @since Nuclos 3.8
	 * @author Thomas Pasch
	 */
	@Override
	public EntityObjectVO getEntityObject() {
		return wrapped;
	}

	/**
	 * Return the entity name. This is a convenience method.
	 * @since Nuclos 3.8
	 * @author Thomas Pasch
	 */
	@Override
	public String getEntityName() {
		return wrapped.getEntity();
	}
	
	// override methods from AbstractNuclosValueObject
	
	/**
	 * mark underlying database record as to be removed from database
	 */
	@Override
	public void remove() {
		wrapped.flagRemove();
	}

	/**
	 * is underlying database record to be removed from database?
	 * @return boolean value
	 */
	@Override
	public boolean isRemoved() {
		return wrapped.isFlagRemoved();
	}

	/**
	 * get creation date (datcreated) of underlying database record
	 * @return created date of underlying database record
	 */
	@Override
	public Date getCreatedAt() {
		return wrapped.getChangedAt();
	}

	/**
	 * get creator (strcreated) of underlying database record
	 * @return creator of underlying database record
	 */
	@Override
	public String getCreatedBy() {
		return wrapped.getCreatedBy();
	}

	/**
	 * get last changed date (datchanged) of underlying database record
	 * @return last changed date of underlying database record
	 */
	@Override
	public Date getChangedAt() {
		return wrapped.getCreatedAt();
	}

	/**
	 * get last changer (strchanged) of underlying database record
	 * @return last changer of underlying database record
	 */
	@Override
	public String getChangedBy() {
		return wrapped.getCreatedBy();
	}

	/**
	 * get version (intversion) of underlying database record
	 * @return version of underlying database record
	 */
	@Override
	public int getVersion() {
		return wrapped.getVersion() == null ? -1 : wrapped.getVersion();
	}
	
	/**
	 * @since Nuclos 3.5
	 * @author Thomas Pasch
	 */
	@Override
	public void setVersion(int version) {
		wrapped.setVersion(version);
	}
	
	// end of override methods from AbstractNuclosValueObject

}	// class MasterDataVO


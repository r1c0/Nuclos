//Copyright (C) 2012  Novabit Informationssysteme GmbH
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

import java.util.List;
import java.util.Map;

import org.nuclos.common.TranslationVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.INuclosValueObject;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Interface to {@link org.nuclos.server.masterdata.valueobject.MasterDataVO}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.8
 * @param <Id> primary key type
 */
public interface IMasterDataVO extends INuclosValueObject<Object> {

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
	
	/**
	 * Return the underlying EntityObjectVO.
	 * @since Nuclos 3.8
	 * @author Thomas Pasch
	 */
	EntityObjectVO getEntityObject();
	
	/**
	 * Return the entity name. This is a convenience method.
	 * @since Nuclos 3.8
	 * @author Thomas Pasch
	 */
	String getEntityName();

	/**
	 * @return a clone of <code>this</code>.
	 * @postcondition result.isChanged() == this.isChanged()
	 * @postcondition result.isRemoved() == this.isRemoved()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == this.getId()
	 * @see #copy()
	 */
	MasterDataVO clone();

	void setChanged(boolean changed);

	boolean equals(Object obj);

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	MasterDataVO copy();

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	MasterDataVO copy(boolean blnWithDependants);

	/**
	 * @return this object's primary key
	 */
	Object getId();

	/**
	 * @return this object's primary key, which must be an Integer, otherwise a ClassCastException is thrown. Use getId()
	 * if you're not sure about the primary key's type.
	 */
	Integer getIntId();

	/**
	 * Set the integer id of this object.
	 * This is only allowed to keep the mpDependants up to date for newly created masterdata records, which is necessary for the logbook!
	 * @param iId
	 */
	void setId(Object iId);

	/**
	 * Returns true if this record is a system record.
	 */
	boolean isSystemRecord();

	/**
	 * @param sFieldName field name
	 * @return the value of the field with the given name.
	 */
	Object getField(String sFieldName);

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
	<T> T getField(String sFieldName, Class<T> cls);

	/**
	 * sets the field with the given name to the given value.
	 * @param sFieldName
	 * @param oValue
	 * @postcondition this.isChanged()
	 * @todo setChanged() only if the given value is different from the old value.
	 */
	void setField(String sFieldName, Object oValue);

	/**
	 * get all fields of master data record
	 * @return map of all fields for master data record
	 * @postcondition result != null
	 */
	Map<String, Object> getFields();

	/**
	 * sets the given fields
	 * @param mpFields Map<String sFieldName, Object oValue>
	 * @precondition mpFields != null
	 * @postcondition this.isChanged()
	 */
	void setFields(Map<String, Object> mpFields);

	/**
	 * CAUTION: this method should only be used in the context of reports, to hide the data
	 * of subforms if the user has no read permission on it
	 *
	 * set all fields to null
	 */
	void clearAllFields();

	/**
	 * checks if all fields in this masterdata cvo are empty (the given foreign key field is ignored)
	 * @param sForeignKey foreign key field to ignore (always filled anyway)
	 * @return Are all fields except for the foreign key field empty?
	 * @todo What is "the" foreign key field? There can be more than one per entity!
	 */
	boolean isEmpty(String sForeignKey);

	/**
	 * @return Has this object been changed since its creation?
	 */
	boolean isChanged();

	/**
	 * generic validity checker for master data records
	 * @param mdmetavo meta information to validateField against
	 * @precondition mdmetavo != null
	 * @deprecated Validation is performed by org.nuclos.server.validation.ValidationSupport.
	 */
	void validate(MasterDataMetaVO mdmetavo) throws CommonValidationException;

	String getDebugInfo();

	String toDescription();

	/**
	 * @return the common fields of this object. Note that this may only be called for entities which have an Integer id.
	 * @see #getIntId()
	 */
	NuclosValueObject getNuclosValueObject();

	void setDependants(DependantMasterDataMap mpDependants);

	DependantMasterDataMap getDependants();

	List<TranslationVO> getResources();

	void setResources(List<TranslationVO> resources);

}

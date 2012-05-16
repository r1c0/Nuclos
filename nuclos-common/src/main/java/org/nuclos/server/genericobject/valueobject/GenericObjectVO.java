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
package org.nuclos.server.genericobject.valueobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.GenericObjectMetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.attribute.BadGenericObjectException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a generic object with all its attributes.
 * <code>GenericObjectVO</code>s may be incomplete (partially loaded), meaning they contain only a selected set of attributes.
 * This allows us to efficiently load a large number of generic objects when only some attributes need to be displayed.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class GenericObjectVO extends NuclosValueObject implements Cloneable {

	/**
	 * name of the attribute, if any, that contains the name.
	 */
	public static final String ATTRIBUTENAME_NAME = "name";

	/**
	 * the id of the module this generic object belongs to.
	 */
	private int iModuleId;

	/**
	 * the id of the parent object (if any).
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	private Integer iParentId;

	/**
	 * the attribute values.
	 */
	private transient Map<Integer, DynamicAttributeVO> mpattrvo;

	/**
	 * Has this object been (logically) deleted?
	 */
	private boolean bDeleted;

	/**
	 * the process instance
	 */
	private Integer iInstanceId;

	/**
	 * the ids of the attributes that were loaded for this generic object. <code>null</code> if all attributes are
	 * available (complete GenericObjectVO), otherwise the object is incomplete and contains only those attributes
	 * whose ids are contained in this set.
	 */
	private final Set<Integer> stLoadedAttributeIds;

	/**
	 * constructor to be called by client only
	 * @param iModuleId module id of underlying database record
	 * @param iParentId id of parent generic object
	 * @param lometadataprovider used for setting default values, if != null
	 * @postcondition this.getId() == null
	 */
	public GenericObjectVO(int iModuleId, Integer iParentId, Integer iInstanceId, GenericObjectMetaDataProvider lometadataprovider) {
		super();
		this.iModuleId = iModuleId;
		this.iParentId = iParentId;
		this.iInstanceId = iInstanceId;
		this.mpattrvo = CollectionUtils.newHashMap();
		this.stLoadedAttributeIds = null;
		this.bDeleted = false;

		if (lometadataprovider != null) {
			for (AttributeCVO attrcvo : lometadataprovider.getAttributeCVOsByModuleId(iModuleId, false)) {
				if (attrcvo.getDefaultValue() != null) {
					this.setAttribute(new DynamicAttributeVO(attrcvo.getId(), attrcvo.getDefaultValueId(), attrcvo.getDefaultValue()));
				}
			}
		}
		assert this.getId() == null;
	}

	/**
	 * @param nvo contains the common fields.
	 * @param iModuleId module id of underlying database record
	 * @param iParentId id of parent generic object
	 * @param bDeleted Deletion flag
	 * @precondition permission != null
	 */
	public GenericObjectVO(NuclosValueObject nvo, Integer iModuleId, Integer iParentId, Integer iInstanceId,
			Set<Integer> stLoadedAttributeIds, boolean bDeleted) {
		super(nvo);
		this.iModuleId = iModuleId;
		this.iParentId = iParentId;
		this.iInstanceId = iInstanceId;
		this.mpattrvo = CollectionUtils.newHashMap();
		if (stLoadedAttributeIds != null) {
			this.stLoadedAttributeIds = new HashSet<Integer>(stLoadedAttributeIds);
		}
		else {
			this.stLoadedAttributeIds = null;
		}
		this.bDeleted = bDeleted;
	}

	/**
	 * copy constructor
	 * @param that
	 * @postcondition this.getId() == that.getId()
	 */
	protected GenericObjectVO(GenericObjectVO that) {
		this(that, that.getModuleId(), that.getParentId(), that.getInstanceId(), that.stLoadedAttributeIds, that.isDeleted());
		assert this.mpattrvo.isEmpty();
		this.mpattrvo.putAll(that.mpattrvo);
		assert this.getId() == that.getId();
	}

	/**
	 * @return an exact clone of this object.
	 * @postcondition result.getId() == this.getId()
	 */
	@Override
	public GenericObjectVO clone() {
		final GenericObjectVO result = (GenericObjectVO) super.clone();
		// deepen shallow copy:
		result.mpattrvo = new HashMap<Integer, DynamicAttributeVO>(this.mpattrvo);
		assert result.getId() == this.getId();
		return result;
	}

	/**
	 * @return a copy of this object that can be inserted into the database.
	 * @precondition this.isComplete()
	 * @postcondition result.getId() == null
	 * @postcondition result.getModuleId() == this.getModuleId()
	 * @postcondition result.getParentId() == this.getParentId()
	 */
	public GenericObjectVO copy() {
		if (!this.isComplete()) {
			throw new IllegalStateException("This object is not complete.");
		}
		final GenericObjectVO result = new GenericObjectVO(new NuclosValueObject(), this.getModuleId(), this.getParentId(),
			this.getInstanceId(), this.stLoadedAttributeIds, this.bDeleted);
		assert result.mpattrvo.isEmpty();
		result.mpattrvo.putAll(this.mpattrvo);

		assert result.getId() == null;
		assert result.getModuleId() == this.getModuleId();
		assert result.getParentId() == this.getParentId();
		return result;
	}

	/**
	 * @return Is this <code>GenericObjectVO</code> complete? That is, have all attribute values been read?
	 * For partially loaded objects, this method returns <code>false</code>.
	 */
	public boolean isComplete() {
		return (this.stLoadedAttributeIds == null);
	}

	public void addAttribute(Integer iAttributeId) {
		if (this.stLoadedAttributeIds != null) {
			this.stLoadedAttributeIds.add(iAttributeId);
		}
	}

	/**
	 * @return the id of the module this generic object belongs to.
	 */
	public int getModuleId() {
		return this.iModuleId;
	}

	/**
	 * sets the module id that this generic object belongs to.
	 */
	public void setModuleId(Integer iModuleId) {
		this.iModuleId = iModuleId;
	}

	/**
	 * @return the parent id, if any. null for main module objects, non-null for submodule objects.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public Integer getParentId() {
		return this.iParentId;
	}

	/**
	 * sets the parent id for submodule objects.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public void setParentId(Integer iParentId) {
		this.iParentId = iParentId;
	}

	/**
	 * get generic object attribute
	 * @param iAttributeId attribute id
	 * @return generic object attribute for attribute id
	 */
	public DynamicAttributeVO getAttribute(Integer iAttributeId) {
		this.checkAttributeIdLoaded(iAttributeId);
		return this.mpattrvo.get(iAttributeId);
	}

	/**
	 * get generic object attribute for the tree view, don't need to check if the attribute was loaded
	 * @param iAttributeId
	 * @return generic object attribute for attribute id
	 */
	public DynamicAttributeVO getAttributeForTreeView(Integer iAttributeId) {

		return this.mpattrvo.get(iAttributeId);
	}

	/**
	 * checks if the attribute with the given id is contained in this <code>GenericObjectVO</code>.
	 * @param iAttributeId
	 */
	private void checkAttributeIdLoaded(Integer iAttributeId) {
		if (!wasAttributeIdLoaded(iAttributeId)) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("govo.not.loaded.attribute.exception", iAttributeId));//"Das Attribut mit der Id " + iAttributeId + " ist nicht in dem partiell geladenen Objekt enthalten.");
		}
	}

	/**
	 * @param iAttributeId
	 * @return Was the value for the attribute with the given id loaded for this <code>GenericObjectVO</code>?
	 * @postcondition this.isComplete() --> result
	 * @see #isComplete()
	 */
	public boolean wasAttributeIdLoaded(Integer iAttributeId) {
		return this.isComplete() || this.stLoadedAttributeIds.contains(iAttributeId);

	}

	/**
	 * get generic object attribute
	 * @param sAttribute attribute name
	 * @param attrprovider
	 * @return generic object attribute for attribute name
	 */
	public DynamicAttributeVO getAttribute(String sAttribute, AttributeProvider attrprovider) {
		return this.getAttribute(attrprovider.getAttribute(this.iModuleId, sAttribute).getId());
	}

	/**
	 * set generic object attribute
	 * @param attrvo generic object attribute
	 */
	public void setAttribute(DynamicAttributeVO attrvo) {
		final Integer iAttributeId = attrvo.getAttributeId();

		this.checkAttributeIdLoaded(iAttributeId);

		this.mpattrvo.put(iAttributeId, attrvo);
	}

	/**
	 * get all attributes for generic object
	 * @return Collection<DynamicAttributeVO> attributes for generic object
	 */
	public Collection<DynamicAttributeVO> getAttributes() {
		return this.mpattrvo.values();
	}

	/**
	 * set all attributes for generic object
	 * @param collattrvo attributes for generic object
	 */
	public void setAttributes(Collection<DynamicAttributeVO> collattrvo) {
		this.mpattrvo = new HashMap<Integer, DynamicAttributeVO>(collattrvo.size(), 1.0f);

		for (DynamicAttributeVO attrvo : collattrvo) {
			this.setAttribute(attrvo);
		}
	}

	/**
	 * generic validity checker for genericobject records; used by the console command -checkattributevalues.
	 * @param attrprovider Attribute provider.
	 * @throws BadGenericObjectException
	 * @deprecated Validation is performed by org.nuclos.server.validation.ValidationSupport.
	 */
	public void validate(AttributeProvider attrprovider) throws BadGenericObjectException {
		// does nothing
	}

	/**
	 * @param attrprovider
	 * @return the usage criteria of this generic object.
	 * @postcondition result != null
	 */
	public UsageCriteria getUsageCriteria(AttributeProvider attrprovider) {
		return new UsageCriteria(this.getModuleId(),
				this.getSystemAttributeValueId(NuclosEOField.PROCESS.getMetaData().getId().intValue()),
				this.getSystemAttributeValueId(NuclosEOField.STATE.getMetaData().getId().intValue()));
	}

	/**
	 * @return this object's system identifier, if any.
	 */
	public String getSystemIdentifier() {
		final DynamicAttributeVO attrvo = this.getAttribute(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
		return (attrvo == null) ? null : (String) attrvo.getValue();
	}

	private Integer getSystemAttributeValueId(Integer iAttributeId) {
		final DynamicAttributeVO attrvo = this.getAttribute(iAttributeId);
		return (attrvo == null) ? null : attrvo.getValueId();
	}

	/**
	 * @return Has this object been (logically) deleted?
	 */
	public boolean isDeleted() {
		return this.bDeleted;
	}

	/**
	 * marks the generic object as deleted (logical)
	 */
	public void setDeleted(boolean bDeleted) {
		this.bDeleted = bDeleted;
	}

	/**
	 * serializes this object, efficiently writing the map of attribute values.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		oos.writeInt(mpattrvo.size());
		for (DynamicAttributeVO attrvo : mpattrvo.values()) {
			oos.writeObject(attrvo);
		}
	}

	/**
	 * deserializes this object.
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();

		final int iAttributeCount = ois.readInt();
		this.mpattrvo = new HashMap<Integer, DynamicAttributeVO>(iAttributeCount);
		for (int i = 0; i < iAttributeCount; ++i) {
			final DynamicAttributeVO attrvo = (DynamicAttributeVO) ois.readObject();
			mpattrvo.put(attrvo.getAttributeId(), attrvo);
		}
	}

	/**
	 * get the status id of this object
	 * @return statusId
	 */
	public Integer getStatusId() {
		DynamicAttributeVO attributeVO = getAttribute(NuclosEOField.STATE.getMetaData().getField(), 
				SpringApplicationContextHolder.getBean(GenericObjectMetaDataProvider.class));
		return (attributeVO != null) ? (Integer)attributeVO.getValueId() : null;
	}

	/**
	 * get the process id of this object
	 * @return processId
	 */
	public Integer getProcessId() {
		DynamicAttributeVO attributeVO = getAttribute(NuclosEOField.PROCESS.getMetaData().getField(), 
				SpringApplicationContextHolder.getBean(GenericObjectMetaDataProvider.class));
		return (attributeVO != null) ? (Integer)attributeVO.getValueId() : null;
	}

	/**
	 * first add attribute if it wasn't loaded and then set the given attribute
	 * @param attrvo
	 */
	public void addAndSetAttribute(DynamicAttributeVO attrvo) {
		if (!wasAttributeIdLoaded(attrvo.getAttributeId())) {
			addAttribute(attrvo.getAttributeId());
		}

		setAttribute(attrvo);
	}

	/**
	 * first add attribute if it wasn't loaded and then set the given attribute
	 * @param collattrvo
	 */
	public void addAndSetAttribute(Collection<DynamicAttributeVO> collattrvo) {
		for (DynamicAttributeVO attrvo : collattrvo) {
			addAndSetAttribute(attrvo);
		}
	}

	/**
	 *
	 * @return
	 */
	public Integer getInstanceId() {
		return iInstanceId;
	}

	/**
	 *
	 * @param instanceId
	 */
	public void setInstanceId(Integer instanceId) {
		iInstanceId = instanceId;
	}
	
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("GoVO[id=").append(iInstanceId);
		result.append(",moduleId=").append(iModuleId);
		if (bDeleted) {
			result.append(",deleted=").append(bDeleted);
		}
		if (iParentId != null) {
			result.append(",parentId=").append(iParentId);
		}
		result.append(",fields=").append(mpattrvo);
		result.append("]");
		return result.toString();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("GenericObjectVO[");
		result.append("id=").append(getId());
		result.append(",moduleId=").append(getModuleId());
		result.append(",instanceId=").append(getInstanceId());
		result.append(",parentId=").append(getParentId());
		result.append(",statusId=").append(getStatusId());
		result.append("]");
		return result.toString();
	}
	
}	// class GenericObjectVO

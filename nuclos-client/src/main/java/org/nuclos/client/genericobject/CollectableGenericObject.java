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
package org.nuclos.client.genericobject;

import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Removable;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Makes a <code>GenericObjectVO</code> <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObject extends AbstractCollectable implements Removable {

	public static final String ATTRIBUTENAME_NAME = GenericObjectVO.ATTRIBUTENAME_NAME;
	
	private final GenericObjectVO govo;

	private final Map<String, CollectableField> mpFields = CollectionUtils.newHashMap();

	/**
	 * @param govo
	 * @precondition govo != null
	 */
	public CollectableGenericObject(GenericObjectVO govo) {
		if (govo == null) {
			throw new NullArgumentException("govo");
		}
		this.govo = govo;
	}

	@Override
	public Integer getId() {
		return this.getGenericObjectCVO().getId();
	}

	/**
	 * @return the encapsulated <code>GenericObjectVO</code>.
	 * @postcondition result != null
	 */
	public GenericObjectVO getGenericObjectCVO() {
		final GenericObjectVO result = this.govo;
		assert result != null;
		return result;
	}

	public CollectableEntity getCollectableEntity() {
		return CollectableGenericObjectEntity.getByModuleId(this.govo.getModuleId());
	}

	@Override
	public String getIdentifierLabel() {
		final String sKeyIdentifier = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();
		return LangUtils.toString(this.getValue(sKeyIdentifier));
	}

	@Override
	public int getVersion() {
		return this.govo.getVersion();
	}

	@Override
	public boolean isMarkedRemoved() {
		return this.getGenericObjectCVO().isRemoved();
	}

	@Override
	public void markRemoved() {
		this.getGenericObjectCVO().remove();
	}

	/**
	 * @param sName
	 * @return the attribute value with the given name, if any.
	 */
	private DynamicAttributeVO getAttributeValueByName(String sName) {
		return this.govo.getAttribute(AttributeCache.getInstance().getAttribute(this.govo.getModuleId(), sName).getId());
	}

	private static DynamicAttributeVO newGenericObjectAttributeVO(Integer iEntityId, String sFieldName) {
		return newGenericObjectAttributeVO(iEntityId, sFieldName, null, null);
	}

	private static DynamicAttributeVO newGenericObjectAttributeVO(Integer iEntityId, String sFieldName, Integer iValueId, Object oValue) {
		return new DynamicAttributeVO(AttributeCache.getInstance().getAttribute(iEntityId, sFieldName).getId(), iValueId, oValue);
	}

	@Override
	public boolean isComplete() {
		return this.getGenericObjectCVO().isComplete();
	}

	@Override
	public CollectableField getField(String sFieldName) {
		CollectableField result;

		// mpFields is used as a cache:
		result = this.mpFields.get(sFieldName);
		if (result == null) {
			DynamicAttributeVO attrvo = this.getAttributeValueByName(sFieldName);
			if (attrvo == null || attrvo.isRemoved()) {
				attrvo = newGenericObjectAttributeVO(this.govo.getModuleId(), sFieldName);
			}
			final CollectableEntityField clctef = this.getCollectableEntity().getEntityField(sFieldName);
			if (clctef == null) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("CollectableGenericObject.1", "Unbekanntes Attribut: {0}", sFieldName));
			}
			result = new CollectableGenericObjectAttributeField(attrvo, clctef.getFieldType());
			/** @todo reactivate cache? */
			//			this.mpFields.put(sFieldName, result);
			
		}
		assert result != null;
		return result;
	}

	/**
	 * @param sFieldName
	 * @param clctfValue
	 * @precondition clctfValue != null
	 * @precondition (clctfValue.getFieldType() == CollectableEntityField.TYPE_VALUEIDFIELD) -> (clctfValue.getValueId() instanceof Integer)
	 */
	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		if (clctfValue == null) {
			throw new NullArgumentException("clctfValue");
		}

		if (clctfValue.isNull()) {
			// remove from cvo:
			final DynamicAttributeVO attrvo = this.getAttributeValueByName(sFieldName);
			if (attrvo != null) {
				attrvo.remove();
			}
		}
		else {
			// 1. cvo \u00e4ndern:
			DynamicAttributeVO attrvo = this.getAttributeValueByName(sFieldName);
			if (attrvo == null) {
				attrvo = newGenericObjectAttributeVO(this.govo.getModuleId(), sFieldName);
			}
			else if (attrvo.isRemoved()) {
				attrvo.unremove();
			}
			assert !attrvo.isRemoved();

			attrvo.setValue(clctfValue.getValue());
			if (this.getCollectableEntity().getEntityField(sFieldName).isIdField()) {
				attrvo.setValueId((Integer) clctfValue.getValueId());
			}

			/** @todo OPTIMIZE: This method is very inefficient - check if this is an issue! */
			this.govo.setAttribute(attrvo);

			// 2. Evtl. vorhandenen Cache-Eintrag l\u00f6schen:
			this.mpFields.remove(sFieldName);
		}

		assert this.getField(sFieldName).equals(clctfValue) :
			CommonLocaleDelegate.getMessage("CollectableGenericObject.2", 
				"Das Feld {0} in der Entit\u00e4t {1} enth\u00e4lt den Wert {2} (erwartet: {3})", 
				sFieldName, getCollectableEntity().getName(), this.getField(sFieldName), clctfValue); 
//			"Das Feld " + sFieldName + " in der Entit\u00e4t " + getCollectableEntity().getName() + " enth\u00e4lt den Wert " +
//						this.getField(sFieldName) + " (erwartet: " + clctfValue + ")";
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getGenericObjectCVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
}	// class CollectableGenericObject

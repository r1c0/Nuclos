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
package org.nuclos.common.genericobject;

import java.util.Date;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.attribute.ComponentType;
import org.nuclos.common.collect.collectable.AbstractCollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO.DataType;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Provides structural (meta) information about a field of a leased object.
 * Formerly known as <code>CollectableAttributeEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectEntityField extends AbstractCollectableEntityField {

	private final AttributeCVO attrcvo;
	private final EntityFieldMetaDataVO  entityFieldMeta;
	
	public CollectableGenericObjectEntityField(AttributeCVO attrcvo, EntityFieldMetaDataVO entityFieldMeta) {
		this.attrcvo = attrcvo;
		this.entityFieldMeta = entityFieldMeta;
	}

	public AttributeCVO getAttributeCVO() {
		return this.attrcvo;
	}

	public EntityFieldMetaDataVO getFieldMeta() {
		return entityFieldMeta;
	}
	
	@Override
	public String getDescription() {
		return CommonLocaleDelegate.getDescriptionFromAttributeCVO(attrcvo);
	}

	@Override
	public boolean isReferencing() {
		return (attrcvo.getExternalEntity() != null);
	}

	@Override
	public String getReferencedEntityName() {
		return attrcvo.getExternalEntity();
	}

	@Override
	public String getReferencedEntityFieldName() {
		final String sFieldNameDefault = attrcvo.isShowMnemonic() ? MasterDataVO.FIELDNAME_MNEMONIC : MasterDataVO.FIELDNAME_NAME;
		return LangUtils.defaultIfNull(attrcvo.getExternalEntityFieldName(), sFieldNameDefault);
	}

	@Override
	public Class<?> getJavaClass() {
		return attrcvo.getJavaClass();
	}

	@Override
	public String getLabel() {
		return CommonLocaleDelegate.getLabelFromAttributeCVO(attrcvo);
	}

	@Override
	public Integer getMaxLength() {
		// for dates, the maximum length is always 10:
		/** @todo maybe this should be written to the database when the attribute is stored. */
		if (attrcvo.getDataType() == DataType.DATE) {
			return 10;
		}
		return attrcvo.getDataScale();
	}
	
	@Override
	public Integer getPrecision() {
		return attrcvo.getDataPrecision();
	}

	@Override
	public String getName() {
		return attrcvo.getName();
	}

	@Override
	public String getFormatInput() {
		return attrcvo.getInputFormat();
	}
	
	@Override
	public String getFormatOutput() {
		return attrcvo.getOutputFormat();
	}
	
	@Override
	public boolean isIdField() {
		return this.attrcvo.isIdField();
	}

	@Override
	public int getFieldType() {
		return this.isIdField() ? CollectableEntityField.TYPE_VALUEIDFIELD : CollectableEntityField.TYPE_VALUEFIELD;
	}

	@Override
	public boolean isRestrictedToValueList() {
		// NUCLOSINT-442 deactivated! We need a new concept for insertable...
//		return !this.attrcvo.isInsertable();
		if (this.isIdField())
			return true;
		else 
			return !this.attrcvo.isInsertable();
	}

	@Override
	public boolean isNullable() {
		return attrcvo.isNullable();
	}

	public boolean isShowMnemonic() {
		return attrcvo.isShowMnemonic();
	}

	@Override
	public CollectableField getDefault() {
		final CollectableField result;

		if (this.isIdField()) {
			result = new CollectableValueIdField(attrcvo.getDefaultValueId(), attrcvo.getDefaultValue());
		}
		else {
			result = new CollectableValueField(attrcvo.getDefaultValue());
		}

		assert result != null;
		assert result.getFieldType() == this.getFieldType();
		if (!(this.getJavaClass()== Date.class && result.getValue() != null && result.getValue().toString().equalsIgnoreCase(RelativeDate.today().toString())))
			assert LangUtils.isInstanceOf(result.getValue(), this.getJavaClass());
		return result;
	}

	/**
	 * @return the default collectable component type for the three flags (searchable, modifiable, insertable).
	 */
	@Override
	public int getDefaultCollectableComponentType() {
		final int result;
		switch (ComponentType.findByFlags(attrcvo.isSearchable(), attrcvo.isModifiable(), attrcvo.isInsertable())) {
			case TEXTFIELD:
				// fall back to the super implementation, as it is more precise here:
				result = super.getDefaultCollectableComponentType();
				break;
			case COMBOBOX:
			case DROPDOWN:
				result = CollectableComponentTypes.TYPE_COMBOBOX;
				break;
			case LISTOFVALUES:
				result = CollectableComponentTypes.TYPE_LISTOFVALUES;
				break;
			default:
				throw new NuclosFatalException("clctgoef.missing.componenttype.error");
					//"Es gibt keinen passenden Komponenten-Typ f\u00fcr diese Kombination.");
		}
		return result;
	}

}	// class CollectableGenericObjectEntityField

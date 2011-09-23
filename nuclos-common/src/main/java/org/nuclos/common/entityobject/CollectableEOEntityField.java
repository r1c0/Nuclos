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
package org.nuclos.common.entityobject;

import java.util.prefs.Preferences;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.collectable.AbstractCollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.masterdata.EnumeratedDefaultValueProvider;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * <p>
 * TODO: Consider {@link org.nuclos.client.common.CollectableEntityFieldPreferencesUtil}
 * to write to {@link Preferences}.
 * </p>
 */
public class CollectableEOEntityField extends AbstractCollectableEntityField {

	private final String entityName;

	private final EntityFieldMetaDataVO efMeta;

	public CollectableEOEntityField(EntityFieldMetaDataVO efMeta, String entityName) {
		if (efMeta == null || entityName == null) {
			throw new NullArgumentException("efMeta");
		}
		this.efMeta = efMeta;
		this.entityName = entityName;
	}

	@Override
	public CollectableField getDefault() {
		try {
			final String sDefault = this.efMeta.getDefaultValue();
			if(sDefault == null || sDefault.length() == 0) {
				return super.getDefault();
			}
			else {
				Object o = SpringApplicationContextHolder.getBean("enumeratedDefaultValueProvider");
				if (o != null && o instanceof EnumeratedDefaultValueProvider) {
					return ((EnumeratedDefaultValueProvider)o).getDefaultValue(this.efMeta);
				}
			}
		}
		catch(Exception ex) {
			// on exception return super.getDefault()
			return super.getDefault();
		}

		return super.getDefault();
	}


	@Override
	public String getDescription() {
		return CommonLocaleDelegate.getTextFallback(efMeta.getLocaleResourceIdForDescription(), efMeta.getFallbacklabel());
	}

	@Override
	public int getFieldType() {
		return efMeta.getForeignEntity()!=null ? CollectableEntityField.TYPE_VALUEIDFIELD: CollectableEntityField.TYPE_VALUEFIELD;
	}

	@Override
	public String getFormatInput() {
		return efMeta.getFormatInput();
	}

	@Override
	public String getFormatOutput() {
		return efMeta.getFormatOutput();
	}

	@Override
	public Class<?> getJavaClass() {
		try {
			return Class.forName(efMeta.getDataType());
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public String getLabel() {
		return CommonLocaleDelegate.getTextFallback(efMeta.getLocaleResourceIdForLabel(), efMeta.getFallbacklabel());
	}

	@Override
	public Integer getMaxLength() {
		// @TODO bei Dates nicht auch \u00fcber locale gesteuert?
		if (java.util.Date.class.getName().equals(efMeta.getDataType())) {
			return 10;
		}
		return efMeta.getScale();
	}

	@Override
	public String getName() {
		return efMeta.getField();
	}

	@Override
	public Integer getPrecision() {
		return efMeta.getPrecision();
	}

	@Override
	public String getReferencedEntityName() {
		return efMeta.getForeignEntity();
	}

	@Override
	public String getReferencedEntityFieldName() {
		return efMeta.getForeignEntityField();
	}

	@Override
	public boolean isNullable() {
		return efMeta.isNullable();
	}

	@Override
	public boolean isReferencing() {
		return efMeta.getForeignEntity() != null;
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	public EntityFieldMetaDataVO getMeta() {
		return efMeta;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof CollectableEOEntityField)) return false;
		final CollectableEOEntityField o = (CollectableEOEntityField) other;
		return efMeta.equals(o.efMeta);
	}
	
	@Override
	public int hashCode() {
		int result = efMeta.hashCode();
		return result + 3971;
	}

}

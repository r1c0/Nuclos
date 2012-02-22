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
package org.nuclos.common.masterdata;

import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.collectable.AbstractCollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Structure (meta data) of a foreign key field in a masterdata table.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: Consider {@link org.nuclos.client.common.CollectableEntityFieldPreferencesUtil}
 * to write to {@link Preferences}.
 * </p>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableMasterDataForeignKeyEntityField extends AbstractCollectableEntityField {

	private static final Logger LOG = Logger.getLogger(CollectableMasterDataForeignKeyEntityField.class);

	private final String entityName;

	/**
	 * @deprecated
	 */
	private final MasterDataMetaFieldVO mdmetafield;

	/**
	 * creates a <code>CollectableEntityField</code> representing the foreign key field <code>mdmetafield</code>.
	 * The collection of enumerated fields is built lazily.
	 * @param mdmetafield
	 */
	public CollectableMasterDataForeignKeyEntityField(MasterDataMetaFieldVO mdmetafield, String entityName) {
		this.mdmetafield = mdmetafield;
		this.entityName = entityName;
		final String sForeignEntity = mdmetafield.getForeignEntity();
		if (sForeignEntity == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("clctmdfkef.missing.foreignentity.error", mdmetafield.getFieldName()));
		}
	}

	/**
	 * @postcondition result != null
	 */
	@Override
	public String getReferencedEntityName() {
		final String result = this.mdmetafield.getForeignEntity();
		assert result != null;
		return result;
	}

	@Override
	public String getReferencedEntityFieldName() {
		final String sFieldNameDefault = MasterDataVO.FIELDNAME_NAME;
		return LangUtils.defaultIfNull(this.mdmetafield.getForeignEntityField(), sFieldNameDefault);
	}

	/**
	 * @return always <code>true</code>
	 * @postcondition result
	 */
	@Override
	public boolean isReferencing() {
		return true;
	}

	@Override
	public String getName() {
		return this.mdmetafield.getFieldName();
	}

	@Override
	public String getFormatInput() {
		return mdmetafield.getInputFormat();
	}

	@Override
	public String getFormatOutput() {
		return mdmetafield.getOutputFormat();
	}

	@Override
	public int getFieldType() {
		return CollectableField.TYPE_VALUEIDFIELD;
	}

	@Override
	public Class<?> getJavaClass() {
		return this.mdmetafield.getJavaClass();
	}

	@Override
	public String getDescription() {
		return SpringLocaleDelegate.getInstance().getTextFallback(
				mdmetafield.getResourceSIdForDescription(), this.mdmetafield.getDescription());
	}

	@Override
	public String getLabel() {
		return SpringLocaleDelegate.getInstance().getTextFallback(
				mdmetafield.getResourceSIdForLabel(), this.mdmetafield.getLabel());
	}

	@Override
	public Integer getMaxLength() {
		return this.mdmetafield.getDataScale();
	}

	@Override
	public Integer getPrecision() {
		return this.mdmetafield.getDataPrecision();
	}

	@Override
	public boolean isNullable() {
		return this.mdmetafield.isNullable();
	}

	@Override
	public int getDefaultCollectableComponentType() {
		return (this.mdmetafield.isSearchable()) ? CollectableComponentTypes.TYPE_LISTOFVALUES : super.getDefaultCollectableComponentType();
	}

	@Override
	public boolean isRestrictedToValueList() {
	// see NUCLOSINT-442
		return true;
	}

	@Override
	public CollectableField getDefault() {
		try {
			final String sDefault = this.mdmetafield.getDefaultValue();
			if(sDefault == null || sDefault.length() == 0) {
				return super.getDefault();
			}
			else if(this.mdmetafield.getForeignEntity() == null || this.mdmetafield.getForeignEntityField() == null) {
				return super.getDefault();
			}
			else {
				Object o = SpringApplicationContextHolder.getBean("enumeratedDefaultValueProvider");
				if (o != null && o instanceof EnumeratedDefaultValueProvider) {
					return ((EnumeratedDefaultValueProvider)o).getDefaultValue(this.mdmetafield);
				}
			}
		}
		catch(Exception e) {
			// on exception return super.getDefault()
			LOG.info("getDefault: " + e);
			return super.getDefault();
		}
		return super.getDefault();
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

}	// class CollectableMasterDataForeignKeyEntityField

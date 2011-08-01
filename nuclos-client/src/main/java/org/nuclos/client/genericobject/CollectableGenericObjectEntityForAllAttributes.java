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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.AbstractCollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Contains meta information about leased objects for all attributes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectEntityForAllAttributes implements CollectableEntity {

	private String sEntityName = null;

	private String sEntityLabel = null;

	/**
	 * caches the dummmy entities - entitynames are keys
	 * @todo this is just a workaround - eliminate!
	 */
	private static Hashtable<String, CollectableGenericObjectEntityForAllAttributes> singletonHash = new Hashtable<String, CollectableGenericObjectEntityForAllAttributes>();

	/**
	 * Map<String, CollectableEntityField> the virtual fields that reference the parent objects in submodules.
	 */
	private final Map<String, CollectableEntityField> mpVirtualFields = CollectionUtils.newHashMap();

	/**
	 * Use getInstance() to get an instance of this class.
	 */
	protected CollectableGenericObjectEntityForAllAttributes(String sEntityName, String sEntityLabel) {
		this.sEntityLabel = sEntityLabel;
		this.sEntityName = sEntityName;

		this.initializeVirtualFields();
	}

	/**
	 * initializes the virtual fields that reference the parent object in submodules.
	 */
	private void initializeVirtualFields() {
		final Modules modules = Modules.getInstance();
		for (MasterDataVO mdvoModule : modules.getModules(true)) {
			final Integer iModuleId = mdvoModule.getIntId();
			if (modules.isSubModule(iModuleId)) {
				final String sEntityName = modules.getEntityNameByModuleId(iModuleId);
				final String sFieldName = CollectableGenericObjectEntity.getParentObjectFieldName(sEntityName);
				final String sReferencedEntityName = Modules.getParentModuleName(mdvoModule);
				this.mpVirtualFields.put(sFieldName, new AbstractCollectableEntityField() {

					@Override
					public String getDescription() {
						return "Verweis auf \u00fcbergeordneten Datensatz";
					}

					@Override
					public int getFieldType() {
						return CollectableEntityField.TYPE_VALUEIDFIELD;
					}

					@Override
					public Class<?> getJavaClass() {
						return String.class;
					}

					@Override
					public String getLabel() {
						return "Referenz auf Vaterobjekt";
					}

					@Override
					public Integer getMaxLength() {
						return null;
					}
					
					@Override
					public Integer getPrecision() {
						return null;
					}

					@Override
					public String getName() {
						return sFieldName;
					}
					
					@Override
					public String getFormatInput() {
						return null;
					}
					
					@Override
					public String getFormatOutput() {
						return null;
					}

					@Override
					public String getReferencedEntityName() {
						return sReferencedEntityName;
					}

					@Override
					public boolean isNullable() {
						return false;
					}

					@Override
					public boolean isReferencing() {
						return true;
					}

					@Override
					public CollectableEntity getCollectableEntity() {
						return null;
					}

					@Override
					public void setCollectableEntity(CollectableEntity clent) {
					}

					@Override
					public String getEntityName() {
						return sEntityName;
					}
				});
			}
		}
	}

	/**
	 * @return the one and only instance of CollectableGenericObjectEntityForAllAttributes
	 */
	public static synchronized CollectableGenericObjectEntityForAllAttributes getInstance(String sEntityName) {
		return singletonHash.get(sEntityName);
	}

	private AttributeCVO getAttributeCVO(String sAttributeName) {
		return AttributeCache.getInstance().getAttribute(this.sEntityName, sAttributeName);
	}
	
	/**
	 * @return the artificial entity "generalsearch"
	 */
	@Override
	public String getName() {
		return sEntityName;
		// This is not 100% clean, however needed, especially for layouts, where we don't know the module yet.
		// A layout can exist without being assigned to a module or being assigned to multiple modules.
		// An alternative would be to return null here, but that would require that getName() may return null
		// generally, and that doesn't look cleaner.
	}

	@Override
	public String getLabel() {
		return sEntityLabel;
	}

	@Override
	public CollectableEntityField getEntityField(String sFieldName) {
		CollectableEntityField result = this.mpVirtualFields.containsKey(sFieldName) ?
				this.mpVirtualFields.get(sFieldName) :
				new CollectableGenericObjectEntityField(
					getAttributeCVO(sFieldName),
					MetaDataClientProvider.getInstance().getEntityField(sEntityName, sFieldName),
					sEntityName);
		result.setCollectableEntity(this);
		return result;
	}

	@Override
	public Set<String> getFieldNames() {
		// attributes of all modules
		Set<String> stNames = new HashSet<String>();
		final Collection<AttributeCVO> collattrcvo = GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(null, false);
		for (AttributeCVO attrcvo : collattrcvo) {
			stNames.add(attrcvo.getName());
		}
	
		// additional system attributes
		stNames.add(NuclosEOField.CREATEDAT.getMetaData().getField());
		stNames.add(NuclosEOField.CREATEDBY.getMetaData().getField());
		stNames.add(NuclosEOField.CHANGEDAT.getMetaData().getField());
		stNames.add(NuclosEOField.CHANGEDBY.getMetaData().getField());
		
		return CollectionUtils.union(stNames, this.mpVirtualFields.keySet());
	}

	@Override
	public String getIdentifierFieldName() {
		return NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();
	}

	@Override
	public int getFieldCount() {
		return getFieldNames().size();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("name=").append(sEntityName);
		result.append(",label=").append(sEntityLabel);
		// result.append(",fields=").append(mpclctef);
		result.append("]");
		return result.toString();
	}

}	// class CollectableGenericObjectEntityForAllAttributes

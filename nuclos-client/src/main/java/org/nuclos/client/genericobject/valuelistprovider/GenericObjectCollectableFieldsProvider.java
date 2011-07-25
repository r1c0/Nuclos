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
package org.nuclos.client.genericobject.valuelistprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.entityobject.EntityFacadeDelegate;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectAttributeField;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProvider;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.valuelistprovider.cache.CacheableCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.cache.ManagedCollectableFieldsProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Provides <code>CollectableField</code>s from the attribute values of a <code>CollectableGenericObjectEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @deprecated Does not use EO mechanism
 */
@Deprecated
public class GenericObjectCollectableFieldsProvider extends ManagedCollectableFieldsProvider implements CacheableCollectableFieldsProvider {

	private CollectableEntityField clctef;

	public GenericObjectCollectableFieldsProvider(CollectableEntityField clctef) {
		this.clctef = clctef;
	}

	@Override
	public void setParameter(String sName, Object oValue) {
		// ignored
	}

	@Override
	public Object getCacheKey() {
		// the cache key is field name + name of referenced entity/field (if any)
		return Arrays.<Object>asList(
			clctef.getName(),
			clctef.isReferencing() ? clctef.getReferencedEntityName() : null,
			clctef.isReferencing() ? clctef.getReferencedEntityFieldName() : null,
			this.getIgnoreValidity());
	}
	
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result;

		if (this.clctef.isReferencing()) {
			if (Modules.getInstance().isModuleEntity(clctef.getReferencedEntityName())) {
				final String sFieldName = this.clctef.getReferencedEntityFieldName();
				result = getCollectableFieldsByName(sFieldName, true);
			}
			else {
				final String sFieldName = this.clctef.getReferencedEntityFieldName();	
				// NUCLOSINT-4
				if (NuclosEOField.STATE.getMetaData().getField().equals(this.clctef.getName()) || 
					NuclosEOField.STATENUMBER.getMetaData().getField().equals(this.clctef.getName())) {

					boolean bShowName = NuclosEOField.STATE.getMetaData().getField().equals(this.clctef.getName());
					Collection<StateVO> states = StateDelegate.getInstance().getStatesByModule(MetaDataClientProvider.getInstance().getEntity(this.clctef.getCollectableEntity().getName()).getId().intValue());

					result = new ArrayList<CollectableField>();
					for (StateVO state : states) {
						CollectableField clctf = new CollectableValueField(bShowName?state.getStatename():state.getNumeral());
						if (!result.contains(clctf)) {
							result.add(clctf);
						}
					}
				} else {
					final MasterDataCollectableFieldsProvider clctfprovider = new MasterDataCollectableFieldsProvider(clctef.getReferencedEntityName());
					clctfprovider.setIgnoreValidity(this.getIgnoreValidity());
					result = StringUtils.isNullOrEmpty(sFieldName) ? clctfprovider.getCollectableFields() : clctfprovider.getCollectableFieldsByName(sFieldName, true && !this.getIgnoreValidity());
				}
			}
		}
		else if (clctef instanceof CollectableGenericObjectEntityField){
			final CollectableGenericObjectEntityField goEntityField = (CollectableGenericObjectEntityField) clctef;
			class MakeCollectableField implements Transformer<AttributeValueVO, CollectableField> {
				@Override
				public CollectableField transform(AttributeValueVO attrvalue) {
					final DynamicAttributeVO loavo;
					try {
						final String sValue = goEntityField.isShowMnemonic() ? attrvalue.getMnemonic() : attrvalue.getValue();
						loavo = DynamicAttributeVO.createGenericObjectAttributeVOCanonical(goEntityField.getAttributeCVO().getId(), attrvalue.getId(), sValue, AttributeCache.getInstance());
					}
					catch (CommonValidationException ex) {
						throw new NuclosFatalException(ex);
					}
					return new CollectableGenericObjectAttributeField(loavo, goEntityField.getFieldType());
				}
			}	// inner class MakeCollectableField

			List<AttributeValueVO> filtered = CollectionUtils.applyFilter(goEntityField.getAttributeCVO().getValues(), new Predicate<AttributeValueVO>() {
				private Calendar today;
				private Calendar endOfToday;
				{
					Calendar base = Calendar.getInstance();
					// "now" without a time
					today = Calendar.getInstance();
					today.clear();
					for(int field : new int[] { Calendar.YEAR, Calendar.MONTH, Calendar.DATE })
						today.set(field, base.get(field));
					endOfToday = (Calendar) today.clone();
					endOfToday.set(Calendar.HOUR, 23);
					endOfToday.set(Calendar.MINUTE, 59);
					endOfToday.set(Calendar.SECOND, 59);
				}

				@Override
				public boolean evaluate(AttributeValueVO t) {
					return (t.getValidFrom() == null ||  t.getValidFrom().before(endOfToday.getTime()))
					&&   (t.getValidUntil() == null || t.getValidUntil().after(today.getTime()) || t.getValidUntil().equals(today.getTime()));
				}}
			);
			Collection<AttributeValueVO> valid = (getIgnoreValidity()? goEntityField.getAttributeCVO().getValues() : filtered);
			result = CollectionUtils.transform(valid, new MakeCollectableField());
		} else {
			throw new NuclosFatalException("Field " + clctef.getName() + " of entity " + clctef.getCollectableEntity().getName() + " is neither a generic object field nor a reference field");
		}

		Collections.sort(result);
		return result;
	}

	/**
	 * returns a list of collectable fields for an attribute name.
	 * @param sAttributeName
	 * @param bValid
	 * @return List<CollectableField>
	 */
	public List<CollectableField> getCollectableFieldsByName(String sAttributeName, boolean bValid) throws CommonBusinessException {
		return getCollectableFieldsByName(clctef.getReferencedEntityName(),
				(sAttributeName != null) ? sAttributeName : CollectableGenericObject.ATTRIBUTENAME_NAME,
				bValid);
	}

	private synchronized List<CollectableField> getCollectableFieldsByName(String sEntityName, String sAttributeName, boolean bValid) throws CommonBusinessException {
		return EntityFacadeDelegate.getInstance().getCollectableFieldsByName(sEntityName, sAttributeName, (!getIgnoreValidity() && bValid));
	}

}	// class GenericObjectCollectableFieldsProvider

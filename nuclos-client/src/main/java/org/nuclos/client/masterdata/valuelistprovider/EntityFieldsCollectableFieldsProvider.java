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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Collectable fields provider for masterdata fields belonging to a certain (masterdata or module) entity. 
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class EntityFieldsCollectableFieldsProvider implements CollectableFieldsProvider{
	
	private static final Logger log = Logger.getLogger(EntityFieldsCollectableFieldsProvider.class);

	private Integer iEntityId;
	
	private boolean blnWithoutSystemFields;
	
	public static final String WITH_SYSTEMFIELDS = "with systemfields";
	public static final String WITHOUT_SYSTEMFIELDS = "without systemfields";
	
	@Override
	public void setParameter(String sName, Object oValue) {
		
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("entityId") || sName.equals("relatedId")) {
			if(oValue instanceof Long) {
				this.iEntityId = ((Long)oValue).intValue();
			}
			else {
				this.iEntityId = (Integer) oValue;
			}
		} else if (sName.equals("entity")) {
			this.iEntityId = MetaDataCache.getInstance().getMetaData((String) oValue).getId();
		}
		else if(sName.equals("restriction")) {
			if(WITH_SYSTEMFIELDS.equals(oValue)) {
				blnWithoutSystemFields = false;
			}
			else if(WITHOUT_SYSTEMFIELDS.equals(oValue)) {
				blnWithoutSystemFields = true;
			}
			else {
				blnWithoutSystemFields = false;
			}
		}
	}	
	
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		
		List<CollectableField> result;
		if (this.iEntityId == null) {
			result = Collections.emptyList();
		}
		else {
				result = CollectionUtils.transform(MetaDataCache.getInstance().getMetaDataById(iEntityId).getFields(),
						new Transformer<MasterDataMetaFieldVO, CollectableField>() {
					@Override
					public CollectableField transform(MasterDataMetaFieldVO mdcvo) {
						return new CollectableValueIdField(mdcvo.getId(), 
								SpringLocaleDelegate.getInstance().getResource(mdcvo.getResourceSIdForLabel(), mdcvo.getLabel()));
					}
				});
				
				  
				if(result.isEmpty())
				{
					MasterDataMetaVO mdmvo = MetaDataCache.getInstance().getMetaDataById(iEntityId);
					
					if(Modules.getInstance().isModuleEntity(mdmvo.getEntityName()))
					{
						Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(mdmvo.getEntityName());
						final Collection<AttributeCVO> collattrcvo = (iModuleId == null) ? AttributeCache.getInstance().getAttributes() :
							GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(iModuleId, Boolean.FALSE);

						result = CollectionUtils.transform(collattrcvo, new Transformer<AttributeCVO, CollectableField>() {
						@Override
						public CollectableField transform(AttributeCVO attrcvo) {
							return new CollectableValueIdField(attrcvo.getId(), 
									SpringLocaleDelegate.getInstance().getLabelFromAttributeCVO(attrcvo));
						}
					});

					Collections.sort(result);
					}
				}
				
				if(blnWithoutSystemFields) {
					Collection<CollectableField> col = new ArrayList<CollectableField>(result);
					for(CollectableField f : col) {
						Number num = (Number)f.getValueId();
						if(num.intValue() < 0){
							result.remove(f);
						}
					}
				}
				
		}
		Collections.sort(result);
		return result;
	}
}

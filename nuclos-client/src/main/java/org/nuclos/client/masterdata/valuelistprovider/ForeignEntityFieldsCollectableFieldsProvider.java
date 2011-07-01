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

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;

/**
 * Value list provider for all foreign entity fields (reference fields) of an entity
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version	01.00.00
 */
public class ForeignEntityFieldsCollectableFieldsProvider implements CollectableFieldsProvider{

	private static Logger log = Logger.getLogger(ForeignEntityFieldsCollectableFieldsProvider.class);
	private String sEntity;
	private String sSubForm;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("entity")) {
			this.sEntity = (String)oValue;
		}
		else if (sName.equals("subform")) {
			this.sSubForm = (String)oValue;
		}
		
	}
	
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		Collection<MasterDataMetaFieldVO> collmdmetafieldsvo = new ArrayList<MasterDataMetaFieldVO>();
		
		if (!StringUtils.isNullOrEmpty(sSubForm)) {
			for (MasterDataMetaFieldVO fieldVO : MetaDataCache.getInstance().getMetaData(sSubForm).getFields()) {
				if (fieldVO.getForeignEntity() != null && fieldVO.getForeignEntity().equals(sEntity))
					collmdmetafieldsvo.add(fieldVO);
			}
		}		
		
		final List<CollectableField> result = CollectionUtils.transform(collmdmetafieldsvo, new Transformer<MasterDataMetaFieldVO, CollectableField>() {
			@Override
			public CollectableField transform(MasterDataMetaFieldVO mdmetafieldvo) {
				return new CollectableValueField(mdmetafieldvo.getFieldName());
			}
		});
		Collections.sort(result);
		
		return result;
	}
}

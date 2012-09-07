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
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class MasterDataEntityFieldsCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(MasterDataEntityFieldsCollectableFieldsProvider.class);
	private HashMap<String, String> params = null;

	public MasterDataEntityFieldsCollectableFieldsProvider(){
		params = new HashMap<String, String>();
	}
	
	@Override
	public void setParameter(String sName, Object oValue) {
		params.put(sName, oValue.toString());
		log.debug("setParameter : "+sName + " = " + oValue);
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		Collection<MasterDataMetaVO> colmdmVO_menupath = new ArrayList<MasterDataMetaVO>();
		Collection<MasterDataMetaVO> colmdmVO = MetaDataCache.getInstance().getMetaData();
		
		// get and add entities with menupath
		for(MasterDataMetaVO mdmVO : colmdmVO) {
			if (mdmVO.getMenuPath() != null) {
				colmdmVO_menupath.add(mdmVO);
			}
		}

		colmdmVO_menupath.add(MetaDataCache.getInstance().getMetaData(NuclosEntity.GENERATIONSUBENTITY));
		
		final List<CollectableField> result = CollectionUtils.transform(colmdmVO_menupath, new Transformer<MasterDataMetaVO, CollectableField>() {
			@Override
			public CollectableField transform(MasterDataMetaVO mdmVO) {
				return new CollectableValueIdField(mdmVO.getEntityName(), 
						SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(mdmVO));
			}
		});

		Collections.sort(result);

		return result;
	}

}

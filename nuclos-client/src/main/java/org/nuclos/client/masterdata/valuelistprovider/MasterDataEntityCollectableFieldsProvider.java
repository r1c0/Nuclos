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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Value list provider to get all masterdata entities containing a menupath.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class MasterDataEntityCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(MasterDataEntityCollectableFieldsProvider.class);

	boolean bModule = false;

	@Override
	public void setParameter(String sName, Object oValue) {
		//modules for rule usages
		if (sName.equals("module")) {
			bModule = Boolean.valueOf(oValue.toString());
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		Set<MasterDataMetaVO> colmdmVO_menupath = new HashSet<MasterDataMetaVO>();
		Collection<MasterDataMetaVO> colmdmVO = MasterDataDelegate.getInstance().getMetaData();

		// get and add entities with menupath
		for(MasterDataMetaVO mdmVO : colmdmVO) {
			if (bModule) {
				if (Modules.getInstance().isModuleEntity(mdmVO.getEntityName())) {
					if (Modules.getInstance().getModuleByEntityName(mdmVO.getEntityName()).getField("menupath") != null) {
						colmdmVO_menupath.add(mdmVO);
					}
				}
			}
			if (mdmVO.getResourceSIdForMenuPath() != null) {
				colmdmVO_menupath.add(mdmVO);
			}
		}

		colmdmVO_menupath.add(MasterDataDelegate.getInstance().getMetaData(NuclosEntity.GENERATIONSUBENTITY));

		final List<CollectableField> result = CollectionUtils.transform(colmdmVO_menupath, new Transformer<MasterDataMetaVO, CollectableField>() {
			@Override
			public CollectableField transform(MasterDataMetaVO mdmVO) {
				String label = CommonLocaleDelegate.getLabelFromMetaDataVO(mdmVO); 
				//String label = CommonLocaleDelegate.getText(mdmVO);
				if (Modules.getInstance().isModuleEntity(mdmVO.getEntityName()))
					label += " (Modul)";
				return new LocalizedCollectableValueField(mdmVO.getEntityName(), label);
			}
		});

		Collections.sort(result);

		return result;
	}
}

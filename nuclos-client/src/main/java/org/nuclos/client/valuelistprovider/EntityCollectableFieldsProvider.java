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
package org.nuclos.client.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Value list provider to get all entities containing a menupath.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 00.01.000
 */
public class EntityCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(EntityCollectableFieldsProvider.class);

	private boolean bWithStatemodel = true;
	private boolean bWithoutStatemodel = true;
	private boolean includeEntitiesWithoutMenu = false;
	protected boolean includeSystemEntities = true;
	
	public static final String ENTITIES_WITH_STATEMODEL_ONLY = "entities with statemodel";
	public static final String ENTITIES_WITHOUT_STATEMODEL_ONLY = "entities without statemodel";

	@Override
	public void setParameter(String parameter, Object oValue) {
		if (parameter.equalsIgnoreCase("nuclosentities")) {
			includeSystemEntities = Boolean.parseBoolean((String)oValue);
		}
		if (parameter.equalsIgnoreCase("restriction")) {
			if (ENTITIES_WITH_STATEMODEL_ONLY.equalsIgnoreCase((String) oValue)) {
				bWithoutStatemodel = false;
			} else if (ENTITIES_WITHOUT_STATEMODEL_ONLY.equalsIgnoreCase((String) oValue)) {
				bWithStatemodel = false;
			}
		}
		if (parameter.equalsIgnoreCase("menupath")) {
			if ("optional".equals(oValue)) {
				includeEntitiesWithoutMenu = true;
			}
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		Collection<EntityMetaDataVO> entites = new ArrayList<EntityMetaDataVO>();
		for (EntityMetaDataVO eMeta : MetaDataClientProvider.getInstance().getAllEntities()) {
			if (!includeSystemEntities && eMeta.getId() < 0){
				continue;
			}
			if (includeEntitiesWithoutMenu || (eMeta.getLocaleResourceIdForMenuPath() != null)) {
				if (bWithStatemodel && eMeta.isStateModel()) {
					entites.add(eMeta);
				} else { 
					if (bWithoutStatemodel && !eMeta.isStateModel()){
						entites.add(eMeta);
					}
				}
			}
		}
		
		final List<CollectableField> result = CollectionUtils.transform(entites, new Transformer<EntityMetaDataVO, CollectableField>() {
			@Override
			public CollectableField transform(EntityMetaDataVO eMeta) {
				String label = SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(eMeta); 
				return makeCollectableField(eMeta, label);
			}
		});

		Collections.sort(result);

		return result;
	}
	
	protected CollectableField makeCollectableField(EntityMetaDataVO eMeta, String label) {
		return new LocalizedCollectableValueField(eMeta.getEntity(), label);
	}
}

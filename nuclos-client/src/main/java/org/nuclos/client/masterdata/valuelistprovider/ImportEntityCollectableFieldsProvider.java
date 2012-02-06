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

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Value list provider to get importable masterdata.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 00.01.000
 */
public class ImportEntityCollectableFieldsProvider implements CollectableFieldsProvider {

	@Override
	public void setParameter(String sName, Object oValue) {
		//ignore
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		Collection<EntityMetaDataVO> colemdvo = new ArrayList<EntityMetaDataVO>();
		for (EntityMetaDataVO eMeta : MetaDataClientProvider.getInstance().getAllEntities()) {
			if (eMeta.getId() < 0) {
				continue;
			}
			colemdvo.add(eMeta);
		}

		final List<CollectableField> result = CollectionUtils.transform(colemdvo, new Transformer<EntityMetaDataVO, CollectableField>() {
			@Override
			public CollectableField transform(EntityMetaDataVO emdVO) {
				return new CollectableValueIdField(IdUtils.unsafeToId(emdVO.getId()), emdVO.getEntity());
			}
		});

		Collections.sort(result);

		return result;
	}

}

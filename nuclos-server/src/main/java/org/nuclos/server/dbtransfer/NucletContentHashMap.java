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
package org.nuclos.server.dbtransfer;

import java.util.Collection;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.dal.vo.EntityObjectVO;

public class NucletContentHashMap extends MultiListHashMap<NuclosEntity, EntityObjectVO> implements NucletContentMap {
	
	

	@Override
	public void addAll(NucletContentMap map) {
		for (NuclosEntity entity : map.keySet()) {
			this.addAllValues(entity, map.getValues(entity));
		}
	}

	@Override
	public void add(EntityObjectVO eo) {
		NuclosEntity entity = NuclosEntity.getByName(eo.getEntity());
		if (entity != null) {
			this.addValue(entity, eo);
		}
	}

	@Override
	public void addValue(NuclosEntity key, EntityObjectVO value) {
		value.getFields().put(ORIGIN_ID_FIELD, value.getId());
		super.addValue(key, value);
	}

	@Override
	public void addAllValues(NuclosEntity key,	Collection<? extends EntityObjectVO> collvalue) {
		for (EntityObjectVO eo : collvalue)
			eo.getFields().put(ORIGIN_ID_FIELD, eo.getId());
		super.addAllValues(key, collvalue);
	}

}

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
package org.nuclos.common.entityobject;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

/**
 * An abstract {@link CollectableEntityProvider} implementation which uses a given
 * {@link MetaDataProvider} as back end.
 */
public abstract class CollectableEOEntityProvider implements CollectableEntityProvider {

	private final MetaDataProvider metaDataProvider;

	protected CollectableEOEntityProvider(MetaDataProvider metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	/**
	 * This implementation is guaranteed to return a CollectableEOEntity.
	 */
	@Override
	public CollectableEntity getCollectableEntity(String entityName) throws NoSuchElementException {
		try {
			EntityMetaDataVO entity = metaDataProvider.getEntity(entityName);
			Map<String, EntityFieldMetaDataVO> fields = new HashMap<String, EntityFieldMetaDataVO>(metaDataProvider.getAllEntityFieldsByEntity(entityName));
			return new CollectableEOEntity(entity, fields);
		} catch (Exception ex) {
			throw new NoSuchElementException(ex.getMessage());
		}
	}

	@Override
	public boolean isEntityDisplayable(String sEntityName) throws NoSuchElementException {
		throw new UnsupportedOperationException();
	}

}

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

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

/**
 * Value list provider to get all entities containing a menupath as
 * CollectableValueIdField.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 00.01.000
 */
public class EntityCollectableIdFieldsProvider extends EntityCollectableFieldsProvider {

	public EntityCollectableIdFieldsProvider() {
		// Including system entities for an ID-based field provider doesn't make sense...
		includeSystemEntities = false;
	}
	
	@Override
	protected CollectableField makeCollectableField(EntityMetaDataVO eMeta, String label) {
		return new CollectableValueIdField(eMeta.getId().intValue(), label);
	}
}

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
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
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
public class AttributeGroupFunctionCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(AttributeGroupFunctionCollectableFieldsProvider.class);

	boolean groupby;

	@Override
	public void setParameter(String parameter, Object oValue) {
		if (parameter.equalsIgnoreCase("relatedId")) {
			if(oValue instanceof Boolean) {
				groupby = (Boolean)oValue;
			}
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		List<CollectableField> lst = new ArrayList<CollectableField>();
		
		if(groupby) {
			lst.add(new CollectableValueField("group by"));
			lst.add(new CollectableValueField("summate"));
			lst.add(new CollectableValueField("minimum value"));
			lst.add(new CollectableValueField("maximum value"));
		}
		
		return lst;
	}
	
	protected CollectableField makeCollectableField(EntityMetaDataVO eMeta, String label) {
		return new LocalizedCollectableValueField(eMeta.getEntity(), label);
	}
}

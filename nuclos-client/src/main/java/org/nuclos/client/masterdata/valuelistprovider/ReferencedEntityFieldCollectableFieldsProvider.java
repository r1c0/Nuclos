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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

public class ReferencedEntityFieldCollectableFieldsProvider implements
	CollectableFieldsProvider {

	private String referencingentity = null;
	private String referencingfield = null;

	@Override
	public void setParameter(String name, Object value) {
		if (name.equals("entityId")) {
			if (value != null) {
				this.referencingentity = MetaDataClientProvider.getInstance().getEntity(((Integer)value).longValue()).getEntity();
			}
			else {
				this.referencingentity = null;
			}
		}
		if (name.equals("field")) {
			this.referencingfield = (String)value;
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result = new ArrayList<CollectableField>();

		if (!StringUtils.isNullOrEmpty(referencingentity) && !StringUtils.isNullOrEmpty(referencingfield)) {
			CollectableEntityField referencingef = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(referencingentity).getEntityField(referencingfield);
			if (referencingef.isReferencing()) {
				String referencedentity = referencingef.getReferencedEntityName();
				for (Map.Entry<String, EntityFieldMetaDataVO> field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(referencedentity).entrySet()) {
					String fieldname = field.getKey();
					if (NuclosEOField.getByField(fieldname) == null || NuclosEOField.getByField(fieldname) == NuclosEOField.PROCESS  || NuclosEOField.getByField(fieldname) == NuclosEOField.STATE) {
						result.add(new CollectableValueField(fieldname));
					}
				}

				Collections.sort(result);
			}
		}
		Collections.sort(result);
		return result;
	}
}

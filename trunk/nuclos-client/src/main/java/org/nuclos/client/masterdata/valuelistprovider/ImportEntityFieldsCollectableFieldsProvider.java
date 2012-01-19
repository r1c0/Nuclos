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
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.exception.CommonBusinessException;

public class ImportEntityFieldsCollectableFieldsProvider implements
	CollectableFieldsProvider {

	private String entity = null;
	private ImportMode mode = null;

	@Override
	public void setParameter(String name, Object value) {
		if (name.equals("entityId")) {
			if (value != null) {
				this.entity = MetaDataClientProvider.getInstance().getEntity(((Integer) value).longValue()).getEntity();
			}
			else {
				this.entity = null;
			}
		}
		else if (name.equals("mode")) {
			if (value != null) {
				this.mode = KeyEnum.Utils.findEnum(ImportMode.class, (String) value);
			}
			else {
				this.mode = null;
			}
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result = new ArrayList<CollectableField>();

		if (entity != null) {
			EntityMetaDataVO entitymeta = MetaDataClientProvider.getInstance().getEntity(entity);
			for (Map.Entry<String, EntityFieldMetaDataVO> field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).entrySet()) {
				String fieldname = field.getKey();
				if (NuclosEOField.getByField(fieldname) == null || NuclosEOField.getByField(fieldname) == NuclosEOField.PROCESS) {
					result.add(new CollectableValueField(fieldname));
				}
			}
			if (this.mode != null && this.mode.equals(ImportMode.DBIMPORT) && entitymeta.isStateModel()) {
				result.add(new CollectableValueField(NuclosEOField.STATE.getMetaData().getField()));
			}

			Collections.sort(result);
		}
		return result;
	}
}

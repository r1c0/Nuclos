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

import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class ImportStructureCollectableFieldsProvider implements CollectableFieldsProvider {

	private ImportMode mode = null;

	@Override
	public void setParameter(String name, Object value) {
		if (name.equals("mode")) {
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

		if (this.mode != null) {
			List<MasterDataVO> isList = MasterDataCache.getInstance().get(NuclosEntity.IMPORT.getEntityName());
			for (MasterDataVO is : isList) {
				if (is.getField("mode") != null && mode.equals(KeyEnum.Utils.findEnum(ImportMode.class, (String) is.getField("mode")))) {
					result.add(new CollectableValueIdField(is.getId(), is.getField("name")));
				}
			}
		}
		Collections.sort(result);
		return result;
	}
}

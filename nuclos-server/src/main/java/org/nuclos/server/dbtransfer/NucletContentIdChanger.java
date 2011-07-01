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

import java.util.ArrayList;
import java.util.Collection;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dbtransfer.content.INucletContent;

public class NucletContentIdChanger {
	
	private final NucletContentMap contentMap;
	private final Collection<NucletContentIdChange> colChanges = new ArrayList<NucletContentIdChange>();
	
	public NucletContentIdChanger(NucletContentMap contentMap) {
		super();
		this.contentMap = contentMap;
	}

	/**
	 * add id to change later
	 * @param nc
	 * @param ncObject
	 * @param targetId
	 */
	public void add(INucletContent nc, EntityObjectVO ncObject, Long targetId) {
		for(EntityFieldMetaDataVO efMeta : nc.getFieldDependencies()) {
			NuclosEntity entity = NuclosEntity.getByName(TransferUtils.getEntity(efMeta));
			if (entity != null && efMeta.getForeignEntity() != null) {
				for(EntityObjectVO eo : contentMap.getValues(entity)) {
					if (ncObject.getId().equals(eo.getFieldId(efMeta.getField()))) {
						colChanges.add(new NucletContentIdChange(eo, efMeta.getField(), targetId));
					}
				}
			}
		}
		colChanges.add(new NucletContentIdChange(ncObject, null, targetId));
	}
	
	/**
	 * change all stored ids
	 */
	public void changeIds() {
		for (NucletContentIdChange change : colChanges) {
			if (change.idField == null) {
				change.ncTarget.setId(change.targetId);
			} else {
				change.ncTarget.getFieldIds().put(change.idField, change.targetId);
			}
		}
	}

	private class NucletContentIdChange {
		final EntityObjectVO ncTarget;
		final String idField;
		final Long targetId;
		public NucletContentIdChange(EntityObjectVO ncTarget, String idField, Long targetId) {
			super();
			this.ncTarget = ncTarget;
			this.idField = idField;
			this.targetId = targetId;
		}
	}
}

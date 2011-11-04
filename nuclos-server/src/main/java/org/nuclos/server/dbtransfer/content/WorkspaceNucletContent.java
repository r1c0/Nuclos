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
package org.nuclos.server.dbtransfer.content;

import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption.Map;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;

public class WorkspaceNucletContent extends DefaultNucletContent {

	public WorkspaceNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.WORKSPACE, null, contentTypes, true);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, Map transferOptions) {
		return CollectionUtils.select(super.getNcObjects(nucletIds, transferOptions),
				new Predicate<EntityObjectVO>() {
					@Override
					public boolean evaluate(EntityObjectVO t) {
						return t.getFieldId("user") == null;
					}
				});
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		
		if (ncObject.getFieldId("user") == null) { // is assignable workspace
			// Remove user assigned worksapces.
			// Usually this makes the foreign key constraints with cascade on delete, but during nuclet import all constraints are disabled/deleted
			DataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_WORKSPACE", "INTID_T_MD_WORKSPACE", ncObject.getId()));
		}
		super.deleteNcObject(result, ncObject);
	}
	
	
}

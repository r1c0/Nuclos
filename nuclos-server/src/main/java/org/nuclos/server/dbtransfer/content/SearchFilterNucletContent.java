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

import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

public class SearchFilterNucletContent extends DefaultNucletContent {

	public SearchFilterNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.CUSTOMCOMPONENT, null, contentTypes);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions);
		for (EntityObjectVO ncObject : result) {
			storeLocaleResources(ncObject, "label", "menupath");
		}
		return result;
	}

	@Override
	public List<DalCallResult> insertOrUpdateNcObject(EntityObjectVO ncObject, boolean isNuclon) {
		restoreLocaleResources(ncObject);
		return super.insertOrUpdateNcObject(ncObject, isNuclon);
	}
}

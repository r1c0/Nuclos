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
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common.dbtransfer.TransferOption.Map;

public class NucletNucletContent extends DefaultNucletContent {

	public NucletNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.NUCLET, null, contentTypes, true);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(final Set<Long> nucletIds, Map transferOptions) {
		if (transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE)) {
			return super.getNcObjects(nucletIds, transferOptions);
		} else {
			return CollectionUtils.select(
					super.getNcObjects(nucletIds, transferOptions), 
					new Predicate<EntityObjectVO>() {
						@Override
						public boolean evaluate(EntityObjectVO t) {
							return nucletIds.contains(t.getId());
						}
					});
		}
	}
	
}

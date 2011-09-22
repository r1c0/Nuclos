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
package org.nuclos.common.dal.util;

import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;

public class DalTransformations {
	private DalTransformations() {}

	public static <S extends IDalVO> Transformer<S, Long> getId() {
		return new Transformer<S, Long>() {
			@Override
			public Long transform(S d) {
				return d.getId();
			}
		};
	}

	private static final Transformer<EntityMetaDataVO, String> GET_ENTITY
	= new Transformer<EntityMetaDataVO, String>() {
		@Override
		public String transform(EntityMetaDataVO md) {
			return md.getEntity();
		}
	};

	public static Transformer<EntityMetaDataVO, String> getEntity() {
		return GET_ENTITY;
	}

	public static Transformer<DynamicEntityVO, String> getDynamicEntityName() {
		return new Transformer<DynamicEntityVO, String>() {
			@Override
			public String transform(DynamicEntityVO i) {
				return MasterDataMetaVO.DYNAMIC_ENTITY_PREFIX + i.getName();
			}
		};
	}
}

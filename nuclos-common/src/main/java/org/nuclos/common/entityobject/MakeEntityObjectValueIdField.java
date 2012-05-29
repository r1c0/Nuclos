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
package org.nuclos.common.entityobject;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.StringUtils;

public class MakeEntityObjectValueIdField implements Transformer<EntityObjectVO, CollectableField> {

	private final boolean bFormat;
	private final String sFieldNameForValue;

	public MakeEntityObjectValueIdField() {
		this("name");
	}
	
	public MakeEntityObjectValueIdField(boolean bFormat) {
		this("name", bFormat);
	}

	public MakeEntityObjectValueIdField(String sFieldNameForValue) {
		this(sFieldNameForValue, true);
	}

	public MakeEntityObjectValueIdField(String sFieldNameForValue, boolean bFormat) {
		this.sFieldNameForValue = sFieldNameForValue;
		this.bFormat = bFormat;
	}

	@Override
	public CollectableField transform(final EntityObjectVO eo) {
		final String entity = eo.getEntity();
		if (sFieldNameForValue.contains("${")){
			String value = StringUtils.replaceParameters(sFieldNameForValue, new FormattingTransformer(bFormat) {
				@Override
				protected Object getValue(String field) {
					return eo.getFields().get(field);
				}

				@Override
				protected String getEntity() {
					return entity;
				}
			});
			return new CollectableValueIdField(IdUtils.unsafeToId(eo.getId()), value);
		}
		else {
			return new CollectableValueIdField(IdUtils.unsafeToId(eo.getId()), eo.getFields().get(this.sFieldNameForValue));
		}
	}

}

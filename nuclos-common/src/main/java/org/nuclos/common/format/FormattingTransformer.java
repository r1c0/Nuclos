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
package org.nuclos.common.format;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;

public abstract class FormattingTransformer implements Transformer<String, String> {
	private final boolean bFormat;
	
	public FormattingTransformer() {
		this(true);
	}
	public FormattingTransformer(boolean bFormat) {
		this.bFormat = bFormat;
	}

	private final MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);

	@Override
	public String transform(String i) {
		Object val = getValue(i);
		if (val == null) {
			return "";
		}
		EntityFieldMetaDataVO meta = metaprovider.getEntityField(getEntity(), i);
		try {
			if (!bFormat)
				return "" + val;
			return CollectableFieldFormat.getInstance(Class.forName(meta.getDataType())).format(meta.getFormatOutput(), val);
		} catch (ClassNotFoundException e) {
			return val != null ? val.toString() : "";
		}
	}

	protected abstract String getEntity();

	protected abstract Object getValue(String field);

}

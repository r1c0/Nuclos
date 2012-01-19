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
package org.nuclos.common.masterdata;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Transformer that makes a value id field out of a <code>MasterDataVO</code>,
 * with the mdvo's id as value id and with the mdvo's field with the given name as value.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class MakeMasterDataLocalizedValueField implements Transformer<MasterDataVO, CollectableField> {

	private final String fieldNameForValue;
	private final String fieldNameForResourceId;

	public MakeMasterDataLocalizedValueField(String fieldNameForValue) {
		this(fieldNameForValue, "localeResourceID");
	}

	public MakeMasterDataLocalizedValueField(String fieldNameForValue, String fieldNameForResourceId) {
		this.fieldNameForValue = fieldNameForValue;
		this.fieldNameForResourceId = fieldNameForResourceId;
	}

	@Override
	public CollectableField transform(MasterDataVO mdvo) {
		Object value = mdvo.getField(fieldNameForValue);
		String resourceId = mdvo.getField(fieldNameForResourceId, String.class);
		return LocalizedCollectableValueField.fromResourceId(value, resourceId);
	}

}	// class MakeMasterDataLocalizedValueField

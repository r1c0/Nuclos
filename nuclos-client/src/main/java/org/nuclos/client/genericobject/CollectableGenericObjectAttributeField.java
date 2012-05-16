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
package org.nuclos.client.genericobject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.AbstractCollectableField;

/**
 * <code>DynamicAttributeVO</code> disguised as a <code>CollectableField</code>.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectAttributeField extends AbstractCollectableField {

	private final int iFieldType;
	private final Object oValue;
	private final Integer iValueId;

	public CollectableGenericObjectAttributeField(DynamicAttributeVO attrvo, int iFieldType) {
		this.iFieldType = iFieldType;
		this.oValue = attrvo.getValue();
		this.iValueId = attrvo.getValueId();
	}

	@Override
	public int getFieldType() {
		return this.iFieldType;
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	@Override
	public Object getValueId() throws UnsupportedOperationException {
		if (!this.isIdField()) {
			throw new UnsupportedOperationException("getValueId");
		}
		return this.iValueId;
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(iFieldType).append(oValue);
		return b.toString();
	}

}	// class CollectableGenericObjectAttributeField

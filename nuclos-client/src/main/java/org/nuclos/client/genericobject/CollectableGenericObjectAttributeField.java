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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.AbstractCollectableField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;

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

	private static final Set<String> stNullCreatableFieldNames = new HashSet<String>(Arrays.asList(
		NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(),
		NuclosEOField.CHANGEDAT.getMetaData().getField(),
		NuclosEOField.CHANGEDBY.getMetaData().getField(),
		NuclosEOField.CREATEDAT.getMetaData().getField(),
		NuclosEOField.CREATEDBY.getMetaData().getField()
	));

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
	public void validate(CollectableEntityField clctef) throws CollectableFieldValidationException {
		if (!mayBeNullOnCreation(clctef.getName())) {
			CollectableUtils.validateNull(this, clctef);
		}
		CollectableUtils.validateValueClass(this, clctef);
	}

	private boolean mayBeNullOnCreation(String sName) {
		return stNullCreatableFieldNames.contains(sName);
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(iFieldType).append(oValue);
		return b.toString();
	}

}	// class CollectableGenericObjectAttributeField

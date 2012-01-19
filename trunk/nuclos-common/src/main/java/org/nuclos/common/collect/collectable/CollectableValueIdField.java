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
package org.nuclos.common.collect.collectable;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;

/**
 * Default implementation of a <code>CollectableField</code>: a field that contains a value id,
 * and a value, which is dependent on the value id.
 * <br>
 * <em>This class is immutable, that means instances may not be modified after creation.
 * Thus it's mandatory that Objects given for value and value id in the ctors of successors
 * are immutable too.</em>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectableValueIdField extends AbstractCollectableField implements Serializable {

	public static final CollectableValueIdField NULL = new CollectableValueIdField(null, null);

	protected final Object oValueId;
	protected final Object oValue;

	/**
	 * @param oValueId must be immutable.
	 * @param oValue must be immutable.
	 */
	public CollectableValueIdField(Object oValueId, Object oValue) {
		this.oValueId = oValueId;
		this.oValue = oValue;
	}

	@Override
	public int getFieldType() {
		return CollectableField.TYPE_VALUEIDFIELD;
	}

	@Override
	public Object getValueId() {
		return this.oValueId;
	}

	@Override
	public void validate(CollectableEntityField clctef) throws CollectableFieldValidationException {
		CollectableUtils.validate(this, clctef);
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	/**
	 * @return (this.getValueId() == null) && (this.getValue() == null)
	 */
	@Override
	public boolean isNull() {
		return (this.getValueId() == null) && (this.getValue() == null);
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(oValueId).append(oValue);
		return b.toString();
	}

}  // class CollectableValueIdField

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

/**
 * Default implementation of a <code>CollectableField</code>: a field that contains a value.
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

public class CollectableValueField extends AbstractCollectableField implements Serializable {

	public static final CollectableValueField NULL = new CollectableValueField(null);

	protected final Object oValue;

	/**
	 * @param oValue must be immutable.
	 * @postcondition this.getValue() == oValue
	 */
	public CollectableValueField(Object oValue) {
		this.oValue = oValue;
	}

	/**
	 * @return CollectableField.TYPE_VALUEFIELD
	 */
	@Override
	public int getFieldType() {
		return CollectableField.TYPE_VALUEFIELD;
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	@Override
	public Object getValueId() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not a reference field: getValueId failed on " + this.toDescription());
	}

	/**
	 * @return (this.getValue() == null)
	 */
	@Override
	public boolean isNull() {
		assert this.getFieldType() == CollectableField.TYPE_VALUEFIELD;

		return (this.getValue() == null);
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(oValue);
		if (oValue != null) {
			b.append(oValue.getClass().getName());
		}
		return b.toString();
	}

}  // class CollectableValueField

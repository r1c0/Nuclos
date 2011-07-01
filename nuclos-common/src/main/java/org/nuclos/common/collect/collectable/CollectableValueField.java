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
	protected final Object oValue;

	public static final CollectableValueField NULL = new CollectableValueField(null);

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
		throw new UnsupportedOperationException("getValueId");
	}

	@Override
	public void validate(CollectableEntityField clctef) throws CollectableFieldValidationException {
		CollectableUtils.validate(this, clctef);
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
		return b.toString();
	}

//	/**
//	 * the natural order of <code>CollectableValueField</code>s is determined by <code>getValue()</code>.
//	 * null is allowed for <code>getValue()</code> in order to represent the <code>NULL</code> value.
//	 * Values that are null are smaller than all non-null values.
//	 * @param o
//	 * @return
//	 */
//	public int compareTo(Object o) {
//		int result;
//
//		CollectableField that = (CollectableField) o;
//		if (this.isNull()) {
//			result = (that.isNull() ? 0 : -1);
//		}
//		else if (that.isNull()) {
//			result = 1;
//		}
//		else {
//			result = LangUtils.compare(this.getValue(), that.getValue());
//		}
//		return result;
//	}

//	public boolean equals(Object o) {
//		if(!(o instanceof CollectableValueField)) {
//			return false;
//		}
//		return this.compareTo(o) == 0;
//	}

//	public int hashCode() {
//		if(this.getValue() == null) {
//			return 0;
//		}
//		return this.getValue().hashCode();
//	}

//	public String toString() {
//		Object oValue = this.getValue();
//		if(oValue == null)
//			return "";
//
//		return CollectableFieldFormat.getInstance(oValue.getClass()).format(null, oValue);
//	}

}  // class CollectableValueField

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

import org.nuclos.common2.LangUtils;

/**
 * Abstract implementation of a <code>CollectableField</code>.
 * Note that this class has a natural ordering that is inconsistent with equals.
 * The natural ordering is defined by <code>getValue()</code>,
 * equality is defined by <ul>
 *   <it><code>getValue()</code> for value fields</it>
 *   <it><code>getValueId()</code> for id fields.</it>
 * </ul>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public abstract class AbstractCollectableField implements CollectableField {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(CollectableField that) {
		return LangUtils.compare(this.getValue(), that.getValue());
	}

	/**
	 * equality is defined by <ul>
	 *   <it><code>getValue()</code> for value fields</it>
	 *   <it><code>getValueId()</code> AND <code>getValue()</code> for value id fields.</it>
	 * </ul>
	 * @param o
	 */
	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableField)) {
			return false;
		}
		return equals((CollectableField) o, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(CollectableField that, boolean strict) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		final int iFieldType = this.getFieldType();
		if (iFieldType != that.getFieldType()) {
			return false;
		}
		switch (iFieldType) {
			case CollectableField.TYPE_VALUEFIELD:
				return LangUtils.equals(this.getValue(), that.getValue());
			case CollectableField.TYPE_VALUEIDFIELD:
				boolean eq = LangUtils.equals(this.getValueId(), that.getValueId());
				if (strict)
					eq &= LangUtils.equals(this.getValue(), that.getValue());
				return eq;
			default:
				throw new IllegalStateException("Invalid fieldtype: " + iFieldType);
		}
	}

	@Override
	public final int hashCode() {
		switch (this.getFieldType()) {
			case CollectableField.TYPE_VALUEFIELD:
				return LangUtils.hashCode(this.getValue());
			case CollectableField.TYPE_VALUEIDFIELD:
				return LangUtils.hashCode(this.getValueId()) ^ LangUtils.hashCode(this.getValue());
			default:
				throw new IllegalStateException("Invalid fieldtype: " + this.getFieldType());
		}
	}

	/**
	 * @return the value of this (as defined by <code>getValue()</code>).
	 */
	@Override
	public String toString() {
		return String.valueOf(getValue());
	}

	/**
	 * @postcondition result <--> ((this.getValue() == null) && (this.isIdField() --> this.getValueId() == null))
	 */
	@Override
	public boolean isNull() {
		final boolean result;
		switch (this.getFieldType()) {
			case TYPE_VALUEFIELD:
				result = (this.getValue() == null);
				break;
			case TYPE_VALUEIDFIELD:
				result = (this.getValueId() == null) && (this.getValue() == null);
				break;
			default:
				throw new IllegalStateException("Invalid fieldtype: " + this.getFieldType());
		}
		assert result == ((this.getValue() == null) && (!this.isIdField() || this.getValueId() == null));
		return result;
	}

	@Override
	public boolean isIdField() {
		return this.getFieldType() == TYPE_VALUEIDFIELD;
	}

}	// class AbstractCollectableField

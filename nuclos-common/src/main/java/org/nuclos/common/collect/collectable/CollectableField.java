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

import org.nuclos.common.collection.Transformer;

/**
 * Represents a field of a <code>Collectable</code>.
 * This corresponds to a single "cell" (column of a row) in a relational database table (instance).
 * <br>
 * <em>All classes implementing this interface must be immutable,
 * that means instances may not be modified after creation.
 * Thus it's mandatory that Objects given for value and value id in the ctors of successors
 * are immutable too.</em>
 * <br>
 * There are two kinds of fields, value fields and (value) id fields.
 * @invariant this.isNull() --> (this.getValue() == null)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public interface CollectableField extends Comparable<CollectableField> {
	/**
	 * This is needed for reading from the preferences.
	 */
	public static final int TYPE_UNDEFINED = CollectableEntityField.TYPE_UNDEFINED;

	/**
	 * @see CollectableValueField
	 */
	public static final int TYPE_VALUEFIELD = CollectableEntityField.TYPE_VALUEFIELD;

	/**
	 * @see CollectableValueIdField
	 */
	public static final int TYPE_VALUEIDFIELD = CollectableEntityField.TYPE_VALUEIDFIELD;

	/**
	 * This is type information, but needed here!
	 * @return the type of this field (value or id field)
	 */
	int getFieldType();

	/**
	 * @return Is this field a (value) id field?
	 * @postcondition result <--> this.getFieldType() == TYPE_VALUEIDFIELD
	 */
	boolean isIdField();

	/**
	 * @return the value of this object. May be <code>null</code>.
	 * @postcondition (result == null) <--> this.isNull()
	 */
	Object getValue();

	/**
	 * Optional method: gets the value id of this field.
	 * @precondition this.isIdField()
	 * @return this field's value id (e.g. foreign key)
	 * @throws UnsupportedOperationException if this field doesn't have an id
	 * @todo a precondition this.isIdField() would be better here!
	 */
	Object getValueId() throws UnsupportedOperationException;

	/**
	 * @return Is the value of this field <code>NULL</code>?
	 * @postcondition result <--> (this.getValue() == null) && (this.isIdField() --> this.getValueId() == null)
	 */
	boolean isNull();

	/**
	 * equality is defined by <ul>
	 *   <it><code>getValue()</code> for value fields</it>
	 *   <it><code>getValueId()</code> for id fields.</it>
	 * </ul>
	 * @param o
	 */
	// @Override
	boolean equals(Object o);

	/** 
	 * Checks equality.  Two value fields are equal if their values are equal.  Two id fields are
	 * equal if they have the same ids.  If strict is true, id fields must also have the same value.
	 * @param that
	 * @param strict
	 */
	boolean equals(CollectableField that, boolean strict);
	
	/**
	 * The natural order of <code>CollectableField</code>s is always determined by <code>getValue()</code>.
	 * <code>null</code> is allowed for <code>getValue()</code> in order to represent the <code>NULL</code> value.
	 * Values that are null are smaller than all non-null values.
	 * @param that
	 * @return {@inheritDoc}
	 */
	// @Override
	int compareTo(CollectableField that);

	/**
	 * @return the value of this (as defined by <code>getValue()</code>).
	 */
	// @Override
	String toString();
	
	/**
	 * A representation of CollectableField suited for debugging only.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	String toDescription();
	
	/**
	 * inner class GetValue: transforms a <code>CollectableField</code> into its value.
	 */
	public static class GetValue implements Transformer<CollectableField, Object> {
		@Override
		public Object transform(CollectableField clctf) {
			return clctf.getValue();
		}
	}

	/**
	 * inner class GetValueId: transforms a <code>CollectableField</code> into its value id.
	 */
	public static class GetValueId implements Transformer<CollectableField, Object> {
		@Override
		public Object transform(CollectableField clctf) {
			return clctf.getValueId();
		}
	}

}	// interface CollectableField

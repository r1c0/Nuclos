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

import org.nuclos.common.HasId;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Represents a collectable object ("to collect data" means "Daten erfassen" in German).
 * This corresponds to a single row or "record" in a relational database table (instance).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo May/Should a Collectable know its entity? Note that AbstractCollectable can't be implemented without
 * getCollectableEntity(), maybe it's time to pull that method down. Even a getEntityName() would be very nice.
 * @see org.nuclos.client.ui.collect.CollectController#readCollectable(Collectable)
 */
public interface Collectable extends HasId<Object> {

	/**
	 * @return the identifier of this <code>Collectable</code>. In database applications, this is the primary key.
	 * Note that this may be anything from an Integer id to a composite key, with one exception:
	 * It's not allowed to return <code>this</code> here as this would lead to an infinite recursion
	 * in <code>equals</code> and <code>hashCode</code>.
	 * If you don't have an explicit id, you may <code>return new Integer(LangUtils.getJavaObjectId(this))</code> here.
	 * @postcondition result != this
	 */
	Object getId();

	/**
	 * @return a String that identifies this <code>Collectable</code>.
	 * This identifier is shown to the user. Clear text is preferred to internal numbers.
	 */
	String getIdentifierLabel();

	/**
	 * The version used in the Version Number pattern (according to Marinescu: EJB Design Patterns).
	 * It is required for a Collectable to implement the version pattern to allow for optimistic
	 * concurrency. If you don't need persistency at all, it's safe to always return 0.
	 * @return the version of the last update of this <code>Collectable</code>.
	 */
	int getVersion();

	/**
	 * shortcut for <code>this.getField(sFieldName).getValue()</code>.
	 * If you only need the value (and not the id), this is the preferred way of getting it.
	 * Subclasses may provide an implementation that is more efficient than <code>this.getField(sFieldName).getValue()</code>.
	 * @param sFieldName
	 * @return the current value of the field with the given name
	 */
	Object getValue(String sFieldName);

	/**
	 * shortcut for <code>this.getField(sFieldName).getValueId()</code>.
	 * If you only need the value id (and not the value), this is the preferred way of getting it.
	 * Subclasses may provide an implementation that is more efficient than <code>this.getField(sFieldName).getValueId()</code>.
	 * @param sFieldName
	 * @return the current value id of the field with the given name
	 * @precondition this.getField(sFieldName).isIdField() -
	 * Note that this precondition may not be checked at runtime by successors because of possible side effects.
	 * @throws UnsupportedOperationException if the field with the given name doesn't have an id
	 */
	Object getValueId(String sFieldName);

	/**
	 * @param sFieldName
	 * @return the <code>CollectableField</code> with the given name
	 * @throws CommonFatalException if there is no field with the given name
	 * @postcondition result != null
	 */
	CollectableField getField(String sFieldName) throws CommonFatalException;

	/**
	 * sets the value of the given field to the given value.
	 * @param sFieldName the name of the field to update
	 * @param clctfValue the new field value. May <em>not</em> be null.
	 * @postcondition getField(sFieldName).equals(clctfValue)
	 */
	void setField(String sFieldName, CollectableField clctfValue);

	/**
	 * @return Has this <code>Collectable</code> been completely loaded? <code>false</code>, if it has been loaded partially
	 * for performance reasons, that is some fields are missing.
	 */
	boolean isComplete();

	/**
	 * <code>Collectable</code>s are equal if they have the same id.
	 * If the id of both <code>Collectable</code>s is <code>null</code>, they are equal
	 * only if they are identical.
	 * @param o
	 * @return {@inheritDoc}
	 */
	// @Override
	boolean equals(Object o);

	/**
	 * Must be compatible with equals (just as a reminder).
	 * @return {@inheritDoc}
	 */
	// @Override
	int hashCode();

	/**
	 * inner class <code>GetId</code>: transforms a <code>Collectable</code> into its id.
	 */
	public static class GetId implements Transformer<Collectable, Object> {
		@Override
		public Object transform(Collectable clct) {
			return clct.getId();
		}
	}	// inner class GetId

	/**
	 * inner class <code>GetField</code>: gets a <code>CollectableField</code> by its (field) name.
	 */
	public static class GetField implements Transformer<Collectable, CollectableField> {
		private final String sFieldName;

		public GetField(String sFieldName) {
			this.sFieldName = sFieldName;
		}

		@Override
		public CollectableField transform(Collectable clct) {
			return clct.getField(this.sFieldName);
		}
	}	// inner class GetField

	/**
	 * inner class <code>IsComplete</code>: calls isComplete() on the iterated <code>Collectable</code>.
	 */
	public static class IsComplete implements Predicate<Collectable> {
		@Override
		public boolean evaluate(Collectable clct) {
			return clct.isComplete();
		}
	}

}	// interface Collectable

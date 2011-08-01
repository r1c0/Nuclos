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
package org.nuclos.common.collect.collectable.searchcondition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;

/**
 * A comparison with another field as a <code>CollectableSearchCondition</code>.
 * Has two operands: a field (left side) and a comparand field (right side, the field to compare with).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectableComparisonWithOtherField extends AtomicCollectableSearchCondition {

	/**
	 * @invariant clctefOther != null
	 * 
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntityField clctefOther;

	/**
	 * @param clctef
	 * @param compop
	 * @param clctefOther
	 * @precondition compop.getOperandCount() == 2
	 * @precondition clctefOther != null
	 * @precondition clctefOther.getJavaClass() == clctef.getJavaClass()
	 * @postcondition this.getComparisonOperator().equals(compop)
	 * @postcondition this.getOtherField().equals(clctefOther)
	 */
	public CollectableComparisonWithOtherField(CollectableEntityField clctef, ComparisonOperator compop,
			CollectableEntityField clctefOther) {

		super(clctef, compop);

		if (compop.getOperandCount() != 2) {
			throw new IllegalArgumentException("compop: " + compop);
		}
		if(clctefOther == null) {
			throw new NullArgumentException("clctefOther");
		}
		if (clctefOther.getJavaClass() != clctef.getJavaClass()) {
			throw new IllegalArgumentException("datatypes don't match - cannot compare " +
					clctefOther.getJavaClass().getName() + " with " + clctef.getJavaClass().getName());
		}

		this.clctefOther = clctefOther;

		assert this.getComparisonOperator().equals(compop);
		assert this.getOtherField().equals(clctefOther);
	}

	/**
	 * @return the field to compare with (the right side of the comparison).
	 * @postcondition result != null
	 */
	public CollectableEntityField getOtherField() {
		return this.clctefOther;
	}

	/**
	 * @return <code>null</code>. The result is used to be displayed in text fields, and we don't want to see the entity field there.
	 */
	@Override
	public String getComparandAsString() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableComparisonWithOtherField)) {
			return false;
		}
		final CollectableComparisonWithOtherField that = (CollectableComparisonWithOtherField) o;

		return super.equals(that) && this.clctefOther.equals(that.clctefOther);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.clctefOther.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitComparisonWithOtherField(this);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// TODO: Entity name null ok?
		// CollectableEntityField generally is not serializable, but DefaultCollectableEntityField is:
		oos.writeObject(new DefaultCollectableEntityField(this.clctefOther, null));
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// CollectableEntityField is not serializable:
		this.clctefOther = (CollectableEntityField) ois.readObject();
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + getComparisonOperator() + 
			":" + getEntityField() + ":"  + clctefOther;
	}
	
}  // class CollectableComparisonWithField

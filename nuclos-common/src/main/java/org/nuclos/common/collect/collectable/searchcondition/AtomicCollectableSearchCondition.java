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

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;

/**
 * Atomic collectable search condition. This class and its subclasses are immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class AtomicCollectableSearchCondition extends AbstractCollectableSearchCondition {

	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntityField clctef;
	
	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient ComparisonOperator compop;

	AtomicCollectableSearchCondition(CollectableEntityField clctef, ComparisonOperator compop) {
		if (compop == ComparisonOperator.NONE)
			throw new IllegalArgumentException("compop");
		this.clctef = clctef;
		this.compop = compop;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_ATOMIC;
	}

	public CollectableEntityField getEntityField() {
		return clctef;
	}

	public String getFieldName() {
		return clctef.getName();
	}

	public String getFieldLabel() {
		return clctef.getLabel() != null ? clctef.getLabel() : clctef.getName();
	}

	public ComparisonOperator getComparisonOperator() {
		return compop;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		return true;
	}

	/**
	 * @return the comparand of this search condition (if any) as <code>String</code>.
	 * Especially useful to display the comparand in text fields.
	 * @precondition this.getComparisonOperator().getOperandCount() > 1
	 * @todo clarify the purpose/usage of this method!
	 */
	public abstract String getComparandAsString();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AtomicCollectableSearchCondition))
			return false;
		final AtomicCollectableSearchCondition that = (AtomicCollectableSearchCondition) o;

		return clctef.equals(that.clctef) && compop.equals(that.compop);
	}

	@Override
	public int hashCode() {
		return clctef.hashCode() ^ compop.hashCode();
	}


	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitAtomicCondition(this);
	}

	/**
	 * dispatch method for Visitor pattern.
	 * For a description of the Visitor pattern, see the "GoF" Patterns book.
	 * @param visitor
	 * @return the result of the visitor's respective method.
	 * @precondition visitor != null
	 */
	public abstract <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// TODO: Entity name null ok?
		// CollectableEntityField generally is not serializable, but DefaultCollectableEntityField is:
		oos.writeObject(new DefaultCollectableEntityField(clctef, null));
		// ComparisonOperator is not serializable:
		oos.writeInt(getComparisonOperator().getIntValue());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// CollectableEntityField is not serializable:
		clctef = (CollectableEntityField) ois.readObject();
		// ComparisonOperator is not serializable:
		compop = ComparisonOperator.getInstance(ois.readInt());
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + compop + ":" + clctef;
	}

}  // class AtomicCollectableSearchCondition

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
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common2.LangUtils;

/**
 * A referencing collectable search condition which represents an expression like this: "Does the Collectable
 * joined to the main entity by the given foreign key field match the criteria given in the subcondition?".
 * Note that <code>ReferencingCollectableSearchCondition</code> is the "inverse concept" to <code>CollectableSubCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class ReferencingCollectableSearchCondition extends AbstractCollectableSearchCondition {

	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntityField clctefReferencing;
	
	private final CollectableSearchCondition condSub;

	/**
	 * @param clctefReferencing the referencing (foreign key) field of the main entity
	 * @param condSub
	 * @precondition clctefReferencing != null
	 * @precondition clctefReferencing.isReferencing()
	 */
	public ReferencingCollectableSearchCondition(CollectableEntityField clctefReferencing, CollectableSearchCondition condSub) {
		if (clctefReferencing == null) {
			throw new NullArgumentException("clctefReferencing");
		}
		if (!clctefReferencing.isReferencing()) {
			throw new IllegalArgumentException("clctefReferencing must be referencing.");
		}
		this.clctefReferencing = clctefReferencing;
		this.condSub = condSub;
	}

	/**
	 * Returns the foreign key field of this entity.
	 */
	public String getForeignKeyFieldName() {
		return this.clctefReferencing.getName();
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	public CollectableEntityField getReferencingField() {
		return this.clctefReferencing;
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	public String getReferencedEntityName() {
		return this.getReferencingField().getReferencedEntityName();
	}

	/**
	 * @return the subcondition, if any.
	 */
	public CollectableSearchCondition getSubCondition() {
		return this.condSub;
	}

	@Override
	public int getType() {
		return TYPE_REFERENCING;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		final CollectableSearchCondition condSub = this.getSubCondition();
		return (condSub != null) && condSub.isSyntacticallyCorrect();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ReferencingCollectableSearchCondition)) {
			return false;
		}
		final ReferencingCollectableSearchCondition that = (ReferencingCollectableSearchCondition) o;

		return this.clctefReferencing.equals(that.clctefReferencing) &&
				LangUtils.equals(this.condSub, that.condSub);
	}

	@Override
	public int hashCode() {
		return this.clctefReferencing.hashCode() ^ LangUtils.hashCode(this.condSub);
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitReferencingCondition(this);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// TODO: Entity name null ok?
		// CollectableEntityField generally is not serializable, but DefaultCollectableEntityField is:
		oos.writeObject(new DefaultCollectableEntityField(this.clctefReferencing, null));
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// CollectableEntityField is not serializable:
		this.clctefReferencing = (CollectableEntityField) ois.readObject();
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + condSub + ":" + 
			clctefReferencing;
	}
	
}  // class ReferencingCollectableSearchCondition

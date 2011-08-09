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

import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

/**
 * A collectable subcondition which represents an expression like this: "Does a Collectable in the given dependant subentity
 * exist that is joined to the main entity by the given foreign key field and that matches the criteria given in the subcondition?".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class PivotJoinCondition extends AbstractCollectableSearchCondition {
	
	private final EntityMetaDataVO subEntity;
	
	private final EntityFieldMetaDataVO field;

	/**
	 * @param sSubEntityName
	 * @param sForeignKeyFieldName
	 * @param condSub
	 * @precondition sSubEntityName != null
	 * @precondition sForeignKeyFieldName != null
	 */
	public PivotJoinCondition(EntityMetaDataVO subEntity, EntityFieldMetaDataVO field) {
		if(subEntity == null || field == null) {
			throw new NullPointerException();
		}
		if (field.getPivotInfo() == null) {
			throw new IllegalArgumentException("Not a pivot field: " + field);
		}
		this.subEntity = subEntity;
		this.field = field;
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	public EntityMetaDataVO getJoinEntity() {
		return subEntity;
	}

	public EntityFieldMetaDataVO getField() {
		return field;
	}

	@Override
	public int getType() {
		return TYPE_PIVOTJOIN;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		// ???
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PivotJoinCondition)) {
			return false;
		}
		final PivotJoinCondition that = (PivotJoinCondition) o;

		return field.equals(that.getField());
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitPivotJoinCondition(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + field;
	}
	
}  // class CollectableJoinCondition

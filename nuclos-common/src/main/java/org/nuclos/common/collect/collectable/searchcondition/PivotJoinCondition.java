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
 * Join a pivot table with given value in the key column to the base entity.
 * <p>
 * The use of this condition results in a (left non-equi) join with the referenced
 * table/entity. 
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 * 
 * @see org.nuclos.server.dal.processor.jdbc.impl.EOSearchExpressionUnparser.UnparseVisitor
 * @see org.nuclos.server.dal.processor.PivotJoinEntityFieldVOMapping
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

	/**
	 * @deprecated Don't use this constant in new applications.
	 */
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

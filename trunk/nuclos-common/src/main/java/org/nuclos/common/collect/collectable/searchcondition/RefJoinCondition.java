//Copyright (C) 2011  Novabit Informationssysteme GmbH
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

/**
 * Join a field from a 'stringified' reference to the base entity.
 * <p>
 * The use of this condition results in a (left equi) join with the referenced
 * table/entity. This is part of the effort to deprecate all views in Nuclos.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 * 
 * @see org.nuclos.server.dal.processor.jdbc.impl.EOSearchExpressionUnparser.UnparseVisitor
 * @see org.nuclos.server.dal.processor.ColumnToRefFieldVOMapping
 */
public final class RefJoinCondition extends AbstractCollectableSearchCondition {
	
	private final String tableAlias;
	
	private final EntityFieldMetaDataVO field;

	/**
	 * @param sSubEntityName
	 * @param sForeignKeyFieldName
	 * @param condSub
	 * @precondition sSubEntityName != null
	 * @precondition sForeignKeyFieldName != null
	 */
	public RefJoinCondition(EntityFieldMetaDataVO field, String tableAlias) {
		if (field == null || tableAlias == null) {
			throw new NullPointerException();
		}
		if (field.getPivotInfo() != null) {
			throw new IllegalArgumentException("Pivot field not allowed: " + field);
		}
		this.tableAlias = tableAlias;
		this.field = field;
	}

	public EntityFieldMetaDataVO getField() {
		return field;
	}
	
	public String getTableAlias() {
		return tableAlias;
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
		if (!(o instanceof RefJoinCondition)) {
			return false;
		}
		final RefJoinCondition that = (RefJoinCondition) o;
		return field.equals(that.getField()) && tableAlias.equals(that.getTableAlias());
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitRefJoinCondition(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + field + ":" + getTableAlias();
	}
	
}  // class RefJoinCondition

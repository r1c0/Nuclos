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
package org.nuclos.common.database.query.statement;


/**
 * Oracle JOIN condition for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class InnerJoinCondition extends Condition implements JoinCondition{

	private final Operand left;
	private final ComparisonOperator op;
	private final Operand right;

	public InnerJoinCondition(Operand left, ComparisonOperator op, Operand right) {
		super(null);
		this.left = left;
		this.right = right;
		this.op = op;
	}

	@Override
	public String toString() {
//	   was also used for outer joins using Oracle syntax
//		return left.toString() + (op.equals(ComparisonOperator.RIGHT_OUTER_JOIN_OPERATOR) ? "(+)" : "") +
//				op.toString() + right.toString() + (op.equals(ComparisonOperator.LEFT_OUTER_JOIN_OPERATOR) ? " (+)" : "");
		return left.toString() + op.toString() + right.toString();
	}

}	// class OracleJoinCondition

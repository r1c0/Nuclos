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
 * General comparison predicate for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class ComparisonPredicate extends Predicate {

	private final ComparisonOperator operator;
	private final Operand right;

	public ComparisonPredicate(Operand left, ComparisonOperator operator, Operand right) {
		super(left);
		this.operator = operator;
		this.right = right;
	}

	public ComparisonOperator getOperator() {
		return operator;
	}

	public Operand getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(this.getOperand().toString());
		sb.append(operator.toString());
		sb.append(right.toString());
		return sb.toString();
	}

}	// class ComparisonPredicate

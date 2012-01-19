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
 * General comparison operator for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class ComparisonOperator {

	private static final byte equals = 1;
	private static final byte notEquals = 2;
	private static final byte lessThan = 3;
	private static final byte greaterThan = 4;
	private static final byte lessThanOrEquals = 5;
	private static final byte greaterThanOrEquals = 6;

	private static final byte leftOuterJoin = 7;
	private static final byte rightOuterJoin = 8;

	public static final ComparisonOperator EQUALS_OPERATOR = new ComparisonOperator(equals);
	public static final ComparisonOperator NOT_EQUALS_OPERATOR = new ComparisonOperator(notEquals);
	public static final ComparisonOperator LESS_THAN_OPERATOR = new ComparisonOperator(lessThan);
	public static final ComparisonOperator GREATER_THAN_OPERATOR = new ComparisonOperator(greaterThan);
	public static final ComparisonOperator LESS_THAN_OR_EQUALS_OPERATOR = new ComparisonOperator(lessThanOrEquals);
	public static final ComparisonOperator GREATER_THAN_OR_EQUALS_OPERATOR = new ComparisonOperator(greaterThanOrEquals);

	public static final ComparisonOperator LEFT_OUTER_JOIN_OPERATOR = new ComparisonOperator(leftOuterJoin);
	public static final ComparisonOperator RIGHT_OUTER_JOIN_OPERATOR = new ComparisonOperator(rightOuterJoin);

	private byte type;

	private ComparisonOperator(byte type) {
		this.type = type;
	}

	@Override
	public String toString() {
		switch (type) {
			case equals:
				return " = ";
			case notEquals:
				return " <> ";
			case lessThan:
				return " < ";
			case greaterThan:
				return " > ";
			case lessThanOrEquals:
				return " =< ";
			case greaterThanOrEquals:
				return " >= ";
			case leftOuterJoin:
				return " = ";
			case rightOuterJoin:
				return " = ";
			default:
				return null;
		}
	}

}	// class ComparisonOperator

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


/**
 * A comparison operator for an atomic collectable search condition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public enum ComparisonOperator {

	/** Note that the operand count for NONE is 1 (not 0). */
	NONE(0, 1, null),
	EQUAL(1, 2, "="),
	LESS(2, 2, "<"),
	GREATER(3, 2, ">"),
	LESS_OR_EQUAL(4, 2, "<="),
	GREATER_OR_EQUAL(5, 2, ">="),
	NOT_EQUAL(6, 2, "<>"),
	LIKE(7, 2, "LIKE"),
	NOT_LIKE(10, 2, "NOT LIKE"),
	IS_NULL(8, 1, "IS NULL"),
	IS_NOT_NULL(9, 1, "IS NOT NULL");

	/**
	 * this value is not valid for any instance of this type. It is just used to specify an (illegal)
	 * default value for reading from the preferences.
	 */
	public static final int INT_UNDEFINED = -1;

	private final int iValue;
	private final int iOperandCount;
	private final String sSql;

	private ComparisonOperator(int iValue, int iOperandCount, String sSql) {
		this.iValue = iValue;
		this.iOperandCount = iOperandCount;
		this.sSql = sSql;
	}

	public static ComparisonOperator[] getComparisonOperators() {
		return values();
	}

	/**
	 * should be used for persistence only.
	 * @return the internal int value of this operator.
	 * @deprecated Use symbolic names for persistence, at least
	 */
	@Deprecated
	public int getIntValue() {
		return this.iValue;
	}

	/**
	 * should be used for persistence only.
	 * @param iIntValue
	 * @return the comparison operator for the given int value.
	 * @deprecated Use symbolic names for persistence, at least
	 */
	@Deprecated
	public static ComparisonOperator getInstance(int iIntValue) {
		switch(iIntValue) {
			case 8: return IS_NULL;
			case 9: return IS_NOT_NULL;
			case 10: return NOT_LIKE;
			default: return values()[iIntValue];
		}
	}

	/**
	 * @param sName
	 * @return the operator name, as in ComparisonOperator.name().
	 */
	public static ComparisonOperator getInstance(String sName) {
		return valueOf(sName);
	}

	/**
	 * @return the number of operands for this operator
	 * @postcondition result >= 1 && result <= 2
	 */
	public int getOperandCount() {
		return this.iOperandCount;
	}

	public String getResourceIdForLabel() {
		return "comparisonOperator." + this.name() + ".label";
	}

	public String getResourceIdForDescription() {
		return "comparisonOperator." + this.name() + ".description";
	}

	/**
	 * @return the SQL representation of this operator, if any.
	 * @postcondition (result == null) <--> (this == NONE)
	 */
	public String getSqlRepresentation() {
		return this.sSql;
	}

	/**
	 * @param compop
	 * @return the complement ("negation") of the given operator. Note that for NONE, there is no complement.
	 * @precondition compop != null
	 * @precondition compop != ComparisonOperator.NONE
	 */
	public static ComparisonOperator complement(ComparisonOperator compop) {
		switch(compop) {
			case EQUAL:
				return NOT_EQUAL;
			case LESS:
				return GREATER_OR_EQUAL;
			case GREATER:
				return LESS_OR_EQUAL;
			case LESS_OR_EQUAL:
				return GREATER;
			case GREATER_OR_EQUAL:
				return LESS;
			case NOT_EQUAL:
				return EQUAL;
			case LIKE:
				return NOT_LIKE;
			case NOT_LIKE:
				return LIKE;
			case IS_NULL:
				return IS_NOT_NULL;
			case IS_NOT_NULL:
				return IS_NULL;
			case NONE:
			default:
				throw new IllegalArgumentException("There is no complement for " + compop.name());
		}
	}

}	// enum ComparisonOperator

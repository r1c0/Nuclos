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
 * A logical operator for a composite collectable search condition. This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public enum LogicalOperator {

	AND(0, 1, Integer.MAX_VALUE, true, true),
	OR(1, 1, Integer.MAX_VALUE, true, true),
	NOT(2, 1, 1, false, false);

	private final int iValue;
	private final int iMinOperandCount;
	private final int iMaxOperandCount;
	private final boolean bAssociative;
	private final boolean bCommutative;

	private LogicalOperator(int iValue, int iMinOperandCount, int iMaxOperandCount, boolean bCommutative,
			boolean bAssociative) {
		this.iValue = iValue;
		this.iMinOperandCount = iMinOperandCount;
		this.iMaxOperandCount = iMaxOperandCount;
		this.bCommutative = bCommutative;
		this.bAssociative = bAssociative;
	}

	/**
	 * should be used for persistence only.
	 * @param iIntValue
	 * @return the logical operator for the given int value.
	 */
	public static LogicalOperator getInstance(int iIntValue) {
		return values()[iIntValue];
	}

	/**
	 * should be used for persistence only.
	 * @return the internal int value of this operator.
	 * @todo make package local
	 */
	public int getIntValue() {
		return this.iValue;
	}

	public String getResourceIdForLabel() {
		return "logicalOperator." + this.name() + ".label";
	}

	public String getResourceIdForDescription() {
		return "logicalOperator." + this.name() + ".description";
	}

	/**
	 * @return the minimum number of operands for this operator
	 */
	public int getMinOperandCount() {
		return this.iMinOperandCount;
	}

	/**
	 * @return the minimum number of operands for this operator
	 */
	public int getMaxOperandCount() {
		return this.iMaxOperandCount;
	}

	public boolean isAssociative() {
		return this.bAssociative;
	}

	public boolean isCommutative() {
		return this.bCommutative;
	}

	/**
	 * @param op
	 * @return <code>AND</code> for <code>OR</code> and vice versa. Undefined for <code>NOT</code>.
	 * @precondition op == OR || op == AND
	 * @postcondition (op == OR) <--> (result == AND)
	 * @postcondition (op == AND) <--> (result == OR)
	 */
	public static LogicalOperator getComplementalLogicalOperator(LogicalOperator op) {
		final LogicalOperator result;
		switch (op) {
			case AND:
				result = OR;
				break;
			case OR:
				result = AND;
				break;
			default:
				throw new IllegalArgumentException("op");
		}

		assert (op == OR) == (result == AND);
		assert (op == AND) == (result == OR);

		return result;
	}

}	// enum LogicalOperator

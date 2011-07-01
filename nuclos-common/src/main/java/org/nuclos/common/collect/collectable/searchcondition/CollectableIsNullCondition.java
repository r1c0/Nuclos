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

import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * An "IS [NOT] NULL" condition as a <code>CollectableSearchCondition</code>.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectableIsNullCondition extends AtomicCollectableSearchCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param clctef
	 * @postcondition this.getComparisonOperator() == ComparisonOperator.IS_NULL
	 */
	public CollectableIsNullCondition(CollectableEntityField clctef) {
		this(clctef, ComparisonOperator.IS_NULL);

		assert this.getComparisonOperator() == ComparisonOperator.IS_NULL;
	}

	/**
	 * @param clctef
	 * @precondition isValidOperator(compop)
	 * @postcondition this.getComparisonOperator() == compop
	 */
	public CollectableIsNullCondition(CollectableEntityField clctef, ComparisonOperator compop) {
		super(clctef, compop);
		if (!isValidOperator(compop)) {
			throw new IllegalArgumentException("Illegal operator: " + compop);
		}

		assert this.getComparisonOperator() == compop;
	}

	/**
	 * @param compop
	 * @return compop == ComparisonOperator.IS_NULL || compop == ComparisonOperator.IS_NOT_NULL
	 */
	public static boolean isValidOperator(ComparisonOperator compop) {
		return compop == ComparisonOperator.IS_NULL || compop == ComparisonOperator.IS_NOT_NULL;
	}

	/**
	 * @return this.getComparisonOperator() == ComparisonOperator.IS_NULL
	 */
	public boolean isPositive() {
		return this.getComparisonOperator() == ComparisonOperator.IS_NULL;
	}

	@Override
	public String getComparandAsString() {
		return null;
	}

	@Override
	public <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitIsNullCondition(this);
	}

}  // class CollectableIsNullCondition

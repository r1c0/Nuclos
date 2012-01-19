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

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common2.LangUtils;

/**
 * A "[NOT] LIKE" condition as a <code>CollectableSearchCondition</code>.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo try to get rid of one or two of the constructors
 */
public final class CollectableLikeCondition extends AtomicCollectableSearchCondition {

	private final String sLikeComparand;

	/**
	 * @param clctef
	 * @param sLikeComparand
	 * @precondition sLikeComparand != null
	 * @postcondition this.getComparisonOperator() == ComparisonOperator.LIKE
	 */
	public CollectableLikeCondition(CollectableEntityField clctef, String sLikeComparand) {
		this(clctef, ComparisonOperator.LIKE, sLikeComparand);

		assert this.getComparisonOperator() == ComparisonOperator.LIKE;
	}

	/**
	 * @param clctef
	 * @param sLikeComparand
	 * @precondition isValidOperator(compop)
	 * @precondition sLikeComparand != null
	 * @postcondition this.getComparisonOperator() == compop
	 */
	public CollectableLikeCondition(CollectableEntityField clctef, ComparisonOperator compop, String sLikeComparand) {
		super(clctef, compop);
		if (!isValidOperator(compop)) {
			throw new IllegalArgumentException("Illegal operator: " + compop);
		}
		if (sLikeComparand == null) {
			throw new NullArgumentException("sLikeComparand");
		}
		this.sLikeComparand = sLikeComparand;

		assert this.getComparisonOperator() == compop;
	}

	/**
	 * @param compop
	 * @return compop == ComparisonOperator.LIKE || compop == ComparisonOperator.NOT_LIKE
	 */
	public static boolean isValidOperator(ComparisonOperator compop) {
		return compop == ComparisonOperator.LIKE || compop == ComparisonOperator.NOT_LIKE;
	}

	/**
	 * @return this.getComparisonOperator() == ComparisonOperator.LIKE
	 */
	public boolean isPositive() {
		return this.getComparisonOperator() == ComparisonOperator.LIKE;
	}

	public String getLikeComparand() {
		return this.sLikeComparand;
	}

	@Override
	public String getComparandAsString() {
		return this.getLikeComparand();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableLikeCondition)) {
			return false;
		}
		final CollectableLikeCondition that = (CollectableLikeCondition) o;

		return super.equals(that) && this.sLikeComparand.equals(that.sLikeComparand);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ LangUtils.hashCode(this.sLikeComparand);
	}

	@Override
	public <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitLikeCondition(this);
	}

	/**
	 * @return an SQL compatible form of the like comparand.
	 */
	public String getSqlCompatibleLikeComparand() {
		return makeWildcardsSqlCompatible(this.getLikeComparand());
	}

	/**
	 * replaces <code>'*'</code> by <code>'%'</code> and <code>'?'</code> by <code>'_'</code>.
	 * @param sLikeComparand
	 */
	public static String makeWildcardsSqlCompatible(String sLikeComparand) {
		return sLikeComparand.replace('*', '%').replace('?', '_');
	}

	/**
	 * @param sLikeComparand
	 * @return Does the given text contain any of the valid wildcards?
	 */
	public static boolean containsWildcard(String sLikeComparand) {
		return StringUtils.indexOfAny(sLikeComparand, new char[] {'%', '_', '*', '?'}) >= 0;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + getComparisonOperator() + 
			":" + getEntityField() + ":" + sLikeComparand;
	}
	
}	// class CollectableLikeCondition

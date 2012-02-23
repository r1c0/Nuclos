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
package org.nuclos.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.functional.BinaryFunction;
import org.nuclos.common2.functional.FunctionalUtils;

/**
 * "UsageCriteria" consisting of module and process. This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class UsageCriteria implements Serializable, Comparable<UsageCriteria> {

	private static final long serialVersionUID = -7357563362566436100L;

	private static final Logger log = Logger.getLogger(UsageCriteria.class);

	private final Integer iModuleId;
	private final Integer iProcessId;
	private final Integer iStatusId;

	public UsageCriteria(Integer iModuleId, Integer iProcessId, Integer iStatusId) {
		this.iModuleId = iModuleId;
		this.iProcessId = iProcessId;
		this.iStatusId = iStatusId;
	}

	public Integer getModuleId() {
		return iModuleId;
	}

	public Integer getProcessId() {
		return iProcessId;
	}

	public Integer getStatusId() {
		return iStatusId;
	}

	@Override
	public boolean equals(Object o) {
		final boolean result;
		if (this == o) {
			result = true;
		}
		else if (!(o instanceof UsageCriteria)) {
			result = false;
		}
		else {
			final UsageCriteria that = (UsageCriteria) o;
			result = LangUtils.equals(this.getModuleId(), that.getModuleId())
					&& LangUtils.equals(this.getProcessId(), that.getProcessId())
							&& LangUtils.equals(this.getStatusId(), that.getStatusId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(getModuleId())
				^ LangUtils.hashCode(getProcessId())
				^ LangUtils.hashCode(getStatusId());
	}

	@Override
	public String toString() {
		return "(ModuleId: " + getModuleId() + ", ProcessId: " + getProcessId() + ", StatusId: " + getStatusId() + ")";
	}

	/**
	 * @param paramprovider
	 * @return the names for the attributes contained in a quintuple. Note that there is no attribute for "module",
	 * so the result contains only four elements.
	 * @postcondition result.size() == 4
	 */
	public static Collection<String> getContainedAttributeNames() {
		return Arrays.asList(
				// module is not an attribute - it cannot be switched dynamically.
			NuclosEOField.PROCESS.getMetaData().getField(),
			NuclosEOField.STATE.getMetaData().getField()
		);
	}

	/**
	 * imposes a partial order on UsageCriteria. Note that not all quintuples are comparable.
	 * For a pair of non-comparable quintuples, this method returns false.
	 * @param that
	 * @return Is <code>this <= that</code>?
	 * @postcondition !this.isComparableTo(that) --> !result
	 * @see #isComparableTo(UsageCriteria)
	 * @see #compareTo(Object)
	 */
	public boolean isLessOrEqual(UsageCriteria that) {
		final boolean result;

		if (this == that) {
			result = true;
		}
		else if (!this.isComparableTo(that)) {
			result = false;
		}
		else {
			result = (this.asBinary() <= that.asBinary());
		}
		assert this.isComparableTo(that) || !result;
		return result;
	}

	/**
	 * @param that
	 * @return Is this comparable to that?
	 * @precondition that != null
	 */
	public boolean isComparableTo(UsageCriteria that) {
		if (that == null) {
			throw new NullArgumentException("that");
		}
		return isComparable(this.getModuleId(), that.getModuleId())
				&& isComparable(this.getProcessId(), that.getProcessId())
				&& isComparable(this.getStatusId(), that.getStatusId());
	}

	/**
	 * tries to compare this UsageCriteria to another. Note that not all quintuples are comparable.
	 * @param that
	 * @return
	 * @throws NuclosFatalException if <code>this</code> is not comparable to <code>o</code>.
	 */
	@Override
	public int compareTo(UsageCriteria that) {
		final int result;
		if (this.equals(that)) {
			result = 0;
		}
		else {
			if (!this.isComparableTo(that)) {
				throw new NuclosFatalException("The given usage criteria " + this + " and " + that + " are not comparable.");
			}
			result = this.isLessOrEqual(that) ? -1 : 1;
		}
		return result;
	}

	private int asBinary() {
		return (binary(this.getModuleId()) << 2) | (binary(this.getProcessId()) << 1) | (binary(this.getStatusId()));
	}

	private static int binary(Integer i) {
		return i == null ? 0 : 1;
	}

	private static boolean isComparable(Integer i1, Integer i2) {
		return i1 == null || i2 == null || i1.equals(i2);
	}

	/**
	 * Note that not all quintuples are comparable.
	 * @param q1
	 * @param q2
	 * @return the minimum of q1 and q2
	 * @throws NuclosFatalException if <code>q1</code> is not comparable to <code>q2</code>.
	 */
	public static UsageCriteria min(UsageCriteria q1, UsageCriteria q2) {
		return (q1.compareTo(q2) <= 0) ? q1 : q2;
	}

	/**
	 * Note that not all quintuples are comparable.
	 * @param q1
	 * @param q2
	 * @return the maximum of q1 and q2
	 * @throws NuclosFatalException if <code>q1</code> is not comparable to <code>q2</code>.
	 */
	public static UsageCriteria max(UsageCriteria q1, UsageCriteria q2) {
		return (q1.compareTo(q2) >= 0) ? q1 : q2;
	}

	/**
	 * @param collUsageCriteria
	 * @param usagecriteria
	 * @return the maximum usagecriteria contained in collUsageCriteria that is less or equal to the given usagecriteria.
	 */
	public static UsageCriteria getBestMatchingUsageCriteria(Collection<UsageCriteria> collUsageCriteria, UsageCriteria usagecriteria) {
		UsageCriteria result = null;
		for (UsageCriteria uc : collUsageCriteria) {
			if (uc.isMatchFor(usagecriteria)) {
				log.debug("uc: " + uc + " - usagecriteria: " + usagecriteria);
				assert result == null || uc.isComparableTo(result);
				result = (result == null) ? uc : max(result, uc);
			}
		}
		return result;
	}

	/**
	 * @param that
	 * @return this.equals(getGreatestCommonUsageCriteria(this, that)
	 */
	public boolean isMatchFor(UsageCriteria that) {
		return this.equals(getGreatestCommonUsageCriteria(this, that));
	}

	/**
	 * @param collusagecriteria Collection<UsageCriteria>
	 * @return the greatest common quintuple in the given Collection
	 * @precondition CollectionUtils.isNonEmpty(collusagecriteria)
	 */
	public static UsageCriteria getGreatestCommonUsageCriteria(Collection<UsageCriteria> collusagecriteria) {
		if (!CollectionUtils.isNonEmpty(collusagecriteria)) {
			throw new IllegalArgumentException("collusagecriteria");
		}

		return FunctionalUtils.foldl1(new GreatestCommonUsageCriteria(), collusagecriteria);
	}

	/**
	 * @param q1
	 * @param q2
	 * @return the greatest common factor in terms of quintuples. This is the greatest common factor for each single element.
	 * @postcondition result.isLessOrEqual(q1) && result.isLessOrEqual(q2)
	 */
	public static UsageCriteria getGreatestCommonUsageCriteria(UsageCriteria q1, UsageCriteria q2) {
		final UsageCriteria result = new UsageCriteria(gcf(q1.getModuleId(), q2.getModuleId()),
				gcf(q1.getProcessId(), q2.getProcessId()), gcf(q1.getStatusId(), q2.getStatusId()));
		assert result.isLessOrEqual(q1) && result.isLessOrEqual(q2);
		return result;
	}

	/**
	 * @param i1
	 * @param i2
	 * @return the "greatest common factor"
	 * @postcondition (i1 == null || i2 == null) --> result == null
	 * @todo Strengthen postcondition:  (i1 == null || i2 == null || i1.intValue() != i2.intValue()) --> result == null
	 * @postcondition LangUtils.equals(i1, i2) --> LangUtils.equals(result, i1)
	 */
	private static Integer gcf(Integer i1, Integer i2) {
		final Integer result = (LangUtils.equals(i1, i2) ? i1 : null);

		assert !(i1 == null || i2 == null) || result == null;
		assert !LangUtils.equals(i1, i2) || LangUtils.equals(result, i1);

		return result;
	}

	private static class GreatestCommonUsageCriteria implements BinaryFunction<UsageCriteria, UsageCriteria, UsageCriteria, RuntimeException> {
		@Override
		public UsageCriteria execute(UsageCriteria q1, UsageCriteria q2) {
			return getGreatestCommonUsageCriteria(q1, q2);
		}
	}

}	// class UsageCriteria

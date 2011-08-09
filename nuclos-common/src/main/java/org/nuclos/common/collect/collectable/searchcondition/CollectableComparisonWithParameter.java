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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common2.Localizable;

/**
 * A comparison with a parameter.
 */
public final class CollectableComparisonWithParameter extends AtomicCollectableSearchCondition {

	public static enum ComparisonParameter implements Localizable{
		TODAY("TODAY", "nuclos.comparisonParameter.TODAY") {
			@Override
			public boolean isCompatible(CollectableEntityField clctef) {
				return (!clctef.isIdField() && java.util.Date.class.isAssignableFrom(clctef.getJavaClass()));
			}
		},
		USER("USER", "nuclos.comparisonParameter.USER") {
			@Override
			public boolean isCompatible(CollectableEntityField clctef) {
				return (clctef.isIdField() && clctef.getReferencedEntityName().equals(NuclosEntity.USER.getEntityName()))
					|| (!clctef.isIdField() && clctef.getJavaClass() == java.lang.String.class);
			}
		};
		
		private final String internalName;
		private final String resId;

		private ComparisonParameter(String internalName, String resId) {
			this.internalName = internalName;
			this.resId = resId;
		}
		
		public abstract boolean isCompatible(CollectableEntityField clctef);
		
		@Override
		public String getResourceId() {
			return resId;
		}
		
		public String getInternalName() {
			return internalName;
		}
		
		@Override
		public String toString() {
			return internalName;
		}
		
		public static List<ComparisonParameter> getCompatibleParameters(CollectableEntityField clctef) {
			if (clctef == null)
				return Collections.emptyList();
			List<ComparisonParameter> result = new ArrayList<ComparisonParameter>();
			for (ComparisonParameter p : ComparisonParameter.values()) {
				if (p.isCompatible(clctef)) {
					result.add(p);
				}
			}
			return result;
		}
		
		public static ComparisonParameter parse(String s) {
			for (ComparisonParameter p : ComparisonParameter.values()) {
				if (p.getInternalName().equals(s)) {
					return p;
				}
			}
			throw new IllegalArgumentException("Invalid comparand parameter " + s);
		}
	}
	
	private final ComparisonParameter parameter;

	public CollectableComparisonWithParameter(CollectableEntityField clctef, ComparisonOperator compop, ComparisonParameter parameter) {
		super(clctef, compop);
		if (compop.getOperandCount() != 2) {
			throw new IllegalArgumentException("compop: " + compop);
		}
		if (parameter == null) {
			throw new NullArgumentException("parameter");
		}
		if (!parameter.isCompatible(clctef)) {
			throw new IllegalArgumentException("datatypes don't match - cannot compare " +
				clctef.getJavaClass().getName() + " with " + parameter);
		}
		
		this.parameter = parameter;
	}

	public ComparisonParameter getParameter() {
		return parameter;
	}
	
	@Override
	public String getComparandAsString() {
		// null is valid here (see e.g. CollectableComparisonWithOtherField),
		// but maybe parameter.getInternalName() would be a better choice??
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableComparisonWithParameter)) {
			return false;
		}
		final CollectableComparisonWithParameter that = (CollectableComparisonWithParameter) o;

		return super.equals(that) && this.parameter.equals(that.parameter);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.parameter.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitComparisonWithParameter(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + getComparisonOperator() + 
			":" + getEntityField() + ":" + parameter;
	}
	
}  // class CollectableComparisonWithParameter

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * A comparison with a value as a <code>CollectableSearchCondition</code>.
 * A comparison has two operands: a field and a comparand (the value to compare with).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo consider renaming to CollectableComparisonWithValue
 */
public final class CollectableComparison extends AtomicCollectableSearchCondition {

	private static final Set<ComparisonOperator> stValidOperators = CollectionUtils.asSet(
			ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL,
			ComparisonOperator.LESS, ComparisonOperator.LESS_OR_EQUAL,
			ComparisonOperator.GREATER, ComparisonOperator.GREATER_OR_EQUAL
	);

	private transient CollectableField clctfComparand;

	/**
	 * @param clctef
	 * @param compop
	 * @param clctfComparand
	 * @precondition isValidOperator(compop)
	 * @precondition clctfComparand != null
	 * @precondition !clctfComparand.isNull()
	 * @todo add precondition clctfComparand.isIdField() == clctef.isIdField()
	 * @todo add precondition "datatype of field and value must match"?
	 * @postcondition this.getComparisonOperator().equals(compop)
	 * @postcondition this.getComparand().equals(clctfComparand)
	 */
	public CollectableComparison(CollectableEntityField clctef, ComparisonOperator compop,
			CollectableField clctfComparand) {
		super(clctef, compop);
		if (!isValidOperator(compop)) {
			throw new IllegalArgumentException("compop: " + compop);
		}
		if (clctfComparand == null) {
			throw new NullArgumentException("clctfComparand");
		}
		if (clctfComparand.isNull()) {
			String sMeldung = "Der Wert des Feldes " + clctef.getLabel() + " darf nicht leer sein!";
			throw new IllegalArgumentException("CollectableComparison.error");//"Der Comparand (Vergleichswert) darf f\u00fcr einen Vergleich nicht leer sein.");
		}
		this.clctfComparand = clctfComparand;

		assert this.getComparisonOperator().equals(compop);
		assert this.getComparand().equals(clctfComparand);
	}

	/**
	 * @param compop
	 * @return Is the given operator valid for a comparison?
	 * @todo this is the same for CollectableComparisonWithOtherField
	 */
	public static boolean isValidOperator(ComparisonOperator compop) {
		return compop != null && stValidOperators.contains(compop);
	}

	/**
	 * @return the comparand. Note that it is explicitly allowed that result.getValue() == null (in case of an id field
	 * where result.getValueId() != null and thus !result.isNull()).
	 * @postcondition result != null
	 * @postcondition !result.isNull()
	 */
	public CollectableField getComparand() {
		// Note that this is an assertion, not a precondition:
		assert this.getComparisonOperator().getOperandCount() > 1;

		final CollectableField result = this.clctfComparand;
		assert result != null;
		assert !result.isNull();
		return result;
	}

	@Override
	public String getComparandAsString() {
		assert this.getComparisonOperator().getOperandCount() > 1;
		final CollectableField comparand = getComparand();
		final CollectableEntityField field = getEntityField();
		String format = field.getFormatOutput();
		if (format == null) {
			if (Date.class.isAssignableFrom(field.getJavaClass())) {
				return CommonLocaleDelegate.getDateFormat().format(comparand.getValue());
			}
		}
		if (format == null) {
			return comparand.toString();
		}
		else {
			return CollectableFieldFormat.getInstance(comparand.getClass()).format(format, comparand.getValue());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableComparison)) {
			return false;
		}
		final CollectableComparison that = (CollectableComparison) o;

		return super.equals(that) && this.clctfComparand.equals(that.clctfComparand);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.clctfComparand.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(AtomicVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitComparison(this);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		// CollectableField is generally not serializable, CollectableValue[Id]Field is:
		final CollectableField clctf;
		if (this.getComparand().isIdField() && !NuclosEOField.isEOFieldWithForceValueSearch(this.getEntityField().getName())) {
			clctf = new CollectableValueIdField(this.getComparand().getValueId(), this.getComparand().getValue());
		}
		else {
			clctf = new CollectableValueField(this.getComparand().getValue());
		}
		oos.writeObject(clctf);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// CollectableField is not serializable:
		this.clctfComparand = (CollectableField) ois.readObject();
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + getComparisonOperator() + 
			":" + getEntityField() + ":"  + clctfComparand;
	}
	
}	// class CollectableComparison

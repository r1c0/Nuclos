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
package org.nuclos.common.collect.collectable;

import java.util.Comparator;

/**
 * A <code>Comparator</code> for <code>Collectable</code>s. This makes it possible to sort
 * <code>Collectable</code>s by the values of one of their fields.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableComparator<Clct extends Collectable> implements Comparator<Clct> {

	private final String sFieldName;
	private final Comparator<CollectableField> comparatorForFields;

	/**
	 * creates a <code>Comparator</code> that compares <code>Collectable</code>s
	 * by the values of the field with the given name.
	 * The fields are compared by using <code>getFieldComparator(clctef)</code>
	 * as Comparator.
	 * @param clctef
	 */
	public CollectableComparator(CollectableEntityField clctef) {
		this(clctef.getName(), getFieldComparator(clctef));
	}

	/**
	 * creates a <code>Comparator</code> that compares <code>Collectables</code>
	 * by the values of the field with the given name.
	 * The fields are compared by using <code>comparatorForFields</code>s.
	 * @param sFieldName
	 * @param comparatorForFields the comparator to use in order to compare the fields. If null,
	 * the natural ordering as defined by CollectableField.compareTo() is used.
	 */
	public CollectableComparator(String sFieldName, Comparator<CollectableField> comparatorForFields) {
		this.sFieldName = sFieldName;
		this.comparatorForFields = comparatorForFields;
	}

	/**
	 * If sorting by field content results in equality, we also compare the id.
	 * This can be especially useful for date fields, where the time component is omitted.
	 * So the order of creation is preserved.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public int compare(Clct clct1, Clct clct2) {
		final CollectableField clctf1 = clct1.getField(this.sFieldName);
		final CollectableField clctf2 = clct2.getField(this.sFieldName);

		int result;

		if (this.comparatorForFields == null) {
			result = clctf1.compareTo(clctf2);
		}
		else {
			result = this.comparatorForFields.compare(clctf1, clctf2);
		}
		
		Object oId1 = clct1.getId();
		Object oId2 = clct2.getId();
		if(result==0 && oId1 instanceof Comparable<?> && oId2 instanceof Comparable<?>) {
			Comparable<Object> comp1 = (Comparable<Object>) oId1;
			Comparable<Object> comp2 = (Comparable<Object>) oId2;
			result = comp1 == null ? 1 : comp2 == null ? -1 : comp1.compareTo(oId2);
		}
		return result;
	}

	/**
	 * @param clctef
	 * @return <code>Comparator</code> that compares <code>CollectableField</code>s that are compatible
	 * with the given entity field. <code>null</code> if the field is not a String field.
	 */
	public static Comparator<CollectableField> getFieldComparator(final CollectableEntityField clctef) {
		final Comparator<CollectableField> result;
		if (clctef.getJavaClass().equals(String.class)) {
			// use default ("modern" German, DIN 5007) collation here:
			result = new CollectableStringFieldComparator();
		}
		else {
			result = null;
		}
		return result;
	}

}  // class CollectableComparator

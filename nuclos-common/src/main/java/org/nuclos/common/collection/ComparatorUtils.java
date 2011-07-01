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
package org.nuclos.common.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility methods for <code>Comparator</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo add what is useful from Apache's ComparatorUtils.
 */
public class ComparatorUtils {

	private ComparatorUtils() {
	}

	/**
	 * @param comparator the comparator to reverse
	 * @return a comparator that reverses the order of the input comparator
	 * @precondition comparator != null
	 */
	public static <T> Comparator<T> reversedComparator(Comparator<T> comparator) {
		if (comparator == null) {
			throw new IllegalArgumentException("comparator");
		}
		return Collections.reverseOrder(comparator);
	}

	/**
	 * Returns a compound comparator. The given comparators are applied from left-to-right.
	 * The first result <> 0 is returned; or 0 if all comparators return 0.
	 */
	public static <T> Comparator<T> compoundComparator(List<Comparator<T>> comparators) {
		if (comparators.isEmpty())
			throw new IllegalArgumentException("At least one comparator required");
		return new CompoundComparator<T>(comparators);
	}

	/**
	 * Returns a compound comparator. The given comparators are applied from left-to-right.
	 * The first result <> 0 is returned; or 0 if all comparators return 0.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Comparator<T> compoundComparator(Comparator<? super T> comparator1, Comparator<? super T> comparator2) {
		return new CompoundComparator<T>(comparator1, comparator2);
	}
	
	/**
	 * @param transformer
	 * @param comparator
	 * @return a comparator that uses transformed elements (the output of the given <code>transformer</code>)
	 * as the given <code>comparator</code>'s input.
	 */
	public static <I, O> Comparator<I> transformedInputComparator(final Transformer<I, O> transformer, final Comparator<? super O> comparator) {
		return new Comparator<I>() {
			@Override
			public int compare(I i1, I i2) {
				return comparator.compare(transformer.transform(i1), transformer.transform(i2));
			}
		};
	}

	public static <T> Comparator<T> byClassComparator(Class<? extends T>...classes) {
		final List<Class<? extends T>> list = Arrays.asList(classes);
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return list.indexOf(o1.getClass()) - list.indexOf(o2.getClass());
			}
		};
	}

	public static <T> Comparator<T[]> arrayComparator(final Comparator<? super T> baseComparator) {
		return new Comparator<T[]>() {
			@Override
			public int compare(T[] array1, T[] array2) {
				int cmp = 0;
				for(int i = 0, n = Math.min(array1.length, array2.length); i < n; i++) {
					cmp = baseComparator.compare(array1[i], array2[i]);
					if (cmp != 0)
						return cmp;
				}
				return array1.length - array2.length;
			}
		};
	}

	private static final class CompoundComparator<T> implements Comparator<T>, Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Comparator<? super T>[] comparators;

		@SuppressWarnings("unchecked")
		private CompoundComparator(List<Comparator<T>> comparators) {
			this.comparators = comparators.toArray(new Comparator[comparators.size()]);
		}

		private CompoundComparator(Comparator<? super T>...comparators) {
			this.comparators = comparators;
		}
		
		@Override
		public int compare(T o1, T o2) {
			for (Comparator<? super T> comparator : comparators) {
				int cmp = comparator.compare(o1, o2);
				if (cmp != 0)
					return cmp;
			}
			return 0;
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof CompoundComparator) && comparators.equals(((CompoundComparator<?>) obj).comparators);
		}
	}
}	// class ComparatorUtils

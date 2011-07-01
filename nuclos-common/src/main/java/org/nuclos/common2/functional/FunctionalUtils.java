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
package org.nuclos.common2.functional;

import java.util.Collection;
import java.util.Iterator;

/**
 * Functional programming in Java :).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class FunctionalUtils {

	private FunctionalUtils() {
	}

	/**
	 * Documentation taken from the Haskell Standard Prelude:
	 * foldl, applied to a binary operator, a starting value (typically the left-identity of the operator),
	 * and a list, reduces the list using the binary operator, from left to right:
	 * <code>
	 *   foldl f z [x1, x2, ..., xn] == (...((z \u0091f\u0091 x1) \u0091f\u0091 x2) \u0091f\u0091...) \u0091f\u0091 xn
	 *   foldl :: (a -> b -> a) -> a -> [b] -> a
	 *   foldl f z [] = z
	 *   foldl f z (x:xs) = foldl f (f z x) xs
	 * </code>
	 */
	public static <E, Ex extends Exception> E foldl(BinaryFunction<E, E, E, Ex> f, E eInitial, Collection<E> coll) throws Ex {
		return foldl(f, eInitial, coll.iterator());
	}

	public static <E, Ex extends Exception> E foldl(BinaryFunction<E, E, E, Ex> f, E eInitial, Iterator<E> iter) throws Ex {
		E result = eInitial;
		while (iter.hasNext()) {
			result = f.execute(result, iter.next());
		}
		return result;
	}

	/**
	 * Alternative implementation for foldl. Well, this is just a design study... ;)
	 * @param f
	 * @param eInitial
	 * @param iter
	 */
	public static <E, Ex extends Exception> E foldlRecursive(BinaryFunction<E, E, E, Ex> f, E eInitial, Iterator<E> iter) throws Ex {
		return (iter.hasNext() ? foldlRecursive(f, f.execute(eInitial, iter.next()), iter) : eInitial);
	}

	/**
	 * Documentation taken from the Haskell Standard Prelude:
	 * foldl1 is a variant that has no starting value argument, and thus must be applied to non-empty lists.
	 * <code>
	 *   foldl1 :: (a -> a -> a) -> [a] -> a
	 *   foldl1 f (x:xs) = foldl f x xs
	 *   foldl1 _ [] = error "Prelude.foldl1: empty list"
	 * <code>
	 * @precondition coll != null && !coll.isEmpty()
	 */
	public static <E, Ex extends Exception> E foldl1(BinaryFunction<E, E, E, Ex> f, Collection<E> coll) throws Ex {
		if (coll.isEmpty()) {
			throw new IllegalArgumentException("coll must not be empty.");
		}
		return foldl1(f, coll.iterator());
	}

	/**
	 * @param f
	 * @param iter
	 * @return
	 * @precondition iter != null && iter.hasNext()
	 */
	private static <E, Ex extends Exception> E foldl1(BinaryFunction<E, E, E, Ex> f, Iterator<E> iter) throws Ex {
		if (!iter.hasNext()) {
			throw new IllegalArgumentException("iter must not be at the end.");
		}
		return foldl(f, iter.next(), iter);
	}

}	// class FunctionalUtils

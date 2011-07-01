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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 * Utility methods for <code>Predicate</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class PredicateUtils {

	private PredicateUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysTrue() {
		return (Predicate<T>) AlwaysTruePredicate.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysFalse() {
		return (Predicate<T>) AlwaysFalsePredicate.INSTANCE;
	}

	/**
	 * creates a Predicate out of a BinaryPredicate by binding the first argument of the binary predicate with the given value.
	 * @param predicate
	 * @param t1
	 * @return (see above)
	 */
	public static <T1, T2> Predicate<T2> bindFirst(BinaryPredicate<? super T1, T2> predicate, T1 t1) {
		return new BindFirstPredicate<T1, T2>(predicate, t1);
	}

	public static <T1, T2> Predicate<T1> bindSecond(BinaryPredicate<T1, T2> predicate, T2 t2) {
		return new BindSecondPredicate<T1, T2>(predicate, t2);
	}


	/**
	 * @param apredicate
	 * @return the conjunction of the given predicates.
	 */
	public static <T> Predicate<T> and(Predicate<? super T>...predicates) {
		return new AndPredicate<T>(predicates);
	}

	/**
	 * @param apredicate
	 * @return the disjunction of the given predicates.
	 * @precondition apredicate.length > 0
	 * @todo write a test
	 */
	public static <T> Predicate<T> or(Predicate<T>... apredicate) {
		return new OrPredicate<T>(apredicate);
	}

	/**
	 * @return the negation of the given predicate.
	 */
	public static <T> Predicate<T> not(Predicate<? super T> predicate) {
		return new NotPredicate<T>(predicate);
	}

	/**
	 * @param tValue
	 * @return a predicate that returns <code>true</code> iff the argument <code>equals tValue</code>.
	 */
	public static <T> Predicate<T> isEqual(T tValue) {
		return new EqualsPredicate<T>(tValue);
	}

	/**
	 * @return a SymmetricBinaryPredicate that evaluates to <code>true</code> iff <code>LangUtils.equals(t1, t2)</code>.
	 */
	public static <T> SymmetricBinaryPredicate<T> equals() {
		return new SymmetricBinaryPredicate<T>() {
			@Override
			public boolean evaluate(T t1, T t2) {
				return LangUtils.equals(t1, t2);
			}
		};
	}

	/**
	 * @return a predicate that returns <code>true</code> iff the argument <code>== null</code>.
	 */
	public static <T> Predicate<T> isNull() {
		return new IsNullPredicate<T>();
	}

	/**
	 * @return a predicate that returns <code>true</code> iff the argument <code>!= null</code>.
	 */
	public static <T> Predicate<T> isNotNull() {
		return not(PredicateUtils.<T>isNull());
	}

	/**
	 * @param cls
	 * @return a predicate that returns <code>true</code> iff the argument is either <code>null</code> or <code>instanceof cls</code>.
	 */
	public static <T> Predicate<T> isInstanceOf(Class<? extends T> cls) {
		return new InstanceOfPredicate<T>(cls);
	}

	/**
	 * @return a new predicate that evaluates to false if applied to more than one equal objects.
	 * Note that this predicate has a state (it's not side effect free), so be careful not to reuse it!
	 */
	public static <T> Predicate<T> isUnique() {
		return new UniquePredicate<T>();
	}

	/**
	 * Returns a new string predicate which evalutes to true if the given regex matches.
	 */
	public static Predicate<String> regex(String regex) {
		return regex(Pattern.compile(regex));
	}

	/**
	 * Returns a new string predicate which evalutes to true if the given regex matches.
	 */
	public static Predicate<String> regex(Pattern pattern) {
		return new PatternPredicate(pattern);
	}

	/**
	 * Returns a new string predicate for the given filter list.  The filter list is a comma-separated
	 * list of inclusion and exclusion patterns. If a pattern starts with the prefix {@code -}, it is
	 * treated as exclusion pattern.  All other patterns are treated as inclusion patterns (the prefix
	 * {@code +} is optional). The pattern themselves are interpreted as wildcard (see
	 * {@link StringUtils#wildcardToRegex(CharSequence)}).
	 * If no inclusion pattern is specified, an implicit "match all" inclusion pattern is assumed.
	 */
	@SuppressWarnings("unchecked")
	public static Predicate<String> wildcardFilterList(String filter) {
		Set<String> inclusionRegexs = new LinkedHashSet<String>();
		Set<String> exclusionRegexs = new LinkedHashSet<String>();
		for (String pattern : filter.split("\\s*,\\s*")) {
			boolean include = true;
			if (pattern.startsWith("+")) {
				pattern = pattern.substring(1);
			} else if (pattern.startsWith("-")) {
				pattern = pattern.substring(1);
				include = false;
			}
			(include ? inclusionRegexs : exclusionRegexs).add(StringUtils.wildcardToRegex(pattern));
		}

		Predicate<String> predicate;
		if (inclusionRegexs.isEmpty()) {
			predicate = PredicateUtils.alwaysTrue();
		} else {
			predicate = PredicateUtils.regex(StringUtils.join("|", inclusionRegexs));
		}
		if (!exclusionRegexs.isEmpty()) {
			predicate = PredicateUtils.and(predicate,
				PredicateUtils.not(PredicateUtils.regex(StringUtils.join("|", exclusionRegexs))));
		}
		return predicate;
	}

	/**
	 * @param transformer
	 * @param predicate
	 * @return a predicate that uses a transformed element (the output of the given <code>transformer</code>)
	 * as the given <code>predicate</code>'s input.
	 */
	public static <I,O> Predicate<I> transformedInputPredicate(Transformer<I, O> transformer, Predicate<O> predicate) {
		return new TransformedInputPredicate<I, O>(transformer, predicate);
	}

	/**
	 * @param transformer
	 * @param oValue
	 * @return a predicate that uses a transformed element (the output of the given <code>transformer</code>)
	 * and compares it (using <code>LangUtils.equals()</code> to the given value.
	 */
	public static <I,O> Predicate<I> transformedInputEquals(Transformer<? super I, ? extends O> transformer, O oValue) {
		return new TransformedInputPredicate<I, O>(transformer, PredicateUtils.<O>isEqual(oValue));
	}

	/**
	 * @param transformer
	 * @return a predicate that compares a transformed element (the output of the given <code>transformer</code>) to <code>null</code>.
	 */
	public static <I,O> Predicate<I> transformedInputIsNull(Transformer<I, O> transformer) {
		return new TransformedInputPredicate<I, O>(transformer, PredicateUtils.<O>isNull());
	}

	/**
	 * @param valuesCollection
	 * @return a predicate that returns <code>true</code> iff the argument is in values <code>collection</code>.
	 */
	public static <T> Predicate<T> valuesCollection(Collection<? extends T> valuesCollection) {
		return new ValuesCollectionPredicate<T>(valuesCollection);
	}

	/**
	 * inner class BindFirstPredicate. Defines a new predicate using partial application of another predicate.
	 */
	private static class BindFirstPredicate<T1,T2> implements Predicate<T2> {
		private final BinaryPredicate<? super T1, T2> predicate;
		private final T1 t1;

		BindFirstPredicate(BinaryPredicate<? super T1, T2> predicate, T1 t1) {
			this.predicate = predicate;
			this.t1 = t1;
		}

		@Override
		public boolean evaluate(T2 t2) {
			return predicate.evaluate(this.t1, t2);
		}
	}

	/**
	 * inner class BindSecondPredicate. Defines a new predicate using partial application of another predicate.
	 */
	private static class BindSecondPredicate<T1,T2> implements Predicate<T1> {
		private final BinaryPredicate<T1, T2> predicate;
		private final T2 t2;

		BindSecondPredicate(BinaryPredicate<T1, T2> predicate, T2 t2) {
			this.predicate = predicate;
			this.t2 = t2;
		}

		@Override
		public boolean evaluate(T1 t1) {
			return predicate.evaluate(t1, this.t2);
		}
	}

	/**
	 * inner class AndPredicate
	 */
	private static class AndPredicate<T> implements Predicate<T> {
		private final Predicate<? super T>[] apredicate;

		/**
		 * @param apredicate
		 * @precondition apredicate.length > 0
		 */
		AndPredicate(Predicate<? super T>... apredicate) {
			this.apredicate = apredicate;
		}

		@Override
		public boolean evaluate(T t) {
			for (Predicate<? super T> predicate : apredicate) {
				if (!predicate.evaluate(t)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * inner class OrPredicate
	 */
	private static class OrPredicate<T> implements Predicate<T> {
		private final Predicate<? super T>[] apredicate;

		/**
		 * @param apredicate
		 * @precondition apredicate.length > 0
		 */
		OrPredicate(Predicate<? super T>... apredicate) {
			this.apredicate = apredicate;
		}

		@Override
		public boolean evaluate(T t) {
			for (Predicate<? super T> predicate : apredicate) {
				if (predicate.evaluate(t)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * inner class NotPredicate
	 */
	private static class NotPredicate<T> implements Predicate<T> {
		private final Predicate<? super T> predicate;

		NotPredicate(Predicate<? super T> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean evaluate(T t) {
			return !predicate.evaluate(t);
		}
	}

	/**
	 * inner class EqualsPredicate
	 */
	private static class EqualsPredicate<T> implements Predicate<T> {
		private final Object tValue;

		EqualsPredicate(Object tValue) {
			this.tValue = tValue;
		}

		@Override
		public boolean evaluate(T t) {
			return t.equals(this.tValue);
		}
	}

	/**
	 * inner class IsNullPredicate
	 */
	private static class IsNullPredicate<T> implements Predicate<T> {
		@Override
		public boolean evaluate(T t) {
			return t == null;
		}
	}

	/**
	 * inner class UniquePredicate: evaluates to false if applied to more than one equal objects.
	 */
	private static class UniquePredicate<T> implements Predicate<T> {

		private final Set<T> stPreviouslySeenObjects = new HashSet<T>();

		@Override
		public boolean evaluate(T t) {
			return this.stPreviouslySeenObjects.add(t);
		}
	}

	/**
	 * inner class ValuesCollectionPredicate: evaluates to true if an object is in the values collection.
	 */
	private static class ValuesCollectionPredicate<T> implements Predicate<T> {
		private final Collection<? extends T> valuesCollection;

		ValuesCollectionPredicate(Collection<? extends T> valuesCollection) {
			this.valuesCollection = valuesCollection;
		}

		@Override
		public boolean evaluate(T t) {
			for(T value : valuesCollection){
				if(value.equals(t)){
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * inner class InstanceOfPredicate
	 */
	private static class InstanceOfPredicate<T> implements Predicate<T> {
		private final Class<? extends T> cls;

		InstanceOfPredicate(Class<? extends T> cls) {
			this.cls = cls;
		}

		@Override
		public boolean evaluate(T t) {
			return LangUtils.isInstanceOf(t, cls);
		}
	}

	private static enum AlwaysTruePredicate implements Predicate<Object> {
		INSTANCE;

		@Override
		public boolean evaluate(Object t) {
			return true;
		}
	}

	private static enum AlwaysFalsePredicate implements Predicate<Object> {
		INSTANCE;

		@Override
		public boolean evaluate(Object t) {
			return false;
		}
	}

	private static class PatternPredicate implements Predicate<String> {

		private final Pattern pattern;

		private PatternPredicate(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean evaluate(String t) {
			return pattern.matcher(t).matches();
		}
	}
}	// class PredicateUtils

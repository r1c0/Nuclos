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
package org.nuclos.common2;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * Utility methods for java.lang.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LangUtils {

	private LangUtils() {
	}

	/**
	 * Replacement for the missing implication operator ("-->") in Java.
	 * <br>
	 * Note that for a boolean term "bConclusion" there is a difference between calling "implies(bPremise, bConclusion)" and
	 * executing "!bPremise || bConclusion" directly: In the former case, the term "bConclusion"
	 * is always evaluated, in the latter case, "bConclusion" is evaluated only if bPremise is true, because of the non-strict
	 * semantics of the "||" operator.
	 * @param bPremise
	 * @param bConclusion
	 * @return !bPremise || bConclusion
	 */
	public static boolean implies(boolean bPremise, boolean bConclusion) {
		return !bPremise || bConclusion;
	}

	/**
	 * compares two objects. If the objects are not comparable, they are compared based on <code>toString()</code>.
	 * @param o1 may be <code>null</code>
	 * @param o2 may be <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public static int compare(Object o1, Object o2) {
		if (o1 == o2) {
			// performance shortcut, esp. for o1 == o2 == null.
			return 0;
		}
		else if (o1 instanceof Comparable<?>) {
			return compareComparables((Comparable) o1, (Comparable) o2);
		}
		else {
			return compareBasedOnToString(o1, o2);
		}
	}

	/**
	 * compares two object based on the given comparator.
	 * @param t1 may be <code>null</code>
	 * @param t2 may be <code>null</code>
	 * @param comparator
	 */
	public static <T> int compare(T t1, T t2, Comparator<T> comparator) {
		final int result;

		if (t1 == null) {
			result = (t2 == null) ? 0 : -1;
		}
		else if (t2 == null) {
			result = 1;
		}
		else {
			result = comparator.compare(t1, t2);
		}
		return result;
	}

	/**
	 * Compares two <code>Comparables</code>, that may be <code>null</code>.
	 * <code>null</code> is less than all other values.
	 * @param t1 may be <code>null</code>
	 * @param t2 may be <code>null</code>
	 * @return @see java.lang.Object#compareTo
	 */
	public static <T extends Comparable<T>> int compareComparables(T t1, T t2) {
		final int result;

		if (t1 == null) {
			result = (t2 == null) ? 0 : -1;
		}
		else if (t2 == null) {
			result = 1;
		}
		else {
			result = t1.compareTo(t2);
		}
		return result;
	}

	/**
	 * <code>false</code> is lower than <code>true</code> by definition.
	 * This is consistent with Boolean.compareTo(Boolean)
	 * @param b1
	 * @param b2
	 */
	public static int comparebooleans(boolean b1, boolean b2) {
		final int result;

		if (b1 == b2) {
			result = 0;
		}
		else {
			result = (b1 ? 1 : -1);
		}
		return result;
	}

	/**
	 * Compares two <code>Objects</code>, that may be <code>null</code>. <code>null</code> is less
	 * than all values. If both objects are not <code>null</code>, they are compared based on their string
	 * representations, according to <code>toString()</code>.
	 * @param o1 may be <code>null</code>
	 * @param o2 may be <code>null</code>
	 */
	public static int compareBasedOnToString(Object o1, Object o2) {
		final int result;

		if (o1 == null) {
			result = (o2 == null) ? 0 : -1;
		}
		else if (o2 == null) {
			result = 1;
		}
		else {
			result = o1.toString().compareTo(o2.toString());
		}
		return result;
	}

	public static <C extends Comparable<? super C>> C min(C c1, C c2) {
		// this is faster than the varargs variant
		return (c1.compareTo(c2) <= 0 ? c1 : c2);
	}

	public static <C extends Comparable<? super C>> C max(C c1, C c2) {
		// this is faster than the varargs variant
		return (c1.compareTo(c2) >= 0 ? c1 : c2);
	}

	/**
	 * @param ac one or more <code>Comparable</code>s, none of which may be <code>null</code>.
	 * @return the minimum of the given <code>Comparable</code>s.
	 * @precondition ac != null
	 */
	public static <C extends Comparable<? super C>> C min(C... ac) {
		return Collections.min(Arrays.asList(ac));
	}

	/**
	 * @param ac one or more <code>Comparable</code>s, none of which may be <code>null</code>.
	 * @return the maximum of the given <code>Comparable</code>s.
	 * @precondition ac != null
	 */
	public static <C extends Comparable<? super C>> C max(C... ac) {
		return Collections.max(Arrays.asList(ac));
	}

	/**
	 * checks if o1 equals o2, allowing <code>null</code> for both arguments.
	 * Note that this method doesn't work for arrays currently.
	 * @param o1 may be <code>null</code>
	 * @param o2 may be <code>null</code>
	 * @return
	 * @postcondition (o1 == null && o2 == null) --> result
	 * @postcondition (o1 != null) --> (result <--> o1.equals(o2))
	 */
	public static boolean equals(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	/**
	 * Note that this method doesn't work for arrays currently.
	 * @param o may be <code>null</code>
	 * @return hash code for o, as in <code>Object.hashCode()</code>
	 */
	public static int hashCode(Object o) {
		return (o == null) ? 0 : o.hashCode();
	}
	
	/**
	 * Generates a hash code for a sequence of input values.
	 */
	public static int hash(Object... values) {
		return Arrays.hashCode(values);
	}

	/**
	 * @param o may be <code>null</code>
	 * @return <code>o.toString()</code> or <code>null</code>
	 */
	public static String toString(Object o) {
		return (o == null) ? null : o.toString();
	}

	/**
	 * @param o may be <code>null</code>
	 * @return <code>o.getClass()</code> or <code>null</code>
	 */
	public static Class<?> getClass(Object o) {
		return (o == null) ? null : o.getClass();
	}

	/**
	 * gets an int value that uniquely identifies the given object inside this JVM (or isolate).
	 * @see System#identityHashCode
	 * @param o
	 * @return an int value containing the object id of <code>o</code>.
	 */
	public static int getJavaObjectId(Object o) {
		return System.identityHashCode(o);
	}

	/**
	 * gets a String that uniquely identifies the given object inside this JVM (or isolate).
	 * @see System#identityHashCode
	 * @param o
	 * @return a String containing the object id of <code>o</code>.
	 */
	public static String getJavaObjectIdAsString(Object o) {
		return Integer.toString(getJavaObjectId(o));
	}

	/**
	 * @return the default <code>Collator</code> for this platform, that is the default <code>Collator</code>
	 * for the default <code>Locale</code>. For international applications, this is the collation of choice.
	 * Note that this is locale dependent.
	 */
	public static Collator getDefaultCollator() {
		return Collator.getInstance();
	}

	/**
	 * @return the default <code>Collator</code> for <code>Locale.GERMANY</code>.
	 * This "modern" collation is compatible with DIN 5007 (which is very similar to the collation proposed by the "Duden").
	 * This can be used wherever <code>String</code>s must be sorted according to German rules.
	 * For name directories (eg. address books or phonebooks), the traditional german collation can be used as an alternative.
	 * @see #getTraditionalGermanCollator()
	 */
	public static Collator getModernGermanCollator() {
		return Collator.getInstance(Locale.GERMANY);
	}

	/**
	 * The current JDK (1.4) does not provide support for traditional German ("phonebook") collation.
	 * This method does. Note that the "modern" collation provided by <code>Collator.getInstance(Locale.GERMANY)</code>
	 * is compatible with DIN 5007 (which is very similar to the collation proposed by the "Duden").
	 * The "traditional" or "phonebook" collation provided by this method may according to DIN 5007
	 * only be used if <em>names</em> are to be collated, as in phonebooks or name directories.
	 * <p><em>For all other cases, the modern German collation should be used! So think twice before you use this traditional
	 * collation.</em>
	 * @see <a href="http://faql.de/eszett.html">Sortierung von Umlauten</a>
	 * @see #getModernGermanCollator()
	 * @return a traditional German collator
	 */
	public static Collator getTraditionalGermanCollator() {
		return TraditionalGermanCollator.getInstance();
	}

	/**
	 * converts an <code>Integer</code> to an <code>int</code>, mapping <code>null</code> to <code>0</code> and
	 * mapping <code>Integer(0)</code> to <code>0</code> also.
	 * Note that this method/function is not bijective, that is, there is no inverse function. Thus, in most cases,
	 * you may want to use the strict version <code>nullToZeroStrict</code> instead.
	 * @param i
	 * @return the <code>intValue()</code> of <code>i</code>, if <code>i != null</code>. <code>0</code> otherwise.
	 * @postcondition (i == null) --> (result == 0)
	 * @postcondition (i != null) --> (result == i)
	 */
	public static int zeroIfNull(Integer i) {
		final int result = (i == null) ? 0 : i;

		// Note that "implies" doesn't work here:
		assert !(i == null) || (result == 0);
		assert !(i != null) || (result == i);

		return result;
	}

	/**
	 * converts an <code>Integer</code> to an <code>int</code>, mapping <code>null</code> to <code>0</code>.
	 * Integer(0) is not allowed for input. This strict version of "nullToZeroStrict" is the inverse of <code>zeroToNull</code>,
	 * always ensuring that <code>LangUtil.equals(zeroToNull(nullToZeroStrict(i)), i)</code>.
	 * @param i
	 * @return the <code>intValue()</code> of <code>i</code>, if <code>i != null</code>. <code>0</code> otherwise.
	 * @throws IllegalArgumentException if <code>i</code> is <code>Integer(0)</code>.
	 * @postcondition (i == null) --> (result == 0)
	 * @postcondition (i != null) && (i != 0) --> (result == i)
	 */
	public static int zeroIfNullStrict(Integer i) {
		final int result;
		if (i == null) {
			result = 0;
		}
		else {
			result = i;
			if (result == 0) {
				throw new IllegalArgumentException("langutils.integer.not.allowed.exception");//"Integer(0) ist nicht erlaubt.");
			}
		}

		// Note that "implies" doesn't work here:
		assert !(i == null) || (result == 0);
		assert !((i != null) && (i != 0)) || (result == i);

		return result;
	}

	/**
	 * converts an <code>int</code> into an <code>Integer</code>, mapping <code>0</code> to <code>null</code>.
	 * @param i
	 * @return an <code>Integer</code> whose <code>intValue()</code> equals <code>i</code>, if <code>i != 0</code>. <code>null</code> otherwise.
	 * @postcondition (i != 0) --> (result != null && result == i)
	 * @postcondition (i == 0) --> (result == null)
	 */
	public static Integer nullIfZero(int i) {
		final Integer result = (i == 0) ? null : i;

		// Note that "implies" doesn't work here:
		assert !(i != 0) || (result != null && result == i);
		assert !(i == 0) || (result == null);

		return result;
	}

	/**
	 * @param tValue
	 * @param tDefault
	 * @return tValue if <code>tValue != null</code>. <code>tDefault</code> otherwise.
	 * @postcondition (tValue != null) --> (result == tValue)
	 * @postcondition (tValue == null) --> (result == tDefault)
	 */
	public static <T> T defaultIfNull(T tValue, T tDefault) {
		return (tValue != null) ? tValue : tDefault;
	}

	public static <T> T firstNonNull(T...values) {
		for (T t : values) {
			if (t != null)
				return t;
		}
		return null;
	}

	/**
	 * Note that it is counterintuitive but true for the Java language, that <code>null</code> <code>instanceof</code> nothing,
	 * while on the other hand, <code>null</code> can be cast into anything.
	 * Hint: Maybe it's counterintuitive but a common OO phenomenon--and it makes sense: From the OO type system's
	 * perspective the null type (the type of <code>null</code>) is (must be (*)) a subtype of all other types.
	 * ((*) That's the reason why you can assign the null value to any other (reference) type).
	 * @param t
	 * @param cls
	 * @return
	 * @postcondition (t == null) --> result
	 */
	public static <T> boolean isInstanceOf(T t, Class<? extends T> cls) {
		final boolean result = (t == null) || cls.isAssignableFrom(t.getClass());
		assert implies(t == null, result);
		return result;
	}

	/**
	 * @param cls
	 * @return the unqualified class name (without the package name) of the given class.
	 */
	public static String unqualifiedClassName(Class<?> cls) {
		/** @todo use cls.getSimpleName()*/
		final String sClassName = cls.getName();
		return sClassName.substring(sClassName.lastIndexOf('.') + 1);
	}

	/**
	 * Tests whether obj is an instance of at least one of the given classes/interfaces.
	 */
	public static boolean isInstanceOf(Object obj, Class<?>...classes) {
		for (Class<?> clazz : classes) {
			if (clazz.isInstance(obj))
				return true;
		}
		return false;
	}

	/**
	 * Creates an instance of the given class and casts it. 
	 */
	public static <T> T instantiate(String className, Class<T> superClass) {
		try {
			Class<? extends T> cl = Class.forName(className).asSubclass(superClass);
			return cl.newInstance();
		} catch(ClassNotFoundException ex) {
			throw new IllegalArgumentException(ex);
		} catch(InstantiationException ex) {
			throw new IllegalArgumentException(ex);
		} catch(IllegalAccessException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public static Integer convertId(Long id) {
		if (id == null) {
			return null;
		}
		
		return new Integer(id.intValue());
	}

	public static Long convertId(Integer id) {
		if (id == null) {
			return null;
		}
		
		return new Long(id.longValue());
	}

	public static String nullIfBlank(String s) {
		if (StringUtils.isBlank(s)) {
			return null;
		}
		else {
			return s;
		}
	}
	
}	// class LangUtils

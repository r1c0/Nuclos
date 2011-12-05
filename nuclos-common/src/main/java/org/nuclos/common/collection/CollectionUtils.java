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

import static org.nuclos.common2.LangUtils.implies;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common2.LangUtils;

/**
 * Generic algorithms for collections (which should be contained in the JDK already...)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectionUtils {

   private CollectionUtils() {
   }
   
   public static <T> List<T> newOneElementArrayList(T t) {
	   final List<T> result = new ArrayList<T>(1);
	   result.add(t);
	   return result;
   }

   public static <T> List<T> newOneElementLinkedList(T t) {
	   final List<T> result = new LinkedList<T>();
	   result.add(t);
	   return result;
   }

   /**
    * @param coll
    * @return (coll == null) || coll.isEmpty();
    */
   public static boolean isNullOrEmpty(Collection<?> coll) {
      return (coll == null) || coll.isEmpty();
   }

   /**
    * @param coll
    * @return !isNullOrEmpty(coll);
    */
   public static boolean isNonEmpty(Collection<?> coll) {
      return !isNullOrEmpty(coll);
   }

   /**
    * @param lst
    * @return an unmodifiable empty list if the given list is null.
    * @postcondition result != null
    * @postcondition lst != null --> result == lst
    * @postcondition lst == null --> result.isEmpty()
    * @todo Wouldn't this be more useful if we return a modifiable list here?
    */
   public static <E> List<E> emptyListIfNull(List<E> lst) {
      final List<E> result = (lst == null) ? Collections.<E>emptyList() : lst;

      assert result != null;
      assert implies(lst != null, result == lst);
      assert implies(lst == null, result.isEmpty());
      return result;
   }

   /**
    * @param st
    * @return an unmodifiable empty set if the given set is null.
    * @postcondition result != null
    * @postcondition st != null --> result == st
    * @postcondition st == null --> result.isEmpty()
    * @todo Wouldn't this be more useful if we return a modifiable set here?
    */
   public static <E> Set<E> emptySetIfNull(Set<E> st) {
      final Set<E> result = (st == null) ? Collections.<E>emptySet() : st;

      assert result != null;
      assert implies(st != null, result == st);
      assert implies(st == null, result.isEmpty());
      return result;
   }

   /**
    * @param coll
    * @return an unmodifiable empty collection if the given collection is null.
    * @postcondition result != null
    * @postcondition coll != null --> result == coll
    * @postcondition coll == null --> result.isEmpty()
    * @todo Wouldn't this be more useful if we return a modifiable list here?
    */
   public static <E> Collection<E> emptyIfNull(Collection<E> coll) {
      final Collection<E> result = (coll == null) ? Collections.<E>emptyList() : coll;

      assert result != null;
      assert implies(coll != null, result == coll);
      assert implies(coll == null, result.isEmpty());
      return result;
   }

   /**
    * @param coll
    * @return <code>null</code> if <code>coll</code> is null or empty, <code>coll</code> otherwise.
    * @postcondition CollectionUtils.isNullOrEmpty(coll) --> result == null
    * @postcondition CollectionUtils.isNonEmpty(coll) --> result == coll
    */
   public static <C extends Collection<?>> C nullIfEmpty(C coll) {
      final C result = CollectionUtils.isNullOrEmpty(coll) ? null : coll;

      assert implies(CollectionUtils.isNullOrEmpty(coll), result == null);
      assert implies(CollectionUtils.isNonEmpty(coll), result == coll);
      return result;
   }

   /**
    * @param mp
    * @return a new empty map if the given map is null.
    * @postcondition result != null
    * @postcondition mp != null --> result == mp
    * @postcondition mp == null --> result.isEmpty()
    */
   public static <K, V> Map<K, V> emptyMapIfNull(Map<K, V> mp) {
      return (mp != null) ? mp : new HashMap<K, V>();
   }

   /**
    * Shortcut for creating a new HashMap.
    * @return a new HashMap<K, V>
    */
   public static <K, V> Map<K, V> newHashMap() {
      return new HashMap<K, V>();
   }

   /**
    * @param mp
    * @return the values of mp or an empty set, if <code>mp == null</code>.
    * @postcondition result != null
    */
   public static <K,V> Collection<V> valuesOrEmptySet(Map<K, V> mp) {
      final Collection<V> result = (mp == null) ? Collections.<V>emptySet() : mp.values();
      assert result != null;
      return result;
   }

   /**
    * @todo check if this method is still necessary with Java 5.
    * @param ai
    * @return a List<Integer> containing the elements of the given int array.
    * @precondition ai != null
    * @postcondition result != null
    * @postcondition result.size() == ai.length
    */
   public static List<Integer> asList(int[] ai) {
      final List<Integer> result = new ArrayList<Integer>(ai.length);
      for (int i : ai) {
         result.add(i);
      }
      assert result.size() == ai.length;
      return result;
   }
   
   /**
    * @param at
    * @return a List containing the elements of the given array.
    * @precondition at != null
    * @postcondition result != null
    * @postcondition result.size() == at.length
    */
   public static <T> List<T> asList(T[] at) {
      final List<T> result = new ArrayList<T>(at.length);
      for (T t : at) 
         result.add(t);
      assert result.size() == at.length;
      return result;
   }
   
   /**
    * Returns a new list with {@code t1} as first element followed by
    * all elements of {@code ts}.
    */
   public static <T> List<T> asList(T t1, T...ts) {
   	List<T> list = new ArrayList<T>(1 + ts.length);
   	list.add(t1);
   	for (T t : ts)
   		list.add(t);
   	return list;
   }

   /**
    * @param ae
    * @return a set containing the given elements
    * @precondition ae != null
    * @postcondition result != null
    * @postcondition result.containsAll(Arrays.asList(ae))
    */
   public static <E> Set<E> asSet(E... ae) {
      final Set<E> result = new HashSet<E>(Arrays.asList(ae));

      assert result != null;
      assert result.containsAll(Arrays.asList(ae));
      return result;
   }

   /**
    * @param coll
    * @postcondition (coll == null) --> (result == 0)
    * @postcondition (coll != null) --> (result == coll.size())
    */
   public static int size(Collection<?> coll) {
      return (coll == null) ? 0 : coll.size();
   }

   /**
    * @param coll
    * @return a new sorted List containing the elements of the given Collection.
    * @precondition coll != null
    * @postcondition result != null
    * @postcondition result != coll
    */
   public static <E extends Comparable<E>> ArrayList<E> sorted(Collection<E> coll) {
      final ArrayList<E> result = new ArrayList<E>(coll);
      Collections.sort(result);
      assert result != null;
      assert result != coll;
      return result;
   }

   /**
    * @param coll
    * @param	comparator Determines the ordering. If <code>null</code>, the natural ordering of the elements is used.
    * @return a new sorted List containing the elements of the given Collection.
    * @precondition coll != null
    * @postcondition result != null
    * @postcondition result != coll
    */
   public static <E> ArrayList<E> sorted(Collection<E> coll, Comparator<? super E> comparator) {
      final ArrayList<E> result = new ArrayList<E>(coll);
      Collections.sort(result, comparator);
      assert result != null;
      assert result != coll;
      return result;
   }

   /**
    * @param iterable
    * @param predicate
    * @return Does any element of <code>iterable</code> satisfy <code>predicate</code>?
    * @precondition iterable != null
    * @precondition predicate != null
    */
   public static <E> boolean exists(Iterable<? extends E> iterable, Predicate<? super E> predicate) {
      for (E e : iterable) {
         if (predicate.evaluate(e)) {
            return true;
         }
      }
      return false;
   }

   /**
    * @param iterable
    * @param predicate
    * @precondition iterable != null
    * @precondition predicate != null
    * @return Do all elements of <code>iterable</code> satisfy <code>predicate</code>?
    */
   public static <E> boolean forall(Iterable<? extends E> iterable, Predicate<? super E> predicate) {
      for (E e : iterable) {
         if (!predicate.evaluate(e)) {
            return false;
         }
      }
      return true;
   }

   /**
    * splits <code>iterable</code> into the objects that satisfy or don't satisfy the given predicate, respectively.
    * @param iterable is not changed.
    * @param predicate
    * @param collTrue on exit, contains the objects from <code>iterable</code> that satisfy the <code>predicate</code>.
    * @param collFalse on exit, contains the objects from <code>iterable</code> that don't satisfy the <code>predicate</code>.
    * @precondition iterable != null
    * @precondition predicate != null
    * @precondition collTrue != null
    * @precondition collFalse != null
    */
   public static <E> void split(Iterable<E> iterable, Predicate<? super E> predicate, Collection<? super E> collTrue, Collection<? super E> collFalse) {
      collTrue.clear();
      collFalse.clear();
      for (E e : iterable) {
         // Unfortunately, the compiler complains about this one:
         // (predicate.evaluate(e) ? collTrue : collFalse).add(e);

         if (predicate.evaluate(e)) {
            collTrue.add(e);
         }
         else {
            collFalse.add(e);
         }
      }
   }

   public static <T> Pair<List<T>, List<T>> split(Iterable<T> input, Predicate<? super T> test) {
   	List<T> listTrue = new ArrayList<T>(), listFalse = new ArrayList<T>();
   	split(input, test, listTrue, listFalse);
   	return new Pair<List<T>, List<T>>(listTrue, listFalse);
   }
   
   public static <T> List<List<T>> splitEvery(Iterable<T> input, int size) {
   	List<List<T>> listOfList = new LinkedList<List<T>>();
   	List<T> list = null;
   	int index = 0;
   	for (T t : input) {
   		if ((index % size) == 0) {
   			list = new ArrayList<T>(size);
   			listOfList.add(list);
   		}
   		index++;
   		list.add(t);
   	}
   	return listOfList;
   }
   
   /**
    * splits the given List<E> into a List<List<E>>.
    * @param lst
    * @param transformerGetValue gets a value from an element <code>e</code>.
    * @return A list of lists, where each contained list contains only elements <code>e</code>
    * with the same value, as specified by <code>transformerGetValue(e)</code>.
    * @precondition lst != null
    * @precondition transformerGetValue != null
    * @postcondition result != null
    */
   public static <E,V> List<List<E>> splitByValue(Collection<E> lst, Transformer<? super E, V> transformerGetValue) {
      return new ArrayList<List<E>>(splitIntoMap(lst, transformerGetValue).asMap().values());
   }

   /**
    * Splits the given elements into sublists. The elements are added in order and a split occurs 
    * every time predicate returns true.
    */
   public static <E,V> List<List<E>> splitBySplitPredicate(Iterable<E> input, Predicate<? super E> predicate) {
   	List<List<E>> listOfLists = new ArrayList<List<E>>();
   	List<E> list = null;
   	for (E e : input) {
   		if (predicate.evaluate(e) || list == null)
   			listOfLists.add(list = new ArrayList<E>());
   		list.add(e);
   	}
   	return listOfLists;
   }
   
   
   /**
    * splits the values in the given list according to a given transformer, thus grouping all elements that get transformed
    * to the same value (or better: key).
    * @param lst
    * @param transformerGetKey transformer that makes a key for the resulting map out of each element in <code>lst</code>.
    * @return a MultiListMap containing a list of all elements in <code>lst</code> that are transformed to the
    * "same" (with respect to equals) key.
    */
   public static <K,E> MultiListMap<K, E> splitIntoMap(Iterable<E> lst, Transformer<? super E, K> transformerGetKey) {
      final MultiListMap<K, E> result = new MultiListHashMap<K, E>();
      for (E e : lst) {
         result.addValue(transformerGetKey.transform(e), e);
      }
      return result;
   }

   /**
    * transforms the given collection into a new list by iterating it and performing the given <code>transformer</code>
    * on each element, then adding the transformer's output to the result list.
    * <p>
    * This function is called "map" in functional languages like Haskell.
    * <p>
    * Concerning the name of this method: "map" would be misleading, as a map is something different in Java.
    * "transformed" would have also been okay, indicating that this method is side-effect free,
    * esp. that it doesn't change the input collection, but "transform" seems a less awkward name.
    * Transforming a list in place isn't as common anyway, so "transformInPlace" is used for that case.
    * The old name of this method "collect" was adapted from the Apache Commons CollectionUtils, but "collect" is
    * already used in the sense of "(Daten) erfassen" in the Novabit common lib, and it doesn't explain what the
    * method really does: transforming a collection into another.
    * @param coll isn't changed. (Note that generics and transforming a Collection in place don't quite fit together!)
    * @param transformer
    * @return a new transformed list resulting from performing the <code>transformer</code> on each element of <code>coll</code>.
    * @precondition coll != null
    * @precondition transformer != null
    * @postcondition result != null
    * @postcondition result != coll
    */
   public static <I, O> List<O> transform(Iterable<I> coll, Transformer<? super I, O> transformer) {
	   return transform(coll, transformer,null);
   }
   
   public static <I, O> List<O> transform(Iterable<I> coll, Transformer<? super I, O> transformer, Predicate<? super O> afterTransformPredicate) {
      final List<O> result = new ArrayList<O>(coll instanceof Collection ? size((Collection<?>) coll) : 10);
      for (I i : coll) {
		O transformedValue = transformer.transform(i);
	   	if(afterTransformPredicate == null || afterTransformPredicate.evaluate(transformedValue)) {
	   	  result.add(transformedValue);
	   	}
      }
      assert result != null;
      assert result != coll;
      return result;
   }
   
   public static <IT, IE extends IT,O> List<O> transform(IE[] input, Transformer<IT, O> transformer) {
	   return transform(Arrays.asList(input), transformer);
   }

   /**
    * transforms the given collection into a new set by iterating it and performing the given <code>transformer</code>
    * on each element, then adding the transformer's output to the result set.
    * @param coll isn't changed. (Note that generics and transforming a Collection in place don't fit together!)
    * @param transformer
    * @return a new transformed set resulting from performing the <code>transformer</code> on each element of <code>coll</code>.
    * @precondition coll != null
    * @precondition transformer != null
    * @postcondition result != null
    * @postcondition result != coll
    */
   public static <IT, IE extends IT,O> Set<O> transformIntoSet(Collection<IE> coll, Transformer<IT, O> transformer) {
      final Set<O> result = new HashSet<O>(coll.size());
      for (IE i : coll) {
         result.add(transformer.transform(i));
      }
      assert result != null;
      assert result != coll;
      return result;
   }

   /**
    * Transforms the given collection into a new map. For every item in the list, an entry is put
    * into the map.  The key of the entry is generated using the given transformer; the value is
    * the item itself.
    */
   public static <T, K> Map<K, T> transformIntoMap(Iterable<? extends T> iter, Transformer<? super T, K> keyTransformer) {
      return CollectionUtils.<T, K, T>transformIntoMap(iter, keyTransformer, TransformerUtils.<T>id());
   }

   /**
    * Transforms the given collection into a new map. For every item in the list, an entry is put
    * into the map.  The key of the entry and value are generated using the given transformers.
    */
   public static <T, K, V> Map<K, V> transformIntoMap(Iterable<? extends T> iter, Transformer<? super T, K> keyTransformer, Transformer<? super T, V> valueTransformer) {
      Map<K, V> map = new HashMap<K, V>();
      for (T item : iter)
         map.put(keyTransformer.transform(item), valueTransformer.transform(item));
      return map;
   }

   /**
    * Transforms the given collection of pairs into a new map. For every pair in the given collection,
    * an entry is put into the map.  The pair's {@link Pair#x} component is used as key, and the {@link Pair#y}
    * is used as value.
    */
   public static <K, V> Map<K, V> transformPairsIntoMap(Iterable<? extends Pair<? extends K, ? extends V>> iter) {
      Map<K, V> map = new HashMap<K, V>();
      for (Pair<? extends K, ? extends V> pair : iter)
         map.put(pair.x, pair.y);
      return map;
   }

   /**
    * transforms the given list in place.
    * Each element <code>e</code> in the list is replaced with <code>transformer.transform(e)</code>.
    * <p>
    * Note that this method is not quite as useful as <code>transform()</code>, but may be used in rare situations
    * when a huge list must be transformed in a list of the same type and when creating another huge list is too expensive.
    * @param lst
    * @param transformer
    * @precondition lst != null
    * @precondition transformer != null
    */
   public static <E> void transformInPlace(List<E> lst, Transformer<E, E> transformer) {
      for (ListIterator<E> lstiter = lst.listIterator(); lstiter.hasNext();) {
         final E e = lstiter.next();
         lstiter.set(transformer.transform(e));
      }
   }

   /**
    * transforms a map by applying the given transformation to each value of the given map.
    * @param mp is not modified
    * @param transformer
    * @return the transformed map
    * @postcondition result != null
    * @postcondition result != mp
    * @todo write a test
    */
   public static <K,VI,VO> Map<K, VO> transformMap(Map<K, ? extends VI> mp, Transformer<VI, VO> transformer) {
      final Map<K, VO> result = new HashMap<K, VO>();
      for (K key : mp.keySet()) {
         result.put(key, transformer.transform(mp.get(key)));
      }
      assert result != null;
      assert result != mp;
      return result;
   }
   
	public static <T, R> R[] transformArray(T[] input, Class<R> componentType, Transformer<T, R> transformer) {
   	R[] result = (R[]) Array.newInstance(componentType, input.length);
   	for (int i = 0, n = input.length; i < n; i++) {
   		result[i] = transformer.transform(input[i]);
   	}
   	return result;
   }

   /**
    * Equivalent to {@code concatAll(transform(input, transformer))}.
    */
   public static <T, R> List<R> concatTransform(Collection<? extends T> input, Transformer<T, ? extends Collection<R>> transformer) {
   	return concatAll(transform(input, transformer));
   }
   
   /**
    * Creates a new list out of the given <code>iterable</code>, containing all the elements that match the given
    * <code>predicate</code>.
    * <p>
    * This function is called "filter" in Haskell.
    * @param iterable
    * @param predicate
    * @return a new list containing the elements of <code>iterable</code> that match <code>predicate</code>.
    * @precondition iterable != null
    * @precondition predicate != null
    * @postcondition result != null
    * @postcondition result != iterable
    */
   public static <E> List<E> select(Iterable<E> iterable, Predicate<? super E> predicate) {
      final List<E> result = new ArrayList<E>();
      for (E e : iterable) {
         if (predicate.evaluate(e)) {
            result.add(e);
         }
      }
      assert result != null;
      assert result != iterable;
      return result;
   }

   /**
    * Creates a new set out of the given <code>iterable</code>, containing all the elements that match the given
    * <code>predicate</code>.
    * @param iterable
    * @param predicate
    * @return a new set containing the elements of <code>iterable</code> that match <code>predicate</code>.
    * @precondition iterable != null
    * @precondition predicate != null
    * @postcondition result != null
    * @postcondition result != iterable
    */
   public static <E> Set<E> selectIntoSet(Iterable<E> iterable, Predicate<? super E> predicate) {
      final Set<E> result = new HashSet<E>();
      for (E e : iterable) {
         if (predicate.evaluate(e)) {
            result.add(e);
         }
      }
      assert result != null;
      assert result != iterable;
      return result;
   }

	public static <E> SortedSet<E> selectIntoSortedSet(Iterable<E> iterable,
			Predicate<? super E> predicate, Comparator<E> comp) 
	{
		final SortedSet<E> result = new TreeSet<E>(comp);
		for (E e : iterable) {
			if (predicate.evaluate(e)) {
				result.add(e);
			}
		}
		assert result != null;
		assert result != iterable;
		return result;
	}

   /**
    * @param iterable
    * @param cls
    * @return a new list containing the elements of <code>iterable</code> that are instances of the given class
    * or instance of a superclass of the given class.
    * @precondition iterable != null
    * @precondition cls != null
    * @postcondition result != null
    * @postcondition result != iterable
    */
   public static <I, O extends I> List<O> selectInstancesOf(Iterable<I> iterable, Class<? extends O> cls) {
      final List<I> lsti = select(iterable, PredicateUtils.<I>isInstanceOf(cls));

      // As now we know that every member of lsti is instanceof (at least) O, we can safely cast the result to List<O>:
      final List<O> result = (List<O>) lsti;

      assert result != null;
      assert result != iterable;

      return result;
   }

   /**
    * selects from the given iterable all elements that have distinct values with respect to the given transformer.
    * In other words, removes all but one of each set of elements that are transformed into the same value.
    * @param iterable
    * @param transformerGetValue
    * @return a new list containing only distinct values.
    * @todo write a test
    */
   public static <E,K> List<E> selectDistinct(Iterable<E> iterable, Transformer<? super E, K> transformerGetValue) {
      final Map<K, E> mp = new HashMap<K, E>();
      for (E e : iterable) {
         final K key = transformerGetValue.transform(e);
         if (!mp.containsKey(key)) {
            mp.put(key, e);
         }
      }
      return new ArrayList<E>(mp.values());
   }

   /**
    * @param iterable
    * @param predicate
    * @return the first element, if any, of <code>iterable</code> that satisfies <code>predicate</code>.
    */
   public static <E> E findFirst(Iterable<E> iterable, Predicate<? super E> predicate) {
      E result = null;
      for (E e : iterable) {
         if (predicate.evaluate(e)) {
            result = e;
            break;
         }
      }
      return result;
   }
   
   /**
    * Returns the index of the first element that satisfies the predicate.
    */
   public static <E> int indexOfFirst(Iterable<E> iterable, Predicate<? super E> predicate) {
	   int index = 0;
	   for (E e : iterable) {
		   if (predicate.evaluate(e)) {
			   return index;
		   }
		   index++;
	   }
	   return -1;
   }

   /**
    * @param iterable
    * @param eValue
    * @param predicateEquals
    * @return Is any element of <code>iterable</code> equal to the given value (where equality is defined by the given predicate)?
    */
   public static <E1, E extends E1> boolean contains(Iterable<? extends E> iterable, E eValue, BinaryPredicate<E1, E1> predicateEquals) {
      return exists(iterable, PredicateUtils.bindFirst(predicateEquals, eValue));
   }

   /**
    * iterates over the given Iterator <code>iter</code> and adds all elements to the given Collection <code>collInOut</code>.
    * @param collInOut
    * @param iter
    * @precondition collInOut != null
    * @precondition iter != null
    * @return the same collection that has been passed as collInOut
    */
   public static <E, C extends Collection<E>> C addAll(C collInOut, Iterator<? extends E> iter) {
      while (iter.hasNext()) {
         collInOut.add(iter.next());
      }
      return collInOut;
   }

   /**
    * Iterates over the given Enumeration <code>e</code> and adds all elements to the given Collection <code>collInOut</code>.
    * @return the same collection that has been passed as collInOut
    */
   public static <E, C extends Collection<E>> C addAll(C collInOut, Enumeration<? extends E> e) {
      while (e.hasMoreElements()) {
         collInOut.add(e.nextElement());
      }
      return collInOut;
   }
   
   /**
    * same as addAll above, only using an array of elements
    * @param collInOut
    * @param elems
    * @precondition collInOut != null
    * @return the same collection that has been passed as collInOut
    */
   public static <E, C extends Collection<E>> C addAll(C collInOut, E[] elems) {
      for(E e : elems)
         collInOut.add(e);
      return collInOut;
   }


   /**
    * removes all elements from the given Collection that satisfy the given predicate.
    * @param collInOut
    * @param predicate
    * @precondition collInOut != null
    * @precondition predicate != null
    */
   public static <E> void removeAll(Collection<E> collInOut, Predicate<? super E> predicate) {
      for (Iterator<E> iter = collInOut.iterator(); iter.hasNext();) {
         if (predicate.evaluate(iter.next())) {
            iter.remove();
         }
      }
   }

   /**
    * removes all elements from the given Collection that DO NOT satisfy the given predicate.
    * @param collInOut
    * @param predicate
    * @precondition collInOut != null
    * @precondition predicate != null
    */
   public static <E> void retainAll(Collection<E> collInOut, Predicate<? super E> predicate) {
      for (Iterator<E> iter = collInOut.iterator(); iter.hasNext();) {
         if (!predicate.evaluate(iter.next())) {
            iter.remove();
         }
      }
   }

   /**
    * removes all elements of <code>collInOut</code> that are NOT contained in <code>iterable</code>, where equality
    * is defined by the given predicate.
    * @param collInOut
    * @param iterable
    * @param predicateEquals defines equality between elements
    * @precondition collInOut != null
    * @precondition iterable != null
    * @precondition predicateEquals != null
    */
   private static <E1, E extends E1> void retainAll(Collection<E> collInOut, Iterable<? extends E> iterable, BinaryPredicate<E1, E1> predicateEquals) {
      for (Iterator<E> iter = collInOut.iterator(); iter.hasNext();) {
         if (!contains(iterable, iter.next(), predicateEquals)) {
            iter.remove();
         }
      }
   }
   
   /**
    * removes all elements of <code>listInOut</code> that are NOT contained in <code>iterable</code>
    * @param listInOut
    * @param iterable
    * @precondition listInOut != null
    * @precondition iterable != null
    */
   public static <E1, E extends E1> void retainAll(List<E> listInOut, Iterable<? extends E> iterable) {
      for (Iterator<E> iter = listInOut.iterator(); iter.hasNext();) {
         if (!contains(iterable, iter.next(), PredicateUtils.<E>equals())) {
            iter.remove();
         }
      }
   }

   /**
    * @return the intersection of all Collections contained in iterable, based on <code>LangUtils.equals()</code>.
    * @see LangUtils#equals(Object, Object)
    */
   public static <E> Set<E> intersection(Collection<? extends E> a, Collection<? extends E> b) {
      ArrayList<Collection<? extends E>> tmp = new ArrayList<Collection<? extends E>>(2);
      tmp.add(a); tmp.add(b);
      return intersectionAll(tmp);
   }

   /**
    * @param iterable
    * @return the intersection of all Collections contained in <code>iterable</code>, based on <code>LangUtils.equals()</code>.
    * @precondition iterable != null
    * @see LangUtils#equals(Object, Object)
    */
   public static <E> Set<E> intersectionAll(Iterable<? extends Collection<? extends E>> iterable) {
      return intersectionAll(iterable, PredicateUtils.<E>equals());
   }

   /**
    * @param iterable
    * @param predicateEquals defines equality between elements.
    * @return the intersectionAll of all Collections contained in iterable.
    * @precondition iterable != null
    * @precondition predicateEquals != null
    * @todo optimize
    */
   public static <E1, E extends E1> Set<E> intersectionAll(Iterable<? extends Collection<? extends E>> iterable, BinaryPredicate<E1, E1> predicateEquals) {
      final Set<E> result = new HashSet<E>();
      final Iterator<? extends Collection<? extends E>> iter = iterable.iterator();
      if (iter.hasNext()) {
         /** @todo foldl could be applied elegantly here */
         result.addAll(iter.next());
         while (iter.hasNext()) {
            retainAll(result, iter.next(), predicateEquals);
         }
      }
      return result;
   }

   /**
    * Wraps an Enumeration (often used in older libraries) in an iterable.
    * (Probably every library nowadays has such a wrapper...)
    *
    * @param e the enum
    * @return an iterable of the same type
    */
   public static <T> Iterable<T> iterableEnum(final Enumeration<T> e) {
      return new Iterable<T>() {
         @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                  return e.hasMoreElements();
               }
            @Override
            public T next() {
                  return e.nextElement();
               }
            @Override
            public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   /**
    * @return a Set containing the elements contained in at least one of the given collections.
    * Note that the result is a set so it will contain no duplicates (based on <code>E.equals()</code>).
    */
   public static <E> Set<E> union(Collection<? extends E> a, Collection<? extends E> b) {
      ArrayList<Collection<? extends E>> tmp = new ArrayList<Collection<? extends E>>(2);
      tmp.add(a); tmp.add(b);
      return unionAll(tmp);
   }

   /**
    * @param iterable iterates thru the collections to be merged into one.
    * @return a Set containing all the elements that are contained in at least one of the given collections.
    * Note that the result is a set so it will contain no duplicates (based on <code>E.equals()</code>).
    * @precondition iterable != null
    * @postcondition result != null
    */
   public static <E> Set<E> unionAll(Iterable<? extends Collection<? extends E>> iterable) {
      final Set<E> result = new HashSet<E>();
      for (Collection<? extends E> coll : iterable) {
         result.addAll(coll);
      }
      assert result != null;
      return result;
   }

   /**
    * concatenates the given Collections into one Collection.
    * @return the elements of all given Collections concatenated into one Collection.
    */
   public static <E> List<E> concat(Collection<? extends E> a, Collection<? extends E> b) {
      ArrayList<Collection<? extends E>> tmp = new ArrayList<Collection<? extends E>>(2);
      tmp.add(a); tmp.add(b);
      return concatAll(tmp);
   }

   /**
    * concatenates the given Collections to one Collection.
    * @param iterable iterates thru the collections to be merged into one.
    * @return the elements of all given Collections concatenated into one Collection.
    * @postcondition result != null
    */
   public static <E> List<E> concatAll(Iterable<? extends Collection<? extends E>> iterable) {
      final List<E> result = new ArrayList<E>();
      for (Collection<? extends E> coll : iterable) {
         result.addAll(coll);
      }
      assert result != null;
      return result;
   }

// Ouch!!!
//
//	/**
//	 * casts a Collection<I> down to a Collection<O>.
//	 * Note that NO ClassCastException is thrown if any element of the given collection is not instanceof O.
//	 * @param coll
//	 * @return coll
//	 * @postcondition result == coll
//	 */
//	public static <I, O extends I> Collection<O> castDown(Collection<I> coll) {
//		return (Collection<O>) coll;
//	}
//
//	/**
//	 * casts a Collection<I> down to a Collection<O>.
//	 * Note that NO ClassCastException is thrown if any element of the given collection is not instanceof O.
//	 * @param lst
//	 * @return lst
//	 * @postcondition result == lst
//	 */
//	public static <I, O extends I> List<O> castDown(List<I> lst) {
//		return (List<O>) lst;
//	}

   /**
    * writes the objects in iterable to the returned String, separating them with the given separator.
    * String.valueOf() is used to write each element of the collection.
    * @param iterable
    * @param sSeparator if non-empty, is written between each two elements (that is, not before the first one, and not after the last one)
    * @return a String containing the String representations of the given Collection's elements.
    * @precondition iterable != null
    * @postcondition result != null
    */
   public static String getSeparatedList(Iterable<?> iterable, String sSeparator) {
      return getSeparatedList(iterable, sSeparator, null, null);
   }

   /**
    * writes the objects in iterable to the returned String, separating them with the given separator.
    * String.valueOf() is used to write each element of the collection.
    * @param iterable
    * @param sSeparator if non-empty, is written between each two elements (that is, not before the first one, and not after the last one)
    * @param sPrefix if non-empty, is prepended before each element.
    * @param sSuffix if non-empty, is appended after each element.
    * @return a String containing the String representations of the given Collection's elements.
    * @precondition iterable != null
    * @postcondition result != null
    */
   public static String getSeparatedList(Iterable<?> iterable, String sSeparator, String sPrefix, String sSuffix) {
      final StringBuilder sb = new StringBuilder();
      for (Iterator<?> iter = iterable.iterator(); iter.hasNext();) {
         // prefix:
         if (sPrefix != null) {
            sb.append(sPrefix);
         }
         // the element itself:
         sb.append(iter.next());
         // suffix:
         if (sSuffix != null) {
            sb.append(sSuffix);
         }
         // separator:
         if (sSeparator != null && iter.hasNext()) {
            sb.append(sSeparator);
         }
      }
      return sb.toString();
   }

   /**
    * Returns an iterable as an enumeration (some older library code excepts
    * these)
    * @param iterable the iterable
    * @return an enumeration returning the same sequence of elements as the
    * parameter iterable
    * @precondition iterable != null
    * @postcondition result != null
    */
   public static <T> Enumeration<T> asEnumeration(final Iterable<T> iterable) {
      final Iterator<T> it = iterable.iterator();
      return new Enumeration<T>() {
         @Override public boolean hasMoreElements() { return it.hasNext(); }
         @Override public T nextElement()           { return it.next(); }
      };
   }

   /**
    * Returns an enumeration as an iterable.
    * @param enumeration the enumeration
    * @return an iterable wrapping the enumeration (note: remove is not supported)
    * @precondition enumeration != null
    * @postcondition result != null
    */
   public static <T> Iterable<T> asIterable(final Enumeration<T> enumeration) {
      return new Iterable<T>(){
         @Override
         public Iterator<T> iterator() {
            return new Iterator<T>() {
               @Override public boolean hasNext() { return enumeration.hasMoreElements(); }
               @Override public T next() { return enumeration.nextElement(); }
               @Override public void remove() { throw new UnsupportedOperationException(); }
            };
         }
      };
   }
   
   /**
    * Returns an iterator as an iterable. The returned iterable can only be used once.
    */
   public static <T> Iterable<T> asIterable(final Iterator<T> iterator) {
	   return new Iterable<T>(){
		   boolean consumed = false;
		   @Override
		   public Iterator<T> iterator() {
			   if (consumed)
				   throw new IllegalStateException("Iterator already used");
			   consumed = true;
			   return iterator;
		   }
	   };
   }


   /**
    * Check the type of list contents (for the situations where a simple upcast
    * is not enough.) As Java generics are implemented via erasure,
    * unfortunately the target class has to be passed as a parameter.
    * <p>
    * Basically, you can convert a list to a typed list via this method, using:
    * <pre>
    *   List someList = ...; // from somewhere else
    *   List&lt;MyType&gt; = typecheck(someList, MyType.class);
    * </pre>
    * The method will raise a class cast exception, if the input list contains
    * any element, that is not instanceof MyType or null.
    * @param T      the target type
    * @param l      the input list
    * @param tClass T's class
    * @return l as a parameterized type
    * 
    * @deprecated In the most common use case this is unnecessary. In the second
    * 		common use case this is a sign that you have not understood generics.
    */
   public static <T> List<T> typecheck(List<?> l, Class<T> tClass) {
      if(l == null)
         return null;

      for(Object o : l)
         if(o != null && !tClass.isInstance(o))
            throw new ClassCastException("ClassCast from " + o.getClass() + " to " + tClass);

      return (List<T>) l;
   }

   /**
    * Same as above, but implemented via a new array list instead of a cast.
    * @param T         the target type
    * @param l         the input collection
    * @param tClass    T's class
    * @return a new array list with all items of l cast into T
    * 
    * @deprecated In the most common use case this is unnecessary. In the second
    * 		common use case this is a sign that you have not understood generics.
    */
   public static <T> List<T> typecheck(Collection<?> l, Class<T> tClass) {
      if(l == null)
         return null;

      ArrayList<T> res = new ArrayList<T>();
      for(Object o : l)
         if(o != null && !tClass.isInstance(o))
            throw new ClassCastException("ClassCast from " + o.getClass() + " to " + tClass);
         else
            res.add((T) o);
      return res;
   }

   /**
    * Typecheck as above, performing on map values
    * @param <K>    Key type
    * @param <V>    Designated value type
    * @param m      the map to check
    * @param vClass the value type's class
    * @return m, accordingly cast, if no classCastException is thrown
    * 
    * @deprecated In the most common use case this is unnecessary. In the second
    * 		common use case this is a sign that you have not understood generics.
    */
   public static <K, V> Map<K, V> typecheck(Map<K, ?> m, Class<V> vClass) {
      if(m == null)
         return null;
      for(Map.Entry<K, ?> e : m.entrySet()) {
         Object o = e.getValue();
         if(o != null && !vClass.isInstance(o))
            throw new ClassCastException("ClassCast from " + o.getClass() + " to " + vClass);
      }
      return (Map<K, V>) m;
   }


   /**
    * Generic version of the org.apache.commons.collections.CollectionUtils.subtract
    * call.
    * @param a  a collection
    * @param b  a collection
    * @return a - b, if both are not null, null if a is null, a if b is null
    */
   public static <T> Collection<T> subtract(Collection<T> a, Collection<T> b) {
      if(a == null)
         return null;
      if(b == null)
         return a;
      ArrayList<T> r = new ArrayList<T>(a);
      r.removeAll(b);
      return r;
   }
   
   
   /**
    * @param a  a list
    * @param b  a list
    * @return a - b, if both are not null, null if a is null, a if b is null
    */
   public static <T> List<T> subtract(List<T> a, List<T> b) {
      if(a == null)
         return null;
      if(b == null)
         return a;
      ArrayList<T> r = new ArrayList<T>(a);
      r.removeAll(b);
      return r;
   }


   /**
    * Internal lookup map generator: all elements of an input go through a key
    * and a value transformer, and those results get put into the map.
    *
    * @param in          input collection
    * @param out         target map
    * @param keyTrans    the key transformer
    * @param valueTrans  the value transformer
    * @return out
    */
   private static <E, K, V, M extends Map<K, V>> M generateLookupMap(Iterable<E> in, M out, Transformer<E, K> keyTrans, Transformer<E, V> valueTrans) {
      for(E e : in)
         out.put(keyTrans.transform(e), valueTrans.transform(e));
      return out;
   }


   /**
    * Lookup-map-generator of the most primitive kind, only using a key-transformer
    *
    * @param c         a collection to generate a map from
    * @param keyTrans  a transformer to create a key from an element of c
    * @return a map filled with all elements of c, so that for each
    * map entry the key is the transformation result, and the value the original
    * object. Note, that this method does not handle key-collisions!
    */
   public static <K, E> HashMap<K, E> generateLookupMap(Iterable<E> in, Transformer<E, K> keyTrans) {
      return generateLookupMap(in, new HashMap<K, E>(), keyTrans, TransformerUtils.<E>id());
   }


   /**
    * Lookup map generator using a key and a value transformer
    * @param in           input collection
    * @param keyTrans     the key transformer
    * @param valueTrans   the value transformer
    * @return a map filled with all elements of c, so that for each
    * map entry the key is the key-transformers result, and the value the
    * value transformation result. Note, that this method does not handle
    * key-collisions!
    */
   public static <K, V, E> HashMap<K, V> generateLookupMap(Iterable<E> in, Transformer<E, K> keyTrans, Transformer<E, V> valueTrans) {
      return generateLookupMap(in, new HashMap<K, V>(), keyTrans, valueTrans);
   }


   /**
    * Filter an "in"-collection using a given predicate and filling the "out"
    * collection. The method adds all matching elements of in to out
    * @param in     collection
    * @param filter the filter
    * @param out    collection
    * @return out
    */
   private static <T, C extends Collection<T>> C applyFilter(Collection<? extends T> in, Predicate<? super T> filter, C out) {
      for(T t : in)
         if(filter.evaluate(t))
            out.add(t);
      return out;
   }


   /**
    * Return a list of all elements of in that satisfy filter
    * @param in      the collection
    * @param filter  the predicate
    * @return a list of matches
    */
   public static <T> List<T> applyFilter(Collection<? extends T> in, Predicate<? super T> filter) {
      return in == null ? null : applyFilter(in, filter, new ArrayList<T>());
   }

   /**
    * removes duplicate values from a collection.
    * @param collection the collection with duplicate values
    * @param predicateEquals the predicate that determines equality
    * @return a collection with distinct values
    */
   public static <E1, E extends E1> Set<E> distinct(Collection<? extends E> collection, BinaryPredicate<E1, E1> predicateEquals) {
         final Set<E> result = new HashSet<E>();
         final Iterator<? extends E> iter = (new ArrayList<E>(collection)).iterator();
         while (iter.hasNext()) {
               E e = iter.next();
               if (!contains(result, e, predicateEquals)) {
                     result.add(e);
               }
         }
         return result;
   }

   /**
    * @deprecated Strongly consider if you want the first element or rather only expect
    * 		one element. In the latter case, use {@link #getUnique(Iterable)} or 
    * 		{@link #getSingleIfExist(Iterable)}.
    */
   public static <T> T getFirst(Iterable<T> iterable) {
      return getFirst(iterable, null);
   }
   
   public static <T> T getLastOrNull(List<T> l) {
	   final int size = l.size();
	   if (size == 0) return null;
	   return l.get(size - 1);
   }
   
   public static <T> T getFirst(Iterable<? extends T> iterable, T def) {
      final Iterator<? extends T> iter = iterable.iterator();
      return iter.hasNext() ? iter.next() : def;
   }
   
	public static <T> T getUnique(Iterable<T> iterable) {
		final Iterator<? extends T> iter = iterable.iterator();
		if (!iter.hasNext()) {
			throw new IllegalArgumentException("No element in " + iterable);
		}
		final T result =  iter.next();
		if (iter.hasNext()) {
			throw new IllegalArgumentException("More than (expected) one element in " + iterable);
		}
		return result;
	}

	public static <T> T getSingleIfExist(Iterable<T> iterable) {
		if (iterable == null) return null;
		final Iterator<? extends T> iter = iterable.iterator();
		if (!iter.hasNext()) return null;
		final T result =  iter.next();
		if (iter.hasNext()) {
			throw new IllegalArgumentException("More than (expected) one element in " + iterable);
		}
		return result;
	}

   public static <T> List<T> iterableToList(Iterable<T> iterable) {
      if (iterable instanceof Collection<?>) {
         return new ArrayList<T>((Collection<T>) iterable);
      } else {
      	List<T> list = new ArrayList<T>();
      	for (T t : iterable)
      		list.add(t);
      	return list;
      }
   }
   
   public static <X, Y> List<Pair<X, Y>> zip(Iterable<X> input1, Iterable<Y> input2) {
   	List<Pair<X, Y>> list = new ArrayList<Pair<X, Y>>();
   	Iterator<X> iter1 = input1.iterator();
   	Iterator<Y> iter2 = input2.iterator();
   	while (iter1.hasNext() && iter2.hasNext())
   		list.add(new Pair<X, Y>(iter1.next(), iter2.next()));
   	return list;
   }   

   public static <X, Y> Pair<List<X>, List<Y>> unzip(Iterable<Pair<X, Y>> input) {
   	List<X> x = new ArrayList<X>();
   	List<Y> y = new ArrayList<Y>();
   	for (Pair<X, Y> p : input) {
   		x.add(p.x); y.add(p.y);
   	}
   	return Pair.makePair(x, y);
   }
   
   public static <T> Iterable<T> replicate(final T value, final int count) {
   	return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					int remaining = count;
					@Override
					public boolean hasNext() {
						return remaining > 0;
					}
					@Override
					public T next() {
						if (remaining-- <= 0)
							throw new NoSuchElementException();
						return value;
					}
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
   	};
   }
   
   public static <T> boolean contains(T value, T...objects) {
   	if (objects == null)
   		return false;
   	for (T obj : objects) {
   		if (ObjectUtils.equals(obj, value))
   			return true;
   	}
   	return false;
   }
   
   
   
   public static <T> List<T> indexedSelection(List<T> in, int ... indices) {
	   ArrayList<T> res = new ArrayList<T>();
	   for(int i : indices)
		   res.add(in.get(i));
	   return res;
   }
   
   public static StringBuilder join(CharSequence separator, Collection<?> col) {
	   final StringBuilder result = new StringBuilder();
	   if (col != null) {
		   for (Iterator<?> it = col.iterator(); it.hasNext();) {
			   result.append(it.next());
			   if (it.hasNext()) {
				   result.append(separator);
			   }
		   }
	   }
	   return result;
   }
   
   public static <T> void removeDublicates(List<T> l) {
	   final Set<T> set = new HashSet<T>();
	   for (Iterator<T> it = l.iterator(); it.hasNext(); ) {
		   final T t = it.next();
		   if (!set.add(t)) {
			   it.remove();
		   }
	   }
   }
   
   public static <T> List<T> copyWithoutDublicates(List<T> l) {
	   final List<T> result = new ArrayList<T>();
	   final Set<T> set = new HashSet<T>();
	   for (Iterator<T> it = l.iterator(); it.hasNext(); ) {
		   final T t = it.next();
		   if (set.add(t)) {
			   result.add(t);
		   }
	   }
	   return result;
   }
   
	/**
	 * Remove all null elements at the end of the list.
	 */
	public static <T> void trimTail(List<T> l) {
		final ListIterator<T> it = l.listIterator(l.size());
		while (it.hasPrevious()) {
			if (it.previous() != null)
				return;
			it.remove();
		}
	}

}	// class CollectionUtils

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

import org.nuclos.server.common.valueobject.NuclosValueObject;


/**
 * Utility methods for <code>Transformer</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo add what is useful from Apache's TransformerUtils.
 */
public class TransformerUtils {

	private TransformerUtils() {
	}

	/**
	 * @param transformer1
	 * @param transformer2
	 * @return a chained transformer combining the two given transformers.
	 */
	public static <I,M,O> ChainedTransformer<I, M, O> chained(Transformer<I, ? extends M> transformer1, Transformer<M, O> transformer2) {
		return new ChainedTransformer<I, M, O>(transformer1, transformer2);
	}

	/**
	 * Factory method for the id-transformer
	 */
	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> id() {
		return (Transformer<T, T>) IdentityTransformer.INSTANCE;
	}

	/**
	 * Returns the toString transformer. The transformer uses {@link String#valueOf(Object)} and
	 * hence is null-safe.
	 */
	public static Transformer<Object, String> toStringTransformer() {
		return ToStringTransformer.INSTANCE;
	}

	/**
	 * Basically the same as the GetId-subclass in NuclosValueObject, but usable
	 * for the whole type hierarchy
	 */
	public static <T extends NuclosValueObject> Transformer<T, Integer> getId() {
		return new Transformer<T, Integer>() {
			@Override
			public Integer transform(T i) {
				return i == null ? null : i.getId();
			}};
	}

	//
	// Internal implementation classes and singletons (single-element enum pattern) 
	//

	/**
	 * A chained transformer combining two given transformers.
	 */
	private static class ChainedTransformer<I, M, O> implements Transformer<I, O> {

		private final Transformer<I, ? extends M> transformer1;
		private final Transformer<M, O> transformer2;

		ChainedTransformer(Transformer<I, ? extends M> transformer1, Transformer<M, O> transformer2) {
			this.transformer1 = transformer1;
			this.transformer2 = transformer2;
		}

		@Override
		public O transform(I i) {
			return transformer2.transform(transformer1.transform(i));
		}

	}	// class ChainedTransformer

	private static enum ToStringTransformer implements Transformer<Object, String> {
		INSTANCE;
		
		@Override
		public String transform(Object o) {
			return String.valueOf(o); // null-safe toString()
		}
	}
	
	/**
	 * The id-transformer
	 */
	private static enum IdentityTransformer implements Transformer<Object, Object> {
		INSTANCE;

		@Override
		public Object transform(Object obj) {
			return obj;
		}
	}
}	// class TransformerUtils

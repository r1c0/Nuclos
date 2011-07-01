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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Factories {
	private Factories() {}


	public static <T> Factory<T> constFactory(final T t) {
		return new Factory<T>() {
			@Override
			public T create() {
				return t;
			}};
	}

	public static <T> Factory<T> memoizingFactory(final Factory<? extends T> factory) {
		return new MemoizingFactory<T>(factory);
	}

	public static <T> Factory<T> synchronizingFactory(final Factory<? extends T> factory) {
		return new SynchronizingFactory<T>(factory);
	}
	
	public static <T extends Cloneable> Factory<T> cloneFactory(final T t) {
		return new Factory<T>() {
			@SuppressWarnings("unchecked")
            @Override
			public T create() {
				Exception x = null;
				try {
					Class<?> c = t.getClass();
					Method m = c.getDeclaredMethod("clone", new Class[0]);
					return (T) m.invoke(t, new Object[0]);
				}
				catch(SecurityException e)         { x = e; }
				catch(IllegalArgumentException e)  { x = e; }
				catch(NoSuchMethodException e)     { x = e; }
				catch(IllegalAccessException e)    { x = e; }
				catch(InvocationTargetException e) { x = e; }
				throw new RuntimeException(x);
			}
		};
	}
	
	
	public static <T> Factory<T> defaultConstructorFactory(final Class<T> c) {
		return new Factory<T>() {
			@Override
            public T create() {
				Exception x = null;
				try {
	                Constructor<T> con = c.getConstructor(new Class[0]);
	                return con.newInstance(new Object[0]);
                }
                catch(SecurityException e)         { x = e; }
                catch(IllegalArgumentException e)  { x = e; }
                catch(NoSuchMethodException e)     { x = e; }
                catch(InstantiationException e)    { x = e; }
                catch(IllegalAccessException e)    { x = e; }
                catch(InvocationTargetException e) { x = e; }
                throw new RuntimeException(x);
            }};
	}
	
	private static class SynchronizingFactory<T> implements Factory<T> {

		private final Factory<? extends T> delegate;
		
		public SynchronizingFactory(Factory<? extends T> delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public T create() {
			synchronized (delegate) {
				return delegate.create();
			}
		}
	}

	private static class MemoizingFactory<T> implements Factory<T> {

		private Factory<? extends T> delegate;
		private T value;
		
		public MemoizingFactory(Factory<? extends T> delegate) {
			this.delegate = delegate;
			if (delegate == null)
				throw new NullPointerException();
		}
		
		@Override
		public T create() {
			Factory<? extends T> factory = delegate;
			if (factory != null) {
				value = factory.create();
				delegate = null;
			}
			return value;
		}
	}
}

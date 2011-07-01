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
package org.nuclos.server.common;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author leo.zeef
 *
 */
public class BeanToString {

	private static final int MAX = 4;
	private Object o1;
	private int level;

	public BeanToString(Object o1) {
		this.o1 = o1;
	}

	public BeanToString(Object o1, int level) {
		super();
		this.o1 = o1;
		this.level = level;
	}

	public String toBeanString() {
		String result = "";
		if (level > MAX) {
			return result;
		}
		try {
			if (o1 == null) {
				return result;
			}
			final Class<?> class1 = o1.getClass();
			// System.out.println(".." + level + " " + class1.getSimpleName());

			if (o1 instanceof Collection<?>) {
				Collection<?> c1 = (Collection<?>) o1;
				Iterator<?> i1 = c1.iterator();
				while (i1.hasNext()) {
					BeanToString bts = new BeanToString(i1.next(), level + 1);
					result += "[" + bts.toBeanString() + "]";
				}
				return result;
			}

			if (class1.isArray()) {
				Object[] array = (Object[]) o1;
				for (Object o : array) {
					BeanToString bts = new BeanToString(o, level + 1);
					result += bts.toBeanString();
				}
				return result;
			}

			if (isPrimitive(class1)) {
				return result + o1.toString();
			}

			BeanInfo bi;
			bi = Introspector.getBeanInfo(class1, Object.class);
			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				Method readMethod = pd.getReadMethod();

				if (readMethod != null) {
					try {
						Object value1 = readMethod.invoke(o1, (Object[]) null);

						BeanToString bts = new BeanToString(value1, level + 1);

						for (int i = 0; i < level; i++) {
							result += " ";
						}
						result += pd.getName() + " = " + bts.toBeanString();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {

		}

		return result;

	}

	private boolean isPrimitive(Class<?> class1) {
		if (class1.equals(Long.class)) {
			return true;
		}
		if (class1.equals(String.class)) {
			return true;
		}
		if (class1.equals(Double.class)) {
			return true;
		}
		if (class1.equals(Date.class)) {
			return true;
		}
		return false;
	}


}

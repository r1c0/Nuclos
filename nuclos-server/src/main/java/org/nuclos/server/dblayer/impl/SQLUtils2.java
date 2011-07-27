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
package org.nuclos.server.dblayer.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Wrapper;

import org.nuclos.common2.exception.CommonFatalException;

public class SQLUtils2 {

	public static <T> T unwrap(Object obj, Class<T> iface) throws SQLException {
		if (obj.getClass().getName().equals("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper")) {
			Method method;
			try {
				method = obj.getClass().getMethod("getInnermostDelegate");
				method.setAccessible(true);

				return unwrap(method.invoke(obj), iface);
			}
			catch(Exception e) {
				throw new CommonFatalException("Error calling org.jboss.resource.adapter.jdbc.WrappedConnection#getUnderlyingConnection()", e);
			}
		}
		if (iface.isInstance(obj)) {
			return iface.cast(obj);
		}
		if (obj instanceof Wrapper) {
			try {
				Wrapper wrapper = (Wrapper) obj;
				if (wrapper.isWrapperFor(iface)) {
					obj = wrapper.unwrap(iface);
				}

			} catch (AbstractMethodError e) {
				// this class didn't implement the interface completely (pre JDBC 4.0 implementation)
			}
		}
		return null;
	}

	public static String escape(String text) {
		// this is about 40% faster than org.apache.commons.lang.StringEscapeUtils.escapeSql and much faster than String.replace():
		return (text == null) ? null : replace(text, '\'', "''");
	}

	/**
	 * creates a new String out of sSource by replacing each occurence of cFrom in sSource with sTo (from left to right).
	 * @param sSource is unchanged
	 * @param cFrom
	 * @param sTo
	 */
	private static String replace(String sSource, char cFrom, String sTo) {
		final int iLength = sSource.length();
		final StringBuilder sb = new StringBuilder(iLength);
		for (int i = 0; i < iLength; ++i) {
			final char c = sSource.charAt(i);
			if (c == cFrom) {
				sb.append(sTo);
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}

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
package org.nuclos.server.dblayer.structure;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;

public class DbColumnType {

	static final int LENGTH = 0x01;
	static final int PRECISION = 0x02;
	static final int SCALE = 0x04;

	/**
	 * The generic type is a type specification which abstracts from some database-specific
	 * nuances. They represents the selection of base types used by Nuclos.
	 *
	 */
	public static enum DbGenericType {
		VARCHAR(String.class, Types.VARCHAR, LENGTH),
		// For better legacy support, the preferred type is Double (instead of BigDecimal)
		// TODO: INTEGER(Integer.class, Types.DECIMAL, PRECISION | SCALE)
		// (Note: Nuclos maps Integer to NUMERIC(_,0), so its part of NUMERIC)
		NUMERIC(new Class<?>[] {Double.class, Integer.class, Long.class, BigDecimal.class}, Types.DECIMAL, PRECISION | SCALE),
		BOOLEAN(Boolean.class, Types.BOOLEAN, 0),
		DATE(java.util.Date.class, Types.DATE, 0),
		CLOB(String.class, Types.CLOB, 0),
		BLOB(byte[].class, Types.BLOB, 0),
		DATETIME(InternalTimestamp.class, Types.TIMESTAMP, 0);

		final Class<?>[] javaTypes;
		final int sqlType;
		final int flags;

		private DbGenericType(Class<?> javaType, int sqlType, int flags) {
			this.javaTypes = new Class<?>[] {javaType};
			this.sqlType = sqlType;
			this.flags = flags;
		}

		private DbGenericType(Class<?>[] javaTypes, int sqlType, int flags) {
			this.javaTypes = javaTypes;
			this.sqlType = sqlType;
			this.flags = flags;
		}

		public Class<?> getPreferredJavaType() {
			return javaTypes[0];
		}

		public Class<?>[] getSupportedJavaTypes() {
			return javaTypes.clone();
		}

		/**
		 * Converts the given object (which must be a supported java type) into
		 * the specified (supported) java type. 
		 */
		@SuppressWarnings({ "unchecked", "cast" })
		public <T> T convert(Object obj, Class<T> javaType) {
			if (obj == null)
				throw new NullPointerException();
			if (javaType.isInstance(obj))
				return javaType.cast(obj);
			if (this == NUMERIC) {
				Class<?> clazz = javaType;
				if (clazz == Double.class) {
					return (T) (Object) Double.valueOf(((Number) obj).doubleValue());
				} else if (clazz == Integer.class) {
					return (T) (Object) Integer.valueOf(((Number) obj).intValue());
				} else if (clazz == Long.class) {
					return (T) (Object) Long.valueOf(((Number) obj).longValue());
				} else if (clazz == BigDecimal.class) {      			
					return (T) (Object) new BigDecimal(obj.toString());
				}
			}
			throw new ClassCastException("Cannot convert " + obj.getClass() + " to " + javaType);
		}


		/**
		 * Returns a portable string representation of the object. 
		 */
		public String encodeAsString(Object obj) {
			switch (this) {
			case VARCHAR:
				return (String) obj;
			case NUMERIC:
				return ((Number) obj).toString();
			case BOOLEAN:
				return ((Boolean) obj).toString();
			case DATE:
				return String.format("%tF", obj);
			case DATETIME:
				return new java.sql.Timestamp(((java.util.Date) obj).getTime()).toString();    
			case CLOB:
				return (String) obj;
			case BLOB:
				return Base64.encode((byte[]) obj);
				//Base64Utils.tob64((byte[]) obj);
			default:
				throw new UnsupportedOperationException("Unsupported type " + this);
			}
		}

		/**
		 * Parses the portable string representation of the object.
		 * Note that  
		 */
		public Object decodeFromString(String str) {
			switch (this) {
			case VARCHAR:
				return str;
			case NUMERIC:
				BigDecimal bd = new BigDecimal(str);
				if (str.indexOf('.') != -1) {
					double d = bd.doubleValue();
					// Test if the double can represent the given value with data loss
					if (bd.compareTo(new BigDecimal(Double.toString(d))) == 0) {
						return d;
					}
				} else {
					try {
						return bd.intValueExact();
					} catch (ArithmeticException e) {
					}
					try {
						return bd.longValueExact();
					} catch (ArithmeticException e) {
					}
				}
				return bd;
			case BOOLEAN:
				return Boolean.valueOf(str);
			case DATE:
				return java.sql.Date.valueOf(str);
			case DATETIME:
				return InternalTimestamp.toInternalTimestamp(java.sql.Timestamp.valueOf(str));
			case CLOB:
				return str;
			case BLOB:
				try {
					return  Base64.decode(str);
				}
				catch(Base64DecodingException e) {
					throw new NuclosFatalException(e);
				}
				//Base64Utils.fromb64(str);
			default:
				throw new UnsupportedOperationException("Unsupported type " + this);
			}
		}

		boolean hasFlag(int flag) {
			return (flags & flag) == flag;
		}
	}

	private final DbGenericType genericType;
	private final String typeName;
	// Common database APIs distinguishes these three parameters
	private final Integer length;
	private final Integer precision;
	private final Integer scale;

	public DbColumnType(DbGenericType genericType) {
		this(genericType, null, null, null, null);
	}

	public DbColumnType(DbGenericType genericType, int length) {
		this(genericType, null, length, null, null);
	}

	public DbColumnType(DbGenericType genericType, int precision, int scale) {
		this(genericType, null, null, precision, scale);
	}

	public DbColumnType(DbGenericType genericType, String typeName, Integer length, Integer precision, Integer scale) {
		this.genericType = genericType;
		this.typeName = typeName;
		this.length = (genericType != null && genericType.hasFlag(LENGTH)) ? length : null;
		this.precision = (genericType != null && genericType.hasFlag(PRECISION)) ? precision : null;
		this.scale = (genericType != null && genericType.hasFlag(SCALE)) ? scale : null;
	}

	public DbGenericType getGenericType() {
		return genericType;
	}

	public String getTypeName() {
		return typeName;
	}

	public Integer getLength() {
		return length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public Integer getScale() {
		return scale;
	}

	public Integer[] getParameters() {
		if (length != null) {
			return new Integer[] { length };
		} else if (precision != null) {
			return new Integer[] { precision, scale != null ? scale : 0 };
		} else {
			return new Integer[0];
		}
	}

	public String getParametersString() {
		Integer[] parameters = getParameters();
		return (parameters.length > 0) ? "(" + StringUtils.join(",", parameters) + ")" : "";
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] {genericType != null ? genericType : typeName, length, precision, scale});
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DbColumnType) {
			DbColumnType other = (DbColumnType) obj;
			if ((genericType != other.genericType) && (typeName == null || !ObjectUtils.equals(typeName, other.typeName)))
				return false;
			return Arrays.equals(getParameters(), other.getParameters());
		}
		return false;
	}
}

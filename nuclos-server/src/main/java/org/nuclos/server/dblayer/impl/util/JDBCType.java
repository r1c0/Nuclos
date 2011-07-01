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
package org.nuclos.server.dblayer.impl.util;

import java.sql.Types;

/**
 * Type mappings from the JDBC 4.0 standard.
 * 
 */
public enum JDBCType {

	CHAR(Types.CHAR, String.class, String.class),
	VARCHAR(Types.VARCHAR, String.class, String.class),
	LONGVARCHAR(Types.LONGVARCHAR, String.class, String.class),
	NUMERIC(Types.NUMERIC, java.math.BigDecimal.class, java.math.BigDecimal.class),
	DECIMAL(Types.DECIMAL, java.math.BigDecimal.class, java.math.BigDecimal.class),
	BIT(Types.BIT, boolean.class, Boolean.class),
	BOOLEAN(Types.BOOLEAN, boolean.class, Boolean.class),
	TINYINT(Types.TINYINT, byte.class, Integer.class),
	SMALLINT(Types.SMALLINT, short.class, Integer.class),
	INTEGER(Types.INTEGER, int.class, Integer.class),
	BIGINT(Types.BIGINT, long.class, Long.class),
	REAL(Types.REAL, float.class, Float.class),
	FLOAT(Types.FLOAT, double.class, Double.class),
	DOUBLE(Types.DOUBLE, double.class, Double.class),
	BINARY(Types.BINARY, byte[].class, byte[].class),
	VARBINARY(Types.VARBINARY, byte[].class, byte[].class),
	LONGVARBINARY(Types.LONGVARBINARY, byte[].class, byte[].class),
	DATE(Types.DATE, java.sql.Date.class, java.sql.Date.class),
	TIME(Types.TIME, java.sql.Time.class, java.sql.Time.class),
	TIMESTAMP(Types.TIMESTAMP, java.sql.Timestamp.class, java.sql.Timestamp.class),
	CLOB(Types.CLOB, java.sql.Clob.class, java.sql.Clob.class),
	BLOB(Types.BLOB, java.sql.Blob.class, java.sql.Blob.class),
	ARRAY(Types.ARRAY, java.sql.Array.class, java.sql.Array.class),
	DISTINCT(Types.DISTINCT, null, null),
	STRUCT(Types.STRUCT, java.sql.Struct.class, java.sql.Struct.class),
	REF(Types.REF, java.sql.Ref.class, java.sql.Ref.class),
	DATALINK(Types.DATALINK, java.net.URL.class, java.net.URL.class),
	JAVA_OBJECT(Types.JAVA_OBJECT, null, null),
	ROWID(Types.ROWID, java.sql.RowId.class, java.sql.RowId.class),
	NCHAR(Types.NCHAR, String.class, String.class),
	NVARCHAR(Types.NVARCHAR, String.class, String.class),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class, String.class),
	NCLOB(Types.NCLOB, java.sql.NClob.class, java.sql.NClob.class),
	SQLXML(Types.SQLXML, java.sql.SQLXML.class, java.sql.SQLXML.class),
	;

	private final int sqlType;
	private final Class<?> type;
	private final Class<?> objectType;

	private JDBCType(int sqlType, Class<?> type, Class<?> objectType) {
		this.sqlType = sqlType;
		this.type = type;
		this.objectType = objectType;
	}
	
	/** The Java SQL type from {@link java.sql.Types}. */
	public int getSqlType() {
		return sqlType;
	}
	
	/** The mapped Java Type. @seealso JDBC 4.0 standard, table B-1. */
	public Class<?> getType() {
		return type;
	}

	/** The mapped Java Object Type. @seealso JDBC 4.0 standard, table B-3. */
	public Class<?> getObjectType() {
		return objectType;
	}
	
	/** The standard mapping from Java Types to JDBC Types. @seealso JDBC 4.0 standard, table B-2. */
	public static JDBCType[] getJDBCTypesForType(Class<?> javaType) {
		if (javaType == String.class) {
			return new JDBCType[] { CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR };
		} else if (javaType == java.math.BigDecimal.class) {
			return new JDBCType[] { NUMERIC };
		} else if (javaType == boolean.class) {
			return new JDBCType[] { BIT, BOOLEAN };
		} else if (javaType == byte.class) {
			return new JDBCType[] { TINYINT };
		} else if (javaType == short.class) {
			return new JDBCType[] { SMALLINT };
		} else if (javaType == int.class) {
			return new JDBCType[] { INTEGER };
		} else if (javaType == long.class) {
			return new JDBCType[] { BIGINT };
		} else if (javaType == float.class) {
			return new JDBCType[] { REAL };
		} else if (javaType == double.class) {
			return new JDBCType[] { DOUBLE };
		} else if (javaType == byte[].class) {
			return new JDBCType[] { BINARY, VARBINARY, LONGVARBINARY };
		} else if (javaType == java.sql.Date.class) {
			return new JDBCType[] { DATE };
		} else if (javaType == java.sql.Time.class) {
			return new JDBCType[] { TIME };
		} else if (javaType == java.sql.Timestamp.class) {
			return new JDBCType[] { TIMESTAMP };
		} else if (javaType == java.sql.Clob.class) {
			return new JDBCType[] { CLOB };
		} else if (javaType == java.sql.Blob.class) {
			return new JDBCType[] { BLOB };
		} else if (javaType == java.sql.Array.class) {
			return new JDBCType[] { ARRAY };
		} else if (javaType == java.sql.Struct.class) {
			return new JDBCType[] { STRUCT };
		} else if (javaType == java.sql.Ref.class) {
			return new JDBCType[] { REF };
		} else if (javaType == java.net.URL.class) {
			return new JDBCType[] { DATALINK };
		} else if (javaType == java.sql.RowId.class) {
			return new JDBCType[] { ROWID };
		} else if (javaType == java.sql.NClob.class) {
			return new JDBCType[] { NCLOB };
		} else if (javaType == java.sql.SQLXML.class) {
			return new JDBCType[] { SQLXML };
		} else {
			return new JDBCType[] { JAVA_OBJECT };
		}
	}
	
	/** The standard mapping from Java Object Types to JDBC Types. @seealso JDBC 4.0 standard, table B-4. */
	public static JDBCType[] getJDCBTypesForObjectType(Class<?> objectType) {
		if(objectType == String.class) {
			return new JDBCType[] { CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR };
		} else if(objectType == java.math.BigDecimal.class) {
			return new JDBCType[] { NUMERIC };
		} else if(objectType == Boolean.class) {
			return new JDBCType[] { BIT, BOOLEAN };
		} else if(objectType == Byte.class) {
			return new JDBCType[] { TINYINT };
		} else if(objectType == Short.class) {
			return new JDBCType[] { SMALLINT };
		} else if(objectType == Integer.class) {
			return new JDBCType[] { INTEGER };
		} else if(objectType == Long.class) {
			return new JDBCType[] { BIGINT };
		} else if(objectType == Float.class) {
			return new JDBCType[] { REAL };
		} else if(objectType == Double.class) {
			return new JDBCType[] { DOUBLE };
		} else if(objectType == byte[].class) {
			return new JDBCType[] { BINARY, VARBINARY, LONGVARBINARY };
		} else if(objectType == java.sql.Date.class) {
			return new JDBCType[] { DATE };
		} else if(objectType == java.sql.Time.class) {
			return new JDBCType[] { TIME };
		} else if(objectType == java.sql.Timestamp.class) {
			return new JDBCType[] { TIMESTAMP };
		} else if(objectType == java.sql.Clob.class) {
			return new JDBCType[] { CLOB };
		} else if(objectType == java.sql.Blob.class) {
			return new JDBCType[] { BLOB };
		} else if(objectType == java.sql.Array.class) {
			return new JDBCType[] { ARRAY };
		} else if(objectType == java.sql.Struct.class) {
			return new JDBCType[] { STRUCT };
		} else if(objectType == java.sql.Ref.class) {
			return new JDBCType[] { REF };
		} else if(objectType == java.net.URL.class) {
			return new JDBCType[] { DATALINK };
		} else if(objectType == java.sql.RowId.class) {
			return new JDBCType[] { ROWID };
		} else if(objectType == java.sql.NClob.class) {
			return new JDBCType[] { NCLOB };
		} else if(objectType == java.sql.SQLXML.class) {
			return new JDBCType[] { SQLXML };
		} else {
			return new JDBCType[] { JAVA_OBJECT };
		}
	}

	/** Find the type object for a given sql {@link java.sql.Types} int value. */ 
	public static JDBCType fromSqlType(int sqlType) {
		for (JDBCType type : JDBCType.values()) {
			if (sqlType == type.sqlType)
				return type;
		}
		return null;
	}
}

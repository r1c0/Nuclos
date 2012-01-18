//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dal.processor;

import java.sql.SQLException;

import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.util.ServerCryptUtil;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;

/**
 * Type parameter T is the java type
 */
abstract class AbstractColumnToVOMapping<T> implements IColumnToVOMapping<T> {

	private final String tableAlias;
	private final String column;
	private final Class<T> dataType;
	private final boolean isReadonly;
	private final boolean caseSensitive;

	AbstractColumnToVOMapping(String tableAlias, String column, String dataType, boolean isReadonly, boolean caseSensitive) throws ClassNotFoundException {
		this(tableAlias, column, (Class<T>) Class.forName(dataType), isReadonly, caseSensitive);
	}

	AbstractColumnToVOMapping(String tableAlias, String column, Class<T> dataType, boolean isReadonly, boolean caseSensitive) {
		if (tableAlias == null) throw new NullPointerException();
		if (column == null) throw new NullPointerException();
		this.tableAlias = tableAlias;
		this.column = column;
		this.dataType = dataType;
		this.isReadonly = isReadonly;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public final String getTableAlias() {
		return tableAlias;
	}

	@Override
	public final String getColumn() {
		return column;
	}

	@Override
	public final boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public final boolean isReadonly() {
		return isReadonly;
	}

	@Override
	public final Class<T> getDataType() {
		return dataType;
	}
	
	@Override
	public DbExpression<T> getDbColumn(DbFrom table) {
		if (isCaseSensitive()) {
			return table.columnCaseSensitive(getTableAlias(), getColumn(),
					(Class<T>) DalUtils.getDbType(getDataType()));
		}
		else {
			return table.column(getTableAlias(), getColumn(),
					(Class<T>) DalUtils.getDbType(getDataType()));
		}
	}

	static <S> S convertFromDbValue(Object value, String column, final Class<S> dataType, final Long recordId) {
		if (dataType == ByteArrayCarrier.class) {
			return value == null ? null : (S) new ByteArrayCarrier((byte[]) value);
		} else if (dataType == NuclosImage.class) {
			NuclosImage ni = new NuclosImage("", (byte[]) value, null, false);
			return (S) ni;
		} else if (dataType == ResourceFile.class) {
			return (S) new ResourceFile((String) value, IdUtils.unsafeToId(recordId));
		} else if (dataType == GenericObjectDocumentFile.class) {
			if (value == null) {
				return null;
			}
			return (S) new GenericObjectDocumentFile((String) value, IdUtils.unsafeToId(recordId));
		} else if (dataType == DateTime.class) {
			return (S) new DateTime((java.util.Date) value);
		} else if (dataType == NuclosPassword.class) {
			if (value instanceof NuclosPassword)
				return (S) value;
			try {
				return (S) new NuclosPassword(ServerCryptUtil.decrypt((String) value));
			} catch (SQLException e) {
				throw new CommonFatalException(e);
			}
		} else {
			return dataType.cast(value);
		}
	}

	static Object convertToDbValue(Class<?> javaType, Object value) {
		if (value == null) {
			return DbNull.forType(DalUtils.getDbType(javaType));
		} else if (value instanceof ByteArrayCarrier) {
			return ((ByteArrayCarrier) value).getData();
		} else if (value instanceof NuclosImage) {
			NuclosImage ni = (NuclosImage) value;
			if (ni.getContent() != null) {
				ByteArrayCarrier bac = new ByteArrayCarrier(ni.getContent());
				return bac.getData();
			} else {
				return DbNull.forType(DalUtils.getDbType(javaType));
			}
		} else if (value instanceof ResourceFile) {
			return ((ResourceFile) value).getFilename();
		} else if (value instanceof GenericObjectDocumentFile) {
			return ((GenericObjectDocumentFile) value).getFilename();
		} else if (value instanceof DateTime) {
			return new InternalTimestamp(((DateTime) value).getTime());
		} else if (value instanceof NuclosPassword) {
			try {
				String encrypted = ServerCryptUtil.encrypt(((NuclosPassword) value).getValue());
				if (encrypted == null) {
					return DbNull.forType(java.lang.String.class);
				} else {
					return encrypted;
				}
			} catch (SQLException e) {
				throw new CommonFatalException(e);
			}
		} else {
			return value;
		}
	}

}

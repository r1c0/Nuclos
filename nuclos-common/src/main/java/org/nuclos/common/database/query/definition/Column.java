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
package org.nuclos.common.database.query.definition;

import java.io.Serializable;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;


/**
 * Column class for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class Column implements Serializable {

	private static final Logger LOG = Logger.getLogger(Column.class);
	
	private Table table;
	private final String name;
	private String alias;
	private DataType type;
	private int length;
	private int precision;
	private int scale;
	private boolean bNullable;
	private boolean bHidden;
	private String comment;
	private boolean bExpression;

	public Column(Column column) {
		this(column.table, column.name, column.alias, column.type, column.length, column.precision, column.scale,
			column.bNullable, column.isExpression());
	}

	/**
	 * @param table
	 * @param name
	 * @param type
	 * @param length
	 * @param nullable
	 * @precondition name != null
	 */
	public Column(Table table, String name, DataType type, int length, int precision, int scale, boolean nullable) {
		this(table, name, null, type, length, precision, scale, nullable, false);
	}

	public Column(Table table, String name, String alias, DataType type, int length, int precision, int scale,
		boolean nullable, boolean isExpression)
	{
		if (name == null) {
			throw new NullArgumentException("name");
		}
		this.table = table;
		this.name = name;
		this.alias = alias;
		this.type = type;
		this.length = length;
		this.precision = precision;
		this.scale = scale;
		this.bNullable = nullable;
		this.bHidden = false;
		this.comment = null;
		this.bExpression = isExpression;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table t) {
		table = t;
	}

	public String getName() {
		return name;
	}

	public DataType getType() {
		return type;
	}

	public boolean isNullable() {
		return bNullable;
	}

	public int getPrecision() {
		return precision;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getScale() {
		return scale;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setHidden(boolean bHidden) {
		this.bHidden = bHidden;
	}

	public boolean isHidden() {
		return bHidden;
	}

	// This method must be overwritten for use in Operand class.
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Column)) {
			return false;
		}
		final Column column = (Column) o;
		if (!name.equals(column.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public void show() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Column: " + name);
			type.show();
			LOG.debug("Length: " + length);
			if (precision != 0) {
				LOG.debug("Precision: " + precision);
			}
			if (scale != 0) {
				LOG.debug("Scale: " + scale);
			}
			if (bNullable) {
				LOG.debug("Column is NULLABLE");
			}
			else {
				LOG.debug("Column is NOT NULLABLE");
			}
		}
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength() {
		return length;
	}

	public void setExpression(boolean value) {
		this.bExpression = value;
	}

	public boolean isExpression() {
		return bExpression;
	}

}	// class Column

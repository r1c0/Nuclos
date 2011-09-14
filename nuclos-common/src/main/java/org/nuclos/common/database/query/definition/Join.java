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

import org.nuclos.common2.KeyEnum;

/**
 * Join for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class Join implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Column srcColumn;
	private final JoinType type;
	private final Column dstColumn;

	public Join(Column srcColumn, JoinType type, Column dstColumn) {
		this.srcColumn = srcColumn;
		this.type = type;
		this.dstColumn = dstColumn;
	}

	public Column getSrcColumn() {
		return srcColumn;
	}

	public JoinType getType() {
		return type;
	}

	public Column getDstColumn() {
		return dstColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dstColumn == null) ? 0 : dstColumn.hashCode());
		result = prime * result + ((srcColumn == null) ? 0 : srcColumn.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Join other = (Join) obj;
		if (dstColumn == null) {
			if (other.dstColumn != null)
				return false;
		} else if (!dstColumn.equals(other.dstColumn))
			return false;
		if (srcColumn == null) {
			if (other.srcColumn != null)
				return false;
		} else if (!srcColumn.equals(other.srcColumn))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public enum JoinType implements KeyEnum<String> {

		INNER_JOIN("InnerJoin", "INNER JOIN"),
		LEFT_OUTER_JOIN("LeftOuterJoin", "LEFT OUTER JOIN"),
		RIGTH_OUTER_JOIN("RightOuterJoin", "RIGHT OUTER JOIN");

		private final String value;
		private final String sql;

		private JoinType(String value, String sql) {
			this.value = value;
			this.sql = sql;
		}

		@Override
		public String getValue() {
			return value;
		}

		public String getSql() {
			return sql;
		}

		public JoinType reverse() {
			if (this.equals(LEFT_OUTER_JOIN)) {
				return RIGTH_OUTER_JOIN;
			}
			else if (this.equals(RIGTH_OUTER_JOIN)) {
				return LEFT_OUTER_JOIN;
			}
			else {
				return this;
			}
		}
	}

}	// class Constraint

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
package org.nuclos.common.database.query.statement;

import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Table;

/**
 * Operand class represents the variable operands that are to be used
 * in predicates column variables.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class Operand { // Not serializable
	private Object o;

	public Operand(Object o) {
		this.o = o;
	}

	public Object getObject() {
		return o;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if (o instanceof Column) {
			final Column column = (Column) o;
			final Table table = column.getTable();
			if (table.getAlias() != null) {
				sb.append(table.getAlias());
				sb.append('.');
			}
			sb.append("\"").append(o.toString()).append("\"");
		}
		else {
			sb.append(o.toString());
		}
		return sb.toString();
	}

}	// class Operand

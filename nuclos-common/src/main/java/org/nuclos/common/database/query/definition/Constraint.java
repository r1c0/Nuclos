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
import java.util.ArrayList;
import java.util.List;


/**
 * Constraint for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class Constraint implements Serializable {

	private final String name;
	private final ConstraintEmumerationType type;
	private final Table table;
	private final List<Column> columns = new ArrayList<Column>();

	public Constraint(Table table, String name, ConstraintEmumerationType type) {
		this.table = table;
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Table getTable() {
		return table;
	}

	public ConstraintEmumerationType getType() {
		return type;
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void show() {
		System.out.println("Constraint: " + name);
		type.show();
		System.out.println("Columns that are part of constraint");
		for (Column column : columns) {
			System.out.println("Column: " + column.getName());
		}
	}

}	// class Constraint

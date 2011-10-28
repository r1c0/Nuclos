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
import java.util.Map;
import java.util.Set;

import org.nuclos.common.collection.CollectionUtils;

/**
 * This class represents a database schema definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00
 */
public class Schema implements Serializable {

	private final Map<String, Table> mpTables = CollectionUtils.newHashMap();

	public Schema() {
	}

	public ArrayList<Table> getTables(Set<String> setQueryTypes) {
		ArrayList<Table> result = new ArrayList<Table>();

		for (Table t : mpTables.values()) {
			if (setQueryTypes != null) {
				if (t.isQuery()) {
					if (setQueryTypes.contains(t.getType())) {
						result.add(t);
					}
				} else {
					result.add(t);
				}
			} else {
				result.add(t);
			}
		}

		return result;
	}

	public Table getTable(String sName) {
		for (String key : mpTables.keySet()) {
			if (key.equalsIgnoreCase(sName)) {
				return mpTables.get(key);
			}
		}
		return null;
	}

	public void addTable(Table table) {
		mpTables.put(table.getName(), table);
	}

	public Constraint getConstraint(String name) {
		for (Table table : mpTables.values()) {
			for (Constraint constraint : table.getConstraints()) {
				if (constraint.getName().equalsIgnoreCase(name)) {
					return constraint;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Schema:\n").append("\n");
		for (Table table : mpTables.values()) {
			sb.append(table.toString());
		}
		return sb.toString();
	}

}	// class Schema

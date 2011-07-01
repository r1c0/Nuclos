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
package org.nuclos.client.datasource.querybuilder.shapes.gui;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Constraint;
import org.nuclos.common.database.query.definition.ConstraintEmumerationType;
import org.nuclos.common.database.query.definition.DataType;
import org.nuclos.common.database.query.definition.Table;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class ConstraintColumn extends Column implements Comparable<ConstraintColumn> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Constraint> constraints = new ArrayList<Constraint>();
	private boolean bMarked = false;

	public ConstraintColumn(Table table, String name, DataType type, int length, int precision, int scale, boolean nullable) {
		super(table, name, type, length, precision, length, nullable);
	}

	public ConstraintColumn(Column column) {
		super(column.getTable(), column.getName(), column.getType(), column.getLength(), column.getScale(), column.getPrecision(), column.isNullable());
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints.addAll(constraints);
	}

	public boolean hasForeignKeyConstraint() {
		return hasConstraint(ConstraintEmumerationType.FOREIGN_KEY);
	}

	public boolean hasPrimaryKeyConstraint() {
		return hasConstraint(ConstraintEmumerationType.PRIMARY_KEY);
	}

	public boolean hasConstraint(ConstraintEmumerationType constraintType) {
		boolean result = false;
		for (Constraint constraint : constraints) {
			if (constraint.getType().equals(constraintType)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean isMarked() {
		return bMarked;
	}

	public void setMark(boolean bMarked) {
		this.bMarked = bMarked;
	}

	public boolean hasReferenceTo(Table t) {
		boolean result = false;
		for (Constraint constraint : constraints) {
			if (constraint.getTable().getName().equals(t.getName())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public Constraint getForeignKeyConstraint() {
		for (Constraint constraint : constraints) {
			if (constraint.getType() == ConstraintEmumerationType.FOREIGN_KEY) {
				return constraint;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof ConstraintColumn)) {
			return false;
		}
		final ConstraintColumn that = (ConstraintColumn) o;
		if (this.getTable() == null || that.getTable() == null) {
			return this.getTable() == that.getTable();
		}
		return this.getName().equals(that.getName()) && this.getTable().getName().equals(that.getTable().getName()) && this.getType().isSameType(that.getType());
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(ConstraintColumn o) {
		return getName().compareTo(o.getName());
	}

}	// class ConstraintColumn

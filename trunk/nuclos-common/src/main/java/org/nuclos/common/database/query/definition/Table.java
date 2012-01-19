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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Table for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:boris.sander@novabit.de">Boris Sander</a>
 * @version 01.00
 */
public class Table implements Serializable {

	private static final Logger LOG = Logger.getLogger(Table.class);
	
	private final Set<Column> stColumns = new HashSet<Column>();
	private final Set<Constraint> stConstraints = new HashSet<Constraint>();

	private Schema schema;
	private String sName;
	private String sViewName;
	private String sComment;
	private String sEntityName;
	private String sAlias;
	private String sType;
	private boolean bQuery;
	private boolean bJoin;
	private String sDatasourceXML;

	/**
	 * @param schema
	 * @param name
	 */
	public Table(Schema schema, String name) {
		setSchema(schema);
		setName(name);
	}

	/**
	 * @param schema
	 * @param name
	 */
	public Table(Schema schema, String name, String sViewName) {
		setSchema(schema);
		setName(name);
		setViewName(sViewName);
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	/**
	 * @return schema
	 */
	public Schema getSchema() {
		return schema;
	}

	/**
	 * @return view name
	 */
	public String getViewName() {
		return sViewName;
	}

	/**
	 * @param sViewName
	 */
	public void setViewName(String sViewName) {
		this.sViewName = sViewName;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return sName;
	}

	/**
	 * @param sName
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * @return type
	 */
	public String getType() {
		return sType;
	}

	/**
	 * @param sType
	 */
	public void setType(String sType) {
		this.sType = sType;
	}

	/**
	 * @return comment
	 */
	public String getComment() {
		return sComment;
	}

	/**
	 * @param comment
	 */
	public void setComment(String comment) {
		this.sComment = comment;
	}

	/**
	 * @return alias
	 */
	public String getAlias() {
		return sAlias;
	}

	/**
	 * @param alias
	 */
	public void setAlias(String alias) {
		this.sAlias = alias;
	}

	/**
	 * @return true if query
	 */
	public boolean isQuery() {
		return bQuery;
	}

	/**
	 *
	 */
	public void setQuery(boolean bValue) {
		bQuery = bValue;
	}

	/**
	 * @return true if join
	 */
	public boolean isJoin() {
		return bJoin;
	}

	/**
	 *
	 */
	public void setJoin(boolean bValue) {
		bJoin = bValue;
	}

	/**
	 *
	 * @param column
	 */
	public void addColumn(Column column) {
		stColumns.add(column);
	}

	/**
	 *
	 * @return Set<Column> of columns
	 */
	public Set<Column> getColumns() {
		return stColumns;
	}

	/**
	 *
	 * @param columnName
	 * @return column with specified name
	 */
	public Column getColumn(String columnName) {
		for (Column column : stColumns) {
			if (column.getName().equalsIgnoreCase(columnName)) {
				column.setTable(this);
				return column;
			}
		}

		return new Column(this, columnName, null, DataType.VARCHAR, 4000, -1, -1, true, true);
	}

	/**
	 *
	 * @param constraint
	 */
	public void addConstraint(Constraint constraint) {
		stConstraints.add(constraint);
	}

	public Set<Constraint> getConstraints() {
		return stConstraints;
	}

	public void show() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Table: " + sName);	
			for (Column column : stColumns) {
				column.show();
			}
			for (Constraint constraint : stConstraints) {
				constraint.show();
			}
		}
	}

	@Override
	public String toString() {
		return sAlias + "." + sName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Table)) {
			return false;
		}

		final Table table = (Table) o;

		if (!sName.equals(table.sName)) {
			return false;
		}
		if (!schema.equals(table.schema)) {
			return false;
		}
		if (sAlias != null ? !sAlias.equals(table.sAlias) : table.sAlias != null) {
			return false;
		}

		return true;
	}

	/**
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		int result = schema.hashCode();
		result = 29 * result + sName.hashCode();
		result = 29 * result + (sAlias != null ? sAlias.hashCode() : 0);
		return result;
	}

	/**
	 *
	 * @return cloned object
	 */
	@Override
	public Object clone() {
		final Table result = new Table(getSchema(), getName());
		result.setAlias(getAlias());
		result.setComment(getComment());
		result.setQuery(isQuery());
		result.setDatasourceXML(sDatasourceXML);
		result.getColumns().addAll(getColumns());
		for (Column column : result.getColumns()) {
			column.setTable(result);
		}
		result.getConstraints().addAll(getConstraints());
		result.setViewName(getViewName());
		return result;
	}

	/**
	 * @return Returns the sDatasourceXML.
	 */
	public String getDatasourceXML() {
		return sDatasourceXML;
	}

	/**
	 * @param sDatasourceXML The sDatasourceXML to set.
	 */
	public void setDatasourceXML(String sDatasourceXML) {
		this.sDatasourceXML = sDatasourceXML;
	}
	
	public String getEntityName() {
		return sEntityName;
	}

	public void setEntityName(String sEntityName) {
		this.sEntityName = sEntityName;
	}

}	// class Table

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
package org.nuclos.server.dblayer.util;

import static org.nuclos.common2.StringUtils.defaultIfNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbIdent;
import org.nuclos.server.dblayer.expression.DbCurrentDate;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbCallableType;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbReference;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;
import org.nuclos.server.dblayer.structure.DbTableData;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;

public class DbArtifactXmlReader {
	
	private final List<DbArtifact> artifacts;
	private final List<DbTableData> dumps;
	private XMLStreamReader reader;
	private String tableName;

	public DbArtifactXmlReader() {
		this.artifacts = new ArrayList<DbArtifact>();
		this.dumps = new ArrayList<DbTableData>();
	}
	
	public void read(InputStream is) throws IOException {
		try {
			if (!(is instanceof BufferedInputStream)) {
				is = new BufferedInputStream(is);
			}
			reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
			reader.nextTag();
			checkStartElement(reader, "database");
			artifacts.addAll(readArtifacts(DbArtifact.class, null));
			checkEndElement(reader, "database");
			reader.close();
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		} finally {
			reader = null;
			tableName = null;
			is.close();
		}
	}
	
	public List<DbArtifact> getArtifacts() {
		return artifacts;
	}
	
	public List<DbTableData> getDumps() {
		return dumps;
	}
	
	private <T extends DbArtifact> List<T> readArtifacts(Class<T> clazz, Map<String, String> hints) throws XMLStreamException {
		List<T> artifacts = new ArrayList<T>();
		while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
			// Interning the string so that == works
			String localName = reader.getLocalName().intern();
			Location location = reader.getLocation();
			DbArtifact artifact;
			if (localName == "table") {
				artifact = readTable();
			} else if (localName == "column") {
				artifact = readColumn();
			} else if (localName == "primarykey") {
				artifact = readPrimaryKey();
			} else if (localName == "foreignkey") {
				artifact = readForeignKey();
			} else if (localName == "unique") {
				artifact = readUniqueConstraint();
			} else if (localName == "index") {
				artifact = readIndex();
			} else if (localName == "simpleview") {
				artifact = readSimpleView();
			} else if (localName == "sequence") {
				artifact = readSequence();
			} else if (localName == "function") {
				artifact = readCallable(DbCallableType.FUNCTION, "function");
			} else if (localName == "procedure") {
				artifact = readCallable(DbCallableType.PROCEDURE, "procedure");
			} else if (localName == "hint" && hints != null) {
				hints.put(reader.getAttributeValue(null, "name"), reader.getAttributeValue(null, "value"));
				reader.nextTag();
				checkEndElement(reader, "hint");
				continue;
			} else if (localName == "data") {
				// Dumps are added directly
				DbTableData dump = readData();
				dumps.add(dump);
				continue;
			} else {
				throw new XMLStreamException("Unsupported artifact type " + localName, location);
			}
			if (!clazz.isInstance(artifact)) {
				throw new XMLStreamException("Artifact type " + localName + " not allowed at this location", location);
			}
			artifacts.add(clazz.cast(artifact));
		}
		return artifacts;
	}
	
	private DbTable readTable() throws XMLStreamException {
		checkStartElement(reader, "table");
		String name = reader.getAttributeValue(null, "name");		
		this.tableName = name; 
		Map<String, String> hints = readHints();
		List<DbTableArtifact> tableArtifacts = readArtifacts(DbTableArtifact.class, hints);
		this.tableName = null;
		checkEndElement(reader, "table");
		return augmentWithHints(new DbTable(name, tableArtifacts), hints);
	}

	private DbColumn readColumn() throws XMLStreamException {
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		
		DbColumnType type = readColumnType();
		DbNullable nullable = Boolean.parseBoolean(reader.getAttributeValue(null, "nullable")) ? DbNullable.NULL : DbNullable.NOT_NULL;
		
		reader.nextTag();
		Map<String, String> hints = readHints();
		checkEndElement(reader, "column");
		return augmentWithHints(new DbColumn(tableName, name, type, nullable, null), hints);
	}

	private DbPrimaryKeyConstraint readPrimaryKey() throws XMLStreamException {
		checkStartElement(reader, "primarykey");
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		List<String> columns = readColumnGroup();
		Map<String, String> hints = readHints();
		checkEndElement(reader, "primarykey");
		return augmentWithHints(new DbPrimaryKeyConstraint(tableName, name, columns), hints);
	}
	
	private DbForeignKeyConstraint readForeignKey() throws XMLStreamException {
		checkStartElement(reader, "foreignkey");
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		String foreignTableName = reader.getAttributeValue(null, "foreigntable");
		String onDeleteRule = reader.getAttributeValue(null, "ondelete");
		Pair<List<String>, List<String>> references = CollectionUtils.unzip(readReferences());
		boolean onDeleteCascade = false;
		if (onDeleteRule != null) {
			if ("cascade".equals(onDeleteRule)) {
				onDeleteCascade = true;
			} else {
				throw new XMLStreamException("Invalid ondelete attribute value " + onDeleteRule, reader.getLocation());
			}
		}
		Map<String, String> hints = readHints();
		checkEndElement(reader, "foreignkey");
		return augmentWithHints(new DbForeignKeyConstraint(tableName, name, references.x, foreignTableName, null, references.y, onDeleteCascade), hints);
	}
	
	private DbUniqueConstraint readUniqueConstraint() throws XMLStreamException {
		checkStartElement(reader, "unique");
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		List<String> columns = readColumnGroup();
		Map<String, String> hints = readHints();
		checkEndElement(reader, "unique");
		return augmentWithHints(new DbUniqueConstraint(tableName, name, columns), hints);
	}

	private DbIndex readIndex() throws XMLStreamException {
		checkStartElement(reader, "index");
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		List<String> columns = readColumnGroup();
		Map<String, String> hints = readHints();
		checkEndElement(reader, "index");
		return augmentWithHints(new DbIndex(tableName, name, columns), hints);
	}
	
	private DbSimpleView readSimpleView() throws XMLStreamException {
		checkStartElement(reader, "simpleview");
		String tableName = getTableNameAttrOrInherit();		
		String name = reader.getAttributeValue(null, "name");
		List<DbSimpleViewColumn> viewColumns = new ArrayList<DbSimpleViewColumn>();
		while (nextTagIsElement("column", "joincolumn")) {
			String elementName = reader.getLocalName();
			String columnName = reader.getAttributeValue(null, "name");
			DbColumnType columnType = readColumnType();
			if ("column".equals(elementName)) {
				viewColumns.add(new DbSimpleViewColumn(columnName));
			} else if ("joincolumn".equals(elementName)) {
				String foreignTableName = reader.getAttributeValue(null, "foreigntable");
				DbReference reference = new DbReferenceImpl(tableName, foreignTableName, readReferences());
				List<Object> pattern = new ArrayList<Object>();
				checkStartElement(reader, "pattern");
				while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
					String elementName2 = reader.getLocalName();
					if ("text".equals(elementName2)) {
						pattern.add(reader.getElementText());
					} else if ("foreigncolumn".equals(elementName2)) {
						pattern.add(DbIdent.makeIdent(reader.getAttributeValue(null, "name")));
						reader.nextTag();
					} else {
						throw new XMLStreamException("Invalid view pattern element " + elementName2, reader.getLocation());
					}
					checkEndElement(reader, elementName2);
				}
				checkEndElement(reader, "pattern");
				viewColumns.add(new DbSimpleViewColumn(columnName, columnType, reference, pattern));
			} else if ("calccolumn".equals(elementName)) {
				String functionName = reader.getAttributeValue(null, "function");
				List<String> args = new ArrayList<String>();
				checkStartElement(reader, "arguments");
				while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
					String elementName2 = reader.getLocalName();
					if ("column".equals(elementName2)) {
						args.add(reader.getAttributeValue(null, "name"));
					} else {
						throw new XMLStreamException("Invalid argument element " + elementName2, reader.getLocation());
					}
					reader.nextTag();
					checkEndElement(reader, elementName2);
				}
				checkEndElement(reader, "arguments");
				viewColumns.add(new DbSimpleViewColumn(columnName, columnType, functionName, args.toArray(new String[args.size()])));				
			}
			reader.nextTag();
			checkEndElement(reader, elementName);
		}
		Map<String, String> hints = readHints();
		checkEndElement(reader, "simpleview");
		return augmentWithHints(new DbSimpleView(tableName, name, viewColumns), hints);
	}
	
	private DbSequence readSequence() throws XMLStreamException {
		checkStartElement(reader, "sequence");
		String name = reader.getAttributeValue(null, "name");
		Long startWith = Long.parseLong(defaultIfNull(reader.getAttributeValue(null, "startwith"), "1"));
		reader.nextTag();
		Map<String, String> hints = readHints();
		checkEndElement(reader, "sequence");
		return augmentWithHints(new DbSequence(name, startWith), hints);
	}
	
	private DbCallable readCallable(DbCallableType type, String elementName) throws XMLStreamException {
		checkStartElement(reader, elementName);
		String name = reader.getAttributeValue(null, "name");
		String code = null;
		if (nextTagIsElement("code")) {
			code = reader.getElementText();
			reader.nextTag();
		}
		Map<String, String> hints = readHints();
		checkEndElement(reader, elementName);
		return augmentWithHints(new DbCallable(type, name, code), hints);
	}

	private DbColumnType readColumnType() throws XMLStreamException {
		String genericTypeName = reader.getAttributeValue(null, "type");
		DbGenericType genericType = (genericTypeName != null) ? DbGenericType.valueOf(genericTypeName) : null;
		String typeName = reader.getAttributeValue(null, "typename");
		if (genericType == null && typeName == null)
			return null;
		Integer length = StringUtils.parseInt(reader.getAttributeValue(null, "length"), null);
		Integer precision = StringUtils.parseInt(reader.getAttributeValue(null, "precision"), null);
		Integer scale = StringUtils.parseInt(reader.getAttributeValue(null, "scale"), null);
		return new DbColumnType(genericType, typeName, length, precision, scale);
	}
	
	private List<String> readColumnGroup() throws XMLStreamException {
		List<String> columns = new ArrayList<String>();
		while (nextTagIsElement("column")) {
			checkName(reader, "column");
			columns.add(reader.getAttributeValue(null, "name"));
			reader.nextTag();
			checkEndElement(reader, "column");
		}
		if (columns.isEmpty()) {
			throw new XMLStreamException("Empty column list", reader.getLocation());
		}
		return columns;
	}
	
	private List<Pair<String, String>> readReferences() throws XMLStreamException {
		List<Pair<String, String>> references = new ArrayList<Pair<String, String>>();
		while (nextTagIsElement("reference")) {
			checkName(reader, "reference");
			references.add(Pair.makePair(reader.getAttributeValue(null, "column"), reader.getAttributeValue(null, "foreigncolumn")));
			reader.nextTag();
			checkEndElement(reader, "reference");
		}
		if (references.isEmpty()) {
			throw new XMLStreamException("Empty reference list", reader.getLocation());
		}
		return references;
	}
	
	private Map<String, String> readHints() throws XMLStreamException {
		Map<String, String> hints = null;	
		while (reader.getEventType() == XMLStreamReader.START_ELEMENT && "hint".equals(reader.getLocalName())) {
			if (hints == null)
				 hints = new LinkedHashMap<String, String>();
			hints.put(reader.getAttributeValue(null, "name"), reader.getAttributeValue(null, "value"));
			reader.nextTag();
			checkEndElement(reader, "hint");
			reader.nextTag();
		}
		return hints;
	}
	
	private <T extends DbArtifact> T augmentWithHints(T artifact, Map<String, String> hints) {
		if (hints != null) {
			for (Map.Entry<String, String> e : hints.entrySet()) {
				artifact.setHint(e.getKey(), e.getValue());
			}
		}
		return artifact;
	}	
	
	private DbTableData readData() throws XMLStreamException {
		checkStartElement(reader, "data");
		String tableName = getTableNameAttrOrInherit();		
		reader.nextTag();
		List<Pair<String, DbGenericType>> columns = readDataColumns();
		List<List<Object>> data = new ArrayList<List<Object>>();
		while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
			data.add(readDataRow(columns));
		}
		checkEndElement(reader, "data");
		return new DbTableData(tableName, columns, data);
	}
	
	private List<Pair<String, DbGenericType>> readDataColumns() throws XMLStreamException {
		checkName(reader, "columns");
		List<Pair<String, DbGenericType>> columns = new ArrayList<Pair<String, DbGenericType>>();
		while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
			checkName(reader, "column");
			String name = reader.getAttributeValue(null, "name"); 
			DbGenericType type = DbGenericType.valueOf(StringUtils.toUpperCase(reader.getAttributeValue(null, "type"))); 
			columns.add(Pair.makePair(name, type));
			reader.nextTag();
			checkEndElement(reader, "column");
		}		
		checkEndElement(reader, "columns");
		return columns;
	}
	
	private List<Object> readDataRow(List<Pair<String, DbGenericType>> columns) throws XMLStreamException {
		checkName(reader, "row");
		List<Object> row = new ArrayList<Object>();
		for (Pair<String, DbGenericType> column : columns) {
			if (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
				DbGenericType type = column.y;
				String localName = reader.getLocalName();
				if ("null".equals(localName)) {
					row.add(DbNull.forType(type));
					reader.nextTag();
				} else if ("currentDate".equals(localName) && type == DbGenericType.DATE) {
					row.add(DbCurrentDate.CURRENT_DATE);
					reader.next();
				} else if ("currentDateTime".equals(localName) && type == DbGenericType.DATETIME) {
					row.add(DbCurrentDateTime.CURRENT_DATETIME);
					reader.next();
				} else if ("value".equals(localName)) {
					row.add(type.decodeFromString(reader.getElementText()));
				} else {
					throw new XMLStreamException("Unexpected data element " + localName, reader.getLocation());
				}
				checkEndElement(reader, localName);
			} else {
				throw new XMLStreamException("Missing data element for column " + column.x, reader.getLocation());
			}
		}
		reader.nextTag();
		checkEndElement(reader, "row");
		return row;
	}

	private boolean nextTagIsElement(String...names) throws XMLStreamException {
		reader.nextTag();
		return (reader.getEventType() == XMLStreamReader.START_ELEMENT) 
			&& Arrays.asList(names).contains(reader.getLocalName());
	}

	
	private String getTableNameAttrOrInherit() throws XMLStreamException {
		String tableName = reader.getAttributeValue(null, "table");
		return tableName != null ? tableName : this.tableName;
	}
	
	private static void checkName(XMLStreamReader reader, String name) throws XMLStreamException {
		if (!name.equals(reader.getLocalName()))
			throw new XMLStreamException("Element " + name + " expected ", reader.getLocation());
	}

	private static void checkStartElement(XMLStreamReader reader, String name) throws XMLStreamException {
		reader.require(XMLStreamReader.START_ELEMENT, null, name);
	}
	
	private static void checkEndElement(XMLStreamReader reader, String name) throws XMLStreamException {
		reader.require(XMLStreamReader.END_ELEMENT, null, name);
	}
	
	private static class DbReferenceImpl implements DbReference, Serializable {

		private List<Pair<String, String>> references;
		private String	tableName;
		private String	foreignTableName;

		public DbReferenceImpl(String table, String foreignTable, List<Pair<String, String>> references) {
			this.tableName = table;
			this.foreignTableName = foreignTable;
			this.references = references;
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append(getClass().getName()).append("[");
			result.append("table=").append(tableName);
			result.append(", refTable=").append(foreignTableName);
			result.append(", refs=").append(references);
			result.append("]");
			return result.toString();
		}

		@Override
		public String getTableName() {
			return tableName;
		}
		
		@Override
		public List<String> getColumnNames() {
			return CollectionUtils.unzip(references).x;
		}

		@Override
		public String getReferencedTableName() {
			return foreignTableName;
		}
		
		@Override
		public List<String> getReferencedColumnNames() {
			return CollectionUtils.unzip(references).y;
		}

		@Override
		public List<Pair<String, String>> getReferences() {
			return references;
		}
	}
}

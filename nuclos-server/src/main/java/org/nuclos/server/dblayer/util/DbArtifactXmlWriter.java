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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbIdent;
import org.nuclos.server.dblayer.expression.DbCurrentDate;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbArtifactVisitor;
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
import org.nuclos.server.dblayer.structure.DbTableColumnGroup;
import org.nuclos.server.dblayer.structure.DbTableData;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;

public class DbArtifactXmlWriter implements Closeable {
	
	private XMLStreamWriter writer;

	private final static String VERSION = "0.9";
	
	public DbArtifactXmlWriter(OutputStream os) throws IOException {
		try {
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(os, "utf-8"));
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("database");
			writer.writeAttribute("version", VERSION);
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	public void writeArtifacts(Collection<? extends DbArtifact> artifacts) throws IOException {
		try {
			DbArtifact.acceptAll(artifacts, new WriteVisitor());
		} catch(NuclosFatalException ex) {
			throw new IOException(ex);
		}
	}
	
	public void writeDumps(Collection<? extends DbTableData> dumps) throws IOException {
		for (DbTableData dump : dumps) {
			writeDump(dump);
		}
	}
	
	public void writeDump(DbTableData dump) {
		writeStart("data");
		writeAttribute("table", dump.getTableName());
		writeStart("columns");
		List<Pair<String, DbGenericType>> columns = dump.getColumns();
		for (Pair<String, DbGenericType> column : columns) {
			writeStart("column");
			writeAttribute("name", column.x);
			writeAttribute("type", column.y.name());
			writeEnd();
		}
		writeEnd();
		for (List<Object> row : dump.getData()) {
			writeStart("row");
			for (int i = 0; i < columns.size(); i++) {
				Object obj = row.get(i);
				if (obj == null || obj instanceof DbNull<?>) {
					writeEmpty("null");
				} else if (obj instanceof DbCurrentDate) {
					writeEmpty("currentDate");
				} else if (obj instanceof DbCurrentDateTime) {
					writeEmpty("currentDateTime");
				} else {
					writeStart("value");
					writeText(columns.get(i).y.encodeAsString(obj));
					writeEnd();
				}
			}
			writeEnd();
		}
		writeEnd();
	}
	
	@Override
	public void close() throws IOException {
		if (writer != null) {
			try {
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.close();
			} catch (XMLStreamException ex) {
				throw new IOException(ex);
			}
			writer = null;
		}
	};
	
	private void writeStart(String name) throws DbException {
		try {
			writer.writeStartElement(name);
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}

	private void writeEnd() throws DbException {
		try {
			writer.writeEndElement();
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}

	private void writeEmpty(String name) throws DbException {
		try {
			writer.writeEmptyElement(name);
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	private void writeAttribute(String name, Object value) throws DbException {
		try {
			if (value != null)
				writer.writeAttribute(name, value.toString());
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}

	private void writeCData(String text) throws DbException {
		try {
			if (text != null)
				writer.writeCData(text);
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}		
	
	private void writeText(String text) throws DbException {
		try {
			if (text != null)
				writer.writeCharacters(text);
		} catch(XMLStreamException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	private class WriteVisitor implements DbArtifactVisitor<Void> {

		private String tableName = "";
		
		@Override
		public Void visitTable(DbTable table) throws DbException {
			writeStart("table");
			writeAttribute("name", table.getTableName());
			this.tableName = table.getTableName();
			writeHints(table);
			DbArtifact.acceptAll(table.getTableArtifacts(), this);
			this.tableName = "";
			writeEnd();
			return null;
		}
		
		@Override
		public Void visitColumn(DbColumn column) throws DbException {
			writeStart("column");
			writeAttribute("name", column.getColumnName());
			writeTableNameAttrIfNeeded(column.getTableName());
			writeColumnType(column.getColumnType());
			if (column.getNullable() != null)
				writeAttribute("nullable", column.getNullable() == DbNullable.NULL);
			writeHints(column);
			writeEnd();
			return null;
		}

		@Override
		public Void visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) throws DbException {
			writeSimpleColumnGroup("primarykey", constraint);
			return null;
		}
		
		@Override
		public Void visitForeignKeyConstraint(DbForeignKeyConstraint constraint) throws DbException {
			writeStart("foreignkey");
			writeAttribute("name", constraint.getConstraintName());
			writeTableNameAttrIfNeeded(constraint.getTableName());
			writeAttribute("foreigntable", constraint.getReferencedTableName());
			if (constraint.isOnDeleteCascade()) {
				writeAttribute("ondelete", "cascade");
			}
			for (Pair<String, String> p : constraint.getReferences()) {
				writeEmpty("reference");
				writeAttribute("column", p.x);
				writeAttribute("foreigncolumn", p.y);
			}
			writeHints(constraint);
			writeEnd();
			return null;
		}

		@Override
		public Void visitUniqueConstraint(DbUniqueConstraint constraint) throws DbException {
			writeSimpleColumnGroup("unique", constraint);
			return null;
		}
		
		@Override
		public Void visitIndex(DbIndex index) throws DbException {
			writeSimpleColumnGroup("index", index);
			return null;
		}


		@Override
		public Void visitSequence(DbSequence sequence) throws DbException {
			writeStart("sequence");
			writeAttribute("name", sequence.getSimpleName());
			writeAttribute("startwith", sequence.getStartWith());
			writeHints(sequence);
			writeEnd();
			return null;
		}

		@Override
		public Void visitView(DbSimpleView view) throws DbException {
			writeStart("simpleview");
			writeAttribute("name", view.getViewName());
			writeTableNameAttrIfNeeded(view.getTableName());
			for (DbSimpleViewColumn viewColumn : view.getViewColumns()) {
				switch (viewColumn.getViewColumnType()) {
				case TABLE:
					writeStart("column");
					writeAttribute("name", viewColumn.getColumnName());
					writeColumnType(viewColumn.getColumnType());
					writeEnd();
					break;
				case FOREIGN_REFERENCE:
					DbReference reference = viewColumn.getReference();
					writeStart("joincolumn");
					writeAttribute("name", viewColumn.getColumnName());
					writeColumnType(viewColumn.getColumnType());
					writeAttribute("foreigntable", reference.getReferencedTableName());
					for (Pair<String, String> p : reference.getReferences()) {
						writeEmpty("reference");
						writeAttribute("column", p.x);
						writeAttribute("foreigncolumn", p.y);
					}
					writeStart("pattern");
					for (Object obj : viewColumn.getViewPattern()) {
						if (obj instanceof String) {
							writeStart("text");
							writeText(obj.toString());
							writeEnd();
						} else if (obj instanceof DbIdent) {
							writeEmpty("foreigncolumn");
							writeAttribute("name", ((DbIdent) obj).getName());
						} else {
							throw new ClassCastException();
						}
					}
					writeEnd();
					writeEnd();
					break;
				case FUNCTION:
					writeStart("calccolumn");
					writeAttribute("name", viewColumn.getColumnName());
					writeColumnType(viewColumn.getColumnType());
					writeAttribute("function", viewColumn.getFunctionName());
					writeStart("arguments");
					for (String arg : viewColumn.getArgColumns()) {
						writeEmpty("column");
						writeAttribute("name", arg);
					}					
					writeEnd();
					writeEnd();
				}
			}
			writeHints(view);
			writeEnd();
			return null;
		}
				
		@Override
		public Void visitCallable(DbCallable callable) throws DbException {
			writeStart(callable.getType() == DbCallableType.FUNCTION ? "function" : "procedure");
			writeAttribute("name", callable.getCallableName());			
			writeHints(callable);
			writeStart("code");
			writeCData(callable.getCode());
			writeEnd();
			writeEnd();
			return null;
		}
		
		private <T extends DbTableArtifact & DbTableColumnGroup> void writeSimpleColumnGroup(String element, T constraint) {
			writeStart(element);
			writeAttribute("name", constraint.getSimpleName());
			writeTableNameAttrIfNeeded(constraint.getTableName());
			for (String column : constraint.getColumnNames()) {
				writeEmpty("column");
				writeAttribute("name", column);
			}
			writeHints(constraint);
			writeEnd();
		}
		
		private void writeTableNameAttrIfNeeded(String tableName) {
			if (!this.tableName.equals(tableName))
				writeAttribute("table", tableName);		
		}
		
		private void writeColumnType(DbColumnType columnType) {
			if (columnType != null) {
				writeAttribute("type", columnType.getGenericType());
				writeAttribute("typename", columnType.getTypeName());
				writeAttribute("length", columnType.getLength());
				writeAttribute("precision", columnType.getPrecision());
				writeAttribute("scale", columnType.getScale());
			}
		}
		
		private void writeHints(DbArtifact artifact) {
			Map<String, String> hints = artifact.getHints();
			if (hints != null) {
				for (Map.Entry<String, String> e : hints.entrySet()) {
					writeStart("hint");
					writeAttribute("name", e.getKey());
					writeAttribute("value", e.getValue());
					writeEnd();
				}
			}
		}
	}
}

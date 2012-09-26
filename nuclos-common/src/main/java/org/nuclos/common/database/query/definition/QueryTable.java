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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Query table for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class QueryTable {
	protected static final String TAG_TABLE = "table";
	protected static final String TAG_TABLES = "tables";
	protected static final String TAG_COLUMN = "column";
	protected static final String TAG_COLUMNS = "columns";
	protected static final String TAG_CONDITION = "condition";
	protected static final String TAG_CONNECTOR = "connector";
	protected static final String TAG_CONNECTORS = "connectors";
	
	protected static final String SYSTEMID = "http://www.novabit.de/technologies/querybuilder/querybuildermodel.dtd";
	protected static final String RESOURCE_PATH = "org/nuclos/common/querybuilder/querybuildermodel.dtd";
	protected static final EntityResolver RESOLVER = XMLUtils.newClasspathEntityResolver(SYSTEMID, RESOURCE_PATH, true);
	
	protected static final String DOCTYPE = "querybuildermodel";

	public static final String QUERY_TYPE_REPORT = 					"DS_REPORT";
	public static final String QUERY_TYPE_DYNAMIC_ENTITY = 		"DS_DYNENTITY";
	public static final String QUERY_TYPE_VALUELIST_PROVIDER = 	"DS_VLPROVIDER";
	public static final String QUERY_TYPE_RECORDGRANT = 	"DS_VRECGRANT";
	public static final String QUERY_TYPE_CHART = 					"DS_CHART";
	public static final String QUERY_TYPE_DYNAMIC_TASK = 		"DS_DYNTASK";

	final class XMLTable {
		String sId;
		String sEntity;

		protected XMLTable(String sId, String sEntity) {
			this.sId = sId;
			this.sEntity = sEntity;
		}
	}

	final class XMLColumn {
		String sTable;
		String sColumn;
		String sAlias;
		boolean bVisible;
		String sGroup;
		String sSort;
		ArrayList<XMLCondition> lstConditions = new ArrayList<XMLCondition>();

		protected XMLColumn(String sTable, String sColumn, String sAlias, boolean bVisible, String sGroup, String sSort) {
			this.sTable = sTable;
			this.sColumn = sColumn;
			this.sAlias = sAlias;
			this.bVisible = bVisible;
			this.sGroup = sGroup;
			this.sSort = sSort;
		}

		protected void addCondition(XMLCondition condition) {
			lstConditions.add(condition);
		}
	}

	final class XMLCondition {
		String sCondition;

		protected XMLCondition(String sCondition) {
			this.sCondition = sCondition;
		}
	}

	final class XMLConnector {
		String sSrcTable;
		String sSrcColumn;
		String sSrcCardinality;
		String sDstTable;
		String sDstColumn;
		String sDstCardinality;
		String sJoinType;

		protected XMLConnector(String sSrcTable, String sSrcColumn, String sSrcCardinality, String sDstTable, String sDstColumn, String sDstCardinality, String sJoinType) {
			this.sSrcTable = sSrcTable;
			this.sSrcColumn = sSrcColumn;
			this.sSrcCardinality = sSrcCardinality;
			this.sDstTable = sDstTable;
			this.sDstColumn = sDstColumn;
			this.sDstCardinality = sDstCardinality;
			this.sJoinType = sJoinType;
		}
	}
	
	private class XMLHandler implements ContentHandler {
		
		private final List<Column> lstColumns;
		
		private Column currentColumn;
		
		private XMLHandler(List<Column> lstColumns) {
			this.lstColumns = lstColumns;
		}

		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
			if (qName.equals(TAG_COLUMN)) {
				atts.getValue("table");
				atts.getValue("column");
				final String sAlias = atts.getValue("alias");
				final boolean bVisible = new Boolean(atts.getValue("visible").equals("yes")).booleanValue();
				atts.getValue("group");
				atts.getValue("sort");
				final String sType = atts.getValue("type");
				final String sLength = atts.getValue("length");
				final boolean bNullable = new Boolean(atts.getValue("nullable").equals("yes")).booleanValue();
				final String sPrecision = atts.getValue("precision");
				final String sScale = atts.getValue("scale");
				// Legacy database support used specific (sub)types for MSSQL and Oracle which were
				// - MSSQL: VARCHAR (1), NUMERIC (2), INTEGER (3), VARBINARY (4), DATETIME (7)
				// - Oracle: VARCHAR (1), NUMBER (2), INTEGER (3), BLOB (4), CLOB (5), LONG_RAW (6), DATE (7), TIMESTAMP (8), VARCHAR2 (9)
				DataType dataType = DataType.findByName(sType, true);
				if (bVisible) {
					currentColumn = new Column(null, sAlias, dataType,
						Integer.parseInt(sLength), Integer.parseInt(sPrecision), Integer.parseInt(sScale),
						bNullable);

					lstColumns.add(currentColumn);
				}
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) {
		}

		@Override
		public void ignorableWhitespace(char ch[], int start, int length) {
		}

		@Override
		public void processingInstruction(String target, String data) {
		}

		@Override
		public void characters(char ch[], int start, int length) {
		}

		@Override
		public void setDocumentLocator(Locator locator) {
		}

		@Override
		public void skippedEntity(String name) {
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) {
		}

		@Override
		public void endPrefixMapping(String prefix) {
		}

		@Override
		public void startDocument() {
		}

		@Override
		public void endDocument() {
		}
		
	}

	public void setAllQueryColumns(Table table) {
		final String sDatasourceXML = table.getDatasourceXML();
		final List<Column> lstColumns = new ArrayList<Column>();
		final XMLHandler xmlHandler = new XMLHandler(lstColumns);
		try {
			XMLUtils.parse(sDatasourceXML, xmlHandler, null, RESOLVER, false);
			for (Iterator<Column> i = lstColumns.iterator(); i.hasNext();) {
				Column column = i.next();
				column.setTable(table);
				table.addColumn(column);
			}
		}
		catch (SAXException e) {
			throw new NuclosFatalException(e);
		}
	}
}

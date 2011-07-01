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
/**
 *
 */
package org.nuclos.common.querybuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceParameterValuelistproviderVO;

/**
 * Datasource xml parser to parse a xml representation of a datasource.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:xxx.xxx@novabit.de">xxx xxx</a>
 * @version 00.01.000
 */
public class DatasourceXMLParser {
	private static final String SYSTEMID = "http://www.novabit.de/technologies/querybuilder/querybuildermodel.dtd";
	private static final String RESOURCE_PATH = "org/nuclos/common/querybuilder/querybuildermodel.dtd";

	private static final String TAG_ENTITYOPTIONS = "entityoptions";
	private static final String TAG_TABLE = "table";
	private static final String TAG_COLUMN = "column";
	private static final String TAG_CONDITION = "condition";
	private static final String TAG_CONNECTOR = "connector";
	private static final String TAG_PARAMETER = "parameter";
	private static final String TAG_SQL = "sql";
	private static final String TAG_VALUELISTPROVIDER = "valuelistprovider";
	private static final String TAG_VALUELISTPROVIDER_PARAMETER = "vlpparameter";

	public static class Result {
		final Map<String, Table> mapTables = new HashMap<String, Table>();
		final Map<String, DatasourceXMLParser.XMLTable> mapXMLTables = new HashMap<String, DatasourceXMLParser.XMLTable>();
		final List<DatasourceXMLParser.XMLColumn> lstColumns = new ArrayList<DatasourceXMLParser.XMLColumn>();
		final List<DatasourceXMLParser.XMLConnector> lstConnectors = new ArrayList<DatasourceXMLParser.XMLConnector>();
		final List<DatasourceParameterVO> lstParameters = new ArrayList<DatasourceParameterVO>();
		boolean bIsModelUsed = true;
		boolean bIsEntityOptionDynamic = false;
		String sQueryStringFromXml = null;

		public boolean isModelUsed() {
			return bIsModelUsed;
		}
		public boolean isEntityOptionDynamic() {
			return bIsEntityOptionDynamic;
		}
		public List<DatasourceXMLParser.XMLColumn> getLstColumns() {
			return lstColumns;
		}
		public List<DatasourceXMLParser.XMLConnector> getLstConnectors() {
			return lstConnectors;
		}
		public List<DatasourceParameterVO> getLstParameters() {
			return lstParameters;
		}
		public Map<String, Table> getMapTables() {
			return mapTables;
		}
		public Map<String, DatasourceXMLParser.XMLTable> getMapXMLTables() {
			return mapXMLTables;
		}
		public String getQueryStringFromXml() {
			return sQueryStringFromXml;
		}
	}	// inner class Result

	public static DatasourceXMLParser.Result parse(String sDatasourceXML) throws NuclosDatasourceException {
		final DatasourceXMLParser.Result result = new Result();

		final XMLReader parser = XMLUtils.newSAXParser();
		final DatasourceXMLParser.XMLContentHandler xmlContentHandler = new XMLContentHandler(result);
		try {
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", xmlContentHandler);
			parser.setContentHandler(xmlContentHandler);

			parser.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws IOException {
					InputSource result = null;
					if (systemId.equals(SYSTEMID)) {
						final URL url = Thread.currentThread().getContextClassLoader().getResource(RESOURCE_PATH);
						if (url == null) {
							throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("querytable.missing.dtd.error", SYSTEMID));
								//"DTD f\u00fcr " + SYSTEMID + " kann nicht gefunden werden.");
						}
						result = new InputSource(new BufferedInputStream(url.openStream()));
					}
					return result;
				}
			});

			result.bIsModelUsed = true;

			parser.parse(new InputSource(new StringReader(sDatasourceXML)));

		}
		catch (IOException e) {
			throw new NuclosFatalException(e);
		}
		catch (SAXException e) {
			throw new NuclosDatasourceException("datasourcexmlparser.invalid.datasource", e);//"Die Datenquelle ist fehlerhaft."
		}
		return result;
	}

	static class XMLContentHandler implements ContentHandler, LexicalHandler {
		DatasourceXMLParser.XMLColumn currentColumn;
		int iPosition = 0;
		boolean isCDATA = false;
		StringBuffer sbCDATA;
		DatasourceXMLParser.Result parseresult;
		DatasourceParameterVO parameter;

		XMLContentHandler(DatasourceXMLParser.Result parseresult) {
			this.parseresult = parseresult;
		}

		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
			if (qName.equals(TAG_ENTITYOPTIONS)) {
				final String sDynamic = atts.getValue("dynamic");
				this.parseresult.bIsEntityOptionDynamic = "yes".equals(sDynamic);
			}
			else if (qName.equals(TAG_TABLE)) {
				final String sId = atts.getValue("id");
				final String sEntity = atts.getValue("entity");
				final DatasourceXMLParser.XMLTable xmlTable = new DatasourceXMLParser.XMLTable(sId, sEntity);
				parseresult.mapXMLTables.put(sId, xmlTable);
			}
			else if (qName.equals(TAG_COLUMN)) {
				final String sTable = atts.getValue("table");
				final String sColumn = atts.getValue("column");
				final String sAlias = atts.getValue("alias");
				final boolean bVisible = atts.getValue("visible").equals("yes");
				final String sGroup = atts.getValue("group");
				final String sSort = atts.getValue("sort");
				currentColumn = new DatasourceXMLParser.XMLColumn(sTable, sColumn, sAlias, bVisible, sGroup, sSort);
				parseresult.lstColumns.add(currentColumn);
			}
			else if (qName.equals(TAG_CONNECTOR)) {
				final String sSrcTableId = atts.getValue("srctableid");
				final String sSrcColumn = atts.getValue("srccolumn");
				final String sDstTableId = atts.getValue("dsttableid");
				final String sDstColumn = atts.getValue("dstcolumn");
				final String sJoinType = atts.getValue("jointype");
				parseresult.lstConnectors.add(new DatasourceXMLParser.XMLConnector(sSrcTableId, sSrcColumn, sDstTableId, sDstColumn, sJoinType));
			}
			else if (qName.equals(TAG_CONDITION)) {
				final String sCondition = atts.getValue("text");
				currentColumn.lstConditions.add(new DatasourceXMLParser.XMLCondition(sCondition));
			}
			// process parameters
			else if (qName.equals(TAG_PARAMETER)) {
				final String sName = atts.getValue("name");
				final String sType = atts.getValue("type");
				final String sDescription = atts.getValue("description");
				parameter = new DatasourceParameterVO(null, sName, sType, sDescription);
			}
			else if (qName.equals(TAG_VALUELISTPROVIDER)) {
				DatasourceParameterValuelistproviderVO vlp = new DatasourceParameterValuelistproviderVO(atts.getValue("type"));
				parameter.setValueListProvider(vlp);
			}
			else if (qName.equals(TAG_VALUELISTPROVIDER_PARAMETER)) {
				parameter.getValueListProvider().addParameter(atts.getValue("name"), atts.getValue("value"));
			}
			// process sql
			else if (qName.equals(TAG_SQL)) {
				final String strIsModelUsed = atts.getValue("isModelUsed");
				parseresult.bIsModelUsed = (strIsModelUsed == null || strIsModelUsed.equals("true"));
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) {
			if (qName.equals(TAG_SQL)) {
				parseresult.sQueryStringFromXml = sbCDATA == null ? "" : sbCDATA.toString().trim();
			}
			else if (qName.equals(TAG_PARAMETER)) {
				parseresult.lstParameters.add(parameter);
				parameter = null;
			}
			sbCDATA = null;
		}

		@Override
		public void characters(char[] ac, int start, int length) {
			if (isCDATA) {
				sbCDATA.append(ac, start, length);
			}
		}

		@Override
		public void endCDATA() throws SAXException {
			isCDATA = false;
		}

		@Override
		public void startCDATA() throws SAXException {
			sbCDATA = new StringBuffer();
			isCDATA = true;
		}

		@Override
		public void ignorableWhitespace(char[] ac, int start, int length) {
		}

		@Override
		public void processingInstruction(String target, String data) {
		}

		@Override
		public void endDTD() throws SAXException {
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

		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
		}

		@Override
		public void endEntity(String name) throws SAXException {
		}

		@Override
		public void startEntity(String name) throws SAXException {
		}

		@Override
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
		}
	}	// inner class XMLContentHandler

	public static class XMLTable {
		private final String sId;
		private final String sEntity;

		XMLTable(String sId, String sEntity) {
			this.sId = sId;
			this.sEntity = sEntity;
		}

		public String getEntity() {
			return sEntity;
		}

		public String getId() {
			return sId;
		}
	}	// inner class XMLTable

	public static class XMLColumn {
		private final String sTable;
		private final String sColumn;
		private final String sAlias;
		private final boolean bVisible;
		private String sGroup;
		private final String sSort;
		final List<DatasourceXMLParser.XMLCondition> lstConditions = new ArrayList<DatasourceXMLParser.XMLCondition>();

		XMLColumn(String sTable, String sColumn, String sAlias, boolean bVisible, String sGroup, String sSort) {
			this.sTable = sTable;
			this.sColumn = sColumn;
			this.sAlias = sAlias;
			this.bVisible = bVisible;
			this.sGroup = sGroup;
			this.sSort = sSort;
		}

		void addCondition(DatasourceXMLParser.XMLCondition condition) {
			lstConditions.add(condition);
		}

		public boolean isVisible() {
			return bVisible;
		}

		public List<DatasourceXMLParser.XMLCondition> getLstConditions() {
			return lstConditions;
		}

		public String getAlias() {
			return sAlias;
		}

		public String getColumn() {
			return sColumn;
		}

		public String getGroup() {
			return sGroup;
		}

		public String getSort() {
			return sSort;
		}

		public String getTable() {
			return sTable;
		}

		public void setGroup(String group) {
			sGroup = group;
		}
	} 	// inner class XMLColumn

	public static class XMLCondition {
		private final String sCondition;

		protected XMLCondition(String sCondition) {
			this.sCondition = sCondition;
		}

		public String getCondition() {
			return sCondition;
		}
	}	// inner class XMLCondition

	public static class XMLConnector {
		private final String sSrcTable;
		private final String sSrcColumn;
		private final String sDstTable;
		private final String sDstColumn;
		private final String sJoinType;

		XMLConnector(String sSrcTable, String sSrcColumn, String sDstTable, String sDstColumn, String sJoinType) {
			this.sSrcTable = sSrcTable;
			this.sSrcColumn = sSrcColumn;
			this.sDstTable = sDstTable;
			this.sDstColumn = sDstColumn;
			this.sJoinType = sJoinType;
		}

		public String getSrcTable() {
			return sSrcTable;
		}

		public String getDstColumn() {
			return sDstColumn;
		}

		public String getDstTable() {
			return sDstTable;
		}

		public String getJoinType() {
			return sJoinType;
		}

		public String getSrcColumn() {
			return sSrcColumn;
		}
	}	// inner class XMLConnector
}


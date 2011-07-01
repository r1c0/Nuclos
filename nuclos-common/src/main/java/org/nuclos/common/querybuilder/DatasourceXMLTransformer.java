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
package org.nuclos.common.querybuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.nuclos.common2.StringUtils;
import org.nuclos.common.NuclosFatalException;

/**
 * Datasource xml transformer to transform a xml representation of a datasource.
 * <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav
 *         Maksymovskyi</a>
 * @version 00.01.000
 */
public class DatasourceXMLTransformer {

	private static final String SYSTEMID = "http://www.novabit.de/technologies/querybuilder/querybuildermodel.dtd";
	private static final String RESOURCE_PATH = "org/nuclos/common/querybuilder/querybuildermodel.dtd";

	public static String replaceColumnAlias(String datasourceXML, String oldAlias, String newAlias) throws NuclosDatasourceException {
		Document doc = DatasourceXMLTransformer.getDatasourceXMLAsDocument(datasourceXML);
		DatasourceXMLTransformer.replaceAlias(doc, oldAlias, newAlias);
		return DatasourceXMLTransformer.getDatasourceXMLAsString(doc);
	}
	
	public static void replaceAlias(Document doc, String oldAlias, String newAlias) {
		NodeList list = doc.getElementsByTagName("column");
		for(int i=0; i<list.getLength(); i++){
			Node node = list.item(i);
			if(node.getAttributes().getNamedItem("alias") != null){
				Node alias = node.getAttributes().getNamedItem("alias");
				if(alias.getNodeValue() != null && alias.getNodeValue().equalsIgnoreCase(oldAlias)){
					alias.setNodeValue(newAlias);
					node.getAttributes().setNamedItem(alias);
				}
			}
		}
	}
	
	public static void replaceInValue(Document doc, String tagsName, String pattern, String value) {
		NodeList list = null;
		if(tagsName != null){
			list = doc.getElementsByTagName(tagsName);
		} else {
			list = doc.getChildNodes();
		}
		if(list != null){
			StringBuffer sb = null;
			int patternPos = -1;
			for(int i=0; i<list.getLength(); i++){
				Node node = list.item(i);
				if(node.getFirstChild().getNodeValue() != null){
					sb = new StringBuffer(node.getFirstChild().getNodeValue());
					
					patternPos = sb.indexOf(pattern);
					if(patternPos > -1){
						sb.replace(patternPos, patternPos+pattern.length(), value);
						node.getFirstChild().setNodeValue(sb.toString());
					}
				}
			}
		}
	}
	
	public static String getDatasourceXMLAsString(Document doc) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.transform(source, result);
			return sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Document getDatasourceXMLAsDocument(String xmlstring)
			throws NuclosDatasourceException {
		Document doc1 = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId,
						String systemId) throws IOException {
					InputSource result = null;
					if (systemId.equals(SYSTEMID)) {
						final URL url = Thread.currentThread()
								.getContextClassLoader().getResource(
										RESOURCE_PATH);
						if (url == null) {
							throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("querytable.missing.dtd.error", SYSTEMID));
								//"DTD f\u00fcr " + SYSTEMID + " kann nicht gefunden werden.");
						}
						result = new InputSource(new BufferedInputStream(url
								.openStream()));
					}
					return result;
				}
			});
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlstring));
			doc1 = db.parse(inStream);
		} catch (Exception e) {
			throw new NuclosDatasourceException("datasourcexmlparser.invalid.datasource", e);
					//"Die Datenquelle ist fehlerhaft.", e);
		}
		return doc1;
	}

	public static String setDatasourceParameters(String datasourceXML, Map<String,Object> params) throws NuclosDatasourceException {
		Document doc = DatasourceXMLTransformer.getDatasourceXMLAsDocument(datasourceXML);
		String pattern;
		Object valueObj;
		String value;
		for(String key : params.keySet()){
			pattern = "$"+key;
			valueObj = params.get(key);
			if(valueObj instanceof String){
				value = "'"+valueObj.toString()+"'";
			} else {
				value = valueObj.toString();
			}
			DatasourceXMLTransformer.replaceInValue(doc, "sql", pattern, value);
		}
		return DatasourceXMLTransformer.getDatasourceXMLAsString(doc);
	}
}

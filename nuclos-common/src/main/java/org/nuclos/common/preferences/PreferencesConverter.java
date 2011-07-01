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
package org.nuclos.common.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides utility methods for reading/writing preferences as a map (Map<String, Map<String, String>>).
 */
public class PreferencesConverter {

	private PreferencesConverter() {
	}
	
	/**
	 * Reads a preferences xml document as a map structure.
	 * The outer map represents the nodes while the inner map (values of the outer map) represents the node's 
	 * properties.  The key of the outer map is the absolute path (the fully qualified name) of the node.
	 * Note: the nesting of the nodes is not directly represented but encoded as part of the absolute path.
	 */
	public static NavigableMap<String, Map<String, String>> loadPreferences(InputStream is) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(getPreferencesDTDResolver());
			return loadPreferences(builder.parse(is));
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	private static NavigableMap<String, Map<String, String>> loadPreferences(Document doc) {
		TreeMap<String, Map<String, String>> map = new TreeMap<String, Map<String, String>>();
		Element root = (Element) doc.getDocumentElement().getElementsByTagName("root").item(0);
		readPreferencesChildNodesInto("/", root, map);
		return map;
	}

	private static void readPreferencesChildNodesInto(String path, Element parent, Map<String, Map<String, String>> map) {
		NodeList childNodes = parent.getChildNodes();
		for (int i = 0, n = childNodes.getLength(); i < n; i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if ("map".equals(child.getNodeName())) {
				// according to the DTD, this element will occur always exactly once
				map.put(path, readPreferencesMap((Element) child));
			} else if ("node".equals(child.getNodeName())) {
				String name = ((Element) child).getAttribute("name");
				String childPath = path.equals("/") ? "/" + name : path + "/" + name;
				readPreferencesChildNodesInto(childPath, (Element) child, map);
			}
		}
	}

	private static Map<String, String> readPreferencesMap(Element parent) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		NodeList childNodes = parent.getChildNodes();
		for (int i = 0, n = childNodes.getLength(); i < n; i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "entry".equals(child.getNodeName())) {
				String key = ((Element) child).getAttribute("key");
				String value = ((Element) child).getAttribute("value");
				result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Writes the preferences to the given output stream.
	 * <p>
	 * For the map structure, see {@link #loadPreferences(InputStream)}.  Missing intermediate
	 * node entries are inserted automatically.
	 */
	public static void writePreferences(OutputStream out, Map<String, Map<String, String>> map, boolean user) throws IOException {
		try {
			Document doc = createDocument(map, user);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, PREFERENCES_DTD_SYSTEM_URI);
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.VERSION, "1.0");
			t.transform(new DOMSource(doc), new StreamResult(out)); 
			out.close();
		} catch (IOException ex) {
			throw ex;			
		} catch(Exception ex) {
			throw new IOException(ex);
		}
	}

	private static Document createDocument(Map<String, Map<String, String>> map, boolean user) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element xmlPref = (Element) doc.appendChild(doc.createElement("preferences"));
		Element xmlRoot = (Element) xmlPref.appendChild(doc.createElement("root"));
		xmlRoot.setAttribute("type", user ? "user" : "system");
		xmlRoot.appendChild(doc.createElement("map"));

		Map<String, Element> nodeElems = new HashMap<String, Element>();
		nodeElems.put("", xmlRoot);
		for (Map.Entry<String, Map<String, String>> node : map.entrySet()) {
			Element xmlNode = getOrCreatePrefsNode(node.getKey(), nodeElems);
			Element xmlMap = (Element) xmlNode.getFirstChild();
			Map<String, String> entries = node.getValue();
			if (entries != null) {
				for (Map.Entry<String, String> entry : entries.entrySet()) {
					Element xmlEntry = doc.createElement("entry");
					xmlEntry.setAttribute("key", entry.getKey());
					xmlEntry.setAttribute("value", entry.getValue());
					xmlMap.appendChild(xmlEntry);
				}
			}
		}

		return doc;
	}

	private static Element getOrCreatePrefsNode(String path, Map<String, Element> xmlNodeElems) {
		Element xmlNode = xmlNodeElems.get(path);
		if (xmlNode == null) {
			int i = path.lastIndexOf('/');
			Element xmlParent = getOrCreatePrefsNode(path.substring(0, i), xmlNodeElems);
			xmlNode = xmlParent.getOwnerDocument().createElement("node");
			xmlNode.setAttribute("name", path.substring(i+1));
			xmlNode.appendChild(xmlParent.getOwnerDocument().createElement("map"));
			xmlParent.appendChild(xmlNode);
			xmlNodeElems.put(path, xmlNode);
		}
		return xmlNode;
	}

	public static EntityResolver getPreferencesDTDResolver() {
		return new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				if (systemId.equals(PREFERENCES_DTD_SYSTEM_URI))
					return new InputSource(PreferencesConverter.class.getResourceAsStream("preferences.dtd"));
				return null;
			}
		};
	}
	
	private static final String PREFERENCES_DTD_SYSTEM_URI = "http://java.sun.com/dtd/preferences.dtd";
}

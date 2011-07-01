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
package org.nuclos.installer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.installer.database.DbType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigFile {

	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

	private static final String XML_ROOT_TAG = "nuclos";

	private final Properties properties;

	public ConfigFile() {
		this.properties = new Properties();
	}

	public void load(InputStream is) throws IOException {
		properties.load(is);
	}

	public void loadXml(Document document) throws IOException {
		Element root = document.getDocumentElement();
		if (!XML_ROOT_TAG.equals(root.getTagName())) {
			throw new IOException("Root element '" + XML_ROOT_TAG + "' required");
		}

		Properties xmlProperties = new Properties();
		Node rootChild = root.getFirstChild();
		while (rootChild != null) {
			if (rootChild.getNodeType() == Node.ELEMENT_NODE) {
				readElementRecursive(xmlProperties, "", (Element) rootChild);
			}
			rootChild = rootChild.getNextSibling();
		}

		if (xmlProperties.containsKey("database.connection.url")) {
			DbType type = DbType.findType(xmlProperties.getProperty("database.adapter"));
			if (type != null) {
				type.parseJdbcConnectionString(xmlProperties.getProperty("database.connection.url"), xmlProperties);
			}
		}
		properties.putAll(xmlProperties);
	}

	private void readElementRecursive(Properties xmlProperties, String prefix, Element e) {
		if (prefix == null || prefix.isEmpty()) {
			prefix = e.getNodeName();
		}
		else {
			prefix = prefix + "." + e.getNodeName().replace('-', '.');
		}

		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Element.TEXT_NODE) {
				String value = child.getTextContent();
				if (xmlProperties.containsKey(prefix)) {
					String oldValue = xmlProperties.getProperty(prefix);
					value = value + System.getProperty("line.separator") + oldValue;
				}
				xmlProperties.put(prefix, value.trim());
			}
			else if (child.getNodeType() == Element.ELEMENT_NODE) {
				readElementRecursive(xmlProperties, prefix, (Element)child);
			}
		}
	}

	public String resolveProperties(String param) {
		if (param == null)
			return null;
		return resolvePropertiesImpl(param, new ArrayList<String>());
	}

	public boolean containsProperty(String property) {
		return properties.getProperty(property) != null;
	}

	public String getProperty(String property) {
		String value = getProperty(property, null);
		if (property == null)
			throw new IllegalArgumentException("Missing property " + property);
		return value;
	}

	public String getPropertyNonNull(String property) {
		String value = getProperty(property, null);
		return (value != null) ? value : "";
	}

	public String getProperty(String property, String defaultValue) {
		return getProperty(property, defaultValue, new ArrayList<String>());
	}

	public Map<String, String> getProperties() {
		Map<String, String> result = new HashMap<String, String>();
		for (Object key : properties.keySet()) {
			String value = getProperty(key.toString());
			result.put(key.toString(), value != null ? value : "");
		}
		return result;
	}


	private String getProperty(String property, String defaultValue, List<String> nesting) {
		if (nesting.contains(property))
			throw new IllegalArgumentException("Recursive property definition for " + property);

		String value = properties.getProperty(property, defaultValue);
		if (value != null) {
			try {
				nesting.add(property);
				return resolvePropertiesImpl(value, nesting);
			} finally {
				nesting.remove(nesting.size() - 1);
			}
		} else {
			return null;
		}
	}

	private String resolvePropertiesImpl(String text, List<String> nesting) {
		StringBuffer sb = new StringBuffer();

		Matcher matcher = PARAM_PATTERN.matcher(text);
		while (matcher.find()) {
			String subProperty = getProperty(matcher.group(1), null, nesting);
			if (subProperty == null)
				throw new IllegalArgumentException("Missing property " + matcher.group(1));
			matcher.appendReplacement(sb, Matcher.quoteReplacement(subProperty));
		}
		matcher.appendTail(sb);

		String result = sb.toString().trim();
		return result.length() > 0 ? result : null;
	}
}

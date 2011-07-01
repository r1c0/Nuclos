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
package org.nuclos.common2;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * XML utilities.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version	01.00.00
 */
public class XMLUtils {

	protected XMLUtils() {
	}

	/**
	 * gets the character encoding from the String representation of an XML file.
	 * @param sXml
	 * @return the encoding specified in the String, or "UTF-8" (default encoding for XML files) if none is specified.
	 * @postcondition result != null
	 */
	public static String getXMLEncoding(String sXml) {
		final int iEncodingStart = sXml.indexOf("encoding=");
		String result = null;

		if (iEncodingStart > 0) {
			final String encbuf = sXml.substring(iEncodingStart);
			final StringTokenizer tokenizer = new StringTokenizer(encbuf, "\"'", true);
			boolean encfound = false;
			while (tokenizer.hasMoreTokens()) {
				sXml = tokenizer.nextToken();
				if (sXml.equals("'") || sXml.equals("\"")) {
					encfound = true;
				}
				else {
					if (encfound) {
						result = sXml;
						break;
					}
				}
			}
		}
		if (result == null) {
			result = "UTF-8";
		}
		assert result != null;
		return result;
	}

   public static List<Element> getSubElements(Node nd) {
      LinkedList<Element> res = new LinkedList<Element>();
      NodeList crp = nd.getChildNodes();
      for(int i = 0, n = crp.getLength(); i < n; i++)
         if(crp.item(i) instanceof Element)
            res.add((Element) crp.item(i));
      return res;
   }

   public static List<Element> getSubElements(Node nd, final String type) {
      return CollectionUtils.applyFilter(getSubElements(nd), new Predicate<Element>() {
         @Override
		public boolean evaluate(Element row) {
            return getTagTransformer.transform(row).equals(type);
         }});
   }

   public static String getAttribute(Element elem, String name, String def) {
   	return elem.hasAttribute(name) ? elem.getAttribute(name) : def;
   }

   public static Transformer<Element, String> getTagTransformer() {
   	return getTagTransformer;
   }

   private static final Transformer<Element, String> getTagTransformer = new Transformer<Element, String>() {
		@Override
		public String transform(Element i) {
			return i.getTagName();
		}};

	public static XMLReader newSAXParser() {
		// bei Problemen: return new org.apache.xerces.parsers.SAXParser();
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = parserFactory.newSAXParser();
			return saxParser.getXMLReader();
		} catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

}  // class XMLUtils

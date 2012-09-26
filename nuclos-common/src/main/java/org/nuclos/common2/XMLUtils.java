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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
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

	private static final Logger LOG = Logger.getLogger(XMLUtils.class);
	
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	
	private static final SAXParserFactory nonValidatingFactory;
	
	private static final SAXParserFactory validatingFactory;
	
	static {
		nonValidatingFactory = SAXParserFactory.newInstance();
		nonValidatingFactory.setValidating(false);
		
		validatingFactory = SAXParserFactory.newInstance();
		validatingFactory.setValidating(true);
	}

	private XMLUtils() {
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

	public static XMLReader newSAXParser(boolean validating) throws SAXException {
		// bei Problemen: return new org.apache.xerces.parsers.SAXParser();
		final SAXParserFactory parserFactory = validating ? validatingFactory : nonValidatingFactory;
		final SAXParser saxParser;
		try {
			saxParser = parserFactory.newSAXParser();
		}
		catch (ParserConfigurationException e) {
			throw new CommonFatalException(e);
		}
		return saxParser.getXMLReader();
	}
	
	public static void parse(InputSource src, ContentHandler chandler, LexicalHandler lhandler, 
			EntityResolver resolver, boolean validating) throws SAXException {
		
		try {
			final XMLReader parser = newSAXParser(validating);
			parser.setContentHandler(chandler);
			if (lhandler != null) {
				parser.setProperty("http://xml.org/sax/properties/lexical-handler", lhandler);
			}
			if (resolver != null) {
				parser.setEntityResolver(resolver);
			}
			final DefaultErrorHandler ehandler = new DefaultErrorHandler(null, null);
			parser.setErrorHandler(ehandler);
			parser.parse(src);
			ehandler.throwFirst();
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}
	}
	
	public static void parse(String xmlContent, ContentHandler chandler, LexicalHandler lhandler, 
			EntityResolver resolver, boolean validating) throws SAXException {
		
		parse(new InputSource(new StringReader(xmlContent)), chandler, lhandler, resolver, validating);
	}
	
	private static class MapEntityResolver implements EntityResolver {
		
		private final Map<String,String> publicIdMap; 
		private final Map<String,String> systemIdMap;
		private final boolean throwException;
		
		private final Map<String,byte[]> cache;
		
		private MapEntityResolver(final Map<String,String> publicIdMap, 
				final Map<String,String> systemIdMap, final boolean throwException) {
			
			this.publicIdMap = publicIdMap;
			this.systemIdMap = systemIdMap;
			this.throwException = throwException;
			this.cache = new HashMap<String, byte[]>();
		}
		
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			byte[] buffer = cache.get(publicId);
			if (buffer == null) {
				buffer = cache.get(systemId);
			}
			if (buffer == null) {
				buffer = _resolveEntity(publicId, systemId);
				assert buffer != null;
				if (publicId != null) {
					cache.put(publicId, buffer);
				}
				if (systemId != null) {
					cache.put(systemId, buffer);
				}
			}
			return new InputSource(new ByteArrayInputStream(buffer));
		}
		
		private byte[] _resolveEntity(String publicId, String systemId) throws SAXException, IOException {		
			String resourcePath = null;
			if (publicIdMap != null) {
				resourcePath = publicIdMap.get(publicId);
			}
			if (resourcePath == null && systemIdMap != null) {
				resourcePath = systemIdMap.get(systemId);
			}
			if (resourcePath == null) {
				if (throwException) {
					throw new NuclosFatalException("Can't resolve entity/dtd for publicId=" + publicId + " nor systemId=" + systemId);
				}
				else {
					// see http://stuartsierra.com/2008/05/08/stop-your-java-sax-parser-from-downloading-dtds
					// return new InputSource(new StringReader(""));
					LOG.warn("Can't resolve entity/dtd for publicId=" + publicId + " nor systemId=" + systemId);
					return EMPTY_BYTE_ARRAY;
				}
			}
			final URL url = getClass().getClassLoader().getResource(resourcePath);
			LOG.info("Resolved entity/dtd for publicId=" + publicId + " systemId=" + systemId + " to " + url);
			return IOUtils.readFromBinaryStream(url.openStream());
		}		
	}
	
	public static EntityResolver newClasspathEntityResolver(final Map<String,String> publicIdMap, 
			final Map<String,String> systemIdMap, final boolean throwException) {
		
		return new MapEntityResolver(publicIdMap, systemIdMap, throwException);
	}
	
	private static class OneSystemIdEntityResolver implements EntityResolver {
		
		private final String systemIdToReplace;
		private final String systemIdReplacement; 
		private final boolean throwException;
		private byte[] cache;
		
		private OneSystemIdEntityResolver(final String systemIdToReplace, final String systemIdReplacement, 
				final boolean throwException) {
			
			if (systemIdToReplace == null || systemIdReplacement == null) {
				throw new IllegalArgumentException();
			}			
			this.systemIdToReplace = systemIdToReplace;
			this.systemIdReplacement = systemIdReplacement;
			this.throwException = throwException;
		}
		
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			byte[] buffer = cache;
			if (buffer == null) {
				buffer = _resolveEntity(publicId, systemId);
				assert buffer != null;
				if (systemId.equals(systemIdToReplace)) {
					cache = buffer;
				}
			}
			return new InputSource(new ByteArrayInputStream(buffer));
		}
		
		private byte[] _resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			final String resourcePath = systemId.equals(systemIdToReplace) ? systemIdReplacement : null;
			if (resourcePath == null) {
				if (throwException) {
					throw new NuclosFatalException("Can't resolve entity/dtd for publicId=" + publicId + " nor systemId=" + systemId);
				}
				else {
					// see http://stuartsierra.com/2008/05/08/stop-your-java-sax-parser-from-downloading-dtds
					// return new InputSource(new StringReader(""));
					LOG.warn("Can't resolve entity/dtd for publicId=" + publicId + " nor systemId=" + systemId);
					return EMPTY_BYTE_ARRAY;
				}
			}
			final URL url = getClass().getClassLoader().getResource(resourcePath);
			LOG.info("Resolved entity/dtd for publicId=" + publicId + " systemId=" + systemId + " to " + url);
			return IOUtils.readFromBinaryStream(url.openStream());
		}
	}

	public static EntityResolver newClasspathEntityResolver(final String systemIdToReplace, final String systemIdReplacement, 
			final boolean throwException) {
		
		return new OneSystemIdEntityResolver(systemIdToReplace, systemIdReplacement, throwException);
	}
	
	public static class DefaultErrorHandler implements ErrorHandler {
		
		private final String resource;
		
		private final Logger log;
		
		private SAXException first;
		
		public DefaultErrorHandler(String resource, Logger log) {
			this.resource = resource;
			if (log == null) {
				this.log = LOG;
			}
			else {
				this.log = LOG;
			}
		}
		
		private String toString(SAXParseException e) {
			final StringBuilder result = new StringBuilder();
			if (resource != null) {
				result.append("in '").append(resource).append("' ");
			}
			if (e.getPublicId() != null) {
				result.append("pId=").append(e.getPublicId()).append(" ");
			}
			if (e.getSystemId() != null) {
				result.append("sId=").append(e.getSystemId()).append(" ");
			}
			result.append("at (").append(e.getLineNumber()).append(",").append(e.getColumnNumber()).append(") ");
			result.append(": ").append(e.getMessage());
			if (e.getCause() != null) {
				result.append(", caused by: ").append(e.getCause());
			}
			return result.toString();
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			log.warn(toString(exception));
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			if (first == null) {
				first = exception;
			}
			log.error(toString(exception));
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			if (first == null) {
				first = exception;
			}
			log.fatal(toString(exception));
		}
		
		public void throwFirst() throws SAXException {
			if (first != null) {
				throw first;
			}
		}
		
	}
	
}  // class XMLUtils

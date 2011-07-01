//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General public static License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General public static License for more details.
//
//You should have received a copy of the GNU Affero General public static License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.installer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlUtils {

	private static final DocumentBuilder documentBuilder;
	private static final XPathFactory xpathFactory;

	static {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = factory.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		xpathFactory = XPathFactory.newInstance();
	}

	public static Document readDocument(File file) throws SAXException, IOException {
		return documentBuilder.parse(file);
	}
	
	public static Document readDocument(InputStream is) throws SAXException, IOException {
		return documentBuilder.parse(is);
	}
	
	public static void writeDocument(Document document, File file) throws TransformerException {
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		Transformer idTransform = transformFactory.newTransformer();
		Source input = new DOMSource(document);
		Result output = new StreamResult(file);
		idTransform.transform(input, output);
	}
	
	public static void writeDocument(Document document, OutputStream out) throws TransformerException {
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		Transformer idTransform = transformFactory.newTransformer();
		Source input = new DOMSource(document);
		Result output = new StreamResult(out);
		idTransform.transform(input, output);
	}
	
	public static void processStylesheet(Document document, OutputStream out, InputStream stylesheet, Map<String, ?> parameters) throws TransformerException {
		processStylesheet(document, out, stylesheet, parameters, null);
	}
	
	public static void processStylesheet(Document document, OutputStream out, InputStream stylesheet, Map<String, ?> parameters, URIResolver resolver) throws TransformerException {
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		Transformer transformer = transformFactory.newTransformer(new StreamSource(stylesheet));
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		if (parameters != null) {
			for (Map.Entry<String, ?> e : parameters.entrySet()) {
				transformer.setParameter(e.getKey(), e.getValue());
			}
		}
		Source input = document != null ? new DOMSource(document) : new DOMSource();
		Result output = new StreamResult(out);
		transformer.setErrorListener(new ErrorListener() {
			@Override
			public void error(TransformerException exception) throws TransformerException {
				System.err.println("error: " + exception);
			}

			@Override
			public void fatalError(TransformerException exception) throws TransformerException {
				System.err.println("fatalError: " + exception);
			}

			@Override
			public void warning(TransformerException exception) throws TransformerException {
				System.err.println("warning: " + exception);
			}
		});
		transformer.setURIResolver(resolver);
		transformer.transform(input, output);
	}
	
	public static Node getXPathNode(Node node, String expression) throws XPathExpressionException {
		XPath xpath = xpathFactory.newXPath();
		return (Node) xpath.evaluate(expression, node, XPathConstants.NODE);
	}
}

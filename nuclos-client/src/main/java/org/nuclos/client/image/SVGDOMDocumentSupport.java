//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.client.image;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDefsElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGLineElement;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPolygonElement;
import org.w3c.dom.svg.SVGPolylineElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGTextElement;
import org.w3c.dom.svg.SVGUseElement;

/**
 * Some basic SVG references:
 * http://svg.tutorial.aptico.de/start.php
 * http://www.w3schools.com/svg/default.asp
 * 
 * @author Thomas Pasch
 */
public class SVGDOMDocumentSupport extends SVGDOMImplementation {
	
    private static final DOMImplementation DOM_IMPL = SVGDOMImplementation.getDOMImplementation();

    //
    
    private final AbstractDocument doc;
    
    public SVGDOMDocumentSupport() {
		doc = newSVG();
	}
    
    public SVGDOMDocumentSupport(String uri) throws IOException {
    	// http://xmlgraphics.apache.org/batik/using/dom-api.html
    	String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        doc = (AbstractDocument) f.createDocument(uri);   	
    }
	
	private static AbstractDocument newSVG() {
		// see http://wiki.apache.org/xmlgraphics-batik/UsingDOM3
		// see http://xmlgraphics.apache.org/batik/using/dom-api.html
		
		// Create a new SVG document
        Document doc = DOM_IMPL.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);

        // Create a 'g' element and append it to the root 'svg' element
        Element e = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        doc.getDocumentElement().appendChild(e);

        // Cast the document object to org.apache.batik.dom.AbstractDocument,
        // so that DOM 3 methods will be guaranteed to be visible
        AbstractDocument document = (AbstractDocument) doc;

        // Now a DOM 3 method can be used
        document.renameNode(e, "http://www.w3.org/2000/svg", "text");
        return document;
	}
	
	public SVGDocument getDocument() {
		return (SVGDocument) doc;
	}
	
	public Float getHeight() {
		final String h = doc.getDocumentElement().getAttribute("height");
		final Float result;
		if (h == null) {
			result = null;
		}
		else {
			result = Float.parseFloat(h);
		}
		return result;
	}
	
	public Float getWidth() {
		final String w = doc.getDocumentElement().getAttribute("width");
		final Float result;
		if (w == null) {
			result = null;
		}
		else {
			result = Float.parseFloat(w);
		}
		return result;
	}
	
	public void writeAs(File file, ImageType type) throws IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			writeAs(out, type);
		}
		finally {
			out.close();
		}		
	}
	
	public void writeAs(OutputStream out, ImageType type) throws IOException {
		switch (type) {
		case SVG:
			writeAsSvg(out);
			break;
		case EMF:
			writeAsEmf(out);
			break;
		case PNG:
			writeAsPng(out);
			break;
		default:
			throw new IllegalArgumentException("Can't output to " + type);
		}
	}
	
	public void writeAsEmf(File file) throws IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			writeAsEmf(out);
		}
		finally {
			out.close();
		}
	}
	
	public void writeAsEmf(OutputStream out) throws IOException {
		final SVG2EMF svg2emf = new SVG2EMF();
		svg2emf.convert((SVGDocument) doc, out);
	}

	public void writeAsPng(File file) throws IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			writeAsPng(out);
		}
		finally {
			out.close();
		}
	}
	
	public void writeAsPng(OutputStream out) throws IOException {
		final PNGTranscoder t = new PNGTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE, Boolean.TRUE);
		t.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.white);
		final Float h = getHeight();
		if (h != null) {
			t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, h);
		}
		final Float w = getWidth();
		if (w != null) {
			t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, w);
		}
		write(t, out);
	}

	public void writeAsSvg(File file) throws IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			writeAsSvg(out);
		}
		finally {
			out.close();
		}
	}
	
	public void writeAsSvg(OutputStream os) throws IOException {
		Writer out = new OutputStreamWriter(os, "UTF-8");
		if (!(os instanceof BufferedOutputStream)) {
			out = new BufferedWriter(out);
		}
		writeAsSvg(out);
	}

	public void writeAsSvg(Writer out) throws IOException {
		Source s = new DOMSource(doc);
		Result r = new StreamResult(out);
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.STANDALONE, "no");
			t.transform(s, r);
		}
		catch (TransformerConfigurationException e) {
			throw new IOException("transformer configuration failed: " + e.toString(), e);
		}
		catch (TransformerException e) {
			throw new IOException("transformation failed: " + e.toString(), e);
		}
	}
	
	// not working
	public void writeAsSvg2(Writer out) throws IOException {
		final SVGTranscoder t = new SVGTranscoder();
		t.addTranscodingHint(SVGTranscoder.KEY_FORMAT, SVGTranscoder.VALUE_FORMAT_ON);
		t.addTranscodingHint(SVGTranscoder.KEY_DOCUMENT_WIDTH, 120);
		t.addTranscodingHint(SVGTranscoder.KEY_DOCTYPE, SVGTranscoder.VALUE_DOCTYPE_REMOVE);
		write(t, out);
	}

	public void write(Transcoder t, OutputStream out) throws IOException {
		final TranscoderInput input = new TranscoderInput(doc);
		final TranscoderOutput output = new TranscoderOutput(out);
		try {
			t.transcode(input, output);
		}
		catch (TranscoderException e) {
			throw new IOException("transcoding failed: " + e.toString(), e);
		}
	}

	public void write(Transcoder t, Writer out) throws IOException {
		final TranscoderInput input = new TranscoderInput(doc);
		final TranscoderOutput output = new TranscoderOutput(out);
		try {
			t.transcode(input, output);
		}
		catch (TranscoderException e) {
			throw new IOException("transcoding failed", e);
		}
	}

	public SVGElement createSVGElement(String name) {
		ElementFactory ef = (ElementFactory) factories.get(name);
		if (ef != null) {
			final SVGElement result = (SVGElement) ef.create(name, doc);
			// avoid NS definition on each node
			result.setPrefix(null);
			return result;
		}
		throw new IllegalArgumentException("Unknown SVG element '" + name + "'");
	}
	
	public SVGRectElement createRect(float x, float y, float width, float height, String clazz) {
		final SVGRectElement result = (SVGRectElement) createSVGElement("rect");
		result.setAttribute("x", Float.toString(x));
		result.setAttribute("y", Float.toString(y));
		result.setAttribute("width", Float.toString(width));
		result.setAttribute("height", Float.toString(height));
		if (clazz != null) {
			result.setAttribute("class", clazz);
		}
		return result;
	}

	public SVGTextElement createText(float x, float y, String text, String clazz) {
		final SVGTextElement result = (SVGTextElement) createSVGElement("text");
		result.setAttribute("x", Float.toString(x));
		result.setAttribute("y", Float.toString(y));
		if (clazz != null) {
			result.setAttribute("class", clazz);
		}
		result.appendChild(doc.createTextNode(text));
		return result;
	}
	
	public SVGPolygonElement createPolygon(String clazz, float... p) {
		final SVGPolygonElement result = (SVGPolygonElement) createSVGElement("polygon");
		result.setAttribute("points", createPathString(p));
		if (clazz != null) {
			result.setAttribute("class", clazz);
		}
		return result;
	}
	
	public SVGPolylineElement createPolyline(String clazz, float... p) {
		final SVGPolylineElement result = (SVGPolylineElement) createSVGElement("polyline");
		result.setAttribute("points", createPathString(p));
		if (clazz != null) {
			result.setAttribute("class", clazz);
		}
		return result;
	}
	
	public SVGPathElement createPath() {
		final SVGPathElement result = (SVGPathElement) createSVGElement("path");
		return result;
	}
		
	public SVGLineElement createLine() {
		final SVGLineElement result = (SVGLineElement) createSVGElement("line");
		return result;
	}
	
	public SVGGElement createGroup() {
		final SVGGElement result = (SVGGElement) createSVGElement("g");
		return result;
	}
	
	public SVGDefsElement createDefs() {
		final SVGDefsElement result = (SVGDefsElement) createSVGElement("defs");
		return result;
	}
	
	public SVGUseElement createUse() {
		final SVGUseElement result = (SVGUseElement) createSVGElement("use");
		return result;
	}
	
	private String createPathString(float... p) {
		final StringBuilder result = new StringBuilder();
		int n = 0;
		for (float f: p) {
			result.append(Float.toString(f));
			if (++n % 2 == 0) {
				result.append(", ");
			}
			else {
				result.append(" ");
			}
		}
		return result.toString();
	}
	
}

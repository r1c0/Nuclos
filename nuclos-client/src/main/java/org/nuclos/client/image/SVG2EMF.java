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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * {@link http://svg2emf.googlecode.com/svn/trunk/SVG2EMF/src/main/java/net/hanjava/svg/SVG2EMF.java}
 * 
 * @author behumble@hanjava.net
 * @author Thomas Pasch
 */
public class SVG2EMF {

	private final UserAgentAdapter ua = new UserAgentAdapter();
	private final DocumentLoader loader = new DocumentLoader(ua);
	private final BridgeContext cxt = new BridgeContext(ua, loader);

	public SVG2EMF() {
	}
	
	public void convert(String svgUri, File emfFile) throws IOException {
		// build a SVGDocument.
		// write to EmfWriter
		final OutputStream emfStream = new BufferedOutputStream(new FileOutputStream(emfFile));
		try {
			Document svgDoc = loader.loadDocument(svgUri);
			convert((SVGDocument) svgDoc, emfStream);
		}
		finally {
			loader.dispose();
			emfStream.close();			
		}
	}

	public void convert(SVGDocument svgDoc, OutputStream out) throws IOException {
		EmfWriterGraphics eg2d = null;
		try {
			// Build a GVTTree
			GVTBuilder gvtBuilder = new GVTBuilder();
			GraphicsNode rootNode = gvtBuilder.build(cxt, svgDoc);
	
			// x,y can be non-(0,0)
			Rectangle2D bounds = rootNode.getBounds();
			int w = (int) (bounds.getX() + bounds.getWidth());
			int h = (int) (bounds.getY() + bounds.getHeight());
	
			Dimension size = new Dimension(w, h);
			eg2d = new EmfWriterGraphics(out, size);

			eg2d.setDeviceIndependent(true);
			eg2d.startExport();
			rootNode.paint(eg2d);
		}
		finally {
			cxt.dispose();
			if (eg2d != null) {
				eg2d.dispose();
				eg2d.endExport();
			}
		}
	}
	
}

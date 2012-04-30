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
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.freehep.graphicsio.emf.EMFGraphics2D;

/**
 * {@link http://svg2emf.googlecode.com/svn/trunk/SVG2EMF/src/main/java/net/hanjava/svg/EmfWriterGraphics.java}
 * 
 * @author behumble@hanjava.net
 */
public class EmfWriterGraphics extends EMFGraphics2D {

	static private GraphicsConfiguration waDC;

	public EmfWriterGraphics(OutputStream os, Dimension size) {
		super(os, size);
		// Workaround Image
		BufferedImage waImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		if (waDC == null) {
			waDC = waImg.createGraphics().getDeviceConfiguration();
		}
		// Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint
		// (http://batik.2283329.n4.nabble.com/unwanted-message-td2977046.html)
		setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, new WeakReference<BufferedImage>(waImg));
	}

	protected EmfWriterGraphics(EMFGraphics2D graphics, boolean doRestoreOnDispose) {
		super(graphics, doRestoreOnDispose);
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return waDC;
	}

	@Override
	public Graphics create() {
		// Create a new graphics context from the current one.
		try {
			// Save the current context for restore later.
			writeGraphicsSave();
		}
		catch (IOException e) {
			handleException(e);
		}
		// The correct graphics context should be created.
		EMFGraphics2D result = new EmfWriterGraphics(this, true);
		return result;
	}
}

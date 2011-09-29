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
 * Abstract shape viewer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

package org.nuclos.client.gef;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Iterator;

import org.nuclos.client.gef.shapes.AbstractConnector;

public abstract class AbstractShapeViewer extends AbstractViewer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected BufferedImage screenBuffer = null;
	protected Graphics2D gfxBuffer = null;
	protected MapMode mapMode = null;
	protected Dimension bufferDimension = new Dimension(-1, -1);

	/**
	 *
	 */
	public AbstractShapeViewer() {
		this(null);
	}

	/**
	 *
	 * @param controller
	 */
	public AbstractShapeViewer(AbstractShapeController controller) {
		super();

		model = new DefaultShapeModel(this);
		model.addShapeModelListener(this);

		shapeController = controller;
		if (controller == null) {
			shapeController = new AbstractController(this);
		}
		else {
			shapeController = controller;
		}
/*		addMouseListener(shapeController);
		addMouseMotionListener(shapeController);
		addKeyListener(shapeController); */

		mapMode = new MapMode();
		mapMode.setMode(MapMode.MM_PIXEL);
	}

	/**
	 *
	 */
	@Override
	protected void initGraphics() {
		if (getWidth() > 0 && getHeight() > 0) {

			screenBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			bufferDimension = new Dimension(getWidth(), getHeight());

			gfxBuffer = (Graphics2D) screenBuffer.getGraphics();
			gfxBuffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			gfxBuffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			gfxBuffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			gfxBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			AffineTransform transform = new AffineTransform();
			switch (mapMode.getMode()) {
				case MapMode.MM_PIXEL:
					transform.setToScale(dZoom, dZoom);
					break;
				case MapMode.MM_HIMETRIC:
					double scale = MapMode.DPI / 25.4d;
					transform.setToScale(scale * dZoom, scale * dZoom);
			}
			gfxBuffer.setTransform(transform);

			float[] afDashes = new float[2];
			afDashes[0] = 5.0f;
			afDashes[1] = 5.0f;
			rubberBandStroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
					1.0f, afDashes, 3.0f);

		}
		
		
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics gfx) {
		initGraphics();
		gfxBuffer.setColor(bgColor);
		gfxBuffer.fillRect(0, 0, getWidth(), getHeight());
		
		paintGrid(gfxBuffer);		
	
		for (Iterator<AbstractShapeModel.Layer> layer = model.getVisibleLayers().iterator(); layer.hasNext();) {
			AbstractShapeModel.Layer currentLayer = layer.next();
			Iterator<Shape> i = currentLayer.getShapes().iterator();
			while (i.hasNext()) {
				Shape shape = i.next();
				shape.paint(gfxBuffer);
				if (shape instanceof AbstractConnector) {
					gfxBuffer.drawString("sdsdsd", 0, 0);
				}
			}
		}	
				
		gfx.drawImage(screenBuffer, 0, 0, this);
						
	}
	
	
 
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex >= 1) {
			return Printable.NO_SUCH_PAGE;
		}

		Graphics2D gfx = (Graphics2D) graphics;

		//Rectangle2D r = model.getShapeDimension();
		//double dPageWidth = pageFormat.getImageableWidth();
		//double dPageHeight = pageFormat.getImageableHeight();
		//double dBufferWidth = r.getWidth();
		//double dBufferHeight = r.getHeight();
		gfx.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		gfx.scale(0.5d, 0.5d);

		for (Iterator<AbstractShapeModel.Layer> layer = model.getVisibleLayers().iterator(); layer.hasNext();) {
			AbstractShapeModel.Layer currentLayer = layer.next();
			Iterator<Shape> i = currentLayer.getShapes().iterator();
			while (i.hasNext()) {
				Shape shape = i.next();
				shape.paint(gfx);
			}
		}
		return Printable.PAGE_EXISTS;
	}
}

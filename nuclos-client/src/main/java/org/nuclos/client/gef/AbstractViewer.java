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
package org.nuclos.client.gef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Collection;

import javax.swing.JPanel;

import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.shapes.AbstractShape;

/**
 * Abstract viewer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public abstract class AbstractViewer extends JPanel implements ShapeViewer, ShapeModelListener, Printable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class AbstractViewerComponentListener implements ComponentListener {
		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			initGraphics();
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}
	}

	;

	protected AbstractController shapeController;
	protected Color bgColor = new Color(255, 255, 255);

	protected boolean bEditable;

	protected Rectangle2D rubberBand = new Rectangle2D.Double();
	private float[] dashes = {1f, 2f};
	protected Stroke rubberBandStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, dashes, 1f);

	protected double dZoom = 1d;
	protected double dGridX, dGridY;
	protected boolean bSnapGrid;
	protected Extents2D extents = new Extents2D(1024d, 768d);
	protected PrinterJob printerJob = null;

	protected AbstractShapeModel model = null;

	public AbstractViewer() {
		super();

		AbstractShape.initShapeId();

		setFocusable(true);
		setBgColor(bgColor);

		addComponentListener(new AbstractViewerComponentListener());

		bEditable = true;
		bSnapGrid = true;
		dGridX = dGridY = 8d;

		initGraphics();
	}

	protected void initGraphics() {
	}

	/**
	 *
	 * @param dX1
	 * @param dY1
	 * @param dX2
	 * @param dY2
	 */
	public void drawRubberBand(double dX1, double dY1, double dX2, double dY2) {
		Graphics2D gfx = (Graphics2D) this.getGraphics();
		gfx.setXORMode(Color.GRAY);
		gfx.setStroke(rubberBandStroke);

		double dX = Math.min(dX1, dX2);
		double dY = Math.min(dY1, dY2);
		double dW = Math.max(dX1, dX2) - dX;
		double dH = Math.max(dY1, dY2) - dY;

		// Restore background
		if (rubberBand.getWidth() >= 0) {
			gfx.draw(rubberBand);
		}
		rubberBand.setRect(dX, dY, dW, dH);
		gfx.draw(rubberBand);
	}

	/**
	 *
	 */
	public void clearRubberBand() {
		Graphics2D gfx = (Graphics2D) this.getGraphics();
		gfx.setXORMode(Color.LIGHT_GRAY);
		gfx.setStroke(rubberBandStroke);
		gfx.draw(rubberBand);
	}

	/**
	 *
	 */
	public void resetRubberBand() {
		rubberBand.setRect(-1.0d, -1.0d, -1.0d, -1.0d);
	}

	/**
	 *
	 * @return
	 */
	public Rectangle2D getRubberBand() {
		return rubberBand;
	}

	/**
	 *
	 * @param newController
	 */
	@Override
	public void setController(AbstractController newController) {
		if (shapeController != null) {
			removeMouseListener(shapeController);
			removeMouseMotionListener(shapeController);
			removeKeyListener(shapeController);
		}
		shapeController = newController;
		addMouseListener(shapeController);
		addMouseMotionListener(shapeController);
		addKeyListener(shapeController);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public AbstractController getController() {
		return shapeController;
	}

	@Override
	public abstract void selectionChanged(Shape shape);

	@Override
	public abstract void multiSelectionChanged(Collection<Shape> collShapes);

	@Override
	public abstract void shapeDeleted(Shape shape);

	@Override
	public abstract void shapesDeleted(Collection<Shape> collShapes);

	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int) (extents.getWidth() * (dZoom < 1d ? 1d : dZoom)),
				(int) (extents.getHeight() * (dZoom < 1d ? 1d : dZoom)));
	}

	/**
	 * @return
	 */
	@Override
	public Color getBgColor() {
		return bgColor;
	}

	/**
	 * @param color
	 */
	@Override
	public void setBgColor(Color color) {
		bgColor = color;
		setBackground(bgColor);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean isEditable() {
		return bEditable;
	}

	/**
	 *
	 * @param editable
	 */
	@Override
	public void setEditable(boolean editable) {
		editable = true;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public double getZoom() {
		return dZoom;
	}

	/**
	 *
	 * @param dValue
	 */
	@Override
	public void setZoom(double dValue) {
		dZoom = dValue;		
		initGraphics();
		setPreferredSize(new Dimension((int) (extents.getWidth() * dZoom), (int) (extents.getHeight() * dZoom)));
		doLayout();
		repaint();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public double getGridX() {
		return dGridX;
	}

	/**
	 *
	 * @param dGridX
	 */
	@Override
	public void setGridX(double dGridX) {
		this.dGridX = dGridX;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public double getGridY() {
		return dGridY;
	}

	/**
	 *
	 * @param dGridY
	 */
	@Override
	public void setGridY(double dGridY) {
		this.dGridY = dGridY;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean isSnapGrid() {
		return bSnapGrid;
	}

	/**
	 *
	 * @param bSnapGrid
	 */
	@Override
	public void setSnapGrid(boolean bSnapGrid) {
		this.bSnapGrid = bSnapGrid;
	}

	public Extents2D getExtents() {
		return extents;
	}

	public void setExtents(Extents2D size) {
		this.extents = size;
	}

	/**
	 *
	 */
	@Override
	public void modelChanged() {
		invalidate();
	}

	@Override
	public abstract int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException;

	/**
	 *
	 * @return
	 */
	@Override
	public ShapeModel getModel() {
		return model;
	}
	
	protected void paintGrid(Graphics2D gfx) {
		final Color gridColor = new Color(192, 192, 192);
		int h = this.getHeight();
		int w = this.getWidth();

		Paint p = gfx.getPaint();
		Color c = gfx.getColor();
		
		gfx.setPaint(gridColor);
		gfx.setColor(gridColor);
		for(int dh = 0; dh < h; dh += this.dGridY){
			for(int dw = 0; dw < w; dw += this.dGridX){				
				gfx.drawLine(dw, dh, dw, dh);
			}
		}
		
		gfx.setPaint(p);
		gfx.setColor(c);

	}
}

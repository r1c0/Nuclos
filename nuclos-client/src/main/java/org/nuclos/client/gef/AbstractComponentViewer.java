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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;

import org.nuclos.client.datasource.querybuilder.shapes.TableShape;
import org.nuclos.client.gef.layout.XYLayout;

/**
 * Abstract component viewer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public abstract class AbstractComponentViewer extends AbstractViewer {

	private JLayeredPane viewPane = new JLayeredPane();

	public AbstractComponentViewer() {
		this(null);
	}

	public AbstractComponentViewer(AbstractController controller) {
		super();

		setLayout(new BorderLayout());
		model = new DefaultComponentModel(this);
		model.addShapeModelListener(this);

		shapeController = controller;

		viewPane.setLayout(new XYLayout());
		viewPane.setBackground(bgColor);
		viewPane.setPreferredSize(new Dimension(1280, 1024));

		add(viewPane, BorderLayout.CENTER);
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		return 0;
	}

	@Override
	public void paint(Graphics gfx) {
		this.setOpaque(true);
		Graphics2D gfx2D = (Graphics2D) gfx;

		gfx2D.scale(dZoom, dZoom);
		super.paint(gfx2D);
		
		paintGrid(gfx2D);

		gfx2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		gfx2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		gfx2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gfx2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (Iterator<AbstractShapeModel.Layer> layers = model.getVisibleLayers().iterator(); layers.hasNext();) {
			AbstractShapeModel.Layer layer = layers.next();
			for (Iterator<Shape> shapes = layer.getShapes().iterator(); shapes.hasNext();) {
				Shape shape = shapes.next();
				if (!(shape instanceof TableShape))
					shape.paint(gfx2D);
			}
		}
	}

	@Override
	public void modelChanged() {
		super.modelChanged();
	}

	public JLayeredPane getViewPane() {
		return viewPane;
	}

	public void addShape(JComponent component, int iOrder) {
		viewPane.add(component, iOrder);
	}

	public void removeAllShapes() {
		viewPane.removeAll();
	}

	public void removeShape(JComponent component) {
		viewPane.remove(component);
	}
}

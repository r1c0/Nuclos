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
package org.nuclos.client.gef.shapes;

import java.awt.ComponentOrientation;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.JComponent;

import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.layout.FlowLayout;
import org.nuclos.client.gef.layout.ILayout;

/**
 * Container shape.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public abstract class ContainerShape extends RectangularShape {

	protected ArrayList<Shape> shapes = new ArrayList<Shape>();
	protected ILayout layoutManager = new FlowLayout();

	public ContainerShape() {
		this(0d, 0d, 0d, 0d);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 */
	public ContainerShape(double dX, double dY, double dWidth, double dHeight) {
		super(dX, dY, dWidth, dHeight);
	}

	/**
	 *
	 * @param shape
	 */
	public void addShape(Shape shape) {
		shapes.add(shape);
		doLayout();
	}

	/**
	 *
	 * @param shape
	 */
	public void removeShape(Shape shape) {
		shapes.remove(shape);
		doLayout();
	}

	/**
	 *
	 * @param shapes
	 */
	public void addShapes(Collection<Shape> shapes) {
		shapes.addAll(shapes);
		doLayout();
	}

	/**
	 *
	 * @param shapes
	 */
	public void removeShapes(Collection<Shape> shapes) {
		shapes.removeAll(shapes);
		doLayout();
	}

	/**
	 *
	 */
	public void clearShapes() {
		shapes.clear();
		doLayout();
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		super.paint(gfx);
	}

	/**
	 *
	 */
	public void doLayout() {

	}

	public int getShapeCount() {
		return shapes.size();
	}

	public Shape getShape(int index) {
		return shapes.get(index);
	}

	public ComponentOrientation getComponentOrientation() {
		return ComponentOrientation.getOrientation(Locale.getDefault());
	}

	public Shape[] getShapeArray() {
		return (Shape[]) shapes.toArray();
	}
	
	public abstract void doubleClicked(JComponent parent);
}

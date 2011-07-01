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

import java.util.Collection;
import java.util.Iterator;

import org.nuclos.client.gef.shapes.ComponentAdapter;

/**
 * Abstract component model.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class AbstractComponentModel extends AbstractShapeModel {
	AbstractComponentViewer componentViewer;

	/**
	 *
	 * @param view
	 */
	public AbstractComponentModel(ShapeViewer view) {
		super(view);
		componentViewer = (AbstractComponentViewer) view;
	}

	/**
	 *
	 * @param shape
	 */
	@Override
	public void addShape(Shape shape) {
		super.addShape(shape);
		if (shape instanceof ComponentAdapter) {
			int iOrder = getActiveLayer().getOrder();
			componentViewer.addShape(((ComponentAdapter) shape).getComponent(), iOrder);
		}
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
	public void addShapes(Collection<Shape> shapes) {
		super.addShapes(shapes);

		for (Iterator<Shape> i = shapes.iterator(); i.hasNext();) {
			Shape shape = i.next();
			if (shape instanceof ComponentAdapter) {
				int iOrder = getActiveLayer().getOrder();
				componentViewer.addShape(((ComponentAdapter) shape).getComponent(), iOrder);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void removeAllShapes() {
		super.removeAllShapes();
		componentViewer.removeAllShapes();
	}

	/**
	 *
	 * @param shape
	 */
	@Override
	public void removeShape(Shape shape) {
		super.removeShape(shape);
		if (shape instanceof ComponentAdapter) {
			componentViewer.removeShape(((ComponentAdapter) shape).getComponent());
		}
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
	public void removeShapes(Collection<Shape> shapes) {
		super.removeShapes(shapes);

		for (Iterator<Shape> i = shapes.iterator(); i.hasNext();) {
			Shape shape = i.next();
			if (shape instanceof ComponentAdapter) {
				getActiveLayer().getOrder();
				componentViewer.removeShape(((ComponentAdapter) shape).getComponent());
			}
		}
	}
}

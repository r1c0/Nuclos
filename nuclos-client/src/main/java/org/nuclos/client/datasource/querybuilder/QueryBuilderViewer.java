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
package org.nuclos.client.datasource.querybuilder;

import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nuclos.client.datasource.querybuilder.controller.QueryBuilderController;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnector;
import org.nuclos.client.gef.AbstractComponentViewer;
import org.nuclos.client.gef.AbstractController;
import org.nuclos.client.gef.AbstractShapeModel;
import org.nuclos.client.gef.Shape;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class QueryBuilderViewer extends AbstractComponentViewer {

	private DropTarget dropTarget;

	public QueryBuilderViewer() {
		super();
		setGridX(8);
		setGridY(8);
		setPreferredSize(new Dimension(2048, 2048));
		setSnapGrid(true);
	}

	@Override
	public void multiSelectionChanged(Collection<Shape> collShapes) {
	}

	@Override
	public void selectionChanged(Shape shape) {
	}

	@Override
	public void shapeDeleted(Shape shape) {
		QueryBuilderController controller = (QueryBuilderController) shapeController;
		controller.removeShape(shape);
	}

	@Override
	public void shapesDeleted(Collection<Shape> collShapes) {
		QueryBuilderController controller = (QueryBuilderController) shapeController;
		controller.removeShapes(collShapes);
	}

	@Override
	public void setController(AbstractController newController) {
		super.setController(newController);
		dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY, ((QueryBuilderController) newController).getDropTargetListener());
	}

	@Override
	public synchronized DropTarget getDropTarget() {
		return dropTarget;
	}

	public List<Shape> getConstraints() {
		List<Shape> lstResult = new ArrayList<Shape>();
		for (Iterator<AbstractShapeModel.Layer> iterLayer = model.getVisibleLayers().iterator(); iterLayer.hasNext();) {
			AbstractShapeModel.Layer layer = iterLayer.next();
			for (Iterator<Shape> iterShapes = layer.getShapes().iterator(); iterShapes.hasNext();) {
				Shape shape = iterShapes.next();
				if (shape instanceof RelationConnector) {
					lstResult.add(shape);
				}
			}
		}
		return lstResult;
	}
}

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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @todo naming?! A ShapeModel seems to be a container holding Shapes, not the model of a Shape.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

public interface ShapeModel {
	/**
	 *
	 * @param shape
	 */
	public void addSelection(Shape shape);

	/**
	 *
	 * @param shapes
	 */
	public void addSelections(Collection<Shape> shapes);

	/**
	 *
	 * @return
	 */
	public Collection<Shape> getSelection();

	/**
	 *
	 * @return
	 */
	public boolean isMultiSelected();

	/**
	 *
	 */
	public void selectAll();

	/**
	 *
	 */
	public void deselectAll();

	/**
	 *
	 * @param p
	 * @return
	 */
	public Shape findSelectionAt(Point2D p);

	/**
	 *
	 */
	public void clear();

	/**
	 *
	 * @param shape
	 */
	public void removeSelection(Shape shape);

	/**
	 *
	 * @param shapes
	 */
	public void removeSelectionList(Collection<Shape> shapes);

	/**
	 *
	 * @param shape
	 */
	public void addShape(Shape shape);

	/**
	 *
	 * @param shapes
	 */
	public void addShapes(Collection<Shape> shapes);

	/**
	 *
	 * @param shape
	 */
	public void removeShape(Shape shape);

	/**
	 *
	 * @param shapes
	 */
	public void removeShapes(Collection<Shape> shapes);

	/**
	 *
	 * @param p
	 * @return
	 */
	public Shape findShapeAt(Point2D p);

	/**
	 *
	 * @param p
	 * @return
	 */
	public Shape findConnectableShapeAt(Point2D p);

	/**
	 *
	 * @param r
	 * @return
	 */
	public Collection<Shape> findShapesIn(Rectangle2D r);

	/**
	 *
	 * @param listener
	 */
	public void addShapeModelListener(ShapeModelListener listener);

	/**
	 *
	 * @param listener
	 */
	public void removeShapeModelListener(ShapeModelListener listener);

	/**
	 *
	 */
	public void beginUpdate();

	/**
	 *
	 */
	public void endUpdate();

	/**
	 *
	 * @param sName
	 * @param bUserLayer
	 * @return
	 */
	public AbstractShapeModel.Layer addLayer(String sName, boolean bUserLayer, int iOrder);

	/**
	 *
	 * @return
	 */
	public ArrayList<AbstractShapeModel.Layer> getVisibleLayers();

	/**
	 *
	 * @return
	 */
	public AbstractShapeModel.Layer getActiveLayer();

	/**
	 *
	 * @param layer
	 */
	public void setActiveLayer(AbstractShapeModel.Layer layer);

	/**
	 *
	 * @param sName
	 * @throws ShapeControllerException
	 */
	public void setActiveLayer(String sName) throws ShapeControllerException;

	/**
	 *
	 * @param viewer
	 */
	public void setView(ShapeViewer viewer);

	/**
	 *
	 * @return
	 */
	public ShapeViewer getView();

	/**
	 *
	 */
	public void fireModelChanged();

	/**
	 *
	 * @return
	 */
	public Rectangle2D getShapeDimension();

	/**
	 *
	 * @param iId
	 * @return
	 */
	Shape getShape(Integer iId);
}

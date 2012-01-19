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
 * Abstract shape model.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

package org.nuclos.client.gef;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public abstract class AbstractShapeModel implements ShapeModel {
	public class Layer implements Comparable<Layer> {
		protected String sName;
		protected Collection<Shape> shapes = null;
		protected boolean bActive;
		protected boolean bVisible;
		protected boolean bUserLayer;
		protected int iOrder;

		/**
		 *
		 * @param sName
		 * @param bUserLayer
		 */
		public Layer(String sName, boolean bUserLayer, int iOrder) {
			this.sName = sName;
			shapes = new LinkedList<Shape>();
			bActive = true;
			bVisible = true;
			this.bUserLayer = bUserLayer;
			this.iOrder = iOrder;
		}

		/**
		 *
		 * @param sName
		 * @param bActive
		 * @param bVisible
		 * @param bUserLayer
		 */
		public Layer(String sName, boolean bActive, boolean bVisible, boolean bUserLayer, int iOrder) {
			this.sName = sName;
			this.bActive = bActive;
			this.bVisible = bVisible;
			this.bUserLayer = bUserLayer;
			this.iOrder = iOrder;
		}

		/**
		 *
		 * @return
		 */
		public String getName() {
			return sName;
		}

		/**
		 *
		 * @param sName
		 */
		public void setName(String sName) {
			this.sName = sName;
		}

		/**
		 *
		 * @return
		 */
		public Collection<Shape> getShapes() {
			return shapes;
		}

		/**
		 *
		 * @return
		 */
		public boolean isActive() {
			return bActive;
		}

		/**
		 *
		 * @param bActive
		 */
		public void setActive(boolean bActive) {
			this.bActive = bActive;
		}

		/**
		 *
		 * @return
		 */
		public boolean isVisible() {
			return bVisible;
		}

		/**
		 *
		 * @param bVisible
		 */
		public void setVisible(boolean bVisible) {
			this.bVisible = bVisible;
		}

		/**
		 *
		 * @return
		 */
		public boolean isUserLayer() {
			return bUserLayer;
		}

		/**
		 *
		 * @param bUserLayer
		 */
		public void setUserLayer(boolean bUserLayer) {
			this.bUserLayer = bUserLayer;
		}

		/**
		 *
		 * @return
		 */
		public int getOrder() {
			return iOrder;
		}

		/**
		 *
		 * @param iOrder
		 */
		public void setOrder(int iOrder) {
			this.iOrder = iOrder;
		}

		@Override
        public int compareTo(Layer o) {
			return new Integer(iOrder).compareTo(new Integer(o.getOrder()));
		}
	}

	public static final int MODELSTATE_READY = 1;
	public static final int MODELSTATE_UPDATE = 2;

	protected LinkedList<Layer> layers = new LinkedList<Layer>();
	protected LinkedList<Shape> selection = new LinkedList<Shape>();
	protected Vector<ShapeModelListener> modelListener = new Vector<ShapeModelListener>();
	protected Layer activeLayer = null;
	protected ShapeViewer view;

	protected int state = MODELSTATE_READY;

	/**
	 *
	 */
	public AbstractShapeModel(ShapeViewer view) {
		addLayer("Connectors", false, 1);
		setActiveLayer(addLayer("Default", true, 2));
		this.view = view;
	}

	/**
	 *
	 * @param sName
	 * @param bUserLayer
	 * @return
	 */
	@Override
    public Layer addLayer(String sName, boolean bUserLayer, int iOrder) {
		Layer layer = new Layer(sName, bUserLayer, iOrder);

		if (bUserLayer) {
			layers.addFirst(layer);
		}
		else {
			layers.add(new Layer(sName, bUserLayer, iOrder));
		}
		return layer;
	}

	/**
	 *
	 * @return
	 */
	@Override
    public ArrayList<Layer> getVisibleLayers() {
		ArrayList<Layer> coll = new ArrayList<Layer>();
		for (Iterator<Layer> i = layers.iterator(); i.hasNext();) {
			Layer layer = i.next();
			if (layer.isVisible()) {
				coll.add(layer);
			}
		}
		return coll;
	}

	/**
	 *
	 * @return
	 */
	@Override
    public Layer getActiveLayer() {
		return activeLayer;
	}

	/**
	 *
	 * @param layer
	 */
	@Override
    public void setActiveLayer(Layer layer) {
		activeLayer = layer;
	}

	/**
	 *
	 * @param sName
	 * @throws ShapeControllerException
	 */
	@Override
    public void setActiveLayer(String sName) throws ShapeControllerException {
		for (Iterator<Layer> i = layers.iterator(); i.hasNext();) {
			Layer layer = i.next();
			if (layer.getName().equals(sName)) {
				activeLayer = layer;
				return;
			}
		}
		activeLayer = null;
		throw new ShapeControllerException("Layer '" + sName + "' not exists");
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	@Override
    public Shape findSelectionAt(Point2D p) {
		Iterator<Shape> i = selection.iterator();
		Shape result = null;

		while (i.hasNext()) {
			Shape shape = i.next();
			if (shape.isPointInside(p)) {
				result = shape;
			}
		}
		return result;
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	@Override
    public Shape findShapeAt(Point2D p) {
		List<Layer> visibleLayers = getVisibleLayers();
		Collections.reverse(visibleLayers);

		for (Iterator<Layer> iLayer = visibleLayers.iterator(); iLayer.hasNext();) {
			Layer layer = iLayer.next();
			for (Iterator<Shape> i = layer.getShapes().iterator(); i.hasNext();) {
				Shape shape = i.next();
				if (shape.isPointInside(p)) {
					return shape;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	@Override
    public Shape findConnectableShapeAt(Point2D p) {
		for (Iterator<Layer> iLayer = getVisibleLayers().iterator(); iLayer.hasNext();) {
			Layer layer = iLayer.next();
			for (Iterator<Shape> i = layer.getShapes().iterator(); i.hasNext();) {
				Shape shape = i.next();
				if (shape.isPointInside(p) && shape.isConnectable()) {
					return shape;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param r
	 * @return
	 */
	@Override
    public Collection<Shape> findShapesIn(Rectangle2D r) {
		Collection<Shape> coll = new Vector<Shape>();
		List<Layer> visibleLayers = getVisibleLayers();
		Collections.reverse(visibleLayers);

		for (Iterator<Layer> iLayer = visibleLayers.iterator(); iLayer.hasNext();) {
			Layer layer = iLayer.next();
			for (Iterator<Shape> i = layer.getShapes().iterator(); i.hasNext();) {
				Shape shape = i.next();
				if (shape.isInside(r)) {
					coll.add(shape);
				}
			}
		}
		return coll;
	}

	/**
	 *
	 * @param shape
	 */
	@Override
    public void addSelection(Shape shape) {
		selection.add(shape);
		shape.setSelection(true);
		fireSelectionChanged(shape);
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
    public void addSelections(Collection<Shape> shapes) {
		selection.addAll(shapes);
		for (Iterator<Shape> iterator = shapes.iterator(); iterator.hasNext();) {
			Shape shape = iterator.next();
			shape.setSelection(true);
		}
		fireMultiSelectionChanged(shapes);
	}

	/**
	 *
	 * @param shape
	 */
	@Override
    public void removeSelection(Shape shape) {
		if (shape.isSelected()) {
			shape.setSelection(false);
			shape.beforeDelete();
			selection.remove(shape);
			fireSelectionChanged(shape);
		}
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
    public void removeSelectionList(Collection<Shape> shapes) {
		for (Iterator<Shape> iterator = shapes.iterator(); iterator.hasNext();) {
			Shape shape = iterator.next();
			shape.beforeDelete();
			removeSelection(shape);
		}
		fireMultiSelectionChanged(shapes);
	}

	@Override
    public void clear() {
		for (Iterator<Layer> i = layers.iterator(); i.hasNext();) {
			Layer layer = i.next();
			layer.getShapes().clear();
		}
		fireModelChanged();
	}

	/**
	 *
	 * @param shape
	 */
	@Override
    public void addShape(Shape shape) {
		getActiveLayer().getShapes().add(shape);
		shape.setView(getView());
		fireModelChanged();
		if (shape.isSelected()) {
			fireSelectionChanged(shape);
		}
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
    public void addShapes(Collection<Shape> shapes) {
		getActiveLayer().getShapes().addAll(shapes);
		fireModelChanged();
	}

	/**
	 *
	 */
	@Override
    public void deselectAll() {
		for (Iterator<Shape> iterator = selection.iterator(); iterator.hasNext();) {
			Shape shape = iterator.next();
			shape.setSelection(false);
		}
		selection.clear();
		fireSelectionChanged(null);
	}

	/**
	 *
	 * @return
	 */
	@Override
    public Collection<Shape> getSelection() {
		return selection;
	}

	/**
	 *
	 * @return
	 */
	@Override
    public boolean isMultiSelected() {
		return selection.size() > 1;
	}

	/**
	 *
	 * @param shape
	 */
	@Override
    public void removeShape(Shape shape) {
		for (Iterator<Layer> i = layers.iterator(); i.hasNext();) {
			Layer layer = i.next();
			if (layer.getShapes().contains(shape)) {
				shape.beforeDelete();
				layer.getShapes().remove(shape);
				return;
			}
		}
		fireShapeDeleted(shape);
	}

	/**
	 *
	 * @param shapes
	 */
	@Override
    public void removeShapes(Collection<Shape> shapes) {
		for (Iterator<Layer> i = layers.iterator(); i.hasNext();) {
			Layer layer = i.next();
			for (Iterator<Shape> j = shapes.iterator(); j.hasNext();) {
				Shape shape = j.next();
				if (layer.getShapes().contains(shape)) {
					shape.beforeDelete();
					layer.getShapes().remove(shape);
				}
			}
		}
		fireShapesDeleted(shapes);
	}

	/**
	 *
	 */
	public void removeAllShapes() {
		for (Iterator<Layer> iter = layers.iterator(); iter.hasNext();) {
			Layer layer = iter.next();
			Object[] shapes = layer.getShapes().toArray();
			for (int i = 0; i < shapes.length; i++) {
				Shape shape = (Shape) shapes[i];
				shape.beforeDelete();
			}
			layer.getShapes().clear();
		}
		fireModelChanged();
	}

	/**
	 *
	 */
	@Override
    public void selectAll() {
		for (Iterator<Layer> iLayer = getVisibleLayers().iterator(); iLayer.hasNext();) {
			Layer layer = iLayer.next();
			selection.addAll(layer.getShapes());
			for (Iterator<Shape> i = layer.getShapes().iterator(); i.hasNext();) {
				Shape shape = i.next();
				shape.setSelection(true);
			}
		}
		fireModelChanged();
	}

	/**
	 *
	 */
	@Override
    public void beginUpdate() {
		state = MODELSTATE_UPDATE;
	}

	/**
	 *
	 */
	@Override
    public void endUpdate() {
		if (state != MODELSTATE_READY) {
			fireModelChanged();
			state = MODELSTATE_READY;
		}
	}

	/**
	 *
	 * @param listener
	 */
	@Override
    public void addShapeModelListener(ShapeModelListener listener) {
		modelListener.add(listener);
	}

	/**
	 *
	 * @param listener
	 */
	@Override
    public void removeShapeModelListener(ShapeModelListener listener) {
		modelListener.remove(listener);
	}

	/**
	 *
	 */
	@Override
	public void fireModelChanged() {
		Iterator<ShapeModelListener> i = modelListener.iterator();
		while (i.hasNext()) {
			ShapeModelListener l = i.next();
			l.modelChanged();
		}
	}

	/**
	 *
	 */
	public void fireSelectionChanged(Shape shape) {
		Iterator<ShapeModelListener> i = modelListener.iterator();
		while (i.hasNext()) {
			ShapeModelListener l = i.next();
			l.selectionChanged(shape);
		}
	}

	/**
	 *
	 */
	public void fireMultiSelectionChanged(Collection<Shape> list) {
		Iterator<ShapeModelListener> i = modelListener.iterator();
		while (i.hasNext()) {
			ShapeModelListener l = i.next();
			l.multiSelectionChanged(list);
		}
	}

	/**
	 *
	 */
	public void fireShapeDeleted(Shape shape) {
		Iterator<ShapeModelListener> i = modelListener.iterator();
		while (i.hasNext()) {
			ShapeModelListener l = i.next();
			l.shapeDeleted(shape);
		}
	}

	/**
	 *
	 */
	public void fireShapesDeleted(Collection<Shape> list) {
		Iterator<ShapeModelListener> i = modelListener.iterator();
		while (i.hasNext()) {
			ShapeModelListener l = i.next();
			l.shapesDeleted(list);
		}
	}

	/**
	 *
	 * @param view
	 */
	@Override
    public void setView(ShapeViewer view) {
		this.view = view;
	}

	/**
	 *
	 * @return
	 */
	@Override
    public ShapeViewer getView() {
		return this.view;
	}

	/**
	 *
	 * @return
	 */
	@Override
    public Rectangle2D getShapeDimension() {
		Rectangle2D result = new Rectangle2D.Double();

		for (Iterator<Layer> layerIter = getVisibleLayers().iterator(); layerIter.hasNext();) {
			Layer l = layerIter.next();
			for (Iterator<Shape> shapeIter = l.getShapes().iterator(); shapeIter.hasNext();) {
				Shape shape = shapeIter.next();
				result.add(shape.getDimension());
			}
		}
		return result;
	}

	/**
	 *
	 * @param iId
	 * @return
	 */
	@Override
    public Shape getShape(Integer iId) {
		for (Iterator<Layer> layerIter = getVisibleLayers().iterator(); layerIter.hasNext();) {
			Layer l = layerIter.next();
			for (Iterator<Shape> shapeIter = l.getShapes().iterator(); shapeIter.hasNext();) {
				Shape shape = shapeIter.next();
				if (shape.getId() == iId.intValue()) {
					return shape;
				}
			}
		}
		return null;
	}
}

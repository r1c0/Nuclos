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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JDesktopPane;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;

import org.apache.log4j.Logger;
import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.gef.shapes.ConnectionPoint;
import org.nuclos.client.gef.shapes.ContainerShape;
import org.nuclos.client.ui.Errors;

/**
 * Abstract controller.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class AbstractController implements MouseListener, MouseMotionListener, KeyListener {
	
	private static final Logger LOG = Logger.getLogger(AbstractController.class);

	/**
	 * (Value for iDragMode) Indicates that no drag action is active
	 */
	public static final int DRAG_NONE = 0;
	/**
	 * (Value for iDragMode) Indicates that the user moves a shape
	 */
	public static final int DRAG_TRANSLATE = 1;
	/**
	 * (Value for iDragMode) Indicates that the user resizes a shape
	 */
	public static final int DRAG_SCALE = 2;
	/**
	 * (Value for iDragMode) Indicates that the user spawns a rubberband for shape selection
	 */
	public static final int DRAG_RUBBERBAND = 4;

	/**
	 * (Value for iMouseMode) selection is done if the user clicks with the left mouse button
	 */
	public static final int MOUSE_SELECTION = 1;
	/**
	 * (Value for iMouseMode) a shape insertion is done if the user clicks with the left mouse button
	 */
	public static final int MOUSE_INSERT_SHAPE = 2;
	/**
	 * (Value for iMouseMode) a connector insertion is done if the user clicks with the left mouse button
	 */
	public static final int MOUSE_INSERT_CONNECTOR = 4;

	/**
	 * Indicates that left mouse button is down
	 */
	protected boolean mouseDown = false;

	/**
	 * Indicates which action occurred when the user clicks the left mouse button
	 */
	protected int iMouseMode = MOUSE_SELECTION;

	/**
	 * Indicates which action occurred when the user drags the mouse pointer (moving with pressed mouse button)
	 */
	protected int iDragMode = DRAG_NONE;

	/**
	 * Always remember the point where the user clicked first
	 */
	protected Point mouseDownPoint = new Point();
	/**
	 * The current drag position
	 */
	protected Point mouseDragPoint = new Point();
	/**
	 * The delta point while dragging
	 */
	protected Point mouseDeltaPoint = new Point();

	protected AbstractViewer viewer = null;
	protected ShapeModel model = null;

	/**
	 * Temporarily used to detect a shape hit by a drag operation
	 */
	protected Shape lastMoveHitShape = null;
	/**
	 * Temporarily used to detect a connector hit by a drag operation
	 */
	protected AbstractConnector lastConnectorHit = null;
	protected int lastConnectorHitPoint = 0;

	protected int deltaX, deltaY;

	/**
	 * Reference to a class which is used to instantiate a shape while in insert mode
	 */
	protected Class<?> selectedTool = null;

	protected JPopupMenu popupMenu = new JPopupMenu();

	protected int iResizeDirection = -1;
	boolean bResizeX = true;
	boolean bResizeY = true;
	boolean bMove = true;

	/**
	 * @param viewer
	 */
	public AbstractController(AbstractViewer viewer) {
		viewer.setController(this);
		this.viewer = viewer;
		this.model = viewer.getModel();
		this.iMouseMode = MOUSE_SELECTION;
		this.iDragMode = DRAG_NONE;
		this.selectedTool = null;
	}

	/**
	 * Gets the associated view
	 *
	 * @return
	 */
	public AbstractViewer getViewer() {
		return viewer;
	}

	/**
	 * Sets a new view
	 *
	 * @param viewer
	 */
	public void setViewer(AbstractShapeViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Gets the associated model which is controlled by this controller
	 *
	 * @return
	 */
	public ShapeModel getModel() {
		return model;
	}

	/**
	 * Sets the associated model which is controlled by this controller
	 *
	 * @param model
	 */
	public void setModel(ShapeModel model) {
		this.model = model;
	}

	/**
	 * Gets the current drag mode
	 *
	 * @return
	 * @see org.nuclos.client.gef.AbstractController#iDragMode
	 */
	public int getDragMode() {
		return iDragMode;
	}

	/**
	 * Sets the current drag mode
	 *
	 * @param iDragMode
	 * @see org.nuclos.client.gef.AbstractController#iDragMode
	 */
	public void setDragMode(int iDragMode) {
		this.iDragMode = iDragMode;
	}

	/**
	 * Rasterizes a screen point by the model's grid option
	 *
	 * @param p
	 * @return
	 * @see AbstractShapeViewer#bSnapGrid
	 */
	protected Point snapGridPoint(Point p) {
		int dX = p.x;
		int dY = p.y;
		return snapGridPoint(dX, dY);
	}

	/**
	 * Same as above
	 *
	 * @param dX
	 * @param dY
	 * @return
	 */
	protected Point snapGridPoint(int dX, int dY) { // Same as above
		Point result = new Point();
		int gridX = (int) viewer.getGridX();
		int gridY = (int) viewer.getGridX();

		result.setLocation(dX, dY);
		if (viewer.isSnapGrid()) {
			result.setLocation(((dX + gridX / 2) / gridX) * gridX, ((dY + gridY / 2) / gridY) * gridY);
		}
		return result;
	}

	/**
	 * This method connects a connector with a connectable shape (while dragging)
	 *
	 * @param connector the connector to be connected to a shape
	 * @param iPoint		indicator for start or end point which is dragged by the user
	 * @param p				 the resulting connection point if connection has been done
	 * @return true if the connector has been succesfully connected to a shape
	 */
	protected boolean connectShape(AbstractConnector connector, int iPoint, Point p) {
		boolean bResult = false;
		Point connPos = new Point();
		int iWhich = -1;

		Connectable shape = (Connectable) lastMoveHitShape;
		if ((shape != null) && ((iWhich = shape.isInsideConnector(p, connPos)) >= 0)) {
			p.setLocation(connPos);
			if (iPoint == AbstractConnector.STARTPOINT) {
				if (!connector.isConnectionAllowed((Shape) shape, AbstractConnector.STARTPOINT, iWhich)) {
					bResult = false;
				}
				else {
					ConnectionPoint cp = new ConnectionPoint((Shape) shape, iWhich);
					if (iWhich < 0) {
						try {
							LOG.error("Startpunkt ist f\u00e4lschlicherweise < 0 (" + iWhich + ", connectShape())");
						}
						catch (Exception e) {
							LOG.warn("connectShape failed: " + e, e);
						}
						iWhich = 0;
					}
					connector.setSourceConnection(cp);
					bResult = true;
				}
			}
			else if (iPoint == AbstractConnector.ENDPOINT) {
				if (!connector.isConnectionAllowed((Shape) shape, AbstractConnector.ENDPOINT, iWhich)) {
					bResult = false;
				}
				else {
					ConnectionPoint cp = new ConnectionPoint((Shape) shape, iWhich);
					if (iWhich < 0) {
						try {
							LOG.error("Endpunkt ist f\u00e4lschlicherweise < 0 (" + iWhich + ", connectShape())");
						}
						catch (Exception e) {
							LOG.warn("connectShape failed: " + e, e);
						}
						iWhich = 0;
					}
					connector.setDestinationConnection(cp);
					bResult = true;
				}
			}
		}
		else {
			if (iPoint == AbstractConnector.STARTPOINT) {
				if (connector != null)
					connector.setSourceConnection(null);
			}
			else if (iPoint == AbstractConnector.ENDPOINT) {
				if (connector != null)
					connector.setDestinationConnection(null);
			}
		}
		return bResult;
	}

	/**
	 * Hilights the connection points of a shape while dragging or moving the mouse cursor over it
	 *
	 * @param p
	 * @return true if a screen update should be performed
	 */
	protected boolean checkConnectableShapes(Point p) {
		boolean bResult = false;
		Shape hitShape = null;
		// check if there is a connectable shape at point p
		if ((hitShape = model.findConnectableShapeAt(p)) != null) {
			bResult = true;
			if (hitShape != lastMoveHitShape) {
				if (lastMoveHitShape != null) { // toggle visibility of connection points
					lastMoveHitShape.setConnectionPointsVisible(false);
				}
				hitShape.setConnectionPointsVisible(true);
			}
		}
		else if (lastMoveHitShape != null) {
			lastMoveHitShape.setConnectionPointsVisible(false);
		}

		lastMoveHitShape = hitShape;
		return bResult;
	}

	/**
	 * This method handles the dragging of a connector
	 *
	 * @param shape
	 * @param mouseDeltaPoint
	 */
	protected void dragConnector(Shape shape, Point mouseDeltaPoint) {
		AbstractConnector connector = (AbstractConnector) shape;
		int iWhich = connector.hitTest(mouseDragPoint);
		switch (iWhich) {
			case AbstractConnector.HIT_STARTPOINT:
				connector.movePoint(AbstractConnector.STARTPOINT, mouseDeltaPoint);
				break;
			case AbstractConnector.HIT_ENDPOINT:
				connector.movePoint(AbstractConnector.ENDPOINT, mouseDeltaPoint);
				break;
			case AbstractConnector.HIT_LINE:
				connector.move(mouseDeltaPoint);
				break;
		}
	}

	/**
	 * Method is called when the user drags the mouse over the view
	 *
	 * @param e
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (getMouseMode() == MOUSE_INSERT_CONNECTOR) {
			mouseDraggedInsertConnector(e);
		}
		else {
			switch (iDragMode) {
				case DRAG_TRANSLATE:
					mouseDraggedTranslate(e);
					break;
				case DRAG_SCALE:
					mouseDragPoint.setLocation(snapGridPoint(mouseDragPoint));
					mouseDraggedScale(e);
					break;
				case DRAG_RUBBERBAND:
					mouseDraggedRubberband(e);
					break;
			}
		}
	}

	/**
	 * Method is called while dragging a newly inserted connector
	 *
	 * @param e
	 */
	protected void mouseDraggedInsertConnector(MouseEvent e) {
		double dZoom = viewer.getZoom();
		deltaX = (int) (e.getX() / dZoom) - mouseDragPoint.x;
		deltaY = (int) (e.getY() / dZoom) - mouseDragPoint.y;
		mouseDeltaPoint.setLocation(deltaX, deltaY);
		Point pe = new Point(e.getX(), e.getY());

		switch (iDragMode) {
			case DRAG_SCALE:
				if (lastConnectorHit != null) {
					Point connectionPoint = new Point(mouseDragPoint.x, mouseDragPoint.y);
					if (checkConnectableShapes(pe) && connectShape(lastConnectorHit, AbstractConnector.ENDPOINT, connectionPoint)) {
						lastConnectorHit.setEndPoint(connectionPoint);
					}
					else {
						lastConnectorHit.movePoint(AbstractConnector.ENDPOINT, mouseDeltaPoint);
					}
				}
				viewer.repaint();
				break;
		}
		mouseDragPoint.setLocation(e.getX() / dZoom, e.getY() / dZoom);
	}

	/**
	 * Method is called while dragging in translation mode
	 *
	 * @param e
	 */
	protected void mouseDraggedTranslate(MouseEvent e) {
		double dZoom = viewer.getZoom();
		for (Iterator<Shape> selection = model.getSelection().iterator(); selection.hasNext();) {
			Shape shape = selection.next();
			if (shape instanceof AbstractConnector && model.getSelection().size() == 1) {
				Point pe = new Point((int) (e.getX() / dZoom), (int) (e.getY() / dZoom));
				deltaX = pe.x - mouseDragPoint.x;
				deltaY = pe.y - mouseDragPoint.y;
				mouseDeltaPoint.setLocation(deltaX, deltaY);

				AbstractConnector connector = (AbstractConnector) shape;
				checkConnectableShapes(pe);
				Point connectionPoint = new Point(mouseDragPoint.x, mouseDragPoint.y);
				switch (lastConnectorHitPoint) {
					case AbstractConnector.HIT_STARTPOINT:
						if (checkConnectableShapes(pe) && connectShape(connector, AbstractConnector.STARTPOINT, connectionPoint)) {
							connector.setStartPoint(connectionPoint);
						}
						else {
							connector.setSourceConnection(null);
							if (connector.getStartPoint().distance(pe) > 3) {
								connector.setStartPoint(pe);
							}
							else {
								connector.movePoint(AbstractConnector.STARTPOINT, mouseDeltaPoint);
							}
						}
						break;
					case AbstractConnector.HIT_ENDPOINT:
						if (checkConnectableShapes(pe) && connectShape(connector, AbstractConnector.ENDPOINT, connectionPoint)) {
							connector.setEndPoint(connectionPoint);
						}
						else {
							connector.setDestinationConnection(null);
							if (connector.getEndPoint().distance(pe) > 3) {
								connector.setEndPoint(pe);
							}
							else {
								connector.movePoint(AbstractConnector.ENDPOINT, mouseDeltaPoint);
							}
						}
						break;
					case AbstractConnector.HIT_LINE:
						connector.removeConnections();
						connector.move(mouseDeltaPoint);
						break;
				}
				mouseDragPoint.setLocation(pe);
			}
			else {
				Point pe = snapGridPoint((int) (Math.round(e.getX() / dZoom)),
						(int) (Math.round(e.getY() / dZoom)));
				mouseDragPoint.setLocation(snapGridPoint(mouseDragPoint));
				deltaX = pe.x - mouseDragPoint.x;
				deltaY = pe.y - mouseDragPoint.y;
				mouseDeltaPoint.setLocation(deltaX, deltaY);

				if (shape.move(mouseDeltaPoint)) {
					;
				}
				mouseDragPoint.setLocation(pe);
			}
			model.fireModelChanged();
		}
		viewer.repaint();
	}

	/**
	 * Method is called while dragging in resize mode
	 *
	 * @param e
	 */
	protected void mouseDraggedScale(MouseEvent e) {
		double dZoom = viewer.getZoom();
		double dX = 0d, dY = 0d, dW = 0d, dH = 0d;
		double dNewX = 0d, dNewY = 0d, dNewW = 0d, dNewH = 0d;

		Point pe = snapGridPoint((int) (e.getX() / dZoom), (int) (e.getY() / dZoom));
		deltaX = pe.x - mouseDragPoint.x;
		deltaY = pe.y - mouseDragPoint.y;
		mouseDeltaPoint.setLocation(deltaX, deltaY);

		bResizeX = bResizeY = false;

		for (Iterator<Shape> selection = model.getSelection().iterator(); selection.hasNext();) {
			Shape shape = selection.next();
			Rectangle2D r = shape.getDimension();
			Rectangle2D newBounds = new Rectangle2D.Double();
			Extents2D eMin = shape.getMinimumSize();
			Extents2D eMax = shape.getMaximumSize();

			dNewX = r.getX();
			dNewY = r.getY();
			dNewW = r.getWidth();
			dNewH = r.getHeight();

			newBounds.setRect(r);

			switch (iResizeDirection) {
				case AbstractShape.RESIZE_NW:	//move(deltaX,deltaY); resize(-deltaX, -deltaY);
					dX = r.getX() + deltaX;
					dY = r.getY() + deltaY;
					dW = r.getWidth() - deltaX;
					dH = r.getHeight() - deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_N: // move(0d, deltaY); resize(0d, -deltaY);
					dX = r.getX();
					dY = r.getY() + deltaY;
					dW = r.getWidth();
					dH = r.getHeight() - deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_NE: // move(0d, deltaY); resize(deltaX, -deltaY);
					dX = r.getX();
					dY = r.getY() + deltaY;
					dW = r.getWidth() + deltaX;
					dH = r.getHeight() - deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_E: // resize(deltaX, 0d);
					dX = r.getX();
					dY = r.getY();
					dW = r.getWidth() + deltaX;
					dH = r.getHeight();

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_SE: // resize(deltaX, deltaY);
					dX = r.getX();
					dY = r.getY();
					dW = r.getWidth() + deltaX;
					dH = r.getHeight() + deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_S: // resize(0d, deltaY);
					dX = r.getX();
					dY = r.getY();
					dW = r.getWidth();
					dH = r.getHeight() + deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_SW: // move(deltaX, 0d); resize(-deltaX, deltaY);
					dX = r.getX() + deltaX;
					dY = r.getY();
					dW = r.getWidth() - deltaX;
					dH = r.getHeight() + deltaY;

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
				case AbstractShape.RESIZE_W: // move(deltaX, 0d); resize(-deltaX, 0d);
					dX = r.getX() + deltaX;
					dY = r.getY();
					dW = r.getWidth() - deltaX;
					dH = r.getHeight();

					if (dX >= 0d && dW <= eMax.getWidth() && dW >= eMin.getWidth()) {
						bResizeX = true;
						dNewX = dX;
						dNewW = dW;
					}
					if (dY >= 0d && dH <= eMax.getHeight() && dH >= eMin.getHeight()) {
						bResizeY = true;
						dNewY = dY;
						dNewH = dH;
					}
					newBounds.setRect(dNewX, dNewY, dNewW, dNewH);
					shape.setDimension(newBounds);
					break;
			}
		}

		mouseDragPoint.setLocation(bResizeX ? pe.getX() : mouseDragPoint.getX(),
				bResizeY ? pe.getY() : mouseDragPoint.getY());
		if (bResizeX || bResizeY) {
			viewer.repaint();
		}
	}

	/**
	 * Method is called while in rubberband mode
	 *
	 * @param e
	 */
	protected void mouseDraggedRubberband(MouseEvent e) {
		double dZoom = viewer.getZoom();
		deltaX = (int) (e.getX() / dZoom) - mouseDragPoint.x;
		deltaY = (int) (e.getY() / dZoom) - mouseDragPoint.y;
		mouseDeltaPoint.setLocation(deltaX, deltaY);

		viewer.drawRubberBand(mouseDownPoint.getX(), mouseDownPoint.getY(), mouseDragPoint.getX(), mouseDragPoint.getY());
		mouseDragPoint.setLocation(e.getX() / dZoom, e.getY() / dZoom);
	}

	/**
	 * Method is called when the user moves the mouse cursor inside the view
	 *
	 * @param e
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		double dZoom = viewer.getZoom();
		boolean bUpdate = false;
		Point pe = new Point((int) (e.getX() / dZoom), (int) (e.getY() / dZoom));
		Shape hitShape = model.findShapeAt(pe);

		if (getMouseMode() == MOUSE_INSERT_CONNECTOR) {
			if (hitShape != null) {
				if (hitShape != lastMoveHitShape) {
					if (lastMoveHitShape != null) {
						lastMoveHitShape.setConnectionPointsVisible(false);
						bUpdate = true;
					}
					hitShape.setConnectionPointsVisible(true);
					bUpdate = true;
				}
			}
			else if (lastMoveHitShape != null) {
				lastMoveHitShape.setConnectionPointsVisible(false);
				bUpdate = true;
			}

			lastMoveHitShape = hitShape;
			if (bUpdate) {
				viewer.repaint();
			}
		}
		else if (getMouseMode() == MOUSE_SELECTION) {
			if (hitShape != null && hitShape.isResizeable()) {
				setDragMode(DRAG_SCALE);
				int iWhich = hitShape.isInsideResizer(pe);
				switch (iWhich) {
					case AbstractShape.RESIZE_NW:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_N:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_NE:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_E:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_SE:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_S:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_SW:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
						break;
					case AbstractShape.RESIZE_W:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
						break;
					default:
						viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						setDragMode(DRAG_NONE);
						setMouseMode(MOUSE_SELECTION);
				}
				iResizeDirection = iWhich;

				if (hitShape != lastMoveHitShape) {
					if (lastMoveHitShape != null) {
						lastMoveHitShape.setResizePointsVisible(false);
						bUpdate = true;
					}
					hitShape.setResizePointsVisible(true);
					bUpdate = true;
				}
			}
			else {
				if (lastMoveHitShape != null) {
					lastMoveHitShape.setResizePointsVisible(false);
					bUpdate = true;
				}
				if (getDragMode() == DRAG_SCALE) {
					viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					setDragMode(DRAG_NONE);
					setMouseMode(MOUSE_SELECTION);
				}
			}

			lastMoveHitShape = hitShape;
			if (bUpdate) {
				viewer.repaint();
			}
		}
	}

	/**
	 * Method is called when the user clicks AND releases a mouse button
	 *
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (popupMenu != null && popupMenu.getComponentCount() > 0 && e.getButton() == MouseEvent.BUTTON3) {
			popupMenu.show(viewer, e.getX(), e.getY());
		}
		int count = e.getClickCount();
		if(count > 1) {
			Shape hitShape = model.findShapeAt(mouseDownPoint);
			
			if(hitShape instanceof ContainerShape) {
				DefaultShapeViewer viewer = (DefaultShapeViewer)e.getSource();
				JRootPane root = viewer.getRootPane();
				((ContainerShape)hitShape).doubleClicked((JDesktopPane)root.getParent().getParent());
			}
		}
	}

	/**
	 * Method is called when the mouse cursor enters the view
	 *
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Method is called when the mouse cursor exits the view
	 *
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Method is called when the user clicks but not yet releases a mouse button
	 *
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		double dZoom = viewer.getZoom();
		Point pe = new Point((int) (e.getX() / dZoom), (int) (e.getY() / dZoom));
		mouseDownPoint.setLocation(pe);
		mouseDragPoint.setLocation(mouseDownPoint);
		mouseDown = true;
		bResizeX = bResizeY = true;
		bMove = true;
		viewer.requestFocusInWindow();

		if (!e.isConsumed() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
			switch (iMouseMode) {
				case MOUSE_SELECTION:
					mousePressedSelection(e);
					break;
				case MOUSE_INSERT_SHAPE:
					mousePressedInsertShape(e);
					break;
				case MOUSE_INSERT_CONNECTOR:
					mousePressedInsertConnector(e);
					break;
			}
		}
	}

	/**
	 * Method is called when the user clicks a mouse button while in selection mode
	 *
	 * @param e
	 */
	protected void mousePressedSelection(MouseEvent e) {
		deltaX = deltaY = 0;
		Shape hitShape = model.findShapeAt(mouseDownPoint);

		// Is there a shape we hit with this click?
		if (hitShape != null) {
			// Yep there is (at least) one shape
			if (hitShape.isSelectable()) {
				if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 1) {
					// Shift not pressed
					if (!hitShape.isSelected() && hitShape.isSelectable()) {
						model.deselectAll();
						model.addSelection(hitShape);
					}
				}
				else {
					// Shift pressed
					if (hitShape.isSelected()) {
						model.removeSelection(hitShape);
					}
					else {
						model.addSelection(hitShape);
					}
				}
			}

			if (hitShape.isMoveable() || hitShape.isResizeable()) {
				if (hitShape instanceof AbstractConnector) {
					AbstractConnector connector = (AbstractConnector) hitShape;
					lastConnectorHit = connector;
					int hitTest = connector.hitTest(mouseDownPoint);
					lastConnectorHitPoint = hitTest;
					if (hitTest >= AbstractConnector.HIT_STARTPOINT && hitTest <= AbstractConnector.HIT_ENDPOINT) {
						viewer.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						if (hitTest == AbstractConnector.HIT_STARTPOINT) {
							connector.setStartPoint(mouseDownPoint);
						}
						else {
							connector.setEndPoint(mouseDownPoint);
						}
					}
					iDragMode = DRAG_TRANSLATE;
				}
				else {	// all other shapes but AbstractConnector
					lastConnectorHit = null;
					iResizeDirection = hitShape.isInsideResizer(mouseDownPoint);
					if (iResizeDirection >= 0) {
						iDragMode = DRAG_SCALE;
					}
					else {
						iDragMode = DRAG_TRANSLATE;
//						viewer.setCursor(new Cursor(Cursor.MOVE_CURSOR));
					}
				}
			}
		}
		else {
			// We hit the background
			if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 1) {
				model.deselectAll();
			}
			iDragMode = DRAG_RUBBERBAND;
			viewer.resetRubberBand();
		}
		viewer.repaint();
	}

	/**
	 * Method is called when the user clicks a mouse button while in insert shape mode
	 *
	 * @param e
	 */
	protected void mousePressedInsertShape(MouseEvent e) {
		double dZoom = viewer.getZoom();
		if (selectedTool != null) {
			Shape shape = null;
			try {
				shape = (Shape) selectedTool.newInstance();
				shape.afterCreate();
			}
			catch (InstantiationException ex) {
				LOG.error("mousePressedInsertShape failed: " + ex, ex);
			}
			catch (IllegalAccessException ex) {
				LOG.error("mousePressedInsertShape failed: " + ex, ex);
			}

			shape.setView(viewer);
			Point pe = snapGridPoint((int) (e.getX() / dZoom) - (int) shape.getDimension().getWidth() / 2,
					(int) (e.getY() / dZoom) - (int) shape.getDimension().getHeight() / 2);
			shape.setLocation(pe);
			shape.setSelection(true);
			setMouseMode(MOUSE_SELECTION);
			setDragMode(DRAG_TRANSLATE);
			model.addShape(shape);
			if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 1) {
				model.deselectAll();
			}
			model.addSelection(shape);
			viewer.repaint();
		}
	}

	/**
	 * Method is called when the user clicks a mouse button while in insert connector mode
	 *
	 * @param e
	 */
	protected void mousePressedInsertConnector(MouseEvent e) {
		double dZoom = viewer.getZoom();
		if (selectedTool != null) {
			AbstractConnector shape = null;
			try {
				shape = (AbstractConnector) selectedTool.newInstance();
				shape.afterCreate();
			}
			catch (InstantiationException ex) {
				Errors.getInstance().showExceptionDialog(viewer, ex.getMessage(), ex);
			}
			catch (IllegalAccessException ex) {
				Errors.getInstance().showExceptionDialog(viewer, ex.getMessage(), ex);
			}
			shape.setView(viewer);
			Point pe = new Point((int) (e.getX() / dZoom), (int) (e.getY() / dZoom));
			shape.setStartPoint(pe);
			shape.setEndPoint(pe);
			shape.setSelection(true);
			setDragMode(DRAG_SCALE);
			model.addShape(shape);
			model.deselectAll();
			model.addSelection(shape);
			lastConnectorHit = shape;

			Point connectionPoint = new Point(pe.x, pe.y);
			if (checkConnectableShapes(pe) && connectShape(lastConnectorHit, AbstractConnector.STARTPOINT, connectionPoint)) {
				lastConnectorHit.setStartPoint(connectionPoint);
			}

			lastConnectorHitPoint = AbstractConnector.ENDPOINT;
			viewer.repaint();
		}
	}

	/**
	 * Method is called when the user releases a mouse button
	 *
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (iDragMode == DRAG_RUBBERBAND) {
			Rectangle2D rubberBand = viewer.getRubberBand();
			if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 1) {
				model.deselectAll();
			}
			Collection<Shape> coll = model.findShapesIn(rubberBand);
			model.addSelections(coll);
			viewer.clearRubberBand();
			viewer.resetRubberBand();
		}
		else if (lastMoveHitShape != null) {
			lastMoveHitShape.setConnectionPointsVisible(false);
		}

		mouseDown = false;
		iDragMode = DRAG_NONE;
		iMouseMode = MOUSE_SELECTION;
		viewer.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		viewer.repaint();
	}

	/**
	 * Method is called when the user presses a key on the keyboard
	 *
	 * @param e
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			model.removeShapes(model.getSelection());
			viewer.repaint();
		}
		else if (e.getKeyCode() == KeyEvent.VK_PLUS && e.isShiftDown()) {
			ConnectionPoint cp = null;
			if ((lastConnectorHit != null) && ((cp = lastConnectorHit.getDestinationConnection()) != null)) {
				lastConnectorHit.incTargetPoint(cp);
				viewer.repaint();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_PLUS) {
			ConnectionPoint cp = null;
			if ((lastConnectorHit != null) && ((cp = lastConnectorHit.getSourceConnection()) != null)) {
				lastConnectorHit.incTargetPoint(cp);
				viewer.repaint();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_MINUS && e.isShiftDown()) {
			ConnectionPoint cp = null;
			if ((lastConnectorHit != null) && ((cp = lastConnectorHit.getDestinationConnection()) != null)) {
				lastConnectorHit.decTargetPoint(cp);
				viewer.repaint();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
			ConnectionPoint cp = null;
			if ((lastConnectorHit != null) && ((cp = lastConnectorHit.getSourceConnection()) != null)) {
				lastConnectorHit.decTargetPoint(cp);
				viewer.repaint();
			}
		}
	}

	/**
	 * @param e
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * @param e
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Sets the mouse mode
	 *
	 * @param mode
	 * @see org.nuclos.client.gef.AbstractController#iMouseMode
	 */
	public void setMouseMode(int mode) {
		iMouseMode = mode;
	}

	/**
	 * Gets the mouse mode
	 *
	 * @return
	 * @see org.nuclos.client.gef.AbstractController#iMouseMode
	 */
	public int getMouseMode() {
		return iMouseMode;
	}

	/**
	 * @return
	 */
	public Class<?> getSelectedTool() {
		return selectedTool;
	}

	/**
	 * @param cls
	 */
	public void setSelectedTool(Class<?> cls) {
		selectedTool = cls;
	}

	public void setPopupMenu(JPopupMenu menu) {
		popupMenu = menu;
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

}


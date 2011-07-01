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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPopupMenu;

import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.gef.shapes.ConnectionPoint;
import org.nuclos.client.ui.Errors;

/**
 * Abstract shape controller.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class AbstractShapeController extends AbstractController {
	/**
	 * Always remember the point where the user clicked first
	 */
	protected Point2D mouseDownPoint = new Point2D.Double();
	/**
	 * The current drag position
	 */
	protected Point2D mouseDragPoint = new Point2D.Double();
	/**
	 * The delta point while dragging
	 */
	protected Point2D mouseDeltaPoint = new Point2D.Double();

	protected double deltaX, deltaY;

	/**
	 *
	 * @param viewer
	 */
	public AbstractShapeController(AbstractShapeViewer viewer) {
		super(viewer);
	}

	/**
	 * Rasterizes a screen point by the model's grid option
	 * @see AbstractShapeViewer#bSnapGrid
	 * @param p
	 * @return
	 */
	protected Point2D snapGridPoint(Point2D p) {
		double dX = p.getX();
		double dY = p.getY();
		return snapGridPoint(dX, dY);
	}

	/**
	 * Same as above
	 * @param dX
	 * @param dY
	 * @return
	 */
	protected Point2D snapGridPoint(double dX, double dY) { // Same as above
		Point2D result = new Point2D.Double();
		result.setLocation(dX, dY);
		if (viewer.isSnapGrid()) {
			result.setLocation(Math.round(dX / viewer.getGridX()) * viewer.getGridX(),
					Math.round(dY / viewer.getGridY()) * viewer.getGridY());
		}
		return result;
	}

	/**
	 * This method connects a connector with a connectable shape (while dragging)
	 * @param connector the connector to be connected to a shape
	 * @param iPoint indicator for start or end point which is dragged by the user
	 * @param p the resulting connection point if connection has been done
	 * @return true if the connector has been succesfully connected to a shape
	 */
	protected boolean connectShape(AbstractConnector connector, int iPoint, Point2D p) {
		boolean bResult = false;
		Point2D connPos = new Point2D.Double();
		int iWhich = -1;

		Connectable shape = (Connectable) lastMoveHitShape;
		if (shape != null && ((iWhich = shape.isInsideConnector(p, connPos)) >= 0)) {
			p.setLocation(connPos);
			if (iPoint == AbstractConnector.STARTPOINT) {
				if (!connector.isConnectionAllowed((Shape) shape, AbstractConnector.STARTPOINT, iWhich)) {
					bResult = false;
				}
				else {
					ConnectionPoint cp = new ConnectionPoint((Shape) shape, iWhich);
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
					connector.setDestinationConnection(cp);
					bResult = true;
				}
			}
		}
		else {
			if (iPoint == AbstractConnector.STARTPOINT) {
				connector.setSourceConnection(null);
			}
			else if (iPoint == AbstractConnector.ENDPOINT) {
				connector.setDestinationConnection(null);
			}
		}
		return bResult;
	}

	/**
	 * Hilights the connection points of a shape while dragging or moveing the mouse cursor over it
	 * @param p
	 * @return true if a screen update should be performed
	 */
	protected boolean checkConnectableShapes(Point2D p) {
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
	 * @param shape
	 * @param mouseDeltaPoint
	 */
	protected void dragConnector(Shape shape, Point2D mouseDeltaPoint) {
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
				connector.removeConnections();
				connector.move(mouseDeltaPoint);
				break;
		}
	}

	/**
	 * Method is called while dragging a newly inserted connector
	 * @param e
	 */
	@Override
	protected void mouseDraggedInsertConnector(MouseEvent e) {
		double dZoom = viewer.getZoom();
		deltaX = e.getX() / dZoom - mouseDragPoint.getX();
		deltaY = e.getY() / dZoom - mouseDragPoint.getY();
		mouseDeltaPoint.setLocation(deltaX, deltaY);
		Point2D pe = new Point2D.Double(e.getX(), e.getY());

		switch (iDragMode) {
			case DRAG_SCALE:
				Point2D connectionPoint = new Point2D.Double(mouseDragPoint.getX(), mouseDragPoint.getY());
				if (checkConnectableShapes(pe) && connectShape(lastConnectorHit, AbstractConnector.ENDPOINT, connectionPoint)) {
					lastConnectorHit.setEndPoint(connectionPoint);
				}
				else {
					lastConnectorHit.movePoint(AbstractConnector.ENDPOINT, mouseDeltaPoint);
				}
				viewer.repaint();
				break;
		}
		mouseDragPoint.setLocation(e.getX() / dZoom, e.getY() / dZoom);
	}

	/**
	 * Method is called while dragging in translation mode
	 * @param e
	 */
	@Override
	protected void mouseDraggedTranslate(MouseEvent e) {
		double dZoom = viewer.getZoom();
		for (Iterator<Shape> selection = model.getSelection().iterator(); selection.hasNext();) {
			Shape shape = selection.next();
			if (shape instanceof AbstractConnector && model.getSelection().size() == 1) {
				Point2D pe = new Point2D.Double(e.getX() / dZoom, e.getY() / dZoom);
				deltaX = pe.getX() - mouseDragPoint.getX();
				deltaY = pe.getY() - mouseDragPoint.getY();
				mouseDeltaPoint.setLocation(deltaX, deltaY);

				AbstractConnector connector = (AbstractConnector) shape;
				checkConnectableShapes(pe);
				Point2D connectionPoint = new Point2D.Double(mouseDragPoint.getX(), mouseDragPoint.getY());
				switch (lastConnectorHitPoint) {
					case AbstractConnector.HIT_STARTPOINT:
						if (checkConnectableShapes(pe) && connectShape(connector, AbstractConnector.STARTPOINT, connectionPoint)) {
							connector.setStartPoint(connectionPoint);
						}
						else {
							connector.setSourceConnection(null);
							if (connector.getStartPoint().distance(pe) > 3d) {
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
							if (connector.getEndPoint().distance(pe) > 3d) {
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
				Point2D pe = snapGridPoint(e.getX() / dZoom, e.getY() / dZoom);
				mouseDragPoint.setLocation(snapGridPoint(mouseDragPoint));
				deltaX = pe.getX() - mouseDragPoint.getX();
				deltaY = pe.getY() - mouseDragPoint.getY();
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
	 * @param e
	 */
	@Override
	protected void mouseDraggedScale(MouseEvent e) {
		double dZoom = viewer.getZoom();
		double dX = 0d, dY = 0d, dW = 0d, dH = 0d;
		double dNewX = 0d, dNewY = 0d, dNewW = 0d, dNewH = 0d;

		Point2D pe = snapGridPoint(e.getX() / dZoom, e.getY() / dZoom);
		deltaX = pe.getX() - mouseDragPoint.getX();
		deltaY = pe.getY() - mouseDragPoint.getY();
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
	 * @param e
	 */
	@Override
	protected void mouseDraggedRubberband(MouseEvent e) {
		double dZoom = viewer.getZoom();
		deltaX = e.getX() / dZoom - mouseDragPoint.getX();
		deltaY = e.getY() / dZoom - mouseDragPoint.getY();
		mouseDeltaPoint.setLocation(deltaX, deltaY);

		viewer.drawRubberBand(mouseDownPoint.getX(), mouseDownPoint.getY(), mouseDragPoint.getX(), mouseDragPoint.getY());
		mouseDragPoint.setLocation(e.getX() / dZoom, e.getY() / dZoom);
	}

	/**
	 * Method is called when the user clicks AND releases a mouse button
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (popupMenu != null && e.getButton() == MouseEvent.BUTTON3) {
			popupMenu.show(viewer, e.getX(), e.getY());
		}
	}

	/**
	 * Method is called when the mouse cursor enters the view
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Method is called when the mouse cursor exits the view
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Method is called when the user clicks but not yet releases a mouse button
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		double dZoom = viewer.getZoom();
		Point2D pe = new Point2D.Double(e.getX() / dZoom, e.getY() / dZoom);
		mouseDownPoint.setLocation(pe);
		mouseDragPoint.setLocation(mouseDownPoint);
		mouseDown = true;
		bResizeX = bResizeY = true;
		bMove = true;
		viewer.requestFocusInWindow();

		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
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
	 * @param e
	 */
	@Override
	protected void mousePressedSelection(MouseEvent e) {
		deltaX = deltaY = 0.0;
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
	 * @param e
	 */
	@Override
	protected void mousePressedInsertShape(MouseEvent e) {
		double dZoom = viewer.getZoom();
		if (selectedTool != null) {
			Shape shape = null;
			try {
				shape = (Shape) selectedTool.newInstance();
				shape.afterCreate();
			}
			catch (InstantiationException ex) {
				System.out.println(ex);
			}
			catch (IllegalAccessException ex) {
				System.out.println(ex);
			}

			shape.setView(viewer);
			Point2D pe = snapGridPoint(e.getX() / dZoom - shape.getDimension().getWidth() / 2d,
					e.getY() / dZoom - shape.getDimension().getHeight() / 2d);
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
	 * @param e
	 */
	@Override
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
			Point2D pe = new Point2D.Double(e.getX() / dZoom, e.getY() / dZoom);
			shape.setStartPoint(pe);
			shape.setEndPoint(pe);
			shape.setSelection(true);
			setDragMode(DRAG_SCALE);
			model.addShape(shape);
			model.deselectAll();
			model.addSelection(shape);
			lastConnectorHit = shape;

			Point2D connectionPoint = new Point2D.Double(pe.getX(), pe.getY());
			if (checkConnectableShapes(pe) && connectShape(lastConnectorHit, AbstractConnector.STARTPOINT, connectionPoint)) {
				lastConnectorHit.setStartPoint(connectionPoint);
			}

			lastConnectorHitPoint = AbstractConnector.ENDPOINT;
			viewer.repaint();
		}
	}

	/**
	 * Method is called when the user releases a mouse button
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
	 *
	 * @param e
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 *
	 * @param e
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Sets the mouse mode
	 * @see AbstractShapeController#iMouseMode
	 * @param mode
	 */
	@Override
	public void setMouseMode(int mode) {
		iMouseMode = mode;
	}

	/**
	 * Gets the mouse mode
	 * @see AbstractShapeController#iMouseMode
	 * @return
	 */
	@Override
	public int getMouseMode() {
		return iMouseMode;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Class<?> getSelectedTool() {
		return selectedTool;
	}

	/**
	 *
	 * @param cls
	 */
	@Override
	public void setSelectedTool(Class<?> cls) {
		selectedTool = cls;
	}

	@Override
	public void setPopupMenu(JPopupMenu menu) {
		popupMenu = menu;
	}

	@Override
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

}


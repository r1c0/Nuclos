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
package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * class for <b>UIDesktopUtils.java</b>...
 * <br>
 * @author <a href="mailto:stefan.geiling@novabit.de">Stefan Geiling</a>
 * @version	$Revision$
 */
public class UIDesktopUtils {
	protected static final int UNUSED_HEIGHT = 48;
	protected static int nextX; // Next X position
	protected static int nextY; // Next Y position
	protected static final int DEFAULT_OFFSETX = 24;
	protected static final int DEFAULT_OFFSETY = 24;
	protected static int offsetX = DEFAULT_OFFSETX;
	protected static int offsetY = DEFAULT_OFFSETY;

	protected UIDesktopUtils() {
	}

	// Layout all of the children of this container
	// so that they are tiled.
	public static void tileVertical(JDesktopPane desktop) {
		DesktopManager manager = desktop.getDesktopManager();
		if (manager == null) {
			// No desktop manager - do nothing
			return;
		}

		Component[] comps = desktop.getComponents();
		Component comp;
		int count = 0;

		// Count and handle only the internal frames
		for (int i = 0; i < comps.length; i++) {
			comp = comps[i];
			if (comp instanceof JInternalFrame && comp.isVisible()) {
				count++;
			}
		}

		if (count != 0) {
			double root = Math.sqrt(count);
			int rows = (int) root;
			int columns = count / rows;
			int spares = count - (columns * rows);

			Dimension paneSize = desktop.getSize();
			int columnWidth = paneSize.width / columns;

			// We leave some space at the bottom that doesn't get covered
			int availableHeight = paneSize.height - UNUSED_HEIGHT;
			int mainHeight = availableHeight / rows;
			int smallerHeight = availableHeight / (rows + 1);
			int rowHeight = mainHeight;
			int x = 0;
			int y = 0;
			int thisRow = rows;
			int normalColumns = columns - spares;

			for (int i = comps.length - 1; i >= 0; i--) {
				comp = comps[i];
				if (comp instanceof JInternalFrame && comp.isVisible()) {
					manager.setBoundsForFrame((JComponent) comp, x, y,
							columnWidth, rowHeight);
					y += rowHeight;
					if (--thisRow == 0) {
						// Filled the row
						y = 0;
						x += columnWidth;

						// Switch to smaller rows if necessary
						if (--normalColumns <= 0) {
							thisRow = rows + 1;
							rowHeight = smallerHeight;
						}
						else {
							thisRow = rows;
						}
					}
				}
			}
		}
	} // end of TileAll

	public static final void tileHorizontal(JDesktopPane desktop) {
		int _resizableCnt = 0;
		JInternalFrame _allFrames[] = desktop.getAllFrames();
		for (int _x = 0; _x < _allFrames.length; _x++) {
			JInternalFrame _frame = _allFrames[_x];
			if ((_frame.isVisible()) && (!_frame.isIcon())) {
				if (!_frame.isResizable()) {
					try {
						_frame.setMaximum(false);
					}
					catch (Exception _e) {
						// OK, to take no action here
					}
				}
				if (_frame.isResizable()) {
					_resizableCnt++;
				}
			}
		} // End for
		int _width = desktop.getBounds().width;
		int _height = arrangeIcons(desktop);
		if (_resizableCnt != 0) {
			int _fHeight = _height / _resizableCnt;
			int _yPos = 0;
			for (int _x = 0; _x < _allFrames.length; _x++) {
				JInternalFrame _frame = _allFrames[_x];
				if ((_frame.isVisible()) &&
						(_frame.isResizable()) &&
						(!_frame.isIcon())) {
					_frame.setSize(_width, _fHeight);
					_frame.setLocation(0, _yPos);
					_yPos += _fHeight;
				}
			} // End for
		}
	}

	public static final int arrangeIcons(JDesktopPane desktop) {
		int _iconCnt = 0;
		JInternalFrame _allFrames[] = desktop.getAllFrames();
		for (int _x = 0; _x < _allFrames.length; _x++) {
			if ((_allFrames[_x].isVisible()) && (_allFrames[_x].isIcon())) {
				_iconCnt++;
			}
		}
		int _height = desktop.getBounds().height;
		int _yPos = _height;
		if (_iconCnt != 0) {
			int _width = desktop.getBounds().width;
			int _xPos = 0;
			for (int _x = 0; _x < _allFrames.length; _x++) {
				JInternalFrame _frame = _allFrames[_x];
				if ((_frame.isVisible()) && (_frame.isIcon())) {
					Dimension _dim = _frame.getDesktopIcon().getSize();
					int _iWidth = _dim.width;
					int _iHeight = _dim.height;
					if (_yPos == _height) {
						_yPos = _height - _iHeight;
					}
					if ((_xPos + _iWidth > _width) && (_xPos != 0)) {
						_xPos = 0;
						_yPos -= _iHeight;
					}
					_frame.getDesktopIcon().setLocation(_xPos, _yPos);
					_xPos += _iWidth;
				} // End if
			} // End for
		} // End if
		return (_yPos);
	} // End method

	// Layout all of the children of this container
// so that they are cascaded.
	public static void cascadeAll(JDesktopPane desktop) {
		Component[] comps = desktop.getComponents();
		int count = comps.length;
		nextX = 0;
		nextY = 0;

		for (int i = count - 1; i >= 0; i--) {
			Component comp = comps[i];
			if (comp instanceof JInternalFrame && comp.isVisible()) {
				cascade(comp, desktop);
			}
		}
	}

	public static void minimizeAll(JDesktopPane desktop) {
		Component[] comps = desktop.getComponents();
		int count = comps.length;

		for (int i = count - 1; i >= 0; i--) {
			Component comp = comps[i];
			if (comp instanceof JInternalFrame && comp.isVisible()) {
				JInternalFrame jif = (JInternalFrame) comp;
				if (jif.isIconifiable()) {
					try {
						jif.setIcon(true);
					}
					catch (java.beans.PropertyVetoException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// Place a component so that it is cascaded
	// relative to the previous one
	protected static void cascade(Component comp, JDesktopPane desktop) {
		Dimension paneSize = desktop.getSize();
		int targetWidth = 3 * paneSize.width / 4;
		int targetHeight = 3 * paneSize.height / 4;

		DesktopManager manager = desktop.getDesktopManager();
		if (manager == null) {
			comp.setBounds(0, 0, targetWidth, targetHeight);
			return;
		}

		if (nextX + targetWidth > paneSize.width ||
				nextY + targetHeight > paneSize.height) {
			nextX = 0;
			nextY = 0;
		}

		manager.setBoundsForFrame((JComponent) comp, nextX, nextY,
				targetWidth, targetHeight);

		nextX += offsetX;
		nextY += offsetY;
	}
}

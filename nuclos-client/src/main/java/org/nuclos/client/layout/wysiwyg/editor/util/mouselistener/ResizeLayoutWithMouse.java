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
package org.nuclos.client.layout.wysiwyg.editor.util.mouselistener;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;

/**
 * This {@link MouseMotionListener} handles the resizing of the Layout with the Mouse.<br>
 * It does change the {@link Cursor}, and handles the Drag Event and modifies the {@link TableLayout}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ResizeLayoutWithMouse extends AbstractControlledListener implements MouseMotionListener, MouseListener {

	private boolean dragConsumed = true;
	private Point mouseoverHighlightLine = new Point(-1, -1);
	private LayoutCell cellToResize;

	private TableLayoutUtil tableLayoutUtil;
	public ResizeLayoutWithMouse(TableLayoutUtil tableLayoutUtil) {
		this.tableLayoutUtil = tableLayoutUtil;
	}

	/**
	 * This Method checks if a Action has to be performed
	 * @param actionToPerform the Action, resizing column, row or both (SE of the Cell)
	 * @return true if a action is to be performed
	 */
	boolean isActionToPerform(ActionToPerform actionToPerform) {
		boolean isActionToPerform = false;

		if (getActionToPerform() == ActionToPerform.PANEL_ALTER_COLUMN)
			isActionToPerform = true;
		if (getActionToPerform() == ActionToPerform.PANEL_ALTER_ROW)
			isActionToPerform = true;
		if (getActionToPerform() == ActionToPerform.PANEL_ALTER_SE)
			isActionToPerform = true;

		return isActionToPerform;
	}
	
	public boolean isPerfomingAction() {
		return isActionToPerform(getActionToPerform());
	}

	/**
	 * This Method does handle the Drag Event
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		/** checking if something has to be done and if its a valid target */
		if (isPerfomingAction() && cellToResize != null) {

			double value = 0;
			boolean column = false;

			/** getting the minimum size - does check if a component is contained */
			int minleft = getMinimumValueForColumn(cellToResize.getCellX(), true);
			int minbottom = getMinimumValueForRow(cellToResize.getCellY(), true);

			int actualWidth = e.getX() - cellToResize.getCellDimensions().x;
			int actualHeight = e.getY() - cellToResize.getCellDimensions().y;

			/**
			 * When the cell will be changed on the SE Corner Row and Column have to be modified
			 */
			
			/** calculating the new value for the row and changing it  */
			if (getActionToPerform() == ActionToPerform.PANEL_ALTER_ROW || getActionToPerform() == ActionToPerform.PANEL_ALTER_SE) {
				if (actualHeight < minbottom)
					value = minbottom;
				else
					value = actualHeight;

				tableLayoutUtil.modifyTableLayoutSizes(value, column, cellToResize, false);
			}
			/** calculating the new value for the column and changing it */
			if (getActionToPerform() == ActionToPerform.PANEL_ALTER_COLUMN || getActionToPerform() == ActionToPerform.PANEL_ALTER_SE) {
				if (actualWidth < minleft)
					value = minleft;
				else {
					value = actualWidth;
				}

				column = true;
				tableLayoutUtil.modifyTableLayoutSizes(value, column, cellToResize, false);
			}
			/** refresh the painting */
			((JPanel) tableLayoutUtil.getContainer()).updateUI();
		}
	}

	/** 
	 * This Method checks what cursor will be displayed.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Component c = e.getComponent();
		setActionToPerform(getActionToPerformForJPanel(e));

		switch (getActionToPerform()) {
			case PANEL_ALTER_COLUMN :
				c.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				break;
			case PANEL_ALTER_ROW :
				c.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				break;
			case PANEL_ALTER_SE :
				c.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				break;
			default :
				c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
		}
		if (isActionToPerform(getActionToPerform())) {
			if (e.getPoint() != null) {
				tableLayoutUtil.setCellForEditing(e.getPoint());
				cellToResize = tableLayoutUtil.getCellForEditing();
			} else {
				cellToResize = null;
			}
		}
	}

	/**
	 * Calculating the minimum/ preferred Size for this Column
	 * @param column the Column Number
	 * @param usePreferredSize if preferredSize should be used, otherwise minimumSize will be used
	 * @return the calculated Size for this Column 
	 */
	private int getMinimumValueForColumn(int column, boolean usePreferredSize) {
		int value = InterfaceGuidelines.MINIMUM_SIZE;

		if (column == 0)
			value = InterfaceGuidelines.MARGIN_LEFT;

		int minimumSize = Integer.MIN_VALUE;
		int preferredSize = Integer.MIN_VALUE;

		if (tableLayoutUtil.containesItemInColumn(column)) {
			preferredSize = tableLayoutUtil.getPreferredWidthForColumn(column);
			minimumSize = tableLayoutUtil.getMinimumWidthForColumn(column);

			if (value < minimumSize)
				value = minimumSize;
			if (value < preferredSize)
				value = preferredSize;

			if (usePreferredSize)
				value = preferredSize;
		}

		return value;
	}

	/**
	 * Calculating the minimum/ preferred Size for this Row
	 * @param row the Row Number
	 * @param usePreferredSize if preferredSize should be used, otherwise minimumSize will be used
	 * @return the calculated Size
	 */
	private int getMinimumValueForRow(int row, boolean usePreferredSize) {
		int value = InterfaceGuidelines.MINIMUM_SIZE;

		if (row == 0)
			value = InterfaceGuidelines.MARGIN_TOP;

		int minimumSize = Integer.MIN_VALUE;
		int preferredSize = Integer.MIN_VALUE;

		if (tableLayoutUtil.containesItemInRow(row)) {
			preferredSize = tableLayoutUtil.getPreferredHeightForRow(row);
			minimumSize = tableLayoutUtil.getMinimumWidthForColumn(row);

			if (value < minimumSize)
				value = minimumSize;
			if (value < preferredSize)
				value = preferredSize;

			if (usePreferredSize)
				value = preferredSize;
		}

		return value;
	}

	/**
	 * This Method checks if the MouseCursor is in the Range of something
	 * @param RANGE the Sensitivity to use {@link InterfaceGuidelines#SENSITIVITY}
	 * @param align_to the Point that should be checked
	 * @param align_what normally the MousePosition
	 * @return true if its in the near of the Point
	 */
	public boolean isInRangeOf(final int RANGE, int align_to, int align_what) {
		if (align_to >= align_what - RANGE && align_to <= align_what + RANGE)
			return true;
		else
			return false;
	}

	public void setMouseoverHighlightLineX(int x) {
		mouseoverHighlightLine.x = x;
	}

	public void setMouseoverHighlightLineY(int y) {
		mouseoverHighlightLine.y = y;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseoverHighlightLine = new Point(-1, -1);
		cellToResize = null;
	}

	/**
	 * This Method controls what action has do be performed
	 * @param e
	 * @return {@link ActionToPerform} with the action found
	 */
	public ActionToPerform getActionToPerformForJPanel(MouseEvent e) {
		if (e.getPoint() == null)
			return ActionToPerform.NOTHING_TO_DO;

		/** prevent resizing if mouse is outside layout */
		if (e.getPoint().x > tableLayoutUtil.getCalculatedLayoutWidth() + InterfaceGuidelines.SENSITIVITY)
			return ActionToPerform.NOTHING_TO_DO;
		if (e.getPoint().y > tableLayoutUtil.getCalculatedLayoutHeight() + InterfaceGuidelines.SENSITIVITY)
			return ActionToPerform.NOTHING_TO_DO;

		
		LayoutCell current = tableLayoutUtil.getLayoutCell(e.getPoint());
		int rightSide = current.getCellDimensions().x + current.getCellDimensions().width;
		int bottom = current.getCellDimensions().y + current.getCellDimensions().height;

		if (isInRangeOf(InterfaceGuidelines.SENSITIVITY, rightSide, e.getX()) && isInRangeOf(InterfaceGuidelines.SENSITIVITY, bottom, e.getY())) {
			if (e.getX() > InterfaceGuidelines.MARGIN_LEFT && e.getY() > InterfaceGuidelines.MARGIN_TOP) {
				return ActionToPerform.PANEL_ALTER_SE;
			}
		}

		if (isInRangeOf(InterfaceGuidelines.SENSITIVITY, rightSide, e.getX())) {
			if (e.getX() > InterfaceGuidelines.MARGIN_LEFT)
				return ActionToPerform.PANEL_ALTER_COLUMN;
		} else if (isInRangeOf(InterfaceGuidelines.SENSITIVITY, bottom, e.getY())) {
			if (e.getY() > InterfaceGuidelines.MARGIN_TOP) {
				return ActionToPerform.PANEL_ALTER_ROW;
			}
		}
		return ActionToPerform.NOTHING_TO_DO;
	}
}

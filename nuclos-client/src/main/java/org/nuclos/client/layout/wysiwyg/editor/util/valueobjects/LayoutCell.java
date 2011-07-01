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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Rectangle;

/**
 * This class represents a cell of the {@link TableLayout}.<br>
 * It contains the position, the calculated width and height.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutCell {

	private int cell_x = 0;
	private int cell_y = 0;
	private int cell2_x = 0;
	private int cell2_y = 0;
	private double cellWidth;
	private double cellHeight;

	private Rectangle cellDimensions = new Rectangle();

	public LayoutCell() {
	}

	/**
	 * 
	 * @param constraint The {@link TableLayoutConstraints} contains all relevant information for this {@link LayoutCell}.
	 */
	public LayoutCell(TableLayoutConstraints constraint) {
		cell_x = constraint.col1;
		cell_y = constraint.row1;
		cell2_x = constraint.col2;
		cell2_y = constraint.row2;
	}

	/**
	 * 
	 * @return Starting X Positon of the {@link LayoutCell}
	 */
	public int getCellX() {
		return cell_x;
	}

	/**
	 * 
	 * @return Starting Y Position of the {@link LayoutCell}
	 */
	public int getCellY() {
		return cell_y;
	}

	/**
	 * 
	 * @return The Ending X Position of the {@link LayoutCell} (may be the same as @see {@link #getCellX()})
	 */
	public int getCell2X() {
		return cell2_x;
	}

	/**
	 * 
	 * @return The Ending Y Position of the {@link LayoutCell} (may be the same as @see {@link #getCellY()})
	 */
	public int getCell2Y() {
		return cell2_y;
	}

	/**
	 * 
	 * @return Rectangle with the CellSize
	 */
	public Rectangle getCellDimensions() {
		return cellDimensions;
	}

	/**
	 * 
	 * @param x set Starting X Position
	 */
	public void setCellX(int x) {
		this.cell_x = x;
	}

	/**
	 * 
	 * @param y set Starting Y Position
	 */
	public void setCellY(int y) {
		this.cell_y = y;
	}

	/**
	 * 
	 * @param x set Ending X Position
	 */
	public void setCell2X(int x) {
		this.cell2_x = x;
	}

	/**
	 * 
	 * @param y set Ending Y Position
	 */
	public void setCell2Y(int y) {
		this.cell2_y = y;
	}

	/**
	 * 
	 * @return Position of the {@link LayoutCell} in the {@link TableLayout} on the TopSide (equal to {@link Rectangle#y})
	 */
	public int getTopSide() {
		return this.cellDimensions.y;
	}

	/**
	 * 
	 * @return Position of the {@link LayoutCell} in the {@link TableLayout} on the Bottom (equal to {@link Rectangle#y} + {@link Rectangle#height})
	 */
	public int getBottomSide() {
		return this.cellDimensions.y + this.cellDimensions.height;
	}

	/**
	 * 
	 * @return Position of the {@link LayoutCell} in the {@link TableLayout} on the left Side (equal to {@link Rectangle#x})
	 */
	public int getLeftSide() {
		return this.cellDimensions.x;
	}
	
	/**
	 * 
	 * @return Position of the {@link LayoutCell} in the {@link TableLayout} on the right Side (equal to {@link Rectangle#x} + {@link Rectangle#width})
	 */
	public int getRightSide() {
		return this.cellDimensions.x + this.cellDimensions.width;
	}

	/**
	 * Setting the CellSize
	 * @param cellDimensions
	 */
	public void setCellDimensions(Rectangle cellDimensions) {
		this.cellDimensions = cellDimensions;
	}

	/**
	 * Set X Position on {@link TableLayout}
	 * @param x
	 */
	public void setCellDimensionsX(int x) {
		this.cellDimensions.x = x;
	}

	/**
	 * Set Y Position on {@link TableLayout}
	 * @param y
	 */
	public void setCellDimensionsY(int y) {
		this.cellDimensions.y = y;
	}

	/**
	 * Set the width of the Cell
	 * @param width
	 */
	public void setCellDimensionsWidth(int width) {
		this.cellDimensions.width = width;
	}

	/**
	 * Set the Height of the Cell
	 * @param height
	 */
	public void setCellDimensionsHeight(int height) {
		this.cellDimensions.height = height;
	}

	/**
	 * Readable version of this {@link LayoutCell}
	 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append(cellDimensions.toString());
		b.append("\n");
		b.append("Starting Position: ");
		b.append(cell_x);
		b.append(" ");
		b.append(cell_y);
		b.append("\n");
		b.append("End Position: ");
		b.append(cell2_x);
		b.append(" ");
		b.append(cell2_y);
		b.append("\n");
		b.append("LayoutSize: \n");
		b.append("width: " + cellWidth);
		b.append("\n");
		b.append("height: " + cellHeight);
		return b.toString();
	}

	/**
	 * This returns not the actual width, it may return {@link TableLayoutConstants#FILL}, {@link TableLayoutConstants#MINIMUM} or {@link TableLayoutConstants#PREFERRED}<br>
	 * The real width (calculated value) is returned by {@link #getCellDimensions()}
	 * 
	 * @return double with the {@link TableLayoutConstraints} confirm width<br>
	 * @see TableLayoutConstants
	 */
	public double getCellWidth() {
		return cellWidth;
	}

	/**
	 * Setting the width of the {@link LayoutCell}
	 * 
	 * @param cellWidth
	 * @see TableLayoutConstants
	 */
	public void setCellWidth(double cellWidth) {
		this.cellWidth = cellWidth;
	}

	/**
	 * This returns not the actual height, it may return {@link TableLayoutConstants#FILL}, {@link TableLayoutConstants#MINIMUM} or {@link TableLayoutConstants#PREFERRED}<br>
	 * The real height (calculated value) is returned by {@link #getCellDimensions()}
	 * 
	 * @return double with the {@link TableLayoutConstraints} confirm height<br>
	 * @see TableLayoutConstants
	 */
	public double getCellHeight() {
		return cellHeight;
	}

	/**
	 * Setting the width of the {@link LayoutCell}
	 * 
	 * @param cellHeight
	 * @see TableLayoutConstants
	 */
	public void setCellHeight(double cellHeight) {
		this.cellHeight = cellHeight;
	}

	/**
	 * Compairing two {@link LayoutCell}
	 * 
	 * @return true if equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LayoutCell) {
			LayoutCell incoming = (LayoutCell) obj;
			boolean equal = true;

			if (incoming.cell2_x != cell2_x)
				equal = false;
			if (incoming.cell2_y != cell2_y)
				equal = false;
			if (incoming.cell_x != cell_x)
				equal = false;
			if (incoming.cell_y != cell_y)
				equal = false;

			if (incoming.cellHeight != cellHeight)
				equal = false;
			if (incoming.cellWidth != cellWidth)
				equal = false;

			return equal;
		}
		return false;
	}

}

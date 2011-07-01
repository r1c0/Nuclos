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
 * Shape viewer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

package org.nuclos.client.gef;

import java.awt.Color;
import java.awt.Graphics;

public interface ShapeViewer {
	/**
	 * Gets the current background color of the view
	 * @return
	 */
	public Color getBgColor();

	/**
	 * Sets the background color of the view
	 * @param color
	 */
	public void setBgColor(Color color);

	/**
	 * Paints the entire model
	 * @param gfx
	 */
	public void paint(Graphics gfx);

	/**
	 * Prints the current selected model
	 * @param gfx
	 */
	public void print(Graphics gfx);

	/**
	 * Prints the entire model
	 * @param gfx
	 */
	public void printAll(Graphics gfx);

	/**
	 * Enables or disables user modifications
	 * @param editable
	 */
	public void setEditable(boolean editable);

	/**
	 *
	 * @return
	 */
	public boolean isEditable();

	/**
	 *
	 * @return
	 */
	public ShapeModel getModel();

	/**
	 *
	 * @return
	 */
	public int getWidth();

	/**
	 *
	 * @return
	 */
	public int getHeight();

	/**
	 *
	 * @return
	 */
	public AbstractController getController();

	/**
	 *
	 * @param newController
	 */
	public void setController(AbstractController newController);

	/**
	 *
	 * @return
	 */
	public double getZoom();

	/**
	 *
	 */
	public void setZoom(double dValue);

	/**
	 *
	 * @return
	 */
	public double getGridX();

	/**
	 *
	 * @param dGridX
	 */
	public void setGridX(double dGridX);

	/**
	 *
	 * @return
	 */
	public double getGridY();

	/**
	 *
	 * @param dGridY
	 */
	public void setGridY(double dGridY);

	/**
	 *
	 * @return
	 */
	public boolean isSnapGrid();

	/**
	 *
	 * @param bSnapGrid
	 */
	public void setSnapGrid(boolean bSnapGrid);
}

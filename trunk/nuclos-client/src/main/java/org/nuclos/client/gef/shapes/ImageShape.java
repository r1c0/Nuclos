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
 * Image shape.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
package org.nuclos.client.gef.shapes;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;

public class ImageShape extends RectangularShape implements ImageObserver {

	protected Image image = null;
	protected double dImageWidth;
	protected double dImageHeight;

	public ImageShape() {
		this(0d, 0d, 0d, 0d);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 */
	public ImageShape(double dX, double dY, double dWidth, double dHeight) {
		super(dX, dY, dWidth, dHeight);
	}

	/**
	 *
	 * @param sImageURL
	 */
	public ImageShape(URL sImageURL) {
		this(0d, 0d, 0d, 0d);
		image = Toolkit.getDefaultToolkit().getImage(sImageURL);
		dImageWidth = image.getWidth(this);
		dImageHeight = image.getHeight(this);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 * @param sImageURL
	 */
	public ImageShape(double dX, double dY, double dWidth, double dHeight, URL sImageURL) {
		super(dX, dY, dWidth, dHeight);
		image = Toolkit.getDefaultToolkit().getImage(sImageURL);
		dImageWidth = image.getWidth(this);
		dImageHeight = image.getHeight(this);
	}

	/**
	 *
	 * @param sImageFileName
	 */
	public ImageShape(String sImageFileName) {
		this(0d, 0d, 0d, 0d);
		image = Toolkit.getDefaultToolkit().getImage(sImageFileName);
		dImageWidth = image.getWidth(this);
		dImageHeight = image.getHeight(this);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 * @param sImageFileName
	 */
	public ImageShape(double dX, double dY, double dWidth, double dHeight, String sImageFileName) {
		super(dX, dY, dWidth, dHeight);
		image = Toolkit.getDefaultToolkit().getImage(sImageFileName);
		dImageWidth = image.getWidth(this);
		dImageHeight = image.getHeight(this);
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		if (image != null) {
			int iX = (int) Math.max(((dimension.getWidth() - dImageWidth) / 2d), 0d);
			int iY = (int) Math.max(((dimension.getHeight() - dImageHeight) / 2d), 0d);
			gfx.drawImage(image, iX, iY, this);
		}
		super.paint(gfx);
	}

	/**
	 *
	 * @param img
	 * @param infoflags
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags,
			int x, int y, int width, int height) {
		return false;
	}
}

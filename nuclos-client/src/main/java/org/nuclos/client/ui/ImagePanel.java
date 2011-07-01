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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class ImagePanel extends JPanel {
	private final ImageIcon icon;

	public ImagePanel(Image image) {
		this(new ImageIcon(image));
	}

	public ImagePanel(ImageIcon icon) {
		this.icon = icon;
		this.setOpaque(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
//		paintOneTile(g);
		paintTiles(g);
//		paintStretched(g);
	}

	private void paintTiles(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		final Composite compositeOriginal = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
		final Rectangle rect = g.getClipBounds();
		int xMax = rect.x + rect.width;
		int yMax = rect.y + rect.height;
		for (int y = 0; y < yMax; y += icon.getIconHeight()) {
			for (int x = 0; x < xMax; x += icon.getIconWidth()) {
				g.drawImage(icon.getImage(), x, y, null);
			}
		}
		g2.setComposite(compositeOriginal);
	}

}  // class ImagePanel

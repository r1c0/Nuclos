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
/*
 * Created on 20.11.2009
 */
package org.nuclos.client.main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

import org.nuclos.client.NuclosIcons;

public class BackgroundImageDesktopPane extends JDesktopPane {
	private ImageIcon baseImage;
	private Image     fullSizeBackground;
	private Dimension fullSizeBackgroundSize;
	
	public BackgroundImageDesktopPane() {
		super();
		baseImage = NuclosIcons.getInstance().getDesktopBackgroundIcon();
		if(baseImage != null) {
			if(baseImage.getIconHeight() > 0 && baseImage.getIconWidth() > 0)
				setOpaque(false);
			else
				baseImage = null;
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if(baseImage == null) {
			super.paint(g);
			return;
		}
		
		Dimension d = getSize();
		if(fullSizeBackground == null || !d.equals(fullSizeBackgroundSize)) {
			fullSizeBackground = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
			Graphics offGraphics = fullSizeBackground.getGraphics();
			for(int x = 0; x < d.width; x += baseImage.getIconWidth())
				for(int y = 0; y < d.height; y += baseImage.getIconHeight())
					offGraphics.drawImage(baseImage.getImage(), x, y, this);
			fullSizeBackgroundSize = d;
		}
		
		Rectangle clip = g.getClipBounds();
		if(clip == null)
			clip = new Rectangle(0, 0, d.width, d.height);
		
		g.drawImage(fullSizeBackground, clip.x, clip.y, clip.x + clip.width, clip.y + clip.height,
			clip.x, clip.y, clip.x + clip.width, clip.y + clip.height, getBackground(), this);
		
		super.paint(g);
	}
}

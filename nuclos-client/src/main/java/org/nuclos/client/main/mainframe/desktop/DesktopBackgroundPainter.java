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
package org.nuclos.client.main.mainframe.desktop;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;

import org.apache.commons.httpclient.util.LangUtils;
import org.nuclos.client.synthetica.NuclosThemeSettings;

public class DesktopBackgroundPainter {
	
	public static final DesktopBackgroundPainter DEFAULT = new DesktopBackgroundPainter(null, null);
	public static final DesktopBackgroundPainter TRANSPARENT = new DesktopBackgroundPainter(null, null, true);

	private final Color backgroundColor;
	private final ImageIcon backgroundImage;
	private final boolean transparent;
	
	public DesktopBackgroundPainter(Color backgroundColor, ImageIcon backgroundImage) {
		this(backgroundColor, backgroundImage, false);
	}
	
	private DesktopBackgroundPainter(Color backgroundColor, ImageIcon backgroundImage, boolean transparent) {
		super();
		this.backgroundColor = backgroundColor;
		this.backgroundImage = backgroundImage;
		this.transparent = transparent;
	}

	public void paint(Graphics2D g, int width, int height) {
		if (!transparent) {
			g.setPaint(backgroundColor != null ? backgroundColor : NuclosThemeSettings.BACKGROUND_PANEL);
			g.fillRect(0, 0, width, height);
			
			if (backgroundImage != null) {
				final int icoHeight = backgroundImage.getIconHeight();
				final int icoWidth = backgroundImage.getIconWidth();
				for (int i = 0; i < width / icoWidth + 1; i++) {
					for (int j = 0; j < height / icoHeight + 1; j++) {
						g.drawImage(backgroundImage.getImage(), i * icoWidth, j * icoHeight, null);
					}
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DesktopBackgroundPainter) {
			return LangUtils.equals(((DesktopBackgroundPainter) obj).backgroundColor, this.backgroundColor) &&
				   LangUtils.equals(((DesktopBackgroundPainter) obj).backgroundImage, this.backgroundImage) &&
				   LangUtils.equals(((DesktopBackgroundPainter) obj).transparent, this.transparent);
		}
		return super.equals(obj);
	}
}

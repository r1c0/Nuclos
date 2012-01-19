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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * A panel displaying an image transparently (or "translucently"). The image is always centered in the component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo extends JComponent?
 */
public class TransparentImagePanel extends JPanel {

	private ImageIcon imgicon;
	private final AlphaComposite composite;

	/**
	 * @param imgicon the image to display
	 * @param fAlpha the alpha value specifying translucency (0..1).
	 */
	public TransparentImagePanel(ImageIcon imgicon, float fAlpha) {
		this.imgicon = imgicon;
		this.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fAlpha);
		this.setVisible(true);
		this.setOpaque(false);
	}
	
	public void setImage(ImageIcon imgicon) {
		this.imgicon = imgicon;
		//repaint();
	}

	/**
	 * This panel is completely transparent. Especially it doesn't consume any mouse [motion] events.
	 * @param x
	 * @param y
	 * @return false
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		this.paintImage(g);
	}

	private void paintImage(Graphics g) {
		if (imgicon != null) {
			// center the image in the panel:
			final Dimension dimPanelSize = this.getSize();
			final int iImageWidth = imgicon.getIconWidth();
			final int iImageHeight = imgicon.getIconHeight();
	
			final int x = (dimPanelSize.width - iImageWidth) / 2;
			final int y = (dimPanelSize.height - iImageHeight) / 2;
	
			final Graphics2D g2 = (Graphics2D) g;
	
			// remember original settings:
			final Composite compositeOriginal = g2.getComposite();
			g2.setComposite(composite);
	
			g2.drawImage(imgicon.getImage(), x, y, imgicon.getImageObserver());
	
			// restore original settings:
			g2.setComposite(compositeOriginal);
		}
	}
}  // class TransparentImagePanel

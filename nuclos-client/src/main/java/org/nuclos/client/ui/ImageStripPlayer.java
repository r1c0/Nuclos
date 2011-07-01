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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Component which plays a sequence of images in a strip ordered from the left to the right.
 * Each image in the sequence must be a square of the same size.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class ImageStripPlayer extends JComponent {
	private int size = 0;
	private int stages = 0;
	private int stagePlayed = -1;

	private final ImageIcon img;
	private Timer timer;

	public ImageStripPlayer(ImageIcon img) {
		this.img = img;
		size = img.getIconHeight();
		stages = img.getIconWidth() / size;
		Dimension dimSize = new Dimension(size, size);
		this.setMaximumSize(dimSize);
		this.setMinimumSize(dimSize);
		this.setPreferredSize(dimSize);

		//this.setOpaque(false);
		//this.setOpaque(true);
		//this.setBackground(Color.BLACK);
	}

	public void play(int iDelay) {
		Dimension dimParent = getParent().getSize();
		this.setLocation((dimParent.width - size) / 2, (dimParent.height - size) / 2);
		this.setVisible(true);

		timer = new Timer(iDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stagePlayed++;
				if(stagePlayed == stages) {
					stagePlayed = 0;
				}

				invalidate();
				repaint();
			}
		});
		timer.start();
	}

	public void stop() {
		if(timer != null) {
			timer.stop();
		}
	}

	@Override
	public void paint(Graphics g) {
		img.paintIcon(ImageStripPlayer.this, g, -stagePlayed*size, 0);
	}
}

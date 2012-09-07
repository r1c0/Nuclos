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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.StartIcons;
import org.nuclos.client.image.ImageScaler;
import org.nuclos.common.ApplicationProperties;

public class BackgroundPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(BackgroundPanel.class);

	private final Color bg = ApplicationProperties.getInstance().getLoginPanelBgColor(Color.WHITE);
	
	private final Image img512 = StartIcons.getInstance().getBigTransparentApplicationIcon512().getImage();
	
	// private final ImageIcon bgImg = new ImageIcon(img512.getScaledInstance(250, -1, java.awt.Image.SCALE_SMOOTH));
	private final ImageIcon bgImg = new ImageIcon(ImageScaler.scaleImage(img512, 250, -1));

	public BackgroundPanel() {
		super();
	}

	public BackgroundPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public BackgroundPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public BackgroundPanel(LayoutManager layout) {
		super(layout);
	}

	public void shake(final double initstep) {
		final Window w = SwingUtilities.getWindowAncestor(this);
		final Point point = w.getLocation();
		final int delay = 10;
		final double duration = 20;

		Runnable r = new Runnable() {
			@Override
            public void run() {
				for(int i = 0; i < duration; i++) {
					try {
						double step = initstep - ((initstep / duration) * i);
						move(w, new Point((int)(point.x - step), point.y));
						Thread.sleep(delay);
						move(w, point);
						Thread.sleep(delay);
						move(w, new Point((int)(point.x + step), point.y));
						Thread.sleep(delay);
						move(w, point);
						Thread.sleep(delay);
					} catch(Exception e) {
						LOG.error("shake failed: " + e, e);
					}
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}

	private void move(final Window w, final Point p) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
            public void run() {
				try {
					w.setLocation(p);
				}
				catch (Exception e) {
					LOG.error("BackgroundPanel.move: " + e, e);
				}					
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints oldRH = g2.getRenderingHints();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.setColor(bg);
		g2.fillRect(0, 0, getWidth(), getHeight());

		final int wIco = bgImg.getIconWidth();
		final int hIco = bgImg.getIconHeight();

		BufferedImage bi = new BufferedImage(wIco, hIco, BufferedImage.TYPE_INT_ARGB);
		Graphics gbi = bi.getGraphics();
		gbi.drawImage(bgImg.getImage(), 0, 0, null);
		gbi.dispose();

		// 50% opaque
		float[] scales = { 1f, 1f, 1f, 0.5f };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
	   	g2.drawImage(bi, rop, getWidth()-wIco/2, -(hIco*1/4));

		g2.setRenderingHints(oldRH);
		super.paint(g);
	}
}

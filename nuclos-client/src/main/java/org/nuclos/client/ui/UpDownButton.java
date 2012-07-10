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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import org.nuclos.client.synthetica.NuclosThemeSettings;

public class UpDownButton extends JButton implements MouseListener {

	private boolean up;
	
	private final int arrowWidth = 16;
	private final int width = 32;
	private final int height = 8;
	private final int border = 4;
	
	private boolean hover;
	
	private boolean gradientUp = false;
	
	private boolean rootPaneBG = false;
	
	public UpDownButton(boolean up) {
		this.up = up;
		this.setSize(width, border+height+border);
		this.setPreferredSize(new Dimension(width, border+height+border));
		this.addMouseListener(this);
	}
	
	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
		repaint();
	}

	public boolean isGradientUp() {
		return gradientUp;
	}

	public void setGradientUp(boolean gradientUp) {
		this.gradientUp = gradientUp;
		repaint();
	}

	public boolean isRootPaneBG() {
		return rootPaneBG;
	}

	public void setRootPaneBG(boolean rootPaneBG) {
		this.rootPaneBG = rootPaneBG;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Object renderingHint = g2
				.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		final Rectangle bounds = getBounds();
		
		final int w = arrowWidth;
		final int h = height;
		final int thickness = h/2;
		
		final int x = bounds.width/2-w/2;
		final int y = border;
		
		if (hover) {
			if (rootPaneBG) {
				Color transparentDefault = new Color(NuclosThemeSettings.BACKGROUND_PANEL.getRed(),
						NuclosThemeSettings.BACKGROUND_PANEL.getGreen(),
						NuclosThemeSettings.BACKGROUND_PANEL.getBlue(), 0);
				g2.setPaint(new GradientPaint(0, 0, 
					gradientUp?transparentDefault:NuclosThemeSettings.BACKGROUND_PANEL, 
							0, bounds.height, 
					gradientUp?NuclosThemeSettings.BACKGROUND_PANEL:transparentDefault, false));
			} else {
				Color transparentBGC3 = new Color(NuclosThemeSettings.BACKGROUND_COLOR3.getRed(),
						NuclosThemeSettings.BACKGROUND_COLOR3.getGreen(),
						NuclosThemeSettings.BACKGROUND_COLOR3.getBlue(), 0);
				g2.setPaint(new GradientPaint(0, 0, 
					gradientUp?transparentBGC3:NuclosThemeSettings.BACKGROUND_COLOR3, 
							0, bounds.height, 
					gradientUp?NuclosThemeSettings.BACKGROUND_COLOR3:transparentBGC3, false));
			}
			g2.fillRoundRect(0, gradientUp?0:-8, bounds.width, bounds.height+8, 12, 12);
		}
		
		final Polygon p = new Polygon();
		
		if (up) {
			p.addPoint(x+ 0, 	y+ h-thickness);
			p.addPoint(x+ w/2, 	y+ 0);
			p.addPoint(x+ w, 	y+ h-thickness);
			p.addPoint(x+ w, 	y+ h);
			p.addPoint(x+ w/2, 	y+ thickness);
			p.addPoint(x+ 0, 	y+ h);
		} else {
			p.addPoint(x+ 0, 	y+ 0);
			p.addPoint(x+ w/2, 	y+ h-thickness);
			p.addPoint(x+ w, 	y+ 0);
			p.addPoint(x+ w, 	y+ thickness);
			p.addPoint(x+ w/2, 	y+ h);
			p.addPoint(x+ 0, 	y+ h-thickness);
		}
		
		if (hover) {
			if (rootPaneBG) {
				g2.setColor(NuclosThemeSettings.BACKGROUND_ROOTPANE);
			} else {
				g2.setColor(NuclosThemeSettings.BACKGROUND_PANEL);
			}
		} else {
			if (rootPaneBG) {
				g2.setColor(NuclosThemeSettings.BACKGROUND_PANEL);
			} else {
				g2.setColor(NuclosThemeSettings.BACKGROUND_COLOR3);
			}
		}
		g2.fillPolygon(p);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				renderingHint);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		hover=false;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		hover=true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hover=false;
		repaint();
	}
	
}
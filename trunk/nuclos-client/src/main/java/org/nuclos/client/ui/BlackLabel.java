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
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nuclos.client.synthetica.NuclosThemeSettings;

public class BlackLabel extends JPanel {
	

	public static final int BORDER_DEFAULT_LEFT = 6;
	
	public static final int BORDER_DEFAULT_RIGHT = 6;
	
	private final JLabel label;
	private final JComponent content;
	
	public BlackLabel(JComponent content, String label) {
		this(content, createJLabel(label));
	}
	
	protected static JLabel createJLabel(String label) {
		JLabel result = new JLabel(label);
		result.setBorder(BorderFactory.createEmptyBorder(0, BORDER_DEFAULT_LEFT, 0, BORDER_DEFAULT_RIGHT));
		return result;
	}
	
	public BlackLabel(JComponent content, JLabel label) {
		super();
		this.content = content;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false);
		setBackground(new Color (0, 0, 0, 0));
		
		this.label = label;
		this.label.setOpaque(false);
		this.label.setForeground(Color.WHITE);
		
		this.content.setOpaque(false);
		
		add(this.label);
		add(this.content);
		
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		final int width = getWidth();
		final int height = getHeight();
		final int contentWidth = this.content.getWidth();
		
		if (width > 0 && height > 0) {
			Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
			
			final Point2D center = new Point2D.Float((width-contentWidth), (height/2));
			final float radius = (width-contentWidth)/2;
		    final Point2D focus = new Point2D.Float((width-contentWidth)/2*3, (height/2));
		    final float[] dist = {0.0f, 1.0f};
		    final Color[] colors = {NuclosThemeSettings.BACKGROUND_COLOR4, NuclosThemeSettings.BACKGROUND_COLOR3};
		    g2.setPaint(new RadialGradientPaint(center, radius, focus,
		                                        dist, colors, CycleMethod.NO_CYCLE));
		    g2.fillRoundRect(0, 0, width, height, 6, 6);
		    
		    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing); 
		}
		
		super.paint(g);
	}
	
	
}

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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.nuclos.client.synthetica.NuclosSyntheticaConstants;

public class StatusBarPanel extends JPanel {

	public StatusBarPanel() {
		super();

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(new Color(0,0,0,0));
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		final int fade = 4;

		final int width = getWidth();
		final int height = getHeight();

		if (width > 1 && height > fade) {
			Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setPaint(new GradientPaint(new Point(0, 0), NuclosSyntheticaConstants.BACKGROUND_DARKER,
										  new Point(0, fade), NuclosSyntheticaConstants.BACKGROUND_DARK));
			g2.fillRect(0, 0, width, height);

			final Point2D center = new Point2D.Float(width/2, fade+(width/3));
			final float radius = width/2;
		    final Point2D focus = new Point2D.Float(width/2, fade+(width/4));
		    final float[] dist = {0.0f, 1.0f};
		    final Color[] colors = {NuclosSyntheticaConstants.BACKGROUND_SPOT, NuclosSyntheticaConstants.BACKGROUND_DARK};
		    g2.setPaint(new RadialGradientPaint(center, radius, focus,
		                                        dist, colors, CycleMethod.NO_CYCLE));
		    g2.fillRect(0, fade, width, height-fade);

		    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
		}

		 super.paint(g2);
	}
}

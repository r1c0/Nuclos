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

package org.nuclos.client.ui.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jdesktop.swingx.painter.Painter;

public class PainterUtils {

	/** Workaround for the AbstractPainter bug. */
	public static <T> void paint(Painter<? super T> painter, Graphics2D g2d, T object, int width, int height) {
		Graphics2D scratch = (Graphics2D) g2d.create();
		try {
			painter.paint(scratch, object, width, height);
		} finally {
			scratch.dispose();
		}
	}

	/** Fills with the painter the given rectangle. For painting, the graphics context is translated,
	 * so that the graphics' origin corresponds to the rect's top-left corner.
	 */
	public static <T> void paint(Painter<? super T> painter, Graphics2D g2d, T object, Rectangle rect) {
		Graphics2D scratch = (Graphics2D) g2d.create();
		scratch.translate(rect.x, rect.y);
		try {
			painter.paint(scratch, object, rect.width, rect.height);
		} finally {
			scratch.dispose();
		}
	}
	
	public static class HeaderPainter implements Painter<Object> {
		
		private static Color DEFAULT_COLOR = new Color(0xe3e3e3);
		private Color color;
		
		public HeaderPainter() {
			this(null);
		}
		
		public HeaderPainter(Color color) {
			setColor(color);
		}
		
		public Color getColor() {
			return color;
		}
		
		public void setColor(Color color) {
			this.color = (color != null ? color : DEFAULT_COLOR);
		}
		
		@Override
		public void paint(Graphics2D g, Object object, int width, int height) {
			int y1 = 3;
			g.setPaint(new GradientPaint(width/2, 0, Color.WHITE, width/2, y1 * 2, color));
			g.fillRect(0, 0, width, y1);
			int y2 = height - 20;
			if (y2 > y1 + 5) {
				g.setPaint(color);
				g.fillRect(0, y1, width, y2);
			} else {
				y2 = y1;
			}
			g.setPaint(new GradientPaint(width/2, y2, color, width/2, height, Color.WHITE));
			g.fillRect(0, y2, width, height - y2);
			
			g.setColor(new Color(0x66ffffff, true));
			g.drawLine(0, 0, 0, height - 1);
			g.setColor(new Color(0x33999999, true));
			g.drawLine(width - 1, 0, width - 1, height - 1);
		}
	}
	
	public static class BorderPainter implements Painter<Object> {

		private Color lineColor;
		private int thickness;

		public BorderPainter(Color color, int thickness) {
			this.lineColor = color;
			this.thickness = thickness;
		}

		@Override
		public void paint(Graphics2D g, Object object, int width, int height) {
			Color oldColor = g.getColor();
			g.setColor(lineColor);
			for(int i = 0; i < thickness; i++) {
				g.drawRect(i, i, width - i - i - 1, height - i - i - 1);
			}
			g.setColor(oldColor);
		}
	}
}

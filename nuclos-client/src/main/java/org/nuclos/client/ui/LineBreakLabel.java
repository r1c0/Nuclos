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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedString;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.nuclos.common2.StringUtils;

public class LineBreakLabel extends JLabel {
	
	private int width;

	public LineBreakLabel(String text, int width) {
		super(text);
		this.width = width;
		setSize(getPreferredSize());
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawText(g2);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
	}
	
	private int drawText(Graphics2D g2) {
		final Font font = getFont();
		final FontMetrics metrics = getFontMetrics(font);
		final int iFontHeight = metrics.getHeight();
		final FontRenderContext frc = metrics.getFontRenderContext();
		if (g2 != null) {
			g2.setFont(font);
		}
		int height = 0;
		
		final String lines[] = getText().split("\\r?\\n");
		for (String line : lines) {
			if (StringUtils.looksEmpty(line)) {
				height += iFontHeight;
//				System.out.println("<--->");
			} else {
				int lastpos = 0;
				final LineBreakMeasurer measurer = new LineBreakMeasurer(new AttributedString(line, font.getAttributes()).getIterator(), frc);
				while (measurer.getPosition() < line.length()) {
					lastpos = measurer.getPosition();
					measurer.nextLayout(width);
					String s = line.substring(lastpos, measurer.getPosition());
//					System.out.println(s);
					if (g2 != null) {
						g2.drawString(s, 0, 0 + height + metrics.getAscent());
					}
				    height += iFontHeight;
				}
			}
		}
		
		return height;
	}
	
	@Override
	public Dimension getPreferredSize() {
		final Dimension result = new Dimension(width, drawText(null));
		return result;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getSize() {
		return getPreferredSize();
	}
	
}

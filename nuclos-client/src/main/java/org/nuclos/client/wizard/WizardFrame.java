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

package org.nuclos.client.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.pietschy.wizard.Wizard;

public class WizardFrame extends JPanel {
	
	final Wizard wiz;
	
	private final Color transparent = new Color(0, 0, 0, 0);
	private final Color shadow = new Color(40, 40, 40);
	
	int shadowRange = 7;
	int minBorder = 10;
	
	public WizardFrame(Wizard wiz) {
		super(new BorderLayout());
		this.wiz = wiz;
		this.wiz.setVisible(false);
		setOpaque(true);
		setBackground(transparent);
		add(this.wiz, BorderLayout.CENTER);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				adjustBorder();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				adjustBorder();
			}
			
		});
	}
	
	private void adjustBorder() {
		if (!wiz.isVisible()) wiz.setVisible(true);
		
		final int left = getWidth()/25 > minBorder ? getWidth()/25 : minBorder;
		final int top = getHeight()/25 > minBorder ? getHeight()/25 : minBorder;
		shadowRange = (left>top ? top : left) * 7 / 10;
		
		setBorder(BorderFactory.createEmptyBorder(top, left, top+shadowRange, left+shadowRange));
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		final int width = getWidth();
		final int height = getHeight();
		final Rectangle wizBounds = wiz.getBounds();
		
		g2.setColor(NuclosSyntheticaConstants.BACKGROUND_DARKER);
		g2.fillRect(0, 0, width, height);
		
		if (width > 0 && height > 0 && wizBounds.width > 0 && wizBounds.height > 0) {
			
			g2.setPaint(new RadialGradientPaint(wizBounds.x+wizBounds.width, wizBounds.y+shadowRange, shadowRange, 
				new float[]{0.0f, 1.0f},
				new Color[]{shadow, transparent}));
			g2.fillRect(wizBounds.x+wizBounds.width, wizBounds.y, shadowRange, shadowRange);
			
			g2.setPaint(new GradientPaint(wizBounds.x+wizBounds.width, wizBounds.y, shadow, wizBounds.x+wizBounds.width+shadowRange, wizBounds.y, transparent));
			g2.fillRect(wizBounds.x+wizBounds.width, wizBounds.y+shadowRange, shadowRange, wizBounds.height-shadowRange);
			
			g2.setPaint(new RadialGradientPaint(wizBounds.x+wizBounds.width, wizBounds.y+wizBounds.height, shadowRange, 
				new float[]{0.0f, 1.0f},
				new Color[]{shadow, transparent}));
			g2.fillRect(wizBounds.x+wizBounds.width, wizBounds.y+wizBounds.height, shadowRange, shadowRange);
			
			g2.setPaint(new GradientPaint(wizBounds.x, wizBounds.y+wizBounds.height, shadow, wizBounds.x, wizBounds.y+wizBounds.height+shadowRange, transparent));
			g2.fillRect(wizBounds.x+shadowRange, wizBounds.y+wizBounds.height, wizBounds.width-shadowRange, shadowRange);
			
			g2.setPaint(new RadialGradientPaint(wizBounds.x+shadowRange, wizBounds.y+wizBounds.height, shadowRange, 
				new float[]{0.0f, 1.0f},
				new Color[]{shadow, transparent}));
			g2.fillRect(wizBounds.x, wizBounds.y+wizBounds.height, shadowRange, shadowRange);
		}
		
		super.paint(g);
	}
	
	public static JComponent createFrameInScrollPane(Wizard wiz) {
		WizardFrame wf = new WizardFrame(wiz);
		
		JScrollPane result = new JScrollPane(wf);
		result.getHorizontalScrollBar().setUnitIncrement(20);
		result.getVerticalScrollBar().setUnitIncrement(20);
		result.setBorder(BorderFactory.createEmptyBorder());
		
		return result;
	}
	
}


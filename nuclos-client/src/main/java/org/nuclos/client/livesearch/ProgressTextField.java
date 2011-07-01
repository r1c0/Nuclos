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

package org.nuclos.client.livesearch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * A JTextField, which can additionally display a progress in the background
 */
public class ProgressTextField extends JTextField {
	private static final Color TOP_COLOR = new Color(0x95ff86);
	private static final Color MID_COLOR = new Color(0xd7ffd2);
	private static final Color BOT_COLOR = new Color(0x43c931);

	private static final Color TOP_BG_COLOR = new Color(0xfffb5f);
	private static final Color MID_BG_COLOR = new Color(0xfffb96);
	private static final Color BOT_BG_COLOR = new Color(0xffdc1f);

	private int currentProgress;
	private int maxProgress;

	private int currentBackgroundProgress;
	private int maxBackgroundProgress;

	public ProgressTextField(String text, int columns, int currentProgress, int maxProgress) {
	    super(text, columns);
	    this.currentProgress = currentProgress;
	    this.maxProgress = maxProgress;
	    
	    this.currentBackgroundProgress = 0;
	    this.maxBackgroundProgress = 0;
	    
	    putClientProperty("Synthetica.opaque", Boolean.FALSE); // ... somtimes I'd like to kill someone... :-/
	    setOpaque(false);
	    setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
    }


	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Dimension d = getSize();
		
		g2.setColor(Color.WHITE);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fillRoundRect(0, 0, d.width, d.height, 16, 16);
		
		int progressX = maxProgress > 0 ? (d.width * currentProgress / maxProgress) : 0;
		int bgProgressX = maxBackgroundProgress > 0 ? (d.width * currentBackgroundProgress / maxBackgroundProgress) : 0;
		int thirdHeight = d.height / 3;
		
		Paint oldPaint = g2.getPaint();
		
		if(bgProgressX > 0) {
			GradientPaint pt = new GradientPaint(
				0, 0, TOP_BG_COLOR,
				0, thirdHeight, MID_BG_COLOR);
			GradientPaint pb = new GradientPaint(
				0, d.height, BOT_BG_COLOR,
				0, thirdHeight, MID_BG_COLOR);

			g2.setPaint(pt);
			g2.fillRoundRect(0, 0, bgProgressX, d.height, 16, 16);
			//g2bi.setPaint(pb);
			//g2bi.fillRect(0, thirdHeight, bgProgressX, d.height);
		}
		
		if(progressX > 0) {
			GradientPaint pt = new GradientPaint(
				0, 0, TOP_COLOR,
				0, thirdHeight, MID_COLOR);
			GradientPaint pb = new GradientPaint(
				0, d.height, BOT_COLOR,
				0, thirdHeight, MID_COLOR);
	
			g2.setPaint(pt);
			g2.fillRoundRect(0, 0, progressX, d.height, 16, 16);
			//g2bi.setPaint(pb);
			//g2bi.fillRect(0, thirdHeight, progressX, d.height);
		}
		
		g2.setPaint(oldPaint);
		
		super.paint(g);
	}

	
	protected Rectangle getVisibleEditorRect() {
		Rectangle alloc = getBounds();
		if ((alloc.width > 0) && (alloc.height > 0)) {
			alloc.x = alloc.y = 0;
			Insets insets = getInsets();
			alloc.x += insets.left;
			alloc.y += insets.top;
			alloc.width -= insets.left + insets.right;
			alloc.height -= insets.top + insets.bottom;
			return alloc;
		}
		return null;
	}

	
	public int getCurrentProgress() {
    	return currentProgress;
    }

	public void setCurrentProgress(int currentProgress) {
    	this.currentProgress = currentProgress;
    	repaint();
    }

	public int getMaxProgress() {
    	return maxProgress;
    }

	public void setMaxProgress(int maxProgress) {
    	this.maxProgress = maxProgress;
    	repaint();
    }

	public int getCurrentBackgroundProgress() {
    	return currentBackgroundProgress;
    }

	public void setCurrentBackgroundProgress(int currentBackgroundProgress) {
    	this.currentBackgroundProgress = currentBackgroundProgress;
    	repaint();
    }

	public int getMaxBackgroundProgress() {
    	return maxBackgroundProgress;
    }

	public void setMaxBackgroundProgress(int maxBackgroundProgress) {
    	this.maxBackgroundProgress = maxBackgroundProgress;
    	repaint();
    }
}

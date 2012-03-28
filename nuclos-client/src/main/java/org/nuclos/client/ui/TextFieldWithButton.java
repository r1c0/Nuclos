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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.nuclos.client.ui.labeled.ILabeledComponentSupport;
import org.nuclos.client.ui.labeled.LabeledComboBox;

/**
 * 
 * TextField with inner button like a ComboBox
 *
 */
public abstract class TextFieldWithButton extends CommonJTextField {
	
	private Icon iconButton;
	
	private final Cursor curIcon = new Cursor(Cursor.DEFAULT_CURSOR);
	private final Cursor curNotEditable = curIcon;
	private final Cursor curDefault;
	
	private final ILabeledComponentSupport support;
	
	private ButtonState bs = ButtonState.NORMAL;
	
	private final int fadeWidthLeft = 7;
	
	public TextFieldWithButton(Icon iconButton, ILabeledComponentSupport support) {
		if (iconButton == null) {
			throw new IllegalArgumentException("iconButton must not be null!");
		}
		if (support == null) {
			throw new IllegalArgumentException();
		}
		
		this.support = support;
		this.iconButton = iconButton;
		this.curDefault = getCursor();
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, LabeledComboBox.DEFAULT_PREFERRED_SIZE.height));
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent me) {
				if (isButtonEnabled() && isMouseOverButton(me)) {
					TextFieldWithButton.this.setCursor(curIcon);
					if (bs != ButtonState.PRESSED) {
						bs = ButtonState.HOVER;
						TextFieldWithButton.this.repaint(getIconRectangleWithFades());
					}
				} else {
					TextFieldWithButton.this.setCursor(TextFieldWithButton.this.isEditable() ? curDefault : curNotEditable);
					bs = ButtonState.NORMAL;
					TextFieldWithButton.this.repaint(getIconRectangleWithFades());
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent me) {
				bs = ButtonState.NORMAL;
				TextFieldWithButton.this.repaint(getIconRectangleWithFades());
			}
			@Override
			public void mouseReleased(MouseEvent me) {
				if (isButtonEnabled() && isMouseOverButton(me)) {
					bs = ButtonState.NORMAL;
					TextFieldWithButton.this.repaint(getIconRectangleWithFades());
				}
			}
			@Override
			public void mousePressed(MouseEvent me) {
				if (isButtonEnabled() && isMouseOverButton(me)) {
					bs = ButtonState.PRESSED;
					TextFieldWithButton.this.repaint(getIconRectangleWithFades());
					buttonClicked();
				} 
			}
		});
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				TextFieldWithButton.this.setSelectionStart(0);
				TextFieldWithButton.this.setSelectionEnd(0);
			}
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}
	
	public abstract boolean isButtonEnabled();
	
	public abstract void buttonClicked();
	
	@Override
	public Color getBackground() {
		final Color colorDefault = super.getBackground();
		if (support == null) {
			return colorDefault;
		}
		final ColorProvider colorproviderBackground = support.getColorProvider();
		if (colorproviderBackground == null) {
			return colorDefault;			
		}
		return colorproviderBackground.getColor(colorDefault);
	}
	
	private boolean isMouseOverButton(MouseEvent me) {
		if (isSelectingText())
			return false; // hide button when selecting text...
		
		final Rectangle r = getIconRectangle();
		if (r.x <= me.getX() && me.getX() <= (r.x+r.width)) {
			return true;
		} else {
			return false;
		}
	}
	
	private Rectangle getIconRectangleWithFades() {
		final Rectangle r = getIconRectangle();
		
		if (fadeLeft()) {
			r.x = r.x - fadeWidthLeft;
			r.width = r.width + fadeWidthLeft;
		}
		
		return r;
	}
	
	private Rectangle getIconRectangle() {
		final Rectangle r = new Rectangle();
		
		final Dimension dimTextField = TextFieldWithButton.this.getSize();
		final ImageIcon ico = ButtonState.NORMAL.getImageIcon();
		r.x = dimTextField.width - ico.getIconWidth() - 2;
		r.y = (dimTextField.height - ico.getIconHeight()) / 2;
		r.width = ico.getIconWidth();
		r.height = ico.getIconHeight();
		
		return r;
	}
	
	protected boolean fadeLeft() {
		return true;
	}
	
	private boolean isSelectingText() {
		return getSelectionStart() != getSelectionEnd();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		final Rectangle r = getIconRectangle();
		
		if (!isSelectingText()) {
			if (fadeLeft()) {
				final Color bgColor = getBackground();
				g2d.setPaint(new GradientPaint(new Point(r.x-fadeWidthLeft, r.y), new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 0), new Point(r.x-1, r.y), bgColor));
				g2d.fillRect(r.x-fadeWidthLeft, r.y, fadeWidthLeft, r.height);
			}
			g2d.setColor(getBackground());
			g2d.fillRect(r.x, r.y, r.width, r.height);
		}
		g2d.drawImage(bs.getImageIcon().getImage(), r.x, r.y, null);
		
		int w = iconButton.getIconWidth();
		int h = iconButton.getIconHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics imgg = bi.getGraphics();
		iconButton.paintIcon(this, imgg, 0, 0);
		imgg.dispose();

		float[] scales = { 1f, 1f, 1f, (isSelectingText() ? 0.3f : (isButtonEnabled() ? 1f : 0.5f)) };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		g2d.drawImage(bi, rop, r.x, r.y);
	}
	
	private enum ButtonState {
		
		NORMAL(Icons.getInstance().getIconTextFieldButton()),
		HOVER(Icons.getInstance().getIconTextFieldButtonHover()),
		PRESSED(Icons.getInstance().getIconTextFieldButtonPressed());
		
		ImageIcon ii;
		
		ButtonState(ImageIcon ii) {
			this.ii = ii;
		}
		
		ImageIcon getImageIcon() {
			return ii;
		}
	}	
}
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
import java.awt.image.BufferedImage;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

/**
 * A JCheckBox with an additional "third" state. This class may become handy when it is needed to reflect the fact
 * that a checkbox neither in its selected nor in its unselected state.
 * This usually happens when common properties of several elements are displayed or, more technically,
 * when the <code>null</code> value is to be displayed.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */

public class TriStateCheckBox extends JCheckBox {

	private boolean lockModel;
	private boolean bUndefined;
	
	public TriStateCheckBox() {
		super();
		setModel(new ToggleButtonModel() {

			@Override
			public void setSelected(boolean b) {
				setUndefined(false);
				super.setSelected(b);
			}
		});
		lockModel = true;
		setUndefined(true);	
	}

		
	@Override
	public void setModel(ButtonModel newModel) {
		if(lockModel)
			throw new UnsupportedOperationException();
		else
			super.setModel(newModel);
	}

	public void setUndefined() {
		setUndefined(true);
	}

	public boolean isUndefined() {
		return bUndefined;
	}

	void setUndefined(boolean b) {
		if (bUndefined != b) {
			bUndefined = b;
			// NUCLOSINT-588: previously, the undefined state was rendered overriding paint and using an 
			// (empty) dummy checkbox as "renderer".  This means copying the state from this checkbox
			// to the renderer (text was missing).  To avoid this, we (re)setting undefined simply
			// (re)sets the icons.
			this.setIcon(b ? getUndefinedIcon() : null);
		}
	}

	Icon getUndefinedIcon() {
		Icon icon = UIManager.getIcon("CheckBox.icon");
		if (icon != null) {
			int w = icon.getIconWidth(), h = icon.getIconHeight();
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = image.getGraphics();
			icon.paintIcon(this, g, 0, 0);
			g.setColor(new Color(0x20000000, true));
			g.fillRect(2, 2, w-4, h-4);
			// g.setColor(new Color(0xff333333, true));
			// g.fillRect(3, w/2-1, w-6, 2);
			g.dispose();
			icon = new ImageIcon(image);
		}
		return icon;
	}




	@Override
	public boolean isFocusPainted() {
		return false;
	}

//	private static final JCheckBox PAINT_DUMMY = new JCheckBox();
//	static {
//		PAINT_DUMMY.setEnabled(false);
//	}
//	
//	@Override
//	public void paint(Graphics g) {
//		if(bUndefined) {
//			PAINT_DUMMY.setHorizontalAlignment(getHorizontalAlignment());
//			PAINT_DUMMY.setBackground(getBackground());
//			PAINT_DUMMY.setForeground(getForeground());
//			PAINT_DUMMY.setBounds(getBounds());
//			PAINT_DUMMY.paint(g);
//		} else
//			super.paint(g);
//	}
}

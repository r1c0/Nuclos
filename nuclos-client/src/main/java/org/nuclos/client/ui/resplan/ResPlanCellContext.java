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

package org.nuclos.client.ui.resplan;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.renderer.CellContext;

public class ResPlanCellContext extends CellContext {

	protected transient boolean mouseOver;

	@Override
	public JResPlanComponent getComponent() {
		return (JResPlanComponent) super.getComponent();
	}

	public void installContext(JResPlanComponent component, Object value, int row, int column, boolean selected, boolean focused, boolean expanded, boolean leaf) {
		this.component = component;
		this.mouseOver = false;
		installState(value, row, column, selected, focused, expanded, leaf);
	}
	
	@Override
	protected Border getBorder() {
		Color color = getForeground();
		boolean thick = false;
		if (isSelected()) {
			thick = true;
			color = new Color(0x8080ff);
		}
		if (isMouseOver()) {
			thick = true;
			color = new Color(0xcc9999);
		}
		Border border = new LineBorder(color, thick ? 3 : 1, true);
		if (!thick) {
			border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}
		return border;
	}
	
	@Override
	protected Color getSelectionBackground() {
		return new Color(0xccccff);
	}
	
	@Override
	protected Color getBackground() {
		return Color.WHITE;
	}
	
	protected boolean isMouseOver() {
		return mouseOver;
	}
	
	void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}
	
	void setDropOn(boolean dropOn) {
		this.dropOn = dropOn;
	}
}

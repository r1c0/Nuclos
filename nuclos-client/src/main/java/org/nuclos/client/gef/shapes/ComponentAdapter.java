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
package org.nuclos.client.gef.shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import org.nuclos.client.gef.layout.Extents2D;

/**
 * Wrapper class that implements the interfaces Shape/Connectable for a JComponent.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ComponentAdapter extends RectangularShape {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	protected JComponent component;

	protected ComponentAdapter() {
		super(0, 0, 0, 0);
	}

	protected ComponentAdapter(JComponent peer, int x, int y, int w, int h) {
		this(peer);
		setDimension(new Rectangle(x, y, w, h));
	}

	/**
	 * Constructs an adapter for a JComponent
	 * @param peer
	 */
	public ComponentAdapter(JComponent peer) {
		super(0, 0, 0, 0);
		this.component = peer;
	}

	/**
	 *
	 * @return
	 */
	public JComponent getComponent() {
		return component;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Color getColor() {
		return component.getBackground();
	}

	/**
	 *
	 * @param color
	 */
	@Override
	public void setColor(Color color) {
		component.setBackground(color);
	}

	/**
	 *
	 * @param dimension
	 */
	@Override
	public void setDimension(Rectangle2D dimension) {
		super.setDimension(dimension);
		if (component != null) {
			component.setBounds(this.dimension.getBounds());
			component.revalidate();
		}
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		if (bResizePointsVisible) {
			paintResizePoints(gfx);
		}
	}

	/**
	 *
	 * @param bVisible
	 */
	@Override
	public void setVisible(boolean bVisible) {
		super.setVisible(bVisible);
		component.setVisible(bVisible);
	}

	/**
	 *
	 * @param d
	 */
	@Override
	public void setPreferredSize(Extents2D d) {
		super.setPreferredSize(d);
		component.setPreferredSize(d.getDimension());
	}

	/**
	 *
	 * @param d
	 */
	@Override
	public void setMinimumSize(Extents2D d) {
		super.setMinimumSize(d);
		component.setMinimumSize(d.getDimension());
	}

	/**
	 *
	 * @param d
	 */
	@Override
	public void setMaximumSize(Extents2D d) {
		super.setMaximumSize(d);
		component.setMaximumSize(d.getDimension());
	}

	/**
	 *
	 */
	protected void selfSelect() {
		view.getModel().deselectAll();
		view.getModel().addSelection(this);
	}
}

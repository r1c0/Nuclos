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
package org.nuclos.client.gef.layout;

import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.shapes.ContainerShape;

/**
 * A flow layout arranges shapes in a left-to-right flow, much
 * like lines of text in a paragraph. Flow layouts are typically used
 * to arrange buttons in a panel. It will arrange
 * buttons left to right until no more buttons fit on the same line.
 * Each line is centered.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class FlowLayout implements ILayout {
	/**
	 * This value indicates that each row of shapes
	 * should be left-justified.
	 */
	public static final int LEFT = 0;

	/**
	 * This value indicates that each row of shapes
	 * should be centered.
	 */
	public static final int CENTER = 1;

	/**
	 * This value indicates that each row of shapes
	 * should be right-justified.
	 */
	public static final int RIGHT = 2;

	/**
	 * This value indicates that each row of shapes
	 * should be justified to the leading edge of the container's
	 * orientation, for example, to the left in left-to-right orientations.
	 */
	public static final int LEADING = 3;

	/**
	 * This value indicates that each row of shapes
	 * should be justified to the trailing edge of the container's
	 * orientation, for example, to the right in left-to-right orientations.
	 */
	public static final int TRAILING = 4;

	/**
	 * <code>align</code> is the property that determines
	 * how each row distributes empty space.
	 * It can be one of the following values:
	 * <ul>
	 * <code>LEFT</code>
	 * <code>RIGHT</code>
	 * <code>CENTER</code>
	 * <code>LEADING</code>
	 * <code>TRAILING</code>
	 * </ul>
	 */
	int align;

	/**
	 * <code>newAlign</code> is the property that determines
	 * how each row distributes empty space for the Java 2 platform,
	 * v1.2 and greater.
	 * It can be one of the following three values:
	 * <ul>
	 * <code>LEFT</code>
	 * <code>RIGHT</code>
	 * <code>CENTER</code>
	 * <code>LEADING</code>
	 * <code>TRAILING</code>
	 * </ul>
	 */
	int newAlign;			 // This is the one we actually use

	/**
	 * The flow layout manager allows a seperation of
	 * shapes with gaps.  The horizontal gap will
	 * specify the space between shapes.
	 */
	double hgap;

	/**
	 * The flow layout manager allows a seperation of
	 * shapes with gaps.  The vertical gap will
	 * specify the space between rows.
	 */
	double vgap;

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 */
	public FlowLayout() {
		this(CENTER, 5d, 5d);
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with the specified
	 * alignment and a default 5-unit horizontal and vertical gap.
	 * The value of the alignment argument must be one of
	 * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
	 * or <code>FlowLayout.CENTER</code>.
	 * @param align the alignment value
	 */
	public FlowLayout(int align) {
		this(align, 5d, 5d);
	}

	/**
	 * Creates a new flow layout manager with the indicated alignment
	 * and the indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of
	 * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
	 * or <code>FlowLayout.CENTER</code>.
	 * @param			align	 the alignment value
	 * @param			hgap		the horizontal gap between shapes
	 * @param			vgap		the vertical gap between shapes
	 */
	public FlowLayout(int align, double hgap, double vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
		setAlignment(align);
	}

	/**
	 * Gets the alignment for this layout.
	 * Possible values are <code>FlowLayout.LEFT</code>,
	 * <code>FlowLayout.RIGHT</code>, <code>FlowLayout.CENTER</code>,
	 * <code>FlowLayout.LEADING</code>,
	 * or <code>FlowLayout.TRAILING</code>.
	 * @return the alignment value for this layout
	 */
	public int getAlignment() {
		return newAlign;
	}

	/**
	 * Sets the alignment for this layout.
	 * Possible values are
	 * <ul>
	 * <li><code>FlowLayout.LEFT</code>
	 * <li><code>FlowLayout.RIGHT</code>
	 * <li><code>FlowLayout.CENTER</code>
	 * <li><code>FlowLayout.LEADING</code>
	 * <li><code>FlowLayout.TRAILING</code>
	 * </ul>
	 * @param			align one of the alignment values shown above
	 */
	public void setAlignment(int align) {
		this.newAlign = align;

		// this.align is used only for serialization compatibility,
		// so set it to a value compatible with the 1.1 version
		// of the class

		switch (align) {
			case LEADING:
				this.align = LEFT;
				break;
			case TRAILING:
				this.align = RIGHT;
				break;
			default:
				this.align = align;
				break;
		}
	}

	/**
	 * Gets the horizontal gap between shapes.
	 * @return the horizontal gap between shapes
	 */
	public double getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between shapes.
	 * @param hgap the horizontal gap between shapes
	 */
	public void setHgap(double hgap) {
		this.hgap = hgap;
	}

	/**
	 * Gets the vertical gap between shapes.
	 * @return the vertical gap between shapes
	 */
	public double getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between shapes.
	 * @param vgap the vertical gap between shapes
	 */
	public void setVgap(double vgap) {
		this.vgap = vgap;
	}

	/**
	 * Adds the specified shape to the layout. Not used by this class.
	 * @param name the name of the shape
	 * @param shape the shape to be added
	 */
	@Override
	public void addLayoutShape(String name, Shape shape) {
	}

	/**
	 * Removes the specified shape from the layout. Not used by
	 * this class.
	 * @param shape the shape to remove
	 */
	@Override
	public void removeLayoutShape(Shape shape) {
	}

	/**
	 * Returns the preferred dimensions for this layout given the
	 * <i>visible</i> shapes in the specified target container.
	 * @param target the shape which needs to be laid out
	 * @return the preferred dimensions to lay out the
	 *            subshapes of the specified container
	 */
	@Override
	public Extents2D preferredLayoutSize(ContainerShape target) {
		Extents2D dim = new Extents2D(0d, 0d);
		int nmembers = target.getShapeCount();
		boolean firstVisibleComponent = true;

		for (int i = 0; i < nmembers; i++) {
			Shape m = target.getShape(i);
			if (m.isVisible()) {
				Extents2D e = m.getPreferredSize();
				dim.height = Math.max(dim.height, e.height);
				if (firstVisibleComponent) {
					firstVisibleComponent = false;
				}
				else {
					dim.width += hgap;
				}
				dim.width += e.width;
			}
		}
		Insets2D insets = target.getInsets();
		dim.width += insets.left + insets.right + hgap * 2d;
		dim.height += insets.top + insets.bottom + vgap * 2d;
		return dim;
	}

	/**
	 * Returns the minimum dimensions needed to layout the <i>visible</i>
	 * shapes contained in the specified target container.
	 * @param target the shape which needs to be laid out
	 * @return the minimum dimensions to lay out the
	 *            subshapes of the specified container
	 */
	@Override
	public Extents2D minimumLayoutSize(ContainerShape target) {
		Extents2D dim = new Extents2D(0d, 0d);
		int nmembers = target.getShapeCount();

		for (int i = 0; i < nmembers; i++) {
			Shape m = target.getShape(i);
			if (m.isVisible()) {
				Extents2D e = m.getMinimumSize();
				dim.height = Math.max(dim.height, e.height);
				if (i > 0) {
					dim.width += hgap;
				}
				dim.width += e.width;
			}
		}
		Insets2D insets = target.getInsets();
		dim.width += insets.left + insets.right + hgap * 2d;
		dim.height += insets.top + insets.bottom + vgap * 2d;
		return dim;
	}

	/**
	 * Centers the elements in the specified row, if there is any slack.
	 * @param target the shape which needs to be moved
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param width the width dimensions
	 * @param height the height dimensions
	 * @param rowStart the beginning of the row
	 * @param rowEnd the the ending of the row
	 */
	private void moveShapes(ContainerShape target, double x, double y, double width, double height,
			int rowStart, int rowEnd, boolean ltr) {
		switch (newAlign) {
			case LEFT:
				x += ltr ? 0 : width;
				break;
			case CENTER:
				x += width / 2;
				break;
			case RIGHT:
				x += ltr ? width : 0;
				break;
			case LEADING:
				break;
			case TRAILING:
				x += width;
				break;
		}
		for (int i = rowStart; i < rowEnd; i++) {
			Shape m = target.getShape(i);
			if (m.isVisible()) {
				if (ltr) {
					m.setLocation(x, y + (height - m.getHeight()) / 2d);
				}
				else {
					m.setLocation(target.getWidth() - x - m.getWidth(), y + (height - m.getHeight()) / 2d);
				}
				x += m.getWidth() + hgap;
			}
		}
	}

	/**
	 * Lays out the container. This method lets each shape take
	 * its preferred size by reshaping the shapes in the
	 * target container in order to satisfy the alignment of
	 * this <code>FlowLayout</code> object.
	 * @param target the specified container being laid out
	 */
	@Override
	public void layoutContainer(ContainerShape target) {
		Insets2D insets = target.getInsets();
		double maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2d);
		int nmembers = target.getShapeCount();
		int start = 0;
		double x = 0d, y = insets.top + vgap;
		double rowh = 0d;

		boolean ltr = target.getComponentOrientation().isLeftToRight();

		for (int i = 0; i < nmembers; i++) {
			Shape m = target.getShape(i);
			if (m.isVisible()) {
				Extents2D e = m.getPreferredSize();
				m.setSize(e);

				if ((x == 0d) || ((x + e.width) <= maxwidth)) {
					if (x > 0d) {
						x += hgap;
					}
					x += e.width;
					rowh = Math.max(rowh, e.height);
				}
				else {
					moveShapes(target, insets.left + hgap, y, maxwidth - x, rowh, start, i, ltr);
					x = e.width;
					y += vgap + rowh;
					rowh = e.height;
					start = i;
				}
			}
		}
		moveShapes(target, insets.left + hgap, y, maxwidth - x, rowh, start, nmembers, ltr);
	}

	/**
	 * Returns a string representation of this <code>FlowLayout</code>
	 * object and its values.
	 * @return a string representation of this layout
	 */
	@Override
	public String toString() {
		String str = "";
		switch (align) {
			case LEFT:
				str = ",align=left";
				break;
			case CENTER:
				str = ",align=center";
				break;
			case RIGHT:
				str = ",align=right";
				break;
			case LEADING:
				str = ",align=leading";
				break;
			case TRAILING:
				str = ",align=trailing";
				break;
		}
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str + "]";
	}


}

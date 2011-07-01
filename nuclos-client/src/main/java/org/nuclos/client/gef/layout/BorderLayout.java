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

import java.awt.geom.Rectangle2D;

import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.shapes.ContainerShape;

/**
 * A border layout lays out a container, arranging and resizing
 * its shapes to fit in five regions:
 * north, south, east, west, and center.
 * Each region may contain no more than one shape, and
 * is identified by a corresponding constant:
 * <code>NORTH</code>, <code>SOUTH</code>, <code>EAST</code>,
 * <code>WEST</code>, and <code>CENTER</code>.
 * <p>
 * In addition, <code>BorderLayout</code> supports the relative
 * positioning constants, <code>PAGE_START</code>, <code>PAGE_END</code>,
 * <code>LINE_START</code>, and <code>LINE_END</code>.
 * In a container whose <code>ComponentOrientation</code> is set to
 * <code>ComponentOrientation.LEFT_TO_RIGHT</code>, these constants map to
 * <code>NORTH</code>, <code>SOUTH</code>, <code>WEST</code>, and
 * <code>EAST</code>, respectively.
 * <p>
 * Mixing both absolute and relative positioning constants can lead to
 * unpredicable results.  If
 * you use both types, the relative constants will take precedence.
 * For example, if you add shapes using both the <code>NORTH</code>
 * and <code>PAGE_START</code> constants in a container whose
 * orientation is <code>LEFT_TO_RIGHT</code>, only the
 * <code>PAGE_START</code> will be layed out.
 * <p>
 * The shapes are laid out according to their
 * preferred sizes and the constraints of the container's size.
 * The <code>NORTH</code> and <code>SOUTH</code> shapes may
 * be stretched horizontally; the <code>EAST</code> and
 * <code>WEST</code> shapes may be stretched vertically;
 * the <code>CENTER</code> shape may stretch both horizontally
 * and vertically to fill any space left over.
 * <p>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class BorderLayout {
	/**
	 * Constructs a border layout with the horizontal gaps
	 * between shapes.
	 * The horizontal gap is specified by <code>hgap</code>.
	 */
	double hgap;

	/**
	 * Constructs a border layout with the vertical gaps
	 * between shapes.
	 * The vertical gap is specified by <code>vgap</code>.
	 */
	double vgap;

	/**
	 * Constant to specify shapes location to be the
	 *      north portion of the border layout.
	 */
	Shape north;
	/**
	 * Constant to specify shapes location to be the
	 *      west portion of the border layout.
	 */
	Shape west;
	/**
	 * Constant to specify shapes location to be the
	 *      east portion of the border layout.
	 */
	Shape east;
	/**
	 * Constant to specify shapes location to be the
	 *      south portion of the border layout.
	 */
	Shape south;
	/**
	 * Constant to specify shapes location to be the
	 *      center portion of the border layout.
	 */
	Shape center;

	/**
	 *
	 * A relative positioning constant, that can be used instead of
	 * north, south, east, west or center.
	 * mixing the two types of constants can lead to unpredicable results.  If
	 * you use both types, the relative constants will take precedence.
	 * For example, if you add shapes using both the <code>NORTH</code>
	 * and <code>BEFORE_FIRST_LINE</code> constants in a container whose
	 * orientation is <code>LEFT_TO_RIGHT</code>, only the
	 * <code>BEFORE_FIRST_LINE</code> will be layed out.
	 * This will be the same for lastLine, firstItem, lastItem.
	 */
	Shape firstLine;
	/**
	 * A relative positioning constant, that can be used instead of
	 * north, south, east, west or center.
	 * Please read Description for firstLine.
	 */
	Shape lastLine;
	/**
	 * A relative positioning constant, that can be used instead of
	 * north, south, east, west or center.
	 * Please read Description for firstLine.
	 */
	Shape firstItem;
	/**
	 * A relative positioning constant, that can be used instead of
	 * north, south, east, west or center.
	 * Please read Description for firstLine.
	 */
	Shape lastItem;

	/**
	 * The north layout constraint (top of container).
	 */
	public static final String NORTH = "North";

	/**
	 * The south layout constraint (bottom of container).
	 */
	public static final String SOUTH = "South";

	/**
	 * The east layout constraint (right side of container).
	 */
	public static final String EAST = "East";

	/**
	 * The west layout constraint (left side of container).
	 */
	public static final String WEST = "West";

	/**
	 * The center layout constraint (middle of container).
	 */
	public static final String CENTER = "Center";

	/**
	 * Synonym for PAGE_START.  Exists for compatibility with previous
	 * versions.  PAGE_START is preferred.
	 *
	 * @see #PAGE_START
	 */
	public static final String BEFORE_FIRST_LINE = "First";

	/**
	 * Synonym for PAGE_END.  Exists for compatibility with previous
	 * versions.  PAGE_END is preferred.
	 *
	 * @see #PAGE_END
	 */
	public static final String AFTER_LAST_LINE = "Last";

	/**
	 * Synonym for LINE_START.  Exists for compatibility with previous
	 * versions.  LINE_START is preferred.
	 *
	 * @see #LINE_START
	 */
	public static final String BEFORE_LINE_BEGINS = "Before";

	/**
	 * Synonym for LINE_END.  Exists for compatibility with previous
	 * versions.  LINE_END is preferred.
	 *
	 * @see #LINE_END
	 */
	public static final String AFTER_LINE_ENDS = "After";

	/**
	 * The shape comes before the first line of the layout's content.
	 * For Western, left-to-right and top-to-bottom orientations, this is
	 * equivalent to NORTH.
	 */
	public static final String PAGE_START = BEFORE_FIRST_LINE;

	/**
	 * The shape comes after the last line of the layout's content.
	 * For Western, left-to-right and top-to-bottom orientations, this is
	 * equivalent to SOUTH.
	 */
	public static final String PAGE_END = AFTER_LAST_LINE;

	/**
	 * The shape goes at the beginning of the line direction for the
	 * layout. For Western, left-to-right and top-to-bottom orientations,
	 * this is equivalent to WEST.
	 */
	public static final String LINE_START = BEFORE_LINE_BEGINS;

	/**
	 * The shape goes at the end of the line direction for the
	 * layout. For Western, left-to-right and top-to-bottom orientations,
	 * this is equivalent to EAST.
	 */
	public static final String LINE_END = AFTER_LINE_ENDS;

	/**
	 * Constructs a new border layout with
	 * no gaps between shapes.
	 */
	public BorderLayout() {
		this(0d, 0d);
	}

	/**
	 * Constructs a border layout with the specified gaps
	 * between shapes.
	 * The horizontal gap is specified by <code>hgap</code>
	 * and the vertical gap is specified by <code>vgap</code>.
	 * @param	 hgap	 the horizontal gap.
	 * @param	 vgap	 the vertical gap.
	 */
	public BorderLayout(double hgap, double vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
	}

	/**
	 * Returns the horizontal gap between shapes.
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
	 * Returns the vertical gap between shapes.
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
	 * Adds the specified shape to the layout, using the specified
	 * constraint object.  For border layouts, the constraint must be
	 * one of the following constants:  <code>NORTH</code>,
	 * <code>SOUTH</code>, <code>EAST</code>,
	 * <code>WEST</code>, or <code>CENTER</code>.
	 * <p>
	 * Most applications do not call this method directly. This method
	 * is called when a shape is added to a container using the
	 * <code>Container.add</code> method with the same argument types.
	 * @param	 shape				 the shape to be added.
	 * @param	 constraints	an object that specifies how and where
	 *                       the shape is added to the layout.
	 * @exception IllegalArgumentException	if the constraint object is not
	 *                 a string, or if it not one of the five specified
	 *              constants.
	 */
	public void addLayoutComponent(Shape shape, Object constraints) {
		if ((constraints == null) || (constraints instanceof String)) {
			addLayoutComponent((String) constraints, shape);
		}
		else {
			throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
		}
	}

	/**
	 *
	 * @param name
	 * @param shape
	 */
	public void addLayoutComponent(String name, Shape shape) {
		/* Special case:  treat null the same as "Center". */
		if (name == null) {
			name = "Center";
		}

		/* Assign the shape to one of the known regions of the layout.
					 */
		if ("Center".equals(name)) {
			center = shape;
		}
		else if ("North".equals(name)) {
			north = shape;
		}
		else if ("South".equals(name)) {
			south = shape;
		}
		else if ("East".equals(name)) {
			east = shape;
		}
		else if ("West".equals(name)) {
			west = shape;
		}
		else if (BEFORE_FIRST_LINE.equals(name)) {
			firstLine = shape;
		}
		else if (AFTER_LAST_LINE.equals(name)) {
			lastLine = shape;
		}
		else if (BEFORE_LINE_BEGINS.equals(name)) {
			firstItem = shape;
		}
		else if (AFTER_LINE_ENDS.equals(name)) {
			lastItem = shape;
		}
		else {
			throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
		}
	}

	/**
	 * Removes the specified shape from this border layout. This
	 * method is called when a container calls its <code>remove</code> or
	 * <code>removeAll</code> methods. Most applications do not call this
	 * method directly.
	 * @param shape the shape to be removed.
	 */
	public void removeLayoutComponent(Shape shape) {
		if (shape == center) {
			center = null;
		}
		else if (shape == north) {
			north = null;
		}
		else if (shape == south) {
			south = null;
		}
		else if (shape == east) {
			east = null;
		}
		else if (shape == west) {
			west = null;
		}
		if (shape == firstLine) {
			firstLine = null;
		}
		else if (shape == lastLine) {
			lastLine = null;
		}
		else if (shape == firstItem) {
			firstItem = null;
		}
		else if (shape == lastItem) {
			lastItem = null;
		}
	}

	/**
	 * Determines the minimum size of the <code>target</code> container
	 * using this layout manager.
	 * <p>
	 * This method is called when a container calls its
	 * <code>getMinimumSize</code> method. Most applications do not call
	 * this method directly.
	 * @param	 target	 the container in which to do the layout.
	 * @return the minimum dimensions needed to lay out the subshapes
	 *          of the specified container.
	 */
	public Extents2D minimumLayoutSize(ContainerShape target) {
		Extents2D dim = new Extents2D(0d, 0d);

		boolean ltr = target.getComponentOrientation().isLeftToRight();
		Shape shape = null;

		if ((shape = getChild(EAST, ltr)) != null) {
			Extents2D d = shape.getMinimumSize();
			dim.width += d.width + hgap;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(WEST, ltr)) != null) {
			Extents2D d = shape.getMinimumSize();
			dim.width += d.width + hgap;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(CENTER, ltr)) != null) {
			Extents2D d = shape.getMinimumSize();
			dim.width += d.width;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(NORTH, ltr)) != null) {
			Extents2D d = shape.getMinimumSize();
			dim.width = Math.max(d.width, dim.width);
			dim.height += d.height + vgap;
		}
		if ((shape = getChild(SOUTH, ltr)) != null) {
			Extents2D d = shape.getMinimumSize();
			dim.width = Math.max(d.width, dim.width);
			dim.height += d.height + vgap;
		}

		Insets2D insets = target.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}

	/**
	 * Determines the preferred size of the <code>target</code>
	 * container using this layout manager, based on the shapes
	 * in the container.
	 * <p>
	 * Most applications do not call this method directly. This method
	 * is called when a container calls its <code>getPreferredSize</code>
	 * method.
	 * @param	 target	 the container in which to do the layout.
	 * @return the preferred dimensions to lay out the subshapes
	 *          of the specified container.
	 */
	public Extents2D preferredLayoutSize(ContainerShape target) {
		Extents2D dim = new Extents2D(0d, 0d);

		boolean ltr = target.getComponentOrientation().isLeftToRight();
		Shape shape = null;

		if ((shape = getChild(EAST, ltr)) != null) {
			Extents2D d = shape.getPreferredSize();
			dim.width += d.width + hgap;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(WEST, ltr)) != null) {
			Extents2D d = shape.getPreferredSize();
			dim.width += d.width + hgap;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(CENTER, ltr)) != null) {
			Extents2D d = shape.getPreferredSize();
			dim.width += d.width;
			dim.height = Math.max(d.height, dim.height);
		}
		if ((shape = getChild(NORTH, ltr)) != null) {
			Extents2D d = shape.getPreferredSize();
			dim.width = Math.max(d.width, dim.width);
			dim.height += d.height + vgap;
		}
		if ((shape = getChild(SOUTH, ltr)) != null) {
			Extents2D d = shape.getPreferredSize();
			dim.width = Math.max(d.width, dim.width);
			dim.height += d.height + vgap;
		}

		Insets2D insets = target.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}

	/**
	 * Returns the maximum dimensions for this layout given the shapes
	 * in the specified target container.
	 * @param target the shape which needs to be laid out
	 */
	public Extents2D maximumLayoutSize(ContainerShape target) {
		return new Extents2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis.  This specifies how
	 * the shape would like to be aligned relative to other
	 * shapes.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public double getLayoutAlignmentX(ContainerShape parent) {
		return 0.5d;
	}

	/**
	 * Returns the alignment along the y axis.  This specifies how
	 * the shape would like to be aligned relative to other
	 * shapes.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public double getLayoutAlignmentY(ContainerShape parent) {
		return 0.5d;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 */
	public void invalidateLayout(ContainerShape target) {
	}

	/**
	 * Lays out the container argument using this border layout.
	 * <p>
	 * This method actually reshapes the shapes in the specified
	 * container in order to satisfy the constraints of this
	 * <code>BorderLayout</code> object. The <code>NORTH</code>
	 * and <code>SOUTH</code> shapes, if any, are placed at
	 * the top and bottom of the container, respectively. The
	 * <code>WEST</code> and <code>EAST</code> shapes are
	 * then placed on the left and right, respectively. Finally,
	 * the <code>CENTER</code> object is placed in any remaining
	 * space in the middle.
	 * <p>
	 * Most applications do not call this method directly. This method
	 * is called when a container calls its <code>doLayout</code> method.
	 * @param	 target	 the container in which to do the layout.
	 */
	public void layoutContainer(ContainerShape target) {
		Insets2D insets = target.getInsets();
		double top = insets.top;
		double bottom = target.getHeight() - insets.bottom;
		double left = insets.left;
		double right = target.getWidth() - insets.right;

		boolean ltr = target.getComponentOrientation().isLeftToRight();
		Shape shape = null;

		if ((shape = getChild(NORTH, ltr)) != null) {
			shape.setSize(new Extents2D(right - left, shape.getHeight()));
			Extents2D d = shape.getPreferredSize();
			shape.setDimension(new Rectangle2D.Double(left, top, right - left, d.height));
			top += d.height + vgap;
		}
		if ((shape = getChild(SOUTH, ltr)) != null) {
			shape.setSize(new Extents2D(right - left, shape.getHeight()));
			Extents2D d = shape.getPreferredSize();
			shape.setDimension(new Rectangle2D.Double(left, bottom - d.height, right - left, d.height));
			bottom -= d.height + vgap;
		}
		if ((shape = getChild(EAST, ltr)) != null) {
			shape.setSize(new Extents2D(shape.getWidth(), bottom - top));
			Extents2D d = shape.getPreferredSize();
			shape.setDimension(new Rectangle2D.Double(right - d.width, top, d.width, bottom - top));
			right -= d.width + hgap;
		}
		if ((shape = getChild(WEST, ltr)) != null) {
			shape.setSize(new Extents2D(shape.getWidth(), bottom - top));
			Extents2D d = shape.getPreferredSize();
			shape.setDimension(new Rectangle2D.Double(left, top, d.width, bottom - top));
			left += d.width + hgap;
		}
		if ((shape = getChild(CENTER, ltr)) != null) {
			shape.setDimension(new Rectangle2D.Double(left, top, right - left, bottom - top));
		}
	}

	/**
	 * Get the shape that corresponds to the given constraint location
	 *
	 * @param	 key		 The desired absolute position,
	 *                  either NORTH, SOUTH, EAST, or WEST.
	 * @param	 ltr		 Is the shape line direction left-to-right?
	 */
	private Shape getChild(String key, boolean ltr) {
		Shape result = null;

		if (key == NORTH) {
			result = (firstLine != null) ? firstLine : north;
		}
		else if (key == SOUTH) {
			result = (lastLine != null) ? lastLine : south;
		}
		else if (key == WEST) {
			result = ltr ? firstItem : lastItem;
			if (result == null) {
				result = west;
			}
		}
		else if (key == EAST) {
			result = ltr ? lastItem : firstItem;
			if (result == null) {
				result = east;
			}
		}
		else if (key == CENTER) {
			result = center;
		}
		if (result != null && !result.isVisible()) {
			result = null;
		}
		return result;
	}

	/**
	 * Returns a string representation of the state of this border layout.
	 * @return a string representation of this border layout.
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
	}
}

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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.shapes.ContainerShape;

/**
 * The <code>GridBagLayout</code> class is a flexible layout
 * manager that aligns components vertically and horizontally,
 * without requiring that the components be of the same size.
 * Each <code>GridBagLayout</code> object maintains a dynamic,
 * rectangular grid of cells, with each component occupying
 * one or more cells, called its <em>display area</em>.
 * <p>
 * Each component managed by a <code>GridBagLayout</code> is associated with
 * an instance of GridBagConstraints.  The constraints object
 * specifies where a component's display area should be located on the grid
 * and how the component should be positioned within its display area.  In
 * addition to its constraints object, the <code>GridBagLayout</code> also
 * considers each component's minimum and preferred sizes in order to
 * determine a component's size.
 * <p>
 * The overall orientation of the grid depends on the container's
 * ComponentOrientation property.  For horizontal left-to-right
 * orientations, grid coordinate (0,0) is in the upper left corner of the
 * container with x increasing to the right and y increasing downward.  For
 * horizontal right-to-left orientations, grid coordinate (0,0) is in the upper
 * right corner of the container with x increasing to the left and y
 * increasing downward.
 * <p>
 * To use a grid bag layout effectively, you must customize one or more
 * of the <code>GridBagConstraints</code> objects that are associated
 * with its components. You customize a <code>GridBagConstraints</code>
 * object by setting one or more of its instance variables:
 * <p>
 * <dl>
 * <dt>{@link GridBagConstraints#gridx},
 * {@link GridBagConstraints#gridy}
 * <dd>Specifies the cell containing the leading corner of the component's
 * display area, where the cell at the origin of the grid has address
 * <code>gridx&nbsp;=&nbsp;0</code>,
 * <code>gridy&nbsp;=&nbsp;0</code>.  For horizontal left-to-right layout,
 * a component's leading corner is its upper left.  For horizontal
 * right-to-left layout, a component's leading corner is its upper right.
 * Use <code>GridBagConstraints.RELATIVE</code> (the default value)
 * to specify that the component be placed immediately following
 * (along the x axis for <code>gridx</code> or the y axis for
 * <code>gridy</code>) the component that was added to the container
 * just before this component was added.
 * <dt>{@link GridBagConstraints#gridwidth},
 * {@link GridBagConstraints#gridheight}
 * <dd>Specifies the number of cells in a row (for <code>gridwidth</code>)
 * or column (for <code>gridheight</code>)
 * in the component's display area.
 * The default value is 1.
 * Use <code>GridBagConstraints.REMAINDER</code> to specify
 * that the component be the last one in its row (for <code>gridwidth</code>)
 * or column (for <code>gridheight</code>).
 * Use <code>GridBagConstraints.RELATIVE</code> to specify
 * that the component be the next to last one
 * in its row (for <code>gridwidth</code>)
 * or column (for <code>gridheight</code>).
 * <dt>{@link GridBagConstraints#fill}
 * <dd>Used when the component's display area
 * is larger than the component's requested size
 * to determine whether (and how) to resize the component.
 * Possible values are
 * <code>GridBagConstraints.NONE</code> (the default),
 * <code>GridBagConstraints.HORIZONTAL</code>
 * (make the component wide enough to fill its display area
 * horizontally, but don't change its height),
 * <code>GridBagConstraints.VERTICAL</code>
 * (make the component tall enough to fill its display area
 * vertically, but don't change its width), and
 * <code>GridBagConstraints.BOTH</code>
 * (make the component fill its display area entirely).
 * <dt>{@link GridBagConstraints#ipadx},
 * {@link GridBagConstraints#ipady}
 * <dd>Specifies the component's internal padding within the layout,
 * how much to add to the minimum size of the component.
 * The width of the component will be at least its minimum width
 * plus <code>(ipadx&nbsp;*&nbsp;2)</code> pixels (since the padding
 * applies to both sides of the component). Similarly, the height of
 * the component will be at least the minimum height plus
 * <code>(ipady&nbsp;*&nbsp;2)</code> pixels.
 * <dt>{@link GridBagConstraints#insets}
 * <dd>Specifies the component's external padding, the minimum
 * amount of space between the component and the edges of its display area.
 * <dt>{@link GridBagConstraints#anchor}
 * <dd>Used when the component is smaller than its display area
 * to determine where (within the display area) to place the component.
 * There are two kinds of possible values: relative and absolute.  Relative
 * values are interpreted relative to the container's
 * <code>ComponentOrientation</code> property while absolute values
 * are not.  Valid values are:</dd>
 * <p>
 * <center><table BORDER=0 COLS=2 WIDTH=800>
 * <tr>
 * <td><b>Absolute Values</b></td>
 * <td><b>Relative Values</b></td>
 * </tr>
 * <tr>
 * <td>
 * <li><code>GridBagConstraints.NORTH</code></li>
 * <li><code>GridBagConstraints.SOUTH</code></li>
 * <li><code>GridBagConstraints.WEST</code></li>
 * <li><code>GridBagConstraints.EAST</code></li>
 * <li><code>GridBagConstraints.NORTHWEST</code></li>
 * <li><code>GridBagConstraints.NORTHEAST</code></li>
 * <li><code>GridBagConstraints.SOUTHWEST</code></li>
 * <li><code>GridBagConstraints.SOUTHEAST</code></li>
 * <li><code>GridBagConstraints.CENTER</code> (the default)</li>
 * </td>
 * <td>
 * <li><code>GridBagConstraints.PAGE_START</code></li>
 * <li><code>GridBagConstraints.PAGE_END</code></li>
 * <li><code>GridBagConstraints.LINE_START</code></li>
 * <li><code>GridBagConstraints.LINE_END</code></li>
 * <li><code>GridBagConstraints.FIRST_LINE_START</code></li>
 * <li><code>GridBagConstraints.FIRST_LINE_END</code></li>
 * <li><code>GridBagConstraints.LAST_LINE_START</code></li>
 * <li><code>GridBagConstraints.LAST_LINE_END</code></li>
 * </ul>
 * </td>
 * </tr>
 * </table></center><p>
 * <dt>{@link GridBagConstraints#weightx},
 * {@link GridBagConstraints#weighty}
 * <dd>Used to determine how to distribute space, which is
 * important for specifying resizing behavior.
 * Unless you specify a weight for at least one component
 * in a row (<code>weightx</code>) and column (<code>weighty</code>),
 * all the components clump together in the center of their container.
 * This is because when the weight is zero (the default),
 * the <code>GridBagLayout</code> object puts any extra space
 * between its grid of cells and the edges of the container.
 * </dl>
 * <p>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class GridBagLayout implements ILayout2, java.io.Serializable {
	class GridBagLayoutInfo implements java.io.Serializable {
		int width, height;		/* number of cells horizontally, vertically */
		double startx, starty;	/* starting point for layout */
		double minWidth[];		/* largest minWidth in each column */
		double minHeight[];		/* largest minHeight in each row */
		double weightX[];		/* largest weight in each column */
		double weightY[];		/* largest weight in each row */

		GridBagLayoutInfo() {
			minWidth = new double[GridBagLayout.MAXGRIDSIZE];
			minHeight = new double[GridBagLayout.MAXGRIDSIZE];
			weightX = new double[GridBagLayout.MAXGRIDSIZE];
			weightY = new double[GridBagLayout.MAXGRIDSIZE];
		}
	}

	/**
	 * The maximum number of grid positions (both horizontally and
	 * vertically) that can be laid out by the grid bag layout.
	 */
	protected static final int MAXGRIDSIZE = 512;

	/**
	 * The smallest grid that can be laid out by the grid bag layout.
	 */
	protected static final int MINSIZE = 1;
	/**
	 * The preferred grid size that can be laid out by the grid bag layout.
	 */
	protected static final int PREFERREDSIZE = 2;

	/**
	 * This hashtable maintains the association between
	 * a component and its gridbag constraints.
	 * The Keys in <code>comptable</code> are the components and the
	 * values are the instances of <code>GridBagConstraints</code>.
	 */
	protected Hashtable<Shape, Object> comptable;

	/**
	 * This field holds a gridbag constraints instance
	 * containing the default values, so if a component
	 * does not have gridbag constraints associated with
	 * it, then the component will be assigned a
	 * copy of the <code>defaultConstraints</code>.
	 */
	protected GridBagConstraints defaultConstraints;

	/**
	 * This field holds the layout information
	 * for the gridbag.  The information in this field
	 * is based on the most recent validation of the
	 * gridbag.
	 * If <code>layoutInfo</code> is <code>null</code>
	 * this indicates that there are no components in
	 * the gridbag or if there are components, they have
	 * not yet been validated.
	 */
	protected GridBagLayoutInfo layoutInfo;

	/**
	 * This field holds the overrides to the column minimum
	 * width.  If this field is non-<code>null</code> the values are
	 * applied to the gridbag after all of the minimum columns
	 * widths have been calculated.
	 * If columnWidths has more elements than the number of
	 * columns, columns are added to the gridbag to match
	 * the number of elements in columnWidth.
	 */
	public int columnWidths[];

	/**
	 * This field holds the overrides to the row minimum
	 * heights.  If this field is non-</code>null</code> the values are
	 * applied to the gridbag after all of the minimum row
	 * heights have been calculated.
	 * If <code>rowHeights</code> has more elements than the number of
	 * rows, rowa are added to the gridbag to match
	 * the number of elements in <code>rowHeights</code>.
	 */
	public int rowHeights[];

	/**
	 * This field holds the overrides to the column weights.
	 * If this field is non-<code>null</code> the values are
	 * applied to the gridbag after all of the columns
	 * weights have been calculated.
	 * If <code>columnWeights[i]</code> &gt; weight for column i, then
	 * column i is assigned the weight in <code>columnWeights[i]</code>.
	 * If <code>columnWeights</code> has more elements than the number
	 * of columns, the excess elements are ignored - they do
	 * not cause more columns to be created.
	 */
	public double columnWeights[];

	/**
	 * This field holds the overrides to the row weights.
	 * If this field is non-</code>null</code> the values are
	 * applied to the gridbag after all of the rows
	 * weights have been calculated.
	 * If <code>rowWeights[i]</code> &gt; weight for row i, then
	 * row i is assigned the weight in <code>rowWeights[i]</code>.
	 * If <code>rowWeights</code> has more elements than the number
	 * of rows, the excess elements are ignored - they do
	 * not cause more rows to be created.
	 */
	public double rowWeights[];

	/**
	 * Creates a grid bag layout manager.
	 */
	public GridBagLayout() {
		comptable = new Hashtable<Shape, Object>();
		defaultConstraints = new GridBagConstraints();
	}

	/**
	 * Sets the constraints for the specified component in this layout.
	 * @param			 shape the component to be modified
	 * @param			 constraints the constraints to be applied
	 */
	public void setConstraints(Shape shape, GridBagConstraints constraints) {
		comptable.put(shape, constraints.clone());
	}

	/**
	 * Gets the constraints for the specified component.  A copy of
	 * the actual <code>GridBagConstraints</code> object is returned.
	 * @param			 shape the component to be queried
	 * @return the constraint for the specified component in this
	 *                  grid bag layout; a copy of the actual constraint
	 *                  object is returned
	 */
	public GridBagConstraints getConstraints(Shape shape) {
		GridBagConstraints constraints = (GridBagConstraints) comptable.get(shape);
		if (constraints == null) {
			setConstraints(shape, defaultConstraints);
			constraints = (GridBagConstraints) comptable.get(shape);
		}
		return (GridBagConstraints) constraints.clone();
	}

	/**
	 * Retrieves the constraints for the specified component.
	 * The return value is not a copy, but is the actual
	 * <code>GridBagConstraints</code> object used by the layout mechanism.
	 * @param			 shape the component to be queried
	 * @return the contraints for the specified component
	 */
	protected GridBagConstraints lookupConstraints(Shape shape) {
		GridBagConstraints constraints = (GridBagConstraints) comptable.get(shape);
		if (constraints == null) {
			setConstraints(shape, defaultConstraints);
			constraints = (GridBagConstraints) comptable.get(shape);
		}
		return constraints;
	}

	/**
	 * Removes the constraints for the specified component in this layout
	 * @param			 shape the component to be modified
	 */
	private void removeConstraints(Shape shape) {
		comptable.remove(shape);
	}

	/**
	 * Determines the origin of the layout area, in the graphics coordinate
	 * space of the target container.  This value represents the pixel
	 * coordinates of the top-left corner of the layout area regardless of
	 * the <code>ComponentOrientation</code> value of the container.  This
	 * is distinct from the grid origin given by the cell coordinates (0,0).
	 * Most applications do not call this method directly.
	 * @return the graphics origin of the cell in the top-left
	 *             corner of the layout grid
	 */
	public Point2D getLayoutOrigin() {
		Point2D origin = new Point2D.Double(0d, 0d);
		if (layoutInfo != null) {
			origin.setLocation(layoutInfo.startx, layoutInfo.starty);
		}
		return origin;
	}

	/**
	 * Determines column widths and row heights for the layout grid.
	 * <p>
	 * Most applications do not call this method directly.
	 * @return an array of two arrays, containing the widths
	 *                       of the layout columns and
	 *                       the heights of the layout rows
	 */
	public double[][] getLayoutDimensions() {
		if (layoutInfo == null) {
			return new double[2][0];
		}

		double dim[][] = new double[2][];
		dim[0] = new double[layoutInfo.width];
		dim[1] = new double[layoutInfo.height];

		System.arraycopy(layoutInfo.minWidth, 0, dim[0], 0, layoutInfo.width);
		System.arraycopy(layoutInfo.minHeight, 0, dim[1], 0, layoutInfo.height);

		return dim;
	}

	/**
	 * Determines the weights of the layout grid's columns and rows.
	 * Weights are used to calculate how much a given column or row
	 * stretches beyond its preferred size, if the layout has extra
	 * room to fill.
	 * <p>
	 * Most applications do not call this method directly.
	 * @return an array of two arrays, representing the
	 *                    horizontal weights of the layout columns
	 *                    and the vertical weights of the layout rows
	 */
	public double[][] getLayoutWeights() {
		if (layoutInfo == null) {
			return new double[2][0];
		}

		double weights[][] = new double[2][];
		weights[0] = new double[layoutInfo.width];
		weights[1] = new double[layoutInfo.height];

		System.arraycopy(layoutInfo.weightX, 0, weights[0], 0, layoutInfo.width);
		System.arraycopy(layoutInfo.weightY, 0, weights[1], 0, layoutInfo.height);

		return weights;
	}

	/**
	 * Determines which cell in the layout grid contains the point
	 * specified by <code>(x,&nbsp;y)</code>. Each cell is identified
	 * by its column index (ranging from 0 to the number of columns
	 * minus 1) and its row index (ranging from 0 to the number of
	 * rows minus 1).
	 * <p>
	 * If the <code>(x,&nbsp;y)</code> point lies
	 * outside the grid, the following rules are used.
	 * The column index is returned as zero if <code>x</code> lies to the
	 * left of the layout for a left-to-right container or to the right of
	 * the layout for a right-to-left container.  The column index is returned
	 * as the number of columns if <code>x</code> lies
	 * to the right of the layout in a left-to-right container or to the left
	 * in a right-to-left container.
	 * The row index is returned as zero if <code>y</code> lies above the
	 * layout, and as the number of rows if <code>y</code> lies
	 * below the layout.  The orientation of a container is determined by its
	 * <code>ComponentOrientation</code> property.
	 * @param			x		the <i>x</i> coordinate of a point
	 * @param			y		the <i>y</i> coordinate of a point
	 * @return an ordered pair of indexes that indicate which cell
	 *             in the layout grid contains the point
	 *             (<i>x</i>,&nbsp;<i>y</i>).
	 */
	public Point2D location(int x, int y) {
		Point2D loc = new Point2D.Double(0d, 0d);
		int i;
		double d, p1, p2;

		if (layoutInfo == null) {
			return loc;
		}

		d = layoutInfo.startx;
		if (!rightToLeft) {
			for (i = 0; i < layoutInfo.width; i++) {
				d += layoutInfo.minWidth[i];
				if (d > x) {
					break;
				}
			}
		}
		else {
			for (i = layoutInfo.width - 1; i >= 0; i--) {
				if (d > x) {
					break;
				}
				d += layoutInfo.minWidth[i];
			}
			i++;
		}
		p1 = i;

		d = layoutInfo.starty;
		for (i = 0; i < layoutInfo.height; i++) {
			d += layoutInfo.minHeight[i];
			if (d > y) {
				break;
			}
		}
		p2 = i;

		loc.setLocation(p1, p2);
		return loc;
	}

	/**
	 * Adds the specified component with the specified name to the layout.
	 * @param			name				 the name of the component
	 * @param			shape				 the component to be added
	 */
	@Override
	public void addLayoutShape(String name, Shape shape) {
	}

	/**
	 * Adds the specified component to the layout, using the specified
	 * <code>constraints</code> object.  Note that constraints
	 * are mutable and are, therefore, cloned when cached.
	 *
	 * @param			shape				 the component to be added
	 * @param			constraints	an object that determines how
	 *                          the component is added to the layout
	 * @exception IllegalArgumentException if <code>constraints</code>
	 *		  is not a <code>GridBagConstraint</code>
	 */
	@Override
	public void addLayoutShape(Shape shape, Object constraints) {
		if (constraints instanceof GridBagConstraints) {
			setConstraints(shape, (GridBagConstraints) constraints);
		}
		else if (constraints != null) {
			throw new IllegalArgumentException("cannot add to layout: constraints must be a GridBagConstraint");
		}
	}

	/**
	 * Removes the specified component from this layout.
	 * <p>
	 * Most applications do not call this method directly.
	 * @param		shape	 the component to be removed.
	 */
	@Override
	public void removeLayoutShape(Shape shape) {
		removeConstraints(shape);
	}

	/**
	 * Determines the preferred size of the <code>parent</code>
	 * container using this grid bag layout.
	 * <p>
	 * Most applications do not call this method directly.
	 *
	 * @param		 parent	 the container in which to do the layout
	 * @return the preferred size of the <code>parent</code>
	 *  container
	 */
	@Override
	public Extents2D preferredLayoutSize(ContainerShape parent) {
		GridBagLayoutInfo info = getLayoutInfo(parent, PREFERREDSIZE);
		return getMinSize(parent, info);
	}

	/**
	 * Determines the minimum size of the <code>parent</code> container
	 * using this grid bag layout.
	 * <p>
	 * Most applications do not call this method directly.
	 * @param		 parent	 the container in which to do the layout
	 * @return the minimum size of the <code>parent</code> container
	 */
	@Override
	public Extents2D minimumLayoutSize(ContainerShape parent) {
		GridBagLayoutInfo info = getLayoutInfo(parent, MINSIZE);
		return getMinSize(parent, info);
	}

	/**
	 * Returns the maximum dimensions for this layout given the components
	 * in the specified target container.
	 * @param target the container which needs to be laid out
	 * @return the maximum dimensions for this layout
	 */
	@Override
	public Extents2D maximumLayoutSize(ContainerShape target) {
		return new Extents2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis.  This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 * <p>
	 * @return the value <code>0.5f</code> to indicate centered
	 */
	@Override
	public double getLayoutAlignmentX(ContainerShape parent) {
		return 0.5d;
	}

	/**
	 * Returns the alignment along the y axis.  This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 * <p>
	 * @return the value <code>0.5f</code> to indicate centered
	 */
	@Override
	public double getLayoutAlignmentY(ContainerShape parent) {
		return 0.5d;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 */
	@Override
	public void invalidateLayout(ContainerShape target) {
	}

	/**
	 * Lays out the specified container using this grid bag layout.
	 * This method reshapes components in the specified container in
	 * order to satisfy the contraints of this <code>GridBagLayout</code>
	 * object.
	 * <p>
	 * Most applications do not call this method directly.
	 * @param parent the container in which to do the layout
	 */
	@Override
	public void layoutContainer(ContainerShape parent) {
		arrangeGrid(parent);
	}

	/**
	 * Returns a string representation of this grid bag layout's values.
	 * @return a string representation of this grid bag layout.
	 */
	@Override
	public String toString() {
		return getClass().getName();
	}

	/**
	 * Print the layout information.  Useful for debugging.
	 */

	/* DEBUG
			*
			*  protected void dumpLayoutInfo(GridBagLayoutInfo s) {
			*    int x;
			*
			*    System.out.println("Col\tWidth\tWeight");
			*    for (x=0; x<s.width; x++) {
			*      System.out.println(x + "\t" +
			*			 s.minWidth[x] + "\t" +
			*			 s.weightX[x]);
			*    }
			*    System.out.println("Row\tHeight\tWeight");
			*    for (x=0; x<s.height; x++) {
			*      System.out.println(x + "\t" +
			*			 s.minHeight[x] + "\t" +
			*			 s.weightY[x]);
			*    }
			*  }
			*/

	/**
	 * Print the layout constraints.  Useful for debugging.
	 */

	/* DEBUG
			*
			*  protected void dumpConstraints(GridBagConstraints constraints) {
			*    System.out.println(
			*		       "wt " +
			*		       constraints.weightx +
			*		       " " +
			*		       constraints.weighty +
			*		       ", " +
			*
			*		       "box " +
			*		       constraints.gridx +
			*		       " " +
			*		       constraints.gridy +
			*		       " " +
			*		       constraints.gridwidth +
			*		       " " +
			*		       constraints.gridheight +
			*		       ", " +
			*
			*		       "min " +
			*		       constraints.minWidth +
			*		       " " +
			*		       constraints.minHeight +
			*		       ", " +
			*
			*		       "pad " +
			*		       constraints.insets.bottom +
			*		       " " +
			*		       constraints.insets.left +
			*		       " " +
			*		       constraints.insets.right +
			*		       " " +
			*		       constraints.insets.top +
			*		       " " +
			*		       constraints.ipadx +
			*		       " " +
			*		       constraints.ipady);
			*  }
			*/

	/**
	 * Fills in an instance of <code>GridBagLayoutInfo</code> for the
	 * current set of managed children. This requires three passes through the
	 * set of children:
	 *
	 * <ol>
	 * <li>Figure out the dimensions of the layout grid.
	 * <li>Determine which cells the components occupy.
	 * <li>Distribute the weights and min sizes amoung the rows/columns.
	 * </ol>
	 *
	 * This also caches the minsizes for all the children when they are
	 * first encountered (so subsequent loops don't need to ask again).
	 * @param parent	the layout container
	 * @param sizeflag either <code>PREFERREDSIZE</code> or
	 *   <code>MINSIZE</code>
	 * @return the <code>GridBagLayoutInfo</code> for the set of children
	 */
	protected GridBagLayoutInfo getLayoutInfo(ContainerShape parent, int sizeflag) {
		return GetLayoutInfo(parent, sizeflag);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only;
	 * new code should call getLayoutInfo() instead.
	 */
	protected GridBagLayoutInfo GetLayoutInfo(ContainerShape parent, int sizeflag) {
		GridBagLayoutInfo r = new GridBagLayoutInfo();
		Shape comp;
		GridBagConstraints constraints;
		Extents2D d;
		Shape components[] = parent.getShapeArray();

		int compindex, i, k, px, py, nextSize;
		int curX, curY, curWidth, curHeight, curRow, curCol;
		double weight_diff, weight, pixels_diff;
		int xMax[], yMax[];

		/*
					 * Pass #1
					 *
					 * Figure out the dimensions of the layout grid (use a value of 1 for
					 * zero or negative widths and heights).
					 */

		r.width = r.height = 0;
		curRow = curCol = -1;
		xMax = new int[MAXGRIDSIZE];
		yMax = new int[MAXGRIDSIZE];

		for (compindex = 0; compindex < components.length; compindex++) {
			comp = components[compindex];
			if (!comp.isVisible()) {
				continue;
			}
			constraints = lookupConstraints(comp);

			curX = constraints.gridx;
			curY = constraints.gridy;
			curWidth = constraints.gridwidth;
			if (curWidth <= 0) {
				curWidth = 1;
			}
			curHeight = constraints.gridheight;
			if (curHeight <= 0) {
				curHeight = 1;
			}

			/* If x or y is negative, then use relative positioning: */
			if (curX < 0 && curY < 0) {
				if (curRow >= 0) {
					curY = curRow;
				}
				else if (curCol >= 0) {
					curX = curCol;
				}
				else {
					curY = 0;
				}
			}
			if (curX < 0) {
				px = 0;
				for (i = curY; i < (curY + curHeight); i++) {
					px = Math.max(px, xMax[i]);
				}

				curX = px - curX - 1;
				if (curX < 0) {
					curX = 0;
				}
			}
			else if (curY < 0) {
				py = 0;
				for (i = curX; i < (curX + curWidth); i++) {
					py = Math.max(py, yMax[i]);
				}

				curY = py - curY - 1;
				if (curY < 0) {
					curY = 0;
				}
			}

			/* Adjust the grid width and height */
			for (px = curX + curWidth; r.width < px; r.width++) {
				;
			}
			for (py = curY + curHeight; r.height < py; r.height++) {
				;
			}

			/* Adjust the xMax and yMax arrays */
			for (i = curX; i < (curX + curWidth); i++) {
				yMax[i] = py;
			}
			for (i = curY; i < (curY + curHeight); i++) {
				xMax[i] = px;
			}

			/* Cache the current slave's size. */
			if (sizeflag == PREFERREDSIZE) {
				d = comp.getPreferredSize();
			}
			else {
				d = comp.getMinimumSize();
			}
			constraints.minWidth = (int) Math.round(d.width);
			constraints.minHeight = (int) Math.round(d.height);

			/* Zero width and height must mean that this is the last item (or
								* else something is wrong). */
			if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
				curRow = curCol = -1;
			}

			/* Zero width starts a new row */
			if (constraints.gridheight == 0 && curRow < 0) {
				curCol = curX + curWidth;
			}

			/* Zero height starts a new column */
			else if (constraints.gridwidth == 0 && curCol < 0) {
				curRow = curY + curHeight;
			}
		}

		/*
					 * Apply minimum row/column dimensions
					 */
		if (columnWidths != null && r.width < columnWidths.length) {
			r.width = columnWidths.length;
		}
		if (rowHeights != null && r.height < rowHeights.length) {
			r.height = rowHeights.length;
		}

		/*
					 * Pass #2
					 *
					 * Negative values for gridX are filled in with the current x value.
					 * Negative values for gridY are filled in with the current y value.
					 * Negative or zero values for gridWidth and gridHeight end the current
					 *  row or column, respectively.
					 */

		curRow = curCol = -1;
		xMax = new int[MAXGRIDSIZE];
		yMax = new int[MAXGRIDSIZE];

		for (compindex = 0; compindex < components.length; compindex++) {
			comp = components[compindex];
			if (!comp.isVisible()) {
				continue;
			}
			constraints = lookupConstraints(comp);

			curX = constraints.gridx;
			curY = constraints.gridy;
			curWidth = constraints.gridwidth;
			curHeight = constraints.gridheight;

			/* If x or y is negative, then use relative positioning: */
			if (curX < 0 && curY < 0) {
				if (curRow >= 0) {
					curY = curRow;
				}
				else if (curCol >= 0) {
					curX = curCol;
				}
				else {
					curY = 0;
				}
			}

			if (curX < 0) {
				if (curHeight <= 0) {
					curHeight += r.height - curY;
					if (curHeight < 1) {
						curHeight = 1;
					}
				}

				px = 0;
				for (i = curY; i < (curY + curHeight); i++) {
					px = Math.max(px, xMax[i]);
				}

				curX = px - curX - 1;
				if (curX < 0) {
					curX = 0;
				}
			}
			else if (curY < 0) {
				if (curWidth <= 0) {
					curWidth += r.width - curX;
					if (curWidth < 1) {
						curWidth = 1;
					}
				}

				py = 0;
				for (i = curX; i < (curX + curWidth); i++) {
					py = Math.max(py, yMax[i]);
				}

				curY = py - curY - 1;
				if (curY < 0) {
					curY = 0;
				}
			}

			if (curWidth <= 0) {
				curWidth += r.width - curX;
				if (curWidth < 1) {
					curWidth = 1;
				}
			}

			if (curHeight <= 0) {
				curHeight += r.height - curY;
				if (curHeight < 1) {
					curHeight = 1;
				}
			}

			px = curX + curWidth;
			py = curY + curHeight;

			for (i = curX; i < (curX + curWidth); i++) {
				yMax[i] = py;
			}
			for (i = curY; i < (curY + curHeight); i++) {
				xMax[i] = px;
			}

			/* Make negative sizes start a new row/column */
			if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
				curRow = curCol = -1;
			}
			if (constraints.gridheight == 0 && curRow < 0) {
				curCol = curX + curWidth;
			}
			else if (constraints.gridwidth == 0 && curCol < 0) {
				curRow = curY + curHeight;
			}

			/* Assign the new values to the gridbag slave */
			constraints.tempX = curX;
			constraints.tempY = curY;
			constraints.tempWidth = curWidth;
			constraints.tempHeight = curHeight;
		}

		/*
					 * Apply minimum row/column dimensions and weights
					 */
		if (columnWidths != null) {
			System.arraycopy(columnWidths, 0, r.minWidth, 0, columnWidths.length);
		}
		if (rowHeights != null) {
			System.arraycopy(rowHeights, 0, r.minHeight, 0, rowHeights.length);
		}
		if (columnWeights != null) {
			System.arraycopy(columnWeights, 0, r.weightX, 0, columnWeights.length);
		}
		if (rowWeights != null) {
			System.arraycopy(rowWeights, 0, r.weightY, 0, rowWeights.length);
		}

		/*
					 * Pass #3
					 *
					 * Distribute the minimun widths and weights:
					 */

		nextSize = Integer.MAX_VALUE;

		for (i = 1;
				i != Integer.MAX_VALUE;
				i = nextSize, nextSize = Integer.MAX_VALUE) {
			for (compindex = 0; compindex < components.length; compindex++) {
				comp = components[compindex];
				if (!comp.isVisible()) {
					continue;
				}
				constraints = lookupConstraints(comp);

				if (constraints.tempWidth == i) {
					px = constraints.tempX + constraints.tempWidth; /* right column */

					/*
													* Figure out if we should use this slave\'s weight.  If the weight
													* is less than the total weight spanned by the width of the cell,
													* then discard the weight.  Otherwise split the difference
													* according to the existing weights.
													*/

					weight_diff = constraints.weightx;
					for (k = constraints.tempX; k < px; k++) {
						weight_diff -= r.weightX[k];
					}
					if (weight_diff > 0.0) {
						weight = 0.0;
						for (k = constraints.tempX; k < px; k++) {
							weight += r.weightX[k];
						}
						for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
							double wt = r.weightX[k];
							double dx = (wt * weight_diff) / weight;
							r.weightX[k] += dx;
							weight_diff -= dx;
							weight -= wt;
						}
						/* Assign the remainder to the rightmost cell */
						r.weightX[px - 1] += weight_diff;
					}

					/*
													* Calculate the minWidth array values.
													* First, figure out how wide the current slave needs to be.
													* Then, see if it will fit within the current minWidth values.
													* If it will not fit, add the difference according to the
													* weightX array.
													*/

					pixels_diff =
							constraints.minWidth + constraints.ipadx +
									constraints.insets.left + constraints.insets.right;

					for (k = constraints.tempX; k < px; k++) {
						pixels_diff -= r.minWidth[k];
					}
					if (pixels_diff > 0) {
						weight = 0.0;
						for (k = constraints.tempX; k < px; k++) {
							weight += r.weightX[k];
						}
						for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
							double wt = r.weightX[k];
							int dx = (int) ((wt * (pixels_diff)) / weight);
							r.minWidth[k] += dx;
							pixels_diff -= dx;
							weight -= wt;
						}
						/* Any leftovers go into the rightmost cell */
						r.minWidth[px - 1] += pixels_diff;
					}
				}
				else if (constraints.tempWidth > i && constraints.tempWidth < nextSize) {
					nextSize = constraints.tempWidth;
				}

				if (constraints.tempHeight == i) {
					py = constraints.tempY + constraints.tempHeight; /* bottom row */

					/*
													* Figure out if we should use this slave's weight.  If the weight
													* is less than the total weight spanned by the height of the cell,
													* then discard the weight.  Otherwise split it the difference
													* according to the existing weights.
													*/

					weight_diff = constraints.weighty;
					for (k = constraints.tempY; k < py; k++) {
						weight_diff -= r.weightY[k];
					}
					if (weight_diff > 0.0) {
						weight = 0.0;
						for (k = constraints.tempY; k < py; k++) {
							weight += r.weightY[k];
						}
						for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
							double wt = r.weightY[k];
							double dy = (wt * weight_diff) / weight;
							r.weightY[k] += dy;
							weight_diff -= dy;
							weight -= wt;
						}
						/* Assign the remainder to the bottom cell */
						r.weightY[py - 1] += weight_diff;
					}

					/*
													* Calculate the minHeight array values.
													* First, figure out how tall the current slave needs to be.
													* Then, see if it will fit within the current minHeight values.
													* If it will not fit, add the difference according to the
													* weightY array.
													*/

					pixels_diff =
							constraints.minHeight + constraints.ipady +
									constraints.insets.top + constraints.insets.bottom;
					for (k = constraints.tempY; k < py; k++) {
						pixels_diff -= r.minHeight[k];
					}
					if (pixels_diff > 0) {
						weight = 0.0;
						for (k = constraints.tempY; k < py; k++) {
							weight += r.weightY[k];
						}
						for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
							double wt = r.weightY[k];
							int dy = (int) ((wt * (pixels_diff)) / weight);
							r.minHeight[k] += dy;
							pixels_diff -= dy;
							weight -= wt;
						}
						/* Any leftovers go into the bottom cell */
						r.minHeight[py - 1] += pixels_diff;
					}
				}
				else if (constraints.tempHeight > i &&
						constraints.tempHeight < nextSize) {
					nextSize = constraints.tempHeight;
				}
			}
		}

		return r;
	}

	/**
	 * Adjusts the x, y, width, and height fields to the correct
	 * values depending on the constraint geometry and pads.
	 * @param constraints the constraints to be applied
	 * @param r the <code>Rectangle</code> to be adjusted
	 * @since 1.4
	 */
	protected void adjustForGravity(GridBagConstraints constraints,
			Rectangle r) {
		AdjustForGravity(constraints, r);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only;
	 * new code should call <code>adjustForGravity</code> instead.
	 */
	protected void AdjustForGravity(GridBagConstraints constraints,
			Rectangle r) {
		int diffx, diffy;

		if (!rightToLeft) {
			r.x += constraints.insets.left;
		}
		else {
			r.x -= r.width - constraints.insets.right;
		}
		r.width -= (constraints.insets.left + constraints.insets.right);
		r.y += constraints.insets.top;
		r.height -= (constraints.insets.top + constraints.insets.bottom);

		diffx = 0;
		if ((constraints.fill != GridBagConstraints.HORIZONTAL &&
				constraints.fill != GridBagConstraints.BOTH)
				&& (r.width > (constraints.minWidth + constraints.ipadx))) {
			diffx = r.width - (constraints.minWidth + constraints.ipadx);
			r.width = constraints.minWidth + constraints.ipadx;
		}

		diffy = 0;
		if ((constraints.fill != GridBagConstraints.VERTICAL &&
				constraints.fill != GridBagConstraints.BOTH)
				&& (r.height > (constraints.minHeight + constraints.ipady))) {
			diffy = r.height - (constraints.minHeight + constraints.ipady);
			r.height = constraints.minHeight + constraints.ipady;
		}

		switch (constraints.anchor) {
			case GridBagConstraints.CENTER:
				r.x += diffx / 2;
				r.y += diffy / 2;
				break;
			case GridBagConstraints.PAGE_START:
			case GridBagConstraints.NORTH:
				r.x += diffx / 2;
				break;
			case GridBagConstraints.NORTHEAST:
				r.x += diffx;
				break;
			case GridBagConstraints.EAST:
				r.x += diffx;
				r.y += diffy / 2;
				break;
			case GridBagConstraints.SOUTHEAST:
				r.x += diffx;
				r.y += diffy;
				break;
			case GridBagConstraints.PAGE_END:
			case GridBagConstraints.SOUTH:
				r.x += diffx / 2;
				r.y += diffy;
				break;
			case GridBagConstraints.SOUTHWEST:
				r.y += diffy;
				break;
			case GridBagConstraints.WEST:
				r.y += diffy / 2;
				break;
			case GridBagConstraints.NORTHWEST:
				break;
			case GridBagConstraints.LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy / 2;
				break;
			case GridBagConstraints.LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy / 2;
				break;
			case GridBagConstraints.FIRST_LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				break;
			case GridBagConstraints.FIRST_LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				break;
			case GridBagConstraints.LAST_LINE_START:
				if (rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy;
				break;
			case GridBagConstraints.LAST_LINE_END:
				if (!rightToLeft) {
					r.x += diffx;
				}
				r.y += diffy;
				break;
			default:
				throw new IllegalArgumentException("illegal anchor value");
		}
	}

	/**
	 * Figures out the minimum size of the
	 * master based on the information from getLayoutInfo().
	 * @param parent the layout container
	 * @param info the layout info for this parent
	 * @return a <code>Dimension</code> object containing the
	 *   minimum size
	 * @since 1.4
	 */
	protected Extents2D getMinSize(ContainerShape parent, GridBagLayoutInfo info) {
		return GetMinSize(parent, info);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only;
	 * new code should call <code>getMinSize</code> instead.
	 */
	protected Extents2D GetMinSize(ContainerShape parent, GridBagLayoutInfo info) {
		Extents2D d = new Extents2D();
		int i, t;
		Insets2D insets = parent.getInsets();

		t = 0;
		for (i = 0; i < info.width; i++) {
			t += info.minWidth[i];
		}
		d.width = t + insets.left + insets.right;

		t = 0;
		for (i = 0; i < info.height; i++) {
			t += info.minHeight[i];
		}
		d.height = t + insets.top + insets.bottom;

		return d;
	}

	transient boolean rightToLeft = false;

	/**
	 * Lays out the grid.
	 * @param parent the layout container
	 */
	protected void arrangeGrid(ContainerShape parent) {
		ArrangeGrid(parent);
	}

	/**
	 * This method is obsolete and supplied for backwards compatability only;
	 * new code should call <code>arrangeGrid</code> instead.
	 */
	protected void ArrangeGrid(ContainerShape parent) {
		Shape comp;
		int compindex;
		GridBagConstraints constraints;
		Insets2D insets = parent.getInsets();
		Shape components[] = parent.getShapeArray();
		Extents2D d;
		Rectangle r = new Rectangle();
		int i, diffw, diffh;
		double weight;
		GridBagLayoutInfo info;

		rightToLeft = !parent.getComponentOrientation().isLeftToRight();

		/*
					 * If the parent has no slaves anymore, then don't do anything
					 * at all:  just leave the parent's size as-is.
					 */
		if (components.length == 0 &&
				(columnWidths == null || columnWidths.length == 0) &&
				(rowHeights == null || rowHeights.length == 0)) {
			return;
		}

		/*
					 * Pass #1: scan all the slaves to figure out the total amount
					 * of space needed.
					 */

		info = getLayoutInfo(parent, PREFERREDSIZE);
		d = getMinSize(parent, info);

		if (parent.getWidth() < d.width || parent.getHeight() < d.height) {
			info = getLayoutInfo(parent, MINSIZE);
			d = getMinSize(parent, info);
		}

		layoutInfo = info;
		r.width = (int) Math.round(d.width);
		r.height = (int) Math.round(d.height);

		/*
					 * DEBUG
					 *
					 * DumpLayoutInfo(info);
					 * for (compindex = 0 ; compindex < components.length ; compindex++) {
					 * comp = components[compindex];
					 * if (!comp.isVisible())
					 *	continue;
					 * constraints = lookupConstraints(comp);
					 * DumpConstraints(constraints);
					 * }
					 * System.out.println("minSize " + r.width + " " + r.height);
					 */

		/*
					 * If the current dimensions of the window don't match the desired
					 * dimensions, then adjust the minWidth and minHeight arrays
					 * according to the weights.
					 */

		diffw = (int) Math.round(parent.getWidth() - r.width);
		if (diffw != 0) {
			weight = 0.0;
			for (i = 0; i < info.width; i++) {
				weight += info.weightX[i];
			}
			if (weight > 0.0) {
				for (i = 0; i < info.width; i++) {
					int dx = (int) (((diffw) * info.weightX[i]) / weight);
					info.minWidth[i] += dx;
					r.width += dx;
					if (info.minWidth[i] < 0) {
						r.width -= info.minWidth[i];
						info.minWidth[i] = 0;
					}
				}
			}
			diffw = (int) Math.round(parent.getWidth() - r.width);
		}
		else {
			diffw = 0;
		}

		diffh = (int) Math.round(parent.getHeight() - r.height);
		if (diffh != 0) {
			weight = 0.0;
			for (i = 0; i < info.height; i++) {
				weight += info.weightY[i];
			}
			if (weight > 0.0) {
				for (i = 0; i < info.height; i++) {
					int dy = (int) (((diffh) * info.weightY[i]) / weight);
					info.minHeight[i] += dy;
					r.height += dy;
					if (info.minHeight[i] < 0) {
						r.height -= info.minHeight[i];
						info.minHeight[i] = 0;
					}
				}
			}
			diffh = (int) Math.round(parent.getHeight() - r.height);
		}
		else {
			diffh = 0;
		}

		/*
					 * DEBUG
					 *
					 * System.out.println("Re-adjusted:");
					 * DumpLayoutInfo(info);
					 */

		/*
					 * Now do the actual layout of the slaves using the layout information
					 * that has been collected.
					 */

		info.startx = diffw / 2 + insets.left;
		info.starty = diffh / 2 + insets.top;

		for (compindex = 0; compindex < components.length; compindex++) {
			comp = components[compindex];
			if (!comp.isVisible()) {
				continue;
			}
			constraints = lookupConstraints(comp);

			if (!rightToLeft) {
				r.x = (int) info.startx;
				for (i = 0; i < constraints.tempX; i++) {
					r.x += info.minWidth[i];
				}
			}
			else {
				r.x = (int) Math.round(parent.getWidth() - (diffw / 2 + insets.right));
				for (i = 0; i < constraints.tempX; i++) {
					r.x -= info.minWidth[i];
				}
			}

			r.y = (int) info.starty;
			for (i = 0; i < constraints.tempY; i++) {
				r.y += info.minHeight[i];
			}

			r.width = 0;
			for (i = constraints.tempX;
					i < (constraints.tempX + constraints.tempWidth);
					i++) {
				r.width += info.minWidth[i];
			}

			r.height = 0;
			for (i = constraints.tempY;
					i < (constraints.tempY + constraints.tempHeight);
					i++) {
				r.height += info.minHeight[i];
			}

			adjustForGravity(constraints, r);

			/* fix for 4408108 - components were being created outside of the container */
			if (r.x < 0) {
				r.width -= r.x;
				r.x = 0;
			}

			if (r.y < 0) {
				r.height -= r.y;
				r.y = 0;
			}

			/*
								* If the window is too small to be interesting then
								* unmap it.  Otherwise configure it and then make sure
								* it's mapped.
								*/

			if ((r.width <= 0) || (r.height <= 0)) {
				comp.setDimension(new Rectangle2D.Double(0d, 0d, 0d, 0d));
			}
			else {
				if (comp.getX() != r.x || comp.getY() != r.y ||
						comp.getWidth() != r.width || comp.getHeight() != r.height) {
					comp.setDimension(new Rectangle2D.Double(r.x, r.y, r.width, r.height));
				}
			}
		}
	}
}

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

/**
 * Gridbag constraints.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class GridBagConstraints {
	/**
	 * Specifies that this shape is the next-to-last shape in its
	 * column or row (<code>gridwidth</code>, <code>gridheight</code>),
	 * or that this shape be placed next to the previously added
	 * shape (<code>gridx</code>, <code>gridy</code>).
	 */
	public static final int RELATIVE = -1;

	/**
	 * Specifies that this shape is the
	 * last shape in its column or row.
	 */
	public static final int REMAINDER = 0;

	/**
	 * Do not resize the shape.
	 */
	public static final int NONE = 0;

	/**
	 * Resize the shape both horizontally and vertically.
	 */
	public static final int BOTH = 1;

	/**
	 * Resize the shape horizontally but not vertically.
	 */
	public static final int HORIZONTAL = 2;

	/**
	 * Resize the shape vertically but not horizontally.
	 */
	public static final int VERTICAL = 3;

	/**
	 * Put the shape in the center of its display area.
	 */
	public static final int CENTER = 10;

	/**
	 * Put the shape at the top of its display area,
	 * centered horizontally.
	 */
	public static final int NORTH = 11;

	/**
	 * Put the shape at the top-right corner of its display area.
	 */
	public static final int NORTHEAST = 12;

	/**
	 * Put the shape on the right side of its display area,
	 * centered vertically.
	 */
	public static final int EAST = 13;

	/**
	 * Put the shape at the bottom-right corner of its display area.
	 */
	public static final int SOUTHEAST = 14;

	/**
	 * Put the shape at the bottom of its display area, centered
	 * horizontally.
	 */
	public static final int SOUTH = 15;

	/**
	 * Put the shape at the bottom-left corner of its display area.
	 */
	public static final int SOUTHWEST = 16;

	/**
	 * Put the shape on the left side of its display area,
	 * centered vertically.
	 */
	public static final int WEST = 17;

	/**
	 * Put the shape at the top-left corner of its display area.
	 */
	public static final int NORTHWEST = 18;

	/**
	 * Place the shape centered along the edge of its display area
	 * associated with the start of a page for the current
	 * <code>ComponentOrienation</code>.  Equal to NORTH for horizontal
	 * orientations.
	 */
	public static final int PAGE_START = 19;

	/**
	 * Place the shape centered along the edge of its display area
	 * associated with the end of a page for the current
	 * <code>ComponentOrienation</code>.  Equal to SOUTH for horizontal
	 * orientations.
	 */
	public static final int PAGE_END = 20;

	/**
	 * Place the shape centered along the edge of its display area where
	 * lines of text would normally begin for the current
	 * <code>ComponentOrienation</code>.  Equal to WEST for horizontal,
	 * left-to-right orientations and EAST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int LINE_START = 21;

	/**
	 * Place the shape centered along the edge of its display area where
	 * lines of text would normally end for the current
	 * <code>ComponentOrienation</code>.  Equal to EAST for horizontal,
	 * left-to-right orientations and WEST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int LINE_END = 22;

	/**
	 * Place the shape in the corner of its display area where
	 * the first line of text on a page would normally begin for the current
	 * <code>ComponentOrienation</code>.  Equal to NORTHWEST for horizontal,
	 * left-to-right orientations and NORTHEAST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int FIRST_LINE_START = 23;

	/**
	 * Place the shape in the corner of its display area where
	 * the first line of text on a page would normally end for the current
	 * <code>ComponentOrienation</code>.  Equal to NORTHEAST for horizontal,
	 * left-to-right orientations and NORTHWEST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int FIRST_LINE_END = 24;

	/**
	 * Place the shape in the corner of its display area where
	 * the last line of text on a page would normally start for the current
	 * <code>ComponentOrienation</code>.  Equal to SOUTHWEST for horizontal,
	 * left-to-right orientations and SOUTHEAST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int LAST_LINE_START = 25;

	/**
	 * Place the shape in the corner of its display area where
	 * the last line of text on a page would normally end for the current
	 * <code>ComponentOrienation</code>.  Equal to SOUTHEAST for horizontal,
	 * left-to-right orientations and SOUTHWEST for horizontal, right-to-left
	 * orientations.
	 */
	public static final int LAST_LINE_END = 26;

	/**
	 * Specifies the cell containing the leading edge of the shape's
	 * display area, where the first cell in a row has <code>gridx=0</code>.
	 * The leading edge of a shape's display area is its left edge for
	 * a horizontal, left-to-right container and its right edge for a
	 * horizontal, right-to-left container.
	 * The value
	 * <code>RELATIVE</code> specifies that the shape be placed
	 * immediately following the shape that was added to the container
	 * just before this shape was added.
	 * <p>
	 * The default value is <code>RELATIVE</code>.
	 * <code>gridx</code> should be a non-negative value.
	 */
	public int gridx;

	/**
	 * Specifies the cell at the top of the shape's display area,
	 * where the topmost cell has <code>gridy=0</code>. The value
	 * <code>RELATIVE</code> specifies that the shape be placed just
	 * below the shape that was added to the container just before
	 * this shape was added.
	 * <p>
	 * The default value is <code>RELATIVE</code>.
	 * <code>gridy</code> should be a non-negative value.
	 */
	public int gridy;

	/**
	 * Specifies the number of cells in a row for the shape's
	 * display area.
	 * <p>
	 * Use <code>REMAINDER</code> to specify that the shape be the
	 * last one in its row. Use <code>RELATIVE</code> to specify that the
	 * shape be the next-to-last one in its row.
	 * <p>
	 * <code>gridwidth</code> should be non-negative and the default
	 * value is 1.
	 */
	public int gridwidth;

	/**
	 * Specifies the number of cells in a column for the shape's
	 * display area.
	 * <p>
	 * Use <code>REMAINDER</code> to specify that the shape be the
	 * last one in its column. Use <code>RELATIVE</code> to specify that
	 * the shape be the next-to-last one in its column.
	 * <p>
	 * <code>gridheight</code> should be a non-negative value and the
	 * default value is 1.
	 */
	public int gridheight;

	/**
	 * Specifies how to distribute extra horizontal space.
	 * <p>
	 * The grid bag layout manager calculates the weight of a column to
	 * be the maximum <code>weightx</code> of all the shapes in a
	 * column. If the resulting layout is smaller horizontally than the area
	 * it needs to fill, the extra space is distributed to each column in
	 * proportion to its weight. A column that has a weight of zero receives
	 * no extra space.
	 * <p>
	 * If all the weights are zero, all the extra space appears between
	 * the grids of the cell and the left and right edges.
	 * <p>
	 * The default value of this field is <code>0</code>.
	 * <code>weightx</code> should be a non-negative value.
	 */
	public double weightx;

	/**
	 * Specifies how to distribute extra vertical space.
	 * <p>
	 * The grid bag layout manager calculates the weight of a row to be
	 * the maximum <code>weighty</code> of all the shapes in a row.
	 * If the resulting layout is smaller vertically than the area it
	 * needs to fill, the extra space is distributed to each row in
	 * proportion to its weight. A row that has a weight of zero receives no
	 * extra space.
	 * <p>
	 * If all the weights are zero, all the extra space appears between
	 * the grids of the cell and the top and bottom edges.
	 * <p>
	 * The default value of this field is <code>0</code>.
	 * <code>weighty</code> should be a non-negative value.
	 */
	public double weighty;

	/**
	 * This field is used when the shape is smaller than its display
	 * area. It determines where, within the display area, to place the
	 * shape.
	 * <p>
	 * There are two kinds of possible values: relative and
	 * absolute.  Relative values are interpreted relative to the container's
	 * shape orientation property while absolute values are not.  The absolute
	 * values are:
	 * <code>CENTER</code>, <code>NORTH</code>, <code>NORTHEAST</code>,
	 * <code>EAST</code>, <code>SOUTHEAST</code>, <code>SOUTH</code>,
	 * <code>SOUTHWEST</code>, <code>WEST</code>, and <code>NORTHWEST</code>.
	 * The relative values are: <code>PAGE_START</code>, <code>PAGE_END</code>,
	 * <code>LINE_START</code>, <code>LINE_END</code>,
	 * <code>FIRST_LINE_START</code>, <code>FIRST_LINE_END</code>,
	 * <code>LAST_LINE_START</code> and <code>LAST_LINE_END</code>.
	 * The default value is <code>CENTER</code>.
	 */
	public int anchor;

	/**
	 * This field is used when the shape's display area is larger
	 * than the shape's requested size. It determines whether to
	 * resize the shape, and if so, how.
	 * <p>
	 * The following values are valid for <code>fill</code>:
	 * <p>
	 * <ul>
	 * <li>
	 * <code>NONE</code>: Do not resize the shape.
	 * <li>
	 * <code>HORIZONTAL</code>: Make the shape wide enough to fill
	 *         its display area horizontally, but do not change its height.
	 * <li>
	 * <code>VERTICAL</code>: Make the shape tall enough to fill its
	 *         display area vertically, but do not change its width.
	 * <li>
	 * <code>BOTH</code>: Make the shape fill its display area
	 *         entirely.
	 * </ul>
	 * <p>
	 * The default value is <code>NONE</code>.
	 */
	public int fill;

	/**
	 * This field specifies the external padding of the shape, the
	 * minimum amount of space between the shape and the edges of its
	 * display area.
	 * <p>
	 * The default value is <code>new Insets2D(0, 0, 0, 0)</code>.
	 */
	public Insets2D insets;

	/**
	 * This field specifies the internal padding of the shape, how much
	 * space to add to the minimum width of the shape. The width of
	 * the shape is at least its minimum width plus
	 * <code>(ipadx&nbsp;*&nbsp;2)</code> pixels.
	 * <p>
	 * The default value is <code>0</code>.
	 */
	public int ipadx;

	/**
	 * This field specifies the internal padding, that is, how much
	 * space to add to the minimum height of the shape. The height of
	 * the shape is at least its minimum height plus
	 * <code>(ipady&nbsp;*&nbsp;2)</code> pixels.
	 * <p>
	 * The default value is 0.
	 */
	public int ipady;

	/**
	 * Temporary place holder for the x coordinate.
	 */
	int tempX;
	/**
	 * Temporary place holder for the y coordinate.
	 */
	int tempY;
	/**
	 * Temporary place holder for the Width of the shape.
	 */
	int tempWidth;
	/**
	 * Temporary place holder for the Height of the shape.
	 */
	int tempHeight;
	/**
	 * The minimum width of the shape.  It is used to calculate
	 * <code>ipady</code>, where the default will be 0.
	 */
	int minWidth;
	/**
	 * The minimum height of the shape. It is used to calculate
	 * <code>ipadx</code>, where the default will be 0.
	 */
	int minHeight;

	/**
	 * Creates a <code>GridBagConstraint</code> object with
	 * all of its fields set to their default value.
	 */
	public GridBagConstraints() {
		gridx = RELATIVE;
		gridy = RELATIVE;
		gridwidth = 1;
		gridheight = 1;

		weightx = 0;
		weighty = 0;
		anchor = CENTER;
		fill = NONE;

		insets = new Insets2D(0, 0, 0, 0);
		ipadx = 0;
		ipady = 0;
	}

	/**
	 * Creates a <code>GridBagConstraints</code> object with
	 * all of its fields set to the passed-in arguments.
	 *
	 * Note: Because the use of this constructor hinders readability
	 * of source code, this constructor should only be used by
	 * automatic source code generation tools.
	 *
	 * @param gridx	The initial gridx value.
	 * @param gridy	The initial gridy value.
	 * @param gridwidth	The initial gridwidth value.
	 * @param gridheight	The initial gridheight value.
	 * @param weightx	The initial weightx value.
	 * @param weighty	The initial weighty value.
	 * @param anchor	The initial anchor value.
	 * @param fill	The initial fill value.
	 * @param insets	The initial insets value.
	 * @param ipadx	The initial ipadx value.
	 * @param ipady	The initial ipady value.
	 */
	public GridBagConstraints(int gridx, int gridy,
			int gridwidth, int gridheight,
			double weightx, double weighty,
			int anchor, int fill,
			Insets2D insets, int ipadx, int ipady) {
		this.gridx = gridx;
		this.gridy = gridy;
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;
		this.fill = fill;
		this.ipadx = ipadx;
		this.ipady = ipady;
		this.insets = insets;
		this.anchor = anchor;
		this.weightx = weightx;
		this.weighty = weighty;
	}

	/**
	 * Creates a copy of this grid bag constraint.
	 * @return a copy of this grid bag constraint
	 */
	@Override
	public Object clone() {
		try {
			GridBagConstraints c = (GridBagConstraints) super.clone();
			c.insets = (Insets2D) insets.clone();
			return c;
		}
		catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
}

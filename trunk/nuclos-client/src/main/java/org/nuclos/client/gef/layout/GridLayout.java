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
 * The <code>GridLayout</code> class is a layout manager that
 * lays out a container's shapes in a rectangular grid.
 * The container is divided into equal-sized rectangles,
 * and one shape is placed in each rectangle.
 * <p>
 * When both the number of rows and the number of columns have
 * been set to non-zero values, either by a constructor or
 * by the <tt>setRows</tt> and <tt>setColumns</tt> methods, the number of
 * columns specified is ignored.  Instead, the number of
 * columns is determined from the specified number or rows
 * and the total number of shapes in the layout. So, for
 * example, if three rows and two columns have been specified
 * and nine shapes are added to the layout, they will
 * be displayed as three rows of three columns.  Specifying
 * the number of columns affects the layout only when the
 * number of rows is set to zero.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class GridLayout {
	/**
	 * This is the horizontal gap which specifies the space
	 * between columns.  They can be changed at any time.
	 * This should be a non-negative integer.
	 */
	double hgap;
	/**
	 * This is the vertical gap which specifies the space
	 * between rows.  They can be changed at any time.
	 * This should be a non negative integer.
	 */
	double vgap;
	/**
	 * This is the number of rows specified for the grid.  The number
	 * of rows can be changed at any time.
	 * This should be a non negative integer, where '0' means
	 * 'any number' meaning that the number of Rows in that
	 * dimension depends on the other dimension.
	 */
	int rows;
	/**
	 * This is the number of columns specified for the grid.  The number
	 * of columns can be changed at any time.
	 * This should be a non negative integer, where '0' means
	 * 'any number' meaning that the number of Columns in that
	 * dimension depends on the other dimension.
	 */
	int cols;

	/**
	 * Creates a grid layout with a default of one column per shape,
	 * in a single row.
	 */
	public GridLayout() {
		this(1, 0, 0d, 0d);
	}

	/**
	 * Creates a grid layout with the specified number of rows and
	 * columns. All shapes in the layout are given equal size.
	 * <p>
	 * One, but not both, of <code>rows</code> and <code>cols</code> can
	 * be zero, which means that any number of objects can be placed in a
	 * row or in a column.
	 * @param		 rows	 the rows, with the value zero meaning
	 *                   any number of rows.
	 * @param		 cols	 the columns, with the value zero meaning
	 *                   any number of columns.
	 */
	public GridLayout(int rows, int cols) {
		this(rows, cols, 0d, 0d);
	}

	/**
	 * Creates a grid layout with the specified number of rows and
	 * columns. All shapes in the layout are given equal size.
	 * <p>
	 * In addition, the horizontal and vertical gaps are set to the
	 * specified values. Horizontal gaps are placed at the left and
	 * right edges, and between each of the columns. Vertical gaps are
	 * placed at the top and bottom edges, and between each of the rows.
	 * <p>
	 * One, but not both, of <code>rows</code> and <code>cols</code> can
	 * be zero, which means that any number of objects can be placed in a
	 * row or in a column.
	 * <p>
	 * All <code>GridLayout</code> constructors defer to this one.
	 * @param		 rows	 the rows, with the value zero meaning
	 *                   any number of rows
	 * @param		 cols	 the columns, with the value zero meaning
	 *                   any number of columns
	 * @param		 hgap	 the horizontal gap
	 * @param		 vgap	 the vertical gap
	 * @exception IllegalArgumentException	if the value of both
	 *			<code>rows</code> and <code>cols</code> is
	 *			set to zero
	 */
	public GridLayout(int rows, int cols, double hgap, double vgap) {
		if ((rows == 0) && (cols == 0)) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.rows = rows;
		this.cols = cols;
		this.hgap = hgap;
		this.vgap = vgap;
	}

	/**
	 * Gets the number of rows in this layout.
	 * @return the number of rows in this layout
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Sets the number of rows in this layout to the specified value.
	 * @param				rows	 the number of rows in this layout
	 * @exception IllegalArgumentException	if the value of both
	 *               <code>rows</code> and <code>cols</code> is set to zero
	 */
	public void setRows(int rows) {
		if ((rows == 0) && (this.cols == 0)) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.rows = rows;
	}

	/**
	 * Gets the number of columns in this layout.
	 * @return the number of columns in this layout
	 */
	public int getColumns() {
		return cols;
	}

	/**
	 * Sets the number of columns in this layout to the specified value.
	 * Setting the number of columns has no affect on the layout
	 * if the number of rows specified by a constructor or by
	 * the <tt>setRows</tt> method is non-zero. In that case, the number
	 * of columns displayed in the layout is determined by the total
	 * number of shapes and the number of rows specified.
	 * @param				cols	 the number of columns in this layout
	 * @exception IllegalArgumentException	if the value of both
	 *               <code>rows</code> and <code>cols</code> is set to zero
	 */
	public void setColumns(int cols) {
		if ((cols == 0) && (this.rows == 0)) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.cols = cols;
	}

	/**
	 * Gets the horizontal gap between shapes.
	 * @return the horizontal gap between shapes
	 */
	public double getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between shapes to the specified value.
	 * @param				hgap	 the horizontal gap between shapes
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
	 * Sets the vertical gap between shapes to the specified value.
	 * @param				 vgap	the vertical gap between shapes
	 * @since JDK1.1
	 */
	public void setVgap(double vgap) {
		this.vgap = vgap;
	}

	/**
	 * Adds the specified shape with the specified name to the layout.
	 * @param name the name of the shape
	 * @param shape the shape to be added
	 */
	public void addLayoutShape(String name, Shape shape) {
	}

	/**
	 * Removes the specified shape from the layout.
	 * @param shape the shape to be removed
	 */
	public void removeLayoutShape(Shape shape) {
	}

	/**
	 * Determines the preferred size of the container argument using
	 * this grid layout.
	 * <p>
	 * The preferred width of a grid layout is the largest preferred
	 * width of any of the shapes in the container times the number of
	 * columns, plus the horizontal padding times the number of columns
	 * plus one, plus the left and right insets of the target container.
	 * <p>
	 * The preferred height of a grid layout is the largest preferred
	 * height of any of the shapes in the container times the number of
	 * rows, plus the vertical padding times the number of rows plus one,
	 * plus the top and bottom insets of the target container.
	 *
	 * @param		 parent	 the container in which to do the layout
	 * @return the preferred dimensions to lay out the
	 *            subshapes of the specified container
	 */
	public Extents2D preferredLayoutSize(ContainerShape parent) {
		Insets2D insets = parent.getInsets();
		int nshapes = parent.getShapeCount();
		int nrows = rows;
		int ncols = cols;

		if (nrows > 0) {
			ncols = (nshapes + nrows - 1) / nrows;
		}
		else {
			nrows = (nshapes + ncols - 1) / ncols;
		}
		double w = 0d;
		double h = 0d;
		for (int i = 0; i < nshapes; i++) {
			Shape shape = parent.getShape(i);
			Extents2D e = shape.getPreferredSize();
			if (w < e.width) {
				w = e.width;
			}
			if (h < e.height) {
				h = e.height;
			}
		}
		return new Extents2D(insets.left + insets.right + ncols * w + (ncols - 1) * hgap,
				insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
	}

	/**
	 * Determines the minimum size of the container argument using this
	 * grid layout.
	 * <p>
	 * The minimum width of a grid layout is the largest minimum width
	 * of any of the shapes in the container times the number of columns,
	 * plus the horizontal padding times the number of columns plus one,
	 * plus the left and right insets of the target container.
	 * <p>
	 * The minimum height of a grid layout is the largest minimum height
	 * of any of the shapes in the container times the number of rows,
	 * plus the vertical padding times the number of rows plus one, plus
	 * the top and bottom insets of the target container.
	 *
	 * @param			 parent	 the container in which to do the layout
	 * @return the minimum dimensions needed to lay out the
	 *                      subshapes of the specified container
	 */
	public Extents2D minimumLayoutSize(ContainerShape parent) {
		Insets2D insets = parent.getInsets();
		int nshapes = parent.getShapeCount();
		int nrows = rows;
		int ncols = cols;

		if (nrows > 0) {
			ncols = (nshapes + nrows - 1) / nrows;
		}
		else {
			nrows = (nshapes + ncols - 1) / ncols;
		}
		double w = 0d;
		double h = 0d;
		for (int i = 0; i < nshapes; i++) {
			Shape shape = parent.getShape(i);
			Extents2D d = shape.getMinimumSize();
			if (w < d.width) {
				w = d.width;
			}
			if (h < d.height) {
				h = d.height;
			}
		}
		return new Extents2D(insets.left + insets.right + ncols * w + (ncols - 1) * hgap,
				insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
	}

	/**
	 * Lays out the specified container using this layout.
	 * <p>
	 * This method reshapes the shapes in the specified target
	 * container in order to satisfy the constraints of the
	 * <code>GridLayout</code> object.
	 * <p>
	 * The grid layout manager determines the size of individual
	 * shapes by dividing the free space in the container into
	 * equal-sized portions according to the number of rows and columns
	 * in the layout. The container's free space equals the container's
	 * size minus any insets and any specified horizontal or vertical
	 * gap. All shapes in a grid layout are given the same size.
	 *
	 * @param parent the container in which to do the layout
	 */
	public void layoutContainer(ContainerShape parent) {
		Insets2D insets = parent.getInsets();
		int nshapes = parent.getShapeCount();
		int nrows = rows;
		int ncols = cols;
		boolean ltr = parent.getComponentOrientation().isLeftToRight();

		if (nshapes == 0) {
			return;
		}
		if (nrows > 0) {
			ncols = (nshapes + nrows - 1) / nrows;
		}
		else {
			nrows = (nshapes + ncols - 1) / ncols;
		}
		double w = parent.getWidth() - (insets.left + insets.right);
		double h = parent.getHeight() - (insets.top + insets.bottom);
		w = (w - (ncols - 1) * hgap) / ncols;
		h = (h - (nrows - 1) * vgap) / nrows;

		if (ltr) {
			double x = insets.left;
			for (int c = 0; c < ncols; c++, x += w + hgap) {
				double y = insets.top;
				for (int r = 0; r < nrows; r++, y += h + vgap) {
					int i = r * ncols + c;
					if (i < nshapes) {
						parent.getShape(i).setDimension(new Rectangle2D.Double(x, y, w, h));
					}
				}
			}
		}
		else {
			double x = parent.getWidth() - insets.right - w;
			for (int c = 0; c < ncols; c++, x -= w + hgap) {
				double y = insets.top;
				for (int r = 0; r < nrows; r++, y += h + vgap) {
					int i = r * ncols + c;
					if (i < nshapes) {
						parent.getShape(i).setDimension(new Rectangle2D.Double(x, y, w, h));
					}
				}
			}
		}
	}

	/**
	 * Returns the string representation of this grid layout's values.
	 * @return a string representation of this grid layout
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap +
				",rows=" + rows + ",cols=" + cols + "]";
	}
}

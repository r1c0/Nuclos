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
 * Defines an interface for classes that know how to layout Containers
 * based on a layout constraints object.
 *
 * This interface extends the LayoutManager interface to deal with layouts
 * explicitly in terms of constraint objects that specify how and where
 * shapes should be added to the layout.
 * <p>
 * This minimal extension to LayoutManager is intended for tool
 * providers who wish to the creation of constraint-based layouts.
 * It does not yet provide full, general support for custom
 * constraint-based layout managers.
 * @see ILayout
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public interface ILayout2 extends ILayout {
	/**
	 * Adds the specified shape to the layout, using the specified
	 * constraint object.
	 * @param shape the shape to be added
	 * @param constraints	where/how the shape is added to the layout.
	 */
	void addLayoutShape(Shape shape, Object constraints);

	/**
	 * Calculates the maximum size dimensions for the specified container,
	 * given the shapes it contains.
	 */
	public Extents2D maximumLayoutSize(ContainerShape target);

	/**
	 * Returns the alignment along the x axis.  This specifies how
	 * the shape would like to be aligned relative to other
	 * shapes.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public double getLayoutAlignmentX(ContainerShape target);

	/**
	 * Returns the alignment along the y axis.  This specifies how
	 * the shape would like to be aligned relative to other
	 * shapes.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public double getLayoutAlignmentY(ContainerShape target);

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 */
	public void invalidateLayout(ContainerShape target);
}

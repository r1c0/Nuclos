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
 * Shape layout manager.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public interface ILayout {
	/**
	 * If the layout manager uses a per-component string,
	 * adds the component <code>comp</code> to the layout,
	 * associating it
	 * with the string specified by <code>name</code>.
	 *
	 * @param name the string to be associated with the component
	 * @param shape the shape to be added
	 */
	void addLayoutShape(String name, Shape shape);

	/**
	 * Removes the specified component from the layout.
	 * @param shape the shape to be removed
	 */
	void removeLayoutShape(Shape shape);

	/**
	 * Calculates the preferred size dimensions for the specified
	 * container, given the components it contains.
	 * @param parent the container to be laid out
	 *
	 * @see #minimumLayoutSize
	 */
	Extents2D preferredLayoutSize(ContainerShape parent);

	/**
	 * Calculates the minimum size dimensions for the specified
	 * container, given the components it contains.
	 * @param parent the component to be laid out
	 * @see #preferredLayoutSize
	 */
	Extents2D minimumLayoutSize(ContainerShape parent);

	/**
	 * Lays out the specified container.
	 * @param parent the container to be laid out
	 */
	void layoutContainer(ContainerShape parent);
}

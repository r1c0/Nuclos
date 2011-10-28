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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

/**
 * The AWT's BorderLayout doesn't treat the minimum size of its component correctly.
 * It uses the components' minimum sizes for calculating the container's minimum size
 * but ignores them when laying out the components - which leads to a stupid layout.
 * This is a simple workaround doesn't allow the minimum size of the container to be smaller than its preferred size.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class CommonBorderLayout extends BorderLayout {

	public CommonBorderLayout() {
		super();
	}

	public CommonBorderLayout(int hgap, int vgap) {
		super(hgap, vgap);
	}

	/**
	 * @param target
	 * @return this.preferredLayoutSize(target)
	 */
	@Override
	public Dimension minimumLayoutSize(Container target) {
		return this.preferredLayoutSize(target);
	}

}	// class CommonBorderLayout

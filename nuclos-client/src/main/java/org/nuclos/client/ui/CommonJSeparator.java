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

import java.awt.Dimension;

import javax.swing.JSeparator;

/**
 * A <code>JSeparator</code> with reasonable minimum height.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonJSeparator extends JSeparator {
	public CommonJSeparator() {
	}

	public CommonJSeparator(int iOrientation) {
		super(iOrientation);
	}

	/**
	 * @return new Dimension(minimumWidth, preferredHeight)
	 */
	@Override
	public Dimension getMinimumSize() {
		Dimension result = new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
		return result;
	}

}  // class CommonJSeparator

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
package org.nuclos.client.statemodel.shapes;

import java.awt.Color;

import org.nuclos.client.gef.shapes.OvalShape;

/**
 * Shape for displaying the starting state in the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateModelStartShape extends OvalShape {
	public StateModelStartShape() {
		super(0d, 0d, 12d, 12d);
		init();
	}

	public StateModelStartShape(double centerX, double centerY, double horzRadius, double vertRadius) {
		super(centerX, centerY, horzRadius, vertRadius);
		init();
	}

	protected void init() {
		setConnectable(true);
		paintBorder = true;
		borderColor = Color.BLACK;
		borderSize = 2d;
		bgColor = Color.WHITE;
		bSelectable = false;

		// @todo add method for enabling/disabling connection points
		for (int i = 0; i < CONNECTION_COUNT; connectionsEnabled[i++] = false) {
			;
		}
		connectionsEnabled[CONNECTION_CENTER] = true;
	}
}

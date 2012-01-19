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
package org.nuclos.client.main.mainframe;

import java.awt.Color;

public enum LinkMarker {
	NONE, ORANGE_FULL, ORANGE_HALF, BLUE_FULL, BLUE_HALF, GRAY;

	public boolean isMarked() {
		return this != NONE;
	}

	public Color getColor() {
		switch (this) {
		case ORANGE_FULL : return new Color(210, 123, 59, 255);
		case ORANGE_HALF : return new Color(210, 123, 59, 120);
		case BLUE_FULL : return new Color(172, 217, 232, 255);
		case BLUE_HALF : return new Color(172, 217, 232, 120);
		case GRAY: return new Color(112, 120, 132, 120);
		default : return new Color(0,0,0,0);
		}
	}
}	
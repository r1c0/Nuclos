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
import java.awt.Font;

import org.nuclos.client.gef.shapes.TextShape;

/**
 * Shape for displaying a note in the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class NoteShape extends TextShape {

	public NoteShape() {
		super("", 0d, 0d, 160d, 80d);
		setBorderColor(Color.BLACK);
		setBorderSize(1d);
		setColor(new Color(255, 255, 80));
		setResizeable(true);
		setMinimumSize(80d, 40d);
		setTextFont(new Font("Arial", Font.TRUETYPE_FONT, 9));
	}

	@Override
	public void beforeDelete() {
		super.beforeDelete();
	}
}

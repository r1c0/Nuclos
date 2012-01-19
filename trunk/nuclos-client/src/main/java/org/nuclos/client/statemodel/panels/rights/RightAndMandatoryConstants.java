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
package org.nuclos.client.statemodel.panels.rights;

import java.awt.Color;

import javax.swing.Icon;

import org.nuclos.client.ui.Icons;
import org.nuclos.common.ApplicationProperties;

public interface RightAndMandatoryConstants {
	
	public final static int GAP_ROWHEADER = 10;
	
	public final static int GAP_LINEBREAK = 6;
	
	public final static int CELL_WIDTH = 18;
	
	public final static int CELL_HEIGHT = 18;
	
	public final static int COLUMN_HEADER_HEIGHT_MAX = 240;
	
	public final static Color COLOR_BACKGROUND = Color.WHITE;
	
	public final static Color COLOR_GRID = new Color(216, 220, 228);
	
	public final static Color COLOR_SELECTION_BACKGROUND = new Color(135, 146, 165);
	
	public final static Color COLOR_MARKER_BACKGROUND = new Color(242, 243, 246);
	
	public final static Color COLOR_MARKER_GRID = new Color(128, 128, 128);
	
	public final static Icon ICON_NO_RIGHT = Icons.getInstance().getIconJobUnknown();
	
	public final static Icon ICON_READ = Icons.getInstance().getIconJobWarning();
	
	public final static Icon ICON_WRITE = Icons.getInstance().getIconJobSuccessful();
	
	public final static boolean DEV_MODUS = ApplicationProperties.getInstance().isFunctionBlockDev();
}

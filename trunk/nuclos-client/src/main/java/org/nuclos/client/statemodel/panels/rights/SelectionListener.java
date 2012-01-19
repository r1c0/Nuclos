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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class SelectionListener implements ActionListener {
	
	public static final String COMMAND_SELECT = "select";
	public static final String COMMAND_DESELECT = "deselect";

	@Override
	public void actionPerformed(ActionEvent e) {
		if (COMMAND_SELECT.equals(e.getActionCommand()))
			select(e.getID());
		else if (COMMAND_DESELECT.equals(e.getActionCommand()))
			deselect();
		else
			throw new IllegalArgumentException("Unknown action command: " + e.getActionCommand());
	}
	
	public abstract void select(Integer role);
	
	public abstract void deselect();

}

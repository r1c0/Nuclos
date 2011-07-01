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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicTextFieldUI;

import org.nuclos.client.synthetica.NuclosSyntheticaConstants;

public class StatusBarTextField extends JTextField {
	
	final int fade = 4;

	public StatusBarTextField(String text) {
		super(text);
		setName("tfStatusBar");
		setOpaque(false);
		setEditable(false);
		setBorder(BorderFactory.createEmptyBorder(fade,3,0,0));
		setForeground(Color.WHITE);
		setSelectionColor(NuclosSyntheticaConstants.BACKGROUND_SELECTION);
		setUI(new BasicTextFieldUI());
		setMinimumSize(new Dimension(0, getPreferredSize().height));
	}
	
}

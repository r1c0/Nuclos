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
package org.nuclos.client.genericobject.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to save/overwrite name and description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rotislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class SaveNameDescriptionPanel extends JPanel {

	private final Box pnlMain = Box.createVerticalBox();
	private final ButtonGroup bg = new ButtonGroup();

	final JRadioButton rbOverwrite = new JRadioButton();
	final JRadioButton rbNew = new JRadioButton();

	public SaveNameDescriptionPanel() {
		super(new BorderLayout());

		this.add(pnlMain, BorderLayout.CENTER);

		pnlMain.add(rbOverwrite);
		bg.add(rbOverwrite);
		pnlMain.add(rbNew);
		bg.add(rbNew);
	}

	public JRadioButton getRadioButtonNew() {
		return rbNew;
	}

	public JRadioButton getRadioButtonOverwrite() {
		return rbOverwrite;
	}

} 

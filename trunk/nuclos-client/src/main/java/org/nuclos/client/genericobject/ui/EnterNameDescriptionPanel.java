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

import org.nuclos.common2.CommonLocaleDelegate;

import java.awt.*;

/**
 * Panel to enter the name and description (of a new or existing filter/template etc.).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rotislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class EnterNameDescriptionPanel extends JPanel {

	private final JTextField tfName = new JTextField(40);
	private final JTextField tfDescription = new JTextField(40);

	public EnterNameDescriptionPanel() {
		super(new GridBagLayout());
		this.init();
	}

	private void init() {
		this.add(new JLabel(CommonLocaleDelegate.getMessage("EnterNameDescriptionPanel.1", "Name")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		    GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		this.add(tfName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		    GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		this.add(new JLabel(CommonLocaleDelegate.getMessage("EnterNameDescriptionPanel.2", "Beschreibung")), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		    GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		this.add(tfDescription, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
	}

	public JTextField getTextFieldDescription() {
		return tfDescription;
	}

	public JTextField getTextFieldName() {
		return tfName;
	}

} 

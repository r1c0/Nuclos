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
package org.nuclos.client.statemodel.admin;

import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.common2.SpringLocaleDelegate;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * Header for the edit panels.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class StateModelHeaderPanel extends JPanel {

	private final JPanel pnlTextFields = new JPanel();

	private final CollectableTextField clcttfName = new CollectableTextField(
			CollectableStateModel.clcte.getEntityField("name"));

	private final CollectableTextField clcttfDescription = new CollectableTextField(
			CollectableStateModel.clcte.getEntityField("description"));

	public StateModelHeaderPanel() {
		super(new BorderLayout());

		this.add(pnlTextFields, BorderLayout.CENTER);
		pnlTextFields.setLayout(new GridBagLayout());
		pnlTextFields.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.right = 10;
		clcttfName.setLabelText(SpringLocaleDelegate.getInstance().getMessage(
				"StateModelHeaderPanel.3","Name"));
		clcttfName.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"StateModelHeaderPanel.4","Name des Statusmodells"));
		clcttfName.setColumns(20);
		pnlTextFields.add(this.clcttfName.getJComponent(), gbc);

		clcttfDescription.setLabelText(SpringLocaleDelegate.getInstance().getMessage(
				"StateModelHeaderPanel.1","Beschreibung"));
		clcttfDescription.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"StateModelHeaderPanel.2","Beschreibung des Statusmodells"));
		gbc.insets.right = 5;
		pnlTextFields.add(this.clcttfDescription.getJLabel(), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets.right = 0;
		this.clcttfDescription.setColumns(25);
		pnlTextFields.add(this.clcttfDescription.getJTextField(), gbc);
	}

	public CollectableComponentsProvider newCollectableComponentsProvider() {
		return new DefaultCollectableComponentsProvider(clcttfName, clcttfDescription);
	}
	
	CollectableTextField getDescriptionField() {
		return clcttfDescription;
	}
	
	CollectableTextField getNameField() {
		return clcttfName;
	}

}	// class StateModelHeaderPanel

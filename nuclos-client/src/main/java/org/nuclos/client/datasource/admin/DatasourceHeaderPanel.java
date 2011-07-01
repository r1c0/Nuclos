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
package org.nuclos.client.datasource.admin;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.valuelistprovider.EntityCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Header for the edit panels.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class DatasourceHeaderPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel pnlTextFields = new JPanel();

	final CollectableTextField clcttfName;
	final CollectableComboBox clbxEntity;
	final CollectableTextField clcttfDescription;

	public DatasourceHeaderPanel(
			CollectableEntityField clctefName,
			CollectableEntityField clctefEntity,
			CollectableEntityField clctefDescription) {
		super(new BorderLayout());

		clcttfName = new CollectableTextField(clctefName);
		if (clctefEntity != null) {
			clbxEntity = new CollectableComboBox(clctefEntity);
			clbxEntity.setValueListProvider(new EntityCollectableFieldsProvider());
			clbxEntity.getValueListProvider().setParameter("menupath", "optional");
	        clbxEntity.refreshValueList(false);
		} else
			clbxEntity = null;
		clcttfDescription = new CollectableTextField(clctefDescription);

		this.add(pnlTextFields, BorderLayout.CENTER);
		pnlTextFields.setLayout(new GridBagLayout());
		pnlTextFields.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx = 0;
		gbc.gridy = 0;
		clcttfName.setLabelText(CommonLocaleDelegate.getMessage("DatasourceHeaderPanel.3","Name"));
		clcttfName.setToolTipText(CommonLocaleDelegate.getMessage("DatasourceHeaderPanel.4","Name des Reports/Formulars"));
		clcttfName.setColumns(50);
		pnlTextFields.add(this.clcttfName.getJLabel(), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		pnlTextFields.add(this.clcttfName.getJTextField(), gbc);

		if (clctefEntity != null) {
			gbc.gridx = 2;
			gbc.insets = new Insets(2, 20, 2, 2);
			clbxEntity.setLabelText(CommonLocaleDelegate.getMessage("nuclos.entityfield.recordgrant.entity.label","Entity"));
			clbxEntity.setToolTipText(CommonLocaleDelegate.getMessage("nuclos.entityfield.recordgrant.entity.description","Entity"));
			pnlTextFields.add(this.clbxEntity.getJLabel(), gbc);
			gbc.gridx = 3;
			gbc.insets = new Insets(2, 2, 2, 2);
			pnlTextFields.add(this.clbxEntity.getJComboBox(), gbc);
		}

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		clcttfDescription.setLabelText(CommonLocaleDelegate.getMessage("DatasourceHeaderPanel.1","Beschreibung"));
		clcttfDescription.setToolTipText(CommonLocaleDelegate.getMessage("DatasourceHeaderPanel.2","Beschreibung des Reports/Formulars"));
		pnlTextFields.add(this.clcttfDescription.getJLabel(), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pnlTextFields.add(this.clcttfDescription.getJTextField(), gbc);
	}

	public CollectableComponentsProvider newCollectableComponentsProvider() {
		if (clbxEntity != null)
			return new DefaultCollectableComponentsProvider(clcttfName, clbxEntity, clcttfDescription);
		else
			return new DefaultCollectableComponentsProvider(clcttfName, clcttfDescription);
	}

}	// class StateModelHeaderPanel

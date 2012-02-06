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
package org.nuclos.client.rule.admin;

import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.component.CollectableCheckBox;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.common2.CommonLocaleDelegate;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * Header panel for rule editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RuleHeaderPanel extends JPanel {

	public final CollectableTextField clcttfName = new CollectableTextField(
			CollectableRule.clcte.getEntityField(CollectableRule.FIELDNAME_NAME));
	public final CollectableTextField clcttfDescription = new CollectableTextField(
			CollectableRule.clcte.getEntityField(CollectableRule.FIELDNAME_DESCRIPTION));
	public final CollectableCheckBox clctchkbxActive = new CollectableCheckBox(
			CollectableRule.clcte.getEntityField(CollectableRule.FIELDNAME_ACTIVE), false);
	public final CollectableCheckBox clctchkbxDebug = new CollectableCheckBox(
			CollectableRule.clcte.getEntityField(CollectableRule.FIELDNAME_DEBUG), false);

	public RuleHeaderPanel() {
		super(new GridBagLayout());
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.bottom = 5;
		gbc.insets.right = 5;
		clcttfName.setLabelText(cld.getMessage("RuleHeaderPanel.4","Name"));
		clcttfName.setToolTipText(cld.getMessage("RuleHeaderPanel.5","Name der Regel"));
		gbc.weightx = 0.0;
		this.add(this.clcttfName.getJLabel(), gbc);

		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER; //end row
		gbc.insets.right = 0;
		this.add(this.clcttfName.getJTextField(), gbc);

		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.insets.right = 5;
		clcttfDescription.setLabelText(cld.getMessage("RuleHeaderPanel.2","Beschreibung"));
		clcttfDescription.setToolTipText(cld.getMessage("RuleHeaderPanel.3","Beschreibung der Regel"));
		this.add(this.clcttfDescription.getJLabel(), gbc);

		gbc.weightx = 1.0;
		gbc.insets.right = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER; //end row
		this.add(this.clcttfDescription.getJTextField(), gbc);

		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		clctchkbxActive.setLabelText(cld.getMessage("RuleHeaderPanel.1","Aktiv?"));
		clctchkbxActive.setToolTipText(cld.getMessage("RuleHeaderPanel.6","Steuert, ob die Regel ausgef\u00fchrt wird"));
		this.add(this.clctchkbxActive.getJCheckBox(), gbc);
		
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		clctchkbxDebug.setLabelText(cld.getMessage("RuleHeaderPanel.7","Debug?"));
		clctchkbxDebug.setToolTipText(cld.getMessage("RuleHeaderPanel.8","Steuert, ob Debugausgaben ausgegeben werden"));
		this.add(this.clctchkbxDebug.getJCheckBox(), gbc);
	}

	public CollectableComponentsProvider newCollectableComponentsProvider() {
		return new DefaultCollectableComponentsProvider(clcttfName, clcttfDescription, clctchkbxActive, clctchkbxDebug);
	}

}  // class RuleHeaderPanel

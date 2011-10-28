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
package org.nuclos.client.ldap;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.nuclos.common2.CommonLocaleDelegate;

public class TryAuthenticationPanel extends JPanel {

	private final JTextField tfUsername	= new JTextField();
	private final JPasswordField pfPassword	= new JPasswordField();

	private Insets insets = new Insets(2, 2, 2, 2);

	public TryAuthenticationPanel() {
		super(new GridBagLayout());

		JLabel lblUsername = new JLabel(CommonLocaleDelegate.getMessage("TryAuthenticationPanel.login.label", "Login"));
		String ttLogin = CommonLocaleDelegate.getMessage("TryAuthenticationPanel.login.description", "Loginname for authentication. This name will be inserted into the user filter by replacing the value '''{'0}''.");
		lblUsername.setToolTipText(ttLogin);
		add(lblUsername, new GridBagConstraints(0, 0, 1, 1, 0.3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		tfUsername.setToolTipText(ttLogin);
		add(tfUsername, new GridBagConstraints(1, 0, 1, 1, 0.7, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		JLabel lblPassword = new JLabel(CommonLocaleDelegate.getMessage("TryAuthenticationPanel.password.label", "Passwort"));
		String ttPassword = CommonLocaleDelegate.getMessage("TryAuthenticationPanel.password.description", "Das Passwort für die Authentisierung.");
		lblPassword.setToolTipText(ttPassword);
		add(lblPassword, new GridBagConstraints(0, 1, 1, 1, 0.3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		pfPassword.setToolTipText(ttPassword);
		add(pfPassword, new GridBagConstraints(1, 1, 1, 1, 0.1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0));
	}

	public String getUsername() {
		return tfUsername.getText();
	}

	public String getPassword() {
		return new String(pfPassword.getPassword());
	}
}

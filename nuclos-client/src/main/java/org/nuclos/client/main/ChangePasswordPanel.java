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
package org.nuclos.client.main;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common.NuclosBusinessException;

class ChangePasswordPanel extends JPanel {
	private final JPasswordField pfOld = new JPasswordField();
	private final JPasswordField pfNew1 = new JPasswordField();
	private final JPasswordField pfNew2 = new JPasswordField();
	
	ChangePasswordPanel() {
	  super(new GridLayout(2,1));
	  	JPanel passwordPanel = new JPanel(new GridLayout(3,2));
	  	JPanel warningPanel = new JPanel(new GridLayout(1,1));
		passwordPanel.add(new JLabel(CommonLocaleDelegate.getMessage("ChangePasswordPanel.1", "Altes Passwort")));
	  	passwordPanel.add(pfOld);
	  	passwordPanel.add(new JLabel(CommonLocaleDelegate.getMessage("ChangePasswordPanel.2", "Neues Passwort")));
	  	passwordPanel.add(pfNew1);
	  	passwordPanel.add(new JLabel(CommonLocaleDelegate.getMessage("ChangePasswordPanel.3", "Neues Passwort (wiederholen)")));
	  	passwordPanel.add(pfNew2);
	  	warningPanel.add(new JLabel(CommonLocaleDelegate.getMessage("ChangePasswordPanel.4", "Der Client wird nach einer Passwort\u00e4nderung neu gestartet")));
	  	this.add(passwordPanel);
	  	this.add(warningPanel);
	}	
	
	String getNewPassword() throws NuclosBusinessException {
		final String new1 = new String(pfNew1.getPassword());
		final String new2 = new String(pfNew2.getPassword());
		if(new1.equals(new2)) {
		  return new1 ;
		} else {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("ChangePasswordPanel.5", "Die beiden neuen Passw\u00f6rter sind nicht identisch."));
		}	  
	}
	
	String getOldPassword() {
		return pfOld.getPassword().length > 0 ? new String(pfOld.getPassword()) : ""; 
	}
} 


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
/*
 * Created on 20.08.2010
 */
package org.nuclos.client.main;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.nuclos.client.livesearch.LiveSearchSettingsPanel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.PreferencesException;

public class NuclosSettingsContainer extends JPanel {

	private NuclosSettingsPanel      settingsPanel;
	private LiveSearchSettingsPanel  livesearchPanel;

	public NuclosSettingsContainer(MainFrame frm) {
		super(new GridLayout(1, 1));
		JTabbedPane  tabPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabPane);

		livesearchPanel = new LiveSearchSettingsPanel();
		tabPane.addTab(
			CommonLocaleDelegate.getResource("nuclos.settings.container.tab2", "Live-Suche"),
			livesearchPanel);

		settingsPanel = new NuclosSettingsPanel(frm);
		tabPane.addTab(
			CommonLocaleDelegate.getResource("nuclos.settings.container.tab1", "Ansichtsoptionen"),
			settingsPanel);

		validate();
	}


	public void save() throws PreferencesException {
		settingsPanel.save();
		livesearchPanel.save();
	}
}

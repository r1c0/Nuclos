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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.nuclos.api.UserPreferences;
import org.nuclos.api.ui.UserPreferencesEditor;
import org.nuclos.client.livesearch.LiveSearchSettingsPanel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class NuclosSettingsContainer extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(NuclosSettingsContainer.class);
	
	private final MainFrame frm;

	private NucletComponentRepository ncr;
	private PreferencesFacadeRemote preferencesFacade;
	
	private NuclosSettingsPanel      settingsPanel;
	private LiveSearchSettingsPanel  livesearchPanel;
	
	private List<UserPreferencesEditor<? extends UserPreferences>> nucletUserPreferencesEditors;
	private Collection<UserPreferences> nucletUserPreferences;

	public NuclosSettingsContainer(MainFrame frm) {
		super(new GridLayout(1, 1));
		this.frm = frm;
	}

	@Autowired
	void setNucletComponentRepository(NucletComponentRepository ncr) {
		this.ncr = ncr;
	}
	
	@Autowired
	void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacade) {
		this.preferencesFacade = preferencesFacade;
	}
	
	@PostConstruct
	void init() {
		JTabbedPane  tabPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabPane);

		livesearchPanel = new LiveSearchSettingsPanel();
		tabPane.addTab(
			SpringLocaleDelegate.getInstance().getResource("nuclos.settings.container.tab2", "Live-Suche"),
			livesearchPanel);

		settingsPanel = new NuclosSettingsPanel(frm);
		tabPane.addTab(
			SpringLocaleDelegate.getInstance().getResource("nuclos.settings.container.tab1", "Ansichtsoptionen"),
			settingsPanel);
		
		nucletUserPreferencesEditors = ncr.getUserPreferencesEditors();
		nucletUserPreferences = preferencesFacade.getApiUserPreferences();
		
		for (UserPreferencesEditor<? extends UserPreferences> upe : CollectionUtils.sorted(nucletUserPreferencesEditors, new Comparator<UserPreferencesEditor<? extends UserPreferences>>() {
			@Override
			public int compare(UserPreferencesEditor<? extends UserPreferences> o1, UserPreferencesEditor<? extends UserPreferences> o2) {
				return StringUtils.compareIgnoreCase(o1.getName(), o2.getName());
			}
		})) {
			UserPreferences userPreferences = null;
			for (UserPreferences up : nucletUserPreferences) {
				if (upe.getPreferencesClass().equals(up.getClass())) {
					userPreferences = up;
					break;
				}
			}
			try {
				final JComponent comp = (JComponent) upe.getClass().getMethod("getPreferencesComponent", upe.getPreferencesClass()).invoke(upe, userPreferences);
				tabPane.addTab(
						upe.getName(), 
						upe.getIcon(), 
						comp);
			} catch (Exception ex) {
				LOG.error(String.format("UserPreferencesComponent (EditorClass=%s) not created! Error=%s", upe.getClass(), ex.getMessage()), ex);
			}
		}

		validate();
	}

	public void save() throws PreferencesException {
		boolean saved = false;
		try {
			settingsPanel.save();
			livesearchPanel.save();
			
			Collection<UserPreferences> newPreferences = new ArrayList<UserPreferences>();
			for (UserPreferencesEditor<? extends UserPreferences> upe : nucletUserPreferencesEditors) {
				newPreferences.add(upe.getPreferences());
			}
			preferencesFacade.setApiUserPreferences(newPreferences);
			saved = true;
		} catch (Exception ex) {
			throw new PreferencesException(ex);
		} finally {
			for (UserPreferencesEditor<? extends UserPreferences> upe : nucletUserPreferencesEditors) {
				try {
					upe.close(saved);
				} catch (Exception e1) {
					LOG.error(String.format("Exception during close of %s: %s", upe.getClass().getName(), e1.getMessage()), e1);
				}
			}
		}
	}
}

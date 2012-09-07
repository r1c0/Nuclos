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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.nuclos.api.Settings;
import org.nuclos.api.ui.UserSettingsEditor;
import org.nuclos.client.livesearch.LiveSearchSettingsPanel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class NuclosSettingsContainer extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(NuclosSettingsContainer.class);
	
	private final MainFrame frm;

	// former Spring injection
	
	private NucletComponentRepository ncr;
	private PreferencesFacadeRemote preferencesFacade;
	
	// end of former Spring injection
	
	private NuclosSettingsPanel      settingsPanel;
	private LiveSearchSettingsPanel  livesearchPanel;
	
	private List<UserSettingsEditor> apiUserSettingsEditors;
	private Map<String, Settings> apiUserSettings;

	public NuclosSettingsContainer(MainFrame frm) {
		super(new GridLayout(1, 1));
		this.frm = frm;
		
		setNucletComponentRepository(SpringApplicationContextHolder.getBean(NucletComponentRepository.class));
		setPreferencesFacadeRemote(SpringApplicationContextHolder.getBean(PreferencesFacadeRemote.class));
		init();
	}

	final void setNucletComponentRepository(NucletComponentRepository ncr) {
		this.ncr = ncr;
	}
	
	final NucletComponentRepository getNucletComponentRepository() {
		return ncr;
	}
	
	final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacade) {
		this.preferencesFacade = preferencesFacade;
	}
	
	final PreferencesFacadeRemote getPreferencesFacadeRemote() {
		return preferencesFacade;
	}
	
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
		
		apiUserSettingsEditors = getNucletComponentRepository().getUserSettingsEditors();
		apiUserSettings = getPreferencesFacadeRemote().getApiUserSettings();
		
		for (UserSettingsEditor use : CollectionUtils.sorted(apiUserSettingsEditors, new Comparator<UserSettingsEditor>() {
			@Override
			public int compare(UserSettingsEditor o1, UserSettingsEditor o2) {
				return StringUtils.compareIgnoreCase(o1.getName(), o2.getName());
			}
		})) {
			Settings userSettings = null;
			for (String key : apiUserSettings.keySet()) {
				if (key.equals(use.getSettingsKey())) {
					userSettings = apiUserSettings.get(key);
					break;
				}
			}
			
			try {
				final JComponent comp = use.getSettingsComponent(userSettings);
				comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				tabPane.addTab(
						use.getName(), 
						use.getIcon(), 
						comp);
			} catch (Exception ex) {
				apiUserSettingsEditors.remove(use);
				Errors.getInstance().showExceptionDialog(this, ex);
			} 
		}

		validate();
	}

	public void save() throws PreferencesException {
		boolean saved = false;
		try {
			settingsPanel.save();
			livesearchPanel.save();
			
			Map<String, Settings> newSettings = new HashMap<String, Settings>();
			for (UserSettingsEditor use : apiUserSettingsEditors) {
				newSettings.put(use.getSettingsKey(), use.getSettings());
			}
			getPreferencesFacadeRemote().setApiUserSettings(newSettings);
			saved = true;
		} catch (Exception ex) {
			throw new PreferencesException(ex);
		} finally {
			for (UserSettingsEditor upe : apiUserSettingsEditors) {
				try {
					upe.close(saved);
				} catch (Exception e1) {
					LOG.error(String.format("Exception during close of %s: %s", upe.getClass().getName(), e1.getMessage()), e1);
				}
			}
		}
	}
}

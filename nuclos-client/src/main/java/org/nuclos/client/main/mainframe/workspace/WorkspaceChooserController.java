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
package org.nuclos.client.main.mainframe.workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class WorkspaceChooserController {
	
	private static final int ICON_SIZE = 12;
	
	private final Map<String, Integer> workspaces = new HashMap<String, Integer>();
	private final JComboBox workspaceChooser = new JComboBox();
	
	private final JPanel contentFrame = new JPanel(new BorderLayout());
	private boolean ignoreEvents = true;
	private String selectedWorkspace = null;
	
	private final JMenuItem miNew = new JMenuItem(new AbstractAction(
		CommonLocaleDelegate.getMessage("WorkspaceChooserController.1","Create new workspace") + "...", 
		MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNew16(), ICON_SIZE)) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			String newName = JOptionPane.showInputDialog(Main.getMainFrame(), 
				CommonLocaleDelegate.getMessage("WorkspaceChooserController.3","Name des neuen Arbeitsbereiches") + ":", 
				CommonLocaleDelegate.getMessage("WorkspaceChooserController.2","Name"), 
				JOptionPane.INFORMATION_MESSAGE);
			
			if (StringUtils.looksEmpty(newName)) {
				setSelectedWorkspace(selectedWorkspace);
			} else {
				RestoreUtils.storeWorkspace(selectedWorkspace);
				RestoreUtils.clearAndRestoreToDefaultWorkspace(newName);
				addAndSelectNewWorkspace(newName);
			}
		}
	});
	private final JMenuItem miSaveAs = new JMenuItem(new AbstractAction(
		CommonLocaleDelegate.getMessage("WorkspaceChooserController.4","Arbeitsbereich sichern unter") + "...", 
		MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconSave16(), ICON_SIZE)) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			String newName = JOptionPane.showInputDialog(Main.getMainFrame(), 
				CommonLocaleDelegate.getMessage("WorkspaceChooserController.6","Aktuellen Arbeitsbereich \"{0}\" sichern unter",getSelectedWorkspace()) + ":", 
				CommonLocaleDelegate.getMessage("WorkspaceChooserController.5","Neuer Name"), 
				JOptionPane.INFORMATION_MESSAGE);
			
			if (StringUtils.looksEmpty(newName)) {
				setSelectedWorkspace(selectedWorkspace);
			} else {
				RestoreUtils.storeWorkspace(newName);
				addAndSelectNewWorkspace(newName);
			}
		}
	});
	private final JMenuItem miRemove = new JMenuItem(new AbstractAction(
		CommonLocaleDelegate.getMessage("WorkspaceChooserController.7","Aktuellen Arbeitsbereich löschen") + "...", 
		MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRealDelete16(), ICON_SIZE)) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.YES_OPTION == 
				JOptionPane.showConfirmDialog(Main.getMainFrame(), 
					CommonLocaleDelegate.getMessage("WorkspaceChooserController.8","Möchten Sie wirklich den aktuellen Arbeitsbereich \"{0}\" löschen?",getSelectedWorkspace()), 
					CommonLocaleDelegate.getMessage("WorkspaceChooserController.9","Sind Sie sicher?"), 
					JOptionPane.YES_NO_OPTION)) {
				
				ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).removeWorkspace(selectedWorkspace);
				workspaces.remove(selectedWorkspace);
				refreshItems(workspaces.keySet());
				RestoreUtils.clearAndRestoreWorkspace(MainFrame.getDefaultWorkspace());
			} else {
				setSelectedWorkspace(selectedWorkspace);
			}
		}
	});

	public WorkspaceChooserController() {
		super();
		initWorkspaceChooser();
	}
	
	private void initWorkspaceChooser() {
		contentFrame.setOpaque(false);
		contentFrame.add(workspaceChooser, BorderLayout.CENTER);
		contentFrame.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 5));
		
		workspaceChooser.setMaximumRowCount(20);
		workspaceChooser.setFocusable(false);
		workspaceChooser.setBorder(BorderFactory.createLineBorder(NuclosSyntheticaConstants.BACKGROUND_SPOT	, 1));
		workspaceChooser.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JPanel result = new JPanel(new BorderLayout());
				JComponent toAdd = null;
				
				if (value instanceof JSeparator) {
					toAdd = (JSeparator) value;
				
				} else if (value instanceof JMenuItem) { 
					toAdd = (JMenuItem) value;
					toAdd.setForeground(Color.WHITE);
				} 
				
				result.setOpaque(true);
				if (isSelected) {
					result.setBackground(NuclosSyntheticaConstants.BACKGROUND_SPOT);
				} else {
					result.setBackground(NuclosSyntheticaConstants.BACKGROUND_DARKER);
				}
				
				if (toAdd != null)
					result.add(toAdd, BorderLayout.CENTER);
				return result;
			}
		});
		contentFrame.setPreferredSize(new Dimension(210, 18));
		contentFrame.setMaximumSize(new Dimension(210, 18));
		
		workspaceChooser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ignoreEvents)
					return;
				
				if (workspaceChooser.getSelectedItem() instanceof JMenuItem) {
					((JMenuItem) workspaceChooser.getSelectedItem()).getAction().actionPerformed(new ActionEvent(workspaceChooser, 0, "workspaceChooser"));
				}
			}
		});
		
		ignoreEvents = false;
	}
	
	public void setupItems() {
		refreshItems(ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).getWorkspaceNames());
	}
	
	private void refreshItems(Collection<String> _userWorkspaces) {
		ignoreEvents = true;
		
		Collection<String> userWorkspaces = new ArrayList<String>(_userWorkspaces);
		if (!userWorkspaces.contains(MainFrame.getDefaultWorkspace())) {
			userWorkspaces.add(MainFrame.getDefaultWorkspace());
		}
		
		workspaces.clear();
		workspaceChooser.removeAllItems();
		
		int i = 0;
		for (final String workspace : CollectionUtils.sorted(userWorkspaces, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}})) {
			
			workspaces.put(workspace, i); i++;
			workspaceChooser.addItem(new JMenuItem(new AbstractAction(workspace, MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconTabGeneric(), ICON_SIZE)) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					RestoreUtils.storeWorkspace(getSelectedWorkspace());
					RestoreUtils.clearAndRestoreWorkspace(workspace);
				}
			}));
		}
		workspaceChooser.addItem(new JSeparator());
		workspaceChooser.addItem(miNew);
		workspaceChooser.addItem(miSaveAs);
		workspaceChooser.addItem(miRemove);
		
		ignoreEvents = false;
	}
	
	private void addAndSelectNewWorkspace(String name) {
		ignoreEvents = true;
		Set<String> workspaceNames = new HashSet<String>(workspaces.keySet());
		workspaceNames.add(name);
		
		refreshItems(workspaceNames);
		setSelectedWorkspace(name);
		ignoreEvents = false;
	}
	
	public String getSelectedWorkspace() {
		return selectedWorkspace;
	}
	
	public void setSelectedWorkspace(String name) {
		ignoreEvents = true;
		if (!workspaces.containsKey(name)) {
			ignoreEvents = false;
			return;
		}
		
		selectedWorkspace = name;
		workspaceChooser.setSelectedIndex(workspaces.get(name));
		ignoreEvents = false;
	}

	public JComponent getChooserComponent() {
		return contentFrame;
	}
	
	public void setEnabled(boolean b) {
		workspaceChooser.setEnabled(b);
	}
	
	public boolean isEnabled() {
		return workspaceChooser.isEnabled();
	}
}

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

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;

public class WorkspaceEditor  {

	private final JDialog dialog;
	private final JPanel contentPanel;

	private final JTextField tfName;
	private final ResourceIconChooser nuclosIconChooser;
	private final JCheckBox chckHideName;
	private final JCheckBox chckHide;
	private final JCheckBox chckHideMenuBar;
	private final JCheckBox chckAlwaysOpenAtLogin;
	private final JCheckBox chckUseLastFrameSettings;
	private final JCheckBox chckAlwaysReset;
	private final JButton btSave;
	private final JButton btCancel;

	private boolean saved;
	private final WorkspaceVO wovo;
	private final WorkspaceVO backup;

	public WorkspaceEditor(WorkspaceVO wovo) {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		this.wovo = wovo;
		this.backup = new WorkspaceVO();
		this.backup.importHeader(wovo.getWoDesc());
		
		boolean showAlwaysReset = wovo.isAssigned() && SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN);

		contentPanel = new JPanel();
		initJPanel(contentPanel,
				new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 
					TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
				new double[] {20,
							  20,
							  20,
							  20,
							  showAlwaysReset? 20 : 0,
							  10,
							  20,
							  TableLayout.FILL,
							  TableLayout.PREFERRED});
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel lbName = new JLabel(cld.getMessage("WorkspaceEditor.2","Name"), JLabel.TRAILING);
        contentPanel.add(lbName, "0, 0");
        tfName = new JTextField(15);
        lbName.setLabelFor(tfName);
        contentPanel.add(tfName, "1, 0");
        chckHideName = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.3","Name ausblenden"));
        contentPanel.add(chckHideName, "2, 0, 3, 0");
        chckHide = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.8","Auswahl Button ausblenden"));
        if (wovo.isAssigned() && SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
        	contentPanel.add(chckHide, "4, 0");
        }
        chckAlwaysOpenAtLogin = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.11","Immer bei Anmeldung öffnen"));
        contentPanel.add(chckAlwaysOpenAtLogin, "1, 1");

        JLabel lbMainFrame = new JLabel(cld.getMessage(
        		"WorkspaceEditor.9","Hauptfenster"), JLabel.TRAILING);
        contentPanel.add(lbMainFrame, "0, 2");
        chckHideMenuBar = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.10","Nur Standard Menuleiste"));
        contentPanel.add(chckHideMenuBar, "1, 2");
        chckUseLastFrameSettings = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.12","Letzte Fenster Einstellungen übernehmen (Größe und Position)"));
        contentPanel.add(chckUseLastFrameSettings, "1, 3, 5, 3");
        chckAlwaysReset = new JCheckBox(cld.getMessage(
        		"WorkspaceEditor.alwaysreset","Zuletzt geöffnete Tabs immer zurücksetzen"));
        if (showAlwaysReset) {
        	contentPanel.add(chckAlwaysReset, "1, 4, 5, 4");
        }

        JLabel lbIcon = new JLabel(cld.getMessage("WorkspaceEditor.4","Icon"), JLabel.TRAILING);
        contentPanel.add(lbIcon, "0, 6");
        nuclosIconChooser = new ResourceIconChooser(WorkspaceChooserController.ICON_SIZE);
        contentPanel.add(nuclosIconChooser, "1, 6, 5, 7");

		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		btSave = new JButton(cld.getMessage("WorkspaceEditor.5","Speichern"));
		btCancel = new JButton(cld.getMessage("WorkspaceEditor.6","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, "0, 8, 5, 8");

		tfName.setText(wovo.getWoDesc().getName());
		chckHide.setSelected(wovo.getWoDesc().isHide());
		chckHideName.setSelected(wovo.getWoDesc().isHideName());
		chckHideMenuBar.setSelected(wovo.getWoDesc().isHideMenuBar());
		chckAlwaysOpenAtLogin.setSelected(wovo.getWoDesc().isAlwaysOpenAtLogin());
		chckUseLastFrameSettings.setSelected(wovo.getWoDesc().isUseLastFrameSettings());
		chckAlwaysReset.setSelected(wovo.getWoDesc().isAlwaysReset());
		nuclosIconChooser.setSelected(wovo.getWoDesc().getNuclosResource());

		dialog = new JDialog(Main.getInstance().getMainFrame(), cld.getMessage(
				"WorkspaceEditor.1","Arbeitsumgebung Eigenschaften"), true);
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getInstance().getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-300, mfBounds.y+(mfBounds.height/2)-200, 600, 400);
		dialog.setResizable(false);

		initListener();
		dialog.setVisible(true);
	}

	private void initListener() {
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String name = tfName.getText().trim();
				final boolean hide = chckHide.isSelected();
				final boolean hideName = chckHideName.isSelected();
				final boolean hideMenuBar = chckHideMenuBar.isSelected();
				final boolean alwaysOpenAtLogin = chckAlwaysOpenAtLogin.isSelected();
				final boolean useLastFrameSettings = chckUseLastFrameSettings.isSelected();
				final boolean alwaysReset = chckAlwaysReset.isSelected();
				final String nuclosResource = nuclosIconChooser.getSelectedResourceIconName();

				if (StringUtils.looksEmpty(name)) {
					JOptionPane.showMessageDialog(contentPanel, CommonLocaleDelegate.getInstance().getMessage(
							"WorkspaceEditor.7","Bitte geben Sie einen Namen an"));
				} else {
					wovo.setName(name);
					wovo.getWoDesc().setHide(hide);
					wovo.getWoDesc().setHideName(hideName);
					wovo.getWoDesc().setHideMenuBar(hideMenuBar);
					wovo.getWoDesc().setAlwaysOpenAtLogin(alwaysOpenAtLogin);
					wovo.getWoDesc().setUseLastFrameSettings(useLastFrameSettings);
					wovo.getWoDesc().setAlwaysReset(alwaysReset);
					wovo.getWoDesc().setNuclosResource(nuclosResource);

					saved = true;
					dialog.dispose();
				}
			}
		});

		btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
	}

	public boolean isSaved() {
		return saved;
	}

	public void revertChanges() {
		this.wovo.importHeader(this.backup.getWoDesc());
	}

	protected void initJPanel(JPanel panel, double[] cols, double[] rows) {
		final double size [][] = {cols, rows};
		final TableLayout layout = new TableLayout(size);

		layout.setVGap(3);
		layout.setHGap(5);

		panel.setLayout(layout);
	}
}

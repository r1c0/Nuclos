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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.ExplorerSettings;
import org.nuclos.client.explorer.ExplorerSettings.FolderNodeAction;
import org.nuclos.client.explorer.ExplorerSettings.ObjectNodeAction;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.settings.KeyEnumComboBoxModel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.message.MessageExchange;
import org.nuclos.client.ui.message.MessageExchange.MessageExchangeListener;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.PreferencesException;

public class NuclosSettingsPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(NuclosSettingsPanel.class);

	private static double settingslayout[][] = {
        { TableLayout.FILL }, // Columns
        { TableLayout.FILL, TableLayout.FILL } };// Rows

	private static double explorerSettingslayout[][] = {
        { TableLayout.FILL, TableLayout.FILL }, // Columns
        { 20.0, 20.0, TableLayout.FILL } };// Rows

	private ExplorerSettings explorerSettings = ExplorerSettings.getInstance();

	private KeyEnumComboBoxModel<ObjectNodeAction> modelObjectNodeAction;
	private KeyEnumComboBoxModel<FolderNodeAction> modelFolderNodeAction;

	public NuclosSettingsPanel(final MainFrame frm) {
		//setBackground(Color.WHITE);
		setBorder(new TitledBorder(""));

		TableLayout layout = new TableLayout(settingslayout);
		layout.setVGap(5);
        layout.setHGap(5);
		setLayout(layout);

		JPanel explorerSettingsPanel = new JPanel();
		explorerSettingsPanel.setBorder(new TitledBorder(CommonLocaleDelegate.getText("settings.explorer.title")));

		layout = new TableLayout(explorerSettingslayout);
		layout.setVGap(5);
        layout.setHGap(5);
        explorerSettingsPanel.setLayout(layout);

		JLabel lbDoubleClickObjectNode = new JLabel(CommonLocaleDelegate.getText("settings.explorer.actions.default.objectnode"));
		explorerSettingsPanel.add(lbDoubleClickObjectNode, "0,0");

		modelObjectNodeAction = new KeyEnumComboBoxModel<ObjectNodeAction>(ObjectNodeAction.values());
		modelObjectNodeAction.setSelectedValue(explorerSettings.getObjectNodeAction());
		JComboBox cbDoubleClickObjectNode = new JComboBox(modelObjectNodeAction);
		explorerSettingsPanel.add(cbDoubleClickObjectNode, "1,0");

		JLabel lbDoubleClickFolderNode = new JLabel(CommonLocaleDelegate.getText("settings.explorer.actions.default.foldernode"));
		explorerSettingsPanel.add(lbDoubleClickFolderNode, "0,1");

		modelFolderNodeAction = new KeyEnumComboBoxModel<FolderNodeAction>(FolderNodeAction.values());
		modelFolderNodeAction.setSelectedValue(explorerSettings.getFolderNodeAction());
		JComboBox cbDoubleClickFolderNode = new JComboBox(modelFolderNodeAction);
		explorerSettingsPanel.add(cbDoubleClickFolderNode, "1,1");

		add(explorerSettingsPanel, "0,0");

		// clear search history
		Box clearHistBox = Box.createVerticalBox();
		clearHistBox.setAlignmentX(CENTER_ALIGNMENT);
		clearHistBox.setBorder(new TitledBorder(CommonLocaleDelegate.getMessage("R00022913", "Client-Daten")));
		JButton btClearHistory = new JButton();
		btClearHistory.setAction(new AbstractAction(CommonLocaleDelegate.getMessage("R00022898", "Suchhistorie l\u00f6schen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(frm,
					CommonLocaleDelegate.getMessage("R00022910", "Suchhistorie wirklich l\u00f6schen?"), "",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					UIUtils.runCommand(NuclosSettingsPanel.this, new Runnable() {

						@Override
						public void run() {
							try {
								Preferences entityPrefs = ClientPreferences.getUserPreferences().node("collect").node("entity");
								for(String entityName : entityPrefs.childrenNames()) {
									Preferences fields = entityPrefs.node(entityName).node("fields");
									for (String fieldName : fields.childrenNames()) {
										Preferences values = fields.node(fieldName);
										values.removeNode();
										values.flush();
										MessageExchange.send(new Pair<String, String>(entityName, fieldName),
											MessageExchangeListener.ObjectType.TEXTFIELD,
											MessageExchangeListener.MessageType.REFRESH);
									}
								}
								JOptionPane.showMessageDialog(frm, CommonLocaleDelegate.getMessage("R00022901", "Die Suchhistorie wurde gel\u00f6scht."));
							}
							catch(/* BackingStore */ Exception e1) {
								/*dann halt nicht*/
								LOG.error("actionPerformed failed: " + e1, e1);
							}
						}
					});
				}
			}
		});
		clearHistBox.add(btClearHistory);
		clearHistBox.add(Box.createGlue());
		add(clearHistBox, "0,1");

		setPreferredSize(new Dimension(400, 450));
		validate();
	}

	public void save() throws PreferencesException {
		explorerSettings.setObjectNodeAction(modelObjectNodeAction.getSelectedValue());
		explorerSettings.setFolderNodeAction(modelFolderNodeAction.getSelectedValue());
		explorerSettings.save();
	}
}

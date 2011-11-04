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

import org.nuclos.client.main.Main;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;

public class WorkspaceEditor  {
	
	private final JDialog dialog;
	private final JPanel contentPanel;
	
	private final JTextField tfName; 
	private final ResourceIconChooser nuclosIconChooser;
	private final JCheckBox chckHideName;
	private final JButton btSave;
	private final JButton btCancel;
	
	private boolean saved;
	private final WorkspaceVO wovo;
	private final WorkspaceVO backup;
	
	public WorkspaceEditor(WorkspaceVO wovo) {
		this.wovo = wovo;
		this.backup = new WorkspaceVO();
		this.backup.importHeader(wovo.getWoDesc());
		
		contentPanel = new JPanel();
		initJPanel(contentPanel,
				new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
				new double[] {20,
							  20,
							  TableLayout.FILL,
							  TableLayout.PREFERRED});
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel lbName = new JLabel(CommonLocaleDelegate.getMessage("WorkspaceEditor.2","Name"), JLabel.TRAILING);
        contentPanel.add(lbName, "0, 0");
        tfName = new JTextField(15);
        lbName.setLabelFor(tfName);
        contentPanel.add(tfName, "1, 0");
        chckHideName = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspaceEditor.3","Name ausblenden"));
        contentPanel.add(chckHideName, "2, 0, 3, 0");
        
        JLabel lbIcon = new JLabel(CommonLocaleDelegate.getMessage("WorkspaceEditor.4","Icon"), JLabel.TRAILING);
        contentPanel.add(lbIcon, "0, 1");
        nuclosIconChooser = new ResourceIconChooser(WorkspaceChooserController.ICON_SIZE);
        contentPanel.add(nuclosIconChooser, "1, 1, 4, 2");
		
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		btSave = new JButton(CommonLocaleDelegate.getMessage("WorkspaceEditor.5","Speichern"));
		btCancel = new JButton(CommonLocaleDelegate.getMessage("WorkspaceEditor.6","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, "0, 3, 4, 3");
		
		tfName.setText(wovo.getWoDesc().getName());
		chckHideName.setSelected(wovo.getWoDesc().isHideName());
		nuclosIconChooser.setSelected(wovo.getWoDesc().getNuclosResource());
		
		dialog = new JDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("WorkspaceEditor.1","Arbeitsumgebung bearbeiten"), true);
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getMainFrame().getBounds();
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
				final boolean hideName = chckHideName.isSelected();
				final String nuclosResource = nuclosIconChooser.getSelectedResourceIconName();
				
				if (StringUtils.looksEmpty(name)) {
					JOptionPane.showMessageDialog(contentPanel, CommonLocaleDelegate.getMessage("WorkspaceEditor.7","Bitte geben Sie einen Namen an"));
				} else {
					wovo.setName(name);
					wovo.getWoDesc().setHideName(hideName);
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

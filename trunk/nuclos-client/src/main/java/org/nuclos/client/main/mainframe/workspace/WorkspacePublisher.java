package org.nuclos.client.main.mainframe.workspace;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.nuclos.client.main.Main;
import org.nuclos.common2.CommonLocaleDelegate;

public class WorkspacePublisher {
	
	private final JDialog dialog;
	private final JPanel contentPanel;

	private final JCheckBox chbxStructureChange;
	private final JCheckBox chbxStructureUpdate;
	private final JCheckBox chbxStarttabConfiguration;
	private final JCheckBox chbxTableColumnConfiguration;
	private final JCheckBox chbxToolbarConfiguration;
	private final JButton btSave;
	private final JButton btCancel;
	
	private boolean isPublishStructureChange;
	private boolean isPublishStructureUpdate;
	private boolean isPublishStarttabConfiguration;
	private boolean isPublishTableColumnConfiguration;
	private boolean isPublishToolbarConfiguration;
	
	private boolean saved;
	
	public WorkspacePublisher(boolean forceStructureChange) {
		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		List<Double> rows = new ArrayList<Double>();
		if (forceStructureChange) rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.PREFERRED);
		rows.add(TableLayout.FILL);
		rows.add(TableLayout.PREFERRED);
		
		double[] rowArray = new double[rows.size()];
		for (int i = 0; i < rows.size(); i++)
			rowArray[i] = rows.get(i);
			
		initJPanel(contentPanel,
				new double[] {20, TableLayout.PREFERRED},
							  rowArray);
		
		chbxStructureChange = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspacePublisher.6","Struktur des Hauptfensters (Fensteraufteilung)"));
		chbxStructureUpdate = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspacePublisher.4","Name, Icon, Position der Trenner und des Fensters"));
		chbxStarttabConfiguration = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspacePublisher.7","Starttab Konfigurationen"));
		chbxTableColumnConfiguration = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspacePublisher.8","Spalteneinstellungen aller Entitäten und Unterformulare"));
		chbxToolbarConfiguration = new JCheckBox(CommonLocaleDelegate.getMessage("WorkspacePublisher.9","Symbolleisten aller Entitäten"));
		
		// DEV ---
		chbxToolbarConfiguration.setEnabled(false);
		// DEV ---
		
		int y = 0;
		if (forceStructureChange) {
			contentPanel.add(new JLabel(CommonLocaleDelegate.getMessage("WorkspacePublisher.5","Aufgrund der geänderten Fensteraufteilung können nur alle Änderungen publiziert werden.")), "0, "+y+", 1, "+y++);
			chbxStructureChange.setSelected(true);
			chbxStructureChange.setEnabled(false);
			chbxStructureUpdate.setSelected(true);
			chbxStructureUpdate.setEnabled(false);
			chbxStarttabConfiguration.setSelected(true);
			chbxStarttabConfiguration.setEnabled(false);
		}
		contentPanel.add(chbxStructureChange, "0, "+y+", 1, "+y++);
		contentPanel.add(chbxStructureUpdate, "1, "+y++);
		contentPanel.add(chbxStarttabConfiguration, "1, "+y++);
		contentPanel.add(new JSeparator(), "0, "+y+", 1, "+y++);
		contentPanel.add(chbxTableColumnConfiguration, "0, "+y+", 1, "+y++);
		contentPanel.add(chbxToolbarConfiguration, "0, "+y+", 1, "+y++);
		
		y++;
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		btSave = new JButton(CommonLocaleDelegate.getMessage("WorkspacePublisher.2","Speichern"));
		btCancel = new JButton(CommonLocaleDelegate.getMessage("WorkspacePublisher.3","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, "0, "+y+", 1, "+y++);
		
		dialog = new JDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("WorkspacePublisher.1","Arbeitsumgebung bearbeiten"), true);
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getMainFrame().getBounds();
		Dimension prefSize = contentPanel.getPreferredSize();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-(prefSize.width/2), mfBounds.y+(mfBounds.height/2)-(prefSize.height/2), prefSize.width, prefSize.height+25);
		dialog.setResizable(false);
		
		initListener();
		dialog.setVisible(true);
	}
	
	private void initListener() {
		chbxStructureChange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chbxStructureChange.isSelected()) {
					chbxStructureUpdate.setSelected(true);
					chbxStructureUpdate.setEnabled(false);
					chbxStarttabConfiguration.setSelected(true);
					chbxStarttabConfiguration.setEnabled(false);
				} else {
					chbxStructureUpdate.setEnabled(true);
					chbxStarttabConfiguration.setEnabled(true);
				}
			}
		});
		
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isPublishStructureChange = chbxStructureChange.isSelected();
				isPublishStructureUpdate = chbxStructureUpdate.isSelected();
				isPublishStarttabConfiguration = chbxStarttabConfiguration.isSelected();
				isPublishTableColumnConfiguration = chbxTableColumnConfiguration.isSelected();
				isPublishToolbarConfiguration = chbxToolbarConfiguration.isSelected();
				
				saved = true;
				dialog.dispose();
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
	
	public boolean isPublishStructureChange() {
		return isPublishStructureChange;
	}

	public boolean isPublishStructureUpdate() {
		return isPublishStructureUpdate;
	}

	public boolean isPublishStarttabConfiguration() {
		return isPublishStarttabConfiguration;
	}
	
	public boolean isPublishTableColumnConfiguration() {
		return isPublishTableColumnConfiguration;
	}
	
	public boolean isPublishToolbarConfiguration() {
		return isPublishToolbarConfiguration;
	}

	protected static void initJPanel(JPanel panel, double[] cols, double[] rows) {	
		final double size [][] = {cols, rows};
		final TableLayout layout = new TableLayout(size);
		
		layout.setVGap(3);
		layout.setHGap(5);
		
		panel.setLayout(layout);
	}
}

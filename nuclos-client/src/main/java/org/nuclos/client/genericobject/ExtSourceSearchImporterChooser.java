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
package org.nuclos.client.genericobject;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.common2.CommonLocaleDelegate;

public class ExtSourceSearchImporterChooser {

	private Integer iModuleId = null;

	private JComponent parentComponent = null;

	private ExtSourceSearchImporter importer = null;
	
	public ExtSourceSearchImporterChooser(Integer iModuleId, JComponent parentComponent) {
		this.parentComponent = parentComponent;
		this.iModuleId = iModuleId;

	}

	public ExtSourceSearchImporter chooseImporter(){
		showChooseImporterDialog();
		return this.importer;
	}
	
	
	private int showDialog(JComponent contents) {
		return JOptionPane.showOptionDialog(parentComponent, contents,
				CommonLocaleDelegate.getMessage("ExtSourceSearchImporterChooser.1", "Importquelle: Auswahl"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.DEFAULT_OPTION, null, null, contents);
	}

	/**
	 *
	 * Procedural method - shows dialog - sets necessary parameters
	 *
	 */
	private void showChooseImporterDialog() {

		JComboBox importSourceCombo = new JComboBox();
		JLabel importSourceLabel = new JLabel(CommonLocaleDelegate.getMessage("ExtSourceSearchImporterChooser.2", "Importquelle"));
		JLabel[] labels = new JLabel[] { importSourceLabel };
		JComboBox[] combos = new JComboBox[] { importSourceCombo };

		ExtSourceSearchImporter[] importSources= new ExtSourceSearchImporter[] {
				new XLSSearchImporter(iModuleId, parentComponent), 
				new DataSourceSearchImporter(iModuleId, parentComponent)};
		//Arrays.sort(importSources,Collator.getInstance());
		for (int i=0; i<importSources.length;i++) {
			importSourceCombo.addItem(importSources[i]);
		}

		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel importSourcePanel = new JPanel(gbLayout);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridwidth = 1;
		for (int i = 0; i < labels.length; i++) {
			labels[i].setPreferredSize(new Dimension(100, 25));
			combos[i].setPreferredSize(new Dimension(200, 25));
			constraints.gridx = 0;
			constraints.gridy = i + 1;
			importSourcePanel.add(labels[i], constraints);
			constraints.gridx = 1;
			importSourcePanel.add(combos[i], constraints);
		}

		int decision = showDialog(importSourcePanel);

		// show dialog until user chooses valid selection or cancels this
		// operation
		while (decision != JOptionPane.CANCEL_OPTION) {
			//check selected column
			if (importSourceCombo.getSelectedItem() == null) {
				JOptionPane.showMessageDialog(parentComponent,
						CommonLocaleDelegate.getMessage("ExtSourceSearchImporterChooser.3", "Bitte Importquelle w\u00e4hlen"));
				decision = showDialog(importSourcePanel);

			} else {
				this.importer = (ExtSourceSearchImporter)importSourceCombo.getSelectedItem();
				return;
			}
		}
	}	
}

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
package org.nuclos.client.searchfilter;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.nuclos.client.ui.table.CommonJTable;

/**
 * @deprecated Maybe this will be used sometime in the future.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

@Deprecated
public class ManageSearchFiltersPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel pnlMain = new JPanel(new BorderLayout());
	private final JScrollPane scrlpn = new JScrollPane();
	final JTable tbl = new CommonJTable();
	private final JPanel pnlButtons = new JPanel(new GridBagLayout());
	private final JPanel pnlButtons1 = new JPanel(new GridLayout(0, 1, 10, 5));
	final JButton btnAdd = new JButton();
	final JButton btnEdit = new JButton();
	final JButton btnRemove = new JButton();

	public ManageSearchFiltersPanel() {
		super(new BorderLayout());
		init();
	}

	private void init() {
		this.add(pnlMain, BorderLayout.CENTER);

		pnlMain.add(scrlpn, BorderLayout.CENTER);
		pnlMain.add(pnlButtons, BorderLayout.EAST);

		scrlpn.getViewport().add(tbl);

//		pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pnlButtons.add(pnlButtons1);
		pnlButtons1.add(btnAdd);
		pnlButtons1.add(btnEdit);
		pnlButtons1.add(btnRemove);

		btnAdd.setText("Hinzuf\u00fcgen...");
		btnEdit.setText("Bearbeiten...");
		btnEdit.setEnabled(false);
		btnRemove.setText("L\u00f6schen...");
		btnRemove.setEnabled(false);
	}

}	// class ManageSearchFiltersPanel

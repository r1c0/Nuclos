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
package org.nuclos.client.statemodel.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import org.nuclos.client.statemodel.models.StateRoleTableModel;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Shows the roles attached to a transition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TransitionRolesPanel extends JPanel {

	private final JToolBar toolbar = new JToolBar();
	private final JButton btnAdd = new JButton();
	private final JButton btnDelete = new JButton();
	private final JTable tblRoles = new JTable();
	private final JScrollPane scrlpn = new JScrollPane(tblRoles);
	private final StateRoleTableModel model = new StateRoleTableModel();

	public TransitionRolesPanel() {
		super(new BorderLayout());
		this.init();
	}

	private void init() {
		btnAdd.setIcon(Icons.getInstance().getIconNew16());
		btnAdd.setToolTipText(CommonLocaleDelegate.getMessage("TransitionRolesPanel.1","Neue Benutzergruppe zuordnen"));
		btnAdd.setActionCommand("add");
		btnDelete.setIcon(Icons.getInstance().getIconDelete16());
		btnDelete.setToolTipText(CommonLocaleDelegate.getMessage("TransitionRolesPanel.2","Zuordnung aufheben"));
		btnDelete.setActionCommand("remove");

		toolbar.setFloatable(false);
		toolbar.add(btnAdd, null);
		toolbar.add(btnDelete, null);
		this.add(toolbar, BorderLayout.NORTH);
		tblRoles.setBorder(BorderFactory.createLoweredBevelBorder());
		tblRoles.setModel(model);
		tblRoles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblRoles.setRowMargin(4);
		tblRoles.setRowHeight(18);
		tblRoles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblRoles.setIntercellSpacing(new Dimension(3, 3));
		this.add(scrlpn, BorderLayout.CENTER);
	}

	public JToolBar getToolBar() {
		return toolbar;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public JButton getBtnDelete() {
		return btnDelete;
	}

	public JTable getTblRoles() {
		return tblRoles;
	}

	public StateRoleTableModel getModel() {
		return model;
	}
}	// class TransitionRolesPanel

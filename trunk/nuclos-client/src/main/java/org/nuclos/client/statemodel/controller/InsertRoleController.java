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
package org.nuclos.client.statemodel.controller;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.nuclos.client.statemodel.models.SelectRoleTableModel;
import org.nuclos.client.statemodel.panels.InsertRolePanel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Controller for inserting roles into a state model.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class InsertRoleController {
	protected InsertRolePanel pnlRoles;
	protected Component parent;

	public InsertRoleController(Component parent) {
		this.parent = parent;
	}

	public boolean run(String sDialogTitle, java.util.List<MasterDataVO> excludeRoles) throws RemoteException {
		boolean result = false;
		pnlRoles = new InsertRolePanel();

		SelectRoleTableModel tableModel = new SelectRoleTableModel();
		tableModel.setExcludeRoles(excludeRoles);
		pnlRoles.setModel(tableModel);
		TableUtils.setPreferredColumnWidth(pnlRoles.getTblRoles(), 10, 10);

		final JOptionPane optionPane = new JOptionPane(pnlRoles, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optionPane.setInitialValue(null);
		pnlRoles.getTblRoles().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					optionPane.setValue(new Integer(JOptionPane.OK_OPTION));
				}
			}
		});
		JDialog dlg = optionPane.createDialog(parent, CommonLocaleDelegate.getMessage("InsertRoleController.1","Benutzergruppenauswahl"));
		dlg.setResizable(true);
		dlg.setVisible(true);
		if (optionPane.getValue() != null) {
			int btn = ((Integer) optionPane.getValue()).intValue();
			result = (btn == JOptionPane.OK_OPTION);
		}
		return result;
	}
}

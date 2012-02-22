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

import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.models.SelectRuleTableModel;
import org.nuclos.client.statemodel.panels.InsertRulePanel;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.table.TableUtils;

/**
 * Controller for adding a rule to a transition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class InsertRuleController extends Controller {

	private InsertRulePanel pnlRules;

	public InsertRuleController(Component parent) {
		super(parent);
	}

	public boolean run(String sDialogTitle, java.util.List<SortedRuleVO> excludeRules) throws RemoteException {
		this.pnlRules = new InsertRulePanel();

		final SelectRuleTableModel tblmodel = new SelectRuleTableModel();
		tblmodel.setExcludeRules(excludeRules);
		pnlRules.setModel(tblmodel);
		TableUtils.setPreferredColumnWidth(pnlRules.getTblRules(), 10, 10);

		final JOptionPane optionPane = new JOptionPane(pnlRules, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optionPane.setInitialValue(null);
		pnlRules.getTblRules().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					optionPane.setValue(JOptionPane.OK_OPTION);
				}
			}
		});
		final JDialog dlg = optionPane.createDialog(this.getParent(), 
				getSpringLocaleDelegate().getMessage("InsertRuleController.1","Regelauswahl"));
		dlg.setResizable(true);
		dlg.setVisible(true);
		boolean result = false;
		if (optionPane.getValue() != null) {
			final int iBtn = (Integer) optionPane.getValue();
			result = (iBtn == JOptionPane.OK_OPTION);
		}
		return result;
	}

	public InsertRulePanel getRulesPanel() {
		return pnlRules;
	}

}	// class InsertRuleController

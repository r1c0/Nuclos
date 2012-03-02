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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.UsageCriteria;

/**
 * Controller class for for choice panel (export of search list or of selected reports).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:dirk.funke@novabit.de">Dirk Funke</a>
 * @version 01.00.00
 */
public class ChoiceListOrReportExportController extends ReportFormatController {

	ChoiceListOrReportExportPanel pnlChoiceExport = new ChoiceListOrReportExportPanel(pnlFormat);
	private final UsageCriteria usagecriteria;
	private final int iObjectCount;

	class ChoiceActionListener implements ActionListener {
		boolean bFirstTime = true;

		@Override
		public void actionPerformed(ActionEvent event) {
			final String sSelectedFormat = event.getActionCommand();
			if (bFirstTime) {
				UIUtils.runCommand(parent, new Runnable() {
					@Override
					public void run() {
						try {
							pnlChoiceExport.prepareSelectionPanel(usagecriteria, iObjectCount, sSelectedFormat);
						}
						catch (Exception ex) {
							Errors.getInstance().showExceptionDialog(parent, ex.getCause());
						}
					}
				});

				bFirstTime = false;
			}
			final CardLayout cardlayoutSelection = (CardLayout) pnlChoiceExport.getPanel().getLayout();
			cardlayoutSelection.show(pnlChoiceExport.getPanel(), sSelectedFormat);
		}
	}

	public ChoiceListOrReportExportController(Component parent, UsageCriteria usagecriteria, int iObjectCount) {
		super(parent);

		this.iObjectCount = iObjectCount;
		this.usagecriteria = usagecriteria;
	}

	@Override
	public boolean run(String sDialogTitle) {
		final ActionListener actionChoiceListener = new ChoiceActionListener();
		pnlChoiceExport.getListButton().addActionListener(actionChoiceListener);
		pnlChoiceExport.getReportButton().addActionListener(actionChoiceListener);

//		final int btnValue = JOptionPane.showConfirmDialog(this.parent, pnlChoiceExport, sDialogTitle,
//				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        JOptionPane pane = new JOptionPane(pnlChoiceExport, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);
        JDialog dialog = pane.createDialog(this.parent, sDialogTitle);
        dialog.setPreferredSize(new Dimension(200, dialog.getHeight()));
        dialog.setResizable(true);
        dialog.setVisible(true);
        
		return ((pane.getValue() == null? -1: (Integer)pane.getValue()) == JOptionPane.OK_OPTION);
	}

}	// class ChoiceListOrReportExportController

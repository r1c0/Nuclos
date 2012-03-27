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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import org.nuclos.client.ui.LineLayout;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.report.NuclosReportException;

/**
 * Panel for choice, whether a search list or the reports of selection shall be exported; for the choosen
 * selection of reports one can select the output format.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:dirk.funke@novabit.de">Dirk Funke</a>
 * @version 01.00.00
 */
public class ChoiceListOrReportExportPanel extends JPanel {

	private Border border1;
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel lblHeadline = new JLabel();

	private ButtonGroup bgPrechoice = new ButtonGroup();
	private JRadioButton rbList = new JRadioButton();
	private JRadioButton rbReport = new JRadioButton();

	private JPanel pnlPrechoice = new JPanel();
	private JPanel pnlPrechoiceHelp = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JPanel pnlSelection = new JPanel(new CardLayout());
	private ReportSelectionPanel pnlReport = new ReportSelectionPanel();

	public ChoiceListOrReportExportPanel(ReportFormatPanel pnlList) {
		setLayout(new BorderLayout());

		pnlPrechoice.setLayout(new LineLayout(LineLayout.VERTICAL));
		lblHeadline.setToolTipText("");
		lblHeadline.setText(SpringLocaleDelegate.getInstance().getMessage(
				"ChoiceListOrReportExportPanel.1", "Bitte Aktion ausw\u00e4hlen:"));
		rbList.setActionCommand("List");
		rbReport.setActionCommand("Report");
		rbList.setText(SpringLocaleDelegate.getInstance().getMessage(
				"ChoiceListOrReportExportPanel.2", "Suchergebnisliste drucken"));
		rbReport.setText(SpringLocaleDelegate.getInstance().getMessage(
				"ChoiceListOrReportExportPanel.3", "Formulare f\u00fcr ausgew\u00e4hlte Objekte drucken"));
		rbList.setSelected(true);
		bgPrechoice.add(rbList);
		bgPrechoice.add(rbReport);
		pnlPrechoice.add(lblHeadline);
		pnlPrechoice.add(rbList);
		pnlPrechoice.add(rbReport);

		pnlPrechoiceHelp.add(pnlPrechoice);
		pnlPrechoice.add(new JPanel());
		pnlPrechoice.add(new JSeparator());
		pnlList.setBorder(BorderFactory.createEmptyBorder(0, -35, 0, 0));
		pnlSelection.add(pnlList, "List");

		add(pnlPrechoiceHelp, BorderLayout.NORTH);
		add(pnlList, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		result.width = Math.max(320, result.width);
		result.height = result.height + 25;

		return result;
	}

	public void prepareSelectionPanel(UsageCriteria usagecriteria, int iObjectCount, String sSelectedFormat)
			throws NuclosBusinessException {
		try {
			pnlSelection.remove(pnlReport);
			pnlReport = ReportController.prepareReportSelectionPanel(usagecriteria, iObjectCount);
			pnlSelection.add(pnlReport, "Report");
		}
		catch (RuntimeException ex) {
			throw new NuclosBusinessException(ex);
		}
		catch (NuclosReportException ex) {
			throw new NuclosBusinessException(ex);
		}
	}
	
	ReportSelectionPanel getSelectionPanel() {
		return pnlReport;
	}
	
	JPanel getPanel() {
		return pnlSelection;
	}
	
	JRadioButton getReportButton() {
		return rbReport;
	}
	
	JRadioButton getListButton() {
		return rbList;
	}
}

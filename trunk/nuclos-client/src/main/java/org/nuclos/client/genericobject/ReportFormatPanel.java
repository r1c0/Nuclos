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

import org.nuclos.common2.CommonLocaleDelegate;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

/**
 * Panel for selection of report output format.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class ReportFormatPanel extends JPanel {

	private Border border1;
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel lblHeadline = new JLabel();
	private ButtonGroup bgFormat = new ButtonGroup();
	private JRadioButton rbPdf = new JRadioButton();
	private JRadioButton rbXls = new JRadioButton();
	private JRadioButton rbCsv = new JRadioButton();

	public ReportFormatPanel() {
		rbXls.setActionCommand("XLS");
		rbPdf.setActionCommand("PDF");
		rbCsv.setActionCommand("CSV");
		border1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		lblHeadline.setToolTipText("");
		lblHeadline.setText(CommonLocaleDelegate.getMessage("ReportFormatPanel.2","Bitte w\u00e4hlen Sie das Zielformat aus:"));
		setBorder(border1);
		setLayout(gridBagLayout1);
		rbPdf.setSelected(true);
		rbPdf.setText(CommonLocaleDelegate.getMessage("ReportFormatPanel.1","Adobe(tm) Acrobat Reader (PDF)"));
		rbXls.setText(CommonLocaleDelegate.getMessage("ReportFormatPanel.4","Microsoft(tm) Excel Worksheet (XLS)"));
		rbCsv.setText(CommonLocaleDelegate.getMessage("ReportFormatPanel.3","CSV"));
		add(lblHeadline, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));
		add(rbPdf, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(rbXls, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(rbCsv, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bgFormat.add(rbPdf);
		bgFormat.add(rbXls);
		bgFormat.add(rbCsv);
	}
	
	ButtonGroup getFormatButtonGroup() {
		return bgFormat;
	}
	
}

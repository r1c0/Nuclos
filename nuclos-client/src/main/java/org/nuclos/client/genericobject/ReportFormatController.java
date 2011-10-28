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

import java.awt.Component;

import javax.swing.ButtonModel;
import javax.swing.JOptionPane;

import org.nuclos.server.report.valueobject.ReportOutputVO;

/**
 * Controller class for report formats.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 * deprecated This controller doesn't have very much functionality...
 */
public class ReportFormatController {

	Component parent;

	ReportFormatPanel pnlFormat = new ReportFormatPanel();

	public ReportFormatController(Component parent) {
		this.parent = parent;
	}

	public ReportOutputVO.Format getFormat() {
		/** @todo refactor using ReportFormat methods */

		ReportOutputVO.Format result = ReportOutputVO.Format.PDF;

		final ButtonModel bm = pnlFormat.getFormatButtonGroup().getSelection();
		if (bm != null) {
			final String sSelectedFormat = bm.getActionCommand();
			if (sSelectedFormat == "PDF") {
				result = ReportOutputVO.Format.PDF;
			}
			else if (sSelectedFormat == "XLS") {
				result = ReportOutputVO.Format.XLS;
			}
			else if (sSelectedFormat == "CSV") {
				result = ReportOutputVO.Format.CSV;
			}
		}

		return result;
	}

	public boolean run(String sDialogTitle) {
		final int btn = JOptionPane.showConfirmDialog(this.parent, pnlFormat, sDialogTitle, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		return (btn == JOptionPane.OK_OPTION);
	}

}	// class ReportFormatController

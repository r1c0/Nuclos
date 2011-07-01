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
package org.nuclos.client.report.reportrunner.export;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Exporter which creates Adobe PDF-files. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 * @todo don't extend AbstractReportExporter
 */
public class PDFExport extends AbstractReportExporter {

	/**
	 * @param sReportName
	 * @param printObj
	 * @param parameter
	 * @throws NuclosReportException
	 */
	public void export(String sReportName, JasperPrint printObj,
			String parameter, boolean bOpenFile) throws NuclosReportException {

		final String sDir = createExportDir(parameter);
		final String sFileName = getFileName(sDir, sReportName, ".pdf");

		try {
			JasperExportManager.exportReportToPdfFile(printObj, sFileName);

			openFile(sFileName, bOpenFile);
		}
		catch (JRException ex) {
			throw new NuclosReportException(ex);
		}
	}

	/**
	 * @param sReportName
	 * @param resultVO
	 * @param destination
	 * @param parameter
	 * @param bOpenFile
	 * @throws NuclosReportException
	 */
	@Override
	public void export(String sReportName, ResultVO resultVO,
			String destination, String parameter, boolean bOpenFile) throws NuclosReportException {
		/** @todo adjust design */
		throw new UnsupportedOperationException();
	}

}	// class PDFExport

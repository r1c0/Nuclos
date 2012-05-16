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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common.csvparser.ExcelCSVPrinter;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.print.CSVPrintJob;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Exporter which creates CSV-files. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class CSVExport extends AbstractReportExporter {

	private final char cDelimiter;
	private final int iQuoteLevel;
	private final char cQuote;
	private final boolean bWriteHeader;
	private final String sFileSuffix;
	private String fileName;

	public CSVExport() {
		this(';', 2, '\"', true, ".csv");
	}

	public CSVExport(char cDelimiter, int iQuoteLevel, char cQuote, boolean bWriteHeader, String sFileSuffix) {
		this.cDelimiter = cDelimiter;
		this.iQuoteLevel = iQuoteLevel;
		this.cQuote = cQuote;
		this.bWriteHeader = bWriteHeader;
		this.sFileSuffix = sFileSuffix;
	}

	@Override
	public void export(final String sReportName, final ResultVO resultVO,
			String sourceFile, final String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {

		final String sFileName = createFile(sReportName, resultVO, parameter);

		switch (destination) {
		case FILE:
			openFile(sFileName, false);
			break;
		case PRINTER_CLIENT:
			openPrintDialog(sFileName, true, false);
			break;
		case PRINTER_SERVER:
			openPrintDialog(sFileName, false, false);
			break;
		case DEFAULT_PRINTER_CLIENT:
			openPrintDialog(sFileName, true, true);
			break;
		case DEFAULT_PRINTER_SERVER:
			openPrintDialog(sFileName, false, true);
			break;
		default:
			// TYPE SCREEN
			openFile(sFileName, true);
			break;
		}
	}
	
	public String getFilename() {
		return this.fileName;
	}

	private String createFile(final String sReportName, final ResultVO resultVO, String parameter) throws NuclosReportException {
		String sFileName = null;

		try {
			sFileName = getFileName(createExportDir(parameter), sReportName, sFileSuffix);
			this.fileName = sFileName;

			final ExcelCSVPrinter excelCSVPrinter = new ExcelCSVPrinter(new BufferedWriter(new FileWriter(sFileName, false)), 
					iQuoteLevel, cDelimiter, cQuote, false);
			excelCSVPrinter.changeDelimiter(cDelimiter);

			if(bWriteHeader) {
				// Column headers first
				for (ResultColumnVO columnvo : resultVO.getColumns()) {
					excelCSVPrinter.write(columnvo.getColumnLabel());
				}
				excelCSVPrinter.writeln();
			}

			// export data
			for (int iRow = 0; iRow < resultVO.getRows().size(); iRow++) {
				for (int iColumn = 0; iColumn < resultVO.getColumns().size(); iColumn++) {
					final Object value = resultVO.getRows().get(iRow)[iColumn];
					final ResultColumnVO column = resultVO.getColumns().get(iColumn);
					excelCSVPrinter.write(column.format(value));
				}
				excelCSVPrinter.writeln();
			}
			excelCSVPrinter.close();

		}
		catch (IOException ex) {
			throw new NuclosReportException(getSpringLocaleDelegate().getMessage(
					"CSVExport.1", "Fehler beim Erzeugen der Datei: {0}", sFileName));
		}

		return sFileName;
	}

	@Override
	protected NuclosReportPrintJob getNuclosReportPrintJob() {
		return new CSVPrintJob();
	}
}	// class CSVExport

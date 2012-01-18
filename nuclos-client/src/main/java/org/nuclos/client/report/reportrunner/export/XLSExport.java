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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Date;
import java.util.Iterator;

import org.jawin.COMException;
import org.jawin.DispatchPtr;
import org.jawin.win32.Ole32;
import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.print.XLSPrintJob;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Exporter which creates MS-Excel files.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class XLSExport extends AbstractReportExporter {

	/** @todo convert to local variable */
	private boolean bUseSaveAs = true;

	/**
	 * Execute the report and display the results.
	 * In the case of an excel collective report excel is opened only once after the last report is done,
	 * and every sheet has been created and completed.
	 * @param sReportName
	 * @param resultVO
	 * @param sourceFile
	 * @param parameter
	 * @param bOpenFile
	 * @throws NuclosReportException
	 */
	@Override
	public void export(String sReportName, ResultVO resultVO, String sourceFile,
			String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {

		checkJawin();
		
		final String sFileName = createFile(resultVO, sReportName);

		if (reportVO == null || reportVO.getOutputType() != ReportVO.OutputType.EXCEL || reportOutputVO == null || reportOutputVO.isLastOfMany())
		{
			switch (destination) {
			case FILE:
				openFile(sFileName, true);
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
				openFile(sFileName, false);
				break;
			}			
		}
	}

	/**
	 * Is called once before a report is executed.
	 * The output file will be deleted, and the template is copied to the output directory.
	 * In the case of an excel collective report this is done only for the first report.
	 * @return The name of the created file
	 * @param reportvo
	 * @param outputvo
	 * @param sReportName
	 */
	private String prepareExport(ReportVO reportvo, ReportOutputVO outputvo, String sReportName) throws NuclosReportException {
		String sFileName = null;
		if (reportvo == null) {
			final String sDir = System.getProperty("java.io.tmpdir");
			sFileName = getFileName(sDir, sReportName, ".xls");
		}
		else if (reportvo.getOutputType() != ReportVO.OutputType.EXCEL || outputvo.isFirstOfMany()) {
			final String sDir = createExportDir(outputvo.getParameter());
			sFileName = getFileName(sDir, reportvo.getName(), ".xls");

			copyTemplateFile(sFileName, outputvo);

			/** @todo Don't alter the parameter here! */
			reportvo.setOutputFileName(sFileName);
		}
		else {
			sFileName = reportvo.getOutputFileName();
		}
		return sFileName;
	}

	/**
	 * create Excel file using the COM API
	 *
	 * @param resultVO
	 * @param sReportName
	 * @return the name of the generated file
	 */
	private String createFile(ResultVO resultVO, String sReportName) throws NuclosReportException {
		String sFileName = null;

		DispatchPtr application = null;
		DispatchPtr workbooks = null;
		DispatchPtr workbook = null;
		DispatchPtr worksheets = null;
		DispatchPtr worksheet = null;

		try {
			// prepare destination:
			sFileName = prepareExport(reportVO, reportOutputVO, sReportName);

			Ole32.CoInitialize();
			application = new DispatchPtr("Excel.Application");
			workbooks = application.getObject("Workbooks");

			if (new File(sFileName).exists()) {
				workbook = (DispatchPtr) workbooks.invoke("Open", sFileName);
				bUseSaveAs = false;
			}
			else {
				//create new file
				workbook = (DispatchPtr) workbooks.invoke("Add");
				bUseSaveAs = true;
			}

			worksheets = workbook.getObject("Worksheets");
			worksheet = null;
			final String sSheetName = getExcelSheetName();

			try {
				worksheet = workbook.getObject("Worksheets", sSheetName);
			}
			catch (Exception ex) {
				worksheet = null;
			}
			if (worksheet == null) {
				// add a new worksheet and rename it
				worksheet = (DispatchPtr) worksheets.invoke("Add");
				worksheet.put("Name", sSheetName);
			}

			writeDataClipboard(worksheet, resultVO);

		}
		catch (Throwable ex) {
			throw new NuclosReportException(getCommonLocaleDelegate().getMessage(
					"DOCExport.2", "Die Datei {0} konnte nicht erstellt werden", sFileName) + ".\n", ex);
		}
		finally {
			if (worksheet != null) {
				worksheet.close();
			}
			if (worksheets != null) {
				worksheets.close();
			}
			try {
				if (workbook != null) {
					// If there is a source file, it has been copied to the destination file name and path; if there is none,
					// the desired file has not yet been created, and we have to use SaveAs and file name
					if (bUseSaveAs) {
						workbook.invoke("SaveAs", sFileName);
					}
					else {
						workbook.invoke("Save");
					}
					workbook.invoke("Close");
					workbook.close();
				}
				if (workbooks != null) {
					workbooks.close();
				}
				if (application != null) {
					application.invoke("Quit");
					application.close();
				}

				Ole32.CoUninitialize();
			}
			catch (Throwable ex) {
				throw new NuclosReportException(getCommonLocaleDelegate().getMessage(
						"DOCExport.2", "Die Datei {0} konnte nicht erstellt werden", sFileName) + ".\n", ex);
			}
		}
		return sFileName;
	}

	private void writeDataClipboard(DispatchPtr worksheet, ResultVO resultvo) throws COMException, NuclosBusinessException {
		short iColumnNum = 0;

		//create a buffer with data in Excel Clipboard Format
		final StringBuffer sbf = new StringBuffer();

		// Column headers first
		for (Iterator<ResultColumnVO> iter = resultvo.getColumns().iterator(); iter.hasNext(); iColumnNum++) {
			final ResultColumnVO resultcolumnvo = iter.next();
			sbf.append(resultcolumnvo.getColumnLabel());
			if (iColumnNum < resultvo.getColumnCount() - 1) {
				sbf.append("\t");
			}

			final DispatchPtr ptrRange = worksheet.getObject("Columns", getExcelColumnName(iColumnNum + 1));
			Class<?> columnClass = resultcolumnvo.getColumnClass();
			if (Date.class.isAssignableFrom(columnClass)) {
				ptrRange.put("NumberFormat", "TT.MM.JJJJ");
			} else if (columnClass == String.class) {
				ptrRange.put("NumberFormat", "@");
			} else if (columnClass == Boolean.class) {
				// @todo set the Boolean Type
				//ptrRange.put("NumberFormat", "");
			}
			ptrRange.close();
		}
		sbf.append("\n");

		// export data
		for (int i = 0; i < resultvo.getRows().size(); i++) {
			iColumnNum = 0;
			final Object[] aoDataRow = resultvo.getRows().get(i);
			for (int iColumn = 0; iColumn < resultvo.getColumns().size(); iColumn++, iColumnNum++) {
				final Object value = aoDataRow[iColumn];
				if (value != null) {
					final ResultColumnVO resultColumn = resultvo.getColumns().get(iColumn);
					// quote Strings with " and cast " in Strings with ""
					if (value instanceof String) {
						sbf.append("\"");
						sbf.append(((String) value).replaceAll("\"", "\"\""));
						sbf.append("\"");
					}
					else {
						sbf.append(resultColumn.format(value));
					}
				}
				if (iColumnNum < resultvo.getColumnCount() - 1) {
					sbf.append("\t");
				}
			}
			sbf.append("\n");
		}

		// copy data to the clipboard:
		final StringSelection stsel = new StringSelection(sbf.toString());
		sbf.setLength(0);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stsel, stsel);

		// paste the clipboard to Excel:
		final DispatchPtr ptrRange = worksheet.getObject("Range", "A1");
		worksheet.invoke("paste", ptrRange);
		ptrRange.close();

		// clear the clipboard:
		final StringSelection stselEmpty = new StringSelection("");
		clipboard.setContents(stselEmpty, stselEmpty);
	}

	/**
	 * get the name of an excel column by index
	 * @param iColumnIndex
	 * @return column name of the specified index
	 * @throws NuclosBusinessException
	 */
	public String getExcelColumnName(int iColumnIndex) throws NuclosBusinessException {
		final StringBuffer sbCorner = new StringBuffer(2);
		final char[] acAlphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
		final int iLetterCount = acAlphabet.length;
		if (iColumnIndex < 1 || iColumnIndex > 256) {
			throw new NuclosBusinessException(getCommonLocaleDelegate().getMessage(
					"XLSExport.1", "Der Excel-Spaltenindex muss zwischen 1 und 256 liegen."));
		}

		if (iColumnIndex <= iLetterCount) {
			sbCorner.append(acAlphabet[iColumnIndex - 1]);
		}
		else {
			final int iFirstIndex = (iColumnIndex % iLetterCount) > 0 ? (iColumnIndex / iLetterCount) - 1 : iColumnIndex / iLetterCount - 2;
			sbCorner.append(acAlphabet[iFirstIndex]);

			final int iSecondIndex = (iColumnIndex % iLetterCount) > 0 ? (iColumnIndex % iLetterCount) - 1 : iLetterCount - 1;
			sbCorner.append(acAlphabet[iSecondIndex]);
		}
		return sbCorner.toString();
	}

	private String getExcelSheetName() {
		String result = "";
		if (reportOutputVO != null) {
			result = reportOutputVO.getSheetname();
		}
		if (result == null || result.equals("")) {
			result = getCommonLocaleDelegate().getMessage("XLSExport.2", "Daten aus Nucleus");
		}

		return result;
	}
	
	@Override
	protected NuclosReportPrintJob getNuclosReportPrintJob() {
		return new XLSPrintJob();
	}

}	// class XLSExport

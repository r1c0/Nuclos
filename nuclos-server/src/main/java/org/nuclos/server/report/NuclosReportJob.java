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
package org.nuclos.server.report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.ejb3.ReportFacadeLocal;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz job for printing reports on the server side.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:lars.rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 *
 */
public class NuclosReportJob extends NuclosQuartzJob {
	private static final Logger logger = Logger.getLogger(NuclosReportJob.class);

	public NuclosReportJob() {
		super(new ReportJobImpl());
	}

	/**
	 * inner class ReportJobImpl: implementation of NuclosReportJob
	 */
	private static class ReportJobImpl implements Job {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			final String sReportName = (String) dataMap.get("ReportName");
			final File reportOutputDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.REPORT_PATH);

			logger.debug(new Date() + ": starting scheduled report: " + sReportName);

			if (sReportName == null || reportOutputDir == null) {
				logger.error("Not able to process 4PM Job: Report Name or Report Output Path is null");
			}
			else {
				try {
					final ReportFacadeLocal reportFacade = ServiceLocator.getInstance().getFacade(ReportFacadeLocal.class);
					final DatasourceFacadeLocal datasourceFacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);
					ReportVO reportVO = null;
					boolean bFound = false;

					for (Iterator<ReportVO> iter = reportFacade.getReports().iterator(); iter.hasNext();) {
						reportVO = iter.next();
						if (reportVO.getName().compareTo(sReportName) == 0) {
							bFound = true;
							break;
						}
					}

					if (bFound) {
						final ResultVO resultVO = datasourceFacade.executeQuery(reportVO.getDatasourceId(), new HashMap<String, Object>(), null);
						final String sFileName = createXlsFile(sReportName, resultVO, null, reportOutputDir);
						logger.debug("Successfully created file " + sFileName);
					}
					else {
						logger.error("Report " + sReportName + " not found");
						writeErrorFile(reportOutputDir, "Report: " + sReportName + " not found");
					}
				}
				catch (NuclosReportException ex) {
					writeErrorFile(reportOutputDir, ex.getMessage());
					ex.printStackTrace();
				}
				catch (Exception ex) {
					/** @todo this is not enough */
					ex.printStackTrace();
				}
			}
		}

		/**
		 * write message to an error file
		 * @param sMessage
		 */
		private void writeErrorFile(File reportOutputDir, String sMessage) {
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(new File(reportOutputDir, "ERROR_4pmReport.txt"), true));
				ps.println(sMessage);
			}
			catch (FileNotFoundException e) {
				logger.error("Error while trying to create errorfile.");
				e.printStackTrace();
			}
			finally {
				/** @todo don't close if ps == null! */
				ps.close();
			}
		}

		/**
		 * @param sExportPath
		 * @param sReportName
		 * @param sExtension
		 * @return
		 * @todo remove duplicate code: code is copied from org.nuclos.client.report.reportrunner.export.ReportExporterImpl
		 */
		protected synchronized String getFileName(File file, String sReportName, String sExtension) {
			if (file.isDirectory()) {
				final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
				String name = sReportName + "_" + df.format(Calendar.getInstance(Locale.getDefault()).getTime());
				name = name.replaceAll("[/+*%?#!.:]", "-");
				return new File(file, name + sExtension).getAbsolutePath();
			}
			else {
				return file.getAbsolutePath();
			}
		}

		/**
		 * @param sExportPath
		 * @return
		 * @throws NuclosReportException
		 * @todo remove duplicate code: code is copied from org.nuclos.client.report.reportrunner.export.ReportExporterImpl
		 */
		protected synchronized String createExportDir(String sExportPath) throws NuclosReportException {
			if (sExportPath != null) {
				final int iLastDotPosition = sExportPath.lastIndexOf(".");
				final int iLastSlashPosition = sExportPath.lastIndexOf("\\");	/** @todo use File ctor or system property */
				if (iLastSlashPosition >= iLastDotPosition) {
					final File fileExportDir = new File(sExportPath);
					if (!fileExportDir.exists()) {
						if (!fileExportDir.mkdir()) {
							throw new NuclosReportException(StringUtils.getParameterizedExceptionMessage("AbstractReportExporter.1", sExportPath));
								//"Das Verzeichnis " + sExportPath + " konnte nicht angelegt werden.");
						}
					}
				}
			}
			else {
				sExportPath = System.getProperty("java.io.tmpdir");
			}
			return sExportPath;
		}

		/**
		 * @param sReportName
		 * @param resultvo
		 * @param sourceFile
		 * @param parameter
		 * @return
		 */
		private synchronized String createXlsFile(final String sReportName, final ResultVO resultvo, final String sourceFile, final File outputDir) throws NuclosReportException {
			final String EXCEL_SHEET_NAME = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_EXCEL_SHEET_NAME);
			String fileName = null;

			BufferedOutputStream os = null;
			BufferedInputStream is = null;
			try {
				//prepare destination
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
				fileName = getFileName(outputDir, sReportName, ".xls");

				final HSSFWorkbook workbook;
				if ((sourceFile != null) && (sourceFile.length() > 0)) {
					is = new BufferedInputStream(new FileInputStream(sourceFile));
					workbook = new HSSFWorkbook(is);
				}
				else {
					workbook = new HSSFWorkbook();
				}

				final HSSFSheet sheet;

				if (workbook.getSheetIndex(EXCEL_SHEET_NAME) == -1) {
					sheet = workbook.createSheet(EXCEL_SHEET_NAME);
				}
				else {
					sheet = workbook.getSheet(EXCEL_SHEET_NAME);
				}

				int iRowNum = 0;
				int iColumnNum = 0;
/*
			Object[][] data = new Object[resultvo.getRows().size()][resultvo.getColumns().size()];
			for (int i = 0; i < resultvo.getRows().size(); i++) {
				for (int j = 0; j < resultvo.getColumns().size(); j++) {
					data[i][j] = ((Object[]) resultvo.getRows().get(i))[j];
				}
			}
*/
				// Column headers first
				HSSFRow sheetRow;
				HSSFCell cell;
				sheetRow = sheet.getRow(iRowNum);
				if (sheetRow == null) {
					sheetRow = sheet.createRow(iRowNum);
				}

				for (Iterator<ResultColumnVO> iter = resultvo.getColumns().iterator(); iter.hasNext(); iColumnNum++) {
					ResultColumnVO resultColumn = iter.next();
					cell = sheetRow.getCell(iColumnNum);
					if (cell == null) {
						cell = sheetRow.createCell(iColumnNum);
					}
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(new HSSFRichTextString(resultColumn.getColumnLabel()));
				}
				iRowNum++;

				// create cell styles: if createCellStyle is called too many times Excel fails opening the file with message
				// too many different cell formats
				final HSSFCellStyle STYLE_DATE = workbook.createCellStyle();
				STYLE_DATE.setDataFormat((short) 14); //14 = dd.mm.jjjj
				final HSSFCellStyle STYLE_STRING = workbook.createCellStyle();
				STYLE_STRING.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				final HSSFCellStyle STYLE_NUMERIC = workbook.createCellStyle();
				STYLE_NUMERIC.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));

				// export data
				for (int i = 0; i < resultvo.getRows().size(); i++, iRowNum++) {
					sheetRow = sheet.getRow(iRowNum);
					if (sheetRow == null) {
						sheetRow = sheet.createRow(iRowNum);
					}

					iColumnNum = 0;
					final Object[] aoDataRow = resultvo.getRows().get(i);
					for (int iColumn = 0; iColumn < resultvo.getColumns().size(); iColumn++, iColumnNum++) {
						final Object oValue = aoDataRow[iColumn];
						final ResultColumnVO columnvo = resultvo.getColumns().get(iColumn);
						cell = sheetRow.getCell(iColumnNum);
						final boolean bCellCreated;
						if (cell == null) {
							cell = sheetRow.createCell(iColumnNum);
							bCellCreated = true;
						}
						else {
							bCellCreated = false;
						}

						if (oValue instanceof Date) {
							cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
							if (bCellCreated && (cell.getCellStyle().getDataFormat() == 0)) {
								cell.setCellStyle(STYLE_DATE);
							}
							cell.setCellValue((Date) oValue);
						} else if (oValue instanceof Number) {
							if (bCellCreated) {
								cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
								cell.setCellStyle(STYLE_NUMERIC);
							}
							cell.setCellValue(((Number) oValue).doubleValue());
						} else if (oValue instanceof Boolean) {
							cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
							cell.setCellValue((Boolean) oValue);
						} else if (oValue instanceof String) {
							if (bCellCreated) {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								cell.setCellStyle(STYLE_STRING);
							}
							cell.setCellValue(new HSSFRichTextString((String) oValue));
						}
						if (oValue == null) {
							cell.setCellValue(new HSSFRichTextString((String) null));
						}
					}
				}

				os = new BufferedOutputStream(new FileOutputStream(fileName));
				workbook.write(os);
			}
			catch (FileNotFoundException ex) {
				throw new NuclosReportException(StringUtils.getParameterizedExceptionMessage("reportjob.error.missing.template", sourceFile), ex);//"Die Vorlage " + sourceFile + " kann nicht gefunden werden.", ex);
			}
			catch (IOException ex) {
				throw new NuclosReportException(StringUtils.getParameterizedExceptionMessage("CSVExport.1", fileName), ex);//"Fehler beim Erzeugen der Datei: " + sFileName, ex);
			}
			catch (Exception ex) {
				throw new NuclosReportException(ex);
			}
			finally {
				if (os != null) {
					try {
						os.close();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}

			return fileName;
		}

	}	// inner class ReportJobImpl

}	// class NuclosReportJob

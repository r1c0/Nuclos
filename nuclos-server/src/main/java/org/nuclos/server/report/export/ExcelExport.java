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
package org.nuclos.server.report.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.Export;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ReportFieldDefinition;
import org.nuclos.server.report.ReportFieldDefinitionFactory;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ExcelExport implements Export {

	private final ReportOutputVO.Format format;

	private DatasourceFacadeLocal datasourceFacade;

	public ExcelExport(ReportOutputVO.Format format) {
		super();
		this.format = format;
	}

	@Autowired
	public void setDatasourceFacade(DatasourceFacadeLocal datasourceFacade) {
		this.datasourceFacade = datasourceFacade;
	}

	@Override
	public NuclosFile test(ReportOutputVO output) throws NuclosReportException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		try {
			newWorkbook(output).write(baos);
			return new NuclosFile(output.getDescription() != null ? output.getDescription() : "Preview" + format.getExtension(), baos.toByteArray());
		}
		catch (IOException e) {
			throw new NuclosReportException(e);
		}
		finally {
			try {
				baos.close();
			}
			catch (IOException e) { }
		}
	}

	@Override
	public NuclosFile export(ReportOutputVO output, Map<String, Object> params, Locale locale, int maxrows) throws NuclosReportException {
		ResultVO resultvo;
		try {
			resultvo = datasourceFacade.executeQuery(output.getDatasourceId(), params, maxrows);
			return export(newWorkbook(output), output.getSheetname(), resultvo, ReportFieldDefinitionFactory.getFieldDefinitions(resultvo), output.getDescription() != null ? output.getDescription() : "Export");
		}
		catch (IOException e1) {
			throw new NuclosReportException(e1);
		}
		catch (CommonBusinessException e1) {
			throw new NuclosReportException(e1);
		}
	}

	@Override
	public NuclosFile export(ResultVO result, List<ReportFieldDefinition> fields) throws NuclosReportException {
		return export(newWorkbook(), null, result, fields, "Export");
	}

	private NuclosFile export(Workbook wb, String sheetname, ResultVO result, List<ReportFieldDefinition> fields, String name) throws NuclosReportException {
		sheetname = sheetname != null ? sheetname : SpringLocaleDelegate.getInstance().getMessage("XLSExport.2", "Daten aus Nucleus");
		Sheet s = wb.getSheet(sheetname);
		if (s == null) {
			s = wb.createSheet(sheetname);
		}

		int iRowNum = 0;
		int iColumnNum = 0;
		CreationHelper createHelper = wb.getCreationHelper();

		Row row = getRow(s, 0);

		Map<Integer, CellStyle> styles = new HashMap<Integer, CellStyle>();

		for (Iterator<ResultColumnVO> i = result.getColumns().iterator(); i.hasNext(); iColumnNum++) {
			i.next();
			Cell cell = getCell(row, iColumnNum);
			cell.setCellValue(fields.get(iColumnNum).getLabel());

			CellStyle style = wb.createCellStyle();
			String f = getFormat(fields.get(iColumnNum));
			if (f != null) {
				style.setDataFormat(createHelper.createDataFormat().getFormat(f));
			}
			styles.put(iColumnNum, style);
		}
		iRowNum++;

		// export data
		for (int i = 0; i < result.getRows().size(); i++, iRowNum++) {
			iColumnNum = 0;
			Object[] dataRow = result.getRows().get(i);
			row = getRow(s, iRowNum);
			for (int j = 0; j < result.getColumns().size(); j++, iColumnNum++) {
				Object value = dataRow[j];
				Cell c = getCell(row, iColumnNum);
				ReportFieldDefinition def = fields.get(j);

				if (value != null) {
					if (value instanceof List) {
						final StringBuilder sb = new StringBuilder();
						for (Iterator<?> it = ((List<?>) value).iterator(); it.hasNext();) {
							final Object v = it.next();
							sb.append(CollectableFieldFormat.getInstance(def.getJavaClass()).format(def.getOutputformat(), v));
							if (it.hasNext()) {
								sb.append(", ");
							}
						}
						c.setCellValue(sb.toString());
					}
					else {
						if (Date.class.isAssignableFrom(def.getJavaClass())) {
							c.setCellStyle(styles.get(iColumnNum));
							c.setCellValue((Date) value);
						}
						else if (Integer.class.isAssignableFrom(def.getJavaClass())) {
							c.setCellStyle(styles.get(iColumnNum));
							c.setCellValue((Integer) value);
						}
						else if (Double.class.isAssignableFrom(def.getJavaClass())) {
							c.setCellStyle(styles.get(iColumnNum));
							c.setCellValue((Double) value);
						}
						else {
							c.setCellValue(String.valueOf(value));
						}
					}
				}
				else {
					c.setCellValue("");
				}
			}
		}

		try {
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
				Sheet sheet = wb.getSheetAt(sheetNum);
				for (Row r : sheet) {
					for (Cell c : r) {
						if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
							evaluator.evaluateFormulaCell(c);
						}
					}
				}
			}
		}
		catch (Exception e) {
		} // ignore any Exception
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		try {
			wb.write(baos);
			return new NuclosFile(name + format.getExtension(), baos.toByteArray());
		}
		catch (IOException e) {
			throw new NuclosReportException(e);
		}
		finally {
			try {
				baos.close();
			}
			catch (IOException e) { }
		}
	}

	private Workbook newWorkbook() {
		if (format == ReportOutputVO.Format.XLS) {
			return new HSSFWorkbook();
		}
		else if (format == ReportOutputVO.Format.XLSX) {
			return new XSSFWorkbook();
		}
		else {
			throw new IllegalStateException();
		}
	}

	private Workbook newWorkbook(ReportOutputVO output) throws IOException {
		if (output.getSourceFile() != null) {
			if (format == ReportOutputVO.Format.XLS) {
				return new HSSFWorkbook(new ByteArrayInputStream(output.getSourceFileContent().getData()));
			}
			else if (format == ReportOutputVO.Format.XLSX) {
				return new XSSFWorkbook(new ByteArrayInputStream(output.getSourceFileContent().getData()));
			}
			else {
				throw new IllegalStateException();
			}
		}
		else {
			return newWorkbook();
		}
	}

	private Row getRow(Sheet sheet, int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		return row;
	}

	private Cell getCell(Row row, int colIndex) {
		Cell cell = row.getCell(colIndex);
		if (cell == null) {
			cell = row.createCell(colIndex);
		}
		return cell;
	}

	private String getFormat(ReportFieldDefinition def) {
		if (def.getOutputformat() != null) {
			return def.getOutputformat();
		}
		else {
			if (Date.class.isAssignableFrom(def.getJavaClass())) {
				DateFormat df = SpringLocaleDelegate.getInstance().getDateFormat();
				if (df instanceof SimpleDateFormat) {
					return ((SimpleDateFormat) df).toPattern();
				}
				else {
					return "dd/mm/yyyy";
				}
			}
			else if (Integer.class.isAssignableFrom(def.getJavaClass())) {
				return "#,##0";
			}
			else if (Double.class.isAssignableFrom(def.getJavaClass())) {
				return "#,##0.00";
			}
			else {
				return null;
			}
		}
	}
}

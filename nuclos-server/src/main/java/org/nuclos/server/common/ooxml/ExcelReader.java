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

package org.nuclos.server.common.ooxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Helper class providing simple read-only access for examing Excel documents (both OOXML and OLE2).
 * 
 * <p>Cell locations are interpreted as cell or area references using the Excel cell reference format, e.g.
 * {@code A1}, {@code A1:B10}, or {@code Sheet1!A1}. If the sheet name is omitted, the active sheet is used.
 * 
 * <p>Cell references will yield a Java object which represents the cell value at the given location. The
 * mapping from Excel values to Java objects is described below.  
 * 
 * <p>Area references will yield a 2-dimensional Object array ({@code Object[][]}). The first component represents
 * the row, the second the column (0-based from within the area). For example, given the area reference {@code A1:C2},
 * {@code arr[0][2]} represents the cell value at {@code C1}.
 * 
 * <p>Mapping from cell values to Java objects:
 * <table border="1">
 *   <tr><th>Cell type</th><th>Java representation</th></tr>
 *   <tr><td>Text</td><td>the text value as {@link java.lang.String String}</td></tr>
 *   <tr><td>Number</td><td>the numeric value as {@link java.lang.Double Double} (see note below)</td></tr>
 *   <tr><td>Number (formatted as date)</td><td>the date value as {@link java.util.Date} (see note below)</td></tr>
 *   <tr><td>Logical</td><td>the logical value as {@link java.lang.Boolean Boolean}</td></tr>
 *   <tr><td>Formula</td><td>the cached value of the calculation (according to the type of the cached value)</td></tr>
 *   <tr><td>Error</td><td>the error text as {@link java.lang.String String}, e.g. {@literal "#DIV/0!"}</td></tr>
 *   <tr><td>Blank</td><td>{@literal null}</td></tr>
 * </table>
 * 
 * <p>Note: Excel stores date values internally as numbers (i.e. number of days from a given base date,
 * usually 1900-01-01).
 * This method uses the following heuristic to distinguish number and date values: If a (numeric) cell is
 * formatted as date, its value is treated as date and converted into a {@link java.util.Date} object.
 * All other numeric values are mapped to {@link Double}.  Since Excel uses internally IEEE 754, 
 * {@link Double} is the most appropriate representation for the retrieved numeric values.
 */
public class ExcelReader {

	private final Workbook workbook;

	public ExcelReader(InputStream is) throws IOException {
		PushbackInputStream pbis = new PushbackInputStream(is, 16);
		if (POIXMLDocument.hasOOXMLHeader(pbis)) {
			workbook = new XSSFWorkbook(pbis);
		}  else {
			workbook = new HSSFWorkbook(pbis);
		}
	}

	public ExcelReader(Workbook workbook) {
		this.workbook = workbook;
	}

	public Object getCellValue(String cellName) {
		// First, test if the given cell name is a (single) cell reference
		// (this is needed in order to distinguish "A1" from "A1:A1").
		CellReference cr;
		try {
			cr = new CellReference(cellName);
		} catch (Exception ex) {
			// No -- we will later try to parse it as area reference
			// (a superset, so we don't care about the exception here) 
			cr = null;
		}

		Object value;
		if (cr != null) {
			value = getSingleCellValue(cr);
		} else {
			AreaReference ar = new AreaReference(cellName);
			value = getAreaValueArray(ar);
		}
		return value;
	}

	/**
	 * Similar to {@link #getCellValue} but takes a (var-arg) array of cell names
	 * and returns the corresponding values as list.
	 */
	public List<Object> getCellValues(String...cellNames) {
		List<Object> cellValues = new ArrayList<Object>();
		for (String cellName : cellNames) {
			cellValues.add(getCellValue(cellName));
		}
		return cellValues;
	}

	/**
	 * Returns the cell value for the given cell reference.
	 */
	private Object getSingleCellValue(CellReference cr) {
		Object value = null;

		Sheet sheet;
		String sheetName = cr.getSheetName();
		if (sheetName != null) {
			sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet '" + sheetName + "' does not exist");
			}
		} else {
			sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
		}

		Row row = sheet.getRow(cr.getRow());
		if (row != null) {
			Cell cell = row.getCell(cr.getCol());
			if (cell != null) {
				int cellType = cell.getCellType();
				if (cellType == Cell.CELL_TYPE_FORMULA) {
					cellType = cell.getCachedFormulaResultType();
				}
				value = getCellValue(cell, cellType);
			}
		}

		return value;
	}

	private Object[][] getAreaValueArray(AreaReference ar) {
		int cols = Math.abs(ar.getFirstCell().getCol() - ar.getLastCell().getCol()) + 1;
		int rows = Math.abs(ar.getFirstCell().getRow() - ar.getLastCell().getRow()) + 1;
		CellReference[] crs = ar.getAllReferencedCells();
		if (crs.length != rows * cols) {
			throw new IllegalArgumentException("Invalid area reference " + ar);
		}
		Object[][] rect = new Object[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				rect[r][c] = getSingleCellValue(crs[r * cols + c]);
			}
		}
		return rect;
	}

	private static Object getCellValue(Cell cell, int cellType) {
		switch (cellType) {
		case Cell.CELL_TYPE_BLANK:
			return null;
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue();
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue();
			} else {
				return cell.getNumericCellValue();
			}
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_ERROR:
			return FormulaError.forInt(cell.getErrorCellValue()).getString();
		case Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula();
		default:
			throw new IllegalArgumentException("Unknown POI cell type " + cellType);
		}
	}
	
	public static class InvalidCellReferenceException extends IllegalArgumentException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidCellReferenceException(String cell) {
			super(cell);
		}
		
	}
}

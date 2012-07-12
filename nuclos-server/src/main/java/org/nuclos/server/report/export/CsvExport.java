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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.csvparser.ExcelCSVPrinter;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ServerParameterProvider;
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

/**
 * Create a CSV-export from a template.
 *
 * @author thomas.schiffmann
 */
@Configurable
public class CsvExport implements Export {

	private DatasourceFacadeLocal datasourceFacade;
	
	private final char cDelimiter;
	private final int iQuoteLevel;
	private final char cQuote;
	private final boolean bWriteHeader;

	public CsvExport() {
		this(';', 2, '\"', true);
	}

	public CsvExport(char cDelimiter, int iQuoteLevel, char cQuote, boolean bWriteHeader) {
		this.cDelimiter = cDelimiter;
		this.iQuoteLevel = iQuoteLevel;
		this.cQuote = cQuote;
		this.bWriteHeader = bWriteHeader;
	}
	
	@Autowired
	public void setDatasourceFacade(DatasourceFacadeLocal datasourceFacade) {
		this.datasourceFacade = datasourceFacade;
	}
	
	@Override
	public NuclosFile test(ReportOutputVO output) throws NuclosReportException {
		if (output.getSourceFile() != null) {
			return new NuclosFile("Vorschau.csv", output.getSourceFileContent().getData());
		}
		throw new NuclosFatalException("No template file assigned!");
	}

	public NuclosFile export(ReportOutputVO output, Map<String, Object> params, Locale locale, int maxrows) throws NuclosReportException {
		ResultVO resultvo;
		try {
			resultvo = datasourceFacade.executeQuery(output.getDatasourceId(), params, maxrows);
			
			if (output.getSourceFile() != null) {
				return export(output, resultvo, locale);
			}
			else {
				return export(resultvo, ReportFieldDefinitionFactory.getFieldDefinitions(resultvo));
			}
		}
		catch (CommonBusinessException e1) {
			throw new NuclosReportException(e1);
		}
	}
	
	public NuclosFile export(ResultVO result, List<ReportFieldDefinition> fields) throws NuclosReportException {
		String sFileName = null;
		String encoding = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_DEFAULT_ENCODING);
		if (encoding == null) 
			encoding = "Cp1252";
		// test encoding.
		try {
			Charset.forName(encoding);
		} catch (UnsupportedCharsetException e) {
			encoding = "Cp1252";
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		try {
			OutputStreamWriter writer = new OutputStreamWriter(baos, encoding);
			final ExcelCSVPrinter excelCSVPrinter = new ExcelCSVPrinter(writer, iQuoteLevel, cDelimiter, cQuote, false);
			excelCSVPrinter.changeDelimiter(cDelimiter);

			if(bWriteHeader) {
				// Column headers first
				for (ResultColumnVO columnvo : result.getColumns()) {
					excelCSVPrinter.write(columnvo.getColumnLabel());
				}
				excelCSVPrinter.writeln();
			}

			// export data
			for (int iRow = 0; iRow < result.getRows().size(); iRow++) {
				for (int iColumn = 0; iColumn < result.getColumns().size(); iColumn++) {
					final Object value = result.getRows().get(iRow)[iColumn];
					final ResultColumnVO column = result.getColumns().get(iColumn);
					excelCSVPrinter.write(column.format(value));
				}
				excelCSVPrinter.writeln();
			}
			excelCSVPrinter.close();
			
			return new NuclosFile(sFileName, baos.toByteArray());
		}
		catch (IOException ex) {
			throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage(
					"CSVExport.1", "Fehler beim Erzeugen der Datei: {0}", sFileName));
		}
		finally {
			try {
				baos.close();
			}
			catch (IOException e) {}
		}
	}

	private NuclosFile export(ReportOutputVO output, ResultVO result, Locale locale) throws NuclosReportException {
		ByteArrayInputStream bais = new ByteArrayInputStream(output.getSourceFileContent().getData());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(bais, "UTF-8"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"));
			Pattern pattern = Pattern.compile("\\{[^\\}]*\\}");
			String line = reader.readLine();
			// 0..* header lines
			while (line != null && !pattern.matcher(line).find()) {
				writer.append(line);
				writer.newLine();
				line = reader.readLine();
			}
			// 1 template line, prepare (replace column names with column index for MessageFormat
			for (int i = 0; i < result.getColumnCount(); i++) {
				ResultColumnVO column = result.getColumns().get(i);
				line = line.replace("{" + column.getColumnLabel(), "{" + i);
			}
			// write lines by template line
			MessageFormat format = new MessageFormat(line, locale);
			Format[] formats = new Format[format.getFormatsByArgumentIndex().length];
			System.arraycopy(format.getFormatsByArgumentIndex(), 0, formats, 0, formats.length);
			NullFormat nullFormat = new NullFormat();
			NullValue nullValue = new NullValue();
			for (Object[] row : result.getRows()) {
				// set formats for each row
				for (int i = 0; i < row.length; i++) {
					if (row[i] == null) {
						row[i] = nullValue;
						format.setFormatByArgumentIndex(i, nullFormat);
					}
					else {
						format.setFormatByArgumentIndex(i, formats[i]);
					}
				}
				writer.append(format.format(row));
				writer.newLine();
			}
			writer.flush();
			return new NuclosFile(output.getDescription() + ".csv", baos.toByteArray());
		}
		catch (UnsupportedEncodingException e) {
			throw new NuclosReportException(e);
		}
		catch (IOException e) {
			throw new NuclosReportException(e);
		}
		finally {
			try {
				bais.close();
			} catch (IOException e) { }
			try {
				baos.close();
			} catch (IOException e) { }
		}
	}

	private class NullFormat extends Format {

		private static final long serialVersionUID = -5137522621157459206L;

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	}

	private class NullValue extends Object {

	}
}

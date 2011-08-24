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
package org.nuclos.server.report.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import org.nuclos.common.NuclosFile;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

public class TemplatedCsvExport {

	private final ReportOutputVO output;

	public TemplatedCsvExport(ReportOutputVO output) {
		this.output = output;
	}

	public NuclosFile export(ResultVO resultvo, Locale locale) throws NuclosReportException {
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
			for (int i = 0; i < resultvo.getColumnCount(); i++) {
				ResultColumnVO column = resultvo.getColumns().get(i);
				line = line.replace("{" + column.getColumnLabel(), "{" + i);
			}
			// write lines by template line
			MessageFormat format = new MessageFormat(line, locale);
			for (Object[] row : resultvo.getRows()) {
				writer.append(format.format(row));
				writer.newLine();
			}
			NuclosFile result = new NuclosFile(output.getDescription() + ".csv", baos.toByteArray());
			return result;
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
}

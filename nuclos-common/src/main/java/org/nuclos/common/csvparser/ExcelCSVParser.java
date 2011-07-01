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
package org.nuclos.common.csvparser;

import java.io.*;
import java.util.Vector;

public class ExcelCSVParser extends CSVReader {

	public ExcelCSVParser(Reader r, char delimiter) {
		super(r, delimiter, '\"', true, true);
	}

	public ExcelCSVParser(Reader r, char separator, char quote,
			boolean allowMultiLineFields, boolean trim) {
		super(r, separator, quote, allowMultiLineFields, trim);
	}

	public String[][] getAllValues() {

		Vector<String[]> v = null;
		try {
			v = new Vector<String[]>();
			String[] line;
			while ((line = this.getAllFieldsInLine()).length != 0) {
				v.add(line);
			}
		}
		catch (EOFException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		if (v.size() == 0) {
			return null;
		}
		String[][] result = new String[v.size()][];
		return v.toArray(result);
	}
}

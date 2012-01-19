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

import java.io.IOException;
import java.io.Writer;

public class ExcelCSVPrinter extends CSVWriter {

	public ExcelCSVPrinter(Writer pw, int quoteLevel, char separator,
			char quote, boolean trim) {
		super(pw, quoteLevel, separator, quote, trim);
		// TODO Auto-generated constructor stub
	}

	public ExcelCSVPrinter(Writer pw) {
		super(pw);
		// TODO Auto-generated constructor stub
	}

	public void write(String values) throws IOException {
		this.put(values);
	}

	public void writeln() {
		this.nl();
	}

	public void changeDelimiter(char newDelimiter) {
		this.separator = newDelimiter;
	}

	public char getDelimiter() {
		return this.separator;
	}


}

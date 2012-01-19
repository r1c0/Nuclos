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
import java.util.ArrayList;
import java.util.List;

/**
 * A parser for CSV files.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Stefan.Barbulescu@novabit.de">Stefan Barbulescu</a>
 * @version 01.00.00
 */

public class CSVParser extends CSVReader {

	public CSVParser(Reader reader, char cSeparator) {
		super(reader, cSeparator, '\"', true, true);
	}

	public CSVParser(Reader reader) {
		super(reader, ';', '\"', true, true);
	}

	public String[] getLine() throws IOException {
		final List<String> lst = new ArrayList<String>();
		try {
			while (true) {
				final String sFieldValue = this.get();
				if (sFieldValue == null) {
					break;
				}
				lst.add(sFieldValue);
			}
		}
		catch (EOFException ex) {
			return null;
		}

		return lst.toArray(new String[lst.size()]);
	}

}	// class CSVParser

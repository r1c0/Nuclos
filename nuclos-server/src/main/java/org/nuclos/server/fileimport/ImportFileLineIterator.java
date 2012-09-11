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
package org.nuclos.server.fileimport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;

import org.nuclos.common.ParameterProvider;
import org.nuclos.common.csvparser.CSVParser;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;

/**
 * Utility class for iterating over lines in a csv file.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportFileLineIterator implements Iterator<String[]> {

	private int lineCount = 0;
	private int currentLine;

	private BufferedReader reader;
	private final CSVParser parser;

	public ImportFileLineIterator(GenericObjectDocumentFile importfile, int headerlines, String delimiter, String encoding) throws IOException {
		this.lineCount = ImportUtils.countLines(importfile, delimiter);
		
		// @see NUCLOS-620
		if (encoding == null) 
			encoding = "Cp1252";
		// test encoding.
		try {
			Charset.forName(encoding);
		} catch (UnsupportedCharsetException e) {
			encoding = "Cp1252";
		}

		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(importfile.getContents()), encoding));

		if (!StringUtils.looksEmpty(delimiter)) {
			parser = new CSVParser(reader, delimiter.charAt(0));
		}
		else {
			parser = new CSVParser(reader);
		}
		currentLine = ImportUtils.skipHeaderLines(parser, headerlines);
	}

	@Override
	public boolean hasNext() {
		return currentLine < lineCount;
	}

	@Override
	public String[] next() {
		try {
			currentLine++;
			return parser.getLine();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public int getLineCount() {
		return lineCount;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void finalize() throws Throwable {
		if (reader != null) {
			reader.close();
		}
	}
}

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
package org.nuclos.tools.ruledoc.javaToHtml;

import java.io.*;

/**
 * Abstract superclass for all converters for converting
 *
 *
 *
 */
public abstract class JavaSourceConverter implements IJavaSourceConverter {
	/** The source code being converted */
	protected JavaSource source;
	/** The result of the conversion */

	private Java2HtmlConversionOptions conversionOptions;

	public JavaSourceConverter() {
		this((JavaSource) null);
	}

	public JavaSourceConverter(JavaSource source) {
		setSource(source);
		setConversionOptions(Java2HtmlConversionOptions.getDefault());
	}

	public void setSource(JavaSource source) {
		this.source = source;
	}

	/** Returns the default filename extension for the output format of this converter,
	 * e.g. "html" or "tex". */
	@Override
	public abstract String getDefaultFileExtension();

	/**
	 * Is called to convert the object 'source' to the destination
	 * format. The result is stored in 'result' and can be retrieved
	 * by calling getResult().
	 */
	public abstract void convert(BufferedWriter writer) throws IOException;

	/**
	 * Is called to convert the object 'source' to the destination
	 * format. The result is stored in 'result' and can be retrieved
	 * by calling getResult().
	 */
	public void convert(Writer writer) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(writer);
			convert(bw);
			bw.flush();
		}
		catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Convenience methode for conversion. The converter will only have to be
	 * instanciated once and a call to this method will convert the given source
	 * code to the given writer.
	 * @see #convert(Writer)
	 * @param source The source code to be converted to the output format specified
	 *         by this converter.
	 * @param writer The writer to write the output to.
	 * @throws IOException if an output error occures while writing to the given writer.
	 */
	@Override
	public void convert(JavaSource source, Writer writer) throws IOException {
		setSource(source);
		convert(writer);
	}

	/**
	 * Returns a header for the result document.
	 * This one will be placed before the first block of converted
	 * code.
	 * Subclasses can return an empty String (&quot;&quot;) if there is none neccessary.
	 */
	public abstract String getDocumentHeader();

	/**
	 * Returns a footer for the result document.
	 * This one will be placed behind the last block of converted
	 * code.
	 * Subclasses can return an empty String (&quot;&quot;) if there is none neccessary.
	 */
	public abstract String getDocumentFooter();

	/**
	 * Returns the code that has to be placed between two blocks
	 * of converted code.
	 * Subclasses can return an empty String (&quot;&quot;) if there is none neccessary.
	 */
	@Override
	public abstract String getBlockSeparator();

	public JavaSourceStyleTable getColorTable() {
		return getConversionOptions().getStyleTable();
	}

	public void setConversionOptions(Java2HtmlConversionOptions options) {
		this.conversionOptions = options;
	}

	@Override
	public Java2HtmlConversionOptions getConversionOptions() {
		return conversionOptions;
	}

	@Override
	public void writeDocumentHeader(Writer writer) throws IOException {
		writer.write(getDocumentHeader());
	}

	@Override
	public void writeDocumentFooter(Writer writer) throws IOException {
		writer.write(getDocumentFooter());
	}

	@Override
	public void writeBlockSeparator(Writer writer) throws IOException {
		writer.write(getBlockSeparator());
	}
}

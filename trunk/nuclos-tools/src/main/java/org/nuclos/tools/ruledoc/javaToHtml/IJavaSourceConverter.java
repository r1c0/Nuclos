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

import java.io.IOException;
import java.io.Writer;

public interface IJavaSourceConverter {
	/** Returns the default filename extension for the output format of this converter,
	 * e.g. "html" or "tex". */
	public String getDefaultFileExtension();

	/**
	 * Convenience methode for conversion. The converter will only have to be
	 * instanciated once and a call to this method will convert the given source
	 * code to the given writer.
	 * @param source The source code to be converted to the output format specified
	 *         by this converter.
	 * @param writer The writer to write the output to.
	 * @throws IOException if an output error occures while writing to the given writer.
	 */
	public void convert(JavaSource source, Writer writer) throws IOException;

	/**
	 * Returns the code that has to be placed between two blocks
	 * of converted code.
	 * Subclasses can return an empty String (&quot;&quot;) if there is none neccessary.
	 */
	public String getBlockSeparator();

	public Java2HtmlConversionOptions getConversionOptions();

	public void writeDocumentHeader(Writer writer) throws IOException;

	public void writeDocumentFooter(Writer writer) throws IOException;

	public void writeBlockSeparator(Writer writer) throws IOException;
}

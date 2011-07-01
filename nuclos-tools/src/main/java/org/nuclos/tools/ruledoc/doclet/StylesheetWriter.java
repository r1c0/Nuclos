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
package org.nuclos.tools.ruledoc.doclet;

import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import java.io.IOException;

/**
 * Writes the style sheet for the doclet output.
 *
 *
 */
public class StylesheetWriter extends CommonDocletWriter {

	/**
	 * Constructor.
	 */
	public StylesheetWriter(ConfigurationImpl configuration,
			String filename) throws IOException {
		super(configuration, filename);
	}

	/**
	 * Generate the style file contents.
	 * @throws DocletAbortException
	 */
	public static void generate(ConfigurationImpl configuration) {
		StylesheetWriter stylegen;
		String filename = "";
		try {
			filename = "stylesheet.css";
			stylegen = new StylesheetWriter(configuration, filename);
			stylegen.generateStyleFile();
			stylegen.close();
		}
		catch (IOException exc) {
			configuration.standardCommonMessage.error(
					"doclet.exception_encountered",
					exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate the style file contents.
	 */
	protected void generateStyleFile() {
		print("/* ");
		printText("doclet.Style_line_1");
		println(" */");
		println("");

		print("/* ");
		printText("doclet.Style_line_2");
		println(" */");
		println("");

		print("/* ");
		printText("doclet.Style_line_3");
		println(" */");
		println("body { background-color: #FFFFFF }");
		println("");

		print("/* ");
		printText("doclet.Style_Headings");
		println(" */");
		println("h1 { font-size: 145% }");
		println("");

		print("/* ");
		printText("doclet.Style_line_4");
		println(" */");
		print(".TableHeadingColor     { background: #CCCCFF }");
		print(" /* ");
		printText("doclet.Style_line_5");
		println(" */");
		print(".TableSubHeadingColor  { background: #EEEEFF }");
		print(" /* ");
		printText("doclet.Style_line_6");
		println(" */");
		print(".TableRowColor         { background: #FFFFFF }");
		print(" /* ");
		printText("doclet.Style_line_7");
		println(" */");
		println("");

		print("/* ");
		printText("doclet.Style_line_8");
		println(" */");
		println(".FrameTitleFont   { font-size: 100%; font-family: Helvetica, Arial, sans-serif }");
		println(".FrameHeadingFont { font-size:  90%; font-family: Helvetica, Arial, sans-serif }");
		println(".FrameItemFont    { font-size:  90%; font-family: Helvetica, Arial, sans-serif }");
		println("");

		// Removed doclet.Style_line_9 as no longer needed

		print("/* ");
		printText("doclet.Style_line_10");
		println(" */");
		print(".NavBarCell1    { background-color:#EEEEFF;}");
		print(" /* ");
		printText("doclet.Style_line_6");
		println(" */");
		print(".NavBarCell1Rev { background-color:#00008B;}");
		print(" /* ");
		printText("doclet.Style_line_11");
		println(" */");

		print(".NavBarFont1    { font-family: Arial, Helvetica, sans-serif; ");
		println("color:#000000;}");
		print(".NavBarFont1Rev { font-family: Arial, Helvetica, sans-serif; ");
		println("color:#FFFFFF;}");
		println("");

		print(".NavBarCell2    { font-family: Arial, Helvetica, sans-serif; ");
		println("background-color:#FFFFFF;}");
		print(".NavBarCell3    { font-family: Arial, Helvetica, sans-serif; ");
		println("background-color:#FFFFFF;}");
		println("");

	}

}




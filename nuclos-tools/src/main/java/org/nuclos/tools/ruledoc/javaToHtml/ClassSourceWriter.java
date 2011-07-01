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

import org.nuclos.tools.ruledoc.doclet.ConfigurationImpl;
import org.nuclos.tools.ruledoc.doclet.SubWriterHolderWriter;
import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import java.io.*;
import java.util.ArrayList;

/**
 * Schreibt eine HTML-Datei mit dem Source-Code einer Klasse und f\u00fchrenden
 * Zeilennummern. Oben und unten werden Header und Footer erzeugt.
 *
 *
 */
public class ClassSourceWriter extends SubWriterHolderWriter {
	protected ClassDoc classdoc = null;

	/**
	 * Konstruktor. Erzeugt einen neuen ClassSourceWriter.
	 *
	 * @param configuration
	 *            Die Konfigurations-Instanz
	 * @param classdoc
	 *            Die ClassDoc-Instanz der Klasse, zu der die Source-Ausgabe
	 *            generiert werden soll
	 * @param path
	 *            Der Pfad, in dem die HTML-Datei erstellt werden soll, ohne
	 *            abschlie\u00dfenden File.separator
	 * @param filename
	 *            Der Dateiname der generierten Datei
	 * @throws IOException
	 */
	public ClassSourceWriter(ConfigurationImpl configuration,
			ClassDoc classdoc, String path, String filename) throws IOException {
		super(configuration, path, filename, getBackPath(path + File.separator));
		this.classdoc = classdoc;
		configuration.currentcd = classdoc;
	}

	/**
	 * Generiert eine HTML-Seite, die den Source-Code der Klasse, die \u00fcber
	 * classdoc bezeichnet wird, enth\u00e4lt.
	 *
	 * @param configuration
	 *            Die globale Konfigurations-Instanz
	 * @param classdoc
	 *            Die ClassDoc-Instanz, die zu der Klasse geh\u00f6rt, zu der die
	 *            Source-Code-Seite generiert werden soll
	 * @param path
	 *            Der Pfad, in dem die generierte Source-Code-HTML-Datei erzeugt
	 *            werden soll, ohne abschlie\u00dfenden File.separator
	 * @param filename
	 *            Der Dateiname, den die generierte Source-Code-HTML-Datei haben
	 *            soll
	 */
	public static void generate(ConfigurationImpl configuration,
			ClassDoc classdoc, String path, String filename) {
		try {
			ClassSourceWriter csw = new ClassSourceWriter(configuration,
					classdoc, path, filename);
			csw.convertClass();
			csw.close();
		}
		catch (Exception exc) {
			configuration.standardCommonMessage.error(
					"doclet.exception_encountered", exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Given a File path string, this will return the reverse path. For example,
	 * if the File path string is (in Windows) "java\lang" the method will
	 * return the string "..\".
	 */
	public static String getBackPath(String path) {
		if (path == null || path.length() == 0) {
			return "";
		}
		StringBuffer backpath = new StringBuffer();
		for (int i = 0; i < path.length(); i++) {
			char ch = path.charAt(i);
			if (ch == File.separatorChar) {
				backpath.append("..");
				backpath.append(File.separatorChar);
			} // there is always a trailing fileseparator
		}
		return backpath.toString();
	}

	/**
	 * Given a string, replace all tabs with the appropriate number of spaces.
	 *
	 * @param configuration
	 *            the current configuration of the doclet.
	 * @param s
	 *            the String to scan.
	 */
	private static void replaceTabs(Configuration configuration, StringBuffer s) {
		int index;
		int col;
		StringBuffer whitespace;
		final int DEFAULT_TAB_STOP_LENGTH = 8;
		int linksourcetab = DEFAULT_TAB_STOP_LENGTH;

		while ((index = s.indexOf("\t")) != -1) {
			whitespace = new StringBuffer();
			col = index;

			do {
				whitespace.append(" ");
				col++;
			} while ((col % linksourcetab) != 0);
			s.replace(index, index + 1, whitespace.toString());
		}
	}

	/**
	 * Convert the given Class to an HTML
	 *

	 */
	public void convertClass() {
		SourcePosition sp = classdoc.position();
		if (sp == null) {
			return;
		}
		File file = sp.file();
		if (file == null) {
			return;
		}
		String clname = "";
		MethodDoc[] methods = classdoc.methods();

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].name().compareTo(METHOD_NAME) == 0) {
				MethodDoc rul = methods[i];
				Tag[] x = rul.tags();
				for (int j = 0; j < x.length; j++) {

					if (x[j].name().compareTo("@name") == 0) {
						clname = x[j].text();
						break;
					}

				}// end of inner for
			}// end of if

			printHtmlHeader(clname, configuration.metakeywords
					.getMetaKeywords(classdoc));
		}

		hr();

		h2();
		/*if (pkgname.length() > 0) {
			font("4");
			print(pkgname);
			fontEnd();
			br();
		}*/

		print("<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"lightgrey\">"
				+ clname + "</td></tr></table></DD>\n");

		h2End();

		if (ConfigurationImpl.USE_STANDARD_SOURCE_WRITER) {
			printSourceStandard(file);
		}
		else {
			printSourceJava2HTML(file, out);
		}
		hr();
		// navLinks(true);
		// printBottom();
		// printBodyHtmlEnd();
	}

	/**
	 * Standard implementation of source code writer.
	 *
	 * @param sourceInput
	 *            The writer to write the generated HTML code to.
	 */
	@SuppressWarnings("unchecked")
	public void printSourceStandard(File sourceInput) {
		println("<!-- ======== START OF SOURCECODE DATA ======== -->");

		print("<DIV CLASS=\"CodeBackground\">");
		pre();
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(
					sourceInput));
			ArrayList lines = new ArrayList();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			for (int i = 0; i < lines.size(); i++) {
				line = (String) lines.get(i);
				anchor("line." + Integer.toString(i + 1));
				StringBuffer lineBuffer = new StringBuffer(format(line));
				replaceTabs(configuration, lineBuffer);
				print("<SPAN CLASS=\"SourceLineNumber\">"
						+ formatLineNo(i + 1, lines.size()) + "</SPAN>"
						+ " <SPAN CLASS=\"Code\">" + lineBuffer.toString()
						+ "</SPAN>");
				if (i < lines.size() - 1) {
					println("");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		preEnd();
		print("</DIV>");

		println("<!-- ========= END OF SOURCECODE DATA ========= -->");
	}

	public void printSourceJava2HTML(File sourceInput, Writer writer) {
		synchronized (lock) {
			println("<!-- ======== START OF SOURCECODE DATA ======== -->");
			Java2HtmlConversionOptions options = Java2HtmlConversionOptions
					.getDefault();
			options.setShowLineNumbers(true);
			options.setAddLineAnchors(true);
			options.setLineAnchorPrefix("line.");

			// Style einstellen
			JavaSourceStyleTable jsst = options.getStyleTable();
			jsst.put(JavaSourceType.BACKGROUND, new JavaSourceStyleEntry(
					new RGB(0xEE, 0xEE, 0xEE)));
			options.setStyleTable(jsst);
			//

			try {
				Reader reader = new FileReader(sourceInput);
				JavaSource source = new JavaSourceParser(options).parse(reader);

				JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter(
						source);
				converter.setConversionOptions(options);
				converter.convert(writer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			println("<!-- ========= END OF SOURCECODE DATA ========= -->");
		}
	}

	/**
	 * Wandelt die Zahl lineNo in einen String um, und formatiert die Nummer mit
	 * so vielen f\u00fchrenden Nullen, dass ein String der zur\u00fcckgegebenen L\u00e4nge
	 * noch eine Zahl der Gr\u00f6\u00dfe maxLineNo halten kann.
	 *
	 * @param lineNo
	 *            Die zu konvertierende Zeilennummer
	 * @param maxLineNo
	 *            Die gr\u00f6\u00dfte Zeilennummer, die noch im zur\u00fcckgegebenen String
	 *            platz haben soll
	 * @return Formatierte Zeilennummer
	 */
	public String formatLineNo(int lineNo, int maxLineNo) {
		String result = Integer.toString(lineNo);
		int length = Integer.toString(maxLineNo).length();
		for (int i = result.length(); i < length; i++) {
			result = "0" + result;
		}
		return result;
	}

	/**
	 * Replace every '<' character in comments with "&lt;".
	 *
	 * @param str
	 *            the string to format.
	 */
	protected static String format(String str) {
		if (str == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer(str);
		int next = -1;
		while ((next = sb.indexOf("<", next + 1)) != -1) {
			sb.replace(next, next + 1, "&lt;");
		}
		return sb.toString();
	}

	/**
	 * Given a <code>Doc</code>, return the anchor name which has been
	 * assigned to that Doc in the Source-Code-HTML Output.
	 *
	 * @param d
	 *            the <code>Doc</code> to check.
	 * @return the name of the anchor
	 */
	public static String getAnchorName(Doc d) {
		return "line." + d.position().line();
	}
}

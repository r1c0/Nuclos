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
 * Generate the Help File for the generated API documentation. The help file
 * contents are helpful for browsing the generated documentation.
 *
 *
 */
public class HelpWriter extends CommonDocletWriter {

	/**
	 * Constructor to construct HelpWriter object.
	 *
	 * @param filename
	 *            File to be generated.
	 */
	public HelpWriter(ConfigurationImpl configuration, String filename)
			throws IOException {
		super(configuration, filename);
	}

	/**
	 * Construct the HelpWriter object and then use it to generate the help
	 * file. The name of the generated file is "help-doc.html". The help file
	 * will get generated if and only if "-helpfile" and "-nohelp" is not used
	 * on the command line.
	 *
	 * @throws DocletAbortException
	 */
	public static void generate(ConfigurationImpl configuration) {
		HelpWriter helpgen;
		String filename = "";
		try {
			filename = "help-doc.html";
			helpgen = new HelpWriter(configuration, filename);
			helpgen.generateHelpFile();
			helpgen.close();
		}
		catch (IOException exc) {
			configuration.standardCommonMessage.error(
					"doclet.exception_encountered", exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate the help file contents.
	 */
	protected void generateHelpFile() {
		printHtmlHeader("Novabit Doclet Hilfe");
		navLinks(true);
		hr();

		printHelpFileContents();

		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print the help file contents from the resource file. While generating the
	 * help file contents it also keeps track of user options. If "-notree" is
	 * used, then the "overview-tree.html" will not get generated and hence help
	 * information also will not get generated.
	 */
	protected void printHelpFileContents() {
		center();
		h1();
		print("Aufbau dieses API Dokuments");
		h1End();
		centerEnd();
		print("Dieses API (Application Programming Interface) Dokument ist aufgebaut aus Seiten, die zu den jeweiligen Punkten in der Navigations-Leiste geh\u00f6ren. Sie werden im Folgenden beschrieben.");
		if (configuration.createoverview) {
			h3();
			printText("doclet.Overview");
			h3End();
			blockquote();
			p();
			printText("doclet.Help_line_3", getHyperLink(
					"overview-summary.html", configuration
					.getText("doclet.Overview")));
			blockquoteEnd();
		}
		h3();
		print("Alle Regeln");
		h3End();
		blockquote();
		p();
		print("Jedes Regel hat eine Seite, die eine Liste von folgenden enth\u00e4lt :");
		ul();
		li();
		print("Name");
		li();
		print("Beschreibung");
		li();
		print("\u00c4nderung");
		li();
		print("Verwendung");

		ulEnd();
		blockquoteEnd();

		h3();
		printText("doclet.Help_line_23");
		h3End();
		print("Diese Links f\u00fchren zu der n\u00e4chsten oder vorherigen Regel oder gleichartigen Seite.");
		h3();
		printText("doclet.Help_line_25");
		h3End();
		print("Diese Links zeigen die HTML Frames und blenden sie aus.  Alle Seiten sind verf\u00fcgbar mit und ohne Frames.");
		p();

	}

	/**
	 * Highlight the word "Help" in the navigation bar as this is the help file.
	 */
	@Override
	protected void navLinkHelp() {
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		bold("API Hilfe");
		fontEnd();
		navCellEnd();
	}
}

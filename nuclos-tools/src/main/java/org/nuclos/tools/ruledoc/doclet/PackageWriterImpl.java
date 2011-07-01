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

import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.PackageSummaryWriter;
import com.sun.tools.doclets.internal.toolkit.util.DirectoryManager;
import com.sun.tools.doclets.internal.toolkit.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class to generate file for each package contents in the right-hand frame.
 * This will list all the Class Kinds in the package. A click on any class-kind
 * will update the frame with the clicked class-kind page.
 *
 *
 */
public class PackageWriterImpl extends CommonDocletWriter implements
		PackageSummaryWriter {

	/**
	 * The prev package name in the alpha-order list.
	 */
	protected PackageDoc prev;

	//public ConfigurationImpl configuration;

	/**
	 * The next package name in the alpha-order list.
	 */
	protected PackageDoc next;

	/**
	 * The package being documented.
	 */
	protected PackageDoc packageDoc;

	/**
	 * The name of the output file.
	 */
	private static final String OUTPUT_FILE_NAME = "package-summary.html";

	protected static String METHOD_NAME = "rule";

	/**
	 * Constructor to construct PackageWriter object and to generate
	 * "package-summary.html" file in the respective package directory. For
	 * example for package "java.lang" this will generate file
	 * "package-summary.html" file in the "java/lang" directory. It will also
	 * create "java/lang" directory in the current or the destination directory
	 * if it doesen't exist.
	 *
	 * @param configuration
	 *            the configuration of the doclet.
	 * @param packageDoc
	 *            PackageDoc under consideration.
	 * @param prev
	 *            Previous package in the sorted array.
	 * @param next
	 *            Next package in the sorted array.
	 */
	public PackageWriterImpl(ConfigurationImpl configuration,
			PackageDoc packageDoc, PackageDoc prev, PackageDoc next)
			throws IOException {
		super(configuration, DirectoryManager.getDirectoryPath(packageDoc),
				OUTPUT_FILE_NAME, DirectoryManager.getRelativePath(packageDoc
				.name()));
		this.prev = prev;
		this.next = next;
		this.packageDoc = packageDoc;
	}

	/**
	 * Return the name of the output file.
	 *
	 * @return the name of the output file.
	 */
	@Override
	public String getOutputFileName() {
		return OUTPUT_FILE_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSummaryHeader() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSummaryFooter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeClassesSummary(ClassDoc[] classes, String label) {
		if (classes.length > 0) {
			sortNameTags(classes);
			//Arrays.sort(classes);
			tableIndexSummary();
			boolean printedHeading = false;
			for (int i = 0; i < classes.length; i++) {
				if (!printedHeading) {
					label = "Alle Regeln";
					String label2 = "Name";
					String label3 = "Beschreibung";
					printFirstRow(label, label2, label3);

					printedHeading = true;
				}
				if (!Util.isCoreClass(classes[i])
						|| !configuration.isGeneratedDoc(classes[i])) {
					continue;
				}
				trBgcolorStyle("white", "TableRowColor");
				summaryRow(1);
				bold();
				print(getClassLink(classes[i]));
				boldEnd();
				summaryRowEnd();
				summaryRow(1);
				if (Util.isDeprecated(classes[i])) {
					boldText("doclet.Deprecated");
					if (classes[i].tags("deprecated").length > 0) {
						space();
						printSummaryDeprecatedComment(classes[i], classes[i]
								.tags("deprecated")[0]);
					}
				}
				else {
					printSummaryComment(classes[i]);
					for (int j = 0; j < classes.length;) {// 2nd Spalte
						String labelBeschreibung = "";
						print(getTagLink(classes[i], "", labelBeschreibung,
								false, "", ""));
						break;
					}// end for-loop "2nd Spalte"
				}
				summaryRowEnd();
				trEnd();
			}
			tableEnd();
			println("&nbsp;");
			p();
		}
	}

	protected static <T extends ClassDoc> void sortNameTags(T[] classArrays) {
		Arrays.sort(classArrays, new Comparator<T>() {
			@Override
			public int compare(T cd1, T cd2) {
				String sRuleName1 = getNameTag(cd1);
				String sRuleName2 = getNameTag(cd2);
				return sRuleName1 != null ? sRuleName1
						.compareTo((sRuleName2 != null ? sRuleName2 : "")) : 0;
			}
		});
	}

	protected static String getNameTag(ClassDoc cd) {
		String sRuleName = null;
		MethodDoc[] methods = cd.methods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].name().compareTo(METHOD_NAME) == 0) {
				MethodDoc rul = methods[i];
				Tag[] x = rul.tags();
				for (int j = 0; j < x.length; j++) {
					if (x[j].name() != null && x[j].name().compareTo("@name") == 0) {
						sRuleName = x[j].text();
						break;
					}
				}
			}
		}
		return sRuleName;
	}

	public String getTagLink(ClassDoc cd, String where, String label,
			boolean bold, String color, String target) {
		boolean nameUnspecified = label.length() == 0;
		if (nameUnspecified) {

			MethodDoc[] methods = cd.methods();

			for (int i = 0; i < methods.length; i++) {

				if (methods[i].name().compareTo(METHOD_NAME) == 0) {

					MethodDoc rul = methods[i];

					Tag[] x = rul.tags();

					for (int j = 0; j < x.length; j++) {

						if (x[j].name().compareTo("@beschreibung") == 0) {

							label = x[j].text();

						}

					}// end of inner for

				}// end of if
			}

			displayLength += label.length();

			// Create a tool tip if we are linking to a class or interface.
			// Don't
			// create one if we are linking to a member.
			/*String title = where == null || where.length() == 0 ? (getText(cd
					.isInterface() ? "doclet.Href_Interface_Title"
					: "doclet.Href_BeschreibungTag_Title", cd
					.containingPackage().name())) : "";*/
			String title = "Beschreibung der Regel";
			if (cd.isIncluded()) {

				if (isGeneratedDoc(cd)) {
					String filename = pathToClass(cd);
					return getHyperLink(filename, where, label, bold, color,
							title, target);
				}

			}
			else {
				String crosslink = getCrossClassLink(cd.qualifiedName(), where,
						label, bold, color, true);
				if (crosslink != null) {
					return crosslink;
				}
			}
			if (nameUnspecified) {
				displayLength -= label.length();
				label = configuration.getClassName(cd);
				displayLength += label.length();
			}
		}
		return label;
	}

	/**
	 * Print the table heading for the class-listing.
	 *
	 * @param label1
	 *            Label for the Class kind listing.
	 */
	protected void printFirstRow(String label1, String label2, String label3) {
		tableHeaderStart("#CCCCFF");
		font("+3");
		bold("<CENTER>" + label1 + "</CENTER>");
		hr();
		font("+1");

		font("+1");
		bold("<table border=0  cellspacing=11 >"
				+ "<tr>"
				+ "<td nowrap=\"nowrap\"  colspan=30   valign=\"bottom\" align=\"center\" >\n"
				+ label2
				+ "</td>"
				+ "<td nowrap=\"nowrap\"   colspan=45    valign=\"bottom\" align=\"right\">\n"
				+ label3 + "</td>" + "</tr>" + "</table> ");
		tableHeaderEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writePackageDescription() {
		if (packageDoc.inlineTags().length > 0) {
			anchor("package_description");
			h2(configuration.getText("doclet.Package_Description", packageDoc
					.name()));
			p();
			printInlineComment(packageDoc);
			p();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writePackageTags() {
		printTags(packageDoc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writePackageHeader(String heading) {
		String pkgName = packageDoc.name();
		String[] metakeywords = {pkgName + " " + "package"};
		printHtmlHeader(pkgName, metakeywords, true);
		navLinks(true);
		hr();
		writeAnnotationInfo(packageDoc);
		//h2(configuration.getText("doclet.Package") + " " + heading);
		if (packageDoc.inlineTags().length > 0 && !configuration.nocomment) {
			printSummaryComment(packageDoc);
			p();
			bold(configuration.getText("doclet.See"));
			br();
			printNbsps();
			printHyperLink("", "package_description", configuration
					.getText("doclet.Description"), true);
			p();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writePackageFooter() {
		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print "Use" link for this pacakge in the navigation bar.
	 */
	protected void navLinkClassUse() {
		navCellStart();
		printHyperLink("package-use.html", "", configuration
				.getText("doclet.navClassUse"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print "PREV PACKAGE" link in the navigation bar.
	 *//*
	protected void navLinkPrevious() {
		if (prev == null) {
			printText("doclet.Prev_Package");
		} else {
			String path = DirectoryManager.getRelativePath(packageDoc.name(),
					prev.name());
			printHyperLink(path + "package-summary.html", "", configuration
					.getText("doclet.Prev_Package"), true);
		}
	}

	*//**
 * Print "NEXT PACKAGE" link in the navigation bar.
 *//*
	protected void navLinkNext() {
		if (next == null) {
			printText("doclet.Next_Package");
		} else {
			String path = DirectoryManager.getRelativePath(packageDoc.name(),
					next.name());
			printHyperLink(path + "package-summary.html", "", configuration
					.getText("doclet.Next_Package"), true);
		}
	}
*/
	/**
	 * Print "Tree" link in the navigation bar. This will be link to the package
	 * tree file.
	 *//*
	protected void navLinkTree() {
		navCellStart();
		printHyperLink("package-tree.html", "", configuration
				.getText("doclet.Tree"), true, "NavBarFont1");
		navCellEnd();
	}*/

	/**
	 * Highlight "Package" in the navigation bar, as this is the package page.
	 */
	@Override
	protected void navLinkPackage() {
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Package");
		fontEnd();
		navCellEnd();
	}
}

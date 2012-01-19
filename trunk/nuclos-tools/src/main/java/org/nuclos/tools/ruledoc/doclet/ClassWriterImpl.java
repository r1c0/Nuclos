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
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.MethodWriter;
import com.sun.tools.doclets.internal.toolkit.taglets.ParamTaglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * Generate the Class Information Page.
 *
 * @see com.sun.javadoc.ClassDoc
 * @see java.util.Collections
 * @see java.util.List
 * @see java.util.ArrayList
 * @see java.util.HashMap
 *
 *
 *
 */
public class ClassWriterImpl extends SubWriterHolderWriter implements
		ClassWriter {

	protected ClassDoc classDoc;

	protected MethodWriter methodSubWriter;

	protected ClassTree classtree;

	protected ClassDoc prev;

	protected ClassDoc next;

	private String methodName = "rule";

	/**
	 * @param classDoc
	 *            the class being documented.
	 * @param prevClass
	 *            the previous class that was documented.
	 * @param nextClass
	 *            the next class being documented.
	 * @param classTree
	 *            the class tree for the given class.
	 */
	public ClassWriterImpl(ClassDoc classDoc, ClassDoc prevClass,
			ClassDoc nextClass, ClassTree classTree) throws Exception {
		super(ConfigurationImpl.getInstance(), DirectoryManager
				.getDirectoryPath(classDoc.containingPackage()), classDoc
				.name()
				+ ".html", DirectoryManager.getRelativePath(classDoc
				.containingPackage().name()));
		this.classDoc = classDoc;
		configuration.currentcd = classDoc;
		this.classtree = classTree;
		this.prev = prevClass;
		this.next = nextClass;

	}

	/**
	 * Print this package link
	 */
	@Override
	protected void navLinkPackage() {
		navCellStart();
		printHyperLink("package-summary.html", "", configuration
				.getText("doclet.Package"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print class use link
	 */
	protected void navLinkClassUse() {

	}

	/**
	 * Print previous package link
	 */
	@Override
	protected void navLinkPrevious() {
		if (prev == null) {
			println();
			printText("doclet.Prev");
			print(" : ");
			font("2");
			fontStyle("NavBarFont1");
			bold();
			print("Vorherige Regel");
			print(" ");
			boldEnd();
		}
		else {
			printText("doclet.Prev");
			print(" : ");
			font("2");
			fontStyle("NavBarFont1");
			bold();
			printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS, prev, "",
					"Vorherige Regel", true));
			boldEnd();
			p();
		}
	}

	/**
	 * Print next package link
	 */
	@Override
	protected void navLinkNext() {
		if (next == null) {
			printText("doclet.Next");
			print(" : ");
			font("2");
			fontStyle("NavBarFont1");
			bold();
			print("N\344chste Regel");
			print(" ");
		}
		else {
			printText("doclet.Next");
			print(" : ");
			font("2");
			fontStyle("NavBarFont1");
			bold();
			printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS, next, "",
					"N\344chste Regel", true));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeHeader(String header) {

		String clname = classDoc.name();
		printHtmlHeader(clname, configuration.metakeywords
				.getMetaKeywords(classDoc), true);
		navLinks(true);
		hr();
		println("<!-- ======== START OF CLASS DATA ======== -->");
		h2();

		String cltype = "Regel";

		String tagname = "";
		MethodDoc[] methods = classDoc.methods();

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].name().compareTo(methodName) == 0) {
				MethodDoc rul = methods[i];
				Tag[] x = rul.tags();
				for (int j = 0; j < x.length; j++) {

					if (x[j].name().compareTo("@name") == 0) {
						tagname = x[j].text();
						break;
					}

				}
			}
		}

		String classLabel = "<FONT SIZE=\""
				+ "2"
				+ "\">"
				+ cltype
				+ "</FONT>"
				+ " "
				+ "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"white\">"
				+ tagname + "</td></tr></table></DD>\n";
		print(classLabel);

		h2End();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFooter() {
		println("<!-- ========= END OF CLASS DATA ========= -->");
		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeClassSignature(String modifiers) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeClassDescription() {
		if (!configuration.nocomment) {
			// generate documentation for the class.
			if (classDoc.inlineTags().length > 0) {
				printInlineComment(classDoc);
				p();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeClassTagInfo() {
		if (!configuration.nocomment) {
			// Print Information about all the tags here
			printTags(classDoc);
			hr();
			p();
		}
		else {
			hr();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeClassDeprecationInfo() {

	}

	/**
	 * Print the class hierarchy tree for the given class.
	 *
	 * @param type
	 *            the class to print the hierarchy for.
	 * @return return the amount that should be indented in the next level of
	 *         the tree.
	 */
	@SuppressWarnings("unused")
	private int writeTreeForClassHelper(Type type) {

		return 0;
	}

	/**
	 * Print the class hierarchy tree for this class only.
	 */
	@Override
	public void writeClassTree() {

	}

	/**
	 * Write the type parameter information.
	 */
	@Override
	public void writeTypeParamInfo() {
		if (classDoc.typeParamTags().length > 0) {
			dl();
			dt();
			TagletOutput output = (new ParamTaglet()).getTagletOutput(classDoc,
					getTagletWriterInstance(false));
			print(output.toString());
			dlEnd();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSubClassInfo() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSubInterfacesInfo() {

	}

	/**
	 * If this is the interface which are the classes, that implement this?
	 */
	@Override
	public void writeInterfaceUsageInfo() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeImplementedInterfacesInfo() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSuperInterfacesInfo() {

	}

	protected void navLinkTree() {

	}

	@Override
	protected void printSummaryDetailLinks() {
		try {
			tr();
			tdVAlignClass("top", "NavBarCell3");
			font("-2");
			print("  ");
			navSummaryLinks();
			fontEnd();
			tdEnd();
			tdVAlignClass("top", "NavBarCell3");
			font("-2");
			navDetailLinks();
			fontEnd();
			tdEnd();
			trEnd();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new DocletAbortException();
		}
	}

	protected void navSummaryLinks() throws Exception {

	}

	/**
	 * Method navDetailLinks
	 *
	 * @throws Exception
	 *
	 */
	protected void navDetailLinks() throws Exception {

	}

	protected void navGap() {
		space();
		print('|');
		space();
	}

	/**
	 * If this is an inner class or interface, write the enclosing class or
	 * interface.
	 */
	@Override
	public void writeNestedClassInfo() {

	}

	@Override
	protected void navLinkClass() {
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		bold("Regel");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Return the classDoc being documented.
	 *
	 * @return the classDoc being documented.
	 */
	@Override
	public ClassDoc getClassDoc() {
		return classDoc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void completeMemberSummaryBuild() {
		p();
	}
}

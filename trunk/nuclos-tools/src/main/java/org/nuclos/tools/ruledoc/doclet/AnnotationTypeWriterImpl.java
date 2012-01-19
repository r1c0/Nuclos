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
import com.sun.tools.doclets.internal.toolkit.AnnotationTypeWriter;
import com.sun.tools.doclets.internal.toolkit.util.DirectoryManager;
import com.sun.tools.doclets.internal.toolkit.util.Util;

/**
 * Generate the Class Information Page.
 * @see com.sun.javadoc.ClassDoc
 * @see java.util.Collections
 * @see java.util.List
 * @see java.util.ArrayList
 * @see java.util.HashMap
 *
 *
 *
 */
public class AnnotationTypeWriterImpl extends SubWriterHolderWriter
		implements AnnotationTypeWriter {

	protected AnnotationTypeDoc annotationType;

	protected Type prev;

	protected Type next;

	/**
	 * @param annotationType the annotation type being documented.
	 * @param prevType the previous class that was documented.
	 * @param nextType the next class being documented.
	 */
	public AnnotationTypeWriterImpl(AnnotationTypeDoc annotationType,
			Type prevType, Type nextType)
			throws Exception {
		super(ConfigurationImpl.getInstance(),
				DirectoryManager.getDirectoryPath(annotationType.containingPackage()),
				annotationType.name() + ".html",
				DirectoryManager.getRelativePath(annotationType.containingPackage().name()));
		this.annotationType = annotationType;
		configuration.currentcd = annotationType.asClassDoc();
		this.prev = prevType;
		this.next = nextType;
	}

	/**
	 * Print this package link
	 */
	@Override
	protected void navLinkPackage() {
		navCellStart();
		printHyperLink("package-summary.html", "",
				configuration.getText("doclet.Package"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print class page indicator
	 */
	@Override
	protected void navLinkClass() {
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Class");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print class use link
	 */
	protected void navLinkClassUse() {
		navCellStart();
		printHyperLink("class-use/" + filename, "",
				configuration.getText("doclet.navClassUse"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print previous package link
	 */
	@Override
	protected void navLinkPrevious() {
		if (prev == null) {
			printText("doclet.Prev_Class");
		}
		else {
			printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS,
					prev.asClassDoc(), "",
					configuration.getText("doclet.Prev_Class"), true));
		}
	}

	/**
	 * Print next package link
	 */
	@Override
	protected void navLinkNext() {
		if (next == null) {
			printText("doclet.Next_Class");
		}
		else {
			printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS,
					next.asClassDoc(), "",
					configuration.getText("doclet.Next_Class"), true));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeHeader(String header) {

		String pkgname = (annotationType.containingPackage() != null) ?
				annotationType.containingPackage().name() : "";
		String clname = annotationType.name();

		printHtmlHeader(clname,
				configuration.metakeywords.getMetaKeywords(annotationType), true);
		navLinks(true);
		hr();
		println("<!-- ======== START OF CLASS DATA ======== -->");
		h2();
		if (pkgname.length() > 0) {
			font("-1");
			print(pkgname);
			fontEnd();
			br();
		}
		print(header + getTypeParameterLinks(new LinkInfoImpl(
				LinkInfoImpl.CONTEXT_CLASS_HEADER,
				annotationType, false)));
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
	public void writeAnnotationTypeSignature(String modifiers) {
		dl();
		dt();
		preNoNewLine();
		writeAnnotationInfo(annotationType);
		print(modifiers);
		String name = annotationType.name() +
				getTypeParameterLinks(new LinkInfoImpl(
						LinkInfoImpl.CONTEXT_CLASS_SIGNATURE, annotationType, false));
		if (configuration().linksource) {
			printSrcLink(annotationType, name);
		}
		else {
			bold(name);
		}
		dlEnd();
		preEnd();
		p();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAnnotationTypeDescription() {
		if (!configuration.nocomment) {
			// generate documentation for the class.
			if (annotationType.inlineTags().length > 0) {
				printInlineComment(annotationType);
				p();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAnnotationTypeTagInfo() {
		boolean needHr = annotationType.elements().length > 0;
		if (!configuration.nocomment) {
			// Print Information about all the tags here
			printTags(annotationType);
			if (needHr) {
				hr();
			}
			p();
		}
		else if (needHr) {
			hr();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAnnotationTypeDeprecationInfo() {
		hr();
		Tag[] deprs = annotationType.tags("deprecated");
		if (Util.isDeprecated(annotationType)) {
			boldText("doclet.Deprecated");
			if (deprs.length > 0) {
				Tag[] commentTags = deprs[0].inlineTags();
				if (commentTags.length > 0) {

					space();
					printInlineDeprecatedComment(annotationType, deprs[0]);
				}
			}
			p();
		}
	}

	protected void navLinkTree() {
		/*navCellStart();
						printHyperLink("package-tree.html", "",
								configuration.getText("doclet.Tree"), true, "NavBarFont1");
						navCellEnd();*/
	}

	@Override
	protected void printSummaryDetailLinks() {
		/*try {
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
						} catch (Exception e) {
								e.printStackTrace();
								throw new DocletAbortException();
						}*/
	}

	protected void navSummaryLinks() throws Exception {
		/*printText("doclet.Summary");
						space();
						MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
								configuration.getBuilderFactory().getMemberSummaryBuilder(this);
						writeNavSummaryLink(memberSummaryBuilder,
								"doclet.navAnnotationTypeRequiredMember",
								VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED);
						navGap();
						writeNavSummaryLink(memberSummaryBuilder,
								"doclet.navAnnotationTypeOptionalMember",
								VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL);   */
	}

	/* private void writeNavSummaryLink(MemberSummaryBuilder builder,
							String label, int type) {
					AbstractMemberWriter writer = ((AbstractMemberWriter) builder.
							getMemberSummaryWriter(type));
					if (writer == null) {
								printText(label);
					} else {
							writer.printNavSummaryLink(null,
									! builder.getVisibleMemberMap(type).noVisibleMembers());
					}
			}*/

	/**
	 * Method navDetailLinks
	 *
	 * @throws Exception
	 *
	 */
	protected void navDetailLinks() throws Exception {
		/*printText("doclet.Detail");
						space();
						MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
								configuration.getBuilderFactory().getMemberSummaryBuilder(this);
						AbstractMemberWriter writerOptional =
								((AbstractMemberWriter) memberSummaryBuilder.
										getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL));
						AbstractMemberWriter writerRequired =
								((AbstractMemberWriter) memberSummaryBuilder.
										getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED));
						if (writerOptional != null){
								writerOptional.printNavDetailLink(annotationType.elements().length > 0);
						} else if (writerRequired != null){
								writerRequired.printNavDetailLink(annotationType.elements().length > 0);
						} else {
								printText("doclet.navAnnotationTypeMember");
						}*/
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
	public void writeNestedClassInfo() {
		/*ClassDoc outerClass = annotationType.containingClass();
						if (outerClass != null) {
								dl();
								dt();
								if (annotationType.isInterface()) {
										boldText("doclet.Enclosing_Interface");
								} else {
										boldText("doclet.Enclosing_Class");
								}
								dd();
								printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS, outerClass,
										false));
								ddEnd();
								dlEnd();
						}*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationTypeDoc getAnnotationTypeDoc() {
		return annotationType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void completeMemberSummaryBuild() {
		p();
	}
}






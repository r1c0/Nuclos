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
import com.sun.tools.doclets.internal.toolkit.ConstructorWriter;
import com.sun.tools.doclets.internal.toolkit.MemberSummaryWriter;
import java.io.IOException;
import java.util.List;

/**
 * Writes constructor documentation.
 *
 *
 *
 */
public class ConstructorWriterImpl extends AbstractExecutableMemberWriter
		implements ConstructorWriter, MemberSummaryWriter {

	/*private boolean foundNonPubConstructor = false;
			private boolean printedSummaryHeader = false;*/

	/**
	 * Construct a new ConstructorWriterImpl.
	 *
	 * @param writer The writer for the class that the constructors belong to.
	 * @param classDoc the class being documented.
	 */
	public ConstructorWriterImpl(SubWriterHolderWriter writer,
			ClassDoc classDoc) {
		super(writer, classDoc);
		/*VisibleMemberMap visibleMemberMap = new VisibleMemberMap(classDoc,
								VisibleMemberMap.CONSTRUCTORS, configuration().nodeprecated);
						List constructors = new ArrayList(visibleMemberMap.getMembersFor(classDoc));
						for (int i = 0; i < constructors.size(); i++) {
								if (((ProgramElementDoc)(constructors.get(i))).isProtected() ||
										((ProgramElementDoc)(constructors.get(i))).isPrivate()) {
										setFoundNonPubConstructor(true);
								}
						} */
	}

	/**
	 * Construct a new ConstructorWriterImpl.
	 *
	 * @param writer The writer for the class that the constructors belong to.
	 */
	public ConstructorWriterImpl(SubWriterHolderWriter writer) {
		super(writer);
	}

	/**
	 * Write the constructors summary header for the given class.
	 *
	 * @param classDoc the class the summary belongs to.
	 */
	@Override
	public void writeMemberSummaryHeader(ClassDoc classDoc) {
		/*printedSummaryHeader = true;
						writer.println();
						writer.println("<!-- ======== CONSTRUCTOR SUMMARY ======== -->");
						writer.println();
						writer.printSummaryHeader(this, classDoc);*/
	}

	/**
	 * Write the constructors summary footer for the given class.
	 *
	 * @param classDoc the class the summary belongs to.
	 */
	@Override
	public void writeMemberSummaryFooter(ClassDoc classDoc) {
		// writer.printSummaryFooter(this, classDoc);
	}

	/**
	 * Write the header for the constructor documentation.
	 *
	 * @param classDoc the class that the constructors belong to.
	 */
	@Override
	public void writeHeader(ClassDoc classDoc, String header) {
		/* writer.println();
						writer.println("<!-- ========= CONSTRUCTOR DETAIL ======== -->");
						writer.println();
						writer.anchor("constructor_detail");
						writer.printTableHeadingBackground(header);*/
	}

	/**
	 * Write the constructor header for the given constructor.
	 *
	 * @param constructor the constructor being documented.
	 * @param isFirst the flag to indicate whether or not the constructor is the
	 *        first to be documented.
	 */
	@Override
	public void writeConstructorHeader(ConstructorDoc constructor, boolean isFirst) {
		/* if ( isFirst) {
								print("");
						}
						writer.println();
						String erasureAnchor;
						if ((erasureAnchor = getErasureAnchor(constructor)) != null) {
								writer.anchor(erasureAnchor);
						}
						writer.anchor(constructor);
						writer.h3();
						writer.print(constructor.name());
						writer.h3End();*/
	}

	/**
	 * Write the signature for the given constructor.
	 *
	 * @param constructor the constructor being documented.
	 */
	@Override
	public void writeSignature(ConstructorDoc constructor) {
		/*writer.displayLength = 0;
					 writer.pre();
					 writer.writeAnnotationInfo(constructor);
						printModifiers(constructor);

						if (configuration().linksource) {
							 print("");
						} else {
							print("");
						}
						writeParameters(constructor);
						writeExceptions(constructor);
						writer.preEnd();
					 writer.dl();*/

	}

	/**
	 * Write the deprecated output for the given constructor.
	 *
	 * @param constructor the constructor being documented.
	 */
	@Override
	public void writeDeprecated(ConstructorDoc constructor) {
		/*String output = ((TagletOutputImpl)
								(new DeprecatedTaglet()).getTagletOutput(constructor,
								writer.getTagletWriterInstance(false))).toString();
						if (output != null && output.trim().length() > 0) {
								writer.print(output);
						}*/
	}

	/**
	 * Write the comments for the given constructor.
	 *
	 * @param constructor the constructor being documented.
	 */
	@Override
	public void writeComments(ConstructorDoc constructor) {
		/*if (constructor.inlineTags().length > 0) {
								writer.dd();
								writer.printInlineComment(constructor);
						}*/
	}

	/**
	 * Write the tag output for the given constructor.
	 *
	 * @param constructor the constructor being documented.
	 */
	@Override
	public void writeTags(ConstructorDoc constructor) {
		//writer.printTags(constructor);
	}

	/**
	 * Write the constructor footer.
	 */
	@Override
	public void writeConstructorFooter() {
		//writer.dlEnd();
	}

	/**
	 * Write the footer for the constructor documentation.
	 *
	 * @param classDoc the class that the constructors belong to.
	 */
	@Override
	public void writeFooter(ClassDoc classDoc) {
		//No footer to write for constructor documentation
	}

	/**
	 * Close the writer.
	 */
	@Override
	public void close() throws IOException {
		//writer.close();
	}

	/**
	 * Let the writer know whether a non public constructor was found.
	 *
	 * @param foundNonPubConstructor true if we found a non public constructor.
	 */
	@Override
	public void setFoundNonPubConstructor(boolean foundNonPubConstructor) {
		// this.foundNonPubConstructor = foundNonPubConstructor;
	}

	@Override
	public void printSummaryLabel(ClassDoc cd) {
		// writer.boldText("doclet.Constructor_Summary");
	}

	@Override
	public void printSummaryAnchor(ClassDoc cd) {
		//writer.anchor("constructor_summary");
	}

	@Override
	public void printInheritedSummaryAnchor(ClassDoc cd) {
	}	 // no such

	@Override
	public void printInheritedSummaryLabel(ClassDoc cd) {
		// no such
	}

	protected void navSummaryLink(List members) {
		/*printNavSummaryLink(classdoc,
										members.size() > 0? true: false);*/
	}

	@Override
	protected void printNavSummaryLink(ClassDoc cd, boolean link) {
		/* if (link) {
								writer.printHyperLink("", "constructor_summary",
												ConfigurationImpl.getInstance().getText("doclet.navConstructor"));
						} else {
								writer.printText("doclet.navConstructor");
						}*/
	}

	@Override
	protected void printNavDetailLink(boolean link) {
		/*if (link) {
								writer.printHyperLink("", "constructor_detail",
												ConfigurationImpl.getInstance().getText("doclet.navConstructor"));
						} else {
								writer.printText("doclet.navConstructor");
						}*/
	}

	@Override
	protected void printSummaryType(ProgramElementDoc member) {
		/*if (foundNonPubConstructor) {
								writer.printTypeSummaryHeader();
								if (member.isProtected()) {
										print("protected ");
								} else if (member.isPrivate()) {
										print("private ");
								} else if (member.isPublic()) {
										writer.space();
								} else {
										writer.printText("doclet.Package_private");
								}
								writer.printTypeSummaryFooter();
						}*/
	}

	/**
	 * Write the inherited member summary header for the given class.
	 *
	 * @param classDoc the class the summary belongs to.
	 */
	@Override
	public void writeInheritedMemberSummaryHeader(ClassDoc classDoc) {
		/*if(! printedSummaryHeader){
								//We don't want inherited summary to not be under heading.
								writeMemberSummaryHeader(classDoc);
								writeMemberSummaryFooter(classDoc);
								printedSummaryHeader = true;
						}*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeInheritedMemberSummary(ClassDoc classDoc,
			ProgramElementDoc member, boolean isFirst, boolean isLast) {
	}

	/**
	 * Write the inherited member summary footer for the given class.
	 *
	 * @param classDoc the class the summary belongs to.
	 */
	@Override
	public void writeInheritedMemberSummaryFooter(ClassDoc classDoc) {
	}
}

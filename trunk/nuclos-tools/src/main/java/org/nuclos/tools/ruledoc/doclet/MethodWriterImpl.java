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
import com.sun.tools.doclets.internal.toolkit.MemberSummaryWriter;
import com.sun.tools.doclets.internal.toolkit.MethodWriter;
import com.sun.tools.doclets.internal.toolkit.taglets.DeprecatedTaglet;
import com.sun.tools.doclets.internal.toolkit.util.Util;
import com.sun.tools.doclets.internal.toolkit.util.VisibleMemberMap;
import java.io.IOException;

/**
 * Writes method documentation in HTML format.
 *
 *
 */
public class MethodWriterImpl extends AbstractExecutableMemberWriter implements
		MethodWriter, MemberSummaryWriter {

	/**
	 * Construct a new MethodWriterImpl.
	 *
	 * @param writer
	 *            the writer for the class that the methods belong to.
	 * @param classDoc
	 *            the class being documented.
	 */
	public MethodWriterImpl(SubWriterHolderWriter writer, ClassDoc classDoc) {
		super(writer, classDoc);
	}

	/**
	 * Construct a new MethodWriterImpl.
	 *
	 * @param writer
	 *            The writer for the class that the methods belong to.
	 */
	public MethodWriterImpl(SubWriterHolderWriter writer) {
		super(writer);
	}

	/**
	 * Write the methods summary header for the given class.
	 *
	 * @param classDoc
	 *            the class the summary belongs to.
	 */
	@Override
	public void writeMemberSummaryHeader(ClassDoc classDoc) {

	}

	/**
	 * Write the methods summary footer for the given class.
	 *
	 * @param classDoc
	 *            the class the summary belongs to.
	 */
	@Override
	public void writeMemberSummaryFooter(ClassDoc classDoc) {

	}

	/**
	 * Write the inherited methods summary header for the given class.
	 *
	 * @param classDoc
	 *            the class the summary belongs to.
	 */
	@Override
	public void writeInheritedMemberSummaryHeader(ClassDoc classDoc) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeInheritedMemberSummary(ClassDoc classDoc,
			ProgramElementDoc method, boolean isFirst, boolean isLast) {

	}

	/**
	 * Write the inherited methods summary footer for the given class.
	 *
	 * @param classDoc
	 *            the class the summary belongs to.
	 */
	@Override
	public void writeInheritedMemberSummaryFooter(ClassDoc classDoc) {

	}

	/**
	 * Write the header for the method documentation.
	 *
	 * @param classDoc
	 *            the class that the methods belong to.
	 */
	@Override
	public void writeHeader(ClassDoc classDoc, String header) {
		writer.println();
		writer.println("<!-- ============ METHOD DETAIL ========== -->");
		writer.println();
		writer.anchor("method_detail");
		writer.printTableHeadingBackground(header);
	}

	/**
	 * Write the method header for the given method.
	 *
	 * @param method
	 *            the method being documented.
	 * @param isFirst
	 *            the flag to indicate whether or not the method is the first to
	 *            be documented.
	 */
	@Override
	public void writeMethodHeader(MethodDoc method, boolean isFirst) {
		if (!isFirst) {
			writer.printMemberHeader();
		}
		writer.println();
		String erasureAnchor;
		if ((erasureAnchor = getErasureAnchor(method)) != null) {
			writer.anchor(erasureAnchor);
		}
		writer.anchor(method);
		writer.h3();
		writer.print(method.name());
		writer.h3End();
	}

	/**
	 * Write the signature for the given method.
	 *
	 * @param method
	 *            the method being documented.
	 */
	@Override
	public void writeSignature(MethodDoc method) {
		writer.displayLength = 0;
		writer.pre();
		writer.writeAnnotationInfo(method);
		printModifiers(method);
		writeTypeParameters(method);
		printReturnType(method);
		if (configuration().linksource) {
			writer.printSrcLink(method, method.name());
		}
		else {
			bold(method.name());
		}
		writeParameters(method);
		writeExceptions(method);
		writer.preEnd();
		writer.dl();
	}

	/**
	 * Write the deprecated output for the given method.
	 *
	 * @param method
	 *            the method being documented.
	 */
	@Override
	public void writeDeprecated(MethodDoc method) {
		String output = ((TagletOutputImpl) (new DeprecatedTaglet())
				.getTagletOutput(method, writer.getTagletWriterInstance(false)))
				.toString();
		if (output != null && output.trim().length() > 0) {
			writer.print(output);
		}
	}

	/**
	 * Write the comments for the given method.
	 *
	 * @param method
	 *            the method being documented.
	 */
	@Override
	public void writeComments(Type holder, MethodDoc method) {
		ClassDoc holderClassDoc = holder.asClassDoc();
		if (method.inlineTags().length > 0) {
			if (holder.asClassDoc().equals(classdoc)
					|| (!(holderClassDoc.isPublic() || Util.isLinkable(
					holderClassDoc, configuration())))) {
				writer.dd();
				writer.printInlineComment(method);
			}
			else {
				String classlink = writer.codeText(writer.getDocLink(
						LinkInfoImpl.CONTEXT_METHOD_DOC_COPY, holder
						.asClassDoc(), method, holder.asClassDoc()
						.isIncluded() ? holder.typeName() : holder
						.qualifiedTypeName(), false));
				writer.dd();
				writer
						.boldText(
								holder.asClassDoc().isClass() ? "doclet.Description_From_Class"
										: "doclet.Description_From_Interface",
								classlink);
				writer.ddEnd();
				writer.dd();
				writer.printInlineComment(method);
			}
		}
	}

	/**
	 * Write the tag output for the given method.
	 *
	 * @param method
	 *            the method being documented.
	 */
	@Override
	public void writeTags(MethodDoc method) {
		writer.printTags(method);
	}

	/**
	 * Write the method footer.
	 */
	@Override
	public void writeMethodFooter() {
		writer.ddEnd();
		writer.dlEnd();
	}

	/**
	 * Write the footer for the method documentation.
	 *
	 * @param classDoc
	 *            the class that the methods belong to.
	 */
	@Override
	public void writeFooter(ClassDoc classDoc) {
		// No footer to write for method documentation
	}

	/**
	 * Close the writer.
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}

	public int getMemberKind() {
		return VisibleMemberMap.METHODS;
	}

	@Override
	public void printSummaryLabel(ClassDoc cd) {
		writer.boldText("doclet.Method_Summary");
	}

	@Override
	public void printSummaryAnchor(ClassDoc cd) {
		writer.anchor("method_summary");
	}

	@Override
	public void printInheritedSummaryAnchor(ClassDoc cd) {

	}

	@Override
	public void printInheritedSummaryLabel(ClassDoc cd) {

	}

	@Override
	protected void printSummaryType(ProgramElementDoc member) {
		MethodDoc meth = (MethodDoc) member;
		printModifierAndType(meth, meth.returnType());
	}

	protected static void printOverridden(CommonDocletWriter writer,
			Type overriddenType, MethodDoc method) {

	}

	/**
	 * Parse the &lt;Code&gt; tag and return the text.
	 */
	protected String parseCodeTag(String tag) {
		if (tag == null) {
			return "";
		}

		String lc = tag.toLowerCase();
		int begin = lc.indexOf("<code>");
		int end = lc.indexOf("</code>");
		if (begin == -1 || end == -1 || end <= begin) {
			return tag;
		}
		else {
			return tag.substring(begin + 6, end);
		}
	}

	protected static void printImplementsInfo(CommonDocletWriter writer,
			MethodDoc method) {

	}

	protected void printReturnType(MethodDoc method) {
		Type type = method.returnType();
		if (type != null) {
			writer.printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_RETURN_TYPE,
					type));
			print(' ');
		}
	}

	@Override
	protected void printNavSummaryLink(ClassDoc cd, boolean link) {
		if (link) {
			writer
					.printHyperLink("", (cd == null) ? "method_summary"
							: "methods_inherited_from_class_"
							+ ConfigurationImpl.getInstance()
							.getClassName(cd),
							ConfigurationImpl.getInstance().getText(
									"doclet.navMethod"));
		}
		else {
			writer.printText("doclet.navMethod");
		}
	}

	@Override
	protected void printNavDetailLink(boolean link) {
		if (link) {
			writer.printHyperLink("", "method_detail", ConfigurationImpl
					.getInstance().getText("doclet.navMethod"));
		}
		else {
			writer.printText("doclet.navMethod");
		}
	}
}

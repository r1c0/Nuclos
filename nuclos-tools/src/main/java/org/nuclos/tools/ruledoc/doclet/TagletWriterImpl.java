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
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.builders.SerializedFormBuilder;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * The taglet writer that writes HTML.
 *
 * @since 1.5
 *
 */

public class TagletWriterImpl extends TagletWriter {

	private CommonDocletWriter htmlWriter;

	public TagletWriterImpl(CommonDocletWriter htmlWriter,
			boolean isFirstSentence) {
		this.htmlWriter = htmlWriter;
		this.isFirstSentence = isFirstSentence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput getOutputInstance() {
		return new TagletOutputImpl("");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput getDocRootOutput() {
		return new TagletOutputImpl(htmlWriter.relativepathNoSlash);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput deprecatedTagOutput(Doc doc) {
		StringBuffer output = new StringBuffer();
		Tag[] deprs = doc.tags("deprecated");
		if (doc instanceof ClassDoc) {
			if (Util.isDeprecated((ProgramElementDoc) doc)) {
				output.append("<B>"
						+ ConfigurationImpl.getInstance().getText(
						"doclet.Deprecated") + "</B>&nbsp;");
				if (deprs.length > 0) {
					Tag[] commentTags = deprs[0].inlineTags();
					if (commentTags.length > 0) {

						output.append(commentTagsToOutput(null, doc,
								deprs[0].inlineTags()).toString());
					}
				}
				output.append("<p>");
			}
		}
		else {
			MemberDoc member = (MemberDoc) doc;
			if (Util.isDeprecated((ProgramElementDoc) doc)) {
				output.append("<DD><B>"
						+ ConfigurationImpl.getInstance().getText(
						"doclet.Deprecated") + "</B>&nbsp;");
				if (deprs.length > 0) {
					output.append("<I>");
					output.append(commentTagsToOutput(null, doc,
							deprs[0].inlineTags()).toString());
					output.append("</I>");
				}
				if (member instanceof ExecutableMemberDoc) {
					output.append(DocletConstants.NL + "<P>"
							+ DocletConstants.NL);
				}
			}
			else {
				if (Util.isDeprecated(member.containingClass())) {
					output.append("<DD><B>"
							+ ConfigurationImpl.getInstance().getText(
							"doclet.Deprecated") + "</B>&nbsp;");
				}
			}
		}
		return new TagletOutputImpl(output.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageRetriever getMsgRetriever() {
		return htmlWriter.configuration.message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput getParamHeader(String header) {
		StringBuffer result = new StringBuffer();
		result.append("<DT>");
		result.append("<B>" + header + "</B>");
		return new TagletOutputImpl(result.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput paramTagOutput(ParamTag paramTag, String paramName) {
		TagletOutput result = new TagletOutputImpl("<DD><CODE>"
				+ paramName
				+ "</CODE>"
				+ " - "
				+ htmlWriter.commentTagsToString(paramTag, null, paramTag
				.inlineTags(), false));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput returnTagOutput(Tag returnTag) {
		TagletOutput result = new TagletOutputImpl(DocletConstants.NL
				+ "<DT>"
				+ "<B>"
				+ htmlWriter.configuration.getText("doclet.Returns")
				+ "</B>"
				+ "<DD>"
				+ htmlWriter.commentTagsToString(returnTag, null, returnTag
				.inlineTags(), false));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput seeTagOutput(Doc holder, SeeTag[] seeTags) {
		String result = "";
		if (seeTags.length > 0) {
			result = addSeeHeader(result);
			for (int i = 0; i < seeTags.length; ++i) {
				if (i > 0) {
					result += ", " + DocletConstants.NL;
				}
				result += htmlWriter.seeTagToString(seeTags[i]);
			}
		}
		if (holder.isField() && ((FieldDoc) holder).constantValue() != null
				&& htmlWriter instanceof ClassWriterImpl) {
			// Automatically add link to constant values page for constant
			// fields.
			result = addSeeHeader(result);
			result += htmlWriter.getHyperLink(
					htmlWriter.relativePath
							+ ConfigurationImpl.CONSTANTS_FILE_NAME
							+ "#"
							+ ((ClassWriterImpl) htmlWriter).getClassDoc()
							.qualifiedName() + "."
							+ ((FieldDoc) holder).name(),
					htmlWriter.configuration
							.getText("doclet.Constants_Summary"));
		}
		if (holder.isClass() && ((ClassDoc) holder).isSerializable()) {
			// Automatically add link to serialized form page for serializable
			// classes.
			if (!(SerializedFormBuilder.serialInclude(holder) && SerializedFormBuilder
					.serialInclude(((ClassDoc) holder).containingPackage()))) {
				return result.equals("") ? null : new TagletOutputImpl(result);
			}
			result = addSeeHeader(result);
			result += htmlWriter.getHyperLink(htmlWriter.relativePath
					+ "serialized-form.html", ((ClassDoc) holder)
					.qualifiedName(), htmlWriter.configuration
					.getText("doclet.Serialized_Form"), false);
		}
		return result.equals("") ? null : new TagletOutputImpl(result);
	}

	private String addSeeHeader(String result) {
		if (result != null && result.length() > 0) {
			return result + ", " + DocletConstants.NL;
		}
		else {
			return "<DT><B>"
					+ htmlWriter.configuration().getText("doclet.See_Also")
					+ "</B><DD>";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput simpleTagOutput(Tag[] simpleTags, String header) {
		String result = "<DT><B>" + header + "</B></DT>" + DocletConstants.NL
				+ "  <DD>";
		for (int i = 0; i < simpleTags.length; i++) {
			if (i > 0) {
				result += ", ";
			}
			result += htmlWriter.commentTagsToString(simpleTags[i], null,
					simpleTags[i].inlineTags(), false);
		}
		return new TagletOutputImpl(result + "</DD>" + DocletConstants.NL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput simpleTagOutput(Tag simpleTag, String header) {
		return new TagletOutputImpl("<DT><B>"
				+ header
				+ "</B></DT>"
				+ "  <DD>"
				+ htmlWriter.commentTagsToString(simpleTag, null, simpleTag
				.inlineTags(), false) + "</DD>" + DocletConstants.NL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput getThrowsHeader() {
		return new TagletOutputImpl("");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput throwsTagOutput(ThrowsTag throwsTag) {

		return new TagletOutputImpl("");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput throwsTagOutput(Type throwsType) {
		return new TagletOutputImpl("");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput valueTagOutput(FieldDoc field, String constantVal,
			boolean includeLink) {
		return new TagletOutputImpl(includeLink ? htmlWriter.getDocLink(
				LinkInfoImpl.CONTEXT_VALUE_TAG, field, constantVal, false)
				: constantVal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput commentTagsToOutput(Tag holderTag, Tag[] tags) {
		return commentTagsToOutput(holderTag, null, tags);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput commentTagsToOutput(Doc holderDoc, Tag[] tags) {
		return commentTagsToOutput(null, holderDoc, tags);
	}

	/**
	 * {@inheritDoc}
	 */
	public TagletOutput commentTagsToOutput(Tag holderTag, Doc holderDoc,
			Tag[] tags) {
		return new TagletOutputImpl(htmlWriter.commentTagsToString(holderTag,
				holderDoc, tags, false));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagletOutput commentTagsToOutput(Tag holderTag, Doc holderDoc, Tag[] tags,
			boolean isFirstSentence) {
		return commentTagsToOutput(holderTag, holderDoc, tags);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Configuration configuration() {
		return htmlWriter.configuration();
	}

	/**
	 * Return an instance of a TagletWriter that knows how to write HTML.
	 *
	 * @return an instance of a TagletWriter that knows how to write HTML.
	 */
	@Override
	public TagletOutput getTagletOutputInstance() {
		return new TagletOutputImpl("");
	}
}

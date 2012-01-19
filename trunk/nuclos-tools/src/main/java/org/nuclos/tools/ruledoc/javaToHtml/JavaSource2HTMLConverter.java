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

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Algorithm and stuff for converting a JavaSource object to to a HTML string
 * representation.
 *
 * The result is XHTML1.0 Transitional compliant.
 *
 *
 */
public class JavaSource2HTMLConverter extends JavaSourceConverter {
	/**
	 * Flag indication whether html output contains a link to the
	 * Java2Html-Homepage or not.
	 *
	 *
	 */
	public static boolean java2HtmlHomepageLinkEnabled = false;

	/**
	 * Site header for a html page. Is not used by this class, but can be used
	 * from outside to add it to one or more converted
	 */
	private final static String HTML_SITE_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
			// "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"
			// \"http://www.w3.org/TR/html4/loose.dtd\">\n"
			+ "<html><head>\n"
			+ "<title></title>\n"
			+ "  <style type=\"text/css\">\n"
			+ "    <!--code { font-family: Courier New, Courier; font-size: 10pt; margin: 0px; }-->\n"
			+ "  </style>\n"
			+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>\n"
			+ "</head><body>\n";

	/**
	 * Site footer for a html page. Is not used by this class, but can be used
	 * from outside to add it to one or more converted
	 */
	private final static String HTML_SITE_FOOTER = "\n</body></html>";

	/** Block seperator for between two blocks of converted source code */
	private final static String HTML_BLOCK_SEPARATOR = "<p/>\n";

	/** HTML-Header for a block (!) of converted code */
	private final static String HTML_BLOCK_HEADER = "\n\n"
			+ "<!-- ======================================================== -->\n"
			+ "<!-- = Java Sourcecode to HTML automatically converted code = -->\n"

			+ "<div align=\"{0}\" class=\"java\">\n"
			+ "<table border=\"{1}\" cellpadding=\"3\" "
			+ "cellspacing=\"0\" bgcolor=\"{2}\">\n";

	/** HTML-code for before the headline */
	private final static String HTML_HEAD_START = "  <!-- start headline -->\n"
			+ "   <tr>\n" + "    <td colspan=\"2\">\n"
			+ "     <center><font size=\"+2\">\n" + "      <code><b>\n";

	/** HTML-code for after the headline */
	private final static String HTML_HEAD_END = "      </b></code>\n"
			+ "     </font></center>\n" + "    </td>\n" + "   </tr>\n"
			+ "  <!-- end headline -->\n";

	/**
	 * HTML-code for before the second column (contaning the converted source
	 * code)
	 */
	private final static String HTML_COL2_START = "  <!-- start source code -->\n"
			+ "   <td nowrap=\"nowrap\" valign=\"top\" align=\"left\">\n"
			+ "    <code>\n";

	/**
	 * HTML-code for after the second column (contaning the converted source
	 * code)
	 */
	private final static String HTML_COL2_END = "</code>\n" + "    \n"
			+ "   </td>\n" + "  <!-- end source code -->\n";

	private final static String HTML_LINK = "  <!-- start Java2Html link -->\n"
			+ "   <tr>\n" + "    <td align=\"right\">\n" + "<small>\n"

			+ "</small>\n" + "    </td>\n" + "   </tr>\n"
			+ "  <!-- end Java2Html link -->\n";

	/** HTML-code for after the end of the block */
	private final static String HTML_BLOCK_FOOTER =

			"</table>\n"
					+ "</div>\n"
					+ "<!-- =       END of automatically generated HTML code       = -->\n"
					+ "<!-- ======================================================== -->\n\n";

	/**
	 * The html representation of the colors used for different source
	 */

	private int lineCifferCount;

	public JavaSource2HTMLConverter() {
		super();
	}

	public JavaSource2HTMLConverter(JavaSource source) {
		super(source);
	}

	@Override
	public String getDocumentHeader() {
		return HTML_SITE_HEADER;
	}

	@Override
	public String getDocumentFooter() {
		return HTML_SITE_FOOTER;
	}

	@Override
	public String getBlockSeparator() {
		return HTML_BLOCK_SEPARATOR;
	}

	@Override
	public void convert(BufferedWriter writer) throws IOException {
		if (source == null) {
			throw new IllegalStateException(
					"Trying to write out converted code without having source set.");
		}

		// Header
		String alignValue = getHtmlAlignValue(getConversionOptions()
				.getHorizontalAlignment());
		String bgcolorValue = getConversionOptions().getStyleTable().get(
				JavaSourceType.BACKGROUND.getName()).getHtmlColor();
		String borderValue = getConversionOptions().isShowTableBorder() ? "2"
				: "0";

		writer.write(MessageFormat.format(HTML_BLOCK_HEADER, new Object[] {
				alignValue, borderValue, bgcolorValue}));

		if (getConversionOptions().isShowFileName()
				&& source.getFileName() != null) {
			writeFileName(writer);
		}

		writer.write("   <tr>");
		writer.newLine();

		writeSourceCode(writer);

		writer.write("   </tr>");
		writer.newLine();

		// 5) Footer with link to web site
		if (getConversionOptions().isShowJava2HtmlLink()
				|| java2HtmlHomepageLinkEnabled) {
			writer.write(HTML_LINK);
		}
		writer.write(HTML_BLOCK_FOOTER);
	}

	private String getHtmlAlignValue(HorizontalAlignment alignment) {
		final StringHolder stringHolder = new StringHolder();
		alignment.accept(new IHorizontalAlignmentVisitor() {
			@Override
			public void visitLeftAlignment(
					HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("left");
			}

			@Override
			public void visitRightAlignment(
					HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("right");
			}

			@Override
			public void visitCenterAlignment(
					HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("center");
			}
		});
		return stringHolder.getValue();
	}

	private void writeFileName(BufferedWriter writer) throws IOException {
		writer.write(HTML_HEAD_START);
		writer.write(source.getFileName());
		writer.newLine();
		writer.write(HTML_HEAD_END);
	}

	private void writeSourceCode(BufferedWriter writer) throws IOException {
		writer.write(HTML_COL2_START);

		lineCifferCount = String.valueOf(source.getLineCount()).length();

		JavaSourceIterator iterator = source.getIterator();
		int lineNumber = 1;
		while (iterator.hasNext()) {
			JavaSourceRun run = iterator.next();

			if (run.isAtStartOfLine()) {
				if (getConversionOptions().isAddLineAnchors()) {
					writeLineAnchorStart(writer, lineNumber);
				}
				if (getConversionOptions().isShowLineNumbers()) {
					writeLineNumber(writer, lineNumber);
				}
				if (getConversionOptions().isAddLineAnchors()) {
					writeLineAnchorEnd(writer);
				}
				lineNumber++;
			}

			toHTML(run, writer);
			if (run.isAtEndOfLine() && iterator.hasNext()) {
				writer.write("<br/>");
				writer.newLine();
			}
		}
		writer.write(HTML_COL2_END);
	}

	private void writeLineAnchorEnd(BufferedWriter writer) throws IOException {
		writer.write("</a>");
	}

	private void writeLineAnchorStart(BufferedWriter writer, int lineNumber)
			throws IOException {
		writer.write("<a name=\"");
		writer.write(getConversionOptions().getLineAnchorPrefix() + lineNumber);
		writer.write("\">");
	}

	private void writeLineNumber(BufferedWriter writer, int lineNo)
			throws IOException {
		JavaSourceStyleEntry styleEntry = getConversionOptions()
				.getStyleTable().get(JavaSourceType.LINE_NUMBERS);
		writeStyleStart(writer, styleEntry);

		String lineNumber = String.valueOf(lineNo);
		int cifferCount = lineCifferCount - lineNumber.length();
		while (cifferCount > 0) {
			writer.write('0');
			--cifferCount;
		}

		writer.write(lineNumber);
		writeStyleEnd(writer, styleEntry);
		writer.write("&nbsp;");
	}

	protected void toHTML(JavaSourceRun run, BufferedWriter writer)
			throws IOException {
		// result.append(htmlColors[sourceTypes[start]]);
		JavaSourceStyleEntry style = getColorTable().get(
				run.getType().getName());

		writeStyleStart(writer, style);

		String t = HTMLTools.encode(run.getCode(), "\n ");

		for (int i = 0; i < t.length(); ++i) {
			char ch = t.charAt(i);
			if (ch == ' ') {
				writer.write("&nbsp;");
			}
			else {
				writer.write(ch);
			}
		}

		writeStyleEnd(writer, style);
	}

	private void writeStyleStart(BufferedWriter writer,
			JavaSourceStyleEntry style) throws IOException {
		writer.write("<font color=\"" + style.getHtmlColor() + "\">");
		if (style.isBold()) {
			writer.write("<b>");
		}
		if (style.isItalic()) {
			writer.write("<i>");
		}
	}

	private void writeStyleEnd(BufferedWriter writer, JavaSourceStyleEntry style)
			throws IOException {
		if (style.isItalic()) {
			writer.write("</i>");
		}
		if (style.isBold()) {
			writer.write("</b>");
		}
		writer.write("</font>");
	}

	@Override
	public String getDefaultFileExtension() {
		return "html";
	}
}

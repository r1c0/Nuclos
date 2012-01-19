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

/**
 * Simple statistics information that can be created when parsing a java source code using the
 * JavaSourceParser.
 *
 */
public class JavaSourceStatistic {
	private int commentLineCount = -1;
	private int lineCount = -1;
	private int codeLineCount = -1;
	private int emptyLineCount = -1;
	private int maxLineLength = -1;
	private int characterCount = -1;
	private String packageName = null;
	private String fileName = null;

	public JavaSourceStatistic() {
		//nothing to do
	}

	/**
	 * Returns the codeLineCount.
	 * @return int
	 */
	public int getCodeLineCount() {
		return codeLineCount;
	}

	/**
	 * Returns the commentLineCount.
	 * @return int
	 */
	public int getCommentLineCount() {
		return commentLineCount;
	}

	/**
	 * Returns the emptyLineCount.
	 * @return int
	 */
	public int getEmptyLineCount() {
		return emptyLineCount;
	}

	/**
	 * Returns the lineCount.
	 * @return int
	 */
	public int getLineCount() {
		return lineCount;
	}

	/**
	 * Returns the maxLineLength.
	 * @return int
	 */
	public int getMaxLineLength() {
		return maxLineLength;
	}

	/**
	 * Sets the codeLineCount.
	 * @param codeLineCount The codeLineCount to set
	 */
	public void setCodeLineCount(int codeLineCount) {
		this.codeLineCount = codeLineCount;
	}

	/**
	 * Sets the commentLineCount.
	 * @param commentLineCount The commentLineCount to set
	 */
	public void setCommentLineCount(int commentLineCount) {
		this.commentLineCount = commentLineCount;
	}

	/**
	 * Sets the emptyLineCount.
	 * @param emptyLineCount The emptyLineCount to set
	 */
	public void setEmptyLineCount(int emptyLineCount) {
		this.emptyLineCount = emptyLineCount;
	}

	/**
	 * Sets the lineCount.
	 * @param lineCount The lineCount to set
	 */
	public void setLineCount(int lineCount) {
		this.lineCount = lineCount;
	}

	/**
	 * Sets the maxLineLength.
	 * @param maxLineLength The maxLineLength to set
	 */
	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	/**
	 * Returns the fileName.
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the fileName.
	 * @param fileName The fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void clear() {
		maxLineLength = 0;
		lineCount = 0;
		commentLineCount = 0;
		codeLineCount = 0;
		emptyLineCount = 0;
	}

	public String getScreenString() {
		StringBuffer result = new StringBuffer();
		result.append(" Package: " + toString(packageName) + "  Filename: " + toString(fileName) + "\n");
		result.append("   Lines total: " + lineCount + "  Code: " + codeLineCount
				+ "  Comments: " + commentLineCount + "  Empty: " + emptyLineCount + "\n");
		result.append("   " + characterCount + " Characters,  Maximum line length: "
				+ maxLineLength);

		return result.toString();
	}

	private String toString(Object value) {
		return value == null ? "" : value.toString();
	}

	public static String getExcelHeader() {
		StringBuffer result = new StringBuffer();
		result.append("package");
		result.append("\t");
		result.append("file");
		result.append("\t");
		result.append("lines total");
		result.append("\t");
		result.append("code lines");
		result.append("\t");
		result.append("comment lines");
		result.append("\t");
		result.append("empty lines");
		result.append("\t");
		result.append("characters total");
		result.append("\t");
		result.append("maximum line length");
		return result.toString();
	}

	public String getExcelString() {
		StringBuffer result = new StringBuffer();
		result.append(packageName);
		result.append("\t");
		result.append(fileName);
		result.append("\t");
		result.append(String.valueOf(lineCount));
		result.append("\t");
		result.append(String.valueOf(codeLineCount));
		result.append("\t");
		result.append(String.valueOf(commentLineCount));
		result.append("\t");
		result.append(String.valueOf(emptyLineCount));
		result.append("\t");
		result.append(String.valueOf(characterCount));
		result.append("\t");
		result.append(String.valueOf(maxLineLength));
		return result.toString();
	}

	/**
	 * Returns the characterCount.
	 * @return int
	 */
	public int getCharacterCount() {
		return characterCount;
	}

	/**
	 * Sets the characterCount.
	 * @param characterCount The characterCount to set
	 */
	public void setCharacterCount(int characterCount) {
		this.characterCount = characterCount;
	}

	/**
	 * Returns the packageName.
	 * @return String
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Sets the packageName.
	 * @param packageName The packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}

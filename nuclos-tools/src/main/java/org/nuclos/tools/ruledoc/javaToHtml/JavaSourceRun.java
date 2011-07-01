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

/** A connected piece of Java source code having the same type
 *
 * JavaSourceRun objects are created by JavaSourceIterator provided
 * from a JavaSource object.
 */
public class JavaSourceRun {
	private JavaSource javaSource;
	private int startIndex;
	private int endIndex;

	public JavaSourceRun(
			JavaSource javaSource,
			int startIndex,
			int endIndex) {
		this.javaSource = javaSource;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public boolean isAtEndOfLine() {
		return endIndex == javaSource.getCode().length() || javaSource.getCode().charAt(endIndex) == '\r';
	}

	public boolean isAtStartOfLine() {
		return (startIndex == 0 || javaSource.getCode().charAt(startIndex - 1) == '\n');
	}

	public JavaSource getJavaSource() {
		return javaSource;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public JavaSourceType getType() {
		return javaSource.getClassification()[startIndex];
	}

	public String getCode() {
		return javaSource.getCode().substring(startIndex, endIndex);
	}

	public void dump() {
		System.out.print(isAtStartOfLine() ? "[" : "(");
		System.out.print(startIndex + ".." + endIndex);
		System.out.print(isAtEndOfLine() ? "]" : ")");
		System.out.println(" '" + getCode() + "'");
	}
}

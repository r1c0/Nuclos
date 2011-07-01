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

public class JavaSource {
	/** The source code as raw text */
	private String source;

	/** Flags for every character in the source code telling
	 the type. */
	private JavaSourceType[] types;

	private JavaSourceStatistic statistic;

	public JavaSource(String source) {
		this.source = source;
		statistic = new JavaSourceStatistic();
	}

	public JavaSourceType[] getClassification() {
		return types;
	}

	public void setClassification(JavaSourceType[] types) {
		this.types = types;
	}

	public String getCode() {
		return source;
	}

	/**
	 * Debug output of the code
	 */
	public void print() {
		System.out.println("------------------------------");
		int start = 0;
		int end = 0;

		while (start < types.length) {
			while (end < types.length - 1 && types[end + 1] == types[start]) {
				++end;
			}

			print(start, end);

			start = end + 1;
			end = start;
		}
	}

	protected void print(int start, int end) {
		System.out.print(types[start] + ": ");
		System.out.println("@" + source.substring(start, end + 1).replace('\n', '#') + "@");
	}

	/**
	 * Returns statistical information as String
	 */
	public String getStatisticsString() {
		/* output format (example):
				 Lines total: 127  Code: 57  Comments: 16  Empty: 54
				 3164 Characters, maximum line length: 95                */
		return statistic.getScreenString();
	}

	public String getFileName() {
		return getStatistic().getFileName();
	}

	public void setFileName(String fileName) {
		getStatistic().setFileName(fileName);
	}

	public int getLineCount() {
		return statistic.getLineCount();
	}

	public int getMaxLineLength() {
		return statistic.getMaxLineLength();
	}

	public JavaSourceStatistic getStatistic() {
		return statistic;
	}

	public JavaSourceIterator getIterator() {
		return new JavaSourceIterator(this);
	}
}

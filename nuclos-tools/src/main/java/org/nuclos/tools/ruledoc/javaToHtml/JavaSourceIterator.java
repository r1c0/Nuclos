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
/**
 * An object for iterating a {@link JavaSource} object by getting connected pieces of Java source
 * code having the same type. Line breaks are omitted, but empty lines will be covered.
 *
 * @see JavaSourceRun
 */

package org.nuclos.tools.ruledoc.javaToHtml;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class JavaSourceIterator implements Iterator<JavaSourceRun> {
	private int startIndex;
	private int endIndex;
	private JavaSource javaSource;
	private boolean finished;
	private boolean isNewLine;

	public JavaSourceIterator(JavaSource javaSource) {
		this.javaSource = javaSource;
		finished = false;
		isNewLine = false;
		startIndex = 0;
		endIndex = 0;
		if (javaSource.getCode().length() == 0) {
			finished = true;
		}
	}

	private void seekToNext() {
		if (isNewLine) {
			startIndex = endIndex + 2;
			endIndex = startIndex + 1;
			isNewLine = false;
		}
		else {
			startIndex = endIndex;
			endIndex = startIndex + 1;
		}

		if (endIndex > javaSource.getCode().length()) {
			--endIndex;
		}

//    System.out.println(startIndex+".."+endIndex);

		while (true) {
			if (endIndex == javaSource.getCode().length()) {
//System.out.println("1");
				break;
			}
			if (endIndex <= javaSource.getCode().length() - 1 && javaSource.getCode().charAt(endIndex) == '\n') {
				--endIndex;

				isNewLine = true;
//System.out.println("2");
				break;
			}
			if (javaSource.getClassification()[endIndex] != javaSource.getClassification()[startIndex] &&
					javaSource.getClassification()[endIndex] != JavaSourceType.BACKGROUND) {
//System.out.println("3");
				break;
			}
//System.out.println("+");

			++endIndex;
		}
//    System.out.println("=>"+startIndex+".."+endIndex);
	}

	@Override
	public boolean hasNext() {
		return !finished;
	}

	@Override
	public JavaSourceRun next() throws NoSuchElementException {
		if (finished) {
			throw new NoSuchElementException();
		}
		seekToNext();

		//Sonderfall: Hinter abschliessendem Newline in letzer Zeile ("\r\n")
		if (startIndex >= javaSource.getCode().length()) {
			--startIndex;
			--endIndex;
		}
		JavaSourceRun run = new JavaSourceRun(javaSource, startIndex, endIndex);
		finished = endIndex == javaSource.getCode().length();
		return run;
	}

	@Override
	public void remove() {
		//nothing to do
	}
}

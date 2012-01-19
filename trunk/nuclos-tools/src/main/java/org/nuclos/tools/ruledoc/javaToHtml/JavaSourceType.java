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
 * Different types of source code for classifying characters in the raw text.
 *
 */
public class JavaSourceType {
	private String name;
	private int id;
	private boolean displayRelevant;

	public final static JavaSourceType UNDEFINED =
			new JavaSourceType("Undefined", false, 15);

	public final static JavaSourceType CODE =
			new JavaSourceType("Others", true, 10);

	public final static JavaSourceType KEYWORD =
			new JavaSourceType("Keywords", true, 4);

	public final static JavaSourceType CODE_TYPE =
			new JavaSourceType("Primitive Types", true, 9);

	public final static JavaSourceType STRING =
			new JavaSourceType("Strings", true, 5); //darker red

	public final static JavaSourceType COMMENT_LINE =
			new JavaSourceType("Single-line comments", true, 3); //green

	public final static JavaSourceType COMMENT_BLOCK =
			new JavaSourceType("Multi-line comments", true, 2); //green

	public final static JavaSourceType JAVADOC =
			new JavaSourceType("Javadoc others", true, 14); //green

	public final static JavaSourceType JAVADOC_KEYWORD =
			new JavaSourceType("Javadoc keywords", true, 11);	//dark green

	public final static JavaSourceType BACKGROUND =
			new JavaSourceType("Background", false, 0);

	public final static JavaSourceType NUM_CONSTANT =
			new JavaSourceType("Numeric constants", true, 7); //dark red

	public final static JavaSourceType CHAR_CONSTANT =
			new JavaSourceType("Character constants", true, 6); //dark red

	public final static JavaSourceType PARENTHESIS =
			new JavaSourceType("Parenthesis", true, 8);

	public final static JavaSourceType JAVADOC_HTML_TAG =
			new JavaSourceType("Javadoc HTML tags", true, 12);

	public final static JavaSourceType JAVADOC_LINKS =
			new JavaSourceType("Javadoc links", true, 13);

	//Not really a Javasource type, but useful for conversion output options
	public final static JavaSourceType LINE_NUMBERS =
			new JavaSourceType("Line numbers", true, 1);

	private final static JavaSourceType[] ALL_TYPES = {
			BACKGROUND,			 //00
			LINE_NUMBERS,		 //01
			COMMENT_BLOCK,		//02
			COMMENT_LINE,		 //03
			KEYWORD,					//04
			STRING,					 //05
			CHAR_CONSTANT,		//06
			NUM_CONSTANT,		 //07
			PARENTHESIS,			//08
			CODE_TYPE,				//09
			CODE,						 //10
			JAVADOC_KEYWORD,	//11
			JAVADOC_HTML_TAG, //12
			JAVADOC_LINKS,		//13
			JAVADOC,					//14
			UNDEFINED,				//15
	};

	/**
	 * @param name The name of the type
	 * @param displayRelevant false if this type does not really matter for
	 * @param id a unique id
	 * display (e.g. because type means empty or illegal code).
	 */
	private JavaSourceType(String name, boolean displayRelevant, int id) {
		this.id = id;
		this.name = name;
		this.displayRelevant = displayRelevant;
	}

	public static JavaSourceType[] getAll() {
		return ALL_TYPES;
	}

	public String getName() {
		return toString();
	}

	public int getID() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isDisplayRelevant() {
		return displayRelevant;
	}
}

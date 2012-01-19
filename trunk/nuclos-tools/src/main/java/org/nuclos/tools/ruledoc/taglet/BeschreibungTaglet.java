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
package org.nuclos.tools.ruledoc.taglet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import java.util.Map;

public class BeschreibungTaglet implements Taglet {
	public static final String NAME = "beschreibung";

	protected ClassDoc classdoc;

	protected static String METHOD_NAME = "rule";

	/** Register an instance of this taglet. */
	public static void register(Map<String, BeschreibungTaglet> tagletMap) {
		tagletMap.put(NAME, new BeschreibungTaglet());
	}

	/** Return the tag name. */
	@Override
	public String getName() {
		return NAME;
	}

	/** Return false -- not an inline tag. */
	@Override
	public boolean isInlineTag() {
		return false;
	}

	/** Return the formatted tag. */
	@Override
	public String toString(Tag tag) {
		return "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"white\">" + tag.text() + "</td></tr></table></DD>\n";
	}

	/** Return the formatted list of tags. */
	@Override
	public String toString(Tag[] tags) {
		String text = "<dt><b>Beschreibung" + (tags.length > 1 ? "" : "")
				+ ":</b><dd>";

		for (int i = 0; i < tags.length; i++) {
			text += (i == 0 ? "" : ", ") + toString(tags[i]);
		}

		return text;
	}

	/** Return true -- can be used in field comments. */
	@Override
	public boolean inField() {
		return false;
	}

	/** Return true -- can be used in constructor comments. */
	@Override
	public boolean inConstructor() {
		return false;
	}

	/** Return true -- can be used in method comments. */
	@Override
	public boolean inMethod() {
		return true;

	}

	/** Return true -- can be used in overview comments. */
	@Override
	public boolean inOverview() {
		return false;
	}

	/** Return true -- can be used in package comments. */
	@Override
	public boolean inPackage() {
		return false;
	}

	/** Return true -- can be used in class comments. */
	@Override
	public boolean inType() {
		return false;
	}
}

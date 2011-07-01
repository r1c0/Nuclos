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

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import java.util.Map;

public class NameTaglet implements Taglet {

	private static final String NAME = "name";
	private static final String HEADER = "Name:";

	/**
	 * Return the name of this custom tag.
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in field documentation.
	 * @return true since <code>@todo</code>
	 * can be used in field documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inField() {
		return true;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in constructor documentation.
	 * @return true since <code>@todo</code>
	 * can be used in constructor documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inConstructor() {
		return true;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in method documentation.
	 * @return true since <code>@todo</code>
	 * can be used in method documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inMethod() {
		return true;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in method documentation.
	 * @return true since <code>@todo</code>
	 * can be used in overview documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inOverview() {
		return true;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in package documentation.
	 * @return true since <code>@todo</code>
	 * can be used in package documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inPackage() {
		return true;
	}

	/**
	 * Will return true since <code>@todo</code>
	 * can be used in type documentation (classes or interfaces).
	 * @return true since <code>@todo</code>
	 * can be used in type documentation and false
	 * otherwise.
	 */
	@Override
	public boolean inType() {
		return true;
	}

	/**
	 * Will return false since <code>@todo</code>
	 * is not an inline tag.
	 * @return false since <code>@todo</code>
	 * is not an inline tag.
	 */

	@Override
	public boolean isInlineTag() {
		return false;
	}

	/**
	 * Register this Taglet.
	 * @param tagletMap	the map to register this tag to.
	 */
	public static void register(Map<String, NameTaglet> tagletMap) {
		NameTaglet tag = new NameTaglet();
		Taglet t = tagletMap.get(tag.getName());
		if (t != null) {
			tagletMap.remove(tag.getName());
		}
		tagletMap.put(tag.getName(), tag);
	}

	/**
	 * Given the <code>Tag</code> representation of this custom
	 * tag, return its string representation.
	 * @param tag	 the <code>Tag</code> representation of this custom tag.
	 */
	@Override
	public String toString(Tag tag) {
		return "<DT><B>" + HEADER + "</B><DD>"
				+ "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"white\">"
				+ tag.text()
				+ "</td></tr></table></DD>\n";
	}

	/**
	 * Given an array of <code>Tag</code>s representing this custom
	 * tag, return its string representation.
	 * @param tags	the array of <code>Tag</code>s representing of this custom tag.
	 */
	@Override
	public String toString(Tag[] tags) {
		if (tags.length == 0) {
			return null;
		}
		String result = "\n<DT><B>" + HEADER + "</B><DD>";
		result += "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"white\">";
		for (int i = 0; i < tags.length; i++) {
			if (i > 0) {
				result += ", ";
			}
			result += tags[i].text();
		}
		return result + "</td></tr></table></DD>\n";
	}
}



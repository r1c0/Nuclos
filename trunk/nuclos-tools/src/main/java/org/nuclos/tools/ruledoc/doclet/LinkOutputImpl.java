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

import com.sun.tools.doclets.internal.toolkit.util.links.LinkOutput;

/**
 * Stores output of a link.
 *

 * @since 1.5
 */
public class LinkOutputImpl implements LinkOutput {

	/**
	 * The output of the link.
	 */
	public StringBuffer output;

	/**
	 * Construct a new LinkOutputImpl.
	 */
	public LinkOutputImpl() {
		output = new StringBuffer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(Object o) {
		output.append(o instanceof String ?
				(String) o : ((LinkOutputImpl) o).toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return output.toString();
	}

}


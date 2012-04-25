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
package org.nuclos.server.navigation.treenode.nuclet.content;

import org.nuclos.common.dal.vo.EntityObjectVO;

/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 00.01.000
 */
public class NucletContentProcessTreeNode extends DefaultNucletContentEntryTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5590014545461543307L;

	public NucletContentProcessTreeNode(EntityObjectVO eo) {
		super(eo);
	}
	
	@Override
	public String getName() {
		return String.format("%s (%s)", eo.getField("name", String.class), eo.getField("module", String.class));
	}

}

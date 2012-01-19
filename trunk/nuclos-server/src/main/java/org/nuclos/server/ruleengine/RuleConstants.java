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
package org.nuclos.server.ruleengine;

import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;

/**
 * Constants that can be used in rule development.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public interface RuleConstants {

	@Deprecated
	public static final Integer MODULE_ORDER = ModuleConstants.MODULEID_ORDER;

	/* constants used for attribute check */
	public static final int NULL = 0;
	public static final int NOT_NULL = 1;
	public static final int EQUAL = 2;
	public static final int NOT_EQUAL = 3;
	public static final int GREATER = 4;
	public static final int LESS = 5;
	public static final int GREATER_OR_EQUAL = 6;
	public static final int LESS_OR_EQUAL = 7;

	public static final String RELATIONTYPE_PREDECESSOR_OF = GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue();
	public static final String RELATIONTYPE_PART_OF = GenericObjectTreeNode.SystemRelationType.PART_OF.getValue();
}

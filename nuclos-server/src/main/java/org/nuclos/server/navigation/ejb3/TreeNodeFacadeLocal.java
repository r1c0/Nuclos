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
package org.nuclos.server.navigation.ejb3;

import java.util.List;

import javax.ejb.Local;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;

@Local
public interface TreeNodeFacadeLocal {

	/**
	 * Note that user rights are ignored here.
	 * @param iGenericObjectId
	 * @param moduleId the module id
	 * @param iRelationId
	 * @param relationtype
	 * @param direction
	 * @return a new tree node for the generic object with the given id.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	public abstract GenericObjectTreeNode newGenericObjectTreeNode(
		Integer iGenericObjectId, Integer moduleId, Integer iRelationId,
		SystemRelationType relationtype, RelationDirection direction)
		throws CommonFinderException;

	/**
	 * gets the list of sub nodes for a specific generic object tree node.
	 * Note that there is a specific method right on this method.
	 * @param node tree node of type generic object tree node
	 * @return list of sub nodes for given tree node
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	public abstract List<TreeNode> getSubNodesIgnoreUser(
		org.nuclos.server.navigation.treenode.GenericObjectTreeNode node);

}

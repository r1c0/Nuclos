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
package org.nuclos.server.navigation.treenode;

import org.nuclos.common2.LangUtils;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import java.util.Arrays;

/**
 * Tree node implementation representing a relation.
 * @todo P3 Implement refreshed(). When a relation is deleted, it will still be shown.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class RelationTreeNode extends StaticTreeNode<Integer> implements Comparable<TreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final RelationDirection direction;

	/**
	 * @param iId id of tree node (group id)
	 * @param sLabel label of tree node
	 * @param iRelationType
	 */
	public RelationTreeNode(Integer iId, String sLabel, String relationType, RelationDirection direction, GenericObjectTreeNode lotreenodeRight) {
		super(iId, sLabel, null, true, Arrays.asList(lotreenodeRight));
		this.direction = direction;
	}

	public RelationDirection getDirection() {
		return this.direction;
	}

	@Override
	public int compareTo(TreeNode that) {
		return LangUtils.compareComparables(this.getLabel(), that.getLabel());
	}

}	// class RelationTreeNode

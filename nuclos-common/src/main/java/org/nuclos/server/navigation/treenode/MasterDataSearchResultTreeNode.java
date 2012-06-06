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

import java.rmi.RemoteException;
import java.util.List;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.Utils;

/**
 * Tree node representing a masterdata search result.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version	01.00.00
 */
public class MasterDataSearchResultTreeNode extends AbstractSearchResultTreeNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String sEntity;
	
	public String getEntity() {
		return this.sEntity;
	}
	
	public MasterDataSearchResultTreeNode(String sEntity, CollectableSearchCondition cond, String sFilterName) {
		super(cond, sFilterName);
		this.sEntity = sEntity;
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubNodes(this);
	}

}

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

import org.nuclos.common.Utils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Tree node implementation representing a subform.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: Create a constructor that takes EntityTreeViewVO instead of MasterDataVO.
 * This way, we could get rid of 
 * {@link org.nuclos.server.genericobject.Modules.getSubnodesMD(String)}.
 * </p>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class SubFormTreeNode<Id> extends DynamicTreeNode<Id>{
	
	/**
	 * @deprecated See class javadoc for details. (Thomas Pasch)
	 */
	public SubFormTreeNode(Id id, TreeNode node, MasterDataVO mdVO) {
	    super(id, node, mdVO);
    }

	public GenericObjectTreeNode getGenericObjectTreeNode(){
		return (GenericObjectTreeNode) super.getTreeNode();
	}

	@Override
    public MasterDataVO getMasterDataVO() {
	    return super.getMasterDataVO();
    }

	@Override
    protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
	    return Utils.getTreeNodeFacade().getSubNodesForSubFormTreeNode(getTreeNode(), getMasterDataVO());
    }

	@Override
    public TreeNode refreshed() {
	    return Utils.getTreeNodeFacade().getSubFormTreeNode(getGenericObjectTreeNode(), getMasterDataVO());
    }
	
}

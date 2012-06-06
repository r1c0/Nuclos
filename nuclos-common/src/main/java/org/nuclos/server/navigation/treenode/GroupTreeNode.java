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

import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.Utils;

/**
 * Tree node implementation representing a group of generic objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class GroupTreeNode extends MasterDataTreeNode<Integer> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String sGroupName;
	private final String sGroupTypeName;
	private final String sDescription;

	public GroupTreeNode(Integer iId, String sGroupName, String sGroupTypeName, String sDescription) {
		super(NuclosEntity.GROUP.getEntityName(), iId);

		this.sGroupName = sGroupName;
		this.sGroupTypeName = sGroupTypeName;
		this.sDescription = sDescription;
	}

	@Override
	public String getLabel() {
		final StringBuilder sb = new StringBuilder(sGroupName);
		if (sGroupTypeName != null) {
			sb.append(" (").append(sGroupTypeName).append(")");
		}
		return sb.toString();
	}

	@Override
	public String getDescription() {
		return this.sDescription;
	}

	public String getGroupName() {
		return this.sGroupName;
	}

	public String getGroupTypeName() {
		return this.sGroupTypeName;
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubNodes(this);
	}

	@Override
	public GroupTreeNode refreshed() throws CommonFinderException {
		try {
			return Utils.getTreeNodeFacade().getGroupTreeNode(this.getId(), false);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}	// class GroupTreeNode

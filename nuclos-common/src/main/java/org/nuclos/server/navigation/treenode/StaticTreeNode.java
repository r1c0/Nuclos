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
import java.util.Collections;
import java.util.List;

/**
 * Static tree node implementation.
 * Label, description and the list of subnodes are given in the constructor and will not be changed during refresh.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class StaticTreeNode<Id> extends AbstractStaticTreeNode<Id> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final boolean bNeedsParent;

	public StaticTreeNode(Id id, String sLabel, String sDescription, boolean bNeedsParent) {
		this(id, sLabel, sDescription, bNeedsParent, Collections.<TreeNode>emptyList());
	}

	public StaticTreeNode(Id id, String sLabel, String sDescription, boolean bNeedsParent, List<? extends TreeNode> lstSubNodes) {
		super(id, sLabel, sDescription);
		this.bNeedsParent = bNeedsParent;
		this.setSubNodes(lstSubNodes);
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		// this method should never be called.
		throw new UnsupportedOperationException("getSubNodesImpl");
	}

	@Override
	public boolean needsParent() {
		return this.bNeedsParent;
	}

}	// class StaticTreeNode

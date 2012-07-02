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
package org.nuclos.client.explorer;

import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;

/**
 * Business Delegate for <code>TreeNodeFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ExplorerDelegate {

	private static ExplorerDelegate INSTANCE;
	
	//

	// Spring injection
	
	private TreeNodeFacadeRemote treeNodeFacadeRemote;
	
	// end of Spring injection

	/**
	 * Use getInstance() to get the one and only instance of this class.
	 */
	ExplorerDelegate() {
		INSTANCE = this;
	}

	public static ExplorerDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setTreeNodeFacadeRemote(TreeNodeFacadeRemote treeNodeFacadeRemote) {
		this.treeNodeFacadeRemote = treeNodeFacadeRemote;
	}

	/**
	 * Get the Node for the given object ignoring user permissions
	 * @param genericObjectId    the generic object id
	 * @param modukeId           the generic object's module
	 * @return the tree for the leased object with the given id
	 */
	public GenericObjectTreeNode getGenericObjectTreeNode(int genericObjectId, int moduleId, Integer parentId) throws CommonFinderException {
		try {
			return this.getFacade().getGenericObjectTreeNode(genericObjectId, moduleId, parentId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iGroupId
	 * @return the tree for the group with the given id
	 */
	public GroupTreeNode getGroupTreeNode(int iGroupId) throws CommonFinderException {
		try {
			return this.getFacade().getGroupTreeNode(iGroupId, false);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 *
	 * @param iNucletId
	 * @return the tree for the nuclet with the given id
	 * @throws CommonFinderException
	 */
	public NucletTreeNode getNucletTreeNode(int iNucletId) throws CommonFinderException {
		try {
			return this.getFacade().getNucletTreeNode(iNucletId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public MasterDataTreeNode<?> getMasterDataTreeNode(Integer iId, String sEntity) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getFacade().getMasterDataTreeNode(iId, sEntity, false);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return the treeNodeFacadeRemote
	 */
	private TreeNodeFacadeRemote getFacade() {
		return this.treeNodeFacadeRemote;
	}

}	// class ExplorerDelegate

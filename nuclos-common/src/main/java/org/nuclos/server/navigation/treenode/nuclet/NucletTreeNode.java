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
package org.nuclos.server.navigation.treenode.nuclet;

import java.rmi.RemoteException;
import java.util.List;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.Utils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node implementation representing a nuclet.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 00.01.000
 */
public class NucletTreeNode extends MasterDataTreeNode<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String label;
	private final String description;
	
	private final boolean showDependeces;
	
	public NucletTreeNode(EntityObjectVO eovo, boolean showDependeces) {
		this(eovo.getId().intValue(), 
			eovo.getField("name", String.class), 
			eovo.getField("description", String.class),
			showDependeces);
	}

	public NucletTreeNode(Integer nucletId, String label, String description, boolean showDependeces) {
		super(NuclosEntity.NUCLET.getEntityName(), nucletId);

		this.showDependeces = showDependeces;
		this.label = label;
		this.description = description;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public boolean isShowDependeces() {
		return showDependeces;
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubNodes(this);
	}

	@Override
	public NucletTreeNode refreshed() throws CommonFinderException {
		try {
			return Utils.getTreeNodeFacade().getNucletTreeNode(this.getId());
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}	// class NucletTreeNode

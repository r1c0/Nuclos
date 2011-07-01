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
package org.nuclos.client.explorer.node.datasource;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Treenode representing an Directory Node in the datasourcetree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class DirectoryDatasourceNode extends AbstractDatasourceTreeNode {

	private static final long serialVersionUID = -4616259638179701100L;

	private boolean isRoot;

	/**
	 * create directory node
	 * @param blnIsRoot
	 * @param aLabel
	 * @param aDescription
	 * @param aSubNodeList
	 */
	public DirectoryDatasourceNode(boolean blnIsRoot, String aLabel, String aDescription,
			List<? extends TreeNode> aSubNodeList) {

		super(null, aLabel, aDescription, aSubNodeList);
		assert(aLabel != null);
		this.isRoot = blnIsRoot;
	}

	/**
	 * is the node the root node of the whole tree
	 * @return
	 */
	public boolean isRoot() {
		return isRoot;
	}

	@Override
	public void refresh() {
		if (this.isRoot) {
			// add All datasource node
			List<AbstractDatasourceTreeNode> subNodeList = new ArrayList<AbstractDatasourceTreeNode>();
			subNodeList.add(new AllDatasourceNode());
			subNodeList.add(new OwnDatasourceNode());

			setSubNodes(subNodeList);
		}
		else {
			List<AbstractDatasourceTreeNode> subNodeList = new ArrayList<AbstractDatasourceTreeNode>();
				
			sortNodeListByLabel(subNodeList);
			setSubNodes(subNodeList);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof DirectoryDatasourceNode
				&& getLabel().equals(((DirectoryDatasourceNode) obj).getLabel());
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

}	// class DirectoryDatasourceNode

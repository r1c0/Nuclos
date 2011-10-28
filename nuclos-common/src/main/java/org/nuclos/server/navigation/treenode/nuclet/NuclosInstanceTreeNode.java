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

import java.util.List;

import org.nuclos.common.Utils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node implementation representing the nuclos instance with root nuclets.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 00.01.000
 */
public class NuclosInstanceTreeNode implements TreeNode {

	private String label = "Nuclos";
	private String description = "";

	public NuclosInstanceTreeNode() {
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public NuclosInstanceTreeNode refreshed() throws CommonFinderException {
		return this;
	}

	@Override
	public Object getId() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	public List<? extends TreeNode> getSubNodes() {
		return Utils.getTreeNodeFacade().getSubNodes(this);
	}

	@Override
	public Boolean hasSubNodes() {
		return true;
	}

	@Override
	public void removeSubNodes() {

	}

	@Override
	public void refresh() {

	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	@Override
	public boolean needsParent() {
		return false;
	}

	@Override
	public String getEntityName() {
		return null;
	}
}

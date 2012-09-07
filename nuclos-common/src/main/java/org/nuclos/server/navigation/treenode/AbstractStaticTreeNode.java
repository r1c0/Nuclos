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

import org.nuclos.common2.exception.CommonFinderException;

/**
 * Abstract tree node with a given label and description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class AbstractStaticTreeNode<Id> extends AbstractTreeNode<Id> {

	private final String sLabel;
	private final String sDescription;

	public AbstractStaticTreeNode(Id id, String sLabel, String sDescription) {
		super(id);
		this.sDescription = sDescription;
		this.sLabel = sLabel;
	}

	@Override
	public String getLabel() {
		return this.sLabel;
	}

	@Override
	public String getDescription() {
		return this.sDescription;
	}

	/**
	 * Default implementation: no refresh.
	 * @return this
	 * @throws CommonFinderException
	 */
	@Override
	public TreeNode refreshed() throws CommonFinderException {
		return this;
	}

}	// class AbstractStaticTreeNode

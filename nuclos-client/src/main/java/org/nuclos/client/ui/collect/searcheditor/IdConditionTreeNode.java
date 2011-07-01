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
package org.nuclos.client.ui.collect.searcheditor;

import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;

/**
 * <code>TreeNode</code> that contains a <code>CollectableIdCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class IdConditionTreeNode extends SearchConditionTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CollectableIdCondition cond;

	public IdConditionTreeNode(CollectableIdCondition cond) {
		this.cond = cond;
	}

	/**
	 * This node cannot be expanded.
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected String getLabelForExpandedState() {
		throw new UnsupportedOperationException("Ein Id-Knoten kann nicht erweitert werden.");
	}

	@Override
	public CollectableIdCondition getSearchCondition() {
		return this.cond;
	}

}	// class IdConditionTreeNode

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

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Tree node implementation representing a master data object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public abstract class MasterDataTreeNode<Id> extends AbstractTreeNode<Id> implements Comparable<MasterDataTreeNode<Id>> {

	private final String sEntityName;

	/**
	 * @param id id of tree node
	 */
	public MasterDataTreeNode(String sEntityName, Id id) {
		super(id);

		this.sEntityName = sEntityName;
	}

	@Override
	public String getEntityName() {
		return this.sEntityName;
	}

	protected String getIdentifier(MasterDataVO mdvo) {
		MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
		return getCommonLocaleDelegate().getTreeViewLabel(mdvo, getEntityName(), metaprovider);
	}

	protected String getDescription(MasterDataVO mdvo) {
		MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
		return getCommonLocaleDelegate().getTreeViewDescription(mdvo, getEntityName(), metaprovider);
	}

	@Override
	public int compareTo(MasterDataTreeNode<Id> that) {
		return LangUtils.compareComparables(this.getLabel(), that.getLabel());
	}

} // class MasterDataTreeNode

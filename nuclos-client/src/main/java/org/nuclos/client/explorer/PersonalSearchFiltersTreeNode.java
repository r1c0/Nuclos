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

import java.util.ArrayList;
import java.util.List;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.AbstractTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node containing all personal search filters by module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 00.01.000
 */
public class PersonalSearchFiltersTreeNode extends AbstractTreeNode<Object> {
	
	private static final long serialVersionUID = 4153932414394405456L;

	public PersonalSearchFiltersTreeNode() {
		super(null);
	}

	@Override
	public String getLabel() {
		return getSpringLocaleDelegate().getMessage("PersonalSearchFiltersTreeNode.2","Eigene Filter");
	}

	@Override
	public String getDescription() {
		return getSpringLocaleDelegate().getMessage("PersonalSearchFiltersTreeNode.1","Alle pers\u00f6nlichen Suchfilter");
	}

	@Override
	public List<? extends TreeNode> getSubNodesImpl() throws NuclosFatalException {
		final List<TreeNode> result = new ArrayList<TreeNode>();

		for (MasterDataVO mdvoModule : Modules.getInstance().getModules()) {
			final Integer iModuleId = (Integer) mdvoModule.getId();
			final String sEntity = mdvoModule.getField("entity", String.class);
			if (SecurityCache.getInstance().isReadAllowedForModule(sEntity, null)) {
				try {
					if (!SearchFilters.forEntity(sEntity).getAll().isEmpty()) {
						result.add(new PersonalSearchFiltersByEntityTreeNode(sEntity, iModuleId));
					}
				} catch (PreferencesException e) {
					throw new NuclosFatalException(e);
				}
			}
		}

		for (MasterDataMetaVO mdmetavo : MasterDataDelegate.getInstance().getMetaData()) {
			String sEntity = mdmetavo.getEntityName();
			if (!Modules.getInstance().isModuleEntity(sEntity) && SecurityCache.getInstance().isReadAllowedForMasterData(sEntity)) {
				try {
					if (!SearchFilters.forEntity(sEntity).getAll().isEmpty()) {
						result.add(new PersonalSearchFiltersByEntityTreeNode(sEntity, mdmetavo.getId()));
					}
				} catch (PreferencesException e) {
					throw new NuclosFatalException(e);
				}
			}
		}
		assert result != null;
		return result;
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		// no refresh necessary as personal search filter tree nodes are static:
		return this;
	}

}	// class PersonalSearchFiltersTreeNode

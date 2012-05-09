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
package org.nuclos.client.searchfilter;

import java.rmi.RemoteException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.common.Utils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node implementation representing an entity search filter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 00.01.000
 */
public class SearchFilterTreeNode extends EntitySearchResultTreeNode {

	private static final long serialVersionUID = -5356067512724852062L;

	private static final Logger log = Logger.getLogger(SearchFilterTreeNode.class);

	private transient EntitySearchFilter searchfilter;
	private Integer searchFilterId;

	public SearchFilterTreeNode(EntitySearchFilter searchfilter) {
		super(searchfilter.getName(),
				(searchfilter.getOwner() != null && !searchfilter.getOwner().equals(
						Main.getInstance().getMainController().getUserName())) ? searchfilter.getDescription() + "("+searchfilter.getOwner()+")" : searchfilter.getDescription(),
				getInternalSearchExpression(searchfilter),
				searchfilter.getName(), searchfilter.getOwner(), searchfilter.getEntityName());
		this.searchfilter = searchfilter;
		this.searchFilterId = searchfilter.getId();
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		// need to build a EntitySearchResultTreeNode because the server does not know SearchFilterTreeNodes:
		final EntitySearchResultTreeNode node = new EntitySearchResultTreeNode(
				this.getLabel(), this.getDescription(), this.getSearchExpression(), this.getFilterName(), this.getOwner(), this.getEntity());

		return Utils.getTreeNodeFacade().getSubNodes(node);
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		try {
			EntitySearchFilter searchFilter = SearchFilters.forEntity(this.getEntity()).get(this.getFilterName(), this.getOwner());
			if (searchFilter != null)
				return new SearchFilterTreeNode(searchFilter);
			else
				throw new CommonFinderException(getSpringLocaleDelegate().getMessage(
						"SearchFilterTreeNode.1","Der Suchfilter existiert nicht mehr") + ".");
		}
		catch (NoSuchElementException ex) {
			throw new CommonFinderException(getSpringLocaleDelegate().getMessage(
					"SearchFilterTreeNode.1","Der Suchfilter existiert nicht mehr") + ".", ex);
		}
		catch (PreferencesException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private static CollectableSearchExpression getInternalSearchExpression(EntitySearchFilter searchfilter) {
		/** @todo respect sorting order! */
		if (Modules.getInstance().isModuleEntity(searchfilter.getEntityName())) {
			return new CollectableGenericObjectSearchExpression(searchfilter.getInternalSearchCondition(), searchfilter.getSearchDeleted());
		}
		return new CollectableSearchExpression(searchfilter.getInternalSearchCondition());
	}

	public EntitySearchFilter getSearchFilter() {
		if (searchfilter == null && searchFilterId != null)
			searchfilter = SearchFilterCache.getInstance().getEntitySearchFilterById(searchFilterId);
		return searchfilter;
	}

	@Override
	public String getLabel() {
		try {
			if (getSearchFilter() != null && getSearchFilter().getLabelResourceId() != null) {
				return getSpringLocaleDelegate().getTextFallback(getSearchFilter().getLabelResourceId(), super.getLabel());
			}
		}
		catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}
		return super.getLabel();
	}

	@Override
	public String getDescription() {
		try {
			if (getSearchFilter() != null && getSearchFilter().getDescriptionResourceId() != null) {
				return getSpringLocaleDelegate().getTextFallback(getSearchFilter().getDescriptionResourceId(), super.getDescription());
			}
		}
		catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}
		return super.getDescription();
	}



}	// class SearchFilterTreeNode

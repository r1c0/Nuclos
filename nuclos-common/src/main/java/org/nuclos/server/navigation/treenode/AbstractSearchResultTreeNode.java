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

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;

/**
 * Abstract base class for the various search result tree nodes in Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class AbstractSearchResultTreeNode extends AbstractTreeNode<Object> {

	private final CollectableSearchCondition cond;
	private final String sFilterName;	

	public AbstractSearchResultTreeNode(CollectableSearchCondition cond, String sFilterName) {
		super(null);

		this.cond = cond;
		this.sFilterName = sFilterName;
	}

	/**
	 * @return search condition for this tree node.
	 */
	public CollectableSearchCondition getSearchCondition() {
		return this.cond;
	}

	/**
	 * @return the name for this search result.
	 */
	public String getFilterName() {
		return this.sFilterName;
	}

	@Override
	public String getLabel() {
		return getSpringLocaleDelegate().getMessage("GenericObjectCollectController.93","Suchergebnis ({0})", this.getFilterName()); 
		//MessageFormat.format(locale.getResourceById(locale.getUserLocaleId(), "searchresult.treenode.label"), this.getFilterName());
		//"Suchergebnis (" + this.getFilterName() + ')';
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		// no refresh necessary as search results are only created adhoc:
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || (this.getClass() != o.getClass())) {
			return false;
		}
		final AbstractSearchResultTreeNode that = (AbstractSearchResultTreeNode) o;
		return LangUtils.equals(this.getFilterName(), that.getFilterName());
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getFilterName());
	}

}	// class AbstractSearchResultTreeNode

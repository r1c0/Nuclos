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

import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilterTreeNode;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.navigation.treenode.AbstractTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node containing the personal search filters for a module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 00.01.000
 */
public class PersonalSearchFiltersByEntityTreeNode extends AbstractTreeNode<Object> {
	
	private static final long serialVersionUID = -1980756173915510766L;

	private final Integer iModuleId;
	private final String sEntity;

	/**
	 * @param iModuleId the module id, if any.
	 */
	public PersonalSearchFiltersByEntityTreeNode(String sEntity, Integer iModuleId) {
		super(null);
		this.iModuleId = iModuleId;
		this.sEntity = sEntity;
	}

	/**
	 * @return the module id, if any.
	 */
	public Integer getModuleId() {
		return this.iModuleId;
	}

	public String getEntity() {
		return this.sEntity;
	}

	@Override
	public String getLabel() {
		return this.getEntityLabel();
	}

	@Override
	public String getDescription() {
		return getSpringLocaleDelegate().getMessage("PersonalSearchFiltersByEntityTreeNode.1","Pers\u00f6nliche Suchfilter f\u00fcr die Entit\u00e4t") + " \"" + this.getEntityLabel() + "\"";
	}

	private String getEntityLabel() {
		if (Modules.getInstance().isModuleEntity(this.getEntity())) {
			return Modules.getInstance().getEntityLabelByModuleId(this.getModuleId());
		}
		return getSpringLocaleDelegate().getLabelFromMetaDataVO(MasterDataDelegate.getInstance().getMetaData(this.getEntity()));//MasterDataDelegate.getInstance().getMetaData(this.getEntity()).getLabel();
	}

	@Override
	public List<? extends TreeNode> getSubNodesImpl() throws NuclosFatalException {
		final List<SearchFilterTreeNode> result = new ArrayList<SearchFilterTreeNode>();

		final List<EntitySearchFilter> lstFilter;
		try {
			lstFilter = SearchFilters.forEntity(this.getEntity()).getAll();
		}
		catch (PreferencesException ex) {
			throw new NuclosFatalException(ex);
		}

		for (EntitySearchFilter searchfilter : lstFilter) {
			result.add(new SearchFilterTreeNode(searchfilter));
		}
		assert result != null;
		return result;
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		// no refresh necessary as personal search filter tree nodes are static:
		return this;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result
	        + ((iModuleId == null) ? 0 : iModuleId.hashCode());
	    result = prime * result + ((sEntity == null) ? 0 : sEntity.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if(this == obj)
		    return true;
	    if(!super.equals(obj))
		    return false;
	    if(getClass() != obj.getClass())
		    return false;
	    PersonalSearchFiltersByEntityTreeNode other = (PersonalSearchFiltersByEntityTreeNode) obj;
	    if(iModuleId == null) {
		    if(other.iModuleId != null)
			    return false;
	    }
	    else if(!iModuleId.equals(other.iModuleId))
		    return false;
	    if(sEntity == null) {
		    if(other.sEntity != null)
			    return false;
	    }
	    else if(!sEntity.equals(other.sEntity))
		    return false;
	    return true;
    }

}	// class PersonalSearchFiltersByModuleTreeNode

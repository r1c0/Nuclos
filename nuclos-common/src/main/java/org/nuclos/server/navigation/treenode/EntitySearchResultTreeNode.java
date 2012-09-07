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

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common.Utils;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Tree node implementation representing a leased object search result.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class EntitySearchResultTreeNode extends AbstractStaticTreeNode<Integer> {
	
	private static final long serialVersionUID = 6784499906928623893L;

	private final String sFilterName;
	private final String sOwner;
	private final String sEntity;
	private CollectableSearchExpression clctexpr;
	private String sLabel;

	/**
	 * @param sLabel label of tree node
	 * @param sDescription description of tree node
	 * @param clctexpr search expression
	 * @param sFilterName name of filter
	 * @param iModuleId id of module
	 */
	public EntitySearchResultTreeNode(String sLabel, String sDescription,
			CollectableSearchExpression clctexpr, String sFilterName, String sOwner, String sEntity) {

		super(null, sLabel, sDescription);

		this.clctexpr = clctexpr;
		this.sFilterName = sFilterName;
		this.sOwner = sOwner;
		this.sEntity = sEntity;
	}

	/**
	 * gets search condition value object for this tree node
	 * @return search condition value object for this tree node
	 */
	public CollectableSearchExpression getSearchExpression() {
		return this.clctexpr;
	}

	/**
	 * @return name of filter
	 */
	public String getFilterName() {
		return this.sFilterName;
	}

	/**
	 * @return owner of filter
	 */
	public String getOwner() {
		return this.sOwner;
	}
	
	/**
	 * @return name of entity this search result is limited to
	 */
	public String getEntity() {
		return this.sEntity;
	}

	@Override
	public String getLabel() {
		return (sLabel != null) ? sLabel : super.getLabel();
	}

	@Override
	public void setLabel(String sLabel) {
		this.sLabel = sLabel;
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubNodes(this);
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		return new EntitySearchResultTreeNode(getLabel(), getDescription(), getSearchExpression(), getFilterName(), getOwner(), getEntity());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || (this.getClass() != o.getClass())) {
			return false;
		}
		final EntitySearchResultTreeNode that = (EntitySearchResultTreeNode) o;
		return LangUtils.equals(this.getFilterName(), that.getFilterName()) && LangUtils.equals(this.getEntity(), that.getEntity());
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getFilterName()) ^ LangUtils.hashCode(this.getEntity());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException {
		try {
			ois.defaultReadObject();

			if (!(clctexpr instanceof CollectableGenericObjectSearchExpression)) {
				clctexpr = new CollectableGenericObjectSearchExpression(clctexpr.getSearchCondition(), clctexpr.getSortingOrder());
			}
		}
		catch (IOException e) {
			// todo
		}
	}

}	// class GenericObjectSearchResultTreeNode

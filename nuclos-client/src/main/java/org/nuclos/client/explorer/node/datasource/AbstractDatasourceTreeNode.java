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

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.datatransfer.DatasourceEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;
import java.text.Collator;
import java.util.*;

/**
 * Treenode representing an Node in the datasource tree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public abstract class AbstractDatasourceTreeNode implements TreeNode {

	protected static final Collator textCollator = Collator.getInstance();

	private final Integer iId;
	private String sLabel;
	private String sDescription;
	private List<? extends TreeNode> lstSubNodes;

	static {
		textCollator.setStrength(Collator.TERTIARY);
	}

	public AbstractDatasourceTreeNode(Integer iId, String sLabel, String sDescription, List<? extends TreeNode> lstSubNodes) {
		this.iId = iId;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.lstSubNodes = lstSubNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getId() {
		return this.iId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return this.sLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return this.sDescription;
	}
	
	@Override
	public String getIdentifier() {
		if (this.getId() != null) {
			return this.getId()+"#"+this.getLabel();
		}
		else {
			return "0#"+this.getLabel();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("deprecation")
	public List<? extends TreeNode> getSubNodes() {
		if (this.lstSubNodes == null) {
			refresh();
			if (this.lstSubNodes == null) {
				this.lstSubNodes = Collections.<TreeNode>emptyList();
			}
		}
		return this.lstSubNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean hasSubNodes() {
		return (this.lstSubNodes == null || this.lstSubNodes.size() == 0) ? null : this.lstSubNodes.size() > 0;
	}

	@Override
	public void removeSubNodes() {
		this.setSubNodes(null);
		assert this.hasSubNodes() == null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubNodes(List<? extends TreeNode> lstSubNodes) {
		this.lstSubNodes = lstSubNodes;
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	/**
	 * not supported.
	 */
	@Override
	public TreeNode refreshed() {
		//throw new UnsupportedOperationException("refreshed");
		return this;
	}

	@Override
	public boolean needsParent() {
		return false;
	}

	/**
	 * Insert the entityToInsert after the datasourceBefore in the subnodes
	 * @param entityToInsert
	 * @throws NuclosBusinessException
	 * @throws CommonBusinessException
	 */
	public void insertDatasource(DatasourceEntity entityToInsert) throws CommonBusinessException {
	}

	/**
	 * sort the given Node List by the Label of the treenodes
	 * @param aNodeList
	 */
	protected static void sortNodeListByLabel(List<? extends TreeNode> aNodeList) {
		Collections.sort(aNodeList, new Comparator<TreeNode>() {
			@Override
			public int compare(TreeNode o1, TreeNode o2) {
				return textCollator.compare(o1.getLabel(), o2.getLabel());
			}
		});
	}

}	// class AbstractDatasourceTreeNode

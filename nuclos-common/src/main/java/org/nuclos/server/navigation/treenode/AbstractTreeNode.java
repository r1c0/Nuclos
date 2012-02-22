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

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Abstract base class for all tree nodes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
@Configurable
public abstract class AbstractTreeNode<Id> implements TreeNode {
	private static final long serialVersionUID = -2950313888999290579L;

	private final Id id;
	private String sLabel;
	private Date dateChangedAt;
	private String sDescription;
	private List<? extends TreeNode> lstSubNodes;
	
	private transient SpringLocaleDelegate localeDelegate;

	/**
	 * @param id the tree node's id. May be <code>null</code>.
	 * @postcondition this.hasSubNodes() == null
	 */
	public AbstractTreeNode(Id id) {
		this.id = id;
		assert this.hasSubNodes() == null;
	}

	/**
	 * @param id the tree node's id. May be <code>null</code>.
	 * @param sLabel label of tree node
	 * @param sDescription description of tree node
	 * @postcondition this.hasSubNodes() == null
	 */
	public AbstractTreeNode(Id id, String sLabel, String sDescription) {
		this.id = id;
		this.setLabel(sLabel);
		this.setDescription(sDescription);
		assert this.hasSubNodes() == null;
	}
	
	/*
	 * Maven don't like this.
	 * {@link org.springframework.beans.factory.aspectj.AbstractInterfaceDrivenDependencyInjectionAspect}.
	public Object readResolve() throws ObjectStreamException {
		setSpringLocaleDelegate(SpringLocaleDelegate.getInstance());
		return this;
	}
	 */
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Id getId() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return this.sLabel;
	}

	protected void setLabel(String sLabel) {
		this.sLabel = sLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getChangedAt(){
		return this.dateChangedAt;
	}
	protected void setChangedAt(Date dateChangedAt){
		this.dateChangedAt = dateChangedAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return this.sDescription;
	}

	protected void setDescription(String sDescription) {
		this.sDescription = sDescription;
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

	@Override
	public synchronized List<? extends TreeNode> getSubNodes() {
		if (lstSubNodes == null) {
			try {
				lstSubNodes = getSubNodesImpl();
			}
			catch (RemoteException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		final List<? extends TreeNode> result = lstSubNodes;

		assert result != null;
		assert this.hasSubNodes() != null;
		return result;
	}

	/**
	 * calculates the list of sub nodes for this node. In contrast to getSubNodes(), this method has no side effects.
	 * @return the list of sub nodes for this node.
	 * @postcondition result != null
	 * @todo Shouldn't throw a RemoteException
	 */
	protected abstract List<? extends TreeNode> getSubNodesImpl() throws RemoteException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean hasSubNodes() {
		return (this.lstSubNodes == null) ? null : !this.lstSubNodes.isEmpty();
	}

	/**
	 * This is just for compatibility - try to avoid using this method.
	 * A tree node should be considered immutable.
	 * @param lstSubNodes
	 */
	protected void setSubNodes(List<? extends TreeNode> lstSubNodes) {
		this.lstSubNodes = lstSubNodes;
	}

	@Override
	public void removeSubNodes() {
		this.lstSubNodes = null;
		assert this.hasSubNodes() == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		throw new UnsupportedOperationException("refresh");
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return true;
	}

	@Override
	public boolean needsParent() {
		return false;
	}

	@Override
	public String getEntityName() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || (this.getClass() != o.getClass())) {
			return false;
		}
		return (LangUtils.equals(this.getId(), ((TreeNode) o).getId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getId());
	}
}	// class AbstractTreeNode

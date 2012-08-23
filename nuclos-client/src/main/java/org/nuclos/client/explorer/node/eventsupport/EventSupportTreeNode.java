//Copyright (C) 2012  Novabit Informationssysteme GmbH
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

package org.nuclos.client.explorer.node.eventsupport;

import java.util.Collections;
import java.util.List;

import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTreeNode implements TreeNode {

	private Object oId;
	private String sLabel;
	private String sDescription;
	private String sName;
	private List<? extends TreeNode> lstSubNodes;
	private EventSupportTargetType esTargetType;
	private boolean bIsRoot;
	private boolean isLeaf = false;
	private EventSupportTreeNode parentNode;
	private EventSupportManagementController controller;

	public EventSupportTreeNode(EventSupportManagementController controller, EventSupportTreeNode parentNode, Object id, String name, String label, String description, EventSupportTargetType type, boolean isRoot) {
		super();
		this.oId = id;		
		this.sLabel = label;
		this.sName = name;
		this.sDescription = description;
		this.esTargetType = type;		
		this.bIsRoot = isRoot;
		this.parentNode = parentNode;
		this.controller = controller;
	
	}
	
	public EventSupportTreeNode(EventSupportManagementController controller, EventSupportTreeNode parentNode, Object id, String name, String label, String description, EventSupportTargetType type) {
		this(controller, parentNode, id, name, label, description, type, false);
	}
	
	@Override
	public Object getId() {
		return this.oId;
	}

	@Override
	public String getEntityName() {
		return this.sName;
	}

	@Override
	public String getLabel() {
		return this.sLabel;
	}

	@Override
	public String getDescription() {
		return this.sDescription;
	}

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	public List<? extends TreeNode> getSubNodes() {	
		if (this.lstSubNodes == null) {
			refresh();
			if (this.lstSubNodes == null) {
				this.lstSubNodes = Collections.<TreeNode>emptyList();
			}
		}
		return this.lstSubNodes;		
	}

	public boolean isExternalSource() {
		return this.oId == null;
	}
	
	@Override
	public Boolean hasSubNodes() {
		boolean retVal = false;
		
		if (this.lstSubNodes != null && !this.lstSubNodes.isEmpty())
			retVal = true;
		
		return retVal;
	}

	@Override
	public void removeSubNodes() {
		this.lstSubNodes.clear();
	}

	@Override
	public void refresh() {
		if (!isLeaf())
			setLstSubNodes(getController().createSubNodesByType(this));
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		return this;
	}

	@Override
	public boolean needsParent() {
		return false;
	}

	public EventSupportTargetType getTreeNodeType() {
		return esTargetType;
	}

	public void setTreeNodeType(EventSupportTargetType sTreeNodeType) {
		this.esTargetType = sTreeNodeType;
	}

	public void setLstSubNodes(List<? extends TreeNode> lstSubNodes) {
		this.lstSubNodes = lstSubNodes;
	}

	public boolean isLeaf() {
		return EventSupportTargetType.EVENTSUPPORT.equals(this.esTargetType);
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public EventSupportManagementController getController() {
		return this.controller;
	}

	public EventSupportTreeNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(EventSupportTreeNode parentNode) {
		this.parentNode = parentNode;
	}
	
}

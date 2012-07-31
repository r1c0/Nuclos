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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.statemodel.EventSupportRepository;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTreeNode implements TreeNode {

	private Object oId;
	private String sLabel;
	private String sDescription;
	private String sName;
	private List<? extends TreeNode> lstSubNodes;
	private EventSupportTargetType esTargetType;
	private boolean bIsRoot;
	private EventSupportManagementController controller;
	private boolean isLeaf = false;
	
	public EventSupportTreeNode(EventSupportManagementController ctrl, Object id, String name, String label, String description, EventSupportTargetType type, boolean isRoot) {
		super();
		this.oId = id;		
		this.sLabel = label;
		this.sName = name;
		this.sDescription = description;
		this.esTargetType = type;		
		this.bIsRoot = isRoot;
		this.controller = ctrl;
		
		if (this.esTargetType == null)
			this.isLeaf = true;
	}
	
	public EventSupportTreeNode(EventSupportManagementController ctrl, Object id, String name, String label, String description, EventSupportTargetType type) {
		this(ctrl, id, name, label, description, type, false);
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
		return Boolean.valueOf(!this.isLeaf);
	}

	@Override
	public void removeSubNodes() {
		this.lstSubNodes.clear();
	}

	@Override
	public void refresh() {
		
		if (!this.isLeaf() && this.lstSubNodes == null) {
			List<EventSupportTreeNode> retVal = new ArrayList<EventSupportTreeNode>();
			if (this.oId == null)
			{
				if (this.bIsRoot)
				{
					try {
						List<EventSupportVO> eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
						for (EventSupportVO s : eventSupportTypes)
						{
							EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this.controller, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false);
							retVal.add(eventSupportTreeNode);
						}									
						this.lstSubNodes = retVal;
					} catch (RemoteException e) {
						Log.error(e.getMessage(), e);
					}
				}
				else
				{
					if (this.esTargetType.equals(EventSupportTargetType.EVENTSUPPORT_TYPE))
					{
						try {
							List<EventSupportVO> eventSupportTypes = EventSupportRepository.getInstance().getEventSupportsByType(this.sName);
							for (EventSupportVO s : eventSupportTypes)
							{
								EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this.controller, null, s.getInterface(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT, false);
								retVal.add(eventSupportTreeNode); 
							}	
							this.lstSubNodes = retVal;
						} catch (RemoteException e) {
							Log.error(e.getMessage(), e);
						}
					}
					
				}
			}
		}
		
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

	public EventSupportManagementController getController() {
		return controller;
	}

	public void setLstSubNodes(List<? extends TreeNode> lstSubNodes) {
		this.lstSubNodes = lstSubNodes;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	
}

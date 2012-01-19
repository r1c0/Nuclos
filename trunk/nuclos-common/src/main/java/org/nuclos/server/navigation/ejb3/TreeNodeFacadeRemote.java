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
package org.nuclos.server.navigation.ejb3;

import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.DefaultMasterDataTreeNode;
import org.nuclos.server.navigation.treenode.DynamicTreeNode;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;
import org.nuclos.server.navigation.treenode.GroupSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NuclosInstanceTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentTreeNode;

// @Remote
public interface TreeNodeFacadeRemote {

	/**
	 * gets a generic object tree node for a specific generic object
	 * @param iGenericObjectId id of generic object to get tree node for
	 * @return generic object tree node for given id, if existing and allowed. null otherwise.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	GenericObjectTreeNode getGenericObjectTreeNode(
		Integer iGenericObjectId, Integer moduleId, Integer parentId) throws CommonFinderException;

	/**
	 * Note that user rights are ignored here.
	 * @param iGenericObjectId
	 * @param moduleId the module id
	 * @param iRelationId
	 * @param relationtype
	 * @param direction
	 * @return a new tree node for the generic object with the given id.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	GenericObjectTreeNode newGenericObjectTreeNode(
		Integer iGenericObjectId, Integer moduleId, Integer iRelationId,
		SystemRelationType relationtype, RelationDirection direction, Integer parentId)
		throws CommonFinderException;

	/**
	 * gets the list of sub nodes for a specific generic object tree node.
	 * Note that there is a specific method right on this method.
	 * @param node tree node of type generic object tree node
	 * @return list of sub nodes for given tree node
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	List<TreeNode> getSubNodesIgnoreUser(
		org.nuclos.server.navigation.treenode.GenericObjectTreeNode node);

	/**
	 * method to get a group tree node for a specific group
	 * @param iId id of group to get tree node for
	 * @return group tree node for given id
	 * @postcondition result != null
	 */
	GroupTreeNode getGroupTreeNode(final Integer iId,
		final boolean bLoadSubNodes) throws CommonFinderException;

	/**
	 * method to get a nuclet tree node for a specific nuclet
	 * @param iId id of nuclet to get tree node for
	 * @return nuclet tree node for given id
	 * @postcondition result != null
	 */
	NucletTreeNode getNucletTreeNode(final Integer iId) throws CommonFinderException;

	/**
	 * method to get a masterdata tree node for a specific masterdata record
	 * @param iId id of masterdata record to get tree node for
	 * @return masterdata tree node for given id
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	MasterDataTreeNode<Integer> getMasterDataTreeNode(
		Integer iId, String sEntity, boolean bLoadSubNodes)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<GenericObjectTreeNode> getSubNodes(GroupTreeNode node);

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<TreeNode> getSubNodes(NucletTreeNode node);

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<AbstractNucletContentEntryTreeNode> getSubNodes(NucletContentTreeNode node);

	/**
	 *
	 * @return the available nodes.
	 * @postcondition result != null
	 */
	List<AbstractNucletContentEntryTreeNode> getAvailableNucletContents();

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<TreeNode> getSubnodes(DefaultMasterDataTreeNode node);

	DynamicTreeNode<Integer> getDynamicTreeNode(TreeNode node,
		MasterDataVO mdVO);

	List<TreeNode> getSubNodesForDynamicTreeNode(TreeNode node,
		MasterDataVO mdVO);

	/**
	 *
	 * @param node
	 * @param mdVO
	 * @return
	 */
	List<SubFormEntryTreeNode> getSubNodesForSubFormTreeNode(TreeNode node,
        MasterDataVO mdVO);

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<GroupTreeNode> getSubNodes(
		GroupSearchResultTreeNode node);

	/**
	 * get the subnodes for a masterdata search result
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	List<DefaultMasterDataTreeNode> getSubNodes(
		MasterDataSearchResultTreeNode node);

	/**
	 * method to get the list of sub nodes for a specific generic object search result tree node
	 * @param node tree node of type search result tree node
	 * @return list of sub nodes for given tree node
	 * @postcondition result != null
	 */
	List<TreeNode> getSubNodes(EntitySearchResultTreeNode node);

	SubFormEntryTreeNode getSubFormEntryTreeNode(Integer iId, String sEntity,
        boolean bLoadSubNodes) throws CommonFinderException,
        CommonPermissionException;

	SubFormTreeNode getSubFormTreeNode(GenericObjectTreeNode node, MasterDataVO mdVO);

	void addNucletContents(Long nucletId, Set<AbstractNucletContentEntryTreeNode> contents) throws NuclosBusinessException;

	AbstractNucletContentEntryTreeNode getNucletContentEntryNode(NuclosEntity entity, Long eoId);

	boolean removeNucletContents(Set<AbstractNucletContentEntryTreeNode> contents);

	List<AbstractNucletContentEntryTreeNode> getNucletContent(NucletTreeNode node);

	List<NucletTreeNode> getSubNodes(NuclosInstanceTreeNode node);
}

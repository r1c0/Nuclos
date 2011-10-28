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
package org.nuclos.server.navigation.treenode.nuclet.content;

import java.util.List;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * Tree node implementation representing a nuclet content.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 00.01.000
 */
public class NucletContentTreeNode implements TreeNode {

	private final Long nucletId;
	private final NuclosEntity content;

	public NucletContentTreeNode(Long nucletId, NuclosEntity content) {
		this.nucletId = nucletId;
		this.content = content;
	}

	public Long getNucletId() {
		return nucletId;
	}

	public NuclosEntity getEntity() {
		return content;
	}

	@Override
	public Object getId() {
		return content;
	}

	@Override
	public String getIdentifier() {
		return content.getEntityName();
	}

	@Override
	public List<? extends TreeNode> getSubNodes() {
		return getTreeNodeFacade().getSubNodes(this);
	}

	@Override
	public Boolean hasSubNodes() {
		return false;
	}

	@Override
	public void removeSubNodes() {
	}

	@Override
	public void refresh() {
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	@Override
	public boolean needsParent() {
		return true;
	}

	@Override
	public String getLabel() {
		return CommonLocaleDelegate.getLabelFromMetaDataVO(getMetaData());
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
		return this;
	}

	private EntityMetaDataVO getMetaData() {
		return SpringApplicationContextHolder.getBean(MetaDataProvider.class).getEntity(content);
	}

	private TreeNodeFacadeRemote getTreeNodeFacade() throws NuclosFatalException {
		return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
	}

	@Override
	public String getEntityName() {
		return null;
	}
}	// class NucletContentTreeNode

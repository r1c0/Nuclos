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
import java.util.List;

import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.Utils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Abstract tree node implementation representing a tree node with label.
 * Label, description and the list of subnodes are given in the constructor and will be (dynamically) changed during refresh.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class DynamicTreeNode<Id> extends AbstractTreeNode<Id>{

	private TreeNode node;
	private MasterDataVO mdVO;

	public DynamicTreeNode(Id id, TreeNode node, MasterDataVO mdVO) {
		super(id);
		this.setLabel(getLabel(mdVO));
		this.setDescription((String)mdVO.getField("entity"));
		this.node = node;
		this.mdVO = mdVO;
	}

	@Override
	protected List<? extends TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubNodesForDynamicTreeNode(getTreeNode(), getMasterDataVO());
	}

	protected TreeNode getTreeNode() {
		return this.node;
	}

	protected MasterDataVO getMasterDataVO() {
		return this.mdVO;
	}

	private String getLabel(MasterDataVO mdVO) {
		String sFolderName = null;
		try {
			sFolderName = getSpringLocaleDelegate().getText((String)mdVO.getField("foldername"), "");
		} catch (Exception e) {}

		if (StringUtils.isNullOrEmpty(sFolderName)) {
			sFolderName = (String)mdVO.getField("foldername");
		}

		if (StringUtils.isNullOrEmpty(sFolderName)) {
			final String sEntityName = (String)mdVO.getField("entity");
			ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
			if (modules.isModuleEntity(sEntityName)) {
				MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
				return getSpringLocaleDelegate().getResource(metaprovider.getEntity(sEntityName).getLocaleResourceIdForLabel(), null);
			}
			MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
			if (cache != null) {
				return getSpringLocaleDelegate().getLabelFromMetaDataVO(cache.getMetaData(sEntityName));//MasterDataMetaCache.getInstance().getMetaData(sEntityName).getLabel();
			}
		}
		return sFolderName;

	}

	@Override
	public TreeNode refreshed(){
		try {
			  return Utils.getTreeNodeFacade().getDynamicTreeNode(this.getTreeNode(), this.getMasterDataVO());
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

}

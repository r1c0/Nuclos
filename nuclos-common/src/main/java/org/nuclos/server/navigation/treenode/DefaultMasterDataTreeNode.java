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

import org.nuclos.common.Utils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * TreeNode for MasterDataRecords
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version	01.00.00
 */
public class DefaultMasterDataTreeNode extends MasterDataTreeNode<Integer> {
	
	/**
	 * Attention:
	 * Even if this sounds weird: mdvo could be null. Do not rely on the
	 * value. See {@link #init()} for details...
	 */
	private final MasterDataVO mdvo;

	public DefaultMasterDataTreeNode(String sEntity, MasterDataVO mdvo) {
		super(sEntity, mdvo.getIntId());
		this.mdvo = mdvo;
	}
	
	// @PostConstruct
	final void init() {
		// after deserialization (e.g. from XStream) this is called again
		// but with mdvo == null!
		if (mdvo != null) {
			this.setLabel(getIdentifier(mdvo));
			this.setDescription(getDescription(mdvo));
		}
		assert getLabel() != null;
	}
	
	@Override
	public String getLabel() {
		String result = super.getLabel();
		// after deserialization (e.g. from XStream) this is called again
		// but with mdvo == null!
		if (result == null && mdvo != null) {
			setLabel(getIdentifier(mdvo));
		}
		return result;
	}

	@Override
	public String getDescription() {
		String result = super.getDescription();
		// after deserialization (e.g. from XStream) this is called again
		// but with mdvo == null!
		if (result == null && mdvo != null) {
			setDescription(getDescription(mdvo));
		}
		return result;
	}

	@Override
	protected List<TreeNode> getSubNodesImpl() throws RemoteException {
		return Utils.getTreeNodeFacade().getSubnodes(this);
	}

	@Override
	public TreeNode refreshed() throws CommonFinderException {
    try {
		  return Utils.getTreeNodeFacade().getMasterDataTreeNode(this.getId(), this.getEntityName(), false);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new CommonRemoteException(ex);
		}
	}
}

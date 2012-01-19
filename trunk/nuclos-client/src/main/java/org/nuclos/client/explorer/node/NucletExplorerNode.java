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
package org.nuclos.client.explorer.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>NucletTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 01.00.00
 */
public class NucletExplorerNode extends ExplorerNode<NucletTreeNode> {

	private static final Logger LOG = Logger.getLogger(NucletExplorerNode.class);
	
	public NucletExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_EXPAND;
	}

	@Override
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {

		if (transferable.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)) {
			final Object transferData = transferable.getTransferData(MasterDataIdAndEntity.dataFlavor);
			final Collection<MasterDataIdAndEntity> collimp = (Collection<MasterDataIdAndEntity>)transferData;

			Set<AbstractNucletContentEntryTreeNode> contents = new HashSet<AbstractNucletContentEntryTreeNode>();
			for (MasterDataIdAndEntity mdiden : collimp) {
				NuclosEntity entity = NuclosEntity.getByName(mdiden.getEntity());
				if (entity == null) {
					return false;
				}
				Long eoid = ((Integer)mdiden.getId()).longValue();
				if (eoid >= 0) {
					contents.add(getTreeNodeFacade().getNucletContentEntryNode(entity, eoid));
				}
			}
			if (!contents.isEmpty()) {
				try {
					getTreeNodeFacade().addNucletContents(getTreeNode().getId().longValue(), contents);
					refresh(tree);
				} catch(Exception e) {
					Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
				}
			}

			return true;
		} else {
			return super.importTransferData(parent, transferable, tree);
		}
	}

	@Override
	protected void cmdShowInOwnTabAction() {
		NucletTreeNode node = getTreeNode();
		getExplorerController().cmdShowInOwnTab(new NucletTreeNode(node.getId(), node.getLabel(), node.getDescription(), false));
	}

	private TreeNodeFacadeRemote getTreeNodeFacade() throws NuclosFatalException {
		return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
	}
}

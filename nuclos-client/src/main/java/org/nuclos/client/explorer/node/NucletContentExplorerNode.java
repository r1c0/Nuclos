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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentTreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>NucletContentTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 01.00.00
 */
public class NucletContentExplorerNode extends ExplorerNode<NucletContentTreeNode> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(NucletContentExplorerNode.class);

	public NucletContentExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		final TreePath treePath = new TreePath(NucletContentExplorerNode.this);
		return tree.isCollapsed(treePath) ? ACTIONCOMMAND_EXPAND : ACTIONCOMMAND_COLLAPSE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importTransferData(Component parent,	Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {

		if (transferable.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)) {
			final Object transferData = transferable.getTransferData(MasterDataIdAndEntity.dataFlavor);
			final Collection<MasterDataIdAndEntity> collimp;
			if (transferData instanceof Collection) {
				collimp = (Collection<MasterDataIdAndEntity>)transferData;
			} else {
				collimp = Collections.singletonList((MasterDataIdAndEntity) transferData);
			}

			Map<AbstractNucletContentEntryTreeNode, MasterDataIdAndEntity> contents = new HashMap<AbstractNucletContentEntryTreeNode, MasterDataIdAndEntity>();
			for (MasterDataIdAndEntity mdiden : collimp) {
				if (getTreeNode().getEntity().getEntityName().equals(mdiden.getEntity())) {
					Long eoid = (mdiden.getId() instanceof Long) ? (Long) mdiden.getId() : ((Integer)mdiden.getId()).longValue();
					if (eoid >= 0) {
						contents.put(getTreeNodeFacade().getNucletContentEntryNode(getTreeNode().getEntity(), eoid),
							mdiden);
					}
				}
			}
			if (!contents.isEmpty()) {
				try {
					for (AbstractNucletContentEntryTreeNode node : contents.keySet()) {
						if(getTreeNodeFacade().removeNucletContents(Collections.singleton(node))) {
							contents.get(node).removeFromSourceTree();
						}
					}
					getTreeNodeFacade().addNucletContents(getTreeNode().getNucletId(), new HashSet<AbstractNucletContentEntryTreeNode>(contents.keySet()));
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
	public Icon getIcon() {
		Integer resId = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntity()).getResourceId();
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntity()).getNuclosResource();
		if(resId != null) {
			ImageIcon standardIcon = ResourceCache.getIconResource(resId);
			return MainFrame.resizeAndCacheTabIcon(standardIcon);
		} else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return Icons.getInstance().getIconGenericObject16();
	}

	private TreeNodeFacadeRemote getTreeNodeFacade() throws NuclosFatalException {
		return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
	}
}

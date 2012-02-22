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
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;

import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> presenting a generic object group.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GroupExplorerNode extends MasterDataExplorerNode<GroupTreeNode> {

	public GroupExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		return null;
		/** @todo return group icon */
	}

	@Override
	public boolean importTransferData(final Component parent, Transferable transferable, final JTree tree)
			throws IOException, UnsupportedFlavorException {
		final List<?> lstloim = (List<?>) transferable.getTransferData(TransferableGenericObjects.dataFlavor);

		UIUtils.runCommand(parent, new Runnable() {
			@Override
			public void run() {
				try {
					/** @todo a progress bar would be nice here (see below) */
					for (Iterator<?> iter = lstloim.iterator(); iter.hasNext();) {
						final Object obj = iter.next();
						if (obj instanceof GenericObjectIdModuleProcess) {
							final GenericObjectIdModuleProcess goimp = (GenericObjectIdModuleProcess) obj;
							GenericObjectDelegate.getInstance().addToGroup(
									goimp.getGenericObjectId(), getTreeNode().getId().intValue(), true);
						}
						else {
							throw new NuclosBusinessException(
									getSpringLocaleDelegate().getMessage("GroupExplorerNode.1", "Der Datentransfer wird nicht unterst\u00fctzt."));
						}
					}

					refresh(tree);
				}
				catch (/* CommonBusiness */ Exception ex) {
					Errors.getInstance().showExceptionDialog(parent, ex);
				}
			}
		});

		return true;
	}
}	// class GroupExplorerNode

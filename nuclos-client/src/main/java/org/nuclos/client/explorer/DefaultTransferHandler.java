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
package org.nuclos.client.explorer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;

public class DefaultTransferHandler extends TransferHandler {

	private static final Logger LOG = Logger.getLogger(DefaultTransferHandler.class);
	
	private final Component parent;
	private boolean result = false;

	public DefaultTransferHandler(Component parent) {
		this.parent = parent;
	}

	@Override
	public int getSourceActions(JComponent comp) {
		int result = NONE;
		if (comp instanceof JTree) {
			final JTree tree = (JTree) comp;
			ExplorerNode<?> explorerNode = this.getSelectedTreeNode(tree);
			result = (explorerNode == null) ? NONE : explorerNode.getDataTransferSourceActions();
		}
		return result;
	}

	@Override
	protected Transferable createTransferable(JComponent comp) {
		Transferable result = null;
		if (comp instanceof JTree) {
//			final JTree tree = (JTree) comp;
//			ExplorerNode explorerNode = this.getSelectedTreeNode(tree);
//			result = (explorerNode == null) ? null : explorerNode.createTransferable();
//		}
		//Task TA0903#0158
			final JTree tree = (JTree)comp;

			List<ExplorerNode<?>> lsExplorerNodes = ExplorerController.getSelectedExplorerNodes(tree);
			int iSelectionCount = lsExplorerNodes.size();

			if (iSelectionCount == 1) {
				result = this.getSelectedTreeNode(tree).createTransferable(tree);
			}
			else if (iSelectionCount > 1) {
				// note: this works only for genericobject not for masterdata explorer nodes
				final List<GenericObjectIdModuleProcess> lstloimp = new ArrayList<GenericObjectIdModuleProcess>(iSelectionCount);
				for (ExplorerNode<?> explorerNode : lsExplorerNodes) {
					if (explorerNode instanceof GenericObjectExplorerNode) {
						final GenericObjectTreeNode lotreenode = (GenericObjectTreeNode)explorerNode.getTreeNode();
						final GenericObjectIdModuleProcess loimp = new GenericObjectIdModuleProcess(lotreenode.getId(),
								lotreenode.getModuleId(), lotreenode.getProcessId(), lotreenode.getLabel());
						lstloimp.add(loimp);
					}
				}
				if (!lstloimp.isEmpty()) {
					result = new TransferableGenericObjects(lstloimp);
				}
			}
		}
		return result;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] aflavors) {
		// Unfortunately, this method is not called for each node, so we only can say yes or no
		// for the whole tree here. We must say yes to enable drop at all.

		return true;

//		class IsIdOrCVO implements CollectionUtils.UnaryPredicate {
//			public boolean evaluate(Object o) {
//				final DataFlavor flavor = (DataFlavor) o;
//				return (flavor instanceof GenericObjectIdModuleProcess.DataFlavor);
//			}
//		}
//		final Object oFlavor = CollectionUtils.findFirst(Arrays.asList(aflavors), new IsIdOrCVO());
//
//		return (oFlavor != null);
	}

	@Override
	public boolean importData(JComponent comp, final Transferable transferable) {
		result = false;

		if (comp instanceof JTree) {
			final JTree tree = (JTree) comp;
			final JTree.DropLocation dl = tree.getDropLocation();

			CommonClientWorkerAdapter<Collectable> importDataWorker = new CommonClientWorkerAdapter<Collectable>(null) {
				@Override
				public void init() {
					UIUtils.setWaitCursor(tree);
				}

				@Override
				public void work() {
					importTransferData(tree, dl, transferable);
				}

				@Override
				public void paint() {
					tree.setCursor(null);
				}

				@Override
				public void handleError(Exception ex) {
					//log.error(ex);
				}
			};

			CommonMultiThreader.getInstance().execute(importDataWorker);
		}

		return result;
	}

	private void importTransferData(JTree tree, JTree.DropLocation dropLoc, Transferable transferable) {
			try {
				// NUCLEUSINT-670: use the drop target, not the selected path
				TreePath targetPath = null;
				if(dropLoc != null && dropLoc != null)
					targetPath = dropLoc.getPath();
				if(targetPath == null)
					targetPath = tree.getSelectionPath();
				if(targetPath == null)
					return;
				ExplorerNode<?> targetNode = (ExplorerNode<?>) targetPath.getLastPathComponent();
				result = targetNode.importTransferData(parent, transferable, tree);
			}
			catch (UnsupportedFlavorException ex) {
				UIUtils.invokeOnDispatchThread(new Runnable() {
					@Override
					public void run() {
						try {
							JOptionPane.showMessageDialog(parent, CommonLocaleDelegate.getMessage(
									"ExplorerController.16","Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt."));
						}
						catch (Exception e) {
							LOG.error("DefaultTransferHandler.importTransferData: " + e, e);
						}            		
					}
				});
			}
			catch (IOException ex) {
				throw new CommonFatalException(ex);
			}
		}

	private ExplorerNode<?> getSelectedTreeNode(final JTree tree) {
		TreePath treePath = tree.getSelectionPath();
		return (treePath == null) ? null : (ExplorerNode<?>) treePath.getLastPathComponent();
	}
}

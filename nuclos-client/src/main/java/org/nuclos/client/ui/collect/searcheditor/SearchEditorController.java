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
package org.nuclos.client.ui.collect.searcheditor;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Controller for the search editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class SearchEditorController {

	private final Component parent;
	private final CollectableEntity clcteRoot;
	private final TransferHandler transferhandler;
	private final SearchEditorPanel pnl;
	private final CollectableFieldsProviderFactory clctfproviderfactory;
	private final Collection<CollectableEntityField> additionalFields;

	/**
	 * @param parent
	 * @param pnl
	 * @param clcteRoot
	 * @param clctfproviderfactory used for displaying the list of possible values for the respective entity field in the atomic node panel.
	 */
	public SearchEditorController(Component parent, SearchEditorPanel pnl, CollectableEntity clcteRoot,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		this.parent = parent;
		this.pnl = pnl;
		this.clcteRoot = clcteRoot;
		this.clctfproviderfactory = clctfproviderfactory;
		this.transferhandler = new TransferHandler(parent);
		this.additionalFields = additionalFields;
		this.setupSearchEditorPanel();
	}

	public Component getParent() {
		return parent;
	}

	private void setupSearchEditorPanel() {
		// enable drag (as in "drag&drop"):
		setupDataTransfer(pnl.getTree());
		addMouseListenerTo(pnl.getTree());

		pnl.getTree().setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object oValue, boolean bSelected, boolean bExpanded,
					boolean bLeaf, int iRow, boolean bHasFocus) {
				final JLabel result = (JLabel) super.getTreeCellRendererComponent(tree, oValue, bSelected, bExpanded, bLeaf, iRow,
						bHasFocus);
				final SearchConditionTreeNode node = (SearchConditionTreeNode) oValue;
				result.setText(node.getLabel(bExpanded));
				return result;
			}
		});

		pnl.getSimplifyButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				pnl.setSearchCondition(SearchConditionUtils.simplified(pnl.getSearchCondition()));
			}
		});
	}

	/**
	 * enables drag&drop, copy&paste
	 * @param tree
	 */
	private void setupDataTransfer(final JTree tree) {
		tree.setDragEnabled(true);
		tree.setTransferHandler(this.transferhandler);
	}

	/**
	 * adds a mouselistener to the given <code>tree</code>,
	 * in order to enable popup menus and double click.
	 * @param tree
	 */
	private void addMouseListenerTo(final JTree tree) {
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent ev) {
				mouseEventOnNode(ev);
			}

			@Override
			public void mouseReleased(MouseEvent ev) {
				mouseEventOnNode(ev);
			}

			@Override
			public void mouseClicked(MouseEvent ev) {
				mouseEventOnNode(ev);
			}

			private void mouseEventOnNode(MouseEvent ev) {
				final int selRow = tree.getRowForLocation(ev.getX(), ev.getY());
				final TreePath treepath = tree.getPathForLocation(ev.getX(), ev.getY());
				if (selRow != -1) {
					SearchEditorController.this.mouseEventOnNode(treepath, ev);
				}
			}
		});
	}

	/**
	 * event: a mouse event occured on a node in the <code>view</code>
	 * @param selPath the path of the node where the mouse event occured.
	 * @param ev
	 */
	private void mouseEventOnNode(TreePath selPath, MouseEvent ev) {
		final SearchConditionTreeNode node = (SearchConditionTreeNode) selPath.getLastPathComponent();
		final JTree tree = (JTree) ev.getComponent();

		// select the node:
		tree.setSelectionPath(selPath);
		
		if (ev.isPopupTrigger()) {
			// show popup menu:
			final JPopupMenu popupMenu = this.getPopupMenu(node, tree);
			if (popupMenu != null) {
				popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
			}
		}
		else if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
			if (ev.getButton() == MouseEvent.BUTTON1) {
				if (ev.getClickCount() == 2) {
					if (this.clctfproviderfactory == null) {
						throw new IllegalStateException("No CollectableFieldsProviderFactory was defined for the search editor.");
					}
					// perform the node's default action:
					final Action actDefault = node.getDefaultTreeNodeAction(tree, this.clcteRoot, this.clctfproviderfactory, this.additionalFields);
					if (actDefault != null) {
						actDefault.actionPerformed(null);
					}
				}
			}
		}
	}

	/**
	 * @param treenode
	 * @param tree the tree that is to contain the popup menu
	 * @return the popupmenu, if any, for the given treenode
	 * @precondition treenode != null
	 */
	private JPopupMenu getPopupMenu(SearchConditionTreeNode treenode, JTree tree) {
		if (treenode == null) {
			throw new NullArgumentException("treenode");
		}
		if (this.clctfproviderfactory == null) {
			throw new IllegalStateException("No CollectableFieldsProviderFactory was defined for the search editor.");
		}

		final List<TreeNodeAction> lstaction = treenode.getTreeNodeActions(tree, this.clcteRoot, this.clctfproviderfactory, this.additionalFields);
		final JPopupMenu result = lstaction.isEmpty() ? null : new JPopupMenu();
		if (result != null) {
			final String sDefaultTreeNodeActionCommand = treenode.getDefaultTreeNodeActionCommand();
			for (TreeNodeAction action : lstaction) {
				if (action == null) {
					Logger.getLogger(SearchEditorController.class).warn("exploreraction == null");
				}
				else {
					final boolean bDefault = (sDefaultTreeNodeActionCommand != null) && sDefaultTreeNodeActionCommand.equals(action.getValue(Action.ACTION_COMMAND_KEY));
					action.addToMenu(result, bDefault);
				}
			}
		}
		return result;
	}

	/**
	 * inner class TransferHandler. Handles drag&drop, copy&paste for the tree.
	 */
	private class TransferHandler extends javax.swing.TransferHandler {

		private final Component parent;
		/**
		 * the source of the cut/copy or drag operation.
		 */
		private SearchConditionTreeNode sourcenode;

		public TransferHandler(Component parent) {
			this.parent = parent;
		}

		@Override
		public int getSourceActions(JComponent comp) {
			int result = NONE;
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				SearchConditionTreeNode explorerNode = this.getSelectedTreeNode(tree);
				result = (explorerNode == null) ? NONE : explorerNode.getDataTransferSourceActions();
			}
			return result;
		}

		@Override
		protected Transferable createTransferable(JComponent comp) {
			Transferable result = null;
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				sourcenode = this.getSelectedTreeNode(tree);
				result = (sourcenode == null) ? null : sourcenode.createTransferable();
			}
			return result;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] aflavors) {
			// Unfortunately, this method is not called for each node, so we only can say yes or no
			// for the whole tree here. We must say yes to enable drop at all.

			return true;
		}

		@Override
		public boolean importData(JComponent comp, Transferable transferable) {
			boolean result = false;

			if (comp instanceof JTree) {
				JTree tree = (JTree) comp;

				try {
					result = this.getSelectedTreeNode(tree).importTransferData(parent, transferable, tree);
				}
				catch (UnsupportedFlavorException ex) {
					JOptionPane.showMessageDialog(parent, "Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt.");
				}
				catch (IOException ex) {
					throw new CommonFatalException(ex);
				}
			}

			return result;
		}

		@Override
		protected void exportDone(JComponent compSource, Transferable transferable, int action) {
			if (action == MOVE && sourcenode != null) {
				sourcenode.removeFromParentAndRefresh((JTree) compSource);
			}
			sourcenode = null;
		}

		private SearchConditionTreeNode getSelectedTreeNode(final JTree tree) {
			TreePath treePath = tree.getSelectionPath();
			return (treePath == null) ? null : (SearchConditionTreeNode) treePath.getLastPathComponent();
		}

	}	// inner class TransferHandler

}	// class SearchEditorController

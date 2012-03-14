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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.rule.AbstractRuleTreeNode;
import org.nuclos.client.explorer.node.rule.CodeTreeNode;
import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode.EntityRuleUsageProcessNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode.EntityRuleUsageStatusNode;
import org.nuclos.client.explorer.node.rule.LibraryTreeNode;
import org.nuclos.client.explorer.node.rule.RuleTreeModel;
import org.nuclos.client.explorer.node.rule.TimelimitNode;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.ChainedTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> representing a node in the Ruletree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */
public abstract class AbstractRuleExplorerNode extends ExplorerNode<AbstractRuleTreeNode> {

	public AbstractRuleExplorerNode(TreeNode treenode) {
		super(treenode);
	}
	
	@Override
	public void refresh(JTree tree) throws CommonFinderException {
		TreeNode tn = getTreeNode();
		if (tn instanceof EntityRuleUsageProcessNode)
			((ExplorerNode<?>)getParent().getParent()).refresh(tree);
		if (tn instanceof EntityRuleUsageStatusNode)
			((ExplorerNode<?>)getParent().getParent().getParent()).refresh(tree);
	
		super.refresh(tree);
	}

	@Override
	public boolean getAllowsChildren() {
		return isLeaf() ? false : super.getAllowsChildren();
	}
	
	@Override
	public boolean importTransferData(Component parent,
			Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {

		boolean result = false;

		if (getTreeNode().isInsertRuleAllowd()
				|| (this.getParent() != null
				&& ((AbstractRuleExplorerNode) this.getParent()).getTreeNode().isInsertRuleAllowd())) {

			final RuleAndRuleUsageEntity.RuleUsageDataFlavor dataflavorRule = new RuleAndRuleUsageEntity.RuleUsageDataFlavor();

			if (transferable.isDataFlavorSupported(dataflavorRule)) {

				final List<RuleAndRuleUsageEntity> ruleUsageEntity = (List<RuleAndRuleUsageEntity>) transferable.getTransferData(dataflavorRule);
				result = insertRule(ruleUsageEntity, parent, tree);
			}
			else {
				throw new UnsupportedFlavorException(null);
			}
		}
		else {
			throw new UnsupportedFlavorException(null);
		}

		return result;
	}

	/**
	 * Add the rule defined by the transferable into the RuleTree
	 * @param parent
	 * @param tree
	 */
	private boolean insertRule(final List<RuleAndRuleUsageEntity>	ruleUsageEntityList,
			final Component parent, final JTree tree) {

		// is insert in this node allowed?
		if (getTreeNode().isInsertRuleAllowd()) {

			UIUtils.runCommand(parent, new CommonRunnable() {
				@Override
				public void run() throws CommonFinderException {
					try {
						getTreeNode().insertRule(ruleUsageEntityList, null);
						refresh(tree);
					}
					catch (CommonBusinessException ex) {
						Errors.getInstance().showExceptionDialog(parent, 
								getSpringLocaleDelegate().getMessage(
										"AbstractRuleExplorerNode.3","Regel kann nicht eingef\u00fcgt werden. Evtl. ist die Regel schon vorhanden") + ".", ex);
						refresh(tree);
					}
				}
			});
		}
		else {
			// insert into parent
			UIUtils.runCommand(parent, new CommonRunnable() {
				@Override
				public void run() throws CommonFinderException {
					try {
						((AbstractRuleExplorerNode) getParent()).getTreeNode().insertRule(ruleUsageEntityList, getTreeNode().getRuleEntity().getRuleVo());
						refreshParent(tree);
					}
					catch (CommonBusinessException ex) {
						Errors.getInstance().showExceptionDialog(parent, 
								getSpringLocaleDelegate().getMessage(
										"AbstractRuleExplorerNode.4","Regel kann nicht eingef\u00fcgt werden. Evtl. ist die Regel schon vorhanden") + ".", ex);
						refreshParent(tree);
					}
				}
			});
		}
		return false;
	}

	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 */
	public void refreshParent(JTree tree) throws CommonFinderException {
		((ExplorerNode<?>) this.getParent()).refresh(tree);
	}

	protected boolean isRefreshAvailable() {
		return true;
	}

	/**
	 * @param tree the tree where the action is about to take place.
	 * @return the list of possible <code>TreeNodeAction</code>s for this node.
	 * These may be shown in the node's context menu.
	 * Separators are shown in the menus for <code>null</code> entries.
	 */
	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();

		if (isRefreshAvailable()) {
			result.add(new RefreshAction(tree));
			result.addAll(this.getExpandCollapseActions(tree));
		} else {
			result.add(TreeNodeAction.newSeparatorAction());
		}

		final TreeNodeAction exploreractCopy = new ChainedTreeNodeAction(ACTIONCOMMAND_COPY, 
				getSpringLocaleDelegate().getMessage("AbstractRuleExplorerNode.2","Kopieren"),
				TransferHandler.getCopyAction(), tree);
		result.add(exploreractCopy);

		final TreeNodeAction exploreractPaste = new ChainedTreeNodeAction(ACTIONCOMMAND_PASTE, 
				getSpringLocaleDelegate().getMessage("AbstractRuleExplorerNode.1","Einf\u00fcgen"),
				TransferHandler.getPasteAction(), tree);
		result.add(exploreractPaste);

		// enable "copy" action according to the tree's TransferHandler:
		exploreractCopy.setEnabled((tree.getTransferHandler().getSourceActions(tree) & TransferHandler.COPY) != 0);

		// enable "paste" action according to the tree's TransferHandler:
		// This is hard because TransferHandler.canImport is not called every time the selection changes.
		// Workaround: call canImport anyway to reduce bad paste actions:
		exploreractPaste.setEnabled(getTreeNode().isInsertRuleAllowd());

		return result;
	}

	/**
	 * Expend the node if it or one of its childs contain the rule
	 * @param ruleIdToGoto
	 * @param jTree
	 */
	public void expandToRuleWithId(Integer ruleIdToGoto, JTree jTree) throws CommonFinderException {
		if (getTreeNode().getRuleEntity() != null &&
				getTreeNode().getRuleEntity().getRuleVo().getId().equals(ruleIdToGoto)) {
			jTree.setSelectionPath(new TreePath(this.getPath()));
			jTree.scrollPathToVisible(new TreePath(this.getPath()));
		}
		else if (getTreeNode() instanceof DirectoryRuleNode) {
			if (((DirectoryRuleNode) getTreeNode()).isRoot()) {
				this.refresh(jTree);
				for (int i = 0; i < getChildCount(); i++) {
					final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
					if (childNode.getLabel().equals(RuleTreeModel.ALL_RULES_NODE_LABEL)) {
						childNode.expandToRuleWithId(ruleIdToGoto, jTree);
					}
				}
			}
			else {
				if (getTreeNode().getLabel().equals(RuleTreeModel.ALL_RULES_NODE_LABEL)) {
					this.refresh(jTree);
					for (int i = 0; i < getChildCount(); i++) {
						final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
						if (childNode.getTreeNode().getRuleEntity().getRuleVo() != null &&
								childNode.getTreeNode().getRuleEntity().getRuleVo().getId().equals(ruleIdToGoto)) {
							childNode.expandToRuleWithId(ruleIdToGoto, jTree);
						}
					}
				}
			}
		}
	}

	public void expandToTimelimitRuleWithId(Integer ruleIdToGoto, JTree jTree) throws CommonFinderException {
		if (getTreeNode().getRuleEntity() != null &&
				getTreeNode().getRuleEntity().getRuleVo().getId().equals(ruleIdToGoto)) {
			jTree.setSelectionPath(new TreePath(this.getPath()));
			jTree.scrollPathToVisible(new TreePath(this.getPath()));
		}
		else if (getTreeNode() instanceof DirectoryRuleNode) {
			if (((DirectoryRuleNode) getTreeNode()).isRoot()) {
				this.refresh(jTree, true);
				for (int i = 0; i < getChildCount(); i++) {
					final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
					if (childNode.getLabel().equals(RuleTreeModel.FRIST_NODE_LABEL)) {
						childNode.expandToTimelimitRuleWithId(ruleIdToGoto, jTree);
					}
				}
			}
		}
		else if (getTreeNode() instanceof TimelimitNode) {
			if (getTreeNode().getLabel().equals(RuleTreeModel.FRIST_NODE_LABEL)) {
				this.refresh(jTree);
				for (int i = 0; i < getChildCount(); i++) {
					final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
					if (childNode.getTreeNode().getRuleEntity().getRuleVo() != null &&
							childNode.getTreeNode().getRuleEntity().getRuleVo().getId().equals(ruleIdToGoto)) {
						childNode.expandToTimelimitRuleWithId(ruleIdToGoto, jTree);
					}
				}
			}
		}
	}

	public void expandToLibraryRuleWithId(Integer ruleIdToGoto, JTree jTree) throws CommonFinderException {
		if (getTreeNode() instanceof CodeTreeNode) {
			CodeTreeNode ctn = (CodeTreeNode) getTreeNode();
			if (ctn.getCodeVO().getId().equals(ruleIdToGoto)) {
				jTree.setSelectionPath(new TreePath(this.getPath()));
				jTree.scrollPathToVisible(new TreePath(this.getPath()));
			}
		}
		else if (getTreeNode() instanceof DirectoryRuleNode) {
			if (((DirectoryRuleNode) getTreeNode()).isRoot()) {
				this.refresh(jTree);
				for (int i = 0; i < getChildCount(); i++) {
					final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
					if (childNode.getLabel().equals(RuleTreeModel.LIBRARY_LABEL)) {
						childNode.expandToLibraryRuleWithId(ruleIdToGoto, jTree);
					}
				}
			}
		}
		else if (getTreeNode() instanceof LibraryTreeNode) {
			if (getTreeNode().getLabel().equals(RuleTreeModel.LIBRARY_LABEL)) {
				this.refresh(jTree);
				for (int i = 0; i < getChildCount(); i++) {
					final AbstractRuleExplorerNode childNode = (AbstractRuleExplorerNode) getChildAt(i);
					if (childNode.getTreeNode() instanceof CodeTreeNode) {
						CodeTreeNode ctn = (CodeTreeNode) childNode.getTreeNode();
						if (ctn.getCodeVO().getId().equals(ruleIdToGoto)) {
							childNode.expandToLibraryRuleWithId(ruleIdToGoto, jTree);
						}
					}
				}
			}
		}
	}
}

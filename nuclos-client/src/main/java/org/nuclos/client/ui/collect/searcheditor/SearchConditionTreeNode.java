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

import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.CompositeTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ToHumanReadablePresentationVisitor;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collect.collectable.searchcondition.visit.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common2.CommonLocaleDelegate;


import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.lang.NullArgumentException;


/**
 * <code>TreeNode</code> that contains a <code>CollectableSearchCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public abstract class SearchConditionTreeNode extends DefaultMutableTreeNode {

	protected static final String ACTIONCOMMAND_ADD_ATOMICNODE = "ADD ATOMIC NODE";

	/**
	 * @param clctcond
	 * @precondition clctcond != null
	 * @return
	 * @postcondition result != null
	 */
	public static SearchConditionTreeNode newInstance(CollectableSearchCondition clctcond) {
		if (clctcond == null) {
			throw new NullArgumentException("clctcond");
		}
		final SearchConditionTreeNode result = clctcond.accept(new NewSearchConditionTreeNodeVisitor());

		assert result != null;
		return result;
	}

	public abstract CollectableSearchCondition getSearchCondition();

	@Override
	public Object getUserObject() {
		return this.getSearchCondition();
	}

	@Override
	public String toString() {
		return this.getLabelForCollapsedState();
	}

	public String getLabel(boolean bExpanded) {
		return bExpanded ? this.getLabelForExpandedState() : this.getLabelForCollapsedState();
	}

	protected String getLabelForCollapsedState() {
		return this.getSearchCondition().accept(new ToHumanReadablePresentationVisitor());
	}

	protected abstract String getLabelForExpandedState();

	@Override
	public SearchConditionTreeNode getChildAt(int iIndex) {
		return (SearchConditionTreeNode) super.getChildAt(iIndex);
	}

	/**
	 * @param tree the tree where the action is about to take place.
	 * @param clcteRoot the <code>CollectableEntity</code> for the whole search condition.
	 * @param clctfproviderfactory
	 * @return the list of possible <code>TreeNodeAction</code>s for this node.
	 * These may be shown in the node's context menu.
	 * Separators are shown in the menus for <code>null</code> entries.
	 * @postcondition result != null
	 */
	public final List<TreeNodeAction> getTreeNodeActions(JTree tree, CollectableEntity clcteRoot,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		final List<TreeNodeAction> result = this._getTreeNodeActions(tree, this.getEntity(clcteRoot), clctfproviderfactory, additionalFields);
		assert result != null;
		return result;
	}

	/**
	 * @param clcteRoot
	 * @return the entity for this treenode
	 */
	private CollectableEntity getEntity(CollectableEntity clcteRoot) {
		final CollectableEntity clcteEnclosing = getEnclosingEntity(this);
		return (clcteEnclosing == null) ? clcteRoot : clcteEnclosing;
	}

	/**
	 * @param treenode
	 * @return the enclosing entity, if any.
	 */
	private static CollectableEntity getEnclosingEntity(SearchConditionTreeNode treenode) {
		final CollectableEntity result;
		if (treenode == null) {
			result = null;
		}
		else if (treenode instanceof SubConditionTreeNode) {
			/** @todo can we encapsulate these getXxxEntity() methods in an interface? */
			result = ((SubConditionTreeNode) treenode).getSubEntity();
		}
		else if (treenode instanceof ReferencingSearchConditionTreeNode) {
			result = ((ReferencingSearchConditionTreeNode) treenode).getReferencedEntity();
		}
		else {
			result = getEnclosingEntity((SearchConditionTreeNode) treenode.getParent());
		}
		return result;
	}

	/**
	 * @param tree the tree where the action is about to take place.
	 * @param clcte	the <code>CollectableEntity</code> for this treenode.
	 * @param clctfproviderfactory
	 * @return the list of possible <code>TreeNodeAction</code>s for this node.
	 * @postcondition result != null
	 * These may be shown in the node's context menu.
	 * Separators are shown in the menus for <code>null</code> entries.
	 */
	protected List<TreeNodeAction> _getTreeNodeActions(JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		return Collections.emptyList();
	}

	/**
	 * There is no default action by default. Subclasses may specify the default action here.
	 * @return the action command of the default <code>TreeNodeAction</code> for this node, if any.
	 */
	public String getDefaultTreeNodeActionCommand() {
		return null;
	}

	/**
	 * finds the default action for this node by searching the tree actions
	 * (as specified in <code>getExplorerActions()</code>) for a tree action with the default tree action
	 * command (as specified in <code>getDefaultExplorerActionCommand()</code>). Note that this method
	 * doesn't have to be overridden in subclasses.
	 * @param tree the tree where the action is about to take place.
	 * @param clcteRoot the <code>CollectableEntity</code> for the whole search condition.
	 * @return the default action for this node, if any.
	 */
	public TreeNodeAction getDefaultTreeNodeAction(JTree tree, CollectableEntity clcteRoot,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		TreeNodeAction result = null;

		final String sDefaultTreeActionCommmand = this.getDefaultTreeNodeActionCommand();
		if (sDefaultTreeActionCommmand != null) {
			for (TreeNodeAction action : this.getTreeNodeActions(tree, clcteRoot, clctfproviderfactory, additionalFields))
			{
				if (action != null) {
					if (sDefaultTreeActionCommmand.equals(action.getValue(Action.ACTION_COMMAND_KEY))) {
						result = action;
						break;
					}
				}
			}
		}
		return result;
	}

	protected void refresh(JTree tree) {
		this.refresh((DefaultTreeModel) tree.getModel());
	}

	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 */
	protected void refresh(DefaultTreeModel dtm) {
		dtm.nodeStructureChanged(this);
	}

	public void removeFromParentAndRefresh(JTree tree) {
		final SearchConditionTreeNode nodeParent = (SearchConditionTreeNode) SearchConditionTreeNode.this.getParent();
		if (parent != null) {
			nodeParent.remove(SearchConditionTreeNode.this);
			nodeParent.refresh(tree);
		}
	}

	/**
	 * @param tree
	 * @param clcte
	 * @return composite actions that contains actions to create the different nodes.
	 */
	protected CompositeTreeNodeAction getAddTreeNodeActions(JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		return new CompositeTreeNodeAction(CommonLocaleDelegate.getMessage("SearchConditionTreeNode.1","Hinzuf\u00fcgen"), Arrays.asList(new TreeNodeAction[] {
				new AddAtomicNodeAction(tree, clcte, clctfproviderfactory, additionalFields), TreeNodeAction.newSeparatorAction(),
				new AddCompositeNodeAction(LogicalOperator.AND, tree),
				new AddCompositeNodeAction(LogicalOperator.OR, tree),
				new AddCompositeNodeAction(LogicalOperator.NOT, tree)
		}));
	}

	/**
	 * source actions for drag&drop.
	 * @see java.awt.dnd.DnDConstants
	 * @return (default: DnDConstants.ACTION_NONE)
	 */
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_NONE;
	}

	/**
	 * @return a <code>Transferable</code> for data transfer
	 */
	public final Transferable createTransferable() {
		return this.getSearchCondition();
	}

	/**
	 * imports data from a drop or paste operation. The default implementation throws an
	 * UnsupportedFlavorException to indicate that drag&drop is not supported by default.
	 * @see javax.swing.TransferHandler#importData
	 * @param transferable
	 * @param tree
	 * @return Was the data imported? <code>false</code> may be returned if the drop action requires
	 * additional user input, and the user cancels the operation.
	 * @throws java.awt.datatransfer.UnsupportedFlavorException
	 */
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {
		throw new UnsupportedFlavorException(null);
	}

	protected class RemoveNodeAction extends TreeNodeAction {

		RemoveNodeAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE, CommonLocaleDelegate.getMessage("SearchConditionTreeNode.2","Entfernen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			SearchConditionTreeNode.this.removeFromParentAndRefresh(this.getJTree());
		}
	}

	protected class AddCompositeNodeAction extends TreeNodeAction {
		private final LogicalOperator logicalOperator;

		AddCompositeNodeAction(LogicalOperator logicalOperator, JTree tree) {
			super("ADD LOGICAL " + CommonLocaleDelegate.getMessage(logicalOperator.getResourceIdForLabel(), null)
				+ " NODE", CommonLocaleDelegate.getMessage(logicalOperator.getResourceIdForDescription(), null), tree);
			this.logicalOperator = logicalOperator;
		}

		public LogicalOperator getLogicalOperator() {
			return this.logicalOperator;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			add(new CompositeSearchConditionTreeNode(new CompositeCollectableSearchCondition(this.logicalOperator)));
			refresh(this.getJTree());
		}
	}

	protected class AddAtomicNodeAction extends TreeNodeAction {

		private final CollectableEntity clcte;
		private final CollectableFieldsProviderFactory clctfproviderfactory;
		private final Collection<CollectableEntityField> additionalFields;

		AddAtomicNodeAction(JTree tree, CollectableEntity clcte,
				CollectableFieldsProviderFactory clctfproviderfactory,
				Collection<CollectableEntityField> additionalFields) {
			super(ACTIONCOMMAND_ADD_ATOMICNODE, CommonLocaleDelegate.getMessage("SearchConditionTreeNode.3","Einfache Bedingung"), tree);
			this.clcte = clcte;
			this.clctfproviderfactory = clctfproviderfactory;
			this.additionalFields = additionalFields;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			/** @todo parent == tree? */
			final Component parent = this.getJTree();
			UIUtils.runCommand(parent, new Runnable() {
				@Override
				public void run() {
					new AtomicNodeController(parent, getJTree(), clcte, clctfproviderfactory, additionalFields).runAdd(SearchConditionTreeNode.this);
				}
			});
		}
	}

	/**
	 * Visitor that creates a new <code>SearchConditionTreeNode</code> out of a <code>CollectableSearchCondition</code>.
	 */
	private static class NewSearchConditionTreeNodeVisitor implements Visitor<SearchConditionTreeNode, RuntimeException>, CompositeVisitor<SearchConditionTreeNode, RuntimeException> {
		@Override
		public AtomicSearchConditionTreeNode visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
			return new AtomicSearchConditionTreeNode(atomiccond);
		}

		@Override
		public CompositeSearchConditionTreeNode visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
			return new CompositeSearchConditionTreeNode(compositecond);
		}

		@Override
		public IdConditionTreeNode visitIdCondition(CollectableIdCondition idcond) {
			return new IdConditionTreeNode(idcond);
		}

		@Override
		public ReferencingSearchConditionTreeNode visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			return new ReferencingSearchConditionTreeNode(refcond);
		}

		@Override
		public SubConditionTreeNode visitSubCondition(CollectableSubCondition subcond) {
			return new SubConditionTreeNode(subcond);
		}

		@Override
		public SubConditionTreeNode visitPivotJoinCondition(PivotJoinCondition subcond) {
			throw new UnsupportedOperationException();
		}

		@Override
		public SubConditionTreeNode visitRefJoinCondition(RefJoinCondition subcond) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public SearchConditionTreeNode visitSelfSubCondition(CollectableSelfSubCondition subcond) {
			return subcond.getSubCondition().accept(new NewSearchConditionTreeNodeVisitor());
		}

		@Override
		public SearchConditionTreeNode visitPlainSubCondition(PlainSubCondition subcond) {
			return new PlainSubSearchConditionTreeNode(subcond);
		}

		/**
		 * @param truecond
		 * @throws IllegalArgumentException
		 */
		@Override
		public SearchConditionTreeNode visitTrueCondition(TrueCondition truecond) {
			throw new IllegalArgumentException("truecond");
		}

		@Override
        public SearchConditionTreeNode visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
			throw new IllegalArgumentException("collectableIdListCondition");
        }
	}

}	// class SearchConditionTreeNode

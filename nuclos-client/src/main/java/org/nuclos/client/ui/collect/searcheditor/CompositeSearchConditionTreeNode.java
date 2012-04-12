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

import org.nuclos.client.ui.tree.CompositeTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.searchcondition.AbstractCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common2.SpringLocaleDelegate;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;


/**
 * <code>TreeNode</code> that contains a <code>CompositeCollectableSearchCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CompositeSearchConditionTreeNode extends SearchConditionTreeNode {

	private static final String ACTIONCOMMAND_CHANGE_NODE = "CHANGE NODE";

	private LogicalOperator logicalOperator;

	public CompositeSearchConditionTreeNode(CompositeCollectableSearchCondition cond) {
		this.logicalOperator = cond.getLogicalOperator();

		// create child nodes:
		for (CollectableSearchCondition condChild : cond.getOperands()) {
			this.add(newInstance(condChild));
		}
	}

	@Override
	public CompositeCollectableSearchCondition getSearchCondition() {
		return new CompositeCollectableSearchCondition(getLogicalOperator(), getOperands());
	}

	private LogicalOperator getLogicalOperator() {
		return this.logicalOperator;
	}

	private void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	/**
	 * @return the respective search conditions of the child nodes.
	 */
	private List<CollectableSearchCondition> getOperands() {
		final List<CollectableSearchCondition> result = new LinkedList<CollectableSearchCondition>();
		for (int iChild = 0; iChild < this.getChildCount(); iChild++) {
			result.add(this.getChildAt(iChild).getSearchCondition());
		}
		return result;
	}

	/**
	 * @param tree
	 * @param clcte
	 * @param clctfproviderfactory
	 * @return
	 * @postcondition result != null
	 */
	@Override
	public List<TreeNodeAction> _getTreeNodeActions(JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super._getTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields));
		if (!result.isEmpty()) {
			result.add(TreeNodeAction.newSeparatorAction());
		}

		final CompositeTreeNodeAction actAdd = this.getAddTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields);
		actAdd.setEnabled(this.getChildCount() < this.getLogicalOperator().getMaxOperandCount());
		result.add(actAdd);
		if (!this.getLogicalOperator().equals(LogicalOperator.NOT)) {
			result.add(new ChangeNodeAction(tree, this));
		}
		result.add(new RemoveNodeAction(tree));

		result.add(TreeNodeAction.newSeparatorAction());

		result.add(TreeNodeAction.newCutAction(tree));
		result.add(TreeNodeAction.newCopyAction(tree));
		result.add(TreeNodeAction.newPasteAction(tree, this));

		assert result != null;
		return result;
	}

	@Override
	protected String getLabelForExpandedState() {
		return SpringLocaleDelegate.getInstance().getMessage(
			this.getSearchCondition().getLogicalOperator().getResourceIdForLabel(), null);
	}

	@Override
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_COPY_OR_MOVE;
	}

	@Override
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {
		final CollectableSearchCondition cond = (CollectableSearchCondition) transferable.getTransferData(
				AbstractCollectableSearchCondition.DATAFLAVOR_SEARCHCONDITION);
		this.add(newInstance(cond));
		this.refresh(tree);

		return true;
	}

	/**
	 * changes AND node into OR node and vice versa.
	 */
	private static class ChangeNodeAction extends TreeNodeAction {

		private final CompositeSearchConditionTreeNode node;

		ChangeNodeAction(JTree tree, CompositeSearchConditionTreeNode node) {
			super(ACTIONCOMMAND_CHANGE_NODE, getName(node), tree);
			this.node = node;
		}

		private static String getName(CompositeSearchConditionTreeNode node) {
			String opLabel = SpringLocaleDelegate.getInstance().getMessage(
				LogicalOperator.getComplementalLogicalOperator(node.getLogicalOperator()).getResourceIdForLabel(), null); 
			return SpringLocaleDelegate.getInstance().getMessage(
					"CompositeSearchConditionTreeNode.1", "In {0}-Knoten \u00e4ndern", opLabel);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final LogicalOperator logopComplemental = LogicalOperator.getComplementalLogicalOperator(
					node.getLogicalOperator());
			node.setLogicalOperator(logopComplemental);
			node.refresh(this.getJTree());
		}
	}
}	// class CompositeSearchConditionTreeNode

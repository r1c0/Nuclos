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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.nuclos.client.ui.tree.CompositeTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AbstractCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;

/**
 * @todo refactor with SubConditionTreeNode.
 * <code>TreeNode</code> that contains a <code>ReferencingCollectableSearchCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ReferencingSearchConditionTreeNode extends SearchConditionTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CollectableEntityField clctefReferencing;

	/**
	 * @param cond
	 * @precondition cond != null
	 */
	public ReferencingSearchConditionTreeNode(ReferencingCollectableSearchCondition cond) {
		this.clctefReferencing = cond.getReferencingField();
		this.setSubCondition(cond.getSubCondition());
	}

	public CollectableEntityField getReferencingField() {
		return this.clctefReferencing;
	}

	CollectableEntity getReferencedEntity() {
		return DefaultCollectableEntityProvider.getInstance().getCollectableEntity(this.getReferencingField().getReferencedEntityName());
	}

	@Override
	protected String getLabelForExpandedState() {
		return "REFERENZIERT " + this.getReferencedEntity().getLabel() + " MIT";
	}

	/**
	 * @return the subcondition, if any.
	 */
	private CollectableSearchCondition getSubCondition() {
		return (this.getChildCount() == 0) ? null : this.getChildAt(0).getSearchCondition();
	}

	public void setSubCondition(CollectableSearchCondition condSub) {
		this.removeAllChildren();
		if (condSub != null) {
			this.add(newInstance(condSub));
		}
	}

	@Override
	public ReferencingCollectableSearchCondition getSearchCondition() {
		return new ReferencingCollectableSearchCondition(this.getReferencingField(), this.getSubCondition());
	}

	/**
	 * @param tree
	 * @param clcte
	 * @param clctfproviderfactory
	 * @return
	 * @postcondition result != null
	 */
	@Override
	protected List<TreeNodeAction> _getTreeNodeActions(JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super._getTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields));
		if (!result.isEmpty()) {
			result.add(TreeNodeAction.newSeparatorAction());
		}
		final CompositeTreeNodeAction actAdd = this.getAddTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields);
		actAdd.setEnabled(this.getChildCount() == 0);
		result.add(actAdd);

		result.add(new RemoveNodeAction(tree));

		result.add(TreeNodeAction.newSeparatorAction());

		result.add(TreeNodeAction.newCutAction(tree));
		result.add(TreeNodeAction.newCopyAction(tree));
		result.add(TreeNodeAction.newPasteAction(tree, this));

		assert result != null;
		return result;
	}

	@Override
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_COPY_OR_MOVE;
	}

	@Override
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {
		/** @todo refactor (copied from RootNode) */
		final CollectableSearchCondition cond = (CollectableSearchCondition) transferable.getTransferData(AbstractCollectableSearchCondition.dataflavorSearchCondition);

		boolean bDoIt = false;
		if (this.getChildCount() == 0) {
			bDoIt = true;
		}
		else {
			final String sMessage = "Soll die Unterbedingung ersetzt werden?";
			final int iBtn = JOptionPane.showConfirmDialog(parent, sMessage, "Unterbedingung ersetzen",
					JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				bDoIt = true;
			}
		}
		if (bDoIt) {
			this.setSubCondition(cond);
			this.refresh(tree);
		}

		return true;
	}

}	// class ReferencingSearchConditionTreeNode

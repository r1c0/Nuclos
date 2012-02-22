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
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common2.SpringLocaleDelegate;

import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;


/**
 * <code>TreeNode</code> that contains an <code>AtomicCollectableSearchCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class AtomicSearchConditionTreeNode extends SearchConditionTreeNode {
	
	private static final Logger LOG = Logger.getLogger(AtomicSearchConditionTreeNode.class);

	protected static final String ACTIONCOMMAND_EDIT_ATOMICNODE = "EDIT ATOMIC NODE";

	private final AtomicCollectableSearchCondition atomiccond;

	/**
	 * @param atomiccond
	 * @precondition atomiccond != null
	 */
	public AtomicSearchConditionTreeNode(AtomicCollectableSearchCondition atomiccond) {
		if(atomiccond == null) {
			throw new NullArgumentException("atomiccond");
		}
		this.atomiccond = atomiccond;
	}

	/**
	 * @postcondition result != null
	 */
	@Override
	public AtomicCollectableSearchCondition getSearchCondition() {
		return this.atomiccond;
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
		result.add(new EditAtomicNodeAction(tree, clcte, clctfproviderfactory, additionalFields));
		result.add(new RemoveNodeAction(tree));

		result.add(TreeNodeAction.newSeparatorAction());

		result.add(TreeNodeAction.newCutAction(tree));
		result.add(TreeNodeAction.newCopyAction(tree));

		assert result != null;
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand() {
		return ACTIONCOMMAND_EDIT_ATOMICNODE;
	}

	/**
	 * This node cannot be expanded.
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected String getLabelForExpandedState() {
		throw new UnsupportedOperationException(SpringLocaleDelegate.getInstance().getMessage(
				"AtomicSearchConditionTreeNode.1","Ein atomarer Knoten kann nicht erweitert werden."));
	}

	@Override
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_COPY_OR_MOVE;
	}

	protected class EditAtomicNodeAction extends TreeNodeAction {

		private final CollectableEntity clcte;
		private final CollectableFieldsProviderFactory clctfproviderfactory;
		private final Collection<CollectableEntityField> additionalFields;

		EditAtomicNodeAction(JTree tree, CollectableEntity clcte,
				CollectableFieldsProviderFactory clctfproviderfactory,
				Collection<CollectableEntityField> additionalFields
		) {
			super(ACTIONCOMMAND_EDIT_ATOMICNODE, SpringLocaleDelegate.getInstance().getMessage(
					"AtomicSearchConditionTreeNode.2","Bearbeiten"), tree);
			this.clcte = clcte;
			this.clctfproviderfactory = clctfproviderfactory;
			this.additionalFields = additionalFields;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			/** @todo parent == tree? */
			final JTree parent = this.getJTree();
			UIUtils.runCommand(parent, new Runnable() {
				@Override
				public void run() {
					try {
						new AtomicNodeController(parent, getJTree(), clcte, clctfproviderfactory, additionalFields).runEdit(AtomicSearchConditionTreeNode.this);
					}
					catch (Exception e) {
						LOG.error("EditAtomicNodeAction.actionPerformed: " + e, e);
					}
				}
			});
		}
	}

}	// class AtomicSearchConditionTreeNode

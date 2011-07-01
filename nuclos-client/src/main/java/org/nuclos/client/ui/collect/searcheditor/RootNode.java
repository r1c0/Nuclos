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
import org.nuclos.common2.CommonLocaleDelegate;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;


/**
 * Root node for the search editor tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class RootNode extends SearchConditionTreeNode {

	public RootNode() {
	}

	public RootNode(CollectableSearchCondition cond) {
		this.setSearchCondition(cond);
	}

	@Override
	public CollectableSearchCondition getSearchCondition() {
		return (this.getChildCount() == 0) ? null : this.getChildAt(0).getSearchCondition();
	}

	public void setSearchCondition(CollectableSearchCondition cond) {
		this.removeAllChildren();
		if (cond != null) {
			this.add(newInstance(cond));
		}
	}

	/**
	 * @param tree
	 * @param clcte
	 * @param clctfproviderfactory
	 * @return
	 * @postcondition result != null
	 */
	@Override
	public List<TreeNodeAction> _getTreeNodeActions(final JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super._getTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields));
		if (!result.isEmpty()) {
			result.add(TreeNodeAction.newSeparatorAction());
		}

//		result.add(new TreeNodeAction("CLEAR", "Leeren", tree) {
//			public void actionPerformed(ActionEvent ev) {
//				RootNode.this.setSearchCondition(null);
//				RootNode.this.refresh(tree);
//			}
//		});

		final CompositeTreeNodeAction actAdd = this.getAddTreeNodeActions(tree, clcte, clctfproviderfactory, additionalFields);
		result.add(actAdd);
		
		boolean bAtomicEnabled = (getChildCount() == 0);
		for(TreeNodeAction act : actAdd.getTreeNodeActions()) {
			if(act instanceof AddAtomicNodeAction) {
				act.setEnabled(bAtomicEnabled);
			}
		}

		result.add(TreeNodeAction.newSeparatorAction());

		result.add(TreeNodeAction.newPasteAction(tree, this));

		assert result != null;
		return result;
	}

	@Override
	protected String getLabelForExpandedState() {
		return CommonLocaleDelegate.getMessage("RootNode.1","Suchbedingung");
	}

	@Override
	protected String getLabelForCollapsedState() {
		final CollectableSearchCondition cond = this.getSearchCondition();
		return CommonLocaleDelegate.getMessage("RootNode.1","Suchbedingung")+": " + ((cond != null) ? cond.toString() : CommonLocaleDelegate.getMessage("RootNode.2","<Alle>"));
	}

	@Override
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {
		final CollectableSearchCondition cond = (CollectableSearchCondition) transferable.getTransferData(AbstractCollectableSearchCondition.dataflavorSearchCondition);

		boolean bDoIt = false;
		if (this.getChildCount() == 0) {
			bDoIt = true;
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("RootNode.3","Soll die Suchbedingung ersetzt werden?");
			final int iBtn = JOptionPane.showConfirmDialog(parent, sMessage, CommonLocaleDelegate.getMessage("RootNode.4","Suchbedingung ersetzen"),
					JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				bDoIt = true;
			}
		}
		if (bDoIt) {
			this.setSearchCondition(cond);
			this.refresh(tree);
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(MutableTreeNode newChild) {
		if(this.getChildCount() == 0) {
			super.add(newChild);
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("RootNode.5","Soll die vorhandene Suchbedingung in die neuen Bedingung eingef\u00fcgt werden?");
			final int iBtn = JOptionPane.showConfirmDialog(null, sMessage, CommonLocaleDelegate.getMessage("RootNode.6","Suchbedingung einf\u00fcgen"),
					JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				Enumeration<MutableTreeNode> enumNodes = this.children();
				if(enumNodes.hasMoreElements()) {	// Must have children here...
					MutableTreeNode node = enumNodes.nextElement();
					newChild.insert(node, 0);
					this.removeAllChildren();
				}
				this.insert(newChild, 0);
			}
		}
	}

}	// class RootNode

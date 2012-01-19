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
package org.nuclos.client.ui.collect;

import java.util.List;

import javax.swing.tree.TreeNode;

import org.nuclos.client.ui.treetable.AbstractTreeTableModel;
import org.nuclos.client.ui.treetable.TreeTableModel;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * <code>TreeTableModel</code> containing <code>Collectable</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableTreeTableModel extends AbstractTreeTableModel {

	public static final int COLUMN_TREE = 0;

	private static final Class<TreeTableModel> COLUMNCLASS_TREE = TreeTableModel.class;

	private final List<? extends CollectableEntityField> lstclctef;
	private final String sNameOfColumn0;

	/**
	 * @param root
	 * @param lstclctef List<String> specifies the fields to display as columns.
	 * @param sNameOfColumn0 the name (caption) of column 0 (where the tree is displayed)
	 */
	public CollectableTreeTableModel(TreeNode root, List<? extends CollectableEntityField> lstclctef, String sNameOfColumn0) {
		super(root);
		this.lstclctef = lstclctef;
		this.sNameOfColumn0 = sNameOfColumn0;
	}

	/**
	 * @return the number of children of <code>node</code>.
	 */
	@Override
	public int getChildCount(Object node) {
		return ((TreeNode) node).getChildCount();
	}

	/**
	 * @return the child of <code>node</code> at index <code>i</code>.
	 */
	@Override
	public TreeNode getChild(Object node, int i) {
		return ((TreeNode) node).getChildAt(i);
	}

	/**
	 * @return Is the given node a leaf?
	 */
	@Override
	public boolean isLeaf(Object node) {
		return ((TreeNode) node).isLeaf();
	}

	/**
	 * @return the number of columns.
	 */
	@Override
	public int getColumnCount() {
		return lstclctef.size() + 1;
	}

	/**
	 * @return the name for the given column.
	 */
	@Override
	public String getColumnName(int iColumn) {
		return (iColumn == COLUMN_TREE) ? sNameOfColumn0 : this.getCollectableEntityField(iColumn).getLabel();
	}

	/**
	 * @return the class for the given column.
	 */
	@Override
	public Class<?> getColumnClass(int iColumn) {
		return (iColumn == COLUMN_TREE) ? COLUMNCLASS_TREE : this.getCollectableEntityField(iColumn).getJavaClass();
	}

	private CollectableEntityField getCollectableEntityField(int iColumn) {
		return this.lstclctef.get(iColumn - 1);
	}

	private String getFieldName(int iColumn) {
		return this.getCollectableEntityField(iColumn).getName();
	}

	/**
	 * @return the value of the given node at the given column.
	 */
	@Override
	public Object getValueAt(Object oNode, int iColumn) {
		final Object result;
		final TreeNode node = (TreeNode) oNode;
		if (iColumn == 0) {
			// for column 0, the node itself must be returned
			result = node;
			/** @todo this should be part of the interface */
		}
		else {
			if (node instanceof CollectableTreeNode) {
				final CollectableTreeNode clctnode = (CollectableTreeNode) node;
				assert iColumn > 0;
				result = clctnode.getCollectable().getValue(this.getFieldName(iColumn));
			}
			else {
				// We explicitly allow for mixed tree nodes, that means: some nodes in the model may be
				// non-CollectableTreeNodes.
				result = null;
			}
		}
		return result;
	}

}	// class CollectableTreeTableModel

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

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common2.SpringLocaleDelegate;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;


/**
 * Panel for the search editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class SearchEditorPanel extends JPanel {

	private final RootNode nodeRoot = new RootNode();
	private final JTree tree = new JTree(nodeRoot);
	private final JButton btnSimplify = new JButton(SpringLocaleDelegate.getInstance().getMessage(
			"SearchEditorPanel.1","Vereinfachen"));

	public SearchEditorPanel() {
		super(new BorderLayout());
		init();
	}

	private void init() {
		final JPanel pnlTree = new JPanel(new BorderLayout());
		pnlTree.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		final JPanel pnlButtons = new JPanel();

		this.add(pnlTree, BorderLayout.CENTER);
		this.add(pnlButtons, BorderLayout.SOUTH);

		pnlTree.add(new JScrollPane(this.tree), BorderLayout.CENTER);

		pnlButtons.add(this.btnSimplify);
		this.btnSimplify.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"SearchEditorPanel.2","Suchbedingung vereinfachen"));
	}
	
	JButton getSimplifyButton() {
		return btnSimplify;
	}
	
	JTree getTree() {
		return tree;
	}

	public CollectableSearchCondition getSearchCondition() {
		return this.nodeRoot.getSearchCondition();
	}

	/**
	 * @param cond
	 * @see #setSortedSearchCondition(CollectableSearchCondition)
	 */
	public void setSearchCondition(CollectableSearchCondition cond) {
		this.nodeRoot.setSearchCondition(cond);
		this.nodeRoot.refresh(this.tree);
	}

	/**
	 * sorts the search condition by labels before setting it.
	 * @param cond is not changed.
	 */
	public void setSortedSearchCondition(CollectableSearchCondition cond) {
		this.setSearchCondition(SearchConditionUtils.sortedByLabels(cond));
	}

	public TreeModel getTreeModel() {
		return this.tree.getModel();
	}

}  // class SearchEditorPanel

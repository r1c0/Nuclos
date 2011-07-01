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
package org.nuclos.client.ui.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.nuclos.common2.LangUtils;

/**
 * Container for <code>TreeNodeAction</code>s. Uses the Composite pattern.
 * Adds itself to a menu by constructing a submenu.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CompositeTreeNodeAction extends TreeNodeAction.NoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<TreeNodeAction> lst;

	public CompositeTreeNodeAction(String sName) {
		this(sName, new LinkedList<TreeNodeAction>());
	}

	public CompositeTreeNodeAction(String sLabel, List<TreeNodeAction> lst) {
		super(sLabel);
		if (lst == null) {
			throw new IllegalArgumentException("lst");
		}
		this.lst = lst;
	}

	public List<TreeNodeAction> getTreeNodeActions() {
		return this.lst;
	}

	/**
	 * @param menu
	 * @param bDefault is ignored
	 */
	@Override
	public void addToMenu(JPopupMenu menu, boolean bDefault) {
		menu.add(newSubMenu());
	}

	/**
	 * @param menu
	 * @param bDefault is ignored
	 */
	@Override
	public void addToMenu(JMenu menu, boolean bDefault) {
		menu.add(newSubMenu());
	}

	/**
	 * @return a new submenu for this composite action, containing a menu item for each contained action.
	 */
	private JMenu newSubMenu() {
		final JMenu submenu = new JMenu(LangUtils.toString(this.getValue(Action.NAME)));
		for (Iterator<TreeNodeAction> iter = lst.iterator(); iter.hasNext();) {
			// Nodes in submenu cannot be default actions. Maybe we could allow that too.
			iter.next().addToMenu(submenu, false);
		}
		submenu.setEnabled(this.isEnabled());
		this.customizeComponent(submenu, false);

		return submenu;
	}

}  // class CompositeTreeNodeAction

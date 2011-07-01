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
package org.nuclos.client.ui;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * This is what <code>AbstractAction</code> should have been like.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class CommonAbstractAction extends AbstractAction {
	/**
	 * creates a bare action
	 */
	public CommonAbstractAction() {
		super();
	}

	/**
	 * creates an action with an icon
	 * @param icon
	 */
	public CommonAbstractAction(Icon icon) {
		this(null, icon, null);
	}

	/**
	 * creates an action with an icon, and a short description
	 * @param icon
	 * @param sShortDescription
	 */
	public CommonAbstractAction(Icon icon, String sShortDescription) {
		this(null, icon, sShortDescription);
	}

	/**
	 * creates an action with a name, an icon, and a short description
	 * @param sName
	 * @param icon
	 * @param sShortDescription
	 */
	public CommonAbstractAction(String sName, Icon icon, String sShortDescription) {
		this.setName(sName);
		this.setIcon(icon);
		this.setShortDescription(sShortDescription);
	}

	/**
	 * creates an action, deriving name, icon and description from the given button.
	 * @param btn
	 */
	public CommonAbstractAction(AbstractButton btn) {
		this(btn.getText(), btn.getIcon(), btn.getToolTipText());
	}

	/**
	 * sets the name, which is displayed in menu items or buttons, but not in toolbar buttons.
	 * @param sName
	 */
	public void setName(String sName) {
		this.putValue(Action.NAME, sName);
	}

	/**
	 * sets the icon
	 * @param icon
	 */
	public void setIcon(Icon icon) {
		this.putValue(Action.SMALL_ICON, icon);
	}

	/**
	 * sets the short description, which is used for tooltips
	 * @param sShortDescription
	 */
	public void setShortDescription(String sShortDescription) {
		this.putValue(Action.SHORT_DESCRIPTION, sShortDescription);
	}

}  // class CommonAbstractAction

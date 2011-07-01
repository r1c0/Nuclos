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
package org.nuclos.client.layout.wysiwyg.editor.util.mouselistener;

/**
 * Small class with common Tasks:
 * <ul>
 * <li> {@link #getActionToPerform()}</li>
 * <li> {@link #clearActionToPerform()} </li>
 * <li> {@link #setActionToPerform(ActionToPerform)}</li>
 * </ul>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public abstract class AbstractControlledListener {

	/**
	 * enumeration collecting every action that can be made
	 */
	public enum ActionToPerform {
		PANEL_ALTER_COLUMN, PANEL_ALTER_ROW, PANEL_ALTER_SE, COMPONENT_RESIZE_NORTH, COMPONENT_RESIZE_SOUTH, COMPONENT_RESIZE_EAST, COMPONENT_RESIZE_WEST, COMPONENT_MOVE, COMPONENT_EXPAND_EAST, COMPONENT_EXPAND_SOUTH, PANEL_SELECT_MULTIPLE, NOTHING_TO_DO
	}

	private ActionToPerform actionToPerform;

	/**
	 * Setting the {@link ActionToPerform}
	 * 
	 * @param actionToPerform
	 */
	public void setActionToPerform(ActionToPerform actionToPerform) {
		this.actionToPerform = actionToPerform;
	}

	/**
	 * @return {@link ActionToPerform} that was set
	 */
	public ActionToPerform getActionToPerform() {
		return actionToPerform;
	}

	/**
	 * clears the {@link ActionToPerform}
	 */
	public void clearActionToPerform() {
		actionToPerform = null;
	}
}

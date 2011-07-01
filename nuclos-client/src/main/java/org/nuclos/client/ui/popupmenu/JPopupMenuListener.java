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
package org.nuclos.client.ui.popupmenu;

import javax.swing.*;
import java.awt.event.MouseListener;

/**
 * <code>MouseListener</code> for <code>JPopupMenu</code>s.
 * Implementations of this interface encapsulate the platform-specific logic about which mouse events lead
 * to opening a popup menu, in a platform independent way.
 * Clients of this interface just have to provide the popup menu itself.<br>
 * There are several implementations of this class which allow to construct a popup menu statically,
 * dynamically or lazily. Clients shouldn't use this interface directly, but one of these implementations.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 *
 * @see AbstractJPopupMenuListener
 * @see DefaultJPopupMenuListener
 * @see DynamicJPopupMenuListener
 * @see LazyJPopupMenuListener
 */
public interface JPopupMenuListener extends MouseListener {

	/**
	 * @return the popup menu to show, if any. This method will be called each time a popup menu is about to be displayed.
	 * If <code>null</code> is returned, just no popup menu will be displayed.
	 */
	JPopupMenu getJPopupMenu();

}  // interface JPopupMenuListener

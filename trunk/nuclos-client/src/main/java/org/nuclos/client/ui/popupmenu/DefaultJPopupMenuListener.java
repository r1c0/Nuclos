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

import java.awt.event.MouseEvent;

import javax.swing.*;

import org.nuclos.common.collection.Factories;
import org.nuclos.common.collection.Factory;

/**
 * <code>JPopupMenuListener</code> for a static popup menu or a simple popup factory.
 * To create a lazy popup menu, create a memoizing listener.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class DefaultJPopupMenuListener extends AbstractJPopupMenuListener implements JPopupMenuListener {

	private final Factory<JPopupMenu> factory;

	/**
	 * @param popupMenu May be <code>null</code>. In that case, no popupmenu will be shown.
	 */
	public DefaultJPopupMenuListener(JPopupMenu popupMenu) {
		this.factory = Factories.constFactory(popupMenu);
	}

	public DefaultJPopupMenuListener(JPopupMenuFactory popupMenuFactory) {
		this.factory = wrapPopupMenuFactory(popupMenuFactory, false);
	}

	public DefaultJPopupMenuListener(JPopupMenuFactory popupMenuFactory, boolean memoizing) {
		this.factory = wrapPopupMenuFactory(popupMenuFactory, memoizing);
	}
	/**
	 * @return the popupmenu provided in the ctor, if any.
	 */
	@Override
	public final JPopupMenu getJPopupMenu() {
		return factory != null ? factory.create() : null;
	}
	
	@Override
	protected final JPopupMenu getJPopupMenu(MouseEvent ev) {
		return getJPopupMenu();
	}
	
	private static Factory<JPopupMenu> wrapPopupMenuFactory(final JPopupMenuFactory popupMenuFactory, boolean memoizing) {
		if (popupMenuFactory == null) {
			return Factories.constFactory(null);
		}
		Factory<JPopupMenu> factory = new Factory<JPopupMenu>() {
			@Override
			public JPopupMenu create() {
				return popupMenuFactory.newJPopupMenu();
			}
		};
		if (memoizing) {
			factory = Factories.memoizingFactory(factory);
		}
		return factory;
	}
}  // class DefaultJPopupMenuListener

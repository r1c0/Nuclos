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

import java.io.Closeable;

import javax.swing.ImageIcon;

import org.nuclos.client.main.mainframe.IconResolver;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.common.collection.Pair;

/**
 * A common super class which is shared by CollectController and other top-level
 * components.
 */
public abstract class TopController extends MainFrameTabController implements Closeable {
	
	public TopController(MainFrameTab mainFrameTab) {
		super(mainFrameTab);
	}
	
	public TopController() {
		super(null);
	}

	public ImageIcon getIconUnsafe() {
		return null;
	}
	
	public abstract Pair<IconResolver, String> getIconAndResolver();
	
	/**
	 * asks the user to save the current record if necessary, so that it can be abandoned afterwards.
	 * @param rl
	 * 		returns: can the action be performed? null=canceled
	 */
	public abstract void askAndSaveIfNecessary(boolean throwCancelException, ResultListener<Boolean> rl);
	
}

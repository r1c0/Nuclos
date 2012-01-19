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
package org.nuclos.client.explorer;

import javax.swing.JComponent;
import javax.swing.JTree;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * An {@link ExplorerView} displays one tree in a {@link MainFrameTab}.
 * Although it is called ExplorerView, an {@link ExplorerView} takes responsibilities of a controller (per explorer view).
 * (The {@link ExplorerController} is a singleton, that controls all existing explorer views).
 *
 * The view can be defined individually, but some standard components have to be provided (tree and main view component).
 * The model is defined by {@link ExplorerNode}s (and according {@link TreeNode}s).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public interface ExplorerView {

	/**
	 * @return the main component of the view
	 */
	public JComponent getViewComponent();

	/**
	 * @return the JTree contained in this ExplorerView
	 */
	public JTree getJTree();

	/**
	 * @return the root ExplorerNode contained in this view
	 */
	public ExplorerNode<?> getRootNode();

}	// interface ExplorerView

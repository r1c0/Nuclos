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

import org.nuclos.client.main.mainframe.MainFrameTab;

public interface MainFrameTabListener {
	
	/**
	 * 
	 * @param tab
	 */
	public void tabTitleChanged(MainFrameTab tab);
	
	/**
	 * 
	 * @param tab
	 */
	public void tabHidden(MainFrameTab tab);
	
	/**
	 * 
	 * @param tab
	 */
	public void tabRestoredFromHidden(MainFrameTab tab);
	
	/**
	 * tab is selected
	 * @param tab
	 */
	public void tabSelected(MainFrameTab tab);
	
	/**
	 * tab is added to tabbed pane
	 * @param tab
	 */
	public void tabAdded(MainFrameTab tab);
	
	/**
	 * tab is removes from tabbed pane
	 * @param tab
	 */
	public void tabClosed(MainFrameTab tab);
	
	/**
	 * For validation if closing is possible: 
	 * - use return false to cancel closing or
	 * - throw CommonBusinessException if you want to show a message.
	 * 
	 * @param tab
	 */
	public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl);
}

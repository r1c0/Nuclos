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

package org.nuclos.common;

import java.awt.Rectangle;

import javax.swing.JFrame;

public class WorkspaceDescriptionDefaultsFactory {
	
	public static WorkspaceDescription createOldMdiStyle() {
		WorkspaceDescription result = new WorkspaceDescription();
		
		/**
		 * TABBED
		 */
		WorkspaceDescription.Tabbed wdTabbedExplorer = new WorkspaceDescription.Tabbed();
		WorkspaceDescription.Tabbed wdTabbedTask = new WorkspaceDescription.Tabbed();
		WorkspaceDescription.Tabbed wdTabbedHome = new WorkspaceDescription.Tabbed();
		wdTabbedExplorer.setHomeTree(true);
		wdTabbedTask.setAlwaysHideStartmenu(true);
		wdTabbedHome.setHome(true);
		wdTabbedHome.setAlwaysHideStartmenu(true);
		
		/**
		 * SPLIT
		 */
		WorkspaceDescription.Split wdSplitTaskHome = new WorkspaceDescription.Split();
		WorkspaceDescription.Split wdSplitExplorerOther = new WorkspaceDescription.Split();
		wdSplitTaskHome.getContentA().setContent(wdTabbedTask);
		wdSplitTaskHome.getContentB().setContent(wdTabbedHome);
		wdSplitTaskHome.setHorizontal(false);
		wdSplitTaskHome.setPosition(160);
		wdSplitExplorerOther.getContentA().setContent(wdTabbedExplorer);
		wdSplitExplorerOther.getContentB().setContent(wdSplitTaskHome);
		wdSplitExplorerOther.setHorizontal(true);
		wdSplitExplorerOther.setPosition(200);
		
		/**
		 * FRAME
		 */
		WorkspaceDescription.Frame wdFrame = new WorkspaceDescription.Frame();
		wdFrame.getContent().setContent(wdSplitExplorerOther);
		wdFrame.setMainFrame(true);
		wdFrame.setNormalBounds(new Rectangle(40, 40, 960, 700));
		wdFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		result.addFrame(wdFrame);
		
		return result;
	}
}

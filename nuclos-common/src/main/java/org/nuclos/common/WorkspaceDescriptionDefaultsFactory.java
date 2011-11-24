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

import org.nuclos.common.WorkspaceDescription.ColumnPreferences;
import org.nuclos.common.WorkspaceDescription.ColumnSorting;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.WorkspaceDescription.TablePreferences;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;

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
		wdTabbedExplorer.setAlwaysHideHistory(true);
		wdTabbedExplorer.setAlwaysHideBookmark(true);
		wdTabbedTask.setAlwaysHideStartmenu(true);
		wdTabbedTask.setAlwaysHideHistory(true);
		wdTabbedTask.setAlwaysHideBookmark(true);
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
		
		/**
		 * PREFERENCES for USER
		 */
		EntityPreferences epUser = getEntity(result, NuclosEntity.USER);
		TablePreferences tpUser = epUser.getResultPreferences();
		tpUser.addSelectedColumnPreferences(getFixedColumn("name", 150));
		tpUser.addSelectedColumnPreferences(getColumn("email", 200));
		tpUser.addSelectedColumnPreferences(getColumn("firstname", 100));
		tpUser.addSelectedColumnPreferences(getColumn("lastname", 100));
		tpUser.addSelectedColumnPreferences(getColumn("superuser", 40));
		tpUser.addSelectedColumnPreferences(getColumn("locked", 40));
		// etc...
		tpUser.addColumnSorting(getSorting("name"));
		// etc...
		tpUser.addHiddenColumn("password");
		tpUser.addHiddenColumn("group");
		// etc...
		
		/*
		 * So kann man Unterformulare hizufügen...
		 */
		TablePreferences tpUserRole = getSubForm(epUser, NuclosEntity.ROLEUSER);
		tpUserRole.addSelectedColumnPreferences(getColumn("role", 150));
		tpUserRole.addColumnSorting(getSorting("role"));
		
		
		return result;
	}
	
	private static EntityPreferences getEntity(WorkspaceDescription wd, NuclosEntity entity) {
		EntityPreferences result = wd.getEntityPreferences(entity.getEntityName());
		return result;
	}
	
	private static TablePreferences getSubForm(EntityPreferences ep, NuclosEntity subEntity) {
		return ep.getSubFormPreferences(subEntity.getEntityName()).getTablePreferences();
	}
	
	private static ColumnPreferences getColumn(String column, int width) {
		return getColumn(column, width, false);
	}
	
	private static ColumnPreferences getFixedColumn(String column, int width) {
		return getColumn(column, width, true);
	}
	
	private static ColumnPreferences getColumn(String column, int width, boolean fixed) {
		ColumnPreferences result = new ColumnPreferences();
		result.setColumn(column);
		result.setWidth(width);
		result.setFixed(fixed);
		return result;
	}
	
	private static ColumnSorting getSorting(String column) {
		ColumnSorting result = new ColumnSorting();
		result.setColumn("name");
		result.setAsc(true);
		return result;
	}
	
}

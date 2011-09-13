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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkspaceDescription implements Serializable {
	private static final long serialVersionUID = 6637996725938917463L;

	private String name;
	private final List<Frame> frames = new ArrayList<Frame>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Frame> getFrames() {
		return frames;
	}

	public void addFrame(Frame frame) {
		this.frames.add(frame);
	}

	public static class Tab implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		private String label;
		private boolean neverClose = false;;
		private String preferencesXML;
		private String restoreController;
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public boolean isNeverClose() {
			return neverClose;
		}
		public void setNeverClose(boolean neverClose) {
			this.neverClose = neverClose;
		}
		public String getPreferencesXML() {
			return preferencesXML;
		}
		public void setPreferencesXML(String preferencesXML) {
			this.preferencesXML = preferencesXML;
		}
		public String getRestoreController() {
			return restoreController;
		}
		public void setRestoreController(String restoreController) {
			this.restoreController = restoreController;
		}
	}

	public static class Tabbed implements NestedContent {
		private static final long serialVersionUID = 6637996725938917463L;

		private boolean home = false;
		private boolean homeTree = false;
		private boolean showEntity = true;
		private boolean showAdministration = false;
		private boolean showConfiguration = false;
		private boolean neverHideStartmenu = false;
		private boolean neverHideHistory = false;
		private boolean neverHideBookmark = false;
		private boolean alwaysHideStartmenu = false;
		private boolean alwaysHideHistory = false;
		private boolean alwaysHideBookmark = false;

		private int selected;
		private final List<Tab> tabs = new ArrayList<Tab>();
		private final Set<String> predefinedEntityOpenLocation = new HashSet<String>();
		private final Set<String> reducedStartmenus = new HashSet<String>();
		private final Set<String> reducedHistoryEntities = new HashSet<String>();
		private final Set<String> reducedBookmarkEntities = new HashSet<String>();

		public boolean isHome() {
			return home;
		}
		public void setHome(boolean home) {
			this.home = home;
		}
		public boolean isHomeTree() {
			return homeTree;
		}
		public void setHomeTree(boolean homeTree) {
			this.homeTree = homeTree;
		}
		public int getSelected() {
			return selected;
		}
		public void setSelected(int selected) {
			this.selected = selected;
		}
		public List<Tab> getTabs() {
			return tabs;
		}
		public void addTab(Tab tab) {
			this.tabs.add(tab);
		}
		public boolean isShowEntity() {
			return showEntity;
		}
		public void setShowEntity(boolean showEntity) {
			this.showEntity = showEntity;
		}
		public boolean isShowConfiguration() {
			return showConfiguration;
		}
		public void setShowConfiguration(boolean showConfiguration) {
			this.showConfiguration = showConfiguration;
		}
		public boolean isShowAdministration() {
			return showAdministration;
		}
		public void setShowAdministration(boolean showAdministration) {
			this.showAdministration = showAdministration;
		}
		public boolean isNeverHideStartmenu() {
			return neverHideStartmenu;
		}
		public void setNeverHideStartmenu(boolean neverHideStartmenu) {
			this.neverHideStartmenu = neverHideStartmenu;
		}
		public boolean isNeverHideHistory() {
			return neverHideHistory;
		}
		public void setNeverHideHistory(boolean neverHideHistory) {
			this.neverHideHistory = neverHideHistory;
		}
		public boolean isNeverHideBookmark() {
			return neverHideBookmark;
		}
		public void setNeverHideBookmark(boolean neverHideBookmark) {
			this.neverHideBookmark = neverHideBookmark;
		}
		public boolean isAlwaysHideStartmenu() {
			return alwaysHideStartmenu;
		}
		public void setAlwaysHideStartmenu(boolean alwaysHideStartmenu) {
			this.alwaysHideStartmenu = alwaysHideStartmenu;
		}
		public boolean isAlwaysHideHistory() {
			return alwaysHideHistory;
		}
		public void setAlwaysHideHistory(boolean alwaysHideHistory) {
			this.alwaysHideHistory = alwaysHideHistory;
		}
		public boolean isAlwaysHideBookmark() {
			return alwaysHideBookmark;
		}
		public void setAlwaysHideBookmark(boolean alwaysHideBookmark) {
			this.alwaysHideBookmark = alwaysHideBookmark;
		}
		public Set<String> getPredefinedEntityOpenLocations() {
			return predefinedEntityOpenLocation;
		}
		public void addPredefinedEntityOpenLocation(String entity) {
			this.predefinedEntityOpenLocation.add(entity);
		}
		public void addAllPredefinedEntityOpenLocations(List<String> entities) {
			this.predefinedEntityOpenLocation.addAll(entities);
		}
		public Set<String> getReducedStartmenus() {
			return reducedStartmenus;
		}
		public void addReducedStartmenu(String reducedStartmenu) {
			this.reducedStartmenus.add(reducedStartmenu);
		}
		public void addAllReducedStartmenus(Set<String> reducedStartmenus) {
			this.reducedStartmenus.addAll(reducedStartmenus);
		}
		public Set<String> getReducedHistoryEntities() {
			return reducedHistoryEntities;
		}
		public void addReducedHistoryEntity(String reducedHistoryEntity) {
			this.reducedHistoryEntities.add(reducedHistoryEntity);
		}
		public void addAllReducedHistoryEntities(Set<String> reducedHistoryEntities) {
			this.reducedHistoryEntities.addAll(reducedHistoryEntities);
		}
		public Set<String> getReducedBookmarkEntities() {
			return reducedBookmarkEntities;
		}
		public void addReducedBookmarkEntity(String reducedBookmarkEntity) {
			this.reducedBookmarkEntities.add(reducedBookmarkEntity);
		}
		public void addAllReducedBookmarkEntities(Set<String> reducedBookmarkEntities) {
			this.reducedBookmarkEntities.addAll(reducedBookmarkEntities);
		}
	}

	public static class Split implements NestedContent {
		private static final long serialVersionUID = 6637996725938917463L;

		private boolean horizontal;
		private int position;
		private final MutableContent contentA = new MutableContent();
		private final MutableContent contentB = new MutableContent();
		public boolean isHorizontal() {
			return horizontal;
		}
		public void setHorizontal(boolean horizontal) {
			this.horizontal = horizontal;
		}
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
		public MutableContent getContentA() {
			return contentA;
		}
		public MutableContent getContentB() {
			return contentB;
		}
	}

	public static class Frame implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		private boolean mainFrame;
		private int number = 0;
		private final MutableContent content = new MutableContent();
		private int extendedState;
		Rectangle normalBounds;
		public MutableContent getContent() {
			return content;
		}
		public int getExtendedState() {
			return extendedState;
		}
		public void setExtendedState(int extendedState) {
			this.extendedState = extendedState;
		}
		public Rectangle getNormalBounds() {
			return normalBounds;
		}
		public void setNormalBounds(Rectangle normalBounds) {
			this.normalBounds = normalBounds;
		}
		public boolean isMainFrame() {
			return mainFrame;
		}
		public void setMainFrame(boolean mainFrame) {
			this.mainFrame = mainFrame;
		}
		public int getNumber() {
			return number;
		}
		public void setNumber(int number) {
			this.number = number;
		}
	}

	public static class MutableContent implements NestedContent {
		private static final long serialVersionUID = 6637996725938917463L;

		private NestedContent content;
		public NestedContent getContent() {
			return content;
		}
		public void setContent(NestedContent content) {
			this.content = content;
		}
	}

	public static interface NestedContent extends Serializable {}
}

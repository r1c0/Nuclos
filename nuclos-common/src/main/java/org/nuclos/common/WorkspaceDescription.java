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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.util.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

public class WorkspaceDescription implements Serializable {
	private static final long serialVersionUID = 6637996725938917463L;

	private String name;
	private boolean hide;
	private boolean hideName;
	private boolean hideMenuBar;
	private boolean alwaysOpenAtLogin;
	private String nuclosResource;
	private List<Frame> frames;
	private List<EntityPreferences> entityPreferences;
	
	public WorkspaceDescription() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public boolean isHideName() {
		return this.hideName;
	}

	public void setHideName(boolean hideName) {
		this.hideName = hideName;
	}
	
	public boolean isHideMenuBar() {
		return hideMenuBar;
	}

	public void setHideMenuBar(boolean hideMenuBar) {
		this.hideMenuBar = hideMenuBar;
	}

	public boolean isAlwaysOpenAtLogin() {
		return alwaysOpenAtLogin;
	}

	public void setAlwaysOpenAtLogin(boolean alwaysOpenAtLogin) {
		this.alwaysOpenAtLogin = alwaysOpenAtLogin;
	}

	public String getNuclosResource() {
		return this.nuclosResource;
	}

	public void setNuclosResource(String nuclosResource) {
		this.nuclosResource = nuclosResource;
	}
	
	private List<Frame> _getFrames() {
		if (this.frames == null)
			this.frames = new ArrayList<Frame>();
		return this.frames;
	}

	public List<Frame> getFrames() {
		return this._getFrames();
	}

	public void addFrame(Frame frame) {
		this._getFrames().add(frame);
	}
	
	private List<EntityPreferences> _getEntityPreferences() {
		if (this.entityPreferences == null)
			this.entityPreferences = new ArrayList<EntityPreferences>();
		return this.entityPreferences;
	}
	
	public List<EntityPreferences> getEntityPreferences() {
		return new ArrayList<EntityPreferences>(this._getEntityPreferences());
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public EntityPreferences getEntityPreferences(String entity) {
		EntityPreferences result = null;
		for (EntityPreferences ep : this._getEntityPreferences()) {
			if (LangUtils.equals(entity, ep.getEntity())) {
				result = ep;
				break;
			}
		}
		if (result == null) {
			result = new EntityPreferences();
			result.setEntity(entity);
			this._getEntityPreferences().add(result);
		}
		return result;
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public boolean containsEntityPreferences(String entity) {
		for (EntityPreferences ep : this._getEntityPreferences()) {
			if (LangUtils.equals(entity, ep.getEntity())) {
				return true;
			}
		}
		return false;
	}
	
	public void addEntityPreferences(EntityPreferences ep) {
		this._getEntityPreferences().add(ep);
	}
	
	public void addAllEntityPreferences(Collection<EntityPreferences> eps) {
		this._getEntityPreferences().addAll(eps);
	}
	
	public void removeEntityPreferences(EntityPreferences ep) {
		this._getEntityPreferences().remove(ep);
	}
	
	public void removeAllEntityPreferences() {
		this._getEntityPreferences().clear();
	}

	public static class Tab implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		private String label;
		private boolean neverClose = false;
		private boolean fromAssigned = false;;
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
		public boolean isFromAssigned() {
			return fromAssigned;
		}
		public void setFromAssigned(boolean fromAssigned) {
			this.fromAssigned = fromAssigned;
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
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Tab)
				return LangUtils.equals(this.preferencesXML, ((Tab) obj).getPreferencesXML()) &&
						LangUtils.equals(this.restoreController, ((Tab) obj).getRestoreController()) &&
						LangUtils.equals(this.fromAssigned, ((Tab) obj).isFromAssigned());
			return super.equals(obj);
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
		private boolean desktopActive = false;

		private int selected;
		private final List<Tab> tabs = new ArrayList<Tab>();
		private final Set<String> predefinedEntityOpenLocation = new HashSet<String>();
		private final Set<String> reducedStartmenus = new HashSet<String>();
		private final Set<String> reducedHistoryEntities = new HashSet<String>();
		private final Set<String> reducedBookmarkEntities = new HashSet<String>();
		
		private Desktop desktop;

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
			return new ArrayList<Tab>(this.tabs);
		}
		public void addTab(Tab tab) {
			this.tabs.add(tab);
		}
		public void addAllTab(List<Tab> tabs) {
			this.tabs.addAll(tabs);
		}
		public void removeTab(Tab tab) {
			this.tabs.remove(tab);
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
		public Desktop getDesktop() {
			return desktop;
		}
		public void setDesktop(Desktop desktop) {
			this.desktop = desktop;
		}
		public boolean isDesktopActive() {
			return desktopActive;
		}
		public void setDesktopActive(boolean desktopActive) {
			this.desktopActive = desktopActive;
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
	
	public static class EntityPreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		
		private String entity;
		private TablePreferences resultPreferences;
		private List<SubFormPreferences> subFormPreferences;
		
		public String getEntity() {
			return entity;
		}
		public void setEntity(String entity) {
			this.entity = entity;
		}
		
		private TablePreferences _getResultPreferences() {
			if (this.resultPreferences == null)
				this.resultPreferences = new TablePreferences();
			return this.resultPreferences;
		}
		public TablePreferences getResultPreferences() {
			return this._getResultPreferences() ;
		}
		
		private List<SubFormPreferences> _getSubFormPreferences() {
			if (this.subFormPreferences == null) 
				this.subFormPreferences = new ArrayList<SubFormPreferences>();
			return this.subFormPreferences;
		}
		public List<SubFormPreferences> getSubFormPreferences() {
			return new ArrayList<SubFormPreferences>(this._getSubFormPreferences());
		}
		
		/**
		 * 
		 * @param subForm
		 * @return
		 */
		public SubFormPreferences getSubFormPreferences(String subForm) {
			SubFormPreferences result = null;
			for (SubFormPreferences sfp : this._getSubFormPreferences()) {
				if (LangUtils.equals(subForm, sfp.getEntity())) {
					result = sfp;
					break;
				}
			}
			if (result == null) {
				result = new SubFormPreferences();
				result.setEntity(subForm);
				this._getSubFormPreferences().add(result);
			}
			return result;
		}
		
		public void addSubFormPreferences(SubFormPreferences sfp) {
			this._getSubFormPreferences().add(sfp);
		}
		public void addAllSubFormPreferences(List<SubFormPreferences> sfps) {
			this._getSubFormPreferences().addAll(sfps);
		}
		public void removeSubFormPreferences(SubFormPreferences sfp) {
			this._getSubFormPreferences().remove(sfp);
		}
		public void removeAllSubFormPreferences() {
			this._getSubFormPreferences().clear();
		}
		public void clearResultPreferences() {
			this._getResultPreferences().clear();
		}
		
		@Override
		public int hashCode() {
			if (entity == null)
				return 0;
			return entity.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true; 
			
			if (obj instanceof EntityPreferences) {
				EntityPreferences other = (EntityPreferences) obj;
				return LangUtils.equals(getEntity(), other.getEntity());
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			if (entity == null)
				return "null";
			return entity.toString();
		}
	}
	
	public static class SubFormPreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		
		private String entity;
		private TablePreferences tablePreferences;
		
		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		private TablePreferences _getTablePreferences() {
			if (this.tablePreferences == null)
				this.tablePreferences = new TablePreferences();
			return this.tablePreferences;
		}
		public TablePreferences getTablePreferences() {
			return this._getTablePreferences();
		}
		
		public void clearTablePreferences() {
			this._getTablePreferences().clear();
		}

		@Override
		public int hashCode() {
			if (entity == null)
				return 0;
			return entity.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			
			if (obj instanceof SubFormPreferences) {
				SubFormPreferences other = (SubFormPreferences) obj;
				return LangUtils.equals(getEntity(), other.getEntity());
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			if (entity == null)
				return "null";
			return entity.toString();
		}
	}
	
	public static class TablePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		
		private List<ColumnPreferences> selectedColumnPreferences;
		private Set<String> hiddenColumns;
		private List<ColumnSorting> columnSorting;
		
		private List<ColumnPreferences> _getSelectedColumnPreferences() {
			if (this.selectedColumnPreferences == null)
				this.selectedColumnPreferences = new ArrayList<ColumnPreferences>();
			return this.selectedColumnPreferences;
		}
		public List<ColumnPreferences> getSelectedColumnPreferences() {
			return new ArrayList<ColumnPreferences>(this._getSelectedColumnPreferences());
		}
		public void addSelectedColumnPreferences(ColumnPreferences cp) {
			this._getSelectedColumnPreferences().add(cp);
		}
		public void addSelectedColumnPreferencesInFront(ColumnPreferences cp) {
			this._getSelectedColumnPreferences().add(0, cp);
		}
		public void addAllSelectedColumnPreferencesInFront(List<ColumnPreferences> cps) {
			this._getSelectedColumnPreferences().addAll(0, cps);
		}
		public void addAllSelectedColumnPreferences(List<ColumnPreferences> cps) {
			this._getSelectedColumnPreferences().addAll(cps);
		}
		public void removeSelectedColumnPreferences(ColumnPreferences cp) {
			this._getSelectedColumnPreferences().remove(cp);
		}
		public void removeAllSelectedColumnPreferences() {
			this._getSelectedColumnPreferences().clear();
		}
		
		private List<ColumnSorting> _getColumnSortings() {
			if (this.columnSorting == null)
				this.columnSorting = new ArrayList<ColumnSorting>();
			return this.columnSorting;
		}
		public List<ColumnSorting> getColumnSortings() {
			return new ArrayList<ColumnSorting>(this._getColumnSortings());
		}
		public void addColumnSorting(ColumnSorting cs) {
			this._getColumnSortings().add(cs);
		}
		public void addAllColumnSortings(List<ColumnSorting> css) {
			this._getColumnSortings().addAll(css);
		}
		public void removeColumnSorting(ColumnSorting cs) {
			this._getColumnSortings().remove(cs);
		}
		public void removeAllColumnSortings() {
			this._getColumnSortings().clear();
		}
		
		private Set<String> _getHiddenColumns() {
			if (this.hiddenColumns == null)
				this.hiddenColumns = new HashSet<String>();
			return this.hiddenColumns;
		}
		public Set<String> getHiddenColumns() {
			return new HashSet<String>(this._getHiddenColumns());
		}
		public void addHiddenColumn(String column) {
			this._getHiddenColumns().add(column);
		}
		public void addAllHiddenColumns(Set<String> columns) {
			this._getHiddenColumns().addAll(columns);
		}
		public void removeHiddenColumn(String column) {
			this._getHiddenColumns().remove(column);
		}
		public void removeAllHiddenColumns() {
			this._getHiddenColumns().clear();
		}
		
		public TablePreferences copy() {
			TablePreferences result = new TablePreferences();
			for (ColumnPreferences cp : this._getSelectedColumnPreferences())
				result.addSelectedColumnPreferences(cp.copy());
			for (String hidden : this._getHiddenColumns())
				result.addHiddenColumn(hidden);
			for (ColumnSorting cs : this._getColumnSortings())
				result.addColumnSorting(cs.copy());
			return result;
		}
		
		public void clear() {
			this.removeAllSelectedColumnPreferences();
			this.removeAllHiddenColumns();
			this.removeAllColumnSortings();
		}
		
		public void clearAndImport(TablePreferences tp) {
			clear();
			final TablePreferences toImportPrefs = tp.copy();
			this.addAllSelectedColumnPreferences(toImportPrefs.getSelectedColumnPreferences());
			this.addAllHiddenColumns(toImportPrefs.getHiddenColumns());
			this.addAllColumnSortings(toImportPrefs.getColumnSortings());
		}
	}
	
	public static class ColumnPreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		
		public static final int TYPE_DEFAULT = 0;
		public static final int TYPE_EOEntityField = 1;
		public static final int TYPE_GenericObjectEntityField = 2;
		public static final int TYPE_MasterDataForeignKeyEntityField = 3;
		public static final int TYPE_EntityFieldWithEntity = 4;
		public static final int TYPE_EntityFieldWithEntityForExternal = 5;
		
		private String column;
		private String entity;
		private int width;
		private boolean fixed;
		
		private int type;
		private String pivotSubForm;
		private String pivotKeyField;
		private String pivotValueField;
		private String pivotValueType;
		
		public String getColumn() {
			return column;
		}
		public void setColumn(String column) {
			this.column = column;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}		
		public String getEntity() {
			return entity;
		}
		public void setEntity(String entity) {
			this.entity = entity;
		}
		public boolean isFixed() {
			return fixed;
		}
		public void setFixed(boolean fixed) {
			this.fixed = fixed;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public String getPivotSubForm() {
			return pivotSubForm;
		}
		public void setPivotSubForm(String pivotSubForm) {
			this.pivotSubForm = pivotSubForm;
		}
		public String getPivotKeyField() {
			return pivotKeyField;
		}
		public void setPivotKeyField(String pivotKeyField) {
			this.pivotKeyField = pivotKeyField;
		}
		public String getPivotValueField() {
			return pivotValueField;
		}
		public void setPivotValueField(String pivotValueField) {
			this.pivotValueField = pivotValueField;
		}
		public String getPivotValueType() {
			return pivotValueType;
		}
		public void setPivotValueType(String pivotValueType) {
			this.pivotValueType = pivotValueType;
		}
		@Override
		public int hashCode() {
			if (column == null)
				return 0;
			return column.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ColumnPreferences) {
				ColumnPreferences other = (ColumnPreferences) obj;
				return LangUtils.equals(getColumn(), other.getColumn()) &&
					   LangUtils.equals(getEntity(), other.getEntity()) &&
					   LangUtils.equals(getType(), other.getType()) &&
					   LangUtils.equals(getPivotSubForm(), other.getPivotSubForm()) &&
					   LangUtils.equals(getPivotKeyField(), other.getPivotKeyField()) &&
					   LangUtils.equals(getPivotValueField(), other.getPivotValueField());
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("ColumnPreferences[").append(entity).append(", ");
			sb.append(column).append(", ");
			sb.append(type).append(", ");
			sb.append(pivotSubForm).append(", ");
			sb.append(pivotKeyField).append(", ");
			sb.append(pivotValueField).append(", ");
			sb.append(pivotValueType);
			sb.append("]");
			return sb.toString();
		}
		
		public ColumnPreferences copy() {
			ColumnPreferences result = new ColumnPreferences();
			result.setColumn(column);
			result.setEntity(entity);
			result.setFixed(fixed);
			result.setWidth(width);
			result.setType(type);
			result.setPivotSubForm(pivotSubForm);
			result.setPivotKeyField(pivotKeyField);
			result.setPivotValueField(pivotValueField);
			result.setPivotValueType(pivotValueType);
			return result;
		}
	}
	
	public static class ColumnSorting implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		
		private String column;
		private boolean asc = true;
		
		public String getColumn() {
			return column;
		}
		public void setColumn(String column) {
			this.column = column;
		}
		public boolean isAsc() {
			return asc;
		}
		public void setAsc(boolean asc) {
			this.asc = asc;
		}
		
		@Override
		public int hashCode() {
			if (column == null)
				return 0;
			return column.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ColumnSorting) {
				ColumnSorting other = (ColumnSorting) obj;
				LangUtils.equals(getColumn(), other.getColumn());
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			if (column == null)
				return "null";
			return column.toString();
		}
		
		public ColumnSorting copy() {
			ColumnSorting result = new ColumnSorting();
			result.setColumn(getColumn());
			result.setAsc(isAsc());
			return result;
		}
	}
	
	public static class Color implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		private final int red, green, blue;
		public Color(int red, int green, int blue) {
			super();
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		public int getRed() {
			return red;
		}
		public int getGreen() {
			return green;
		}
		public int getBlue() {
			return blue;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof Color) {
				Color other = (Color) obj;
				return this.red == other.red &&
						this.green == other.green &&
						this.blue == other.blue;
			}
			return super.equals(obj);
		}
		public java.awt.Color toColor() {
			return new java.awt.Color(red, green, blue);
		}
	}
	
	public static interface DesktopItem extends Serializable{}
	
	public static class Action implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		private String action;
		private Map<String, String> stringParams;
		private Map<String, Long> longParams;
		private Map<String, Boolean> booleanParams;
		public String getAction() {
			return action;
		}
		public void setAction(String action) {
			this.action = action;
		}
		public void putStringParameter(String key, String value) {
			_getStringParams().put(key, value);
		}
		public String getStringParameter(String key) {
			return _getStringParams().get(key);
		}
		public void putLongParameter(String key, Long value) {
			_getLongParams().put(key, value);
		}
		public Long getLongParameter(String key) {
			return _getLongParams().get(key);
		}
		public void putBooleanParameter(String key, Boolean value) {
			_getBooleanParams().put(key, value);
		}
		public Boolean getBooleanParameter(String key) {
			return _getBooleanParams().get(key);
		}
		private Map<String, String> _getStringParams() {
			if (stringParams == null) {
				stringParams = new HashMap<String, String>();
			}
			return stringParams;
		}
		private Map<String, Long> _getLongParams() {
			if (longParams == null) {
				longParams = new HashMap<String, Long>();
			}
			return longParams;
		}
		private Map<String, Boolean> _getBooleanParams() {
			if (booleanParams == null) {
				booleanParams = new HashMap<String, Boolean>();
			}
			return booleanParams;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
			    return true;
			if (obj instanceof Action) {
				Action other = (Action) obj;
				if (!LangUtils.equals(this.getAction(), other.getAction())) {
					return false;
				}
				if (!LangUtils.equals(this._getStringParams(), other._getStringParams())) {
					return false;
				}
				if (!LangUtils.equals(this._getLongParams(), other._getLongParams())) {
					return false;
				}
				if (!LangUtils.equals(this._getBooleanParams(), other._getBooleanParams())) {
					return false;
				}
				return true;
			}
			return super.equals(obj);
		}
		@Override
		public int hashCode() {
			if (action != null) {
				return action.hashCode();
			}
			return 0;
		}
		@Override
		public String toString() {
			final StringBuffer result = new StringBuffer();
			result.append("Action=").append(getAction());
			result.append(",StringParams=").append(_getStringParams().toString());
			result.append(",LongParams=").append(_getLongParams().toString());
			result.append(",BooleanParams=").append(_getBooleanParams().toString());
			return result.toString();
		}
		
	}
	
	public static class MenuItem implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		private Action menuAction;
		public Action getMenuAction() {
			return menuAction;
		}
		public void setMenuAction(Action menuAction) {
			this.menuAction = menuAction;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof MenuItem) {
				MenuItem other = (MenuItem) obj;
				return LangUtils.equals(this.menuAction, other.menuAction);
			}
			return super.equals(obj);
		}
		
	}
	
	public static class MenuButton implements DesktopItem {
		private static final long serialVersionUID = 6637996725938917463L;
		private Action menuAction;
		private String resourceIcon, resourceIconHover, nuclosResource, nuclosResourceHover;
		private List<MenuItem> menuItems;
		public Action getMenuAction() {
			return menuAction;
		}
		public void setMenuAction(Action menuAction) {
			this.menuAction = menuAction;
		}
		public String getResourceIcon() {
			return resourceIcon;
		}
		public void setResourceIcon(String resourceIconHover) {
			this.resourceIcon = resourceIconHover;
		}
		public String getResourceIconHover() {
			return resourceIconHover;
		}
		public void setResourceIconHover(String resourceIconHover) {
			this.resourceIconHover = resourceIconHover;
		}
		public String getNuclosResource() {
			return nuclosResource;
		}
		public void setNuclosResource(String nuclosResource) {
			this.nuclosResource = nuclosResource;
		}
		public String getNuclosResourceHover() {
			return nuclosResourceHover;
		}
		public void setNuclosResourceHover(String nuclosResourceHover) {
			this.nuclosResourceHover = nuclosResourceHover;
		}
		private List<MenuItem> _getMenuItems() {
			if (menuItems == null)
				menuItems = new ArrayList<MenuItem>();
			return menuItems;
		}
		public List<MenuItem> getMenuItems() {
			return new ArrayList<MenuItem>(_getMenuItems());
		}
		public void addMenuItem(MenuItem mi) {
			_getMenuItems().add(mi);
		}
		public void addMenuItem(int index, MenuItem mi) {
			_getMenuItems().add(index, mi);
		}
		public void addAllMenuItems(List<MenuItem> mis) {
			_getMenuItems().addAll(mis);
		}
		public boolean removeMenuItem(MenuItem mi) {
			return _getMenuItems().remove(mi);
		}
		public void removeAllMenuItems() {
			_getMenuItems().clear();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof MenuButton) {
				MenuButton other = (MenuButton) obj;
				return LangUtils.equals(this.menuAction, other.menuAction) &&
						LangUtils.equals(this.menuItems, other.menuItems) &&
						LangUtils.equals(this.nuclosResource, other.nuclosResource) &&
						LangUtils.equals(this.resourceIcon, other.resourceIcon) &&
						LangUtils.equals(this.resourceIconHover, other.resourceIconHover);
			}
			return super.equals(obj);
		}
	}
	
	public static class Desktop implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;
		public static final int LAYOUT_WRAP = 0;
		public static final int LAYOUT_ONE_ROW = 1;
		/** <code>SwingConstants.CENTER</code> */		public static final int HORIZONTAL_ALIGNMENT_CENTER = 0;
		/** <code>SwingConstants.LEFT</code> */			public static final int HORIZONTAL_ALIGNMENT_LEFT = 2;
		/** <code>SwingConstants.RIGHT</code> */		public static final int HORIZONTAL_ALIGNMENT_RIGHT = 4;
		private int horizontalGap, verticalGap, menuItemTextSize, layout, menuItemTextHorizontalPadding, menuItemTextHorizontalAlignment;
		private Color menuItemTextColor, menuItemTextHoverColor;
		private String resourceMenuBackground, resourceMenuBackgroundHover, resourceBackground, nuclosResourceBackground;
		private boolean hideToolBar = false;
		private List<DesktopItem> desktopItems;
		public int getHorizontalGap() {
			return horizontalGap;
		}
		public void setHorizontalGap(int horizontalGap) {
			this.horizontalGap = horizontalGap;
		}
		public int getLayout() {
			return layout;
		}
		public void setLayout(int layout) {
			this.layout = layout;
		}
		public int getVerticalGap() {
			return verticalGap;
		}
		public void setVerticalGap(int verticalGap) {
			this.verticalGap = verticalGap;
		}
		public int getMenuItemTextSize() {
			return menuItemTextSize;
		}
		public void setMenuItemTextSize(int menuItemTextSize) {
			this.menuItemTextSize = menuItemTextSize;
		}
		public int getMenuItemTextHorizontalPadding() {
			return menuItemTextHorizontalPadding;
		}
		public void setMenuItemTextHorizontalPadding(int menuItemTextHorizontalPadding) {
			this.menuItemTextHorizontalPadding = menuItemTextHorizontalPadding;
		}
		public int getMenuItemTextHorizontalAlignment() {
			return menuItemTextHorizontalAlignment;
		}
		public void setMenuItemTextHorizontalAlignment(
				int menuItemTextHorizontalAlignment) {
			this.menuItemTextHorizontalAlignment = menuItemTextHorizontalAlignment;
		}
		public Color getMenuItemTextColor() {
			return menuItemTextColor;
		}
		public void setMenuItemTextColor(Color menuItemTextColor) {
			this.menuItemTextColor = menuItemTextColor;
		}
		public void setMenuItemTextColor(java.awt.Color menuItemTextColor) {
			if (menuItemTextColor == null) {
				this.menuItemTextColor = null;
			} else {
				this.menuItemTextColor = new Color(menuItemTextColor.getRed(), menuItemTextColor.getGreen(), menuItemTextColor.getBlue());
			}
		}
		public Color getMenuItemTextHoverColor() {
			return menuItemTextHoverColor;
		}
		public void setMenuItemTextHoverColor(Color menuItemTextHoverColor) {
			this.menuItemTextHoverColor = menuItemTextHoverColor;
		}
		public void setMenuItemTextHoverColor(java.awt.Color menuItemTextHoverColor) {
			if (menuItemTextHoverColor == null) {
				this.menuItemTextHoverColor = null;
			} else {
				this.menuItemTextHoverColor = new Color(menuItemTextHoverColor.getRed(), menuItemTextHoverColor.getGreen(), menuItemTextHoverColor.getBlue());
			}
		}
		public String getResourceMenuBackground() {
			return resourceMenuBackground;
		}
		public void setResourceMenuBackground(String resourceMenuBackground) {
			this.resourceMenuBackground = resourceMenuBackground;
		}
		public String getResourceMenuBackgroundHover() {
			return resourceMenuBackgroundHover;
		}
		public void setResourceMenuBackgroundHover(String resourceMenuBackgroundHover) {
			this.resourceMenuBackgroundHover = resourceMenuBackgroundHover;
		}
		public String getResourceBackground() {
			return resourceBackground;
		}
		public void setResourceBackground(String resourceBackground) {
			this.resourceBackground = resourceBackground;
		}
		public String getNuclosResourceBackground() {
			return nuclosResourceBackground;
		}
		public void setNuclosResourceBackground(String nuclosResourceBackground) {
			this.nuclosResourceBackground = nuclosResourceBackground;
		}
		public boolean isHideToolBar() {
			return hideToolBar;
		}
		public void setHideToolBar(boolean hideToolBar) {
			this.hideToolBar = hideToolBar;
		}
		private List<DesktopItem> _getDesktopItems() {
			if (desktopItems == null)
				desktopItems = new ArrayList<DesktopItem>();
			return desktopItems;
		}
		public List<DesktopItem> getDesktopItems() {
			return new ArrayList<DesktopItem>(_getDesktopItems());
		}
		public void addDesktopItem(DesktopItem di) {
			_getDesktopItems().add(di);
		}
		public void addDesktopItem(int index, DesktopItem di) {
			_getDesktopItems().add(index, di);
		}
		public void addAllDesktopItems(List<DesktopItem> dis) {
			_getDesktopItems().addAll(dis);
		}
		public boolean removeDesktopItem(DesktopItem di) {
			return _getDesktopItems().remove(di);
		}
		public void removeAllDesktopItems() {
			_getDesktopItems().clear();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof Desktop) {
				Desktop other = (Desktop) obj;
				return LangUtils.equals(this.desktopItems, other.desktopItems) &&
						this.horizontalGap == other.horizontalGap &&
						this.verticalGap == other.verticalGap &&
						this.menuItemTextSize == other.menuItemTextSize &&
						this.layout == other.layout &&
						LangUtils.equals(this.resourceMenuBackground, other.resourceMenuBackground) &&
						LangUtils.equals(this.resourceMenuBackgroundHover, other.resourceMenuBackgroundHover) &&
						LangUtils.equals(this.menuItemTextColor, other.menuItemTextColor) &&
						LangUtils.equals(this.menuItemTextHoverColor, other.menuItemTextHoverColor);
			}
			return super.equals(obj);
		}
	}
	
	@Override
	public int hashCode() {
		if (name == null)
			return 0;
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WorkspaceDescription) {
			WorkspaceDescription other = (WorkspaceDescription) obj;
			LangUtils.equals(getName(), other.getName());
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		if (name == null)
			return "null";
		return name.toString();
	}
	
	/**
	 * 
	 * @param wd
	 */
	public void importHeader(WorkspaceDescription wd) {
		setName(wd.getName());
		setHide(wd.isHide());
		setHideName(wd.isHideName());
		setHideMenuBar(wd.isHideMenuBar());
		setAlwaysOpenAtLogin(wd.isAlwaysOpenAtLogin());
		setNuclosResource(wd.getNuclosResource());
	}
	
	/**
	 * 
	 * @return
	 */
	public Frame getMainFrame() {
		for (Frame f : frames) {
			if (f.isMainFrame())
				return f;
		}
		throw new CommonFatalException("No main frame in workspace description");
	}
	
	/**
	 * 
	 * @param wd
	 * @return
	 */
	public List<Tabbed> getTabbeds() {
		List<Tabbed> result = new ArrayList<Tabbed>();
		for (Frame f : getFrames()) {
			result.addAll(getTabbeds(f.getContent()));
		}
		return result;
	}
	
	/**
	 * 
	 * @param nc
	 * @return
	 */
	public static List<Tabbed> getTabbeds(NestedContent nc) {
		List<Tabbed> result = new ArrayList<Tabbed>();
		if (nc instanceof MutableContent) {
			result.addAll(getTabbeds(((MutableContent) nc).getContent()));
		} else if (nc instanceof Split) {
			result.addAll(getTabbeds(((Split) nc).getContentA()));
			result.addAll(getTabbeds(((Split) nc).getContentB()));
		} else if (nc instanceof Tabbed) {
			result.add((Tabbed) nc);
		}
		return result;
	}
	
	/**
	 * 
	 * @param wd
	 * @return
	 * @throws CommonBusinessException 
	 */
	public Tabbed getHomeTabbed() throws CommonBusinessException {
		for (Tabbed tbb : getTabbeds()) {
			if (tbb.isHome()) {
				return tbb;
			}
		}
		throw new CommonBusinessException("Workspace.contains.no.home.tabbed");
	}
	
	/**
	 * 
	 * @param wd
	 * @return
	 * @throws CommonBusinessException 
	 */
	public Tabbed getHomeTreeTabbed() throws CommonBusinessException {
		for (Tabbed tbb : getTabbeds()) {
			if (tbb.isHomeTree()) {
				return tbb;
			}
		}
		throw new CommonBusinessException("Workspace.contains.no.home.tabbed");
	}
	
}

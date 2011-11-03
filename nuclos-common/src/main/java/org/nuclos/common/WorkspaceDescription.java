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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.util.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

public class WorkspaceDescription implements Serializable {
	private static final long serialVersionUID = 6637996725938917463L;

	private String name;
	private boolean hideName;
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

	public boolean isHideName() {
		return this.hideName;
	}

	public void setHideName(boolean hideName) {
		this.hideName = hideName;
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
		public void removeSubFormPreferences(SubFormPreferences sfp) {
			this._getSubFormPreferences().remove(sfp);
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
			if (obj instanceof EntityPreferences) {
				EntityPreferences other = (EntityPreferences) obj;
				LangUtils.equals(getEntity(), other.getEntity());
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
			if (obj instanceof SubFormPreferences) {
				SubFormPreferences other = (SubFormPreferences) obj;
				LangUtils.equals(getEntity(), other.getEntity());
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
		setHideName(wd.isHideName());
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

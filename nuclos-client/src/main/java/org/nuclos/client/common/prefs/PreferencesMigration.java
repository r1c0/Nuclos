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
package org.nuclos.client.common.prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.main.mainframe.MainFrameSpringComponent;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.WorkspaceDescription.ColumnPreferences;
import org.nuclos.common.WorkspaceDescription.ColumnSorting;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityFieldPref;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataForeignKeyEntityField;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.PreferencesException;

public class PreferencesMigration {
	
	protected static final Logger log = Logger.getLogger(PreferencesMigration.class);
	
	/**
	 * Key within user preferences under which the List of field names of an entity is stored.
	 *
	 * This is e.g. used to remember the fields/columns in the result panel of an entity.
	 */
	private static final String PREFS_NODE_SELECTEDFIELDS = "selectedFields";

	/**
	 * Key within user preferences under which the List of field entities of an entity is stored.
	 *
	 * This is e.g. used to remember the fields/columns in the result panel of an entity. The entity
	 * could be different for fields from subforms and pivot subforms.
	 */
	private static final String PREFS_NODE_SELECTEDFIELDENTITIES = "selectedFieldEntities";

	/**
	 * Key within user preferences under which the List of XML representations of a field of an entity is stored.
	 *
	 * The sequence is the same as for PREFS_NODE_SELECTEDFIELDENTITIES. For backward compatibility, if this
	 * list is missing, CollectableEntityFieldWithEntityForExternal is assumed for all field entities.
	 *
	 * @see org.nuclos.client.genericobject.GenericObjectClientUtils.getCollectableEntityFieldForResult(CollectableEntity, String, CollectableEntity)
	 */
	private static final String PREFS_NODE_SELECTEDFIELDBEANS = "selectedFieldBeans";

	private static final String PREFS_NODE_SELECTEDFIELDWIDTHS = "selectedFieldWidths";
	private static final String PREFS_NODE_ORDERBYSELECTEDFIELD = "orderBySelectedField";
	private static final String PREFS_NODE_ORDERASCENDING = "orderAscending";
	
	private static final String PREFS_NODE_FIXEDFIELDS = "fixedFields";
	private static final String PREFS_NODE_FIXEDFIELDS_WIDTHS = "fixedFieldWidths";
	
	private static PreferencesMigration INSTANCE;
	
	//
	
	// Spring injection
	
	private MainFrameSpringComponent mainFrameSpringComponent;
	
	// end of Spring injection
	
	PreferencesMigration() {
		INSTANCE = this;
	}
	
	public static PreferencesMigration getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setMainFrameSpringComponent(MainFrameSpringComponent mainFrameSpringComponent) {
		this.mainFrameSpringComponent = mainFrameSpringComponent;
	}
	
	public void migrateEntityAndSubFormColumnPreferences() {
		if (!mainFrameSpringComponent.getMainFrame().getWorkspace().getWoDesc().getEntityPreferences().isEmpty()) {
			return; // only migrate if new preferences are empty
		}
		
		// old preferences
		Preferences userPreferencesRoot = org.nuclos.common2.ClientPreferences.getUserPreferences();
		
		try {
			for (String entity : userPreferencesRoot.node("collect").node("entity").childrenNames()) {
				Preferences entityPrefs = userPreferencesRoot.node("collect").node("entity").node(entity);
				
				EntityPreferences ep = new EntityPreferences();
				ep.setEntity(entity);				
				
				/*
				 * SELECTED COLUMNS
				 */
				List<String> lstSelectedFieldNames = new ArrayList<String>();
				List<String> lstSelectedFieldEntities = new ArrayList<String>();
				List<Integer> lstSelectedFieldWidths = new ArrayList<Integer>();
				try {
					lstSelectedFieldWidths.addAll(PreferencesUtils.getIntegerList(entityPrefs, PREFS_NODE_SELECTEDFIELDWIDTHS));
				} catch (Exception e1) {
					log.error("PREFS_NODE_SELECTEDFIELDWIDTHS", e1);
				}
				
				boolean migrateOldPrefs = false;
				try {
					final List<?> raws = PreferencesUtils.getSerializableListXML(entityPrefs, PREFS_NODE_SELECTEDFIELDBEANS, false);
					if (raws.isEmpty())
						migrateOldPrefs = true;
					for (int i = 0; i < raws.size(); i++) {
						final Object raw = raws.get(i);
						final ColumnPreferences cp = new ColumnPreferences();
						if (raw instanceof CollectableEntityFieldPref) {
							final CollectableEntityFieldPref pref = (CollectableEntityFieldPref) raw;
							cp.setColumn(pref.getField());
							cp.setEntity(pref.getEntity());
							if (CollectableEOEntityField.class.getName().equals(pref.getType())) {
								cp.setType(ColumnPreferences.TYPE_EOEntityField);
								if (pref.getPivot() != null) {
									cp.setPivotSubForm(pref.getPivot().getSubform());
									cp.setPivotKeyField(pref.getPivot().getKeyField());
									cp.setPivotValueField(pref.getPivot().getValueField());
									cp.setPivotValueType(pref.getPivot().getValueType().getName());
								}
							}
							if (CollectableGenericObjectEntityField.class.getName().equals(pref.getType()))
								cp.setType(ColumnPreferences.TYPE_GenericObjectEntityField);
							if (CollectableMasterDataForeignKeyEntityField.class.getName().equals(pref.getType()))
								cp.setType(ColumnPreferences.TYPE_MasterDataForeignKeyEntityField);
							if (CollectableEntityFieldWithEntityForExternal.class.getName().equals(pref.getType()))
								cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntityForExternal);
							if (CollectableEntityFieldWithEntity.class.getName().equals(pref.getType()))
								cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntity);
							
							cp.setWidth(lstSelectedFieldWidths.size()>i?lstSelectedFieldWidths.get(i):100);
							
							ep.getResultPreferences().addSelectedColumnPreferences(cp);
						}
						// compatibility case
						else if (raw instanceof CollectableEntityField) {
							CollectableEntityField clctef = (CollectableEntityField) raw;
							cp.setColumn(clctef.getName());
							cp.setEntity(clctef.getEntityName());
							
							ep.getResultPreferences().addSelectedColumnPreferences(cp);
						}
					}
				} catch (Exception e1) {
					migrateOldPrefs = true;
					log.error("PREFS_NODE_SELECTEDFIELDBEANS", e1);
				}
				if (migrateOldPrefs) {
					try {
						lstSelectedFieldNames.addAll(PreferencesUtils.getStringList(entityPrefs, PREFS_NODE_SELECTEDFIELDS));
						lstSelectedFieldEntities.addAll(PreferencesUtils.getStringList(entityPrefs, PREFS_NODE_SELECTEDFIELDENTITIES));
					} catch (PreferencesException e2) {
						log.error("PREFS_NODE_SELECTEDFIELDS", e2);
					}
					for (int i = 0; i < lstSelectedFieldNames.size(); i++) {
						final ColumnPreferences cp = new ColumnPreferences();
						cp.setColumn(lstSelectedFieldNames.get(i));
						cp.setWidth(lstSelectedFieldWidths.size()>i?lstSelectedFieldWidths.get(i):100);
						if (lstSelectedFieldEntities.size() > i) {
							cp.setEntity(lstSelectedFieldEntities.get(i));
						}
						ep.getResultPreferences().addSelectedColumnPreferences(cp);
					}
				}
				
				/*
				 * FIXED COLUMNS
				 */
				try {
					List<String> fixedSelectedFieldNames = PreferencesUtils.getStringList(entityPrefs, PREFS_NODE_FIXEDFIELDS);
					for (String field : fixedSelectedFieldNames) {
						for (ColumnPreferences cp : ep.getResultPreferences().getSelectedColumnPreferences()) {
							if (LangUtils.equals(cp.getColumn(), field)) {
								cp.setFixed(true);
							}
						}
					}
				}
				catch (PreferencesException ex) {
					log.error("PREFS_NODE_FIXEDFIELDS", ex);
				}
				
				/*
				 * COLUMN SORTING
				 */
				try {
					List<SortKey> sortKeys = readSortKeysFromPrefs(entityPrefs);
					for (SortKey sk : sortKeys) {
						if (lstSelectedFieldNames.size() > sk.getColumn()) {
							ColumnSorting cs = new ColumnSorting();
							cs.setColumn(lstSelectedFieldNames.get(sk.getColumn()));
							cs.setAsc(sk.getSortOrder() == SortOrder.ASCENDING);
							ep.getResultPreferences().addColumnSorting(cs);
						}
					}
				} catch (PreferencesException e) {
					log.error("PREFS_NODE_ORDERBYSELECTEDFIELD & PREFS_NODE_ORDERASCENDING", e);
				}
				
				/*
				 * HIDDEN COLUMNS
				 */
				try {
					for (String field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).keySet()) {
						if (!lstSelectedFieldNames.contains(field)) {
							ep.getResultPreferences().addHiddenColumn(field);
						}
					}
				} catch (Exception e) {
					log.error("HIDDEN COLUMNS", e);
				}
				
				/*
				 * REMOVE ENTITY NODES
				 */
				if (true) {
					entityPrefs.node(PREFS_NODE_SELECTEDFIELDS).removeNode();
					entityPrefs.node(PREFS_NODE_SELECTEDFIELDWIDTHS).removeNode();
					entityPrefs.node(PREFS_NODE_FIXEDFIELDS).removeNode();
					entityPrefs.node(PREFS_NODE_ORDERBYSELECTEDFIELD).removeNode();
					entityPrefs.node(PREFS_NODE_ORDERASCENDING).removeNode();
					entityPrefs.node(PREFS_NODE_SELECTEDFIELDENTITIES).removeNode();
					entityPrefs.node(PREFS_NODE_SELECTEDFIELDBEANS).removeNode();
				}
				
				/*
				 * SUBFORMS
				 */
				for (String subform : entityPrefs.node("subentity").childrenNames()) {
					Preferences subFormPrefs = entityPrefs.node("subentity").node(subform);
					
					SubFormPreferences sfp = new SubFormPreferences();
					sfp.setEntity(subform);
					ep.addSubFormPreferences(sfp);
					
					// SELECTED AND FIXED
					List<String> subformColumns = new ArrayList<String>();
					try {
						List<String> fixedFields = PreferencesUtils.getStringList(subFormPrefs, PREFS_NODE_FIXEDFIELDS);
						subformColumns.addAll(fixedFields);
						subformColumns.addAll(PreferencesUtils.getStringList(subFormPrefs, PREFS_NODE_SELECTEDFIELDS));
						
						try {
							List<Integer> widths = PreferencesUtils.getIntegerList(subFormPrefs, PREFS_NODE_FIXEDFIELDS_WIDTHS);
							widths.addAll(PreferencesUtils.getIntegerList(subFormPrefs, PREFS_NODE_SELECTEDFIELDWIDTHS));
							for (int i = 0; i < subformColumns.size(); i++) {
								ColumnPreferences cp = new ColumnPreferences();
								cp.setColumn(subformColumns.get(i));
								cp.setWidth(widths.size()>i?widths.get(i):100);
								cp.setFixed(fixedFields.contains(cp.getColumn()));
								sfp.getTablePreferences().addSelectedColumnPreferences(cp);
							}
						}
						catch (PreferencesException ex) {
							log.error("PREFS_NODE_FIXEDFIELDS_WIDTHS & PREFS_NODE_SELECTEDFIELDWIDTHS", ex);
						}
					} catch (PreferencesException e) {
						log.error("PREFS_NODE_FIXEDFIELDS & PREFS_NODE_SELECTEDFIELDS", e);
					}
					
					// COLUMN SORTING
					try {
						List<SortKey> sortKeys = readSortKeysFromPrefs(subFormPrefs);
						for (SortKey sk : sortKeys) {
							if (subformColumns.size() > sk.getColumn()) {
								ColumnSorting cs = new ColumnSorting();
								cs.setColumn(subformColumns.get(sk.getColumn()));
								cs.setAsc(sk.getSortOrder() == SortOrder.ASCENDING);
								sfp.getTablePreferences().addColumnSorting(cs);
							}
						}
					} catch (PreferencesException e) {
						log.error("PREFS_NODE_ORDERBYSELECTEDFIELD & PREFS_NODE_ORDERASCENDING", e);
					}
					
					// HIDDEN COLUMNS
					try {
						for (String field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(subform).keySet()) {
							if (!subformColumns.contains(field)) {
								sfp.getTablePreferences().addHiddenColumn(field);
							}
						}
					} catch (Exception e) {
						log.error("HIDDEN COLUMNS", e);
					}
					
					// REMOVE SUBFORM NODES
					if (true) {
						subFormPrefs.node(PREFS_NODE_SELECTEDFIELDS).removeNode();
						subFormPrefs.node(PREFS_NODE_SELECTEDFIELDWIDTHS).removeNode();
						subFormPrefs.node(PREFS_NODE_FIXEDFIELDS).removeNode();
						subFormPrefs.node(PREFS_NODE_FIXEDFIELDS_WIDTHS).removeNode();
						subFormPrefs.node(PREFS_NODE_ORDERBYSELECTEDFIELD).removeNode();
						subFormPrefs.node(PREFS_NODE_ORDERASCENDING).removeNode();
					}
				}
			}
		} catch (BackingStoreException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private List<SortKey> readSortKeysFromPrefs(Preferences prefs) throws PreferencesException {
		List<Integer> sortColumns = PreferencesUtils.getIntegerList(prefs, PREFS_NODE_ORDERBYSELECTEDFIELD);
		List<Integer> sortOrders = PreferencesUtils.getIntegerList(prefs, PREFS_NODE_ORDERASCENDING);

		List<SortKey> sortKeys = new ArrayList<SortKey>(sortColumns.size());
		for (int i = 0, n = sortColumns.size(); i < n; i++) {
			int column = sortColumns.get(i);
			if (column == -1)
				continue;
			// ascending is the default
			SortOrder order = (i < sortOrders.size() && sortOrders.get(i) == 0) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
			sortKeys.add(new SortKey(column, order));
		}
		return sortKeys;
	}
}

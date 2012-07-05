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
package org.nuclos.client.task;

import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.OneDropNuclosDropTargetListener;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.IMainFrameTabClosableController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.collect.model.GenericObjectsResultTableModel;
import org.nuclos.client.ui.collect.model.MasterDataResultTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * View on a list of leased objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectTaskView extends TaskView implements IMainFrameTabClosableController, NuclosDropTargetVisitor{

	private static final Logger LOG = Logger.getLogger(GenericObjectTaskView.class);
	
	private static final String PREFS_NODE_SELECTEDFIELDS = "selectedFields";
	private static final String PREFS_NODE_SELECTEDFIELDWIDTHS = "selectedFieldWidths";

	private final JMenuItem btnPrint = new JMenuItem();
	private final JMenuItem btnRename = new JMenuItem();

	private final JTable tbl = new CommonJTable();

	private EntitySearchFilter filter;

	public GenericObjectTaskView(EntitySearchFilter filter) {
		this.setFilter(filter);
		
		this.tbl.setRowHeight(SubForm.MIN_ROWHEIGHT);
		this.tbl.setTableHeader(new TaskViewTableHeader(tbl.getColumnModel()));
	}

	@Override
	public void init() {
		super.init();
		setupDragDrop();
	}
	
	JMenuItem getPrintMenuItem() {
		return btnPrint;
	}
	
	JMenuItem getRenameMenuItem() {
		return btnRename;
	}
	
	protected void setupDragDrop() {		
		OneDropNuclosDropTargetListener listener = new OneDropNuclosDropTargetListener(this, ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_DRAG_CURSOR_HOLDING_TIME, 600));		
		DropTarget drop = new DropTarget(tbl, listener);
		drop.setActive(true);		
	}

	/**
	 * @return the table containing leased objects
	 */
	JTable getJTable() {
		return this.tbl;
	}

	public CollectableTableModel<Collectable> getTableModel() {
		return (CollectableTableModel<Collectable>) this.tbl.getModel();
	}

	/**
	 * sets the filter for this view
	 * @param filter
	 */
	public void setFilter(EntitySearchFilter filter) {
		if (this.filter != filter) {
			this.filter = filter;
			//this.refresh();
		}
	}

	/**
	 * @return the filter for this view
	 */
	public EntitySearchFilter getFilter() {
		return this.filter;
	}

	/**
	 * @param filter
	 * @param collclct
	 * @return a new collectable table model containing <code>collclct</code>.
	 */
	public final SortableCollectableTableModel<Collectable> newResultTableModel(final EntitySearchFilter filter, Collection<? extends Collectable> collclct) {
		final CollectableEntity clcteMain = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(filter.getEntityName());//CollectableGenericObjectEntity.getByModuleId(filter.getModuleId());
		final SortableCollectableTableModel<Collectable> result;
		if (Modules.getInstance().isModuleEntity(filter.getEntityName())) {
			result = new GenericObjectsResultTableModel<Collectable>(clcteMain, getColumnOrderFromPreferences());
		}
		else {
			result = new MasterDataResultTableModel<Collectable>(clcteMain, getColumnOrderFromPreferences());

		}
		result.setCollectables(new ArrayList<Collectable>(collclct));

		if (result.getColumnCount() > 0) {
			// setup sorted fields and sorting order from preferences
			try {
				result.setSortKeys(readColumnOrderFromPreferences(), true);
			} catch (IllegalArgumentException e) {
				result.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)), false);
			}
		}

		// enable sorting by clicking the header:
		/*TableUtils.addMouseListenerForSortingToTableHeader(tbl, result, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				List<? extends SortKey> sortKeys = result.getSortKeys();
				List<SortKey> filteredSortKeys = CollectionUtils.applyFilter(sortKeys, new Predicate<SortKey>() {
					@Override
					public boolean evaluate(SortKey k) {
						final CollectableEntityFieldWithEntity clctefSorted = (CollectableEntityFieldWithEntity) result.getCollectableEntityField(k.getColumn());
						return clctefSorted.getCollectableEntityName().equals(clcteMain.getName());
					}
				});
				if (filteredSortKeys.size() == sortKeys.size()) {
					result.sort();
				} else {
					result.setSortKeys(Collections.<SortKey>emptyList(), false);
					throw new CommonBusinessException(
							getSpringLocaleDelegate().getMessage(
									"GenericObjectTaskView.1", "Das Suchergebnis kann nicht nach Unterformularspalten sortiert werden."));
				}
			}
		});*/

		return result;
	}

	private List<? extends CollectableEntityField> getColumnOrderFromPreferences() {
		List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		Map<String,CollectableEntityField> entityFields = new HashMap<String,CollectableEntityField>();

		for (CollectableEntityField cef : filter.getVisibleColumns())
			entityFields.put(cef.getName(), cef);

		final List<String> lstFieldNames = new LinkedList<String>(CollectableUtils.getFieldNamesFromCollectableEntityFields(filter.getVisibleColumns()));

		// sort using the order stored in the preferences:
		List<String> lstFieldNameOrderTemp;
		try {
			lstFieldNameOrderTemp = PreferencesUtils.getStringList(getPreferences(), PREFS_NODE_SELECTEDFIELDS);
		}
		catch (PreferencesException ex) {
			LOG.error("Failed to retrieve list of selected fields from the preferences. They will be empty.");
			lstFieldNameOrderTemp = new ArrayList<String>();
		}
		final List<String> lstFieldNameOrder = lstFieldNameOrderTemp;

		Collections.sort(lstFieldNames, new Comparator<String>() {
			private int getOrder(Object o) {
				int result = lstFieldNameOrder.indexOf(o);
				if (result == -1) {
					// new fields are shown at the end:
					result = lstFieldNameOrder.size();
				}
				return result;
			}

			@Override
			public int compare(String o1, String o2) {
				int iDiff = getOrder(o1) - getOrder(o2);
				return (iDiff == 0) ? 0 : (iDiff / Math.abs(iDiff));
			}
		});

		final List<String> lstNamesOfFieldsToDisplay = CollectableUtils.getFieldNamesFromCollectableEntityFields(filter.getVisibleColumns());
		for (Iterator<String> iter = lstFieldNames.iterator(); iter.hasNext();) {
			final String sFieldName = iter.next();
			final CollectableEntityField clctef = entityFields.get(sFieldName);
			if (lstNamesOfFieldsToDisplay.contains(sFieldName)) {
				result.add(clctef);
			}
		}

		return result;
	}

	public void storeOrderBySelectedColumnToPreferences() {
		if (getTableModel() instanceof SortableCollectableTableModel<?>) {
			try {
				PreferencesUtils.writeSortKeysToPrefs(getPreferences(), ((SortableCollectableTableModel<?>) getTableModel()).getSortKeys());
			} catch (PreferencesException e1) {
				Errors.getInstance().showExceptionDialog(this.getParent(), 
						getSpringLocaleDelegate().getMessage(
								"gotaskview.error.save.sortorder", "Fehler beim Abspeichern der Sortierreihenfolge des Suchfilters."), e1);
			}
		}
	}

	/**
	 * Reads the user-preferences for the sorting order.
	 */
	private List<SortKey> readColumnOrderFromPreferences() {
		try {
			return PreferencesUtils.readSortKeysFromPrefs(getPreferences());
		}
		catch (PreferencesException ex) {
			LOG.error("The column order could not be loaded from preferences.", ex);
			return Collections.emptyList();
		}
	}
	
	public List<Integer> readColumnWidthsFromPreferences() {
		List<Integer> lstColumnWidths = null;
		try {
			lstColumnWidths = PreferencesUtils.getIntegerList(getPreferences(), PREFS_NODE_SELECTEDFIELDWIDTHS);
		}
		catch (PreferencesException ex) {
			LOG.error("Die Spaltenbreite konnte nicht aus den Preferences geladen werden.", ex);
			return lstColumnWidths;
		}

		return lstColumnWidths;
	}

	private Preferences getPreferences() {
		return org.nuclos.common2.ClientPreferences.getUserPreferences().node("taskPanel").node(getFilter().getId().toString());
	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		Point here = dtde.getLocation();
		int hereRow = tbl.rowAtPoint(here);
		
		CollectableTableModel<Collectable> model = getTableModel();							
		
		final Collectable clctSelected = model.getCollectable(hereRow);
		try {
			if (clctSelected != null) {
				if (Modules.getInstance().isModuleEntity(getFilter().getEntityName())) {
					final CollectableGenericObject clctloSelected = (CollectableGenericObject) clctSelected;
					// we must reload the partially loaded object:
					final int iModuleId = clctloSelected .getGenericObjectCVO().getModuleId();
					GenericObjectClientUtils.showDetails(iModuleId, clctloSelected.getId());
				}
				else {
					final CollectableMasterDataWithDependants clctmdSelected = (CollectableMasterDataWithDependants) clctSelected;
					Main.getInstance().getMainController().showDetails(clctmdSelected.getCollectableEntity().getName(), clctmdSelected.getId());
				}					
			}
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}	
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	@Override
	protected List<JComponent> getToolbarComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<JComponent> getExtrasMenuComponents() {
		List<JComponent> result = new ArrayList<JComponent>();
		result.add(btnPrint);
		result.add(btnRename);
		return result;
	}

	@Override
	protected JTable getTable() {
		return tbl;
	}
}	// class GenericObjectTaskView

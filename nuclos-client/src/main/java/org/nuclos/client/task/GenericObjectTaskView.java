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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.OneDropNuclosDropTargetListener;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.IMainFrameTabClosableController;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.collect.model.GenericObjectsResultTableModel;
import org.nuclos.client.ui.collect.model.MasterDataResultTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
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

	private final JToolBar toolbar = UIUtils.createNonFloatableToolBar();
	private final JButton btnRefresh = new JButton();
	private final JMenuItem btnPrint = new JMenuItem();
	private final JMenuItem btnRename = new JMenuItem();

	private final PopupButton popupExtras = new PopupButton(CommonLocaleDelegate.getMessage("PopupButton.Extras","Extras"));

	private final JScrollPane scrlpn = new JScrollPane();
	private final JTable tbl = new CommonJTable();

	public final JTextField tfStatusBar = new StatusBarTextField(" ");

	private EntitySearchFilter filter;

	private final Logger log = Logger.getLogger(this.getClass());

	public GenericObjectTaskView() {
		this.init();
	}	// ctor

	public GenericObjectTaskView(EntitySearchFilter filter) {
		this();
		this.setFilter(filter);
	}

	private void init() {
		this.setLayout(new BorderLayout());
		this.add(toolbar, BorderLayout.NORTH);
		this.add(scrlpn, BorderLayout.CENTER);
		this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);
		this.tfStatusBar.setMinimumSize(new Dimension(0, this.tfStatusBar.getPreferredSize().height));

		toolbar.add(btnRefresh);
		btnRefresh.setToolTipText(CommonLocaleDelegate.getMessage("PersonalTaskController.3","Aufgabenliste aktualisieren"));
		this.popupExtras.add(btnPrint);
		
		this.popupExtras.add(btnRename);
		
		super.addRefreshIntervalsToPopupButton(popupExtras);
		this.toolbar.add(popupExtras);

//		toolbar.add(btnSelectColumns);
//		this.btnSelectColumns.setIcon(Icons.getInstance().iconSelectColumn16);
//		this.btnSelectColumns.setToolTipText("Spalten ein-/ausblenden");

		scrlpn.getViewport().add(tbl, null);
		tbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrlpn.setBackground(Color.WHITE);
		tbl.setBackground(Color.WHITE);
		UIUtils.setupCopyAction(tbl);
		setupDragDrop();
	}
	
	JButton getRefreshButton() {
		return btnRefresh;
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
		TableUtils.addMouseListenerForSortingToTableHeader(tbl, result, new CommonRunnable() {
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
						CommonLocaleDelegate.getMessage("GenericObjectTaskView.1", "Das Suchergebnis kann nicht nach Unterformularspalten sortiert werden."));
				}
			}
		});

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
			lstFieldNameOrderTemp = PreferencesUtils.getStringList(getPreferences(), CollectController.PREFS_NODE_SELECTEDFIELDS);
		}
		catch (PreferencesException ex) {
			log.error("Failed to retrieve list of selected fields from the preferences. They will be empty.");
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
				CollectController.writeSortKeysToPrefs(getPreferences(), ((SortableCollectableTableModel<?>) getTableModel()).getSortKeys());
			} catch (PreferencesException e1) {
				Errors.getInstance().showExceptionDialog(this.getParent(), 
					CommonLocaleDelegate.getMessage("gotaskview.error.save.sortorder", "Fehler beim Abspeichern der Sortierreihenfolge des Suchfilters."), e1);
			}
		}
	}

	/**
	 * Reads the user-preferences for the sorting order.
	 */
	private List<SortKey> readColumnOrderFromPreferences() {
		try {
			return CollectController.readSortKeysFromPrefs(getPreferences());
		}
		catch (PreferencesException ex) {
			log.error("The column order could not be loaded from preferences.", ex);
			return Collections.emptyList();
		}
	}
	
	public List<Integer> readColumnWidthsFromPreferences() {
		List<Integer> lstColumnWidths = null;
		try {
			lstColumnWidths = PreferencesUtils.getIntegerList(getPreferences(), CollectController.PREFS_NODE_SELECTEDFIELDWIDTHS);
		}
		catch (PreferencesException ex) {
			log.error("Die Spaltenbreite konnte nicht aus den Preferences geladen werden.", ex);
			return lstColumnWidths;
		}

		return lstColumnWidths;
	}

	private Preferences getPreferences() {
		return org.nuclos.common2.ClientPreferences.getUserPreferences().node("taskPanel").node(getFilter().getId().toString());
	}

	@Override
	public boolean isClosable() {
		return !this.filter.isForced();
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
					GenericObjectClientUtils.showDetails(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iModuleId)).getEntity()), iModuleId, clctloSelected.getId());
				}
				else {
					final CollectableMasterDataWithDependants clctmdSelected = (CollectableMasterDataWithDependants) clctSelected;
					Main.getMainController().showDetails(clctmdSelected.getCollectableEntity().getName(), clctmdSelected.getId());
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
	
	
	
//	/**
//	 * command: select columns
//	 * Lets the user select the columns to show in the result list.
//	 */
//	private void cmdSelectColumns() {
//		final SelectColumnsController ctl = new SelectColumnsController(this);
//		final List lstAvailable = fields.getAvailableFields();
//		final List lstSelected = fields.getSelectedFields();
//		final boolean bOK = ctl.run(lstAvailable, lstSelected);
//
//		if (bOK) {
//			final CollectableTableModel model = (CollectableTableModel) this.getCollectPanel().
//			    getResultPanel().getResultTable().getModel();
//			this.fields.set(ctl.getAvailableColumns(), ctl.getSelectedColumns());
//			model.setColumns(this.fields.getSelectedFields());
//			this.storeSelectedFieldsInPrefs(this.fields.getSelectedFields());
//			TableUtils.setPreferredColumnWidth(this.getCollectPanel().getResultPanel().getResultTable(), 100, TABLE_INSETS);
//		}
//	}

}	// class GenericObjectTaskView

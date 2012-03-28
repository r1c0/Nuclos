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
package org.nuclos.client.common;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModelImpl;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFactory;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for collecting dependant data (in a one-to-many relationship) in a subform,
 * without the hassles concerning ValueObjectList or parent id.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class AbstractDetailsSubFormController<Clct extends Collectable>
		extends SubFormController
		implements CollectableFactory<Clct> {
	
	private static final Logger LOG = Logger.getLogger(AbstractDetailsSubFormController.class);

	/**
	 * Successors must call postCreate.
	 * @param parent
	 * @param parentMdi
	 * @param clctcompmodelproviderParent
	 * @param sParentEntityName
	 * @param subform
	 * @param prefsUserParent
	 * @param clctfproviderfactory
	 * @see #postCreate()
	 */
	public AbstractDetailsSubFormController(CollectableEntity clcte, Component parent, JComponent parentMdi, CollectableComponentModelProvider clctcompmodelproviderParent,
			String sParentEntityName, SubForm subform, Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderFactory clctfproviderfactory) {

		super(clcte, parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform, false, prefsUserParent, entityPrefs, clctfproviderfactory);

		// initialize table model:
		DetailsSubFormTableModel<Clct>  tblmdl = new DetailsSubFormTableModelImpl<Clct>(this.newCollectableList(new ArrayList<Clct>()));
		tblmdl.setColumns(getTableColumns());
		tblmdl.addSortingListener(newSubFormTablePreferencesUpdateListener());
		getJTable().setModel(tblmdl);

		// Inititialize listeners for toolbar actions:
		ListenerUtil.registerSubFormToolListener(subform, new SubFormToolListener() {
			@Override
			public void toolbarAction(String actionCommand) {
				if(SubForm.ToolbarFunction.fromCommandString(actionCommand) == SubForm.ToolbarFunction.MULTIEDIT)
					cmdMultiEdit();
			}
		});

		if (this.getCollectableTableModel().getColumnCount() > 0) {
			SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);
			final String sInitialSortingColumn = subform.getInitialSortingColumn();
			if (sInitialSortingColumn != null) {
				final String sInitialSortingOrder = subform.getInitialSortingOrder();
				final int iColumn = this.getCollectableTableModel().findColumnByFieldName(sInitialSortingColumn);
				if (iColumn >= 0) {
					sortKey = new SortKey(iColumn, sInitialSortingOrder.equals("ascending") ? SortOrder.ASCENDING : SortOrder.DESCENDING);
				}
			}
			this.getCollectableTableModel().setSortKeys(Collections.singletonList(sortKey), true);
		}

		if (this.isColumnSelectionAllowed(sParentEntityName)) {
			final List<String> lstStoredFieldNames = WorkspaceUtils.getSelectedColumns(getSubFormPrefs());
			if (!lstStoredFieldNames.isEmpty()) {
				removeColumnsFromTableColumnModel(subform.getJTable(), lstStoredFieldNames, true);
			}
			else {
				List<String> lstSystemFields = new ArrayList<String>();
				for(NuclosEOField field : NuclosEOField.values()) {
					lstSystemFields.add(field.getMetaData().getField());
				}
				removeColumnsFromTableColumnModel(subform.getJTable(), lstSystemFields, false);
			}
		}

	}

	/**
	 * @return a new <code>Collectable</code> for this subform.
	 */
	@Override
	public abstract Clct newCollectable();

	/**
	 * @param lstclct
	 * @return a new list of collectables for the given list. Successors may want to wrap the given list.
	 */
	protected abstract List<Clct> newCollectableList(List<Clct> lstclct);

	/**
	 * @return the table model used for the subform. May be used to add/remove/change rows in the subform.
	 */
    public final SortableCollectableTableModel<Clct> getCollectableTableModel() {
		return (DetailsSubFormTableModel<Clct>) getJTable().getModel();
	}

	/**
	 * @return the table model used for the subform. May be used to add/remove/change rows in the subform.
	 */
    @Override
	protected final SubForm.SubFormTableModel getSubFormTableModel() {
		return (DetailsSubFormTableModel<Clct>) getJTable().getModel();
	}

	/**
	 * @return an unmodifiable list containing the rows in the table model.
	 */
	public final List<Clct> getCollectables() {
		return this.getCollectableTableModel().getCollectables();
	}

	protected List<Clct> getModifiableListOfCollectables() {
		/** @todo elimminate this workaround - why not make getRows() public? */
		return ((DetailsSubFormTableModelImpl<Clct>) this.getCollectableTableModel()).getRows();
	}

	/**
	 * stops editing and assigns the given list to this subform's table model.
	 * @param lstclct
	 */
	protected final void setCollectables(List<Clct> lstclct) {
		// Stop editing the current cell if any; else controls may be left over / UA
		this.stopEditing();
		isIgnorePreferencesUpdate = true;
		this.getCollectableTableModel().setCollectables(this.newCollectableList(lstclct));
		isIgnorePreferencesUpdate = false;
	}

	/**
	 * updates the table model with the given list of <code>Collectable</code>s.
	 * @todo make this one public or fillSubForm()?
	 * @param lstclct List<Collectable>
	 * @todo refactor: setCollectables(lstclct, bSortTable)
	 */
	public void updateTableModel(List<Clct> lstclct) {
		this.setCollectables(lstclct);
		isIgnorePreferencesUpdate = true;
		this.getCollectableTableModel().sort();
		isIgnorePreferencesUpdate = false;
	}

	@Override
	protected Clct insertNewRow() throws CommonBusinessException {
		Clct clct = this.newCollectable();
		this.getCollectableTableModel().add(clct);
		return clct;
	}

	@Override
	public boolean isColumnEnabled(String sColumnName) {
		return this.isEnabled() && this.getSubForm().isColumnEnabled(sColumnName);
	}

	/**
	 * lets the user add/remove multiple rows at once. This requires the subform to define a unique master data column.
	 */
	public void cmdMultiEdit() {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
			public void run() {
				final SubForm subform = AbstractDetailsSubFormController.this.getSubForm();
				if (subform.getUniqueMasterColumnName() == null) {
					throw new IllegalStateException("No unique master column defined for subform " + subform.getEntityName() + ".");
				}

				if (AbstractDetailsSubFormController.this.stopEditing()) {
					new SubFormMultiEditController<Clct>(subform, AbstractDetailsSubFormController.this).run();
				}
			}
		});
	}

	/**
	 * @param sParentEntityName
	 * @return May the user select the columns to display?
	 * This implementation returns true for module entities only, not for masterdata entities
	 */
	protected boolean isColumnSelectionAllowed(String sParentEntityName) {
		return true;//Modules.getInstance().isModuleEntity(sParentEntityName);
	}

	/**
	 * must be called at the end of the ctor in each subclass.
	 */
	protected void postCreate() {
		final JTable tbl = this.getJTable();

		this.setupTableCellRenderers(this.getSubFormTableModel());
		this.setupRowHeight(this.getSubFormTableModel());
		this.setupStaticTableCellEditors(tbl);

		this.setColumnWidths();
		this.setColumnOrder();

		this.setupTableModelListener();
		this.setupColumnModelListener();

		TableUtils.addMouseListenerForSortingToTableHeader(tbl, this.getCollectableTableModel());
	}
	
	@Override
	protected final PreferencesUpdateListener newSubFormTablePreferencesUpdateListener() {
		return new PreferencesUpdateListener();
	}
	
	protected class PreferencesUpdateListener extends SubFormController.PreferencesUpdateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent ev) {
//			System.out.println("stateChanged " + ev);
			if (!isIgnorePreferencesUpdate) {
				storeColumnOrderToPreferences();
			}
		}
	}

	protected void storeColumnOrderToPreferences(){
		WorkspaceUtils.setSortKeys(getSubFormPrefs(), getCollectableTableModel().getSortKeys(), new WorkspaceUtils.IColumnNameResolver() {	
			@Override
			public String getColumnName(int iColumn) {
				return getSubFormTableModel().getColumnFieldName(iColumn);
			}
		});
	}

	/**
	 * Reads the user-preferences for the sorting order.
	 */
	protected List<SortKey> readColumnOrderFromPreferences() {
		return WorkspaceUtils.getSortKeys(getSubFormPrefs(), new WorkspaceUtils.IColumnIndexRecolver() {
			@Override
			public int getColumnIndex(String columnIdentifier) {
				return getCollectableTableModel().findColumnByFieldName(columnIdentifier);
			}
		});
	}

	/**
	 * sorts the TableModel by a given column if declared in preferences.
	 */
	protected void setColumnOrder() {
		LOG.debug("setColumnOrder");
		
		isIgnorePreferencesUpdate = true;
		List<SortKey> sortKeys = readColumnOrderFromPreferences();
		if (this.getCollectableTableModel().getColumnCount() > 0) {
			try {
				this.getCollectableTableModel().setSortKeys(sortKeys, false);
			} catch (IllegalArgumentException e) {
				LOG.warn("Sorting in subform \"" + this.getParentEntityName() + "." + this.getSubForm().getEntityName() +
					"\" could not be restored. Column count has changed.", e);
			}
		}
		isIgnorePreferencesUpdate = false;
	}

	/**
	 * <code>TableModel</code> that can be used in a Details subform.
	 */
	protected static interface DetailsSubFormTableModel<Clct extends Collectable> extends SubForm.SubFormTableModel, SortableCollectableTableModel<Clct> {
	}

	/**
	 * implementation of <code>DetailsSubFormTableModel</code>.
	 */
	protected class DetailsSubFormTableModelImpl<T extends Collectable>
			extends SortableCollectableTableModelImpl<T>
			implements DetailsSubFormTableModel<T> {

		/**
		 * @param lstclct
		 */
		DetailsSubFormTableModelImpl(List<T> lstclct) {
			super(getEntityAndForeignKeyFieldName().getEntityName(), lstclct);
		}

		@Override
		protected List<T> getRows() {
			return super.getRows();
		}

		/**
		 * @param clctef
		 * @return a null value that can be set in a table cell for the given entity field.
		 */
		@Override
		public Object getNullValue(CollectableEntityField clctef) {
			return CollectableUtils.getNullField(clctef);
		}

		@Override
		public boolean isCellEditable(int iRow, int iColumn) {
			final String sColumnName = this.getCollectableEntityField(iColumn).getName();
			return AbstractDetailsSubFormController.this.isColumnEnabled(sColumnName) && AbstractDetailsSubFormController.this.isRowEditable(iRow);
		}

		/**
		 * @deprecated Strongly consider to use {@link #getCollectableEntityField(int)} instead.
		 */
		@Override
		public String getColumnName(int iColumn) {
			String sLabel = AbstractDetailsSubFormController.this.getSubForm().getColumnLabel(this.getCollectableEntityField(iColumn).getName());
			if (sLabel != null) {
				return sLabel;
			}
			return this.getCollectableEntityField(iColumn).getLabel();
		}

		@Override
		public String getColumnFieldName(int columnIndex) {
			return getCollectableEntityField(columnIndex).getName();
		}

		@Override
		public int getMinimumColumnWidth(int columnIndex) {
			return TableUtils.getMinimumColumnWidth(getCollectableEntityField(columnIndex).getJavaClass());
		}
	}	// class DetailsSubFormTableModelImpl

}	// class AbstractDetailsSubFormController

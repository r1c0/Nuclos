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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.SizeKnownEvent;
import org.nuclos.client.ui.SizeKnownListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormTableModel;
import org.nuclos.client.ui.collect.SubForm.ToolbarFunction;
import org.nuclos.client.ui.collect.SubFormParameterProvider;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.CollectableEntityFieldBasedTableModel;
import org.nuclos.client.ui.table.TableCellEditorProvider;
import org.nuclos.client.ui.table.TableCellRendererProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFieldNotInModelException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils.GivenFieldOrderComparator;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Controller for collecting or searching for dependant data (in a one-to-many relationship) in a subform.
 * @todo move to common?
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class SubFormController extends Controller
		implements TableCellRendererProvider, TableCellEditorProvider, 
		SubFormParameterProvider, FocusActionListener, Closeable {

	private static final Logger LOG = Logger.getLogger(SubFormController.class);

	private static final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

	/**
	 * this controller's subform
	 */
	private SubForm subform;
	/**
	 * the parent for new MDI (internal) frames
	 */
	private final JComponent parentMdi;
	/**
	 * the <code>CollectableEntity</code> of the subform.
	 */
	private final CollectableEntity clcte;
	/**
	 * Is this controller's subform searchable (to be used in a Search panel)?
	 */
	private final boolean bSearchable;
	private final CollectableComponentModelProvider clctcompmodelproviderParent;
	private final String sParentEntityName;
	private final Preferences prefs;
	private final SubFormPreferences subFormPrefs;
	private ListSelectionListener listselectionlistener;
	private final CollectableFieldsProviderFactory clctfproviderfactory;
	
	protected boolean isIgnorePreferencesUpdate = true;
	
	private boolean closed = false;

	/**
	 * @param parent
	 * @param parentMdi
	 * @param clctcompmodelproviderParent provides the enclosing <code>CollectController</code>'s <code>CollectableComponentModel</code>s.
	 * This avoids handing the whole <code>CollectController</code> to the <code>SubFormController</code>.
	 * May be <code>null</code> if there are no dependencies from subform columns to fields of the main form.
	 * @param sParentEntityName
	 * @param subform
	 * @param bSearchable
	 * @param prefsUserParent the preferences of the parent (controller)
	 * @param clctfproviderfactory
	 * @precondition prefsUserParent != null
	 */
	public SubFormController(CollectableEntity clcte, Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, SubForm subform,
			boolean bSearchable, Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderFactory clctfproviderfactory) {

		super(parent);
		this.clcte = clcte;
		this.parentMdi = parentMdi;
		this.clctcompmodelproviderParent = clctcompmodelproviderParent;
		this.sParentEntityName = sParentEntityName;
		this.subform = subform;
		this.bSearchable = bSearchable;
		final String sEntityName = subform.getEntityName();
		this.prefs = prefsUserParent.node("subentity").node(sEntityName);
		this.subFormPrefs = entityPrefs.getSubFormPreferences(sEntityName);
		WorkspaceUtils.validatePreferences(subFormPrefs);
		this.clctfproviderfactory = clctfproviderfactory;

		assert this.getCollectableEntity() != null;
		assert this.getCollectableEntity().getName().equals(this.getSubForm().getEntityName());

		// Inititialize listeners for toolbar actions:
		subform.addSubFormToolListener(subformToolListener);

		this.setupListSelectionListener(subform);

		// set a provider for dynamic table cell editors and providers:
		subform.setTableCellEditorProvider(this);
		subform.setTableCellRendererProvider(this);

		// If this is a data subform indicate that the size is loading in the
		// corresponding tab.
		if (!bSearchable) {
			SizeKnownListener listener = subform.getSizeKnownListener();
			if (listener != null) {
				listener.actionPerformed(new SizeKnownEvent(subform, null));
			}
		}
		subform.addFocusActionListener(this);
		
		subform.addColumnModelListener(newSubFormTablePreferencesUpdateListener());
	}
	
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() {
		if (!closed) {
			LOG.info("close(): " + this);
			removeListSelectionListener(this.getJTable());
			subform.removeSubFormToolListener(subformToolListener);
			
			subform.close();
			subform = null;
			
			closed = true;
		}
	}
	
	public void setIgnorePreferencesUpdate(boolean ignore) {
		this.isIgnorePreferencesUpdate = ignore;
	}
	
	protected PreferencesUpdateListener newSubFormTablePreferencesUpdateListener() {
		return new PreferencesUpdateListener();
	}

	protected class PreferencesUpdateListener implements TableColumnModelListener  {		
		@Override
		public void columnSelectionChanged(ListSelectionEvent ev) {
			if (!isIgnorePreferencesUpdate) {
//				storeColumnOrderAndWidths(getJTable());
			}
		}
		
		@Override
		public void columnMoved(TableColumnModelEvent ev) {
			if (!isIgnorePreferencesUpdate) {
				storeColumnOrderAndWidths(getJTable());
			}
		}
		
		@Override
		public void columnMarginChanged(ChangeEvent ev) {
			if (!isIgnorePreferencesUpdate) {
				storeColumnOrderAndWidths(getJTable());
			}
		}
		
		@Override
		public void columnAdded(TableColumnModelEvent ev) {
		}
		
		@Override
		public void columnRemoved(TableColumnModelEvent ev) {
		}		
	}

	private final SubForm.SubFormToolListener subformToolListener = new SubForm.SubFormToolListener() {
		@Override
		public void toolbarAction(String actionCommand) {
			ToolbarFunction cmd = SubForm.ToolbarFunction.fromCommandString(actionCommand);
			if(cmd != null) {
				switch(cmd) {
				case NEW:
					cmdInsert(); break;
				case REMOVE:
					cmdRemove(); break;
				}
			}
		}
	};


	public final SubForm getSubForm() {
		return subform;
	}

	protected final JTable getJTable() {
		return subform.getJTable();
	}

	/**
	 * @return the table model that contains the data for the subform.
	 */
	protected abstract SubForm.SubFormTableModel getSubFormTableModel();

	/**
	 * @return the <code>CollectableEntity</code> for the data that is to be collected or searched for via this subform.
	 */
	public final CollectableEntity getCollectableEntity() {
		return clcte;
	}

	/**
	 * @return the entity name and foreign key field name used to uniquely 
	 * 		identify this subform in its <code>CollectController</code>.
	 */
	public final EntityAndFieldName getEntityAndForeignKeyFieldName() {
		return new EntityAndFieldName(this.getCollectableEntity().getName(), this.getForeignKeyFieldName());
	}

	/**
	 * @return Is this controller's subform searchable (to be used in a Search panel)?
	 */
	protected boolean isSearchable() {
		return this.bSearchable;
	}

	protected final Preferences getPrefs() {
		return this.prefs;
	}
	
	protected final SubFormPreferences getSubFormPrefs() {
		return this.subFormPrefs;
	}

	public final String getParentEntityName() {
		return this.sParentEntityName;
	}

	protected final JComponent getParentMdi() {
		return this.parentMdi;
	}

	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 */
	protected final void setColumnWidths() {
		LOG.debug("setColumnWidths");
		isIgnorePreferencesUpdate = true;
		getSubForm().setColumnWidths(this.getTableColumnWidthsFromPreferences());
		isIgnorePreferencesUpdate = false;
	}

	/**
	 * @return the table columns widths. If there are stored user preferences, the sizes will be restored.
	 * Size and order of list entries is determined by number and order of visible columns
	 */
	protected List<Integer> getTableColumnWidthsFromPreferences() {
		List<Integer> result = WorkspaceUtils.getColumnWidthsWithoutFixed(getSubFormPrefs());

		if (LOG.isDebugEnabled()) {
			LOG.debug("getTableColumnWidthsFromPreferences for entity " + this.getSubForm().getEntityName());
			for (Object o : result) {
				LOG.debug("getTableColumnWidthsFromPreferences: column width = " + o);
			}
		}

		return result;
	}

	/**
	 * stores the order of the columns in the table
	 */
	protected void storeColumnOrderAndWidths(JTable tbl) {
		if (!isSearchable()) {
			final List<String> lstFields = CollectableTableHelper.getFieldNamesFromColumns(tbl);
			final List<Integer> lstFieldWidths = CollectableTableHelper.getColumnWidths(tbl);
			WorkspaceUtils.setColumnPreferences(getSubFormPrefs(), lstFields, lstFieldWidths);
		}
	}

	/**
	 * @return the table columns. If there are stored user preferences, the column order will be restored.
	 * By default, the table column order is controlled by the record order of entities in table t_ad_masterdata_field
	 */
	protected List<CollectableEntityField> getTableColumns() {
		final List<CollectableEntityField> allFields = new ArrayList<CollectableEntityField>();

		final String sForeignKeyFieldName = this.getForeignKeyFieldName();
		for (String sFieldName : this.getCollectableEntity().getFieldNames()) {
			if (getSubForm().isColumnVisible(sFieldName) && !sFieldName.equals(sForeignKeyFieldName)) {
				allFields.add(this.getCollectableEntity().getEntityField(sFieldName));
			}
		}
		
		final List<String> definitionOrder = new ArrayList<String>(getSubForm().getColumnNames());
		final List<String> storedFieldNames = WorkspaceUtils.getSelectedWithoutFixedColumns(getSubFormPrefs());		
		
		final List<CollectableEntityField> availableFields = new ArrayList<CollectableEntityField>(allFields); 
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		
		if (storedFieldNames.isEmpty()) {
			// try to order by meta data
			try {
				List<EntityFieldMetaDataVO> efsOrdered = CollectionUtils.sorted(
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(getCollectableEntity().getName()).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								Integer order1 = (o1.getOrder()==null)?0:o1.getOrder();
								Integer order2 = (o2.getOrder()==null)?0:o2.getOrder();
								return order1.compareTo(order2);
							}
						}); 
				
				for (EntityFieldMetaDataVO efMeta : efsOrdered) {
					for (CollectableEntityField clctef : new ArrayList<CollectableEntityField>(availableFields)) {
						if (LangUtils.equals(efMeta.getField(), clctef.getName())) {
							result.add(clctef);
							availableFields.remove(clctef);
							break;
						}
					}
				}
			} catch (Exception ex) {
				LOG.warn("no column order from meta data", ex);
			}
			
		} else {
			// restore order from preferences
			for (String storedFieldName : storedFieldNames) {
				for (CollectableEntityField clctef : new ArrayList<CollectableEntityField>(availableFields)) {
					if (LangUtils.equals(storedFieldName, clctef.getName())) {
						result.add(clctef);
						availableFields.remove(clctef);
						break;
					}
				}
			}
		}
		
		Comparator<CollectableEntityField> comparator = new GivenFieldOrderComparator(definitionOrder);
		Collections.sort(availableFields, comparator);
		result.addAll(availableFields);

		return result;
	}
	
	/**
	 * makes sure the given list of selected fields is non-empty. If the list is empty, this method adds one field to it.
	 * This is to avoid a seemingly empty search result, which might be irritating to the user.
	 * @param clcte
	 * @param lstclctefSelected
	 * @precondition clcte != null
	 * @precondition lstclctefSelected != null
	 * @postcondition !lstclctefSelected.isEmpty()
	 *
	 * TODO: Make this protected again.
	 */
	public void makeSureSelectedFieldsAreNonEmpty(CollectableEntity clcte, List<CollectableEntityField> lstclctefSelected) {
		if (lstclctefSelected.isEmpty()) {
			
			List<String> fieldNames = new ArrayList<String>();
			try {
				List<EntityFieldMetaDataVO> fields = CollectionUtils.sorted(
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(clcte.getName()).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								Integer order1 = (o1.getOrder()==null)?0:o1.getOrder();
								Integer order2 = (o2.getOrder()==null)?0:o2.getOrder();
								return order1.compareTo(order2);
							}
						});
				
				for (EntityFieldMetaDataVO efMeta : fields) {
					if (StringUtils.isNullOrEmpty(efMeta.getCalcFunction())) {
						fieldNames.add(efMeta.getField());
					}
				}
			} catch (Exception ex) {
				LOG.warn("No entity fields for entity " + clcte.getName(), ex);
			}
			if (fieldNames.isEmpty()) {
				fieldNames.addAll(clcte.getFieldNames());
			}
			
			CollectableEntityField sysStateIcon = null;
			CollectableEntityField sysStateNumber = null;
			CollectableEntityField sysStateName = null;
			
			Set<String> clcteFieldNames = clcte.getFieldNames();
			for (String field : fieldNames) {
				if (!clcteFieldNames.contains(field)) {
					LOG.warn("Field " + field + " in collectable entity " + clcte.getName() + " does not exists");
					continue;
				}

				CollectableEntityField clctef = clcte.getEntityField(field);
				boolean select = true;
				if (NuclosEOField.getByField(field) != null) {
					select = false;
					switch (NuclosEOField.getByField(field)) {
					case STATEICON : 
						sysStateIcon = clctef;
						break;
					case STATENUMBER : 
						sysStateNumber = clctef;
						break;
					case STATE : 
						sysStateName = clctef;
						break;
					}
				}
				if (select) {
					lstclctefSelected.add(clctef);
				}
			}
			
			if (sysStateIcon != null)
				lstclctefSelected.add(sysStateIcon);
			if (sysStateNumber != null)
				lstclctefSelected.add(sysStateNumber);
			if (sysStateName != null)
				lstclctefSelected.add(sysStateName);
			
			final String sForeignKeyFieldName = this.getForeignKeyFieldName();
			for (CollectableEntityField clctef : new ArrayList<CollectableEntityField>(lstclctefSelected)) {
				final String sFieldName = clctef.getName();
				boolean remove = false;
				if (!getSubForm().isColumnVisible(sFieldName))
					remove = true;
				if (sFieldName.equals(sForeignKeyFieldName))
					remove = true;
				
				if (remove)
					lstclctefSelected.remove(clctef);
			}
			
			if (lstclctefSelected.isEmpty()) {
				lstclctefSelected.add(clcte.getEntityField(NuclosEOField.CHANGEDBY.getName()));
			}
		}
	}

	/**
	 * removes the columns whose names are not contained in <code>lstStoredFieldNames</code> from the given table's column model.
	 */
	protected final void removeColumnsFromTableColumnModel(JTable tbl, List<String> lstRemoveStoredFieldNames, boolean remove) {
		final TableColumnModel columnmodel = tbl.getColumnModel();

		final Set<TableColumn> stColumnsToRemove = new HashSet<TableColumn>();
		for (Enumeration<TableColumn> enumcolumn = columnmodel.getColumns(); enumcolumn.hasMoreElements();) {
			final TableColumn column = enumcolumn.nextElement();
			final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) tbl.getModel()).getCollectableEntityField(column.getModelIndex());

			if (remove && !lstRemoveStoredFieldNames.contains(clctef.getName())) {
				stColumnsToRemove.add(column);
			}
			if(!remove && lstRemoveStoredFieldNames.contains(clctef.getName())){
				stColumnsToRemove.add(column);
			}

		}

		for (TableColumn column : stColumnsToRemove) {
			columnmodel.removeColumn(column);
		}
	}

	/**
	 * @return the name of the foreign key field referencing the parent entity
	 * @postcondition result != null
	 * @todo try to eliminate this on (or at least, make it optional). Why does a Collectable collected in a subform have
	 * to have a foreign key field to the "parent entity"? In the subform (view), the relation to the parent is clear.
	 * The "parent id" is not needed before we actually store the model in the database.
	 * Considering the usages of "getForeignKeyFieldName()", it's mostly used to <i>exclude</i> this field from the list of all fields!
	 * Historically, this method was necessary because the master data mechanism demands a foreign key field.
	 */
	public final String getForeignKeyFieldName() {
		final CollectableEntity clcte = this.getCollectableEntity();
		final String sParentEntityName = this.getParentEntityName();

		return this.getSubForm().getForeignKeyFieldName(sParentEntityName, clcte);
	}

	public boolean isCellEditing() {
		return this.getJTable().isEditing();
	}

	public boolean isFixedCellEditing() {
		return this.getSubForm().getSubformRowHeader().getHeaderTable().isEditing();
	}

	public boolean stopEditing() {
		boolean result = true;

		if (this.isCellEditing()) {
			if (!this.getJTable().getCellEditor().stopCellEditing()) {
				result = false;
			}
		}
		else if (this.isFixedCellEditing()) {
			if (!this.getSubForm().getSubformRowHeader().getHeaderTable().getCellEditor().stopCellEditing()) {
				result = false;
			}
		}

		return result;
	}

	public void cancelEditing() {
		if (this.getJTable().isEditing()) {
			this.getJTable().getCellEditor().cancelCellEditing();
		}
	}

	protected final void setupTableCellRenderers(SubForm.SubFormTableModel subformtblmdl) {
		this.getSubForm().setupTableCellRenderers(this.getCollectableEntity(), subformtblmdl, this.getCollectableFieldsProviderFactory(), this.isSearchable());
	}

	protected final void setupRowHeight(SubForm.SubFormTableModel subformtablemodel) {
		this.getSubForm().setRowHeight(this.getPreferredRowHeight(subformtablemodel));
	}

	private int getPreferredRowHeight(SubForm.SubFormTableModel subformtablemodel) {
		final JTable tbl = this.getJTable();
		int result = SubForm.MIN_ROWHEIGHT;
		for (Enumeration<TableColumn> enumeration = tbl.getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final CollectableEntityField clctef = subformtablemodel.getCollectableEntityField(column.getModelIndex());
			result = Math.max(result, this.getSubForm().getTableCellRenderer(clctef).getTableCellRendererComponent(tbl, subformtablemodel.getNullValue(clctef),
					true, true, 0, 0).getPreferredSize().height);
		}
		return result;
	}

	/**
	 * Command: insert a new row.
	 */
	public void cmdInsert() {
		// TODO: check if this really must be encapsuled with runCommand
		try {
			if (stopEditing()) {
				JTable tbl = getJTable();
				insertNewRow();
				// TODO: und wenn die neue Zeile wg. Sortierung NICHT am Ende liegt???
				final int lastRow = tbl.getRowCount() - 1;
				// Hotfix for 'Strukturdefinition' -> Subform Attribute -> add row (to empty subform) (tp)
				if (lastRow >= 0) {
					tbl.addRowSelectionInterval(lastRow, lastRow);
				}
			}
		} catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(getParent(), e);
		}
	}

	protected abstract Collectable insertNewRow() throws CommonBusinessException;

	/**
	 * Command: removes the selected row
	 */
	public void cmdRemove() {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
            public void run() {
				if (stopEditing()) {
					removeSelectedRows();
				}
			}
		});
	}

	protected void removeSelectedRows() {
		final JTable tbl = this.getJTable();
		int[] viewIndices = tbl.getSelectedRows();
		int[] modelIndices = new int[viewIndices.length];
		for (int i = 0; i < viewIndices.length; i++) {
			modelIndices[i] = tbl.convertRowIndexToModel(viewIndices[i]);
		}

		this.getSubFormTableModel().remove(modelIndices);

		// Select the nearest row if any (that may then be deleted next etc.)
		if (tbl.getRowCount() > 0 && viewIndices.length > 0) {
			if (tbl.getRowCount() > viewIndices[0]) {
				tbl.setRowSelectionInterval(viewIndices[0], viewIndices[0]);
			}
			else {
				tbl.setRowSelectionInterval(tbl.getRowCount() - 1, tbl.getRowCount() - 1);
			}
		}
	}

	protected final boolean isEnabled() {
		return this.getSubForm().isEnabled();
	}

	public class FocusListSelectionListener implements ListSelectionListener {

		private final JTable tbl;
		private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

		public FocusListSelectionListener(JTable tbl) {
			this.tbl = tbl;
		}

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
	        if(currentEvent instanceof KeyEvent){
	        	KeyEvent ke = (KeyEvent)currentEvent;
	            if(!KeyStroke.getKeyStrokeForEvent(ke).equals(tabKeyStroke))
	            	return;
	            int rowIndex = tbl.getSelectedRow();
	            int columnIndex = tbl.getSelectedColumn();
	            if(rowIndex == 0 && columnIndex == 0 && e.getLastIndex() > 0) {
					SubFormController.this.cmdInsert();
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							tbl.editCellAt(e.getLastIndex()+1, 0);
							tbl.getEditorComponent().requestFocusInWindow();
						}
					});

	            }
	            // focus change with keyboard
	        }
		}

	}

	protected class IsRemovableListSelectionListener implements ListSelectionListener {
		private final JTable tbl;

		public IsRemovableListSelectionListener(JTable tbl) {
			this.tbl = tbl;
		}

		@Override
        public void valueChanged(ListSelectionEvent ev) {
			final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
			boolean bEnabled = !lsm.isSelectionEmpty() && SubFormController.this.isEnabled();
			for (int i : tbl.getSelectedRows()) {
				if (!isRowRemovable(getSubForm().getSubformTable().convertRowIndexToModel(i))) {
					bEnabled = false;
				}
			}
			subform.setToolbarFunctionState(SubForm.ToolbarFunction.REMOVE.name(), bEnabled ? SubForm.ToolbarFunctionState.ACTIVE : SubForm.ToolbarFunctionState.DISABLED);

			// scroll table if necessary:
			if (tbl.getAutoscrolls()) {
				final int iRowIndex = lsm.getAnchorSelectionIndex();
				final int iColumnIndex = tbl.getSelectedColumn();

				final Rectangle cellRect = tbl.getCellRect(iRowIndex, iColumnIndex != -1 ? iColumnIndex : 0, true);
				if (cellRect != null) {
					tbl.scrollRectToVisible(cellRect);
				}
			}
		}
	}

	private void setupListSelectionListener(final SubForm subform) {
		final JTable tbl = subform.getJTable();

		// initialize listener for row selection in the table:
		this.listselectionlistener = new IsRemovableListSelectionListener(tbl);

		tbl.getSelectionModel().addListSelectionListener(listselectionlistener);

		tbl.getSelectionModel().addListSelectionListener(new FocusListSelectionListener(tbl));
	}

	private void removeListSelectionListener(final JTable tbl) {
		tbl.getSelectionModel().removeListSelectionListener(this.listselectionlistener);
		this.listselectionlistener = null;
	}

	protected final void setupTableModelListener() {
		this.getSubForm().setupTableModelListener();
	}

	protected void removeTableModelListener() {
		if (getSubForm() != null) {
			getSubForm().removeTableModelListener();
		}
	}

	protected final void setupColumnModelListener() {
		this.getSubForm().setupColumnModelListener();
	}

	protected void removeColumnModelListener() {
		if (getSubForm() != null) {
			getSubForm().removeColumnModelListener();
		}
	}

	protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactory() {
		return this.clctfproviderfactory;
	}

	/**
	 * Implementation of <code>TableCellRendererProvider</code>.
	 * Subclasses may do special rendering for specific fields here.
	 * @param clctefTarget
	 * @return this.getSubForm().getTableCellRenderer(clctefTarget);
	 */
	@Override
    public TableCellRenderer getTableCellRenderer(CollectableEntityField clctefTarget) {
		return this.getSubForm().getTableCellRenderer(clctefTarget);
	}

	/**
	 * Implementation of <code>TableCellEditorProvider</code>.
	 * @param tbl
	 * @param iRow row of the table (not the table model)
	 * @param clctefTarget collectable entity field (for the column)
	 * @return a <code>TableCellEditor</code> for columns that need a dynamic <code>TableCellEditor</code>.
	 * <code>null</code> for all other columns.
	 */
	@Override
    public TableCellEditor getTableCellEditor(JTable tbl, int iRow, CollectableEntityField clctefTarget) {
		subform.endEditing();

		return this.getSubForm().getTableCellEditor(tbl, iRow, this.getCollectableEntity(), clctefTarget,
				this.getSubFormTableModel(), this.isSearchable(), this.getPrefs(), this.getCollectableFieldsProviderFactory(), this);
	}

	@Override
    public final CollectableField getParameterForRefreshValueList(final SubFormTableModel subformtblmdl, int iRow, final String sParentComponentName, final String sParentComponentEntityName) {
		assert sParentComponentEntityName != null;

		final CollectableField result;
		if (sParentComponentEntityName.equals(this.getCollectableEntity().getName())) {
			final int iColumnParent = subformtblmdl.findColumnByFieldName(sParentComponentName);
			final Object oValue = subformtblmdl.getValueAt(iRow, iColumnParent);

			// the parent component resides in the same subform as the target:
			if (SubFormController.this.isSearchable()) {
				final CollectableField clctf = getComparand((CollectableSearchCondition) oValue);
				result = (clctf == null) ? CollectableValueIdField.NULL : clctf;
			}
			else {
				result = (CollectableField) oValue;
			}
		}
		else if (sParentComponentEntityName.equals(this.getParentEntityName())) {
			// the parent component resides in the form outside of the subform:
			final CollectableComponentModel clctcompmodel = clctcompmodelproviderParent.getCollectableComponentModelFor(sParentComponentName);
			String sParentSubForm = subform.getParentSubForm();
			if(sParentSubForm != null)
				result = getFieldFromParentSubform(sParentComponentName);
			else
				result = clctcompmodel.getField();
		}
		else if (Modules.getInstance().isModuleEntity(sParentComponentEntityName)) {
			final CollectableComponentModel clctcompmodel = clctcompmodelproviderParent.getCollectableComponentModelFor(sParentComponentName);
			result = clctcompmodel.getField();
		}
		else {
			throw new CommonFatalException(getSpringLocaleDelegate().getMessage("SubFormController.1",
				"Die Entit\u00e4t der Vaterkomponente ({0}) muss der Entit\u00e4t des Unterformulars ({1}) oder der Entit\u00e4t des \u00fcbergeordneten Formulars ({2}) entsprechen.",
				sParentComponentEntityName, this.getCollectableEntity().getName(), this.getParentEntityName()));
		}

		if(result == null && isSearchable()) {
			throw new NuclosFieldNotInModelException();
		}
		else if(result == null) {
			throw new CommonFatalException(getSpringLocaleDelegate().getMessage(
					"SubFormController.2", "Das Feld ({0}) ist nicht in der Entität ({1}) vorhanden!", sParentComponentEntityName, this.getParentEntityName()));
		}

		return result;
	}

	protected abstract CollectableField getFieldFromParentSubform(String sFieldName);

	/**
	 * @param cond May be <code>null</code>.
	 * @return the <code>CollectableField</code> contained in <code>cond</code>, if <code>cond</code> is a <code>CollectableComparison</code>.
	 * <code>null</code> otherwise.
	 */
	protected static CollectableField getComparand(CollectableSearchCondition cond) {
		final CollectableField result;
		if (cond != null && cond instanceof CollectableComparison) {
			result = ((CollectableComparison) cond).getComparand();
		}
		else {
			result = null;
		}
		return result;
	}

	/**
	 * @param sColumnName
	 * @return
	 * @postcondition !this.isEnabled() --> !result
	 */
	public abstract boolean isColumnEnabled(String sColumnName);

	public abstract boolean isRowEditable(int row);

	public abstract boolean isRowRemovable(int row);

	public void setCollectableComponentFactory(CollectableComponentFactory collectableComponentFactory) {
		this.getSubForm().setCollectableComponentFactory(collectableComponentFactory);
		setupStaticTableCellEditors(this.getSubForm().getJTable());
	}

	protected final void setupStaticTableCellEditors(JTable tbl) {
		this.getSubForm().setupStaticTableCellEditors(tbl, this.isSearchable(), this.getPrefs(), this.getSubFormTableModel(),
				this.getCollectableFieldsProviderFactory(), this.getParentEntityName(), this.getCollectableEntity());
	}

	@Override
	public void focusAction(EventObject eObject) {
		cmdInsert();
	}




}	// class SubFormController

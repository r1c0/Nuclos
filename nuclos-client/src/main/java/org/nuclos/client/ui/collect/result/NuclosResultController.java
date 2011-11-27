//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.result;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.UIUtils.CommandHandler;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.FixedColumnRowHeader;
import org.nuclos.client.ui.collect.SelectFixedColumnsController;
import org.nuclos.client.ui.collect.SelectFixedColumnsPanel;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.collect.model.CollectableEntityFieldBasedTableModel;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.popupmenu.AbstractJPopupMenuListener;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

/**
 * A specialization of ResultController for use with an {@link NuclosCollectController}.
 * <p>
 * This implementation adds support for 'fixed' columns in the shown result.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class NuclosResultController<Clct extends Collectable> extends ResultController<Clct> {
	
	private static final Logger LOG = Logger.getLogger(NuclosResultController.class);
	
	public NuclosResultController(CollectableEntity clcte, ISearchResultStrategy<Clct> srs) {
		super(clcte, srs);
	}

	/**
	 * @deprecated You should really provide a CollectableEntity here.
	 */
	public NuclosResultController(String entityName, ISearchResultStrategy<Clct> srs) {
		super(entityName, srs);
	}
	
	/**
	 * Factory method for instantiating a {@link SelectFixedColumnsController} suited 
	 * for the NuclosResultController. 
	 * <p>
	 * Maybe overridden by subclasses.
	 * </p>
	 */
	public SelectFixedColumnsController newSelectColumnsController(Component parent) {
		return new SelectFixedColumnsController(parent, new SelectFixedColumnsPanel());
	}
	
	/**
	 * Initializes the <code>fields</code> field as follows: 
	 * <ol>
	 *   <li>Calling {@link ResultController#initializeFields(CollectableEntity, CollectController, Preferences)}</li>
	 *   <li>(re-)set the fixed column set by reading the preferences. The set will only contain fixed columns that
	 *     have been <em>selected</em>.</li>
	 * </ul>
	 * 
	 * TODO: Make protected again.
	 */
	@Override
	public void initializeFields(CollectableEntity clcte, CollectController<Clct> clctctl) {
		assert clctctl == getCollectController() && clctctl.getFields() == getFields() && clctctl.getResultController() == this;
		assert getEntity().equals(clcte);
		super.initializeFields(clcte, clctctl);

		List<String> lstSelectedFieldNames = WorkspaceUtils.getFixedColumns(clctctl.getEntityPreferences());

		ChoiceEntityFieldList fields = clctctl.getFields();
		for (CollectableEntityField clctef : fields.getSelectedFields()) {
			if (lstSelectedFieldNames.contains(clctef.getName())) {
				// this.stFixedColumns.add(clctef);
				getNuclosResultPanel().getFixedColumns().add(clctef);
			}
		}
	}
	
	/**
	 * initializes the <code>fields</code> field.
	 * @param clcte
	 * @param preferences
	 */
	public void initializeFields(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl, 
			final List<CollectableEntityField> lstSelectedNew, final Set<CollectableEntityField> lstFixedNew, 
			final Map<String,Integer> lstColumnWiths) 
	{
		assert clctctl == getCollectController() && clctctl.getFields() == getFields() && clctctl.getResultController() == this;
		final NuclosCollectController<Clct> elisaController = (NuclosCollectController<Clct>) clctctl;
		final List<CollectableEntityField> lstSelected = new ArrayList<CollectableEntityField>(fields.getSelectedFields());
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable tblResult = panel.getResultTable();
		
		UIUtils.runCommand(clctctl.getFrame(),
			new CommandHandler() {
				@Override
				public void commandStarted(Component parent) {
					panel.setVisibleTable(false);
				}

				@Override
				public void commandFinished(Component parent) {
					//don't set setVisibleTable(true) here
					//see finishSearchObserver in this.refreshResult(... and this.run(...
				}
			},
			new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final int iSelRow = tblResult.getSelectedRow();
				NuclosResultController.super.initializeFields(fields, clctctl, lstSelectedNew);

				panel.setFixedColumns(new HashSet<CollectableEntityField>(lstFixedNew));

				final List<CollectableEntityField> lstSelectedNewAfterMove = (List<CollectableEntityField>) fields.getSelectedFields();
				((SortableCollectableTableModel<Clct>) tblResult.getModel()).setColumns(lstSelectedNewAfterMove);
				TableColumnModel variableColumnModel = tblResult.getColumnModel();
				TableColumnModel fixedColumnModel = panel.getFixedResultTable().getColumnModel();

				adjustColumnModels(lstSelectedNewAfterMove, variableColumnModel, fixedColumnModel);

				final Collection<CollectableEntityField> collNewlySelected = CollectionUtils.subtract(lstSelectedNewAfterMove, lstSelected);
				if (!collNewlySelected.isEmpty() && !elisaController.getSearchStrategy().getCollectablesInResultAreAlwaysComplete()) {
					// refresh the result:
					refreshResult(clctctl);
				} else {
					panel.setVisibleTable(true);
				}

				// reselect the previously selected row (which gets lost by refreshing the model)
				if (iSelRow != -1) {
					tblResult.setRowSelectionInterval(iSelRow, iSelRow);
				}

				// restore the widths of the still present columns
				isIgnorePreferencesUpdate = true;
				panel.restoreColumnWidths(lstSelectedNew, lstColumnWiths);
				isIgnorePreferencesUpdate = false;

				// panel.invalidateFixedTable();
				panel.setupTableCellRenderers(tblResult);
			}

			private void refreshResult(final CollectController<Clct> clctctl) throws CommonBusinessException {
				//((ElisaCollectController) clctctl).refreshResult();
				Observer finishSearchObserver = new Observer() {
					@Override
					public void update(Observable beobachtbarer, Object arg) {
						panel.setVisibleTable(true);
					}
				};
				List<Observer> lstObserver = new ArrayList<Observer>();
				lstObserver.add(finishSearchObserver);
				getSearchResultStrategy().cmdRefreshResult(lstObserver);
			}
		});
	}
	
	private void adjustColumnModels(final List<? extends CollectableEntityField> lstclctefSelected,
			TableColumnModel columnmodelVariable, TableColumnModel columnmodelFixed) 
	{

		for (CollectableEntityField clctefSelected : lstclctefSelected) {
			if (getNuclosResultPanel().getFixedColumns().contains(clctefSelected)) {
				try {
					final int iSelectedIndex = columnmodelVariable.getColumnIndex(clctefSelected.getLabel());
					columnmodelVariable.removeColumn(columnmodelVariable.getColumn(iSelectedIndex));
				}
				catch (IllegalArgumentException ex) {
					// ignore
				}
			}
			else {
				try {
					final int iSelectedIndex = columnmodelFixed.getColumnIndex(clctefSelected.getLabel());
					columnmodelFixed.removeColumn(columnmodelFixed.getColumn(iSelectedIndex));
				}
				catch (IllegalArgumentException ex) {
					// ignore
				}
			}
		}
	}

	protected final void setSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl, 
			final SortedSet<CollectableEntityField> lstAvailableObjects, final List<CollectableEntityField> lstSelectedObjects, 
			final Set<CollectableEntityField> stFixedObjects, final boolean restoreWidthsFromPreferences, final boolean restoreOrder) 
	{
		assert clctctl == getCollectController() && clctctl.getFields() == getFields() && clctctl.getResultController() == this;
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable tblResult = panel.getResultTable();
		// remember the widths of the currently visible columns
		final Map<String, Integer> mpWidths = panel.getVisibleColumnWidth(fields.getSelectedFields());

		UIUtils.runCommand(clctctl.getFrame(),
				new CommandHandler() {
					@Override
					public void commandStarted(Component parent) {
						panel.setVisibleTable(false);
					}

					@Override
					public void commandFinished(Component parent) {
						//don't set setVisibleTable(true) here
						//see finishSearchObserver in this.refreshResult(... and this.run(...
					}
				},
				new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final int iSelRow = tblResult.getSelectedRow();
					final List<CollectableEntityField> lstSelectedOld = (List<CollectableEntityField>) fields.getSelectedFields();
					fields.set(lstAvailableObjects, lstSelectedObjects, clctctl.getResultController().getCollectableEntityFieldComparator());

					final List<CollectableEntityField> lstSelectedNew = (List<CollectableEntityField>) fields.getSelectedFields();
					panel.setFixedColumns(stFixedObjects);

					((SortableCollectableTableModel<Clct>) tblResult.getModel()).setColumns(lstSelectedNew);
					TableColumnModel variableColumnModel = tblResult.getColumnModel();
					TableColumnModel fixedColumnModel = panel.getFixedResultTable().getColumnModel();

					adjustColumnModels(lstSelectedNew, variableColumnModel, fixedColumnModel);

					final Collection<? extends CollectableEntityField> collNewlySelected = CollectionUtils.subtract(lstSelectedNew, lstSelectedOld);
					if (!collNewlySelected.isEmpty() && !clctctl.getSearchStrategy().getCollectablesInResultAreAlwaysComplete()) {
						// refresh the result:
						refreshResult(clctctl);
					} else {
						panel.setVisibleTable(true);
					}
					
					// add DEselected to hidden in preferences
					final Collection<? extends CollectableEntityField> collDeselected = CollectionUtils.subtract(lstSelectedOld, lstSelectedNew);
					for (CollectableEntityField clctef : collDeselected) {
						WorkspaceUtils.addHiddenColumn(getCollectController().getEntityPreferences(), clctef.getName());
					}

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelRow != -1) {
						tblResult.setRowSelectionInterval(iSelRow, iSelRow);
					}

					isIgnorePreferencesUpdate = true;
					if (restoreWidthsFromPreferences) {
						// restore widths from preferences
						panel.setColumnWidths(getNuclosResultPanel().getResultTable(), getCollectController().getEntityPreferences());
					} else {
						// restore the widths of the still present columns
						panel.restoreColumnWidths(lstSelectedNew, mpWidths);
					}
					isIgnorePreferencesUpdate = false;

					panel.invalidateFixedTable();

					panel.setupTableCellRenderers(tblResult);
					
					if (restoreOrder) {
						getCollectController().restoreColumnOrderFromPreferences(false);
						getCollectController().getSearchStrategy().search(true);
					}
					
					// write preferences after column width are restored
					writeSelectedFieldsAndWidthsToPreferences(mpWidths);
				}

				private void refreshResult(final CollectController<Clct> clctctl) throws CommonBusinessException {
					Observer finishSearchObserver = new Observer() {
						@Override
						public void update(Observable beobachtbarer, Object arg) {
							panel.setVisibleTable(true);
						}
					};
					List<Observer> lstObserver = new ArrayList<Observer>();
					lstObserver.add(finishSearchObserver);
					getSearchResultStrategy().cmdRefreshResult(lstObserver);
				}

			});
	}

	/**
	 * TODO: Make this protected again.
	 */
	@Override
	public void setModel(CollectableTableModel<Clct> tblmodel, final CollectableEntity clcte, final CollectController<Clct> clctctl) {
		assert getEntity().equals(clcte);
		super.setModel(tblmodel, clcte, clctctl);
		
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable fixedTable = panel.getFixedResultTable();
		fixedTable.setModel(tblmodel);
		
		ToolTipsTableHeader tblHeader = new ToolTipsTableHeader(tblmodel, fixedTable.getColumnModel());

		tblHeader.setName("tblHeader");
		fixedTable.setTableHeader(tblHeader);
		
		TableUtils.addMouseListenerForSortingToTableHeader(fixedTable, (SortableTableModel) tblmodel, new CommonRunnable() {
	         @Override
	       	public void run() {
	             getSearchResultStrategy().cmdRefreshResult();
	          }
	       });

		removeColumnsFromFixedTable();
		removeColumnsFromResultTable();

		setFixedTable();
//		TableUtils.setOptimalColumnWidths(fixedTable);
		
		panel.getResultTable().getTableHeader().addMouseListener(new TableHeaderColumnPopupListener() {
			@Override
			protected void removeColumnVisibility(TableColumn column) {
				final Map<String, Integer> mpWidths = panel.getVisibleColumnWidth(clctctl.getFields().getSelectedFields());
				final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) panel.getResultTable().getModel()).getCollectableEntityField(column.getModelIndex());
				cmdRemoveColumn(clctctl.getFields(), clctef, clctctl);
				isIgnorePreferencesUpdate = true;
				panel.restoreColumnWidths(clctctl.getFields().getSelectedFields(), mpWidths);
				isIgnorePreferencesUpdate = false;
			}
		});
	}

	/**
	 * removes the columns from the fixed table, which are not in the stFixedColumns
	 */
	private void removeColumnsFromFixedTable() {
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable fixedTable = panel.getFixedResultTable();
		
		TableColumnModel fixedColumnModel = fixedTable.getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		for (Enumeration<TableColumn> columnEnum = fixedColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			TableColumn varColumn = columnEnum.nextElement();
			boolean doRemove = true;
			for (CollectableEntityField clctefFixed : panel.getFixedColumns()) {
				if (clctefFixed.getLabel().equals(varColumn.getIdentifier())) {
					doRemove = false;
				}
			}
			if (doRemove) {
				columnsToRemove.add(varColumn);
			}
		}

		for (TableColumn columnToRemove : columnsToRemove) {
			fixedColumnModel.removeColumn(columnToRemove);
		}
	}

	/**
	 * removes the columns from the result table, which are  in the stFixedColumns
	 */
	private void removeColumnsFromResultTable() {
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable resultTable = panel.getResultTable();
		
		TableColumnModel resultColumnModel = resultTable.getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		for (Enumeration<TableColumn> columnEnum = resultColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			final TableColumn varColumn = columnEnum.nextElement();
			boolean doRemove = false;
			for (CollectableEntityField clctefFixed : panel.getFixedColumns()) {
				if (clctefFixed.getLabel().equals(varColumn.getIdentifier())) {
					doRemove = true;
				}
			}
			if (doRemove) {
				columnsToRemove.add(varColumn);
			}
		}

		for (TableColumn columnToRemove : columnsToRemove) {
			resultColumnModel.removeColumn(columnToRemove);
		}
	}

	/**
	 */
	private void setFixedTable() {
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable fixedTable = panel.getFixedResultTable();
		
		fixedTable.setRowHeight(panel.getResultTable().getRowHeight());
		panel.getResultTableScrollPane().setRowHeaderView(fixedTable);
		panel.getResultTableScrollPane().getRowHeader().setBackground(fixedTable.getBackground());
		panel.getResultTableScrollPane().getRowHeader().setPreferredSize(fixedTable.getPreferredSize());
		panel.getResultTableScrollPane().setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable.getTableHeader());

		final TableCellRenderer originalRenderer = fixedTable.getTableHeader().getDefaultRenderer();
		TableCellRenderer headerRenderer = new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component renderComp = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (renderComp != null && renderComp.getBackground() != null) {
					renderComp.setBackground(FixedColumnRowHeader.FIXED_HEADER_BACKGROUND);
				}
				return renderComp;
			}

			;
		};
				
		fixedTable.getTableHeader().setDefaultRenderer(headerRenderer);

		panel.invalidateFixedTable();
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 */
	@Override
	public final void cmdSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl) {
		assert clctctl == getCollectController() && clctctl.getFields() == getFields() && clctctl.getResultController() == this;
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final NuclosCollectController<Clct> nucleusctl = (NuclosCollectController<Clct>) clctctl;
		
		// final SelectFixedColumnsController ctl = new PivotController(clctctl.getFrame(), new PivotPanel(subFormFields), 
		// 		(GenericObjectResultController) nucleusctl.getResultController());
		final SelectFixedColumnsController ctl = newSelectColumnsController(clctctl.getFrame());
		final SortedSet<CollectableEntityField> lstAvailable = fields.getAvailableFields();
		final List<CollectableEntityField> lstSelected = fields.getSelectedFields();
		final ChoiceEntityFieldList ro = new ChoiceEntityFieldList(panel.getFixedColumns());
		ro.set(lstAvailable, lstSelected, nucleusctl.getResultController().getCollectableEntityFieldComparator());

		// TODO: What the hell is the side effect of this? (Thomas Pasch)
		panel.getVisibleColumnWidth(lstSelected);
		ctl.setModel(ro);
		final boolean bOK = ctl.run(  
				CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));

		if (bOK) {
			setSelectColumns(fields, clctctl, ctl.getAvailableObjects(), ctl.getSelectedObjects(), ctl.getFixedObjects(), false, false);
		}
	}

	@Override
	protected final void cmdAddColumn(ChoiceEntityFieldList fields, TableColumn columnBefore, String sFieldNameToAdd) throws CommonBusinessException {
		super.cmdAddColumn(fields, columnBefore, sFieldNameToAdd);
		
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable table = panel.getResultTable();
		final JTable fixedTable = panel.getFixedResultTable();
		
		final TableColumnModel columnmodelVariable = table.getColumnModel();
		final TableColumnModel columnmodelFixed = fixedTable.getColumnModel();
		adjustColumnModels(fields.getSelectedFields(), columnmodelVariable, columnmodelFixed);
	}

	@Override
	protected final void cmdRemoveColumn(ChoiceEntityFieldList fields, CollectableEntityField clctef, CollectController<Clct> ctl) {
		super.cmdRemoveColumn(fields, clctef, ctl);

		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		panel.getFixedColumns().remove(clctef);
		// TODO: Is the copy really needed? (Thomas Pasch)
		SortedSet<CollectableEntityField> lstAvailableFields = new TreeSet<CollectableEntityField>(fields.getComparatorForAvaible());
		lstAvailableFields.addAll(fields.getAvailableFields());
		// TODO: Is the copy really needed? (Thomas Pasch)
		List<CollectableEntityField> lstSelectedFields = new ArrayList<CollectableEntityField>(fields.getSelectedFields());
		setSelectColumns(fields, ctl, lstAvailableFields, lstSelectedFields, panel.getFixedColumns(), false, false);
	}
	
	@Override
	protected void writeSelectedFieldsAndWidthsToPreferences(
			EntityPreferences entityPreferences,
			List<? extends CollectableEntityField> lstclctefSelected, Map<String, Integer> mpWidths) {
		super.writeSelectedFieldsAndWidthsToPreferences(entityPreferences,
				lstclctefSelected, mpWidths);
		WorkspaceUtils.updateFixedColumns(entityPreferences, 
				CollectableUtils.getFieldNamesFromCollectableEntityFields(getNuclosResultPanel().getFixedColumns()));
	}
	
	@Override
	protected List<Integer> getFieldWidthsForPreferences() {
		final List<Integer> lstFieldWidths = CollectableTableHelper.getColumnWidths(getNuclosResultPanel().getFixedResultTable());
		lstFieldWidths.addAll(CollectableTableHelper.getColumnWidths(getNuclosResultPanel().getResultTable()));
		return lstFieldWidths;
	}
	
	protected NuclosResultPanel<Clct> getNuclosResultPanel() {
		return (NuclosResultPanel<Clct>) getCollectController().getResultPanel();
	}
	
	@Override
	public void setupResultPanel() {
		super.setupResultPanel();
		getNuclosResultPanel().addFixedColumnModelListener(newResultTablePreferencesUpdateListener());
	}

	/**
	 * Popup menu for the columns in the Result table.
	 */
	protected abstract class TableHeaderColumnPopupListener extends AbstractJPopupMenuListener {

		private Point ptLastOpened;
		protected JPopupMenu popupmenuColumn = new JPopupMenu();
		protected final JTableHeader usedHeader;

		public TableHeaderColumnPopupListener() {
			super();
			this.usedHeader = getNuclosResultPanel().getResultTable().getTableHeader();
			if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS) ||
					!MainFrame.getWorkspace().isAssigned()) {
				this.popupmenuColumn.add(createHideColumnItem());
				this.popupmenuColumn.addSeparator();
			}
			
			if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) &&
					MainFrame.getWorkspace().isAssigned()) {
				this.popupmenuColumn.add(createPublishColumnsItem());
			}
			this.popupmenuColumn.add(createRestoreColumnsItem());
		}

		private JMenuItem createHideColumnItem() {
			final JMenuItem miPopupHideThisColumn = new JMenuItem(CommonLocaleDelegate.getMessage("NuclosResultController.1","Diese Spalte ausblenden"));
			miPopupHideThisColumn.setIcon(Icons.getInstance().getIconRemoveColumn16());
			miPopupHideThisColumn.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent ev) {
					final int iColumnIndex = usedHeader.columnAtPoint(getLatestOpenPoint());
					removeColumnVisibility(usedHeader.getColumnModel().getColumn(iColumnIndex));
				}
			});

			return miPopupHideThisColumn;
		}
		
		private JMenuItem createRestoreColumnsItem() {
			final JMenuItem miPopupRestoreColumns = new JMenuItem(CommonLocaleDelegate.getMessage("NuclosResultController.2", "Alle Spalten auf Vorlage zurücksetzen"));
			miPopupRestoreColumns.setIcon(Icons.getInstance().getIconUndo16());
			miPopupRestoreColumns.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						WorkspaceUtils.restoreEntityPreferences(getCollectController().getEntityPreferences());
						
						final List<CollectableEntityField> allFields = CollectionUtils.concat(
								getFields().getAvailableFields(), 
								getFields().getSelectedFields());
						
						// add missing pivot fields. (why they are not avaiable?)
						WorkspaceUtils.addMissingPivotFields(getCollectController().getEntityPreferences(), allFields);
						
						final List<CollectableEntityField> selected = WorkspaceUtils.getSelectedFields(getCollectController().getEntityPreferences(), allFields);
						getCollectController().makeSureSelectedFieldsAreNonEmpty(getEntity(), selected);
						
						final SortedSet<CollectableEntityField> avaiable = new TreeSet<CollectableEntityField>(new CollectableEntityField.LabelComparator());
						avaiable.addAll(CollectionUtils.subtract(allFields, selected));

						setSelectColumns(
								getFields(), 
								getCollectController(), 
								avaiable, 
								selected, 
								WorkspaceUtils.getFixedFields(getCollectController().getEntityPreferences(), selected),
								true,
								true);
				
					} catch (CommonBusinessException e1) {
						Errors.getInstance().showExceptionDialog(getNuclosResultPanel(), e1);
					}
					
				}
			});
			
			return miPopupRestoreColumns;
		}
		
		private JMenuItem createPublishColumnsItem() {
			final JMenuItem miPublishColumns = new JMenuItem(new AbstractAction(CommonLocaleDelegate.getMessage("NuclosResultController.3", "Spalten in Vorlage publizieren"), Icons.getInstance().getIconRedo16()) {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).publishEntityPreferences(MainFrame.getWorkspace(), getCollectController().getEntityPreferences());
					} catch (CommonBusinessException e1) {
						Errors.getInstance().showExceptionDialog(getNuclosResultPanel(), e1);
					}
				}
			});
			return miPublishColumns;
		}
		
		@Override
		protected final JPopupMenu getJPopupMenu(MouseEvent ev) {
			this.ptLastOpened = ev.getPoint();
			return popupmenuColumn;
		}

		/**
		 * the point where the popup menu was opened latest
		 */
		public Point getLatestOpenPoint() {
			return this.ptLastOpened;
		}

		protected abstract void removeColumnVisibility(TableColumn column);

	}  // inner class PopupMenuColumnListener

}

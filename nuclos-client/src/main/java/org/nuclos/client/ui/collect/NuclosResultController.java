package org.nuclos.client.ui.collect;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.UIUtils.CommandHandler;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * A specialization of ResultController for use with an {@link NuclosCollectController}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class NuclosResultController<Clct extends Collectable> extends ResultController<Clct> {
	
	public NuclosResultController(CollectableEntity clcte) {
		super(clcte);
	}

	/**
	 * @deprecated You should really provide a CollectableEntity here.
	 */
	public NuclosResultController(String entityName) {
		super(entityName);
	}
	
	/**
	 * Initializes the <code>fields</code> field as follows: 
	 * <ol>
	 *   <li>Calling {@link ResultController#initializeFields(CollectableEntity, CollectController, Preferences)}</li>
	 *   <li>(re-)set the fixed column set by reading the preferences. The set will only contain fixed columns that
	 *     have been <em>selected</em>.</li>
	 * </ul>
	 */
	@Override
	protected void initializeFields(CollectableEntity clcte, CollectController<Clct> clctctl, Preferences preferences) {
		assert clctctl == getCollectController() && clctctl.getFields() == getFields();
		assert getEntity().equals(clcte);
		super.initializeFields(clcte, clctctl, preferences);

		List<String> lstSelectedFieldNames;
		try {
			lstSelectedFieldNames = PreferencesUtils.getStringList(preferences, NuclosResultPanel.PREFS_NODE_FIXEDFIELDS);
		}
		catch (PreferencesException ex) {
			lstSelectedFieldNames = new ArrayList<String>();
		}

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
		assert clctctl == getCollectController() && clctctl.getFields() == getFields();
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
			@SuppressWarnings("unchecked")
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
				if (!collNewlySelected.isEmpty() && !elisaController.getCollectablesInResultAreAlwaysComplete()) {
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
				panel.restoreColumnWidths(lstSelectedNew, lstColumnWiths);

				panel.invalidateFixedTable();
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
				((NuclosCollectController<Clct>) clctctl).refreshResult(lstObserver);
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
			final List<CollectableEntityField> lstAvailableObjects, final List<CollectableEntityField> lstSelectedObjects, 
			final Set<CollectableEntityField> stFixedObjects) 
	{
		assert clctctl == getCollectController() && clctctl.getFields() == getFields();
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
				@SuppressWarnings("unchecked")
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
					if (!collNewlySelected.isEmpty() && !((NuclosCollectController<Clct>)clctctl).getCollectablesInResultAreAlwaysComplete()) {
						// refresh the result:
						refreshResult(clctctl);
					} else {
						panel.setVisibleTable(true);
					}

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelRow != -1) {
						tblResult.setRowSelectionInterval(iSelRow, iSelRow);
					}

					// restore the widths of the still present columns
					panel.restoreColumnWidths(lstSelectedNew, mpWidths);

					// write preferences after column width was restored
					writeFieldWidthsToPreferences(clctctl.getPreferences());

					panel.invalidateFixedTable();

					panel.setupTableCellRenderers(tblResult);
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
					((NuclosCollectController<Clct>) clctctl).refreshResult(lstObserver);
				}

			});
	}

	@Override
	protected void setModel(CollectableTableModel<Clct> tblmodel, final CollectableEntity clcte, final CollectController<Clct> clctctl) {
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
	             clctctl.cmdRefreshResult();
	          }
	       });

		removeColumnsFromFixedTable();
		removeColumnsFromResultTable();

		setFixedTable();
		TableUtils.setOptimalColumnWidths(fixedTable);
		
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
					renderComp.setBackground(Color.LIGHT_GRAY);
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
	@SuppressWarnings("unchecked")
	@Override
	public void cmdSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl) {
		assert clctctl == getCollectController() && clctctl.getFields() == getFields();
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();

		final NuclosCollectController<Clct> nucleusctl = (NuclosCollectController<Clct>) clctctl;
		final SelectFixedColumnsController ctl = new PivotController(clctctl.getFrame(), new PivotPanel(), 
				(GenericObjectResultController) nucleusctl.getResultController());
		final List<CollectableEntityField> lstAvailable = fields.getAvailableFields();
		final List<CollectableEntityField> lstSelected = fields.getSelectedFields();
		final ChoiceEntityFieldList ro = new ChoiceEntityFieldList(panel.getFixedColumns());
		ro.set(lstAvailable, lstSelected, nucleusctl.getResultController().getCollectableEntityFieldComparator());

		// TODO: What the hell is the side effect of this? (Thomas Pasch)
		panel.getVisibleColumnWidth(lstSelected);

		final boolean bOK = ctl.run(ro,  
				CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));

		if (bOK) {
			setSelectColumns(fields, clctctl, ctl.getAvailableObjects(), ctl.getSelectedObjects(), ctl.getFixedObjects());
		}
	}

	@Override
	protected void cmdAddColumn(ChoiceEntityFieldList fields, TableColumn columnBefore, String sFieldNameToAdd) throws CommonBusinessException {
		super.cmdAddColumn(fields, columnBefore, sFieldNameToAdd);
		
		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		final JTable table = panel.getResultTable();
		final JTable fixedTable = panel.getFixedResultTable();
		
		final TableColumnModel columnmodelVariable = table.getColumnModel();
		final TableColumnModel columnmodelFixed = fixedTable.getColumnModel();
		adjustColumnModels(fields.getSelectedFields(), columnmodelVariable, columnmodelFixed);
	}

	@Override
	protected void cmdRemoveColumn(ChoiceEntityFieldList fields, CollectableEntityField clctef, CollectController<Clct> ctl) {
		super.cmdRemoveColumn(fields, clctef, ctl);

		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		panel.getFixedColumns().remove(clctef);
//		final TableColumnModel columnmodelVariable = getResultTable().getColumnModel();
//		final TableColumnModel columnmodelFixed = tblFixedResult.getColumnModel();
//		adjustColumnModels(fields.getSelectedFields(), columnmodelVariable, columnmodelFixed);

		List<CollectableEntityField> lstAvailableFields = new ArrayList<CollectableEntityField>(fields.getAvailableFields());
		//Collections.copy(lstAvailableFields, (List<CollectableEntityField>)fields.getAvailableFields());
		List<CollectableEntityField> lstSelectedFields = new ArrayList<CollectableEntityField>(fields.getSelectedFields());
		//Collections.copy(lstSelectedFields, (List<CollectableEntityField>)fields.getSelectedFields());
		setSelectColumns(fields, ctl, lstAvailableFields, lstSelectedFields, panel.getFixedColumns());
	}

	@Override
	protected void writeFieldWidthsToPreferences(Preferences preferences) throws PreferencesException {
		super.writeFieldWidthsToPreferences(preferences);

		final NuclosResultPanel<Clct> panel = getNuclosResultPanel();
		PreferencesUtils.putIntegerList(preferences, NuclosResultPanel.PREFS_NODE_FIXEDFIELDS_WIDTHS, 
				CollectableTableHelper.getColumnWidths(panel.getFixedResultTable()));
	}

	private NuclosResultPanel<Clct> getNuclosResultPanel() {
		return (NuclosResultPanel<Clct>) getCollectController().getResultPanel();
	}

}

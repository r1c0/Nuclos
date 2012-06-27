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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.KeyBinding;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.event.TableColumnModelAdapter;
import org.nuclos.client.ui.table.NuclosTableRowSorter;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription.TasklistPreferences;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Controller for <code>DynamicTaskView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	thomas.schiffmann
 * @version 01.00.00
 */
public class DynamicTaskController extends RefreshableTaskController {

	private static final Logger LOG = Logger.getLogger(DynamicTaskController.class);
	
	//
	
	// Spring injection
	
	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	private WorkspaceUtils workspaceUtils;
	
	private MainFrame mainFrame;
	
	// end of Spring injection

	private Map<Integer, DynamicTaskView> views;

	DynamicTaskController() {
		super();
		views= new HashMap<Integer, DynamicTaskView>();
	}
	
	@Autowired
	final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
	}
	
	@Autowired
	final void setWorkspaceUtils(WorkspaceUtils workspaceUtils) {
		this.workspaceUtils = workspaceUtils;
	}
	
	@Autowired
	final void setMainFrame(@Value("#{mainFrameSpringComponent.mainFrame}") MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public DynamicTaskView newDynamicTaskView(TasklistDefinition def, DynamicTasklistVO dtl) {
		final DynamicTaskView taskview = new DynamicTaskView(def, dtl);
		taskview.init();
		views.put(dtl.getId(), taskview);
		refresh(taskview, true);

		setActions(taskview);
		setRenderers(taskview);
		setColumnModelListener(taskview);
		setPopupMenuListener(taskview);

		KeyBinding keybinding = KeyBindingProvider.REFRESH;
		taskview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		taskview.getActionMap().put(keybinding.getKey(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh(taskview, false);
			}
		});

		return taskview;
	}

	private void setActions(final DynamicTaskView view) {
		super.addRefreshIntervalActions(view);

		//add mouse listener for double click in table:
		view.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					cmdShowDetails(view);
				}
			}
		});

		view.getRefreshButton().setAction(new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdRefresh(view);
			}
		});
	}

	private void setRenderers(DynamicTaskView view) {
		view.getTable().setRowHeight(SubForm.MIN_ROWHEIGHT);
		view.getTable().setTableHeader(new TaskViewTableHeader(view.getTable().getColumnModel()));
	}

	private void setPopupMenuListener(final DynamicTaskView taskview){
		taskview.getTable().getTableHeader().addMouseListener(new MouseAdapter() {
			@SuppressWarnings("serial")
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu pop = new JPopupMenu();

					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) && mainFrame.getWorkspace().isAssigned()) {
						final JMenuItem miPublishColumns = new JMenuItem(new AbstractAction(getSpringLocaleDelegate().getMessage(
								"DetailsSubFormController.4", "Spalten in Vorlage publizieren"), 
								Icons.getInstance().getIconRedo16()) {
							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									preferencesFacadeRemote.publishTaskListPreferences(
											mainFrame.getWorkspace(), 
											getTasklistPreferences(taskview));
								} catch (CommonBusinessException e1) {
									Errors.getInstance().showExceptionDialog(Main.getInstance().getMainController().getTaskController().getTabFor(taskview), e1);
								}
							}
						});
						pop.add(miPublishColumns);
					}
					
					JMenuItem miRestoreColumns = new JMenuItem(new AbstractAction(
							getSpringLocaleDelegate().getMessage("DetailsSubFormController.3", "Alle Spalten auf Vorlage zurücksetzen"), 
							Icons.getInstance().getIconUndo16()) {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								workspaceUtils.restoreTasklistPreferences(getTasklistPreferences(taskview));
								refresh(taskview, true);
							} 
							catch (CommonBusinessException e1) {
								Errors.getInstance().showExceptionDialog(Main.getInstance().getMainController().getTaskController().getTabFor(taskview), e1);
							}
						}
					});
					pop.add(miRestoreColumns);
					
					pop.setLocation(e.getLocationOnScreen());
					pop.show(taskview.getTable(), e.getX(), e.getY());
				}
			}

		});
	}

	private void cmdRefresh(final DynamicTaskView gotaskview) {
		UIUtils.runCommand(gotaskview, new CommonRunnable() {
			@Override
            public void run() {
				refresh(gotaskview, false);
			}
		});
	}

	private void cmdPrint(final DynamicTaskView gotaskview) {
		UIUtils.runCommand(gotaskview, new CommonRunnable() {
			@Override
            public void run() {
				print(gotaskview);
			}
		});
	}

	void refresh(final DynamicTaskView taskview, final boolean fromPreferences) {
		DynamicTasklistVO dtl = taskview.getDynamicTasklist();
		try {
			ResultVO vo = DatasourceDelegate.getInstance().getDynamicTasklistData(dtl.getId());
			if (fromPreferences) {
				TableModel mdl = new DynamicTaskTableModel(taskview.getDef(), vo, getColumnOrderFromPreferences(taskview));
				final NuclosTableRowSorter<TableModel> rs = new NuclosTableRowSorter<TableModel>(mdl);
				rs.addRowSorterListener(new RowSorterListener() {
					@Override
					public void sorterChanged(RowSorterEvent e) {
						storeSortOrder(taskview);
					}
				});
				taskview.getTable().setRowSorter(rs);
				taskview.getTable().setModel(mdl);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						List<SortKey> sorting = getSortKeys(taskview);
						rs.setSortKeys(sorting);
					}
				});
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Map<String, Integer> widths = getColumnWidthsFromPreferences(taskview);
						for (int iColumn = 0; iColumn < taskview.getTable().getColumnModel().getColumnCount(); iColumn++) {
							final String name = taskview.getTable().getColumnName(iColumn);
							final int iPreferredCellWidth;
							if (widths.containsKey(name)) {
								// known column
								iPreferredCellWidth = widths.get(name);
							} else {
								// new column
								iPreferredCellWidth = TableUtils.getPreferredColumnWidth(taskview.getTable(), iColumn, 50, TableUtils.TABLE_INSETS);
							}
							TableColumn column = taskview.getTable().getColumn(name);
							column.setPreferredWidth(iPreferredCellWidth);
							column.setWidth(iPreferredCellWidth);
						}
					}
				});
			}
			else {
				DynamicTaskTableModel mdl = (DynamicTaskTableModel) taskview.getTable().getModel();
				mdl.setData(vo);				
			}
		}
		catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(taskview, e);
		}
	}

	void print(DynamicTaskView gotaskview) {
		try {
			new ReportController(getTabbedPane().getComponentPanel()).export(gotaskview.getTable(), null);
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), ex);
		}
	}

	private void cmdShowDetails(final DynamicTaskView view) {
		JTable table = view.getTable();
		
		String entity = view.getDef().getTaskEntity();
		String idfield = view.getDef().getDynamicTasklistIdFieldname();
		String entityfield = view.getDef().getDynamicTasklistEntityFieldname();
		if (idfield != null && (entityfield != null || entity != null)) {
			if (table.getModel() instanceof DynamicTaskTableModel) {
				DynamicTaskTableModel model = (DynamicTaskTableModel) table.getModel();

				int[] selection = table.getSelectedRows();
				for (int i : selection) {
					final Object oId = model.getValueByField(i, idfield);
					final Long id;
					if (oId instanceof Double) {
						id = ((Double)oId).longValue();
					}
					else {
						id = IdUtils.toLongId(oId);
					}
					final String _entity = entityfield != null ? (String) model.getValueByField(i, entityfield) : entity;
					UIUtils.runCommandLater(Main.getInstance().getMainController().getTaskController().getTabFor(view), new Runnable() {
						@Override
						public void run() {
							try {
								Main.getInstance().getMainController().showDetails(_entity, id);
							}
							catch (CommonBusinessException e) {
								Errors.getInstance().showExceptionDialog(view, e);
							}
						};
					});
				}
			}
		}
	}

	@Override
	public ScheduledRefreshable getSingleScheduledRefreshableView() {
		throw new IllegalStateException();
	}

	@Override
	public void refreshScheduled(ScheduledRefreshable sRefreshable) {
		if (sRefreshable instanceof DynamicTaskView){
			refresh((DynamicTaskView)sRefreshable, false);
		}
		else {
			throw new IllegalStateException();
		}
	}
	
	private void setColumnModelListener(final DynamicTaskView view) {
		view.getTable().getColumnModel().addColumnModelListener(new TableColumnModelAdapter() {
			@Override
			public void columnMoved(TableColumnModelEvent ev) {				
				if (ev.getFromIndex() != ev.getToIndex()) {
					storePreferences(view);
				}
			}

			@Override
			public void columnMarginChanged(ChangeEvent ev) {
				storePreferences(view);
			}
		});
	}
	
	private void storePreferences(DynamicTaskView view) {
		TasklistPreferences prefs = getTasklistPreferences(view);
		List<String> fields = new ArrayList<String>();
		List<Integer> widths = new ArrayList<Integer>();
		for (int i = 0; i < view.getTable().getColumnModel().getColumnCount(); i++) {
			fields.add(view.getTable().getColumnName(i));
			TableColumn tc = view.getTable().getColumnModel().getColumn(i);
			widths.add(tc.getWidth());
		}
		workspaceUtils.setColumnPreferences(prefs.getTablePreferences(), fields, widths);
	}
	
	private void storeSortOrder(final DynamicTaskView view) {
		TasklistPreferences prefs = getTasklistPreferences(view);
		workspaceUtils.setSortKeys(prefs.getTablePreferences(), view.getTable().getRowSorter().getSortKeys(), 
				new WorkspaceUtils.IColumnNameResolver() {
					@Override
					public String getColumnName(int iColumn) {
						return view.getTable().getColumnName(iColumn);
					}
		});
	}
	
	private List<String> getColumnOrderFromPreferences(DynamicTaskView view) {
		TasklistPreferences prefs = getTasklistPreferences(view);
		return workspaceUtils.getSelectedColumns(prefs.getTablePreferences());
	}
	
	private Map<String, Integer> getColumnWidthsFromPreferences(DynamicTaskView view) {
		TasklistPreferences prefs = getTasklistPreferences(view);
		return workspaceUtils.getColumnWidthsMap(prefs.getTablePreferences());
	}
	
	private List<SortKey> getSortKeys(final DynamicTaskView view) {
		TasklistPreferences prefs = getTasklistPreferences(view);
		return workspaceUtils.getSortKeys(prefs.getTablePreferences(), new WorkspaceUtils.IColumnIndexRecolver() {
			@Override
			public int getColumnIndex(String columnIdentifier) {
				return view.getTable().getColumnModel().getColumnIndex(columnIdentifier);
			}
		});
	}
	
	private TasklistPreferences getTasklistPreferences(DynamicTaskView view) {
		return mainFrame.getWorkspaceDescription().getTasklistPreferences(TasklistPreferences.DYNAMIC, view.getDef().getName());
	}
}



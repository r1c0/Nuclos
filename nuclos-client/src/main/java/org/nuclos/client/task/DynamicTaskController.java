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
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.nuclos.client.common.KeyBinding;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.ResultVO;

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

	private Map<Integer, DynamicTaskView> views;

	DynamicTaskController() {
		super();
		views= new HashMap<Integer, DynamicTaskView>();
	}

	public DynamicTaskView newDynamicTaskView(TasklistDefinition def, DynamicTasklistVO dtl) {
		final DynamicTaskView taskview = new DynamicTaskView(def, dtl);
		taskview.init();
		views.put(dtl.getId(), taskview);
		refresh(taskview);

		setActions(taskview);
		setPopupMenuListener(taskview);
		setRenderers(taskview);

		KeyBinding keybinding = KeyBindingProvider.REFRESH;
		taskview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		taskview.getActionMap().put(keybinding.getKey(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh(taskview);
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

	}

	private void cmdRefresh(final DynamicTaskView gotaskview) {
		UIUtils.runCommand(gotaskview, new CommonRunnable() {
			@Override
            public void run() {
				refresh(gotaskview);
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

	void refresh(DynamicTaskView taskview) {
		DynamicTasklistVO dtl = taskview.getDynamicTasklist();
		try {
			ResultVO vo = DatasourceDelegate.getInstance().getDynamicTasklistData(dtl.getId());
			TableModel mdl = new DynamicTaskTableModel(taskview.getDef(), vo);
			taskview.getTable().setRowSorter(new TableRowSorter<TableModel>(mdl));
			taskview.getTable().setModel(mdl);
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
			refresh((DynamicTaskView)sRefreshable);
		}
		else {
			throw new IllegalStateException();
		}
	}
}


